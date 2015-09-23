package ru.etu.astamir.compression.grid.legacy;

import com.google.common.base.Optional;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.model.legacy.LegacyTopologyElement;

import java.util.Collection;
import java.util.List;

/**
 * Интерфейс сетки.
 */
// TODO добавить или удалить методы.
interface LegacyGrid {
    // getters
    Collection<LegacyTopologyElement> getColumn(int columnIndex);
    Collection<LegacyTopologyElement> getRow(int rowIndex);
    Optional<LegacyTopologyElement> getElement(int columnIndex, int rowIndex);
    List<List<LegacyTopologyElement>> getColumns();
    List<List<LegacyTopologyElement>> getRows();
    List<List<LegacyTopologyElement>> walk(Direction direction);

    // adders
    boolean addElement(LegacyTopologyElement element);
    boolean addElementToColumn(LegacyTopologyElement element, int columnIndex);

    // setters
    boolean setElementAt(int columnIndex, int rowIndex, LegacyTopologyElement element);

    // removers
    void removeElementAt(int columnIndex, int rowIndex);
    
    
    int rowCount();
    int columnCount();


}
