package ru.etu.astamir.common.collections;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Orientation;
import ru.etu.astamir.model.Entity;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.contacts.Contact;
import ru.etu.astamir.model.wires.Wire;

import java.io.Serializable;
import java.util.*;

/**
 * @author Artem Mon'ko
 */
public class EntitySet<V extends Entity> extends AbstractEntitySet<V> implements Serializable, Cloneable {
    private Map<String, V> backing_map = new LinkedHashMap<>();

    public EntitySet(Iterable<V> entities) {
        super();
        addAll(CollectionUtils.filterNullElements(entities));
    }

    public EntitySet() {
    }

    @Override
    protected Map<String, V> getMap() {
        return backing_map;
    }

    public static <V extends Entity> EntitySet<V> create(V... entities) {
        return new EntitySet<V>(Arrays.asList(entities));
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
