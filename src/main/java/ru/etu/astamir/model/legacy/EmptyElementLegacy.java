package ru.etu.astamir.model.legacy;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import ru.etu.astamir.model.TopologyLayer;

import java.awt.Graphics2D;
import java.util.Map;

/**
 * Просто пустой элемент топологии для заполнения промежутков в сетке.
 */
public class EmptyElementLegacy extends LegacyTopologyElement {
    static Map<TopologyLayer, EmptyElementLegacy> map = Maps.newHashMap();

    protected EmptyElementLegacy(TopologyLayer layer) {
        super(layer);
    }

    @Override
    public void draw(Graphics2D g) {
        // we don't have to do anything here, since it's an empty element.
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    public static EmptyElementLegacy createEmptyElementWithCache(TopologyLayer layer) {
        if (layer == null) {
            return map.size() > 0 ? Lists.newArrayList(map.values()).get(0) : new EmptyElementLegacy(null);
        }

        if (map.containsKey(layer)) {
            return map.get(layer);
        }

        EmptyElementLegacy element = new EmptyElementLegacy(layer);
        map.put(layer, element);

        return element;
    }

    public static EmptyElementLegacy create(TopologyLayer layer) {
        return new EmptyElementLegacy(layer);
    }
}
