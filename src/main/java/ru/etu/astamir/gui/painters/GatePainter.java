package ru.etu.astamir.gui.painters;

import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.*;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.wires.Gate;
import ru.etu.astamir.model.wires.SimpleWire;

import java.awt.*;
import java.util.function.Function;

/**
 * @author Artem Mon'ko
 */
public class GatePainter implements Painter<Gate> {
    @Override
    public void paint(Gate entity, Graphics graphics, Function<Point, Point> coordinateTranslator) {
        Color color = ProjectObjectManager.getColorCentral().getColor(entity);
        graphics.setColor(color);

        for (SimpleWire wire : entity.getParts()) {
            Edge axis = wire.getAxis();
            Edge edge = Edge.of(coordinateTranslator.apply(axis.getStart()), coordinateTranslator.apply(axis.getEnd()));
            DrawingUtils.drawEdge(edge, 4, true, graphics);
            graphics.drawString(String.valueOf(wire.getIndex()), edge.getCenter().intX(), edge.getCenter().intY());
            // Polygon bounds = Polygon.of(Lists.transform(wire.buildBounds().vertices(), coordinateTranslator));
            // DrawingUtils.drawPolygon(bounds, 0, false, graphics);
//            Point center = axis.getCenter();
//            graphics.drawString(String.valueOf(wire.getIndex()), center.intX(), center.intY());
        }

        //DrawingUtils.drawPolygon(Polygon.of(entity.getBounds()), 0, false, graphics);
    }
}
