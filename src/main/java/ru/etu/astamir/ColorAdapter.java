package ru.etu.astamir;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.awt.Color;

public class ColorAdapter extends XmlAdapter<String,Color> {
    public Color unmarshal(String s) {
        return Color.decode(s);
    }
    public String marshal(Color c) {
        return "#"+Integer.toHexString(c.getRGB());
    }
}