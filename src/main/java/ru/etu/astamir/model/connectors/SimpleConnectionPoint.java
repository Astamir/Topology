package ru.etu.astamir.model.connectors;

import com.google.common.base.Preconditions;
import ru.etu.astamir.model.Entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

/**
 * Некоторая виртуальная точка к которой подключены различные элементы.
 */
public class SimpleConnectionPoint extends Entity implements ConnectionPoint, Serializable, Cloneable {
    private Collection<String> connected_elements = new HashSet<>();

    public SimpleConnectionPoint() {
    }

    public SimpleConnectionPoint(Collection<String> connected_elements) {
        this.connected_elements = connected_elements;
    }

    public void setConnectedElements(Collection<String> connected_elements) {
        this.connected_elements = connected_elements;
    }

    public void addConnectedElement(String name) {
        this.connected_elements.add(Preconditions.checkNotNull(name));
    }

    public void removeConnectedElement(String name) {
        this.connected_elements.remove(Preconditions.checkNotNull(name));
    }

    @Override
    public Collection<String> getConnectedNames() {
        return connected_elements;
    }

    @Override
    public SimpleConnectionPoint clone() {
        SimpleConnectionPoint clone = (SimpleConnectionPoint) super.clone();
        clone.connected_elements = new HashSet<>(connected_elements);

        return clone;
    }

    @Override
    public boolean isSimple() {
        return true;
    }
}
