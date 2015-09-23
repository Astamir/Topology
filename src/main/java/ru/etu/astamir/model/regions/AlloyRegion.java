package ru.etu.astamir.model.regions;

import ru.etu.astamir.geom.common.Polygon;

/**
 * @author Artem Mon'ko
 */
public class AlloyRegion extends Bulk {
    public AlloyRegion(String name, String symbol) {
        super(name, symbol);
    }

    public AlloyRegion(String symbol, Polygon area) {
        super(symbol, area);
    }

    public AlloyRegion(String name, String symbol, Polygon area) {
        super(name, symbol, area);
    }
}
