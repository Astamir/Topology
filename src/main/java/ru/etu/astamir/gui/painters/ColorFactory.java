package ru.etu.astamir.gui.painters;

import ru.etu.astamir.model.Entity;
import ru.etu.astamir.model.TopologyElement;

import java.awt.*;
import java.util.Collection;

/**
 * @author Artem Mon'ko
 */
public interface ColorFactory {
    Color getGateColor();

    Color getActiveRegionColor();

    Color getContactColor();

    Color getOtherColor();

    Color getColor(Class<? extends Entity> aClass);
}
