package ru.etu.astamir.common.collections;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import ru.etu.astamir.model.Entity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Artem Mon'ko
 */
public class EntitySet<V extends Entity> extends AbstractEntitySet<V> implements Serializable, Cloneable {
    private Map<String, V> backingMap = new LinkedHashMap<>();

    public EntitySet(Iterable<V> entities) {
        super();
        addAll(CollectionUtils.filterNullElements(entities));
    }

    public EntitySet() {
    }

    @Override
    protected Map<String, V> getMap() {
        return backingMap;
    }

    public static <V extends Entity> EntitySet<V> create(V... entities) {
        return new EntitySet<>(Arrays.asList(entities));
    }



    public static <V extends Entity> EntitySet<V> clone(Iterable<V> entities) {
        return new EntitySet<V>(Iterables.transform(Iterables.filter(entities, Predicates.notNull()), new Function<V, V>() {
            @Override
            public V apply(V input) {
                return (V) input.clone();
            }
        }));
    }

    @Override
    public EntitySet<V> clone() {
        return clone(this); // clone every element
    }
}
