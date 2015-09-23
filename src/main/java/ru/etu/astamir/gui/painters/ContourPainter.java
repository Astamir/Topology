package ru.etu.astamir.gui.painters;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sun.istack.internal.Nullable;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.regions.Contour;

import java.awt.*;
import java.util.List;

/**
 * @author Artem Mon'ko
 */
public class ContourPainter implements Painter<Contour> {
    @Override
    public void paint(Contour entity, Graphics graphics, @Nullable Function<Point, Point> coordinateTranslator) {
        Color color = ProjectObjectManager.getColorCentral().getColor(entity);
        graphics.setColor(color);
        List<Point> vertices = Lists.newArrayList(Iterables.transform(entity.getCoordinates(), coordinateTranslator));
        DrawingUtils.drawPolygon(ru.etu.astamir.geom.common.Polygon.of(vertices), 0, false, graphics);
    }
}
