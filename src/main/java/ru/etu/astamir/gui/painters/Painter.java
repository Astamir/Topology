package ru.etu.astamir.gui.painters;

import com.google.common.base.Function;
import ru.etu.astamir.geom.common.Point;

import java.awt.Graphics;
import java.util.Map;

/**
 * Created by Astamir on 10.03.14.
 */
public interface Painter<E> {
    /**
     * Paint some entity on the give graphics.
     *
     * @param entity entity to paint
     * @param graphics should be an instanceof #Graphics2D
     */
    void paint(E entity, Graphics graphics, Function<Point, Point> coordinateTranslator);
}
