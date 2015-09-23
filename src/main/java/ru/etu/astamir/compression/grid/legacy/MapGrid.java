package ru.etu.astamir.compression.grid.legacy;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import ru.etu.astamir.compression.grid.legacy.LegacyGrid;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.model.legacy.EmptyElementLegacy;
import ru.etu.astamir.model.legacy.LegacyTopologyElement;
import ru.etu.astamir.model.TopologyLayer;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Рабочее название, сетка реализованная на основе TreeMap. 
 */
public class MapGrid implements LegacyGrid {
    TopologyLayer layer;
    private Map<Integer, Map<Integer, LegacyTopologyElement>> elements = Maps.newTreeMap();

    @Override
    public Collection<LegacyTopologyElement> getColumn(int columnIndex) {
        return elements.get(columnIndex).values();
    }

    @Override
    public Collection<LegacyTopologyElement> getRow(int rowIndex) {
        List<LegacyTopologyElement> row = Lists.newArrayList();
        EmptyElementLegacy emptyElement = EmptyElementLegacy.create(layer);
        for (Map<Integer, LegacyTopologyElement> column : elements.values()) {
            if (column.containsKey(rowIndex)) {
                row.add(column.get(rowIndex));
            } else {
                row.add(emptyElement);
            }
        }
        return row;
    }

    @Override
    public Optional<LegacyTopologyElement> getElement(int columnIndex, int rowIndex) {
        if (elements.containsKey(columnIndex)) {
            Map<Integer, LegacyTopologyElement> column = elements.get(columnIndex);
            return Optional.fromNullable(column.get(rowIndex));            
        }
        
        return Optional.absent();
    }

    @Override
    public List<List<LegacyTopologyElement>> getColumns() {
        List<List<LegacyTopologyElement>> columns = Lists.newArrayList();
        for (Map<Integer, LegacyTopologyElement> column : elements.values()) {
            columns.add(Lists.newArrayList(column.values()));
        }
        return columns;
    }

    @Override
    public List<List<LegacyTopologyElement>> getRows() {
        List<List<LegacyTopologyElement>> rows = Lists.newArrayList();
        List<List<LegacyTopologyElement>> columns = getColumns();
        for (int i = 0; i < rowCount(); i++) {

        }

        return rows;
    }

    @Override
    public List<List<LegacyTopologyElement>> walk(Direction direction) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean addElement(LegacyTopologyElement element) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean addElementToColumn(LegacyTopologyElement element, int columnIndex) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean setElementAt(int columnIndex, int rowIndex, LegacyTopologyElement element) {
        return false;
    }

    @Override
    public void removeElementAt(int columnIndex, int rowIndex) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
    
    public int rowCount() {
        return Collections.max(getColumns(), new Comparator<List<LegacyTopologyElement>>() {
            @Override
            public int compare(List<LegacyTopologyElement> o1, List<LegacyTopologyElement> o2) {
                return Ints.compare(o1.size(), o2.size());
            }
        }).size();
    }
    
    public int columnCount() {
        return elements.size();
    }
}
