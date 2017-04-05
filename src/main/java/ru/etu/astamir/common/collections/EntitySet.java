package ru.etu.astamir.common.collections;

import ru.etu.astamir.model.Entity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Artem Mon'ko
 */
public class EntitySet<V extends Entity> extends AbstractEntitySet<V> implements Serializable, Cloneable {
    private final Map<String, V> backingMap = new LinkedHashMap<>();

    public EntitySet(Iterable<V> entities) {
        super();
        addAll(CollectionUtils.filterNullElements(entities));
    }

    public EntitySet() {
    }

    public static <V extends Entity> EntitySet<V> create(V... entities) {
        return new EntitySet<>(Arrays.asList(entities));
    }

    public static <V extends Entity> EntitySet<V> clone(Iterable<V> entities) {
        return new EntitySet<V>(StreamSupport.stream(entities.spliterator(), false)
                .filter(Objects::nonNull)
                .map(e -> (V) e.clone())
                .collect(Collectors.toList()));
    }

    @Override
    protected Map<String, V> getMap() {
        return backingMap;
    }

    @Override
    public EntitySet<V> clone() {
        return clone(this); // clone every element
    }
}
