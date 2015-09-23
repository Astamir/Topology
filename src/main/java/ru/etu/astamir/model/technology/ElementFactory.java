package ru.etu.astamir.model.technology;

import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.Entity;
import ru.etu.astamir.model.TopologyElement;

import java.util.List;
import java.util.Map;

/**
 * @author Artem Mon'ko
 */
public interface ElementFactory {
    TopologyElement getElement(String symbol, Point[] coordinates, Map<String, Object> properties);
    Class<? extends TopologyElement> getEntityClass(String symbol);
}
