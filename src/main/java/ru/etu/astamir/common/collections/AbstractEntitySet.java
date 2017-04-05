package ru.etu.astamir.common.collections;

import com.google.common.base.Preconditions;
import ru.etu.astamir.common.reflect.ReflectUtils;
import ru.etu.astamir.model.Entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public abstract class AbstractEntitySet<V extends Entity> implements Set<V>, Serializable, Cloneable {

    public AbstractEntitySet() {

    }

    protected abstract Map<String, V> getMap();

    @Override
    public int size() {
        return getMap().size();
    }

    @Override
    public boolean isEmpty() {
        return getMap().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Entity) {
            Entity entity = (Entity) o;
            return getMap().containsKey(entity.getName());
        } else if (o instanceof String) {
            return getMap().containsKey(o);
        }

        return false;
    }

    public boolean contains(String name) {
        return getMap().containsKey(name);
    }

    public V get(String name) {
        return getMap().get(name);
    }

    @Override
    public Iterator<V> iterator() {
        return getMap().values().iterator();
    }

    @Override
    public Object[] toArray() {
        return getMap().values().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return getMap().values().toArray(a);
    }

    @Override
    public boolean add(V v) {
        Preconditions.checkNotNull(v);
        return getMap().put(v.getName(), v) != null;
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Entity) {
            return removeByValue((Entity) o) != null;
        } else if (o instanceof String) {
            return removeByName((String) o) != null;
        }

        return false;
    }

    private V removeByValue(Entity v) {
        Preconditions.checkNotNull(v);
        return getMap().remove(v.getName());
    }

    private V removeByName(String name) {
        return getMap().remove(name);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return getMap().values().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends V> c) {
        boolean allAdded = true;
        for (V v : c) {
            allAdded &= add(v);
        }

        return allAdded;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return removeIf(v -> !c.contains(v));
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean somethingWasRemoved = false;
        for (Object o : c) {
            somethingWasRemoved |= remove(o);
        }
        return somethingWasRemoved;
    }


    @Override
    public void clear() {
        getMap().clear();
    }
}
