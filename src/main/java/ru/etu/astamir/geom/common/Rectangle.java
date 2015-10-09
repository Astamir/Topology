package ru.etu.astamir.geom.common;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import ru.etu.astamir.model.exceptions.UnexpectedException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.awt.Graphics2D;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Прямоугольник. Это тип ортогонального полигона без
 * возможности добавления или удаления вершин.
 *
 * @version 1.0
 * @author astamir
 */
@XmlRootElement
public class Rectangle extends Polygon {
    /**
     * левый верхний угол прямоугольника.
     */
    private double left;
    private double top;
    
    /**
     * Ширина.
     */
    private double width;

    /**
     * Высота.
     */
    private double height;

    public Rectangle(double left, double top, double width, double height) {
        super(vertices(left, top, width, height));    
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    public Rectangle(Polygon polygon) {
        Preconditions.checkArgument(polygon.isRectangle());
        vertices = polygon.vertices;
        backReconstruct();
    }

    public Rectangle(Point leftTop, double width, double height) {
        this(leftTop.x(), leftTop.y(), width, height);
    }

    private Rectangle() {
    }

    public static Rectangle createSquare(Point center, double size) {
        return createForCenter(center, size, size);
    }

    public static Rectangle createForCenter(Point center, double width, double height) {
        return new Rectangle(center.x() - width / 2.0, center.y() + height / 2.0, width, height);
    }

    public static Rectangle of(Point start, Direction direction, double length, double width, double widthAtBorder) {
        return Rectangle.of(Edge.of(start, direction, length), width, widthAtBorder);
    }

    public static Rectangle of(Iterable<Point> vertices) {
        return new Rectangle(Polygon.of(vertices));
    }

    public static Rectangle of(final Edge axis, double width, double widthAtBorder) {
        Orientation axisOrientation = axis.getOrientation();
        Preconditions.checkArgument(axisOrientation != Orientation.BOTH, axis);
        Edge goodAxis = (Edge) axis.clone();
        if (goodAxis.getStart().compareTo(goodAxis.getEnd()) > 0) {
            goodAxis.reverse();
        } // now its good all right

        Point start = goodAxis.getStart();
        Point end = goodAxis.getEnd();

        if (axisOrientation == Orientation.HORIZONTAL) {
            double left = start.x() - widthAtBorder;
            double top = start.y() + width;

            return new Rectangle(left, top, goodAxis.length() + widthAtBorder * 2, width * 2);
        } else if (axisOrientation == Orientation.VERTICAL) {
            double left = end.x() - width;
            double top = end.y() + widthAtBorder;

            return new Rectangle(left, top, width * 2, goodAxis.length() + widthAtBorder * 2);
        } else {
            throw new UnexpectedException();
        }
    }
    
    private static List<Point> vertices(double left, double top, double width, double height) {
        double right = left + width;
        double bottom = top - height;
        return Lists.newArrayList(Point.of(left, top), Point.of(right, top),
                Point.of(right, bottom), Point.of(left, bottom));        
    }
    
    private void reconstruct() {
        double right = left + width;
        double bottom = top - height;
        
        Iterator<Point> i = vertexIterator();
        i.next().setPoint(left, top);
        i.next().setPoint(right, top);
        i.next().setPoint(right, bottom);
        i.next().setPoint(left, bottom);
    }

    public void backReconstruct() {
        Point leftTop = vertexSearchMax(new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                if (o1 != null && o2 != null) {
                    double x1 = o1.x();
                    double x2 = o2.x();
                    double y1 = o1.y();
                    double y2 = o2.y();

                    if (x1 > x2) {
                        return -1;
                    } else if (x1 < x2) {
                        return 1;
                    } else {
                        if (y1 > y2) {
                            return 1;
                        } else if (y1 < y2) {
                            return -1;
                        }
                    }
                }

                return 0;
            }
        });

        left = leftTop.x();
        top = leftTop.y();

        width = getRightVertex().x() - leftTop.x();
        height = leftTop.y() - getBottomVertex().y();
    }

    @Override
    public Point getCenter() {
        return Point.of(left + width / 2.0, top - height / 2.0);
    }

    @XmlAttribute
    public double getLeft() {
        return left;
    }

    public void setLeft(double left) {
        this.left = left;
        reconstruct();
    }

    @XmlAttribute
    public double getTop() {
        return top;
    }

    public double getBottom() {
        return top - height;
    }

    public double getRight() {
        return left + width;
    }

    public void setTop(double top) {
        this.top = top;
        reconstruct();
    }

    @XmlAttribute
    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
        reconstruct();
    }

    @XmlAttribute
    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
        reconstruct();
    }

    @Override
    public boolean move(double dx, double dy) {
        super.move(dx, dy);
        left += dx;
        top += dy;

        return true;
    }

    public void moveEdge(int index, Direction dir, double length) {
        Edge edge = getEdge(index);
        GeomUtils.move(edge, dir, length);
        backReconstruct();
    }

    public void moveEdge(Edge edge, Direction dir, double length) {
        GeomUtils.move(edge, dir, length);
        backReconstruct();
    }
    
    public void moveEdge(final Direction dir, double length) {
        Edge edge = Collections.max(Lists.newArrayList(Iterators.filter(edgeIterator(), new Predicate<Edge>() {
            @Override
            public boolean apply(Edge input) {
                return input.getOrientation().isOrthogonal(dir.toOrientation());
            }
        })), dir.getOppositeDirection().getEdgeComparator());
        moveEdge(edge, dir, length);
    }

    public void moveRightEdge(Direction dir, double length) {
        moveEdge(getRightEdge(), dir, length);
    }

    public void moveTopEdge(Direction dir, double length) {
        moveEdge(getTopEdge(), dir, length);
    }

    public void moveBottomEdge(Direction dir, double length) {
        moveEdge(getBottomEdge(), dir, length);
    }

    public void moveLeftEdge(Direction dir, double length) {
        moveEdge(getLeftEdge(), dir, length);
    }

    public void stretch(double step) {
        setLeft(left - step);
        setTop(top + step);
        setHeight(height + step*2);
        setWidth(width + step*2);
        reconstruct();
    }

    public Edge getLeftEdge() {
        return Collections.max(Lists.newArrayList(edgeIterator()), new Comparator<Edge>() {
            @Override
            public int compare(Edge o1, Edge o2) {
                if (o1 != null && o2 != null) {
                    int startCompare = Doubles.compare(o2.getStart().x(), o1.getStart().x());
                    return startCompare == 0 ? Doubles.compare(o2.getEnd().x(), o1.getEnd().x()) : startCompare;
                }

                return 0;
            }
        });
    }

    public Edge getRightEdge() {
        return Collections.max(Lists.newArrayList(edgeIterator()), new Comparator<Edge>() {
            @Override
            public int compare(Edge o1, Edge o2) {
                if (o1 != null && o2 != null) {
                    return Doubles.compare(o1.getStart().x(), o2.getStart().x());
                }

                return 0;
            }
        });

    }
    
    public Edge getBottomEdge() {
        return Collections.max(Lists.newArrayList(edgeIterator()), new Comparator<Edge>() {
            @Override
            public int compare(Edge o1, Edge o2) {
                if (o1 != null && o2 != null) {
                    return Doubles.compare(o2.getStart().y(), o1.getStart().y());
                }

                return 0;
            }
        });
    }

    public Edge getTopEdge() {
        return Collections.max(Lists.newArrayList(edgeIterator()), new Comparator<Edge>() {
            @Override
            public int compare(Edge o1, Edge o2) {
                if (o1 != null && o2 != null) {
                    return Doubles.compare(o1.getStart().y(), o2.getStart().y());
                }

                return 0;
            }
        });
    }
    
    public Edge getEdge(Direction dir) {
        switch (dir) {
            case RIGHT: return getRightEdge();
            case LEFT: return getLeftEdge();
            case UP: return getTopEdge();
            case DOWN: return getBottomEdge();
            default: throw new UnexpectedException();
        }
    }

    public Collection<Edge> getEdges(Orientation orientation) {
        switch (orientation) {
            case HORIZONTAL: return Lists.newArrayList(getEdge(Direction.UP), getEdge(Direction.DOWN));
            case VERTICAL: return Lists.newArrayList(getEdge(Direction.RIGHT), getEdge(Direction.LEFT));
            default: return edges();
        }
    }

    @Override
    public int size() {
        return 4;
    }

    @Override
    public void setVertices(Collection<Point> vertices) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(Point vertex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addAll(Collection<Point> vertices) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearVertices() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean insert(int index, Point vertex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Point vertex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Point remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPointIn(Point point) {
        return isPointInConvexPolygon(point);
    }

    @Override
    public boolean isConvex() {
        return true;
    }

    @Override
    public boolean isOrthogonal() {
        return true;
    }

    @Override
    public double perimeter() {
        return (2 * width) + (2 * height);
    }

    @Override
    public double area() {
        return width * height;
    }
    
    public Iterator<Point> vertexIterator(int index) {
        return new VertexIterator(index);
    }

    public Iterator<Point> vertexIterator() {
        return new VertexIterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Rectangle rectangle = (Rectangle) o;

        if (Double.compare(rectangle.height, height) != 0) return false;
        if (Double.compare(rectangle.left, left) != 0) return false;
        if (Double.compare(rectangle.top, top) != 0) return false;
        if (Double.compare(rectangle.width, width) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = left != +0.0d ? Double.doubleToLongBits(left) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = top != +0.0d ? Double.doubleToLongBits(top) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = width != +0.0d ? Double.doubleToLongBits(width) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = height != +0.0d ? Double.doubleToLongBits(height) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public Rectangle clone() {
        return new Rectangle(left, top, width, height);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);
    }
    
    private class VertexIterator implements Iterator<Point> {
        /**
         * Индекс элемента, который вернет функция next().
         */
        int cursor = 0;

        /**
         * Индекс элемента, полученный последним вызовом next или
         * previous.
         */
        int lastRet = -1;

        VertexIterator(int index) {
            cursor = index;
        }

        VertexIterator() {
            this(0);
        }

        public boolean hasNext() {
            return cursor != size();
        }

        public Point next() {            
            try {
                Point next = get(cursor);
                lastRet = cursor++;
                return next;
            } catch (IndexOutOfBoundsException e) {                 
                throw new NoSuchElementException();
            }
        }

        public Point previous() {            
            try {
                int i = cursor - 1;
                Point previous = get(i);
                lastRet = cursor = i;
                return previous;
            } catch (IndexOutOfBoundsException e) {                
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public boolean hasPrevious() {
            return cursor != 0;
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor-1;
        }
    }
}
