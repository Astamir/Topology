package ru.etu.astamir.model.connectors;

import java.util.Collection;

/**
 *
 */
public interface ConnectionPoint extends Cloneable {
    String getName();

    Collection<String> getConnectedNames();

    ConnectionPoint clone();

    boolean isSimple();
}
