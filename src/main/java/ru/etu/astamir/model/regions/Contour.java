package ru.etu.astamir.model.regions;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import ru.etu.astamir.common.collections.EntitySet;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.model.Movable;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.exceptions.UnexpectedException;
import ru.etu.astamir.model.wires.Wire;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Контур какой-то области.
 */
public abstract class Contour extends TopologyElement implements Movable {
    /**
     * Элементы, принадлежащие этому контуру. Тут они просто списком
     */
    protected EntitySet<TopologyElement> elements = new EntitySet<>();

    /**
     * Огибающая линия в порядке обхода.
     */
    protected Polygon area = Polygon.emptyPolygon();

    protected Contour(String name) {
        super(name);
    }

    protected Contour() {
    }

    public Contour(String name, String symbol) {
        super(name, symbol);
    }

    public Contour(String name, String symbol, Polygon area) {
        super(name, symbol);
        this.area = area;
    }

    protected Contour(String symbol, Polygon area) {
        super();
        this.symbol = symbol;
        this.area = area;
    }

    public Contour(Polygon area) {
        super();
        this.area = area;
    }

    public Edge get(final Direction direction) {
        return Collections.max(Collections2.filter(area.edges(), new Predicate<Edge>() {
            @Override
            public boolean apply(Edge edge) {
                return edge.getOrientation().isOrthogonal(direction.toOrientation());
            }
        }), direction.getEdgeComparator());
    }

    public boolean contains(TopologyElement element) {
        return elements.contains(element);
    }

    public void addElement(TopologyElement element) {
        elements.add(element);
    }

    public void addAllElements(Collection<TopologyElement> elements) {
        this.elements.addAll(elements);
    }

    public void setElements(Collection<TopologyElement> elements) {
        this.elements = new EntitySet<>(elements);
    }

    public void removeElement(TopologyElement element) {
        elements.remove(element);
    }

    public Collection<TopologyElement> getElements() {
        return elements;
    }

    public void setCoordinates(Point... coordinates) {
        area.setVertices(coordinates);
    }

    public void setCoordinates(List<Point> vertices) {
        area.setVertices(vertices);
    }

    public void setContour(List<Edge> edges) {
        setContour(Polygon.of(edges));
    }

    public void setContour(Polygon contour) {
        this.area = contour;
    }

    public List<Edge> toEdges() {
        return Lists.newArrayList(area.edges());
    }

    @Override
    public boolean move(double dx, double dy) {
        return area.move(dx, dy);
    }

    @Override
    public Collection<Point> getCoordinates() {
        return area.vertices();
    }

    @Override
    public void setCoordinates(Collection<Point> coordinates) {
        area.setVertices(coordinates);
    }

    @Override
    public Polygon getBounds() {
        return area;
    }

    @Override
    public Contour clone() {
        Contour clone = (Contour) super.clone();
        clone.setContour(area.clone());
        clone.setElements(elements);

        return clone;
    }
}
