package ru.etu.astamir.gui.painters;

import ru.etu.astamir.model.Entity;

/**
 * @author Artem Mon'ko
 */
public interface PainterCentral {
    Painter getEntityPainter(Entity entity);

    void addCustomPainter(String key, Painter painter);

    Painter getCustomPainter(String key);
}
