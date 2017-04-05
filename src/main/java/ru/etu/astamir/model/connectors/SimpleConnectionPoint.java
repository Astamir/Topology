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
    private Collection<String> connectedElements = new HashSet<>();

    public SimpleConnectionPoint() {
    }

    public SimpleConnectionPoint(Collection<String> connectedElements) {
        this.connectedElements = connectedElements;
    }

    public void setConnectedElements(Collection<String> connectedElements) {
        this.connectedElements = connectedElements;
    }

    public void addConnectedElement(String name) {
        this.connectedElements.add(Preconditions.checkNotNull(name));
    }

    public void removeConnectedElement(String name) {
        this.connectedElements.remove(Preconditions.checkNotNull(name));
    }

    @Override
    public Collection<String> getConnectedNames() {
        return connectedElements;
    }

    @Override
    public SimpleConnectionPoint clone() {
        SimpleConnectionPoint clone = (SimpleConnectionPoint) super.clone();
        clone.connectedElements = new HashSet<>(connectedElements);

        return clone;
    }

    @Override
    public boolean isSimple() {
        return true;
    }
}
