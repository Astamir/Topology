package ru.etu.astamir.geom.common;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import ru.etu.astamir.common.Utils;
import ru.etu.astamir.model.Drawable;
import ru.etu.astamir.model.Movable;
import ru.etu.astamir.common.Pair;
import ru.etu.astamir.model.exceptions.UnexpectedException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Класс ребро.
 */
@XmlRootElement
public class Edge implements Serializable, Cloneable, Drawable, Movable {

	@XmlElement
    private Point start = new Point();

	@XmlElement
    private Point end = new Point();

    /**
     * Возможные отношения векторов
     */
    public static enum EdgeRelation{ COLLINEAR, PARALLEL, SKEW /*наклон*/, SKEW_CROSS, SKEW_NO_CROSS
    }

    public static enum StretchMethod{FORWARD, BACKWARD}

    public Edge(Point start, Point end) {
        this.start = start;
        this.end = end;
        round();
    }

    public Edge(double x1, double y1, double x2, double y2) {
        this.start = new Point(x1, y1);
        this.end = new Point(x2, y2);
    }

    Edge() {
    }

    public void round() {
        start.round();
        end.round();
    }

    public static Edge of(Point start, Point end) {
        return new Edge(start, end);
    }
    
    public static Edge of(final Point start, Direction dir, double length) {
        Point end;
        if (dir.isUpOrDown()) {
            end = Point.of(start.x(), dir == Direction.UP ? start.y() + length : start.y() - length);
        } else {
            end = Point.of(dir == Direction.RIGHT ? start.x() + length : start.x() - length, start.y());
        }

        return new Edge(start, end);
    }

    public static Edge of(double startX, double startY, Direction dir, double length) {
        return Edge.of(new Point(startX, startY), dir, length);
    }

    public static Edge of(double x1, double y1, double x2, double y2) {
        return new Edge(x1, y1, x2, y2);
    }

    public static Edge ray(double x, double y, Direction direction) {
        return Edge.of(Point.of(x, y), direction, Float.MAX_VALUE);
    }

    public static Edge ray(Point start, Direction direction) {
        return Edge.of(start, direction, Float.MAX_VALUE);
    }

    public static Edge of(Point point) {
        return Edge.of((Point) point.clone(), (Point) point.clone());
    }

    public Point getStart() {
        return start;
    }

    public int startIntX() {
        return start.intX();
    }

    public int endIntX() {
        return end.intX();
    }

    public int startIntY() {
        return start.intY();
    }

    public int endIntY() {
        return end.intY();
    }

    public void setStart(Point start) {
        this.start = start;
    }

    public Point getEnd() {
        return end;
    }

    public void setEnd(Point end) {
        this.end = end;
    }

    public void setEdge(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    public void setEdge(Edge edge) {
        this.start = edge.start;
        this.end = edge.end;
    }

	/**
	 * Just returns this edge point in collection.
	 *
	 * @return returns n points of this edge, where 0 < n < 3
	 */
	public Collection<Point> getPoints() {
		return start.equals(end) ? Collections.singleton(start) : Lists.newArrayList(start, end);
	}

    public Point getTopPoint() {
        return isHorizontal() ? start : (start.y() > end.y() ? start : end);
    }

    public Point getLeftPoint() {
        return isVertical() ? start : (start.x() < end.x() ? start : end);
    }

    public Point getRightPoint() {
        return getOtherPoint(getLeftPoint());
    }

    public Point getBottomPoint() {
        return getOtherPoint(getTopPoint());
    }

    public Edge flip() {
        return rotate().rotate();
    }

    Point point(double t) {
        return Point.plus(start, Point.multiply(Point.minus(end, start), t)); // a + t * (b - a)
    }

    /**
     * Меняет направленность вектора.
     * start = end;
     * end = start;
     */
    public void reverse() {
        Point tmp = start;
        start = end;
        end = tmp;
    }

    public void correct() {
        if (getOrientation() != Orientation.BOTH) {
            if (!getDirection().isUpOrRight()) {
                reverse();
            }
        }
    }

    /**
     * Растяжение отрезка в направлении от start к end.
     * Тоесть точка начала отрезка фиксируется, а концу отрезка
     * присваивается новове значение, такое что длина отрезка становится
     * равной "предыдущая длина" + {#param length}
     * @param length Длина на которую нужно растянуть отрезок.
     * @return Растянутый отрезок.
     */
    public Edge stretch(double length) {
        stretch(StretchMethod.FORWARD, length);
        return this;
    }

    public Edge stretchBackwards(double length) {
        stretch(StretchMethod.BACKWARD, length);
        return this;
    }

    private void stretch(StretchMethod way, double length) {
        Point workingPoint = (way == StretchMethod.BACKWARD) ? start : end;
        if (isHorizontal()) {
            workingPoint.shiftX(length);
        } else if (isVertical()) {
            workingPoint.shiftY(length);
        } else { // SKEW
            double xAngle = Math.atan(slope());
            workingPoint.moveByAngle(xAngle, length);
        }
    }

    /**
     * shifts both start and end to each other. Stops if edge will become a point.
     * @param length
     */
    public void shrink(double length) {
        double eps = 1e-6; // error
        Point center = getCenter();
        if ((length() - (2 * length)) < eps) {
            setEdge(center.clone(), center.clone());
        } else {
            stretch(-length);
            stretchBackwards(length);
        }
    }

    public void scale(double scale) {
        start.multiply(scale);
        end.multiply(scale);
    }


    public Edge stretch(Direction direction, double length) {
        Orientation orientation = getOrientation();
        if (orientation == Orientation.BOTH) {
            if (direction.isUpOrRight()) {
                Point toStretch = getRightTopPoint();
                toStretch.moveByAngle(Math.atan(slope()), length);
            } else {
                Point toStretch = getLeftBottomPoint();
                toStretch.moveByAngle(Math.atan(slope()), -length);
            }
        } else {
            Direction edgeDirection = getDirection();
            if (edgeDirection.isSameOrientation(direction)) {
                if (isHorizontal()) {
                    return edgeDirection == direction ? stretch(length) : stretchBackwards(length);
                } else {
                    return edgeDirection == direction ? stretch(length) : stretchBackwards(-length);
                }
            }
        }

        return this;
    }

    public Edge stretch(Point workingPoint, Direction dir, double length) {
        double signedD = length * dir.getDirectionSign();
        Orientation orientation = getOrientation();
        if (isPoint()) {
            orientation = dir.toOrientation();
        }
        if (start.equals(workingPoint)) {
            if (orientation == Orientation.HORIZONTAL) {
                start.shiftX(signedD);
            } else {
                start.shiftY(signedD);
            }
        } else if (end.equals(workingPoint)) {
            if (orientation == Orientation.HORIZONTAL) {
                end.shiftX(signedD);
            } else {
                end.shiftY(signedD);
            }
        }

        return this;
    }


    /**
     *Возвращает левый нижний конец отрезка.
     */
    public Point getLeftBottomPoint() {
        if (end.x() < start.x()) {
            return end;
        } else if (start.x() == end.x()) {
            return start.y() <= end.x() ? start : end;
        }

        return start;
    }

    public Point getRightTopPoint() {
        return getOtherPoint(getLeftBottomPoint());
    }

    /**
     * Проверка грани на вертикальность.
     * @return
     */
    public boolean isVertical() {
        return start.x() == end.x();
    }

    public boolean isHorizontal() {
        return start.y() == end.y();
    }

    public boolean isPoint() {
        return start.equals(end);
    }

    /**
     * Проверяет, что точка лежит строго внутри отрезка, и не на его концах.
     *
     * @param point Точка которую проверяем.
     *
     * @return true, если точка лежит внутри отрезка.
     */
    public boolean isPointIn(Point point) {
        return point.classify(this) == Point.Position.BETWEEN;
    }

    public boolean isPointInOrOnEdges(Point point) {
        Point.Position pos = point.classify(this);
        return pos == Point.Position.BETWEEN || pos == Point.Position.ORIGIN || pos == Point.Position.DESTINATION;
    }


    /**
     * Возвращает величину наклона текущего ребра
     * или значение Double.MAX_VALUE, если текущее ребро вертикально:
     * @return
     */
    public double slope() {
        if (start.x() != end.x())
            return (end.y() - start.y()) / (end.x() - start.x());
        return Double.MAX_VALUE;
    }

    public boolean overlays(final Edge edge) {
        return cross(edge) == EdgeRelation.COLLINEAR && // If they aren't collinear they cant overlay
                (isPointInOrOnEdges(edge.start) || isPointInOrOnEdges(edge.end) // first check our edge points
                        || edge.isPointInOrOnEdges(start) || edge.isPointInOrOnEdges(end)); // then the other edge points
    }
    
    public boolean contains(final Edge edge) {
        Point commonPoint = findCommonPoint(edge);
        return overlays(edge) && commonPoint != null && isPointInOrOnEdges(edge.getOtherPoint(commonPoint));
    }

    public Point getCenter() {
        return Point.plus(start, end).multiply(0.5);
    }
    
    public Point overlay(final Edge edge) {
        if (!overlays(edge)) {
            throw new UnexpectedException("Edges aren't overlay");
        }

        Point commonPoint = findCommonPoint(edge); // do we have common point ?
        if (commonPoint != null) {
            return commonPoint; // yes, we do.
        }

        // alright, now it's tricky, because our edge overlays the other and we have to return
        // either one point containing in other edge or vice versa. We will return
        // point from the edge given as the parameter.

        return isPointIn(edge.start) ? edge.start : edge.end;
    }


    /**
     * Для компонентной функции у задается значение х и она возвращает значение у,
     * соответствующее точке (х, у) на текущей бесконечной прямой линии.
     * Если ребро вертикально, возвращается у координата начала отрезка.
     *
     * @param x
     * @return
     */
    public double у(double x) {
        if (isVertical()) {
            return start.y();
        }

        return slope() * (x - start.x()) + start.y();
    }

    public EdgeRelation intersect(Edge e) {
        Point a = (Point) start.clone();
        Point b = (Point) end.clone();

        Point c = (Point) e.start.clone();
        Point d = (Point) e.end.clone();

        Point n = new Point(Point.minus(d, c).y(), Point.minus(c, d).x());

        double denom = Point.dotProduct(n, Point.minus(b, a));
        if (denom == 0.0) {
            Point.Position pointPosition = start.classify(e);
            if ((pointPosition == Point.Position.LEFT) || (pointPosition == Point.Position.RIGHT)) {
                return EdgeRelation.PARALLEL;
            } else {
                return EdgeRelation.COLLINEAR;
            }
        }

        return EdgeRelation.SKEW;
    }

    double intersectionCoefficient(Edge e) throws IllegalCoefficientException {
        Point a = start.clone();
        Point b = end.clone();

        Point c = e.start.clone();
        Point d = e.end.clone();

        Point n = new Point(Point.minus(d, c).y(), Point.minus(c, d).x());

        double denom = Point.dotProduct(n, Point.minus(b, a));
        double num = Point.dotProduct(n, Point.minus(a, c));
        if (denom == 0.0) {
            throw new IllegalCoefficientException(-num / denom);
        }

        return -num / denom;
    }

    public Point intersection(final Edge edge) throws IllegalCoefficientException {
        return point(this.intersectionCoefficient(edge));
    }

    /**
     * Проверка пересечения двух отрезков.
     * @param e
     * @return
     */
    public EdgeRelation cross(final Edge e) {
        EdgeRelation crossType = e.intersect(this);
        if ((crossType == EdgeRelation.COLLINEAR) || (crossType == EdgeRelation.PARALLEL)) {
            return crossType;
        } else {
            double s = e.intersectionCoefficient(this);
            if ((s < 0.0) || (s > 1.0)) {
                return EdgeRelation.SKEW_NO_CROSS;
            }

            double t = intersectionCoefficient(e);
            if ((0.0 <= t) && (t <= 1.0)) {
                return EdgeRelation.SKEW_CROSS;
            } else {
                return EdgeRelation.SKEW_NO_CROSS;
            }
        }
    }

    /**
     * Угол пересечения двух отрезков,
     *
     * @return угол пересечения отрезков, 0, если отрезки коллинеарны
     * или не пересекаются
     */
    double crossCoefficient(final Edge e) throws IllegalCoefficientException {
        EdgeRelation crossType = e.intersect(this);
        if ((crossType == EdgeRelation.COLLINEAR) || (crossType == EdgeRelation.PARALLEL)) {
            return 0.0;
        }
        double s = e.intersectionCoefficient(this);
        if ((s < 0.0) || (s > 1.0)) {
            return 0.0;
        }

        return intersectionCoefficient(e);
    }

    public Point crossing(final Edge e) {
        EdgeRelation relation = cross(e);
        return relation == EdgeRelation.SKEW_CROSS ? point(crossCoefficient(e)) : findCommonPoint(e);
    }

    /**
     * Угол между двумя отрезками. Подразумевается, что
     * отрезки имеют общую точку, причем это начало или конец каждого отрезка.
     *
     * @param e Другой отрезок.
     * @return Угол между отрезками в радианах.
     */
    public double angle(final Edge e) {
        double angle = 0.0;
        Point commonPoint = findCommonPoint(e);
        if (commonPoint != null) {
            Point one = (Point) getOtherPoint(commonPoint).clone();
            Point another = (Point) e.getOtherPoint(commonPoint).clone();

            double dx1 = one.x() - commonPoint.x();
            double dy1 = one.y() - commonPoint.y();
            double dx2 = another.x() - commonPoint.x();
            double dy2 = another.y() - commonPoint.y();

            angle = Math.atan2(dx1*dy2 - dy1*dx2, dx1*dx2 + dy1*dy2);
        }

        return angle;
    }
    
    public Point findCommonPoint(Edge e) {
        if (equals(e)) {
            return start; // this scenario is undefined, but we will return start.
        }
        Point.Position startPosition = start.classify(e);
        Point.Position endPosition = end.classify(e);
        if (startPosition == Point.Position.ORIGIN || startPosition == Point.Position.DESTINATION) {
            return start;
        }

        if (endPosition == Point.Position.ORIGIN || endPosition == Point.Position.DESTINATION) {
            return end;
        }

        return null;
    }

    public double getConstantComponent() {
        if (isVertical()) {
            return start.x();
        } else if (isHorizontal()) {
            return start.y();
        }

        throw new UnexpectedException("Don't use this method on skew edges. Use isSkew() method before");
    }

    public boolean isSkew() {
        return !isHorizontal() && !isVertical();
    }

    /**
     * Отрезки пересекаются и точка пересечения не лежит на каком нибудь из отрезков.
     * @return
     */
    public boolean pureCross(Edge edge) {
        if (cross(edge) == EdgeRelation.SKEW_CROSS) {
            Point crossPoint = crossing(edge);
            return !crossPoint.equals(start) && !crossPoint.equals(end) &&
                   !crossPoint.equals(edge.start) && !crossPoint.equals(edge.end);
        }
        
        return false;  
    }
    
    public Point getOtherPoint(Point p) {
        return start.equals(p) ? end : start;
    }

    public boolean isOnEdges(Point point) {
        return start.equals(point) || end.equals(point);
    }

    /**
     *  Поворот ребра на 90 градусов вокруг центра.
     * @return Ребро, повернутое на 90 градусов. (чтобы можно было писать edge.rotate().rotate())
     */
    public Edge rotate() {
        Point m = Point.multiply(Point.plus(start, end), 0.5);
        Point v = Point.minus(end, start);
        Point n = new Point(v.y(), -v.x());

        start.setPoint(Point.minus(m, Point.multiply(n, 0.5)));
        end.setPoint(Point.plus(m, Point.multiply(n, 0.5)));

        return this;
    }

    /**
     * Вычисление длины отрезка.
     * @return длина отрезка.
     */
    public double length() {
        return Point.distance(start, end);
    }

    public double lengthSq() {
        return Point.distanceSq(start, end);
    }

    public boolean move(double dx, double dy) {
        start.move(dx, dy);
        end.move(dx, dy);

        return true;
    }

    public void shiftX(double dx) {
        move(dx, 0);
    }

    public void shiftY(double dy) {
        move(0, dy);
    }


    public Orientation getOrientation() {
        return isHorizontal() ? Orientation.HORIZONTAL :
                (isVertical() ? Orientation.VERTICAL : Orientation.BOTH);
    }

    public Direction getDirection(Orientation orientation) {
        Preconditions.checkArgument(orientation != Orientation.BOTH, "orientation should be either horizontal or vertical");
        Orientation workingOrientation = getOrientation();
        if (workingOrientation == Orientation.BOTH) {
            workingOrientation = orientation;
        }

        if (workingOrientation == Orientation.HORIZONTAL) {
            return start.x() > end.x() ? Direction.LEFT : Direction.RIGHT;
        } else {
            return start.y() > end.y() ? Direction.DOWN : Direction.UP;
        }
    }


    /**
     * Работает только для ортогональных векторов.
     * @return
     */
    public Direction getDirection() {
        Orientation orientation = getOrientation();

        if (orientation == Orientation.HORIZONTAL) {
            return start.x() > end.x() ? Direction.LEFT : Direction.RIGHT;
        } else {
            return start.y() > end.y() ? Direction.DOWN : Direction.UP;
        }
    }

    /**
     * Расстояние от точки до прямой
     *
      * @param p Точка до которой считаем расстояние.
     *  @return Расстояние до точки p.
     */
    public double distanceToPoint(Point p) {
        return Utils.round(Math.abs((start.y() - end.y()) * p.x() + p.y() * (end.x() - start.x()) +
                (start.x() * end.y() - end.x()*start.y())) / (Math.sqrt(Math.pow((end.x() - start.x()),2) + Math.pow((end.y() - start.y()),2))));
    }
    
    
    public double distanceToEdge(final Edge edge) {        
        if (isHorizontal() && edge.isHorizontal()) {
            double ourY = start.y();
            double otherY = edge.start.y();
            
            return Math.abs(ourY - otherY);
        }
        
        if (isVertical() && edge.isVertical()) {
            double ourX = start.x();
            double otherX = edge.start.x();

            return Math.abs(ourX - otherX);
        }
        
        // it is pretty unlikely that we would face such a case, so its just for the hell of it
        
        List<Double> distances = Lists.newArrayList(distanceToPoint(edge.start), distanceToPoint(edge.end),
                                                    edge.distanceToPoint(start), edge.distanceToPoint(end));
        return Utils.round(Collections.min(distances));
    }
    
    public <V> Optional<V> closestEdge(List<V> data, Function<V, Edge> toEdge) {
        List<Pair<V, Double>> distList = Lists.newArrayList();
        for (V elem : data) {
            Edge edge = toEdge.apply(elem);
            distList.add(Pair.of(elem, distanceToEdge(edge)));
        }

        if (distList.isEmpty()) {
            return Optional.absent();
        }

        return Optional.of(Collections.min(distList, new Comparator<Pair<V, Double>>() {
            @Override
            public int compare(Pair<V, Double> o1, Pair<V, Double> o2) {
                return Doubles.compare(o1.right, o2.right);
            }
        }).left);
    }

    /**
     * Поиск конца отрезка по точке внутри отрезка, направлению деформации и половине.
     * Работает для вертикальных или горизонтальных отрезков.
     * @param point
     * @param direction
     * @param half
     * @return
     */
    public Point findEdgePoint(Point point, Direction direction, Direction half) {
        if (!isPointIn(point)) {
            throw new UnexpectedException("given point is outside or on the edge boundaries");
        }

        if (!half.isLeftOrRight()) {
            throw new IllegalArgumentException("Половина для деформации задана некорректно. Половина : " + half);
        }

        boolean isHalfRight = (half == Direction.RIGHT);

        final Point rightPoint = getRightPoint();
        final Point leftPoint = getLeftPoint();
        final Point bottomPoint = getBottomPoint();
        final Point topPoint = getTopPoint();
        switch (direction) {
            case UP: return isHalfRight ? rightPoint : leftPoint;
            case DOWN: return isHalfRight ? leftPoint : rightPoint;
            case RIGHT: return isHalfRight ? bottomPoint : topPoint;
            case LEFT: return isHalfRight ? topPoint : bottomPoint;
            default: return null;
        }
    }


    /**
     * Деформация, подразумевая, что {@param point} лежит на прямой.
     *
     * @param point
     * @param direction
     * @param half
     * @param width
     * @return
     */
    public List<Edge> deform(Point point, Direction direction, Direction half, double width) {
        if (!isPointIn(point)) {
            throw new IllegalArgumentException("Точка излома не лежит внутри отрезка");
        }

        if (!half.isLeftOrRight()) {
            throw new IllegalArgumentException("Половина для деформации задана некорректно. Половина : " + half);
        }
        
        if ((isHorizontal() && !direction.isUpOrDown()) || (isVertical() && !direction.isLeftOrRight())) {
            throw new IllegalArgumentException("Направление деформации задано некорректно. Направление : " + direction);
        }

        double directedWidth = direction.isUpOrRight() ? width : -width;
        Edge workingEdge = (Edge) clone();

        boolean reverse = workingEdge.start.compareTo(end) >= 0; 
        
        if (reverse) {
            workingEdge.flip();
        }

        if (direction.isUpOrDown()) {
            // horizontal deform

            Point c = new Point(point.x(), point.y() + directedWidth);
            ArrayList<Edge> upRightOrDownLeft = Lists.newArrayList(new Edge(workingEdge.start, point),
                    new Edge(point, c), new Edge(c, new Point(workingEdge.end.x(), c.y())));
            ArrayList<Edge> downRightOrUpLeft = Lists.newArrayList(new Edge(Point.of(workingEdge.start.x(), c.y()), c),
                    new Edge(c, point), new Edge(point, workingEdge.end));
            if (half.equals(Direction.RIGHT)) {
                if (direction == Direction.UP) {
                    return wrapDeformedEdges(upRightOrDownLeft, reverse);
                } else {
                    return wrapDeformedEdges(downRightOrUpLeft, reverse);
                }
            } else {
                if (direction == Direction.UP) {
                    return wrapDeformedEdges(downRightOrUpLeft, reverse);
                } else {
                    return wrapDeformedEdges(upRightOrDownLeft, reverse);
                }
            }
        } else {
            // vertical deform
            Point c = new Point(point.x() + directedWidth, point.y());
            ArrayList<Edge> rightLeftOrLeftRight = Lists.newArrayList(new Edge(workingEdge.start, point),
                    new Edge(point, c), new Edge(c, new Point(c.x(), workingEdge.end.y())));
            ArrayList<Edge> leftLeftOrRightRight = Lists.newArrayList(new Edge(Point.of(c.x(), workingEdge.start.y()), c),
                    new Edge(c, point), new Edge(point, workingEdge.end));
            if (half.equals(Direction.RIGHT)) {
                if (direction == Direction.RIGHT) {
                    return wrapDeformedEdges(leftLeftOrRightRight, reverse);
                } else {
                    return wrapDeformedEdges(rightLeftOrLeftRight, reverse);
                }
            } else {
                if (direction == Direction.RIGHT) {
                    return wrapDeformedEdges(rightLeftOrLeftRight, reverse);
                } else {
                    return wrapDeformedEdges(leftLeftOrRightRight, reverse);
                }
            }
        }


    }
    
    private List<Edge> wrapDeformedEdges(List<Edge> deformedEdges, boolean reverse) {
        if (!reverse) {
            return deformedEdges;
        }

        // all edges have wrong direction
        for (Edge edge : deformedEdges) {
            edge.reverse();
        }

        // and wrong order
        return Lists.reverse(deformedEdges);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Edge edge = (Edge) o;

        if (!end.equals(edge.end)) {
            return false;
        }
        if (!start.equals(edge.start)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = start.hashCode();
        result = 31 * result + end.hashCode();
        return result;
    }

    @Override
    public Edge clone() {
        try {
            Edge clone = (Edge) super.clone();
            clone.setEdge(start.clone(), end.clone());
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    @Override
    public String toString() {
        return start + " - " + end;
    }

    public Line2D.Double toLine2D() {
        return new Line2D.Double(start.x(), start.y(), end.x(), end.y());
    }

    @Override
    public void draw(Graphics2D g) {
        g.draw(toLine2D());
      //  start.draw(g);
     //   end.draw(g);
//        g.drawString("A", start.intX(), start.intY());
//        g.drawString("B", end.intX(), end.intY());
    }
}
