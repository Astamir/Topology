package ru.etu.astamir.model.regions;

import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.model.TopologyElement;

/**
 * карман
 * @author Artem Mon'ko
 */
public class Well extends Contour implements ConductionRegion {
    public Well(String symbol, Polygon area) {
        super(symbol, area);
    }

    public Well(String name, String symbol) {
        super(name, symbol);
    }

    public Well(String name, String symbol, Polygon area) {
        super(name, symbol, area);
    }

    @Override
    public boolean accept(TopologyElement element) {
        return !getConductionType().equals(element.getConductionType());
    }
}
