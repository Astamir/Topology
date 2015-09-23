package ru.etu.astamir.model.legacy;

import ru.etu.astamir.compression.grid.legacy.LegacyVirtualGrid;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.model.Drawable;
import ru.etu.astamir.model.TopologyLayer;

import java.awt.Graphics2D;

/**
 * Область чего либо. Многоугольник в общем случае, хотя
 * видимо в большинстве случаев это прямоугольник или какая то композиция
 * прямоугольников.  Может рисовать границы области.
 * В конкретную область можно помещать конкретные элементы
 * и больше ничего.
 */
public abstract class AbstractRegion extends LegacyTopologyElement implements Drawable {
    /**
     * Элементы в области.
     */
    public LegacyVirtualGrid grid = new LegacyVirtualGrid();

    public DirectedBounds bounds;

    protected AbstractRegion(TopologyLayer layer) {
        super(layer);
    }

    protected AbstractRegion(int x, int y, TopologyLayer layer) {
        super(x, y, layer);
    }

    protected AbstractRegion(Point coordinates, TopologyLayer layer) {
        super(coordinates, layer);
    }

    protected AbstractRegion(Point coordinates, TopologyLayer layer, Polygon bounds) {
        super(coordinates, layer);
        setBounds(bounds);
    }

    public void setDirectedBounds(DirectedBounds bounds) {
        this.bounds = bounds;
    }

    @Override
    public void draw(Graphics2D g) {
        bounds.draw(g);
        //grid.draw(g);
    }

    /**
     * Проверяет может ли элемент находится в данной области.
     * Должны подходить координаты (относительно сетки или чего то еще), а так
     * же соответствовать тип проводимости, если элемент чувствителен к нему.
     * @param element
     * @return
     */
    public abstract boolean accept(LegacyTopologyElement element);

}
