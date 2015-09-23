package ru.etu.astamir.model.regions;

import ru.etu.astamir.geom.common.Polygon;

/**
 * Подложка. Какая-то область с конкретным материалом (металл, кремний и тд)
 */
public class Substrate extends Contour {
    protected Substrate(String name) {
        super(name);
    }

    public Substrate(String name, String symbol) {
        super(name, symbol);
    }

    public Substrate(String name, String symbol, Polygon area) {
        super(name, symbol, area);
    }
}
