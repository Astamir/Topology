package ru.etu.astamir.model;

import java.util.Collection;
import java.util.List;

/**
 * @author Artem Mon'ko
 */
public interface ComplexElement {
    Collection<? extends TopologyElement> getElements();
}
