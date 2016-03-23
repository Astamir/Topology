package ru.etu.astamir.gui.common;

import ru.etu.astamir.model.TopologyElement;

/**
 * Created by amonko on 14/03/16.
 */
public interface ElementContainer {
    void addElement(TopologyElement element);
    void removeElement(TopologyElement element);
}
