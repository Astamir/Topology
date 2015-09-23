package ru.etu.astamir.model.regions;

import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.model.TopologyElement;

/**
 * @author Artem Mon'ko
 */
public class Bulk extends Contour implements ConductionRegion {
    public Bulk(String symbol, Polygon area) {
        super(symbol, area);
    }

    public Bulk(String name, String symbol) {
        super(name, symbol);
    }

    public Bulk(String name, String symbol, Polygon area) {
        super(name, symbol, area);
    }

    @Override
    public boolean accept(TopologyElement element) {
        return !getConductionType().equals(element.getConductionType());
    }
}
