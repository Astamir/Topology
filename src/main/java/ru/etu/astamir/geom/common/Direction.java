package ru.etu.astamir.geom.common;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Doubles;
import ru.etu.astamir.model.exceptions.UnexpectedException;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Направления.
 *
 * @version 1.0
 * @author astamir
 */
@XmlRootElement
public enum Direction {
    RIGHT, LEFT, UP, DOWN, UNDETERMINED;

    public static Direction of(Orientation orientation, double sign) {
        return orientation.toDirection(sign);
    }

    public static List<Direction> walk(Direction start) {
        return ImmutableList.of(start, start.clockwise(), start.clockwise().clockwise(), start.counterClockwise());
    }

    public static List<Direction> crippledWalk(Direction start) {
        return ImmutableList.of(start.clockwise(), start.clockwise().clockwise(), start.counterClockwise());
    }

    public static Iterable<Direction> all() {
        return ImmutableList.of(RIGHT, LEFT, UP, DOWN);
    }

    public boolean isLeftOrRight() {
        return equals(RIGHT) || equals(LEFT);
    }

    public boolean isUpOrDown() {
        return equals(UP) || equals(DOWN);
    }

    public boolean isUpOrRight() {
        return equals(UP) || equals(RIGHT);
    }

    public boolean isSameOrientation(Direction direction) {
        if (direction == null) {
            return false;
        }

        if (this == direction) {
            return true;
        }

        Orientation one = toOrientation();
        Orientation other = direction.toOrientation();

        return one.equals(other);
    }

    public int getDirectionSign() {
        return isUpOrRight() ? 1 : -1;
    }

    public Orientation toOrientation() {
        return isLeftOrRight() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
    }

    public Direction orthogonal() {
        if (equals(UP)) {
            return RIGHT;
        } else if (equals(RIGHT)) {
            return UP;
        } else if (equals(LEFT)) {
            return DOWN;
        }  else if (equals(DOWN)) {
            return LEFT;
        }

        throw new UnexpectedException();
    }

    public Direction opposite() {
        if (equals(UP)) {
            return DOWN;
        } else if (equals(RIGHT)) {
            return LEFT;
        } else if (equals(LEFT)) {
            return RIGHT;
        }  else if (equals(DOWN)) {
            return UP;
        }

        throw new UnexpectedException();
    }

    public static Direction randomDirection(Orientation orientation) {
        return orientation == Orientation.VERTICAL ?
                (new Random().nextInt(2) == 0 ? Direction.UP : Direction.DOWN) :
                (new Random().nextInt(2) == 0 ? Direction.RIGHT : Direction.LEFT);

    }

    public static Direction randomDirection() {
        return randomDirection(new Random().nextInt(2) == 0 ? Orientation.VERTICAL : Orientation.HORIZONTAL);
    }

    public boolean isDetermined() {
        return this != UNDETERMINED;
    }

    public Direction clockwise() {
        switch (this) {
            case RIGHT: return DOWN;
            case DOWN: return LEFT;
            case LEFT: return UP;
            case UP: return RIGHT;
            default: throw new UnexpectedException();
        }
    }

    public Direction counterClockwise() {
        switch (this) {
            case RIGHT: return UP;
            case DOWN: return RIGHT;
            case LEFT: return DOWN;
            case UP: return LEFT;
            default: throw new UnexpectedException();
        }
    }

    public boolean isOrthogonal(Direction direction) {
        return direction.toOrientation().isOrthogonal(this.toOrientation());
    }

    public boolean isOrthogonal(Orientation orientation) {
        return orientation.isOrthogonal(toOrientation());
    }

    public boolean isReverse(Direction direction) {
        return isSameOrientation(direction) && direction != this;
    }

    public Comparator<Edge> getEdgeComparator() {
        return getEdgeComparator(true);
    }

    public <V> Comparator<V> comparator(final Function<V, Edge> func) {
        return new Comparator<V>() {
            @Override
            public int compare(V o1, V o2) {
                return getEdgeComparator().compare(func.apply(o1), func.apply(o2));
            }
        };
    }

    public Comparator<Point> getVertexComparator() {
        switch (this) {
            case RIGHT: return new Comparator<Point>() {
                @Override
                public int compare(Point o1, Point o2) {
                    if (o1 != null && o2 != null) {
                            double x1 = o1.x();
                            double x2 = o2.x();

                            return Doubles.compare(x1, x2);
                    }

                    return 0;
                }
            };
            case DOWN: return new Comparator<Point>() {
                @Override
                public int compare(Point o1, Point o2) {
                    if (o1 != null && o2 != null) {
                            double y1 = o1.y();
                            double y2 = o2.y();

                            return Doubles.compare(y2, y1);
                    }

                    return 0;
                }
            };
            case LEFT: return new Comparator<Point>() {
                @Override
                public int compare(Point o1, Point o2) {
                    if (o1 != null && o2 != null) {
                            double x1 = o1.x();
                            double x2 = o2.x();

                            return Doubles.compare(x2, x1);
                    }

                    return 0;
                }
            };
            case UP: return new Comparator<Point>() {
                @Override
                public int compare(Point o1, Point o2) {
                    if (o1 != null && o2 != null) {
                            double y1 = o1.y();
                            double y2 = o2.y();

                            return Doubles.compare(y1, y2);
                    }

                    return 0;
                }
            };
            default: throw new UnexpectedException();
        }
    }

    /**
     * Получать нужную координату точки отрезков. Так как обычно отрезки либо вертикальные либо горизонтальные, то
     * нам важна только х или у состоявляющая.
     * @param point
     * @return
     */
    public double getDirectionalComponent(Point point) {
        if (!isDetermined()) {
            throw new UnexpectedException("Direction has to be determined for this function [getDirectionalComponent]");
        }
        if (isLeftOrRight()) {
            return point.x();
        } else {
            return point.y();
        }
    }

    public Comparator<Edge> getEdgeComparator(final boolean edgesShouldBeCorrect) {
        switch (this) {
            case RIGHT: return new Comparator<Edge>() {
                @Override
                public int compare(Edge o1, Edge o2) {
                    if (o1 != null && o2 != null) {
                        if (!edgesShouldBeCorrect || o1.isVertical() && o2.isVertical()) {
                            double x1 = o1.getStart().x();
                            double x2 = o2.getStart().x();

                            return Doubles.compare(x1, x2);
                        }
                    }

                    return 0;
                }
            };
            case DOWN: return new Comparator<Edge>() {
                @Override
                public int compare(Edge o1, Edge o2) {
                    if (o1 != null && o2 != null) {
                        if (!edgesShouldBeCorrect || o1.isHorizontal() && o2.isHorizontal()) {
                            double y1 = o1.getStart().y();
                            double y2 = o2.getStart().y();

                            return Doubles.compare(y2, y1);
                        }
                    }

                    return 0;
                }
            };
            case LEFT: return new Comparator<Edge>() {
                @Override
                public int compare(Edge o1, Edge o2) {
                    if (o1 != null && o2 != null) {
                        if (!edgesShouldBeCorrect || o1.isVertical() && o2.isVertical()) {
                            double x1 = o1.getStart().x();
                            double x2 = o2.getStart().x();

                            return Doubles.compare(x2, x1);
                        }
                    }

                    return 0;
                }
            };
            case UP: return new Comparator<Edge>() {
                @Override
                public int compare(Edge o1, Edge o2) {
                    if (o1 != null && o2 != null) {
                        if (!edgesShouldBeCorrect || o1.isHorizontal() && o2.isHorizontal()) {
                            double y1 = o1.getStart().y();
                            double y2 = o2.getStart().y();

                            return Doubles.compare(y1, y2);
                        }
                    }

                    return 0;
                }
            };
            default: throw new UnexpectedException();
        }
    }
}
