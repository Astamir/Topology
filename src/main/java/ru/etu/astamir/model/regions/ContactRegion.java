package ru.etu.astamir.model.regions;

import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.model.regions.Contour;

/**
 * @author Astamir
 */
public class ContactRegion extends Contour {
    public ContactRegion() {
        super();
    }

    public ContactRegion(String name, String symbol) {
        super(name, symbol);
    }

    public ContactRegion(String symbol, Polygon area) {
        super(symbol, area);
    }

    public ContactRegion(String name, String symbol, Polygon area) {
        super(name, symbol, area);
    }


}
