package ru.etu.astamir.geom.common;

import ru.etu.astamir.math.MathUtils;
import ru.etu.astamir.model.Movable;
import ru.etu.astamir.common.Pair;
import ru.etu.astamir.model.exceptions.UnexpectedException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.awt.geom.Line2D;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

/**
 * Класс ребро.
 */
@XmlRootElement
public class Edge implements Serializable, Cloneable, Movable, Roundable {

	@XmlElement
    private Point start = new Point();

	@XmlElement
    private Point end = new Point();

    /**
     * Возможные отношения векторов
     */
    public enum EdgeRelation{ COLLINEAR, PARALLEL, SKEW /*наклон*/, SKEW_CROSS, SKEW_NO_CROSS
    }

    public enum StretchMethod{FORWARD, BACKWARD}

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
        return Edge.of(point.clone(), point.clone());
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
		return isPoint() ? Collections.singleton(start) : Arrays.asList(start, end);
	}

    public Point getTopPoint() {
        return isHorizontal() ? start : (MathUtils.compare(start.y(), end.y()) >= 0 ? start : end);
    }

    public Point getLeftPoint() {
        return isVertical() ? start : (MathUtils.compare(start.x(), end.x()) <= 0 ? start : end);
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
        Point center = getCenter();
        if (MathUtils.compare(length(), 2 * length) <= 0) {
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

    public Edge stretch(Point workingPoint, Direction dir, double length, boolean useStartForLinks) {
        double signedD = length * dir.getDirectionSign();
        Orientation orientation = dir.toOrientation();
        if (isPoint()) {
            if (useStartForLinks) {
                shift(start, orientation, signedD);
            } else {
                shift(end, orientation, signedD);
            }
        } else {
            if (start.eq(workingPoint)) {
                shift(start, orientation, signedD);
            } else if (end.eq(workingPoint)) {
                shift(end, orientation, signedD);
            }
        }

        return this;
    }

    private static void shift(Point point, Orientation orientation, double signedD) {
        if (orientation == Orientation.HORIZONTAL) {
            point.shiftX(signedD);
        } else {
            point.shiftY(signedD);
        }
    }


    /**
     *Возвращает левый нижний конец отрезка.
     */
    public Point getLeftBottomPoint() {
        if (MathUtils.compare(end.x(), start.x()) < 0) {
            return end;
        } else if (MathUtils.equals(start.x(), end.x())) {
            return MathUtils.compare(start.y(), end.x()) <= 0 ? start : end;
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
        return MathUtils.equals(start.x(), end.x());
    }

    public boolean isHorizontal() {
        return MathUtils.equals(start.y(),end.y());
    }

    public boolean isPoint() {
        return start.eq(end);
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

    public EdgeRelation intersect(Edge e) {
        Point a = start.clone();
        Point b = end.clone();

        Point c = e.start.clone();
        Point d = e.end.clone();

        Point n = new Point(Point.minus(d, c).y(), Point.minus(c, d).x());

        double denom = Point.dotProduct(n, Point.minus(b, a));
        if (MathUtils.equals(denom, 0.0)) {
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
        if (MathUtils.equals(denom, 0.0)) {
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
            if (MathUtils.compare(t, 0) >= 0 && MathUtils.compare(t, 1.0) <= 0) {
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
            Point one = getOtherPoint(commonPoint).clone();
            Point another = e.getOtherPoint(commonPoint).clone();

            double dx1 = one.x() - commonPoint.x();
            double dy1 = one.y() - commonPoint.y();
            double dx2 = another.x() - commonPoint.x();
            double dy2 = another.y() - commonPoint.y();

            angle = Math.atan2(dx1*dy2 - dy1*dx2, dx1*dx2 + dy1*dy2);
        }

        return angle;
    }
    
    public Point findCommonPoint(Edge e) {
        if (eq(e)) {
            return start; // this scenario is undefined, but we will return start.
        }
        if (start.eq(e.getStart()) || start.eq(e.getEnd())) {
            return start.clone();
        }

        if (end.eq(e.getStart()) || end.eq(e.getEnd())) {
            return end.clone();
        }

        return null;
    }

    /**
     * Получить неизменяему координату отрезка. Работает только для вертикальных или горизонтальных.
     */
    public double constant() {
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
     * Отрезки пересекаются и точка пересечения не лежит на границе какого-нибудь из отрезков.
     */
    public boolean pureCross(Edge edge) {
        if (cross(edge) == EdgeRelation.SKEW_CROSS) {
            Point crossPoint = crossing(edge);
            return !crossPoint.eq(start) && !crossPoint.eq(end) &&
                   !crossPoint.eq(edge.start) && !crossPoint.eq(edge.end);
        }
        
        return false;  
    }
    
    public Point getOtherPoint(Point p) {
        return start.eq(p) ? end : start;
    }

    public boolean isOnEdges(Point point) {
        return start.eq(point) || end.eq(point);
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

    /**
     * Работает только для ортогональных векторов.
     * @return
     */
    public Direction getDirection() {
        Orientation orientation = getOrientation();
        if (orientation == Orientation.BOTH) {
            return Direction.UNDETERMINED;
        }
        if (orientation == Orientation.HORIZONTAL) {
            return MathUtils.compare(start.x(), end.x()) > 0 ? Direction.LEFT : Direction.RIGHT;
        } else {
            return MathUtils.compare(start.y(), end.y()) > 0 ? Direction.DOWN : Direction.UP;
        }
    }

    /**
     * Расстояние от точки до прямой
     *
      * @param p Точка до которой считаем расстояние.
     *  @return Расстояние до точки p.
     */
    public double distanceToPoint(Point p) {
        return Math.abs((start.y() - end.y()) * p.x() + p.y() * (end.x() - start.x()) +
                (start.x() * end.y() - end.x()*start.y())) / (Math.sqrt(Math.pow((end.x() - start.x()),2) + Math.pow((end.y() - start.y()),2)));
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
        List<Double> distances = Arrays.asList(distanceToPoint(edge.start), distanceToPoint(edge.end),
                                                    edge.distanceToPoint(start), edge.distanceToPoint(end));
        return Collections.min(distances);
    }
    
    public <V> Optional<V> closestEdge(List<V> data, Function<V, Edge> toEdge) {
        return Optional.ofNullable(data.stream().map(d -> Pair.of(d, distanceToEdge(toEdge.apply(d))))
                .min((o1, o2) -> MathUtils.compare(o1.right, o2.right)).orElse(Pair.nullPair()).left);
    }

    @Override
    @Deprecated
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

    public boolean eq(Edge e) {
        return this == e || e != null &&
                start.eq(e.getStart()) && end.eq(e.getEnd());
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
        return "[" + start + ";" + end + "]";
    }

    public Line2D.Double toLine2D() {
        return new Line2D.Double(start.x(), start.y(), end.x(), end.y());
    }
}
