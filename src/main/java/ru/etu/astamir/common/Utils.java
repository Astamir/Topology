package ru.etu.astamir.common;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import ru.etu.astamir.compression.BorderPart;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Orientation;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.math.MathUtils;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.wires.SimpleWire;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: astamir
 * Date: 13.11.12
 * Time: 13:41
 * To change this template use File | Settings | File Templates.
 */
public class Utils {

    public static class Functions {
        public static final Function<TopologyElement, String> NAME_FUNCTION = new Function<TopologyElement, String>() {
            @Override
            public String apply(TopologyElement input) {
                return input.getName();
            }
        };

        public static final Function<TopologyElement, String> SYMBOL_FUNCTION = new Function<TopologyElement, String>() {
            @Override
            public String apply(TopologyElement input) {
                return input.getSymbol();
            }
        };

        public static final Function<SimpleWire, Edge> WIRE_AXIS_FUNCTION = new Function<SimpleWire, Edge>() {
            @Override
            public Edge apply(SimpleWire input) {
                return input.getAxis();
            }
        };

        public static final Function<BorderPart, Edge> BORDER_PART_AXIS_FUNCTION = new Function<BorderPart, Edge>() {
            @Override
            public Edge apply(BorderPart input) {
                return input.getAxis();
            }
        };

        public static final Function<SimpleWire, Iterable<Point>> WIRE_TO_COORDINATES_FUNCTION = new Function<SimpleWire, Iterable<Point>>() {
            @Override
            public Iterable<Point> apply(SimpleWire input) {
                return input.getCoordinates();
            }
        };

        public static final Function<Point, Point> SELF_FUNCTION = new Function<Point, Point>() {
            @Override
            public Point apply(Point input) {
                return input;
            }
        };

        public static final Function<TopologyElement, Collection<BorderPart>> TO_BORDER_PART_FUNCTION = new Function<TopologyElement, Collection<BorderPart>>() {
            Direction dir = Direction.UNDETERMINED;

            public void setDirection(Direction direction) {
                this.dir = direction;
            }

            @Override
            public Collection<BorderPart> apply(TopologyElement input) {
                return BorderPart.of(input, dir);
            }
        };
    }

    public static class UtilPredicates {
        public static final class OrientationPredicate implements Predicate<SimpleWire> {
            private static OrientationPredicate VERTICAL = new OrientationPredicate(Orientation.VERTICAL);
            private static OrientationPredicate HORIZONTAL = new OrientationPredicate(Orientation.HORIZONTAL);
            private final Orientation orientation;

            public OrientationPredicate(Orientation orientation) {
                this.orientation = orientation;
            }

            @Override
            public boolean apply(SimpleWire input) {
                return !input.getAxis().isPoint() && input.getAxis().getOrientation() == orientation;
            }

            public static Predicate<SimpleWire> forOrientation(Orientation orientation) {
                return orientation == Orientation.HORIZONTAL ? HORIZONTAL : orientation == Orientation.VERTICAL ? VERTICAL : Predicates.<SimpleWire>alwaysFalse();
            }
        }
    }

    public static boolean isOdd(int number) {
        return !isEven(number);
    }

    public static boolean isEven(int number) {
        return (number & 1) == 0;
    }

    public static double round(double value) {
        return new BigDecimal(value).setScale(MathUtils.MAX_PRECISION, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double assignIfSmaller(double current, double new_one, double nan) {
        return current <= new_one && current != nan ? current : new_one;
    }

    public static double assignIfSmaller(double current, double new_one) {
        return assignIfSmaller(current, new_one, 0.0);
    }
}
