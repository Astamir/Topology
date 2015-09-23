package ru.etu.astamir.compression.grid.legacy;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.BorderPart;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Orientation;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.model.Drawable;
import ru.etu.astamir.model.legacy.EmptyElementLegacy;
import ru.etu.astamir.model.legacy.LegacyTopologyElement;
import ru.etu.astamir.model.TopologyLayer;
import ru.etu.astamir.model.exceptions.UnexpectedException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.awt.Graphics2D;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Виртуальная сетка. Отличается от обычной тем, что у нее может изменятся шаг и размер
 * в зависимости от каких-то событий.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class LegacyVirtualGrid implements Drawable, LegacyGrid {
    private TopologyLayer layer;

    /**
     * Шаг сетки.
     */
    private double step = 15.0;

    /**
     * Элементы топологии.
     */
    private List<List<LegacyTopologyElement>> elements = Lists.newArrayList();

    private List<LegacyTopologyElement> links = Lists.newArrayList();

    /**
     * Максимальное количество элементов в столбце.
     */
    private int maxRowCount = Integer.MAX_VALUE;

    /**
     * Максимальное кол-во колонок.
     */
    private int maxColumnCount = Integer.MAX_VALUE;


    public LegacyVirtualGrid(List<List<LegacyTopologyElement>> elements, double step) {
        this.step = step;
        this.elements = Lists.newArrayList(elements);
    }

    public LegacyVirtualGrid(int columnCount, double step) {
        this.step = step;
        this.elements = Lists.newArrayListWithCapacity(columnCount);
    }

    public LegacyVirtualGrid(int columnCount) {
        this(columnCount, 15.0);
    }


    public LegacyVirtualGrid() {
        this(5, 15.0);
    }

    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        this.step = step;
    }

    /**
     * 
     * @param elements
     */
    // TODO look through
    public void setAllElements(List<LegacyTopologyElement> elements) {
        for (LegacyTopologyElement element : elements) {
            setElementAt(element.getCoordinates(), element);
        }
    }


    public List<LegacyTopologyElement> getLinks() {
        links.clear();
        links.addAll(getAllElements());
        return links;
    }

    public void resolveLinks() {
        setAllElements(links);
    }

    @XmlElement
    private void setLinks(List<LegacyTopologyElement> links) {
        System.out.println("trying to set links");
    }

    /**
     * Добавление элемента в колонку.
     *
     * @param element Некоторый элемент топологии
     * @param columnIndex индекс колонки, в конец которой добавляем элемент.
     * @return false, если не получилось добавить элемент.
     */
    public boolean addElementToColumn(LegacyTopologyElement element, int columnIndex) {
        if (columnIndex >= columnCount()) {
            ensureColumnCount(columnIndex + 1);
        }

        List<LegacyTopologyElement> column = getColumn(columnIndex);
        if (column.size() == maxRowCount) {
            return false;
        }
        element.setCoordinates(columnIndex, column.size());
        return column.add(element);
    }

    public boolean addElementToRow(LegacyTopologyElement element, int rowIndex) {
        if (rowIndex >= rowCount()) {
            if (!addEmptyRow()) {
                return false;
            }
        }

        ensureColumnCount(rowIndex + 1);
        return setElementAt(indexOfLastNotEmptyElementInRow(rowIndex), rowIndex, element);
    }

    int indexOfLastNotEmptyElementInRow(int rowIndex) {
        if (rowIndex >= rowCount()) {
            return -1;
        }

        final List<LegacyTopologyElement> row = getRow(rowIndex);
        for (int i = row.size() - 1; i >= 0; i--) {
            if (!row.get(i).isEmpty()) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Удостоверение, что нам хватает колонок. Грубо говоря, добавляем
     * |[новое число колонок] - [текущее число колонок]| пустых колонок.
     * @param newColumnCount Новое число колонок. Столько колонок должно стать
     *                       после работы метода.
     * @see #columnCount()
     */
    private void ensureColumnCount(int newColumnCount) {
        int columnsToAdd = newColumnCount - columnCount();
        for (int i = 0; i < columnsToAdd; i++) {
            addEmptyColumn();
        }
    }

    /**
     * Добавление пустой колонки.
     */
    private void addEmptyColumn() {
        List<LegacyTopologyElement> column = Lists.newArrayListWithCapacity(rowCount());
        elements.add(column);
    }

    /**
     * Добавление пустой колонки по указанному индексу.
     * Все колонки начиная с указанного индекса сдвигаются вправо
     * на одну позицию, координаты элементов обновляются.
     *
     * @param index Индекс пустой колонки.
     */
    public void insertEmptyColumn(int index) {
        Preconditions.checkArgument(index >= 0);

        if (index >= columnCount()) {
            ensureColumnCount(index + 1);
            return;
        }
        List<LegacyTopologyElement> column = Lists.newArrayListWithCapacity(rowCount());
        elements.add(index, column);
        ensureCoordinates();
    }

    // TODO test
    public void shiftGrid(Direction direction) {
        // either insert empty column or remove first column and add empty to the end;
        switch (direction) {
            case RIGHT: {
                insertEmptyColumn(0);
            } break;
            case LEFT: {
                removeColumn(0);
                addEmptyColumn();
            } break;
            case UP: {
                insertEmptyRow(0);
            } break;
            case DOWN: {
                removeRow(0);
                addEmptyRow();
            } break;

            default: throw new UnexpectedException();
        }


    }

    boolean addEmptyRow() {
        return addRow(Collections.nCopies(columnCount(), EmptyElementLegacy.create(layer)));
    }



    boolean insertEmptyRow(int rowIndex) {
        if (rowCount() + 1 < maxRowCount) {
            int columnIndex = 0;
            for (int i = 0; i < elements.size();i++/*List<LegacyTopologyElement> column : elements*/) {
                insertElement(columnIndex, rowIndex, EmptyElementLegacy.create(layer));
                columnIndex++;
            }

            return true;
        }

        return false;
    }



    /**
     * Добавляет элемент в конец таблицы. Если в последней колонке нет места,
     * создается новая. Добавление элемента ограничивается параметром
     * {@link #maxRowCount}
     *
     * @param element Элемент, который мы хотим добавить.
     * @return true, если удалось добавить элемент.
     */
    public boolean addElement(LegacyTopologyElement element) {
        if (elements.isEmpty()) {
            return addElementToColumn(element, 0);
        }

        final int columnCount = columnCount();
        List<LegacyTopologyElement> lastColumn = elements.get(columnCount - 1);

        if (lastColumn.size() < maxRowCount) {
            element.setCoordinates(columnCount - 1, lastColumn.size());
            return lastColumn.add(element);
        }

        return addElementToColumn(element, columnCount);
    }

    /**
     * Добавляет элемент в заданную ячейку сетки, если в ней не содержится другого элемента. В колонке
     * элементы до rowIndex заполняются пустыми элементами. Этот метод не стоит вызывать напрямую, нужно
     * пользоваться методом {@link #setElementAt(int, int, ru.etu.astamir.model.legacy.LegacyTopologyElement)}
     *
     * @param columnIndex Индекс колонки
     * @param rowIndex Индекс строки.
     * @param element Элемент, который нужно добавить.
     *
     * @return true, если получилось добавить элемент по заданным координатам, иначе false.
     * @see #setElementAt(int, int, ru.etu.astamir.model.legacy.LegacyTopologyElement)
     *
     */
    private boolean addElementAt(int columnIndex, int rowIndex, LegacyTopologyElement element) {
        // we are absolutely sure that we don't have element with this coordinates
        // we have to find working column
        ensureColumnCount(columnIndex + 1); // we have to ensure its existance.
        List<LegacyTopologyElement> column = getColumn(columnIndex);

        // now we have to fill all empty spots with empty elements

        if (rowIndex >= column.size()) {
            LegacyTopologyElement emptyElement = EmptyElementLegacy.create(layer);
            for (int i = column.size(); i < rowIndex; i++) {
                emptyElement.setCoordinates(columnIndex, i);
                column.add(emptyElement);
            }

            element.setCoordinates(columnIndex, rowIndex);
            return column.add(element);
        }

        return false;
    }

    /**
     * Добавление колонки в правый конец сетки. Все элементы с индексом
     * больше чем {@link #maxRowCount} не учитываются.
     * @param column список элементов
     */
    public void addColumn(List<? extends LegacyTopologyElement> column) {
        int lastIndex = column.size() - 1;
        int lastAllowedIndex = maxRowCount - 1;
        if (lastIndex >= 0) { // we have some elements in the column
            elements.add(Lists.newArrayList(column.subList(0,
                    (lastAllowedIndex > lastIndex ? lastIndex : lastAllowedIndex) + 1))); // we have to trim the column according to maxRowCount
            ensureCoordinates(); // also we have to update elements' coordinates,
            // although we only have to set actual coordinates to the give column.
        } else { // means we got an empty column
            addEmptyColumn(); // so we add an empty column
        }
    }

    /**
     * Добавление строки элементов в сетку. Мы сможем добавить строку только
     * если нам это позволит параметр максимального кол-ва строк {@link #maxRowCount}.
     *
     * @param row Строка элементов.
     * @return true, если получилось добавить строку элементов, false, если
     * у нас уже максимальное кол-во строк.
     */
    public boolean addRow(Collection<? extends LegacyTopologyElement> row) {
        int columnCount = columnCount();
        int rowCount = rowCount();
        if (rowCount + 1 <= maxRowCount) { // we actually can add another row.
            if (columnCount < row.size()) {
                ensureColumnCount(row.size());
            } // making sure, we have enough columns to work with

            int index = 0;
            for (LegacyTopologyElement element : row) {
                setElementAt(index, rowCount, element); // in case we also have to add empty elements.
                index++;
            }

            return true;
        }

        return false;
    }

    public boolean insertRow(Collection<? extends LegacyTopologyElement> row, int rowIndex) {
        if (insertEmptyRow(rowIndex)) {
            int index = 0;
            for (LegacyTopologyElement element : row) {
                setElementAt(index, rowIndex, element);
                index++;
            }
        }

        return true;
    }

    public boolean insertColumn(Collection<? extends LegacyTopologyElement> column, int index) {
        if (column.size() <= maxRowCount) {
            insertEmptyColumn(index);
            int k = 0;
            for (LegacyTopologyElement element : column) {
                setElementAt(index, k, element);
                k++;
            }
        }

        return false;
    }

    public void insertGrid(LegacyVirtualGrid grid, int columnIndex, int rowIndex) {
        for (List<LegacyTopologyElement> column : grid.getColumns()) {
            int rowI = rowIndex;
            for (LegacyTopologyElement element : column) {
                setElementAt(columnIndex, rowI, element);
                rowI++;
            }

            columnIndex++;
        }

        ensureCoordinates();
    }

    /**
     * Задать элемент с конкретными координатами. Если элемент с такими координатами уже есть в сетке,
     * мы просто заменяем его на переданный, иначе мы создаем все условия, чтобы переданный элемент
     * оказался в сетке с требуемыми координатами, а именно добавляем необходимые колонки и заполняем
     * все элементы перед ним пустыми.
     *
     * @param columnIndex индекс колонки.
     * @param rowIndex индекс строки.
     * @param newElement сам элемент.
     * @return true, если получилось добавить элемент с заданными координатами.
     */
    public boolean setElementAt(int columnIndex, int rowIndex, LegacyTopologyElement newElement) {
        if (getElement(columnIndex, rowIndex).isPresent()) { // if we already have an element with such coordinates, we replace it.
            newElement.setCoordinates(columnIndex, rowIndex); // setting actual coordinates to the element.
            elements.get(columnIndex).set(rowIndex, newElement);
            return true;
        }

        return addElementAt(columnIndex, rowIndex, newElement); // we were unable to find existing element, so we have to add one
    }

    public boolean setElementAt(Point coordinates, LegacyTopologyElement newElement) {
        return setElementAt(coordinates.intX(), coordinates.intY(), newElement);
    }

    /**
     * Удаление элемента, или просто замена его пустым. Для полноценного
     * удаления элемента со сдвигом нужно вызвать {@link #removeEmptyElements()}
     * @param columnIndex
     * @param rowIndex
     */
    @Override
    public void removeElementAt(int columnIndex, int rowIndex) {
        setElementAt(columnIndex, rowIndex, EmptyElementLegacy.create(layer));
    }

    /**
     * Удаляем целый ряд, если в колонке нету элементов заданной строки,
     * заполняем пустыми. Этот метод не полностью удаляет элементы, если
     * нужно удалить их полностью следует затем вызвать метод {@link #removeEmptyElements()}
     *
     * @param rowIndex индекс строки, которую нужно удалить.
     */
    public void removeRow(int rowIndex) {
        Preconditions.checkElementIndex(rowIndex, columnCount());
        for (int i = 0; i < columnCount(); i++) {
            removeElementAt(i, rowIndex);
        }
    }

    /**
     * Удаление колонки со здвигом влево, а так же обновление координат элементов.
     * @param columnIndex индекс колонки, которую хотим удалить.
     */
    public void removeColumn(int columnIndex) {
        Preconditions.checkElementIndex(columnIndex, columnCount());
        elements.remove(columnIndex);
        ensureCoordinates();
    }

    /**
     * Получение колонки по заданному индексу.
     * @param columnIndex индекс колонки, которую хотим получить.
     * @return колонка по заданному индексу.
     */
    public List<LegacyTopologyElement> getColumn(int columnIndex) {
        Preconditions.checkElementIndex(columnIndex, elements.size());
        return elements.get(columnIndex);
    }

    /**
     * Получение колонки, как отображения интекса строки в элемент.
     * Все пустые элементы игнорируются.
     *
     * @param columnIndex индекс колонки, которую хотим получить.
     * @return Отображение колонки без пустых элементов.
     */
    public Map<Integer, LegacyTopologyElement> columnMap(int columnIndex) {
        Preconditions.checkElementIndex(columnIndex, columnCount());
        List<LegacyTopologyElement> column = getColumn(columnIndex);
        if (!column.isEmpty()) {
            Map<Integer, LegacyTopologyElement> columnMap = Maps.newHashMap();
            for (int i = 0; i < column.size(); i++) {
                LegacyTopologyElement element = column.get(i);
                if (!element.isEmpty()) {
                    columnMap.put(i, element);
                }
            }

            return columnMap;
        }

        return Maps.newHashMap();
    }

    /**
     * Получение строки, как отображения интекса столбца в элемент. Все
     * пустые элементы игнорируются.
     *
     * @param rowIndex индекс строки, которую хотим получить.
     * @return Отображение строки без пустых элементов.
     */
    public Map<Integer, LegacyTopologyElement> rowMap(int rowIndex) {
        Preconditions.checkElementIndex(rowIndex, rowCount());
        List<LegacyTopologyElement> row = getRow(rowIndex);
        if (!row.isEmpty()) {
            Map<Integer, LegacyTopologyElement> rowMap = Maps.newHashMap();
            for (int i = 0; i < row.size(); i++) {
                LegacyTopologyElement element = row.get(i);
                if (!element.isEmpty()) {
                    rowMap.put(i, element);
                }
            }

            return rowMap;
        }

        return Maps.newHashMap();
    }

    /**
     * Получение сетки как отображения координат в элементы, чтобы
     * для получения элемента по координатам не приходилось каждый раз
     * пробегать весь список элементов.
     *
     * @return Отображение координат в элементы.
     */
    public Map<Point, LegacyTopologyElement> getElementPointMap() {
        ensureCoordinates();
        Map<Point, LegacyTopologyElement> elementMap = Maps.newHashMap();
        for (LegacyTopologyElement element : getAllElements()) {
            if (!element.isEmpty()) {
                elementMap.put(element.getCoordinates(), element);
            }
        }

        return elementMap;
    }

    /**
     * Получение сетки как отображения индекса колонок в отображение индексов строк.
     * Все пустые элементы игнорируются.
     *
     * @return Отображение координат в элементы.
     */
    public Map<Integer, Map<Integer, LegacyTopologyElement>> getElementMap() {
        ensureCoordinates();
        Map<Integer, Map<Integer, LegacyTopologyElement>> map = Maps.newHashMap();
        for (int i = 0; i < columnCount(); i++) {
            List<LegacyTopologyElement> column = elements.get(i);
            if (!column.isEmpty()) {
                Map<Integer, LegacyTopologyElement> columnMap = columnMap(i);
                map.put(i, columnMap);
            }
        }

        return map;
    }


    /**
     * Получение строки по индексу.
     *
     * @param rowIndex индекс строки, которую мы хотим получить.
     * @return копия строки с заданным идексом.
     */
    public List<LegacyTopologyElement> getRow(int rowIndex) {
        Preconditions.checkElementIndex(rowIndex, rowCount());
        List<LegacyTopologyElement> row = Lists.newArrayListWithCapacity(columnCount());
        for (List<LegacyTopologyElement> column : elements) {
            if (rowIndex < column.size()) {
                row.add(column.get(rowIndex));
            } else {
                row.add(EmptyElementLegacy.create(layer));
            }
        }

        return row;
    }

    // TODO test
    public boolean insertElement(int columnIndex, int rowIndex, LegacyTopologyElement element) {
        ensureColumnCount(columnIndex);
        List<LegacyTopologyElement> column = getColumn(columnIndex);
        if (column.size() + 1 <= maxRowCount) {
            column.add(rowIndex, element);
            ensureCoordinates();
            removeEmptyColumns();

            return true;
        }

        removeEmptyColumns(); // we still might have some unwanted empty columns from ensureColumnCount
        return false;
    }


    @Override
    public List<List<LegacyTopologyElement>> getColumns() {
        return Lists.newArrayList(elements);
    }

    @Override
    public List<List<LegacyTopologyElement>> getRows() {
        List<List<LegacyTopologyElement>> rows = Lists.newArrayList();
        final int rowCount = rowCount();
        for (int i = 0; i < rowCount; i++) {
            rows.add(getRow(i));
        }

        return rows;
    }

    @Override
    public List<List<LegacyTopologyElement>> walk(Direction direction) {
        switch (direction) {
            case LEFT: return getColumns();
            case RIGHT: return Lists.reverse(getColumns());
            case DOWN: return getRows();
            case UP: return Lists.reverse(getRows());
            default: throw new UnexpectedException();
        }
    }

    /**
     * Пытается найти элемент с заданными координатами.
     *
     * @param columnIndex Координата columnIndex элемента
     * @param rowIndex Координата rowIndex элемента
     * @return Элемент с координатами (columnIndex, rowIndex) или null, если на этом месте ничего нету.
     */
    @Override
    public Optional<LegacyTopologyElement> getElement(final int columnIndex, final int rowIndex) {
        if (columnIndex >= columnCount()) {
            return Optional.absent();
        }

        List<LegacyTopologyElement> column = elements.get(columnIndex);

        if (rowIndex < column.size()) {
            return Optional.of(column.get(rowIndex));
        } // trying to get the element by coordinates.

        return Optional.absent();
    }

    /**
     * Получение всех элементов сетки, как единого списка. Итератор будет построен
     * на основе колонок, то есть сначала будут пройдены все элементы первой колонки потом второй и т.д.
     *
     * @return список всех элементов сетки.
     */
    public List<LegacyTopologyElement> getAllElements() {
        return Lists.newArrayList(Iterables.filter(Iterables.concat(elements), new Predicate<LegacyTopologyElement>() {
            @Override
            public boolean apply(LegacyTopologyElement input) {
                return !input.isEmpty();
            }
        }));
    }

    /**
     * Устанавливает всем элементам координаты в соответствии
     * с их положением в сетке.
     */
    public void ensureCoordinates() {
        for (int i = 0; i < columnCount(); i++) {
            for (int j = 0; j < elements.get(i).size(); j++) {
                LegacyTopologyElement element = elements.get(i).get(j);
                element.setCoordinates(i, j);
            }
        }
    }

    /**
     * Удаляет пустые элементы из сетки, а так же обновляет координаты оставшихся.
     * Если в ходе удаления в какой-то колонке заканчиваются элементы, то колонка удаляется.
     */
    public void removeEmptyElements() {
        for (Iterator<List<LegacyTopologyElement>> columnIterator = elements.iterator(); columnIterator.hasNext();) {
            List<LegacyTopologyElement> column = columnIterator.next();
            for (Iterator<LegacyTopologyElement> i = column.iterator(); i.hasNext();) {
                LegacyTopologyElement element = i.next();
                if (element.isEmpty()) {
                    i.remove();
                }
            }

            if (column.isEmpty()) {
                columnIterator.remove();
            }
        }

        ensureCoordinates();
    }

    /**
     * Удаляет полностью пустые колонки,
     * то есть колонки состоящие полностью из пустых элементов.
     */
    public void removeEmptyColumns() {
        for (Iterator<List<LegacyTopologyElement>> it = elements.iterator(); it.hasNext();) {
            final List<LegacyTopologyElement> column = it.next();
            if (Iterables.all(column, new Predicate<LegacyTopologyElement>() {
                @Override
                public boolean apply(LegacyTopologyElement input) {
                    return input.isEmpty();
                }
            })) {
                it.remove();
            }
        }
    }

    /**
     * Удаляет все пустые элементы с конца столбца. Как только встречается
     * непустой элемент, процесс останавливается.
     * @param columnIndex индекс колонки.
     */
    // TODO IOOBE
    private void removeLastEmptyElements(int columnIndex) {
        Preconditions.checkElementIndex(columnIndex, columnCount());
        List<LegacyTopologyElement> column = elements.get(columnIndex);
        for (ListIterator<LegacyTopologyElement> i = column.listIterator(column.size() - 1);i.hasPrevious();) {
            LegacyTopologyElement elem = i.previous();
            if (elem.isEmpty()) {
                i.remove();
            } else {
                break;
            }
        }
    }

    public void removeLastEmptyElements() {
        for (int columnIndex = 0; columnIndex < columnCount(); columnIndex++) {
            removeLastEmptyElements(columnIndex);
        }
    }

    /**
     * Присваивает элементам виртальные координаты на основе их
     * реальных координат.
     */
    // TODO
    public void reorderByNaturalCoordinates() {
        throw new UnexpectedException("not implemented yet");
    }

    /**
     * Перестраивает элементы в сетке в соответствии с их координатами.
     */
    // TODO
    public void reorderByElementsCoordinates() {
        throw new UnexpectedException("not implemented yet");
    }

    // TODO look through
//    public Border getBorder(final Direction direction, int index, BorderPart... additionalParts) {
//        index = direction.isUpOrRight() ? index : index + 1;
//        Border border = new Border(direction.getOrthogonalDirection().toOrientation());
//        ListIterator<List<LegacyTopologyElement>> target = direction.isLeftOrRight() ? getColumns().listIterator(index) : getRows().listIterator(index);
//        Predicate<ListIterator<List<LegacyTopologyElement>>> counter = new Predicate<ListIterator<List<LegacyTopologyElement>>>() {
//            @Override
//            public boolean apply(ListIterator<List<LegacyTopologyElement>> input) {
//                return direction.isUpOrRight() ? input.hasNext() : input.hasPrevious();
//            }
//        };
//        Function<ListIterator<List<LegacyTopologyElement>>, List<LegacyTopologyElement>> next = new Function<ListIterator<List<LegacyTopologyElement>>, List<LegacyTopologyElement>>() {
//            @Override
//            public List<LegacyTopologyElement> apply(ListIterator<List<LegacyTopologyElement>> input) {
//                return direction.isUpOrRight() ? input.next() : input.previous();
//            }
//        };
//
//        for (;counter.apply(target);) {
//            List<LegacyTopologyElement> column = next.apply(target);
//            List<BorderPart> borderParts = Lists.newArrayList();
//            for (LegacyTopologyElement element : column) {
//                borderParts.addAll(BorderPart.of(element));
//            }
//            border.overlay(borderParts, direction);
//        }
//
//        border.overlay(Lists.newArrayList(additionalParts), direction);
//
//        return border;
//    }

    // TODO look through
//    public Border getBorder(Direction direction) {
//        int columnCount = columnCount();
//        int rowCount = rowCount();
//
//        if (direction.isLeftOrRight() && columnCount == 0) {
//            return Border.emptyBorder(Orientation.VERTICAL);
//        }
//
//        if (direction.isUpOrDown() && rowCount == 0) {
//            return Border.emptyBorder(Orientation.HORIZONTAL);
//        }
//
//        return this.getBorder(direction, direction.isUpOrRight() ? 0 :
//                (direction.isLeftOrRight() ? columnCount - 1 : rowCount - 1));
//    }

    // TODO look through
//    public Border getBorderWithOffset(Direction direction, int dec) {
//        int columnCount = columnCount();
//        int rowCount = rowCount();
//
//        if (direction.isLeftOrRight() && columnCount == 0) {
//            return Border.emptyBorder(Orientation.VERTICAL);
//        }
//
//        if (direction.isUpOrDown() && rowCount == 0) {
//            return Border.emptyBorder(Orientation.HORIZONTAL);
//        }
//
//        return this.getBorder(direction, direction.isUpOrRight() ? dec :
//                (direction.isLeftOrRight() ? columnCount - dec - 1 : rowCount - dec - 1));
//    }

    /**
     * Получить число колонок.
     *
     * @return число колонок сетки.
     */
    public int columnCount() {
        return elements.size();
    }

    /**
     * Кол-во строк, число элементов в самой большой колонке.
     * Функция ищет максимальную колоноку, поэтому не стоит вызывать
     * слишком часто.
     * @return Максимальное кол-во строк.
     */
    public int rowCount() {
        return elements.isEmpty() ? 0 : Collections.max(elements, new Comparator<List<LegacyTopologyElement>>() {
            @Override
            public int compare(List<LegacyTopologyElement> o1, List<LegacyTopologyElement> o2) {
                return Ints.compare(o1.size(), o2.size());
            }
        }).size();
    }

    public void setMaxRowCount(int maxRowCount) {
        this.maxRowCount = maxRowCount;
        // remove all above rows ?
    }

    @Override
    public void draw(Graphics2D g) {
        //ensureCoordinates();
        for (LegacyTopologyElement element : getAllElements()) {
            element.draw(g);
            Polygon bounds = element.getBounds();
            if (bounds != null) {
                if (bounds.vertices().size() > 0) {
                    Point center = bounds.getCenter();
                   // g.drawString(element.getCoordinates().toString(), center.intX(), center.intY());
                }
            }
        }

    }
}
