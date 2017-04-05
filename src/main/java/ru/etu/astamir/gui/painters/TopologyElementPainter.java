package ru.etu.astamir.gui.painters;

import com.google.common.collect.Lists;
import ru.etu.astamir.common.reflect.ReflectUtils;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.*;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.exceptions.UnexpectedException;
import ru.etu.astamir.model.regions.Contour;
import ru.etu.astamir.model.wires.Wire;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @author Artem Mon'ko
 */
public class TopologyElementPainter implements Painter<TopologyElement> {


    private void checkForMoreSuitablePainter(Class<?> elementClass) {
        Optional<Class> painter = ReflectUtils.forSimpleName(elementClass.getSimpleName() + "Painter");
        if (painter.isPresent()) {
            throw new UnexpectedException("Odd: We have " + painter.get() + ", but we're using " + getClass());
        }
    }

    @Override
    public void paint(TopologyElement elem, Graphics graphics, Function<Point, Point> coordinateTranslator) {
        checkForMoreSuitablePainter(elem.getClass()); // maybe we have more suitable painter

        List<Point> points = Lists.newArrayList(elem.getCoordinates());
        if (coordinateTranslator != null) {
            points = points.stream().map(coordinateTranslator).collect(Collectors.toList());
        }
        if (points.isEmpty()) { // we have no coordinates.
            return;
        }

        Color color = ProjectObjectManager.getColorCentral().getColor(elem);
        Graphics2D graphics2D = null;
        try {
            graphics2D = (Graphics2D) graphics.create();
            for (Point p : points) {
                graphics2D.setColor(color);
                DrawingUtils.drawPoint(p, 6, true, graphics2D);
            }

            if (elem instanceof Contour) {
                Polygon polygon = Polygon.of(points);
                DrawingUtils.drawPolygon(polygon, 6, false, graphics2D);
            } else if (elem instanceof Wire) {
                Point start = points.get(0);
                for (int i = 1; i < points.size(); i++) {
                    Point end = points.get(i);
                    DrawingUtils.drawEdge(Edge.of(start, end), 6, false, graphics2D);
                    start = end;
                }
            }
        } finally {
            if (graphics2D != null) {
                graphics2D.dispose();
            }
        }
    }
}
