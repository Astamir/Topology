package ru.etu.astamir.model;

import java.util.Collection;
import java.util.List;

/**
 * @author Artem Mon'ko
 */
// TODO use it!
public interface ComplexElement {
    Collection<? extends TopologyElement> getElements();
}
