package ru.etu.astamir.model.legacy;


import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.TopologyLayer;

import java.awt.Stroke;

/**
 * Created with IntelliJ IDEA.
 * User: astamir
 * Date: 11/7/12
 * Time: 2:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class LegacyContactFactory {

	private LegacyContactFactory(int technology) {

	}


    public static Stroke createDefaultContactStroke() {
        return null; // TODO implementation
    }

    public static LegacyContactWindow createContactWindowForLayer(TopologyLayer layer, Point center) {
        return null; //TODO implementation
    }

    public static double getDefaultContactWidth(TopologyLayer layer) {
        return 40; // TODO
    }
}
