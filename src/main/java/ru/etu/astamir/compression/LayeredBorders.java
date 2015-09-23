package ru.etu.astamir.compression;

import com.google.common.collect.Maps;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.model.TopologyLayer;

import java.util.Map;

/**
 * Многослойный частокол.
 */
public class LayeredBorders {
    private Map<TopologyLayer, Map<Direction, Border>> borders = Maps.newHashMap();
}
