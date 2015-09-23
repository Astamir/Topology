package ru.etu.astamir.gui.painters;

import ru.etu.astamir.model.Entity;
import ru.etu.astamir.model.contacts.Contact;
import ru.etu.astamir.model.regions.ActiveRegion;
import ru.etu.astamir.model.wires.Gate;
import ru.etu.astamir.model.wires.Wire;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Artem Mon'ko
 */
public class DefaultColorFactory implements ColorFactory {

    private Map<Class<? extends Entity>, Color> colors = new HashMap<>();
    private Color defaultColor = Color.BLACK;

    public DefaultColorFactory() {
        init();
    }

    private void init() {
        colors.put(Gate.class, Color.GREEN);
        colors.put(ActiveRegion.class, Color.RED);
        colors.put(Contact.class, Color.BLACK);
        colors.put(Wire.class, Color.BLACK);
    }

    @Override
    public Color getGateColor() {
        return colors.get(Gate.class);
    }

    @Override
    public Color getActiveRegionColor() {
        return colors.get(ActiveRegion.class);
    }

    @Override
    public Color getContactColor() {
        return colors.get(Contact.class);
    }

    @Override
    public Color getOtherColor() {
        return defaultColor;
    }

    @Override
    public Color getColor(Class<? extends Entity> aClass) {
        return colors.containsKey(aClass) ? colors.get(aClass) : getOtherColor();
    }
}
