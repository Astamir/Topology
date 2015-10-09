package ru.etu.astamir.gui.painters;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.*;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.model.wires.SimpleWire;
import ru.etu.astamir.model.wires.Wire;
import ru.etu.astamir.model.wires.WireUtils;

import java.awt.*;
import java.util.List;


/**
 * @author Artem Mon'ko
 */
public class WirePainter implements Painter<Wire> {

    @Override
    public void paint(Wire entity, Graphics graphics, Function<Point, Point> coordinateTranslator) {
        //List<Edge> edges = WireUtils.fromPoints(Lists.transform(Lists.newArrayList(entity.getCoordinates()), coordinateTranslator));

        Color color = ProjectObjectManager.getColorCentral().getColor(entity);
        graphics.setColor(color);

        for (SimpleWire wire : entity.getParts()) {
            Edge axis = wire.getAxis();
            Edge edge = Edge.of(coordinateTranslator.apply(axis.getStart()), coordinateTranslator.apply(axis.getEnd()));
            DrawingUtils.drawEdge(edge, 4, true, graphics);
           // graphics.drawString(String.valueOf(wire.getIndex()), edge.getCenter().intX(), edge.getCenter().intY());
            String symbol = wire.getWire() != null ? wire.getIndex() == 0 ? wire.getWire().getName() : "" : "";
            //graphics.drawString(String.valueOf(symbol), edge.getCenter().intX(), edge.getCenter().intY());
           // Polygon bounds = Polygon.of(Lists.transform(wire.buildBounds().vertices(), coordinateTranslator));
           // DrawingUtils.drawPolygon(bounds, 0, false, graphics);
//            Point center = axis.getCenter();
//            graphics.drawString(String.valueOf(wire.getIndex()), center.intX(), center.intY());
        }

        //DrawingUtils.drawPolygon(Polygon.of(entity.getBounds()), 0, false, graphics);
    }
}
