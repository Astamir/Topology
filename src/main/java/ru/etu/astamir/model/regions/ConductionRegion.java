package ru.etu.astamir.model.regions;

import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.legacy.LegacyTopologyElement;

/**
 * @author Artem Mon'ko
 */
public interface ConductionRegion {
    boolean accept(TopologyElement element);
}
