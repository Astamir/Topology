package ru.etu.astamir.launcher;

import ru.etu.astamir.compression.grid.VirtualGrid;
import ru.etu.astamir.model.technology.Technology;

/**
 * @author Artem Mon'ko
 */
public interface Topology {

    VirtualGrid getGrid();

    Technology getTechnology();
}
