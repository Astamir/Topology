package ru.etu.astamir.gui.painters;

import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.Entity;

import java.awt.*;
import java.util.function.Function;

/**
 * @author Artem Mon'ko
 */
public class EmptyPainter implements Painter<Entity> {
    @Override
    public void paint(Entity entity, Graphics graphics, Function<Point, Point> coordinateTranslator) {
        // nothing to do here
    }
}
