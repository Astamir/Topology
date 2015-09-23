package ru.etu.astamir.model;

import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.model.regions.Contour;

/**
 * @author Astamir
 */
public class TopologicalCell extends Contour {
    public static final String DEFAULT_CELL_SYMBOL = "TC";

    protected TopologicalCell(String symbol) {
        super(symbol);
    }

    public TopologicalCell(String name, String symbol) {
        super(name, symbol);
    }

    public TopologicalCell(String name, String symbol, Polygon area) {
        super(name, symbol, area);
    }
}
