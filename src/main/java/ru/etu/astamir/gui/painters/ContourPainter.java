package ru.etu.astamir.gui.painters;

import com.google.common.collect.Lists;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.regions.Contour;

import java.awt.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Artem Mon'ko
 */
public class ContourPainter implements Painter<Contour> {
    @Override
    public void paint(Contour entity, Graphics graphics, Function<Point, Point> coordinateTranslator) {
        Color color = ProjectObjectManager.getColorCentral().getColor(entity);
        graphics.setColor(color);
        List<Point> vertices = Lists.newArrayList(entity.getCoordinates().stream().map(coordinateTranslator).collect(Collectors.toList()));
        DrawingUtils.drawPolygon(ru.etu.astamir.geom.common.Polygon.of(vertices), 0, false, graphics);
    }
}
