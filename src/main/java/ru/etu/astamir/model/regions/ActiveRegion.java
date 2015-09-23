package ru.etu.astamir.model.regions;

import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.model.TopologyElement;

/**
 * @author Astamir
 */
public class ActiveRegion extends Contour implements ConductionRegion {


    public ActiveRegion(String name, String symbol) {
        super(name, symbol);
    }

    public ActiveRegion(String symbol, Polygon area) {
        super(symbol, area);
    }

    public ActiveRegion(String name, String symbol, Polygon area) {
        super(name, symbol, area);
    }

    @Override
    public boolean accept(TopologyElement element) {
        return false;
    }
}
