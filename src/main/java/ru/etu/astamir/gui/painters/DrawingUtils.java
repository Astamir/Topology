package ru.etu.astamir.gui.painters;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import ru.etu.astamir.geom.common.*;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Polygon;

import java.awt.Graphics;
import java.util.List;


/**
 * @author Artem Mon'ko
 */
public class DrawingUtils {
    public static void drawPoint(Point point, int radius, boolean drawCoordinates, Graphics graphics) {
        Graphics g = null;
        try {
            g = graphics.create();
            g.fillOval((point.intX() - radius / 2), (point.intY() - radius / 2), radius, radius);
            if (drawCoordinates) {
                g.drawString(point.toString(), point.intX(), point.intY());
            }
        } finally {
            if (g != null) {
                g.dispose();
            }
        }
    }

    public static void drawEdge(Edge edge, int radius, boolean drawPoints, Graphics graphics) {
        Graphics g = null;
        try {
            g = graphics.create();
            g.drawLine(edge.startIntX(), edge.startIntY(), edge.endIntX(), edge.endIntY());
            if (drawPoints) {
                g.drawString("s", edge.getStart().intX() - 5, edge.getStart().intY());
                drawPoint(edge.getStart(), radius, true,  g);
                g.drawString("e", edge.getEnd().intX() - 5, edge.getEnd().intY());
                drawPoint(edge.getEnd(), radius, true, g);
            }
        } finally {
            if (g != null) {
                g.dispose();
            }
        }
    }

    public static void drawEdge(Edge edge, int radius, Graphics graphics, Function<Point, Point> pointTranslator) {
        Graphics g = null;
        try {
            g = graphics.create();
            Point start = Preconditions.checkNotNull(pointTranslator.apply(edge.getStart()));
            Point end = Preconditions.checkNotNull(pointTranslator.apply(edge.getEnd()));
            g.drawLine(start.intX(), start.intY(), end.intX(), end.intY());
            if (radius > 0) {
                drawPoint(start, radius,true, g);
                drawPoint(end, radius,true, g);
            }
        } finally {
            if (g != null) {
                g.dispose();
            }
        }
    }

    public static void drawEdge(Edge edge, int radius, Graphics graphics) {
        drawEdge(edge, radius, false, graphics);
    }

    public static void drawPolygon(Polygon polygon, int radius, boolean drawVertices, Graphics graphics) {
        Graphics g = null;
        try {
            g = graphics.create();
            graphics.drawPolygon(polygon.toAWTPolygon());
            if (drawVertices) {
                for (Point vertex : polygon.vertices()) {
                    drawPoint(vertex, radius, true, graphics);
                }
            }
        } finally {
            if (g != null) {
                g.dispose();
            }
        }
    }
}
