package ru.etu.astamir.compression.grid;


import com.google.common.base.Optional;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.model.TopologyElement;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Эта сетка для элементов топологии, которые должны учавствовать в сжатии.
 */
public interface Grid {
    Map<Double, Collection<TopologyElement>> getRow(double rowIndex);

    Map<Double, Collection<TopologyElement>> getColumn(double columnIndex);

    // todo description
    Collection<TopologyElement> getElements(double x, double y);

    // todo
    List<List<TopologyElement>> getColumns();

    // todo
    List<List<TopologyElement>> getRows();

    // todo description
    List<List<TopologyElement>> walk(Direction direction);

    // todo description
    Collection<TopologyElement> getAllElements();

    Optional<TopologyElement> findElementByName(String name);

    void putElement(double x, double y, TopologyElement element);

    void addElement(TopologyElement element);

    int size();
}
