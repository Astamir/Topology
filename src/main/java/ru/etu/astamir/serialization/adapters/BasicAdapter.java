package ru.etu.astamir.serialization.adapters;

import com.google.common.collect.Lists;
import com.google.common.primitives.Primitives;
import org.jdom2.Document;
import org.w3c.dom.Node;
import ru.etu.astamir.common.reflect.ReflectUtils;
import ru.etu.astamir.model.Entity;
import ru.etu.astamir.model.exceptions.UnexpectedException;
import ru.etu.astamir.serialization.Attribute;
import ru.etu.astamir.serialization.AttributeAdapter;
import ru.etu.astamir.serialization.AttributeContainer;
import ru.etu.astamir.serialization.AttributeFactory;
import ru.etu.astamir.serialization.ComplexAttribute;
import ru.etu.astamir.serialization.IgnoreAttribute;
import ru.etu.astamir.serialization.JAXBUtils;
import ru.etu.astamir.serialization.LookIntoAttribute;
import ru.etu.astamir.serialization.xml.XMLAttributeParser;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Рефлексивный получатель аттрибутов. Работает сомнительно, и мб где-нить сломается.
 * Использовать стоит в том случае, если не нашлось нормального адаптера в контейнере.
 *
 * @param <E>
 */
// todo implement for maps
@SuppressWarnings("unchecked")
public class BasicAdapter<E extends Entity> implements AttributeAdapter<E> {
    Class<? extends Entity> clazz;

    public BasicAdapter(Class<? extends Entity> clazz) {
        this.clazz = clazz;
    }

    @Override
    public List<Attribute> getAttributes(E entity) {
        List<Attribute> attributes = new ArrayList<>();
        for (Field field : ReflectUtils.getFieldsUpTo(entity.getClass(), Entity.class)) {
            if (Modifier.isStatic(field.getModifiers())) { // skip static fields
                continue;
            }
            String name = field.getName();
            field.setAccessible(true);
            Attribute attribute = null;
            try {
                if (field.getAnnotation(IgnoreAttribute.class) != null) {
                    continue;
                }

                if (Collection.class.isAssignableFrom(field.getType())) {
                    Class<?> collectionClass = ReflectUtils.getCollectionType(field);
                    if (!Entity.class.isAssignableFrom(collectionClass)) { // Collection of something else that entity
                        Optional<AttributeAdapter<Object>> adapter = AttributeContainer.getInstance().getAdapterSafely(collectionClass);
                        if (adapter.isPresent()) {
                            attributes.addAll(adapter.get().getAttributes(field.get(entity))); // trying to use our adapters first
                        } else { // we're now in deep shit. ok lets try to ask jaxb for help.
                            try {
                                JAXBContext context = JAXBContext.newInstance(collectionClass);
                                Marshaller marshaller = context.createMarshaller();
                                Node node = marshaller.getNode(field.get(entity));
                                Document document = JAXBUtils.jaxbTojdom(node.getOwnerDocument());
                                attributes.addAll(XMLAttributeParser.fromDocument(document));
                            } catch (JAXBException e) {
                                throw new UnexpectedException("We came to JAXB for help and it let us down");
                            }
                        }
                    } else {
                        if (field.isAnnotationPresent(LookIntoAttribute.class)) {
                            Optional<AttributeAdapter<Object>> adapter = AttributeContainer.getInstance().getAdapterSafely(collectionClass);
                            if (adapter.isPresent()) {
                                AttributeAdapter ad = adapter.get();
                                ComplexAttribute complexAttribute = AttributeFactory.createComplexAttribute(name);
                                Collection collection = (Collection) field.get(entity);
                                for (Object o : collection) {
                                    ComplexAttribute atr = AttributeFactory.createComplexAttribute(name.substring(0, name.length() - 1));
                                    List attributes1 = ad.getAttributes(o);
                                    atr.addAllAttributes(attributes1);

                                    complexAttribute.addAttribute(atr);
                                }

                                attributes.add(complexAttribute);
                            }
                        } else {
                            Collection<Attribute> atrs = new ArrayList<>();
                            Collection<? extends Entity> entities = (Collection<? extends Entity>) field.get(entity);
                            if (entities == null) { // weird but valid case
                                continue;
                            }
                            for (Entity e : entities) {
                                atrs.add(AttributeFactory.createReferenceAttribute(name.substring(0, name.length() - 1), e.getName()));
                            }

                            attribute = AttributeFactory.createAttribute(name, atrs);
                        }
                    }

                } else {
                    Object entValue = field.get(entity);
                    if (entValue == null) { // lets skip null values
                        continue;
                    }

                    if (field.getType().isPrimitive() || field.getType().isAssignableFrom(String.class)) {
                        attribute = AttributeFactory.createAttribute(name, entValue.toString(), field.isAnnotationPresent(XmlID.class) || field.isAnnotationPresent(XmlAttribute.class));
                    } else if (Entity.class.isAssignableFrom(field.getType())) {
                        if (field.isAnnotationPresent(LookIntoAttribute.class)) {
                            Collection<Attribute> attributesFor = AttributeContainer.getInstance().getAttributesFor(entValue);
                            attribute = AttributeFactory.createAttribute(name, attributesFor);
                        } else {
                            Entity ent = (Entity) entValue;
                            attribute = AttributeFactory.createReferenceAttribute(field.getName(), ent.getName());
                        }
                    } else {
                        List<Attribute> attributesFor = new ArrayList<>(AttributeContainer.getInstance().getAttributesFor(entValue));
                        if (attributesFor.isEmpty()) {
                            continue;
                        }

                        if (attributesFor.size() == 1 && attributesFor.get(0).isSimple()) {
                            attribute = attributesFor.get(0);
                        } else {
                            attribute = AttributeFactory.createAttribute(name, attributesFor);
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                throw new UnexpectedException(name + " field is inaccessible, which is weird since we made it accessible", e);
            }

            if (attribute != null) { // somehow we got null attribute, we wont add it here
                attributes.add(attribute);
            }
        }

        return attributes;
    }

    @Override
    public E getEntity(Collection<Attribute> attributes) {
        E entity = null;
        try {
            Constructor<? extends Entity> declaredConstructor = clazz.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            entity = (E) declaredConstructor.newInstance();
            for (Attribute attribute : attributes) {
                String name = attribute.getName();
                Optional<Field> fieldO = ReflectUtils.findField(clazz, name);
                if (!fieldO.isPresent()) {
                    continue;
                }
                Field field = fieldO.get();
                field.setAccessible(true);
                if (attribute.isSimple()) {
                    Class<?> type = field.getType().isPrimitive() ? Primitives.wrap(field.getType()) : field.getType();
                    if (Number.class.isAssignableFrom(type)) { // we have to somehow parse it
                        field.set(entity, Primitives.wrap(type).cast(ReflectUtils.parse((String) attribute.getValue(), type)));
                    } else if (Boolean.class.isAssignableFrom(type)) {
                        field.set(entity, Primitives.wrap(type).cast(Boolean.parseBoolean((String) attribute.getValue())));
                    } else {
                        field.set(entity, attribute.getValue());
                    }
                } else {
                    if (Collection.class.isAssignableFrom(field.getType()) && field.isAnnotationPresent(LookIntoAttribute.class)) {
                        Class<?> collectionType = ReflectUtils.getCollectionType(field);
                        Optional<AttributeAdapter<Object>> adapter = AttributeContainer.getAdapterSafely(collectionType);
                        if (!adapter.isPresent()) {
                            throw new UnexpectedException("no adapter for " + field.getType());
                        }
                        List<Object> entities = Lists.newArrayList();
                        for (Attribute attr : ((ComplexAttribute) attribute).getValue()) {
                            List<Attribute> entityAttributes = attr.isSimple() ? Lists.newArrayList(attr) : ((ComplexAttribute) attr).getValue();
                            entities.add(adapter.get().getEntity(entityAttributes));
                        }

                        field.set(entity, entities);
                    } else {
                        Optional<AttributeAdapter<Object>> adapter = AttributeContainer.getAdapterSafely(field.getType());
                        if (!adapter.isPresent()) {
                            throw new UnexpectedException("no adapter for " + field.getType());
                        }

                        field.set(entity, adapter.get().getEntity(((ComplexAttribute) attribute).getValue()));
                    }
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return entity;
    }
}
