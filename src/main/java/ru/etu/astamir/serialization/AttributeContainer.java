package ru.etu.astamir.serialization;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import org.jdom2.Document;
import org.jdom2.Element;
import ru.etu.astamir.compression.grid.Grid;
import ru.etu.astamir.compression.grid.VirtualGrid;
import ru.etu.astamir.geom.common.*;
import ru.etu.astamir.model.*;
import ru.etu.astamir.model.contacts.Contact;
import ru.etu.astamir.model.contacts.ContactType;
import ru.etu.astamir.model.exceptions.UnexpectedException;
import ru.etu.astamir.model.wires.SimpleWire;
import ru.etu.astamir.model.wires.Wire;
import ru.etu.astamir.serialization.adapters.*;

import java.util.*;

/**
 * @author Artem Mon'ko
 */
// TODO
public class AttributeContainer {
    private static final Map<Class<?>, AttributeAdapter<?>> cache = new HashMap<>();
    private static final Map<Class<?>, AttributeAdapter<?>> substitutes = new HashMap<>();

    private static final Map<String, Class<?>> names = new HashMap<>();


    static {
        cache.put(Contact.class, new BasicAdapter<Contact>(Contact.class));
        cache.put(Point.class, new PointAdapter());
        cache.put(Edge.class, new EdgeAdapter());
        cache.put(TopologyLayer.class, new TopologyLayerAdapter());
        cache.put(Material.class, new EnumAdapter<>(Material.class));
        cache.put(ConductionType.class, new EnumAdapter<>(ConductionType.class));
        cache.put(Wire.class, new BasicAdapter<>(Wire.class));
        cache.put(Orientation.class, new EnumAdapter<>(Orientation.class));
        cache.put(Grid.class, new GridAdapter());
        cache.put(VirtualGrid.class, new GridAdapter());
        cache.put(ContactType.class, new EnumAdapter<>(ContactType.class));
        cache.put(SimpleWire.class, new BasicAdapter<>(SimpleWire.class));
        cache.put(Polygon.class, new PolygonAdapter());

        substitutes.put(Rectangle.class, new PolygonAdapter());

        //---------

        names.put("point", Point.class);
    }

    private AttributeContainer() {
    }

    public static AttributeContainer getInstance() {
        return AttributeContainerHolder.INSTANCE;
    }

    private static class AttributeContainerHolder {
        private static final AttributeContainer INSTANCE = new AttributeContainer();
    }

    public static <V extends Entity> void addSubstituteParser(Class<V> clazz, AttributeAdapter<V> adapter) {
        if (!cache.containsKey(clazz)) {
            cache.put(clazz, adapter);
        }

        substitutes.put(clazz, adapter);
    }

    public static AttributeAdapter<?> getAdapterFor(Class<?> clazz, boolean useSubstitute) {
       // checkForAdapter(clazz);

        if (!cache.containsKey(clazz) && !substitutes.containsKey(clazz)) {
            return null;
        }

        if (useSubstitute && substitutes.containsKey(clazz)) {
            return substitutes.get(clazz);
        }

        if (cache.containsKey(clazz)) {
            return cache.get(clazz);
        } else if (substitutes.containsKey(clazz)) {
            return substitutes.get(clazz);
        }

        return null;
    }

    public static Optional<AttributeAdapter<Object>> getAdapterSafely(Class<?> clazz) {
        checkForAdapter(clazz);

        if (!cache.containsKey(clazz) && !substitutes.containsKey(clazz)) {
            return Optional.absent();
        }

        if (cache.containsKey(clazz)) {
            AttributeAdapter<Object> reference = (AttributeAdapter<Object>) cache.get(clazz);
            return Optional.of(reference);
        } else if (substitutes.containsKey(clazz)) {
            AttributeAdapter<Object> reference = (AttributeAdapter<Object>) substitutes.get(clazz);
            return Optional.of(reference);
        }

        return Optional.absent();
    }

    public static AttributeAdapter<?> getAdapterFor(Class<?> clazz) {
        return getAdapterFor(clazz, false);
    }

    // TODO MOVE THIS TO PARSERS
    public Element getElementFor(Entity entity) {
        // entity to attributes -> attributes to Element
        return null;
    }

    public Document getJDOMDocumentFor(Entity entity) {
        return null;
    }

    public org.w3c.dom.Document getXMLDocumentFor(Entity entity) {
        return null;
    }

    public String getCompactStringFor(Entity entity) {
        return null;
    }

    public String getXMLStringFor(Entity entity) {
        return null;
    }

    public String getJSONStringFor(Entity entity) {
        return null;
    }
    // TODO MOVE THIS TO PARSERS

    public static Collection<Attribute> getAttributesFor(Object entity) {
        Optional<AttributeAdapter<Object>> adapterFor = getAdapterSafely(entity.getClass());
        if (!adapterFor.isPresent()) {
            throw new UnexpectedException("no adapter for " + entity.getClass()); // TODO remove later;
        }

        return adapterFor.get().getAttributes(entity);
    }



    /* Static utility methods */

    public static Optional<Attribute> findAttribute(Attribute attribute, String name, boolean goDeep) {
        if (attribute.isSimple()) {
            NamePredicate namePredicate = new NamePredicate(name);
            if (namePredicate.apply(attribute)) {
                return Optional.of(attribute);
            }
        } else {
            ComplexAttribute complexAttribute = (ComplexAttribute) attribute;
            Optional<Attribute> found = findAttribute(complexAttribute.getValue(), name, false);
            if (!found.isPresent() && goDeep) {
                return findAttribute(complexAttribute.getValue(), name, true);
            }

            return found;
        }

        return Optional.absent();
    }

    public static Optional<Attribute> findAttribute(Attribute attribute, String name) {
        return findAttribute(attribute, name, false);
    }

    public static Optional<Attribute> findAttribute(Collection<Attribute> attributes, String name, boolean goDeep) {
        NamePredicate namePredicate = new NamePredicate(name);
        for (Attribute attribute : attributes) {
            if (namePredicate.apply(attribute)) {
                return Optional.of(attribute);
            }
        }

        // if we're here that means we didn't find anything, lets try to go deeper if we're allowed to
        if (goDeep) {
            for (Attribute attribute : attributes) {
                if (attribute instanceof ComplexAttribute) {
                    ComplexAttribute complexAttribute = (ComplexAttribute) attribute;
                    return findAttribute(complexAttribute.getValue(), name, true);
                }
            }
        }

        return Optional.absent();
    }

    public static Optional<Attribute> findAttribute(Collection<Attribute> attributes, String name) {
        return findAttribute(attributes, name, false);
    }

    public static List<Attribute> findAllAttribute(String name, Attribute attribute) {
        List<Attribute> result = new ArrayList<>();
        if (attribute.isSimple()) {
            result.add(attribute);
        } else {
            ComplexAttribute complexAttribute = (ComplexAttribute) attribute;
            for (Attribute atr : complexAttribute.getValue()) {
                if (atr.getName() != null && atr.getName().equalsIgnoreCase(name)) {
                    result.add(atr);
                }
            }
        }

        return result;
    }

    public static List<Attribute> findAllAttribute(String name, Collection<Attribute> attributes) {
        List<Attribute> result = new ArrayList<>();
        for (Attribute atr : attributes) {
            if (atr.getName() != null && atr.getName().equalsIgnoreCase(name)) {
                result.add(atr);
            }
        }

        return result;
    }

    public static <E> E getEntity(AttributeAdapter<E> adapter, Attribute attribute) {
        List<Attribute> attributes = new ArrayList<>();
        if (attribute.isSimple()) {
            attributes.add(attribute);
        } else {
            attributes.addAll(((ComplexAttribute) attribute).getValue());
        }

        return adapter.getEntity(attributes);
    }

    public static Collection<Attribute> getAttributesForTopologyElement(TopologyElement element) {
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(AttributeFactory.createAttribute("id", element.getName(), true));
        attributes.add(AttributeFactory.createAttribute("Material", element.getMaterial().name()));
        attributes.add(AttributeFactory.createAttribute("ConductionType", element.getConductionType().name()));
        TopologyLayerAdapter layerAdapter = (TopologyLayerAdapter) getAdapterFor(TopologyLayer.class);
        attributes.add(AttributeFactory.createAttribute("layer", layerAdapter.getAttributes(element.getLayer())));
        return attributes;
    }

    public static void setAttributesForTopologyElement(Collection<Attribute> attributes, TopologyElement element) {
        Optional<String> name = getAttributeValue(attributes, "id");
        if (name.isPresent()) {
            element.setName(name.get());
        }

        Optional<String> material = getAttributeValue(attributes, "Material");
        if (material.isPresent()) {
            element.setMaterial(Material.valueOf(material.get()));
        }

        Optional<String> conductionType = getAttributeValue(attributes, "ConductionType");
        if (conductionType.isPresent()) {
            element.setConductionType(ConductionType.valueOf(conductionType.get()));
        }

        TopologyLayerAdapter adapter = (TopologyLayerAdapter) getAdapterFor(TopologyLayer.class);
        TopologyLayer layer = adapter.getEntity(getComplexAttributeValue(attributes, "layer"));
        element.setLayer(layer);
    }

    public static Optional<String> getAttributeValue(Collection<Attribute> attributes, String name) {
        Optional<Attribute> attribute = findAttribute(attributes, name);
        if (attribute.isPresent()) {
            Attribute atr = attribute.get();
            if (atr.isSimple()) {
                return Optional.of((String) atr.getValue());
            }

            throw new UnexpectedException("Simple attribute is expected here, but complex one is found");
        }

        return Optional.absent();
    }

    public static Collection<Attribute> getComplexAttributeValue(Collection<Attribute> attributes, String name) {
        Optional<Attribute> atr = findAttribute(attributes, name, false);
        if (atr.isPresent()) {
            Attribute attribute = atr.get();
            if (attribute instanceof ComplexAttribute) {
                return ((ComplexAttribute) attribute).getValue();
            }

            throw new UnexpectedException("Complex attribute is expected here, but simple one is found");
        }

        return Collections.emptyList();
    }

    public static List<Attribute> toAttributes(String name, Collection<?> elements, Class<?> aClass, boolean useBasic) {
        Optional<AttributeAdapter<Object>> adapter = getAdapterSafely(aClass);
        List<Attribute> attributes = new ArrayList<>();
        AttributeAdapter actualAdapter = null;
        if (adapter.isPresent()) {
            actualAdapter = adapter.get();
        } else {
            checkForAdapter(aClass); // cheap try but still
            if (useBasic) {
                actualAdapter = new BasicAdapter(aClass);
            }
        }

        if (actualAdapter == null) {
            throw new UnexpectedException("no adapter for " + aClass.getSimpleName());
        }

        for (Object obj : elements) {
            attributes.add(AttributeFactory.createAttribute(name, actualAdapter.getAttributes(obj)));
        }


        return attributes;
    }

    private static void checkForAdapter(Class<?> elementClass) {
        for (Package p : Package.getPackages()) {
            String name = p.getName() + "." + elementClass.getSimpleName() + "Adapter";
            try {
                Class<?> aClass = Class.forName(name);
                if (AttributeAdapter.class.isAssignableFrom(aClass) && !cache.containsKey(elementClass) && !substitutes.containsKey(elementClass)) {
                    throw new UnexpectedException("WTF, we have adapter " + name + " not added to cache!!!");
                }
            } catch (ClassNotFoundException e) {
            }
        }
    }

    static class NamePredicate implements Predicate<Attribute> {
        final String name;

        public NamePredicate(String name) {
            this.name = name;
        }

        @Override
        public boolean apply(Attribute attribute) {
            String attributeName = attribute.getName();
            return attribute != null && attributeName.equals(name);
        }
    }
}
