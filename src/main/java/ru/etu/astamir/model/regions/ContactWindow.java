package ru.etu.astamir.model.regions;

import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.geom.common.Rectangle;

/**
 * @author Artem Mon'ko
 */
public class ContactWindow extends Contour {

    public ContactWindow(String name, String symbol) {
        super(name, symbol);
    }

    public ContactWindow(String symbol) {
      super(symbol);
    }

    public ContactWindow(String symbol, Polygon area) {
        super(symbol, area);
    }

    public ContactWindow(String name, String symbol, Rectangle area) {
        super(name, symbol, area);
    }
}
