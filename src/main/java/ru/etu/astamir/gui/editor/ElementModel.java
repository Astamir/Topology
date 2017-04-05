package ru.etu.astamir.gui.editor;

import ru.etu.astamir.compression.grid.Grid;
import ru.etu.astamir.model.Entity;
import ru.etu.astamir.model.TopologyElement;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Artem Mon'ko
 */
public interface ElementModel {
    boolean addElement(TopologyElement element);

    Optional<? extends Entity> getElementByName(String name);

    boolean hasElement(String name);

    Collection<TopologyElement> getAllElements();

    Grid grid();
}
