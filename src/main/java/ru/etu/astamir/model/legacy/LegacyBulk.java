package ru.etu.astamir.model.legacy;

import ru.etu.astamir.model.ConductionType;
import ru.etu.astamir.model.legacy.ConductionRegion;
import ru.etu.astamir.model.legacy.LegacyTopologyElement;
import ru.etu.astamir.model.TopologyLayer;

/**
 * Карман. Некоторая область с конкретным типом проводимости ?
 * Некоторые элементы могут помещатся только в конкретные карманы.
 */
public class LegacyBulk extends ConductionRegion {

    public LegacyBulk(TopologyLayer layer, ConductionType type) {
        super(layer, type);
    }

    @Override
    public boolean accept(LegacyTopologyElement element) {
        return super.accept(element);
    }
}
