package ru.etu.astamir.model.legacy;

import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.model.ConductionType;
import ru.etu.astamir.model.Drawable;
import ru.etu.astamir.model.legacy.AbstractRegion;
import ru.etu.astamir.model.legacy.LegacyTopologyElement;
import ru.etu.astamir.model.TopologyLayer;

import java.awt.Graphics2D;

/**
 * Область проводимости. Просто прямоугольник с некоторым типом проводимости.
 */
public class ConductionRegion extends AbstractRegion implements Drawable {
    private ConductionType type = ConductionType.UNKNOWN;

    public ConductionRegion(TopologyLayer layer, ConductionType type) {
        super(layer);
        this.type = type;
    }

    public ConductionRegion(int x, int y, TopologyLayer layer, ConductionType type) {
        super(x, y, layer);
        this.type = type;
    }

    public ConductionRegion(Point coordinates, TopologyLayer layer, ConductionType type) {
        super(coordinates, layer);
        this.type = type;
    }

    public ConductionRegion(Point coordinates, TopologyLayer layer, Polygon bounds, ConductionType type) {
        super(coordinates, layer, bounds);
        this.type = type;
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);
        // заштриховать в зависимости от типа проводимости или в зависимости от материала.
    }

    @Override
    public boolean accept(LegacyTopologyElement element) {
        return !element.getConductionType().equals(type);
    }
}
