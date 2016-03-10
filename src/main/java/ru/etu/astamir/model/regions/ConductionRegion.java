package ru.etu.astamir.model.regions;

import ru.etu.astamir.model.TopologyElement;

/**
 * @author Artem Mon'ko
 */
public interface ConductionRegion {
    boolean accept(TopologyElement element);
}
