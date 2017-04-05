package ru.etu.astamir.common;

import ru.etu.astamir.compression.BorderPart;
import ru.etu.astamir.compression.commands.compression.ActiveBorder;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Orientation;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.Entity;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.wires.SimpleWire;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by IntelliJ IDEA.
 * User: astamir
 * Date: 13.11.12
 * Time: 13:41
 * To change this template use File | Settings | File Templates.
 */
public class Utils {
    public static final double LENGTH_NAN = -1D;

    public static class Transformers {
        public static final Function<TopologyElement, String> NAME_FUNCTION = Entity::getName;

        public static final Function<TopologyElement, String> SYMBOL_FUNCTION = TopologyElement::getSymbol;

        public static final Function<SimpleWire, Edge> WIRE_AXIS_FUNCTION = SimpleWire::getAxis;

        public static final Function<BorderPart, Edge> BORDER_PART_AXIS_FUNCTION = BorderPart::getAxis;

        public static final Function<SimpleWire, Collection<Point>> WIRE_TO_COORDINATES_FUNCTION = SimpleWire::getCoordinates;

        public static final Function<Point, Point> SELF_FUNCTION = Function.identity();

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
            private static final OrientationPredicate VERTICAL = new OrientationPredicate(Orientation.VERTICAL);
            private static final OrientationPredicate HORIZONTAL = new OrientationPredicate(Orientation.HORIZONTAL);
            private final Orientation orientation;

            public OrientationPredicate(Orientation orientation) {
                this.orientation = orientation;
            }

            @Override
            public boolean test(SimpleWire input) {
                return !input.getAxis().isPoint() && input.getAxis().getOrientation() == orientation;
            }

            public static Predicate<SimpleWire> forOrientation(Orientation orientation) {
                return orientation == Orientation.HORIZONTAL ? HORIZONTAL : orientation == Orientation.VERTICAL ? VERTICAL : i -> false;
            }
        }
    }

    public static int fact(int n) {
        int fact = 1;
        for (int i = 1; i <= n; i++) {
            fact *= i;
        }

        return fact;
    }

    public static boolean isOdd(int number) {
        return !isEven(number);
    }

    public static boolean isEven(int number) {
        return (number & 1) == 0;
    }

    public static double assignIfSmaller(double current, double newOne, double nan) {
        return current <= newOne && current != nan ? current : newOne;
    }

    public static double assignIfSmaller(double current, double newOne) {
        return assignIfSmaller(current, newOne, LENGTH_NAN);
    }

    public static <V extends Comparable<V>> V assignIfSmaller(V current, V newOne, V nan) {
        return current.compareTo(newOne) <= 0 && current.compareTo(nan) != 0 ? current : newOne;
    }

    public static ActiveBorder assignIfSmaller(ActiveBorder current, ActiveBorder newOne) {
        return assignIfSmaller(current, newOne, ActiveBorder.NAN);
    }
}
