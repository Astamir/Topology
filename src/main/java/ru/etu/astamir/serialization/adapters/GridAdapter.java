package ru.etu.astamir.serialization.adapters;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.reflections.Reflections;
import ru.etu.astamir.common.reflect.ReflectUtils;
import ru.etu.astamir.compression.grid.Grid;
import ru.etu.astamir.compression.grid.VirtualGrid;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.exceptions.UnexpectedException;
import ru.etu.astamir.serialization.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author Artem Mon'ko
 */
public class GridAdapter implements AttributeAdapter<Grid> {
    static final Map<String, Class<?>> grids = new HashMap<>();

    {
        Reflections collect = new Reflections("ru.etu.astamir");
        Set<Class<? extends Grid>> subTypesOfGrid = collect.getSubTypesOf(Grid.class);
        for (Class<?> aClass : subTypesOfGrid) {
            grids.put(aClass.getSimpleName(), aClass);
        }
    }

    @Override
    public List<Attribute> getAttributes(Grid entity) {
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(AttributeFactory.createLocalAttribute("class", entity.getClass().getSimpleName()));

        for (TopologyElement element : entity.getAllElements()) {
            ComplexAttribute elementAttribute = AttributeFactory.createComplexAttribute(element.getClass().getSimpleName());
            elementAttribute.addAttribute(AttributeFactory.createAttribute("coordinates", AttributeContainer.toAttributes("coordinate", element.getCoordinates(), Point.class, false)));

            Optional<AttributeAdapter<Object>> adapter = AttributeContainer.getInstance().getAdapterSafely(element.getClass());
            if (adapter.isPresent()) {
                elementAttribute.addAllAttributes(adapter.get().getAttributes(element));
            } else {
                // lets try basic then
                elementAttribute.addAllAttributes(new BasicAdapter(element.getClass()).getAttributes(element));
            }

            attributes.add(elementAttribute);
        }

        return attributes;
    }

    @Override
    public Grid getEntity(Collection<Attribute> attributes) {
        Grid grid = null;

        Optional<Attribute> classAttribute = AttributeContainer.findAttribute(attributes, "class");
        if (classAttribute.isPresent()) {
            Optional<Class> cl = ReflectUtils.forSimpleName((String) classAttribute.get().getValue());
            if (cl.isPresent()) {
                try {
                    Constructor voidConstructor = cl.get().getDeclaredConstructor();
                    voidConstructor.setAccessible(true);
                    grid = (Grid) voidConstructor.newInstance();
                } catch (NoSuchMethodException e) {
                    throw new UnexpectedException("We have no default constructor for " + cl.get());
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    throw new UnexpectedException("Bullshit, we made it accessible");
                }
            }
        } else {
            grid = new VirtualGrid();
        }

        if (grid == null) {
            throw new UnexpectedException("Weird we was not able to create grid");
        }
        for (Attribute attribute : attributes) {
            Optional<Class> cl = ReflectUtils.forSimpleName(attribute.getName());
            if (!cl.isPresent()) {
                //throw new UnexpectedException("We have no class for simple id " + attribute.getName());
                continue;
            }

            if (!TopologyElement.class.isAssignableFrom(cl.get())) {
                continue; // for now
            }

            Optional<AttributeAdapter<Object>> adapter = AttributeContainer.getAdapterSafely(cl.get());
            if (!adapter.isPresent()) {
                throw new UnexpectedException("No adapter for " + cl.get().getName() + ", might wanna fix it later");
            }

            TopologyElement element = (TopologyElement) adapter.get().getEntity(((ComplexAttribute)attribute).getValue());
            grid.addElement(element);
        }



        // try to resolve some references
        resolveReferences(grid.getAllElements(), (ComplexAttribute) AttributeFactory.createAttribute("grid", attributes));

        return grid;
    }

    private void resolveReferences(Collection<TopologyElement> parsed, final ComplexAttribute complexAttribute) {
        for (final Attribute attribute : complexAttribute.getValue()) {
            if (attribute.isSimple()) {
                if (attribute.isReference()) {
                    Optional<TopologyElement> referenceParent = Iterables.tryFind(parsed, new Predicate<TopologyElement>() {
                        @Override
                        public boolean apply(TopologyElement input) {
                            Optional<Attribute> name = AttributeContainer.findAttribute(complexAttribute, "id");
                            return name.isPresent() && input.getName().equals(name.get().getValue());
                        }
                    });

                    Optional<TopologyElement> referenceTarget = Iterables.tryFind(parsed, new Predicate<TopologyElement>() {
                        @Override
                        public boolean apply(TopologyElement input) {
                            return input.getName().equals(attribute.getValue());
                        }
                    });

                    if (referenceTarget.isPresent() && referenceParent.isPresent()) {
                        try {
                            Field ref = referenceParent.get().getClass().getDeclaredField(attribute.getName().replaceAll("_ref", ""));
                            ref.setAccessible(true);
                            ref.set(referenceParent.get(), referenceTarget.get());
                        } catch (NoSuchFieldException e) {
                            throw new UnexpectedException("no field for attribute " + attribute.getName(), e);
                        } catch (IllegalAccessException e) {
                            throw new UnexpectedException("We made it accessible", e);
                        }
                    }
                }
            } else {
                resolveReferences(parsed, (ComplexAttribute) attribute);
            }
        }
    }
}
