package ru.etu.astamir.gui.painters;

import com.google.common.base.Function;
import com.sun.istack.internal.Nullable;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.Entity;

import java.awt.*;

/**
 * @author Artem Mon'ko
 */
public class EmptyPainter implements Painter<Entity> {
    @Override
    public void paint(Entity entity, Graphics graphics, @Nullable Function<Point, Point> coordinateTranslator) {
        // nothing to do here
    }
}
