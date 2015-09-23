package ru.etu.astamir;

import ru.etu.astamir.graphics.StrokeFactory;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.awt.Stroke;

/**
 * Created by IntelliJ IDEA.
 * User: Astamir
 * Date: 02.01.14
 * Time: 22:36
 * To change this template use File | Settings | File Templates.
 */
public class StrokeAdapter extends XmlAdapter<String, Stroke> {
    public Stroke unmarshal(String s) {
        return StrokeFactory.defaultStroke();
    }
    public String marshal(Stroke c) {
        return "default stroke";
    }
}
