package ru.etu.astamir.gui.painters;

import ru.etu.astamir.model.TopologyElement;

import java.awt.*;

/**
 * @author Artem Mon'ko
 */
public interface ColorCentral {
    Color getColor(TopologyElement element);
    Color getColor(String key);

    void addElementBond(TopologyElement element, Color color);
    void addBond(String key, Color color);
}
