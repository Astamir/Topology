package ru.etu.astamir.geom.common;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import ru.etu.astamir.common.Pair;
import ru.etu.astamir.model.Movable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Различные вспомогательные геометрические функции.
 */
public class GeomUtils {
    private GeomUtils() {

    }

    public static final Function<Pair<Direction, Double>, Pair<Double, Double>> TO_DXDY = new Function<Pair<Direction, Double>, Pair<Double, Double>>() {
        @Override
        public Pair<Double, Double> apply(Pair<Direction, Double> input) {
            double d = input.right;
            Direction direction = input.left;
            double signedD = d * direction.getDirectionSign();
            if (direction.isLeftOrRight()) {
                return Pair.of(signedD, 0.0);
            } else {
                return Pair.of(0.0, signedD);
            }
        }
    };

    public static double area(List<Point> points) {
        return Polygon.of(points).area();
    }

    /**
     *
     * @param movable
     * @param direction
     * @param d
     * @return
     */
    public static boolean move(Movable movable, Direction direction, double d) {
        double signedD = d * direction.getDirectionSign();
        if (direction.isLeftOrRight()) {
            return movable.move(signedD, 0);
        } else {
            return movable.move(0, signedD);
        }
    }

    public static boolean move(Movable movable, Pair<Double, Double> dxdy) {
        return movable.move(dxdy.left, dxdy.right);
    }
    
    public static Polygon triangle(Point p, double width, Direction dir) {
        double directedWidth = dir.getDirectionSign() * width;
        if (dir.isUpOrDown()) {
            return Polygon.of(p, Point.of(p.x() + width / 2.0, p.y() - directedWidth), Point.of(p.x() - width / 2.0, p.y() - directedWidth));
        } else {
            return Polygon.of(p, Point.of(p.x() - directedWidth, p.y() + width / 2.0), Point.of(p.x() - directedWidth, p.y() - width / 2.0));
        }
    }

    public static Pair<Double, Double> getDistanceBetweenPoints(Point one, Point another) {
        return Pair.of(Math.abs(another.x() - one.x()), Math.abs(another.y() - one.y()));
    }

    public static double distance(double one, double another) {
        return one > another ? one - another : another - one;
    }

    public static Optional<Point> getClosestPoint(Collection<Point> points, Point target_point) {
        if (target_point == null || points.isEmpty()) {
            return Optional.absent();
        }

        if (points.size() == 1) {
            return Optional.of(points.iterator().next());
        }

        Point closest = null;
        double min_dist = 0;
        for (Point point : points) {
            double dist = Point.distance(point, target_point);
            if (dist < min_dist || closest == null) {
                closest = point;
                min_dist = dist;
            }
        }

        return Optional.fromNullable(closest);
    }

    public static boolean isOnOneLine(Point one, Point two) {
        Edge edge = Edge.of(one, two);
        return edge.isHorizontal() || edge.isVertical();
    }


    public static void move(Collection<Point> points, double dx, double dy) {
        for (Point point : points) {
            point.move(dx, dy);
        }
    }

    public static void move(Collection<Point> points, Direction direction, double d) {
        double signedD = d * direction.getDirectionSign();
        if (direction.isLeftOrRight()) {
            move(points, signedD, 0);
        } else {
            move(points, 0, signedD);
        }
    }

    public static Collection<Point> prepareToMove(Collection<Point> points, double dx, double dy) {
        List<Point> result = new ArrayList<>(points.size());
        for (Point point : points) {
            Point clone = point.clone();
            clone.move(dx, dy);
            result.add(clone);
        }

        return result;
    }

    public static double getSignedLength(Direction direction, double l) {
        return l * direction.getDirectionSign();
    }

    public static Collection<Point> prepareToMove(Collection<Point> points, Direction direction, double d) {
        double signedD = getSignedLength(direction, d);
        if (direction.isLeftOrRight()) {
            return prepareToMove(points, signedD, 0);
        } else {
            return prepareToMove(points, 0, signedD);
        }
    }

    public static List<Edge> fromPoints(List<Point> points) {
        int length = points.size();
        if (length < 2) {
            return Collections.emptyList();
        }

        ImmutableList<Edge> edges = Polygon.of(points).edges();
        return Lists.newArrayList(edges.subList(0, edges.size() - 1));
    }

    public static Orientation getOrientation(Iterable<Edge> edges) {
        double vertical = 0.0;
        double horizontal = 0.0;
        for (Edge edge : edges) {
            double length = edge.length();
            if (edge.isHorizontal()) {
                horizontal += length;
            } else if (edge.isVertical()) {
                vertical += length;
            } else {
                return Orientation.BOTH;
            }
        }

        return vertical > horizontal ? Orientation.VERTICAL : Orientation.HORIZONTAL;
    }

    public static Point[] toPoints(double... coordinates) {
        if (coordinates.length < 2) {
            return new Point[0];
        }

        int length = coordinates.length / 2;
        Point[] points = new Point[length];
        for (int i = 0, k = 0; i < length; i++, k+=2) {
            points[i] = Point.of(coordinates[k], coordinates[k + 1]);
        }

        return points;
    }
}
