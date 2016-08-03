package ru.etu.astamir.geom.common;

import ru.etu.astamir.math.MathUtils;
import ru.etu.astamir.model.Movable;
import ru.etu.astamir.model.exceptions.UnexpectedException;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Класс двухмерной точки.
 *
 *  @version 1.0
 *  @author astamir
 */
@XmlRootElement
public class Point implements Serializable, Cloneable, Movable, Roundable, Comparable<Point> {
    private static final long serialVersionUID = 1L;

    @XmlAttribute
    private double x;

    @XmlAttribute
    private double y;

    /**
     * Возможные положение точки относительно прямой.
     */
    public enum Position {LEFT,  RIGHT,  BEYOND,  BEHIND, BETWEEN, ORIGIN, DESTINATION;
        public boolean isOnEdge() {
            return this == ORIGIN || this == BETWEEN || this == DESTINATION;
        }

        public boolean isLeftOrRight() {
            return this == LEFT || this == RIGHT;
        }
    }

    private static final Pattern POINT_PATTERN = Pattern.compile("[0-9]");

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point p) {
        this.x = p.x;
        this.y = p.y;
    }

    public Point() {
        this(0.0, 0.0);
    }

    public static Point of(double x, double y) {
        return new Point(x, y);
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public int intX() {
        return (int) x;
    }

    public int intY() {
        return (int) y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public boolean move(double dx, double dy) {
        this.x += dx;
        this.y += dy;

        return true;
    }

    public void moveByAngle(double xAngle, double length) {
        this.x += Math.cos(xAngle) * length;
        this.y += Math.sin(xAngle) * length;
    }

    public void shiftX(double dx) {
        this.x += dx;
    }

    public void shiftY(double dy) {
        this.y += dy;
    }

    public void setPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setPoint(final Point p) {
        this.x = p.x;
        this.y = p.y;
    }

    public static Point plus(final Point p1, final Point p2) {
        return new Point(p1.x + p2.x, p1.y + p2.y);
    }
    
    public Point plus(final Point p) {
        x += p.x;
        y += p.y;

        return this;
    }

    public static Point minus(final Point p1, final Point p2) {
        return new Point(p1.x - p2.x, p1.y - p2.y);
    }

    public static Point multiply(final Point p, double s) {
        return new Point(p.x * s, p.y * s);
    }
    
    public Point multiply(double s) {
        x *= s;
        y *= s;

        return this;
    }

    public static double distanceSq(final Point p1, final Point p2) {
        return Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2);
    }

    public static double distance(final Point p1, final Point p2) {
        return Math.sqrt(distanceSq(p1, p2));
    }

    /**
     * Сравнение двух точек в лексикографическом порядке.
     */
    public int compareTo(final @Nonnull Point p) {
        int xCmp = MathUtils.compare(x, p.x);
        int yCmp = MathUtils.compare(y, p.y);
        return xCmp != 0 ? xCmp : yCmp;
    }

    public double length() {
        return Math.sqrt(x*x + y*y);
    }

    public double distance(Edge e) {
        Edge ab = e.clone();
        ab.flip().rotate();          // поворот ab на 90 градусов
                                     // против часовой стрелки
        Point n = Point.minus(ab.getEnd(), ab.getStart());
                                    // n = вектор, перпендикулярный ребру е
        n = Point.multiply(n, 1.0 / n.length());
                                    // нормализация вектора n
        Edge f = new Edge(this, Point.plus(this, n));
                                    // ребро f = n позиционируется
                                    // на текущей точке
                                    // t = расстоянию со знаком
        return f.intersectionCoefficient(e);// вдоль вектора f до точки,
                                    // в которой ребро f пересекает ребро е
    }

    /**
     * Округляет координаты точки.
     */
    public void round() {
        x = MathUtils.round(x);
        y = MathUtils.round(y);
    }

    /**
     * Скалярное произведение точек(как векторов из двух координат).
     * @return
     */
    public static double dotProduct(final Point p, final Point q) {
        return p.x * q.x + p.y * q.y;
    }

    public Point rotate(double angle, @Nonnull Point center) {
        double oldX = x;
        double oldY = y;

        x = (oldX - center.x()) * Math.cos(angle) - (oldY - center.y()) * Math.sin(angle);
        y = (oldX - center.x()) * Math.sin(angle) + (oldY - center.y()) * Math.cos(angle);

        return this;
    }

    public Position classify(final Point p1, final Point p2) {
        Point a = Point.minus(p2, p1);
        Point b = Point.minus(this, p1);
        double sa = a. x * b.y - b.x * a.y;
        int saToZero = MathUtils.compare(sa, 0.0);
        if (saToZero == 1)
            return Position.LEFT;
        if (saToZero == -1)
            return Position.RIGHT;
        if ( MathUtils.compare(a.x * b.x, 0.0) == -1 || MathUtils.compare(a.y * b.y, 0.0) == -1)
            return Position.BEHIND;
        if (MathUtils.compare(a.length(), b.length()) == -1)
            return Position.BEYOND;
        if (this.eq(p1))
            return Position.ORIGIN;
        if (this.eq(p2))
            return Position.DESTINATION;
        return Position.BETWEEN;
    }
    
    public Position classify(final Edge e) {
        return classify(e.getStart(), e.getEnd());
    }

    public static Point fromPoint2D(final Point2D point2D) {
        return new Point(point2D.getX(), point2D.getY());
    }

    public Point2D.Double toPoint2D() {
        return new Point2D.Double(x, y);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    @Deprecated
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Point other = (Point) obj;
        return Double.doubleToLongBits(x) == Double.doubleToLongBits(other.x) &&
                Double.doubleToLongBits(y) == Double.doubleToLongBits(other.y);
    }

    public boolean eq(Point p) {
        return this == p || p != null && MathUtils.equals(x, p.x) && MathUtils.equals(y, p.y);

    }

    public Point clone() {
        try {
            return (Point) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    @Override
    public String toString() {
        return "(" + MathUtils.round(x) + ", " + MathUtils.round(y) + ")";
    }

    /**
     * Format should be (x, y)
     * 
     * @return Point from string representation
     */
    public static Point fromString(String string) {
        Matcher m = POINT_PATTERN.matcher(string);
        if (m.groupCount() != 2) {
            throw new UnexpectedException("Wrong pattern for point deserialization");
        }
        return Point.of(Double.valueOf(m.group(0)), Double.valueOf(m.group(1)));
    }
}
