package ru.etu.astamir.gui.painters;

import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.model.TopologyElement;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Artem Mon'ko
 */
public class DefaultColorCentral implements ColorCentral{
    private Map<String, Color> colorMap = new HashMap<>();

    private Map<TopologyElement, Color> elementColorMap = new HashMap<>();

    public DefaultColorCentral() {
        init();
    }

    private void init() {
        colorMap.put("SI", Color.BLUE);
        colorMap.put("M1", Color.BLACK);
        colorMap.put("M2", Color.BLACK);
        colorMap.put("NA", Color.RED);
        colorMap.put("KN", Color.BLACK);
        colorMap.put("SP", Color.GREEN);
        colorMap.put("SN", Color.GREEN);
        colorMap.put("CNA", Color.BLACK);
        colorMap.put("TC", Color.BLACK);
        colorMap.put("PIN", Color.ORANGE);
    }

    @Override
    public Color getColor(TopologyElement element) {
        if (elementColorMap.containsKey(element)) {
            return elementColorMap.get(element);
        } else if (colorMap.containsKey(element.getSymbol())) {
            return colorMap.get(element.getSymbol());
        } else if (colorMap.containsKey(element.getName())) {
            return colorMap.get(element.getName());
        }

        return ProjectObjectManager.getColorFactory().getColor(element.getClass());
    }

    @Override
    public Color getColor(String key) {
        return colorMap.get(key);
    }

    @Override
    public void addElementBond(TopologyElement element, Color color) {
        elementColorMap.put(element, color);
    }

    @Override
    public void addBond(String key, Color color) {
        colorMap.put(key, color);
    }
}
