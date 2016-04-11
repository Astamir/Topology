package ru.etu.astamir.compression.grid;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import ru.etu.astamir.common.Pair;
import ru.etu.astamir.common.collections.CollectionUtils;
import ru.etu.astamir.common.collections.EntitySet;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.regions.Contour;

import java.io.Serializable;
import java.util.*;

/**
 *
 */
public class VirtualGrid implements Grid, Serializable {
    private EntitySet<TopologyElement> elements = EntitySet.create();

    /**
     * Unsorted backing table
     */
    private Table<Double, Double, Collection<String>> table = TreeBasedTable.create();

    public VirtualGrid(Collection<TopologyElement> elements) {
        this.elements = new EntitySet<>(elements);
        fillTable(elements);
    }

    public VirtualGrid() {
    }

    public static VirtualGrid deepCopyOf(VirtualGrid grid) {
        VirtualGrid vg = new VirtualGrid();
        for (TopologyElement element : grid.elements) {
            vg.addElement(element.clone());
        }

        return vg;
    }

    private void fillTable(Collection<TopologyElement> elements) {
        for (TopologyElement element : elements) {
            addElement(element);
        }
    }

    public void putElement(Point coordinate, TopologyElement element) {
        putElement(coordinate.x(), coordinate.y(), element);
    }

    public void putElement(double x, double y, TopologyElement element) {
        elements.add(element);
        String name = element.getName();
        if (table.contains(x, y)) { // If we already have such coordinate, then just add elements id to it
            Collection<String> names = table.get(x, y);
            names.add(name);
        } else  {
            Set<String> names = Sets.newHashSet(name);
            table.put(x, y, names);
        }
    }

    /**
     * Adding element base on its internal coordinates.
     *
     * @param element element to add
     */
    @Override
    public void addElement(TopologyElement element) {
        for (Point coordinate : element.getCoordinates()) {
            putElement(coordinate, element);
        }
    }

    public boolean removeElement(TopologyElement element) {
        return elements.remove(element) && removeFromTable(element.getName());
    }

    private boolean removeFromTable(String name) {
        final Collection<Pair<Double, Double>> keys = findInTable(name);
        boolean all_removed = true;
        for (Pair<Double, Double> key : keys) {
            Collection<String> elements = table.get(key.left, key.right);
            all_removed &= elements.remove(name);
        }

        return all_removed;
    }

    private Collection<Pair<Double, Double>> findInTable(final String name) {
        Collection<Pair<Double, Double>> keys = new ArrayList<>();
        Map<Double, Map<Double, Collection<String>>> rowMap = table.rowMap();
        for (double x : rowMap.keySet()) {
            Map<Double, Collection<String>> row = rowMap.get(x);
            for (double y : row.keySet()) {
                final Optional<String> element = row.get(y).stream().filter(input -> input.equals(name)).findFirst();
                if (element.isPresent()) {
                    keys.add(Pair.of(x, y));
                }
            }
        }

        return keys;
    }

    @Override
    public int size() {
        return elements.size();
    }

    private Collection<TopologyElement> toElements(Iterable<String> names) {
        Collection<TopologyElement> result = new ArrayList<>();
        for (String name : names) {
            if (elements.contains(name)) {
                result.add(elements.get(name));
            }
        }

        return result;
    }

    public <V extends TopologyElement> Collection<V> toElements(Iterable<String> names, Class<V> clazz) {
        return CollectionUtils.cast(toElements(names), clazz);
    }

    @Override
    public Map<Double, Collection<TopologyElement>> getColumn(double columnIndex) {
        Map<Double, Collection<TopologyElement>> column = new TreeMap<>();
        if (table.containsRow(columnIndex)) {// here, we're getting row cause column is x and we have rows as x
            Map<Double, Collection<String>> column_names = table.row(columnIndex);
            for (double index : column_names.keySet()) {
                Collection<String> names = column_names.get(index);
                column.put(index, toElements(names));
            }
        }

        return column;
    }

    @Override
    public Map<Double, Collection<TopologyElement>> getRow(double rowIndex) {
        Map<Double, Collection<TopologyElement>> row = new TreeMap<>();
        if (table.containsColumn(rowIndex)) {// here, we're getting column cause row is y and we have columns as y
            Map<Double, Collection<String>> row_names = table.column(rowIndex);
            for (double index : row_names.keySet()) {
                Collection<String> names = row_names.get(index);
                row.put(index, toElements(names));
            }
        }

        return row;
    }

    @Override
    public Collection<TopologyElement> getElements(double x, double y) {
        return toElements(table.get(x, y));
    }

    @Override
    public List<List<TopologyElement>> getColumns() {
        Map<Double, Map<Double, Collection<String>>> columnMap = new TreeMap<>(table.rowMap());
        List<List<TopologyElement>> columns = new ArrayList<>();
        for (Map<Double, Collection<String>> column : columnMap.values()) {
            List<TopologyElement> elements = Lists.newArrayList(Iterables.transform(Iterables.concat(column.values()), new Function<String, TopologyElement>() {
                @Override
                public TopologyElement apply(String input) {
                    return VirtualGrid.this.elements.get(input);
                }
            }));
            columns.add(elements);
        }
        return columns;
    }

    public List<List<TopologyElement>> getReversedColumns() {
        List<List<TopologyElement>> reversedColumns = getColumns();
        Collections.reverse(reversedColumns);
        return reversedColumns;
    }

    public Map<Double, Map<Double, Collection<TopologyElement>>> columnMap() {
        Map<Double, Map<Double, Collection<String>>> columnMap = new TreeMap<>(table.rowMap());
        Map<Double, Map<Double, Collection<TopologyElement>>> result = new TreeMap<>();
        for (Map.Entry<Double, Map<Double, Collection<String>>> columns : columnMap.entrySet()) {
            Map<Double, Collection<TopologyElement>> column = new TreeMap<>();
            for (Map.Entry<Double, Collection<String>> collectionEntry : columns.getValue().entrySet()) {
                column.put(collectionEntry.getKey(), Lists.transform(Lists.newArrayList(collectionEntry.getValue()), new Function<String, TopologyElement>() {
                    @Override
                    public TopologyElement apply(String input) {
                        return VirtualGrid.this.elements.get(input);
                    }
                }));
            }
            result.put(columns.getKey(), column);
        }

        return result;
    }

    public Optional<TopologyElement> findElementByName(String name) {
        return Optional.ofNullable(elements.get(name));
    }

    @Override
    public Collection<TopologyElement> findAllElements(Point point) {
        List<TopologyElement> result = new ArrayList<>();
        for (TopologyElement element : elements) {
            Polygon bounds = element.getBounds();
            if (bounds.isPointIn(point)) {
                result.add(element);
            }
        }
        return result;
    }

    @Override
    public Optional<TopologyElement> findElement(Point point) {
        Collection<TopologyElement> allElements = findAllElements(point);
        if (allElements.isEmpty()) {
            return Optional.empty();
        }

        TopologyElement result = null;
        for (TopologyElement element : allElements) {
            if (result == null) {
                result = element;
            } else if (result instanceof Contour) {
                if (((Contour) result).contains(element)) {
                    result = element;
                }
            }
        }
        return Optional.ofNullable(result);
    }

    @Override
    public <V extends TopologyElement> Collection<V> findAllOfType(Class<? extends V> type) {
        return (Collection<V>) toElements(getSymbolsOfClass(type));
    }

    public Collection<String> getSymbolsOfClass(Collection<Class<? extends TopologyElement>> types) {
        Set<String> symbols = new HashSet<>();
        for (TopologyElement element : elements) {
            if (types.contains(element.getClass())) {
                symbols.add(element.getSymbol());
            }
        }
        return symbols;
    }

    public Collection<String> getSymbolsOfClass(Class<? extends TopologyElement> type) {
        Set<String> symbols = new HashSet<>();
        for (TopologyElement element : elements) {
            if (type.isInstance(element)) {
                symbols.add(element.getSymbol());
            }
        }
        return symbols;
    }

    public <V extends TopologyElement> Collection<V> getElementsOfType(final Class<V> type) {
        return CollectionUtils.cast(Collections2.filter(elements, new Predicate<TopologyElement>() {
            @Override
            public boolean apply(TopologyElement input) {
                return type.isAssignableFrom(input.getClass());
            }
        }), type);
    }

    public Optional<Contour> getElementsContainer(String name) {
        if (elements.contains(name)) {
            TopologyElement element = elements.get(name);
            for (TopologyElement container : elements) {
                if (container.equals(element)) {
                    continue;
                }

                if (container instanceof Contour) {
                    Contour contour = ((Contour) container);
                    if (contour.contains(element)) {
                        return Optional.of(contour);
                    }
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public List<List<TopologyElement>> getRows() {
        Map<Double, Map<Double, Collection<String>>> rowMap = new TreeMap<>(table.columnMap());
        List<List<TopologyElement>> rows = new ArrayList<>();
        for (Map<Double, Collection<String>> column : rowMap.values()) {
            List<TopologyElement> elements = Lists.newArrayList(Iterables.transform(Iterables.concat(column.values()), new Function<String, TopologyElement>() {
                @Override
                public TopologyElement apply(String input) {
                    return VirtualGrid.this.elements.get(input);
                }
            }));
            rows.add(elements);
        }
        return rows;
    }

    @Override
    public List<List<TopologyElement>> walk(Direction direction) {
        switch (direction) {
            case LEFT: return getColumns();
            case RIGHT:
                List<List<TopologyElement>> columns = getColumns();
                Collections.reverse(columns);
                return columns;
            case DOWN: return getRows();
            case UP:
                List<List<TopologyElement>> rows = getRows();
                Collections.reverse(rows);
                return rows;
            default: return getColumns();
        }
    }

    @Override
    public Collection<TopologyElement> getAllElements() {
        return ImmutableList.copyOf(elements);
    }

    @Override
    public VirtualGrid clone() {
        return deepCopyOf(this);
    }
}
