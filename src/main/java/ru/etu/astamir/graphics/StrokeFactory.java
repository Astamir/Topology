package ru.etu.astamir.graphics;

import java.awt.BasicStroke;
import java.awt.Stroke;

/**
 * Created with IntelliJ IDEA.
 * User: astamir
 * Date: 11/13/12
 * Time: 2:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class StrokeFactory {
    public static Stroke defaultStroke() {
        return new BasicStroke(); // TODO implementation
    }

    public static Stroke highlightedDefaultStroke() {
        BasicStroke stroke = (BasicStroke) defaultStroke();
        return new BasicStroke(stroke.getLineWidth() + 2);
    }

    public static Stroke highlightedDashedStroke() {
        BasicStroke stroke = (BasicStroke) dashedStroke();
        return new BasicStroke(stroke.getLineWidth() + 2);
    }


    public static Stroke dashedStroke() {
        return new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 1);
    }
}
