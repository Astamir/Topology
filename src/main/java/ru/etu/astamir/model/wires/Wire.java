package ru.etu.astamir.model.wires;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import ru.etu.astamir.common.Pair;
import ru.etu.astamir.common.Utils;
import ru.etu.astamir.common.collections.CollectionUtils;
import ru.etu.astamir.geom.common.*;
import ru.etu.astamir.math.MathUtils;
import ru.etu.astamir.model.ComplexElement;
import ru.etu.astamir.model.Movable;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.connectors.ConnectionPoint;
import ru.etu.astamir.model.exceptions.UnexpectedException;
import ru.etu.astamir.serialization.LookIntoAttribute;

import java.io.Serializable;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public class Wire extends TopologyElement implements ComplexElement, Movable, Serializable {

    @LookIntoAttribute
    private List<SimpleWire> parts = new ArrayList<>();

    /**
     * Это расстояние от осевой линии.
     */
    double width;

    /**
     * Расстояние от осевой линии на границе шины.
     */
    double widthAtBorder;

    /**
     * Максимальная протяженность шины.
     * <p/>
     * Double.MAX_VALUE если длина шины не ограничена.
     */
    double maxLength = Double.MAX_VALUE;

    /**
     * Максимальная длина коленца.
     */
    double maxBendLength = Double.MAX_VALUE;

    /**
     * Миксимальное число коленец.
     */
    int maxBendCount = Integer.MAX_VALUE;

    /**
     * Максимальная общая длина коленец. Сумма длин всех коленец не должна превышать это значение.
     */
    double totalBendLength = Double.MAX_VALUE;

    /**
     * Ориентация шины.
     */
    Orientation orientation = Orientation.BOTH;

    Collection<ConnectionPoint> connections = new HashSet<>();

    public Wire(String name, Orientation orientation) {
        super(name);
        this.orientation = orientation;
    }

    public Wire(Orientation orientation) {
        super();
        this.orientation = orientation;
    }

    protected Wire() {
    }

    // ----------------------PARTS BUILDING -------------------------------

    /**
     * Установка первого элемента шины. Нужно для последующего добавления кусков к концу этого.
     *
     * @param start Точка начала.
     * @param end Точка конца.
     * @param minLength Минимальная длина кусочка.
     * @param maxLength Максимальная длина кусочка.
     * @param stretchable Возможность куска растягиваться.
     * @param deformable Возможность куска распадаться на несколько.
     * @param movable Возможность кусочка менять свое местоположение.
     */
    public Wire setFirstPart(Point start, Point end, double minLength, double maxLength, boolean stretchable, boolean deformable, boolean movable) {
        SimpleWire.Builder builder = new SimpleWire.Builder(this);
        builder.setAxis(Edge.of(start, end));
        builder.setMaxLength(maxLength);
        builder.setMinLength(minLength);
        builder.setStretchable(stretchable);
        builder.setDeformable(deformable);
        builder.setMovable(movable);

        parts = Lists.newArrayList(builder.build());
        return this;
    }

    public Wire setFirstPart(Edge axis, double minLength, double maxLength, boolean stretchable, boolean deformable, boolean movable) {
        SimpleWire.Builder builder = new SimpleWire.Builder(this);
        builder.setAxis(axis);
        builder.setMaxLength(maxLength);
        builder.setMinLength(minLength);
        builder.setStretchable(stretchable);
        builder.setDeformable(deformable);
        builder.setMovable(movable);

        parts = Lists.newArrayList(builder.build());
        return this;
    }

    /**
     * Добавляет очередной кусок к шине. Для обеспечения ортогональности, все последующие куски прикрепяются к последнему.
     * Нельзя добавлять куски противоположного направления последнему.
     *
     * @param direction Направление очередного кусочка шины.
     * @param length Длина кусочка.
     * @param maxLength Максимальная длина кусочка.
     * @param stretchable Возможность куска растягиваться.
     * @param movable Возможность кусочка менять свое местоположение.
     */
    public Wire addPart(Direction direction, double length, double minLength, double maxLength, boolean stretchable,
                        boolean deformable, boolean movable) {
        if (parts.isEmpty()) {
            throw new IllegalStateException("Wire must have at least one part");
        }

        SimpleWire lastPart = getLastPart();
        Direction lastDirection = lastPart.getAxis().getDirection();
        if (lastDirection.isReverse(direction) && !lastPart.getAxis().isPoint()) {
            throw new UnexpectedException("Direction can not be reverse");
        }

        SimpleWire.Builder builder = new SimpleWire.Builder(this);
        builder.setMinLength(minLength);
        builder.setMaxLength(maxLength);
        builder.setStretchable(stretchable);
        builder.setDeformable(deformable);
        builder.setMovable(movable);

        Point start = lastPart.getAxis().getEnd().clone();
        if (lastDirection == direction && length != 0) {
            SimpleWire.Builder emptyLink = new SimpleWire.Builder(this);
            emptyLink.setAxis(Edge.of(start, direction, 0));
            emptyLink.setMaxLength(maxBendLength);
            emptyLink.setStretchable(lastPart.movable || movable);
            emptyLink.setMovable(lastPart.stretchable || stretchable);

            parts.add(emptyLink.build());
        }

        Edge axis = Edge.of(start.clone(), direction, length);
        builder.setAxis(axis);

        parts.add(builder.build());
        rebuildBounds();

        return this;
    }

    public Wire addPart(Direction direction, double length, double maxLength, boolean stretchable) {
        return addPart(direction, length, 0, maxLength, stretchable, true, true);
    }

    public boolean isFirstPart(SimpleWire part) {
        return indexOf(part) == 0;
    }

    public boolean isLastPart(SimpleWire part) {
        return indexOf(part) == size() -1;
    }

    public boolean isEdgePart(SimpleWire part) {
        return isFirstPart(part) || isLastPart(part);
    }

    public SimpleWire getLastPart() {
        return parts.get(parts.size() - 1);
    }

    public SimpleWire getFirstPart() {
        return parts.get(0);
    }

    public SimpleWire getPart(int index) {
        return index >= 0 && index < size() ? parts.get(index) : null;
    }
    // ----------------------PARTS BUILDING -------------------------------

    //-----------------------GETTERS AND SETTERS---------------------------

    public List<SimpleWire> getParts() {
        return parts;
    }

    public void setParts(List<SimpleWire> parts) {
        this.parts = parts;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getWidthAtBorder() {
        return widthAtBorder;
    }

    public void setWidthAtBorder(double widthAtBorder) {
        this.widthAtBorder = widthAtBorder;
    }

    public double getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(double maxLength) {
        this.maxLength = maxLength;
    }

    public double getMaxBendLength() {
        return maxBendLength;
    }

    public void setMaxBendLength(double maxBendLength) {
        this.maxBendLength = maxBendLength;
    }

    public int getMaxBendCount() {
        return maxBendCount;
    }

    public void setMaxBendCount(int maxBendCount) {
        this.maxBendCount = maxBendCount;
    }

    public double getTotalBendLength() {
        return totalBendLength;
    }

    public void setTotalBendLength(double totalBendLength) {
        this.totalBendLength = totalBendLength;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public Collection<ConnectionPoint> getConnections() {
        return connections;
    }

    public void setConnections(Collection<ConnectionPoint> connections) {
        this.connections = connections;
    }

    public void addConnection(ConnectionPoint connectionPoint) {
        this.connections.add(connectionPoint);
    }

    public void removeConnection(ConnectionPoint connectionPoint) {
        this.connections.remove(connectionPoint);
    }
    @Override
    public Collection<Point> getCoordinates() {
        Set<Point> coordinates = new LinkedHashSet<>();
        for (SimpleWire wire : parts) {
            coordinates.addAll(wire.getCoordinates());
        }

        return coordinates;
    }

    @Override
    public boolean setCoordinates(Collection<Point> coordinates) {
        throw new UnsupportedOperationException();
    }

    public void rebuildBounds() {
        if (parts.isEmpty()) {
            return; // todo
        }
    }

    @Override
    public Polygon getBounds() {
        return Polygon.emptyPolygon(); // todo
    }

    //----------------------GETTERS AND SETTERS---------------------------

    public int size() {
        return parts.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int indexOf(SimpleWire part) {
        return  parts.indexOf(part);
    }

    /**
     * Получение всех соседних кусоков заданного.
     *
     * @param part Собственно заданный кусочек.
     * @return Список прямых соседий заданного кусочка.
     */
    public List<SimpleWire> getConnectedParts(SimpleWire part) {
        int index = indexOf(part);
        Preconditions.checkElementIndex(index, size(), "wire does not contain given part : " + part);

        return getConnectedParts(index);
    }
    
    public Collection<SimpleWire> getConnectedParts(Collection<SimpleWire> parts) {
        Set<SimpleWire> connectedParts = Sets.newHashSet(Iterables.concat(Iterables.transform(parts, new Function<SimpleWire, Iterable<SimpleWire>>() {
            @Override
            public Iterable<SimpleWire> apply(SimpleWire input) {
                return getConnectedParts(input);
            }
        })));
        connectedParts.removeAll(parts); // we need only connected parts
        
        return connectedParts;
    }

    public List<SimpleWire> getConnectedParts(int index) {
        int size = size();
        if (size <= 1) {
            return Collections.emptyList();
        }

        if (index == 0) {
            return Collections.singletonList(parts.get(index + 1));
        }

        if (index == size - 1) {
            return Collections.singletonList(parts.get(index - 1));
        }

        if (size >= 3) {
            return Arrays.asList(parts.get(index - 1), parts.get(index + 1));
        }

        return Collections.emptyList();
    }


    /**
     * Удостоверяется, что кусочки шины идут "друг за другом", те part(i).end = part(i+1).start
     */
    public void ensureChained() {
        if (size() <= 1) {
            return; // already chained
        }
        if (isChained()) {
            return; // already chained
        }

        for (ListIterator<SimpleWire> i = parts.listIterator(); i.hasNext();) {
            final Edge current = i.next().getAxis();
            if (i.hasNext()) {
                final Edge next = i.next().getAxis();
                final Point commonPoint = current.findCommonPoint(next);
                if (commonPoint == null) {
                    i.remove();
                    i.previous();
                    continue;
                    //throw new UnexpectedException("Two consecutive wire parts does not have common point");
                }

                if (!current.getEnd().eq(next.getStart())) {
                    i.remove();
                    i.previous();
                    continue;
                }
                i.previous();

            }
            if (isChained()) {
                break;
            }
        }
    }

    public boolean isChained() {
        for (ListIterator<SimpleWire> i = parts.listIterator(); i.hasNext();) {
            SimpleWire current = i.next();
            if (i.hasNext()) {
                SimpleWire next = i.next();
                if (!current.getAxis().getEnd().eq(next.getAxis().getStart())) {
                    return false;
                }
                i.previous();
            }
        }

        return true;
    }

    public boolean isOrthogonal() {
        SimpleWire previous = null;
        int size = size();
        if (size < 1) {
            return true;
        }
        if (size == 1) {
            return !parts.get(0).isLink();
        }
        for (int i = 0; i < size; i++) {
            SimpleWire current = parts.get(i);
            if (i + 1 < size) {
                SimpleWire next = parts.get(i + 1);

                if (current.isLink()) {
                    if (next.isLink()) {
                        return false; // two consecutive links
                    }
                    if (previous != null) {
                        if (previous.getAxis().getOrientation() != next.getAxis().getOrientation()) {
                            return false; //
                        }
                    }
                } else {
                    if (!current.getAxis().getOrientation().isOrthogonal(next.getAxis().getOrientation()) && !next.isLink()) {
                        return false;
                    }
                }
            }
            previous = current;
        }

        return true;
    }


    public boolean isConnected() {
        for (ListIterator<SimpleWire> i = parts.listIterator(); i.hasNext();) {
            SimpleWire current = i.next();
            if (i.hasNext()) {
                SimpleWire next = i.next();
                if (current.getAxis().findCommonPoint(next.getAxis()) == null) {
                    return false;
                }
                i.previous();
            }
        }

        return true;
    }

    /**
     *
     * @param part
     * @param direction
     * @param width
     * @see #movePart(int, ru.etu.astamir.geom.common.Direction, double)
     * @return
     */
    public boolean movePart(SimpleWire part, Direction direction, double width) {
        int index = parts.indexOf(part);
        return movePart(index, direction, width);
    }

    /**
     * Перемещение кусочка шины без учета ограничений на длину,
     * только возможность растягиваться и двигаться.
     *
     * @param partIndex
     * @param direction
     * @param width
     *
     * @return true, если получилось передвинуть заданный кусок, false иначе.
     */
    public boolean movePart(int partIndex, Direction direction, double width) {
        if (MathUtils.equals(width, 0D)) {
            return false;
        }

        if (partIndex < 0 || partIndex > parts.size() - 1) {
            return false;
        }

        SimpleWire part = parts.get(partIndex);
        if (!part.movable) {
            return false; // we can't even move this part.
        }

        if (part.isLink()) {
            return false; // cant move empty link
        }

        Direction partDirection = part.getAxis().getDirection();
        List<SimpleWire> connectedParts = getConnectedParts(part);
        if (direction.isOrthogonal(partDirection)) {
            // firstly, we have to be sure that all connected parts can be stretched
            boolean canStretch = connectedParts.stream().allMatch(SimpleWire::isStretchable);

            if (canStretch /*&& !isFlapAttached(partIndex)*/) { // if we actually can stretch connected parts
                for (SimpleWire connectedPart : connectedParts) {
                    connectedPart.stretchDirectly(WireUtils.getCommonPoint(part, connectedPart), direction, width, partIndex < connectedPart.getIndex()); // TODO common point
                }
                part.moveDirectly(direction, width); // if direction is orthogonal we just moving the part


                //rebuildBounds();
                return true;
            }

            return false;
        }

//        // in this case we have to move some connected parts too.
//        List<SimpleWire> partsToMove = Lists.newArrayList(connectedParts);
//        boolean canMove = Iterables.all(partsToMove, new Predicate<SimpleWire>() {
//            @Override
//            public boolean apply(SimpleWire input) {
//                return input.movable && Iterables.all(getConnectedParts(input), new Predicate<SimpleWire>() {
//                    @Override
//                    public boolean apply(SimpleWire input) {
//                        return input.stretchable;
//                    }
//                });
//            }
//        });
//        if (canMove) {
////            partsToMove.add(part);
////
////            stretchParts(getConnectedParts(partsToMove), partsToMove.stream().map(SimpleWire::getCoordinates).flatMap(Collection::stream).collect(Collectors.toList()), direction, width);
////            moveParts(partsToMove, direction, width);
////            //rebuildBounds();
////
////            return true;
//            part.moveDirectly(direction, width);
//            partsToMove.stream().forEach(p -> movePart(p, direction, width));
//        }

        return false;
    }

    /**
     * Перемещение кусков шины, посредсвтом перемещения точек. В этом методе ничего не отслеживается,
     * а просто производится передвижение. Отслеживание всяких параметров должно происходить до вызвова этого метода.
     *
     * @param partsToMove
     * @param dx
     * @param dy
     * @return
     */
    static boolean moveParts(Collection<SimpleWire> partsToMove, double dx, double dy) {
        for (SimpleWire wire : partsToMove) {
            wire.moveDirectly(dx, dy);
        }
        return true;
    }

    static boolean stretchParts(Collection<SimpleWire> partsToStretch, Collection<Point> connectedPartsPoints, Direction direction, double length) {
        for (final SimpleWire part : partsToStretch) {
            final Collection<Point> part_coordinates = part.getCoordinates();
            Optional<Point> base = connectedPartsPoints.stream().filter(part_coordinates::contains).findFirst();

            if (base.isPresent() && part.isStretchable()) {
                part.stretchDirectly(base.get(), direction, length, true);
            }
        }

        return true;
    }

    /**
     * Перемещение заданных кусочков шины. Никакие ограничение тут не учитываются. Все ограничения должны
     * быть учтены до вызова этого метода.
     *
     * @param partsToMove Сообственно куски, которые надо переместить.
     * @param direction Направление перемещения.
     * @param width Расстояние на которое надо переместить
     * @return false, если не получилось переместить кусочки(хотя там скорее всего всегда true)
     */
    public static boolean moveParts(Collection<SimpleWire> partsToMove, Direction direction, double width) {
        double signedD = width * direction.getDirectionSign();
        if (direction.isLeftOrRight()) {
            return moveParts(partsToMove, signedD, 0);
        } else {
            return moveParts(partsToMove, 0, signedD);
        }
    }

    /**
     * Проверка кусочков шины на нарушение максимальной или минимальной длины.
     *
     * @return false, если размеры хотя бы одного кусчка шины нарушают заданные им ограничения.
     */
    private boolean checkLength() {
        for (SimpleWire part : parts) {
            if (part.length() > part.getMaxLength()) {
                return false;
            }
        }

        return true;
    }

    public double length() {
        return parts.stream().map(SimpleWire::length).reduce((l1, l2) -> l1 + l2).orElse(0.0);
    }

    private boolean checkLength(Orientation orientation) {
        for (SimpleWire part : orientationParts(orientation)) {
            if (part.length() > part.getMaxLength()) {
                return false;
            }
        }

        return true;
    }

    private Point getDeformPoint(Edge partAxis, Point point, Direction direction) {
        Edge ray = Edge.of(point, direction, Float.MAX_VALUE);
        return ray.crossing(partAxis);
    }


    /**
     *
     * @param part
     * @param direction
     * @param length
     * @return
     */
    public boolean stretch(SimpleWire part, Direction direction, double length) {
        if (!part.stretchable) {
            return false; // we can't even stretch it.
        }

        int index = parts.indexOf(part);
        if (index < 0) {
            return false;
        }

        Edge axis = part.getAxis();
        Direction axisDirection = axis.getDirection();
        if (!axisDirection.isSameOrientation(direction)) {
            return false;
        }

        // all we have to do is to call movePart on the connected part if it exists, or simply stretch otherwise.
        final Point workingPoint = (axisDirection == direction) ? axis.getEnd() : axis.getStart();
        List<SimpleWire> connectedParts = getConnectedParts(index);
        Optional<SimpleWire> startingPart = connectedParts.stream().filter(input -> input.getAxis().isOnEdges(workingPoint)).findFirst();
        if (startingPart.isPresent()) {
            movePart(startingPart.get(), direction, length);
            rebuildBounds();
        } else {
            stretchOnly(part, direction, length);
            return true;
        }

        return false;
    }

    /**
     * Расятгивает заданный кусок в его направлении. Опасный метод, так как не учитывает соседних кусков.
     * Использовать крайне осторожно.
     *
     * @param part
     * @param direction
     * @param length
     */
    private void stretchOnly(SimpleWire part, Direction direction, double length) {
        Edge axis = part.getAxis();
        axis.stretch(direction, length);
        rebuildBounds();
    }

    public void stretchOnly(SimpleWire part, Point working_point, Direction direction, double length) {
        Edge axis = part.getAxis();
        axis.stretch(working_point, direction, length, true);
        rebuildBounds();
    }

    Predicate<SimpleWire> orientationPredicate() {
        return new Predicate<SimpleWire>() {
            @Override
            public boolean apply(SimpleWire input) {
                return input.getAxis().getOrientation() == orientation;
            }
        };
    }

    Predicate<SimpleWire> orientationPredicate(final Orientation orientation) {
        return new Predicate<SimpleWire>() {
            @Override
            public boolean apply(SimpleWire input) {
                return input.getAxis().getOrientation() == orientation;
            }
        };
    }

    public List<SimpleWire> orientationParts() {
        return orientationParts(orientationPredicate());
    }

    List<SimpleWire> orientationParts(Predicate<SimpleWire> cmp) {
        return Lists.newArrayList(Iterables.filter(parts, cmp));
    }

    List<SimpleWire> orientationParts(Direction dir) {
        return Lists.newArrayList(Iterables.filter(parts, orientationPredicate(dir.orthogonal().toOrientation())));
    }

    List<SimpleWire> orientationParts(Orientation orientation) {
        return Lists.newArrayList(Iterables.filter(parts, orientationPredicate(orientation)));
    }

    /**
     * Поиск ближайшей части по выбранному направлению. Анализируются только
     * те отрезки шины, чьи направления перпендикулярны заданному.
     *
     * @param point Начальная точка поиска.
     * @param direction Направления поиска.
     *
     * @return Ближайшая, к заданной точке поиска, часть шины.
     */
    public Optional<SimpleWire> getClosestPart(Point point, Direction direction) {
        return getClosestPart(point, direction, orientationPredicate(direction.orthogonal().toOrientation()));
    }

    /**
     * Поиск ближайшей части по выбранному направлению. Анализируются только
     * те отрезки шины, которые удовлетворяют заданному предикату.
     *
     * @param point Начальная точка поиска.
     * @param direction Направления поиска.
     * @param predicate Предикат отбора шин.
     *
     * @return Ближайшая, к заданной точке поиска, часть шины.
     */
    public Optional<SimpleWire> getClosestPart(Point point, Direction direction, Predicate<SimpleWire> predicate) {
        Edge ray = Edge.ray(point, direction);
        List<Pair<SimpleWire, Double>> distances = Lists.newArrayList();
        for (SimpleWire part : orientationParts(predicate)) {
            Edge axis = part.getAxis();
            if (axis.cross(ray) == Edge.EdgeRelation.SKEW_CROSS) {
                distances.add(Pair.of(part, axis.distanceToPoint(point)));
            }
        }

        if (distances.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(Collections.min(distances, new Comparator<Pair<SimpleWire, Double>>() {
            @Override
            public int compare(Pair<SimpleWire, Double> o1, Pair<SimpleWire, Double> o2) {
                return o1.right.compareTo(o2.right);
            }
        }).left);
    }

    public Optional<SimpleWire> findPartWithPoint(Point point) {
        Collection<SimpleWire> found_parts = new ArrayList<>();
        for (SimpleWire part : parts) {
            Edge axis = part.getAxis();
            if (axis.isOnEdges(point)) {
                found_parts.add(part);
            }
        }

        if (found_parts.isEmpty()) {
            return Optional.empty();
        }

        for (SimpleWire found_part : found_parts) {
            if (found_part.getAxis().isPoint()) {
                return Optional.of(found_part);
            }
        }

        return Optional.of(found_parts.iterator().next());
    }

    public Optional<SimpleWire> findLink(Point point) {
        return parts.stream().filter(part -> part.isLink() && part.getAxis().isOnEdges(point)).findAny();
    }

    /**
     * Удаляет одинаковые, подряд идущие куски шины.
     */
    private void removeIdenticalParts() {
        for (ListIterator<SimpleWire> i = parts.listIterator(); i.hasNext();) {
            SimpleWire part = i.next();

            while (i.hasNext()) {
                SimpleWire next = i.next();
                if (next.getAxis().eq(part.getAxis())) {
                    i.remove();
                } else {
                    i.previous();
                    break;
                }
            }
        }

        rebuildBounds();
    }

    public boolean hasEmptyPartsOnEdges() {
        if (size() < 2) {
            return false;
        }

        return parts.get(0).getAxis().isPoint() || parts.get(size() - 1).getAxis().isPoint();
    }


    // todo think about connecting parts.
    /**
     * Удаляет "пустые" отрезки шины, соединяя соседнии отрезки между собой.
     */
    public List<SimpleWire> removeEmptyParts() {
        return removeEmptyParts(true);
    }

    public List<SimpleWire> removeEmptyParts(boolean removePartsOnEdges) {
        removeIdenticalParts();
        List<SimpleWire> unitedParts = Lists.newArrayList();
        for (ListIterator<SimpleWire> i = parts.listIterator(); i.hasNext();) {
            SimpleWire part = i.next();
            if (part.isLink()) {
                if ((part.getIndex() == 0 || part.getIndex() == size() - 1) && !removePartsOnEdges) {
                    continue;
                }
                // we found an empty link.
                List<SimpleWire> connectedParts = getConnectedParts(part);
                if (connectedParts.size() <= 1) {
                    i.remove();
                    continue;
                }

                Point commonPoint = part.getAxis().getStart();
                SimpleWire one = connectedParts.get(0);
                SimpleWire another = connectedParts.get(1);

                if (one.getAxis().getDirection().isOrthogonal(another.getAxis().getDirection())) {
                    i.remove();
                    continue;
                }

                Edge newEdge = Edge.of(one.getAxis().getOtherPoint(commonPoint),
                        another.getAxis().getOtherPoint(commonPoint));


                SimpleWire.Builder united = new SimpleWire.Builder(this);
                united.setAxis(newEdge).setMaxLength(one.maxLength).setStretchable(one.stretchable).setMovable(one.movable);
                SimpleWire unitedPart = united.build();

                unitedParts.add(unitedPart);

                i.previous();
                SimpleWire previous = i.previous();
                i.remove();

                SimpleWire next = i.next();
                i.remove();

                next = i.next();
                i.remove();

                i.add(unitedPart);
            }
        }

        rebuildBounds();

        return unitedParts;
    }

    public void removeEmptyPartsOnEdges() {
        if (size() < 2) {
            return;
        }

        SimpleWire first = getFirstPart();
        SimpleWire last = getLastPart();

        if (first.getAxis().isPoint()) {
            parts.remove(first);
            removeEmptyPartsOnEdges();
        }

        if (last.getAxis().isPoint()) {
            parts.remove(last);
            removeEmptyPartsOnEdges();
        }
    }

    private boolean hasEmptyLink(Point p) {
        for (SimpleWire part : parts) {
            Edge partAxis = part.getAxis();
            if (partAxis.isPoint() && partAxis.getStart().eq(p)) {
                return true;
            }
        }

        return false;
    }

    public List<SimpleWire> createAnEmptyLink(Point p, Direction direction) {
        Optional<SimpleWire> closestPart = getClosestPart(p, direction);
        if (closestPart.isPresent()) {
            SimpleWire part = closestPart.get();
            if (part.deformable) { // only in that case we can create an empty link
                Edge axis = part.getAxis();
                Point deformPoint = getDeformPoint(axis, p, direction);
                if (axis.isOnEdges(deformPoint)) {
                    List<SimpleWire> connectedParts = getConnectedParts(part);
                    if (connectedParts.size() > 1 || connectedParts.stream().filter(sw -> sw.getAxis().isOnEdges(deformPoint)).count() > 0) {
                        return Lists.newArrayList();
                    }
                }
                return createAnEmptyLink(deformPoint, part, maxBendLength);
            }
        }
        if (!isConnected()) {
//            throw new UnexpectedException("not connected");
        }
        return Lists.newArrayList();
    }

    // TODO links on edges
    protected List<SimpleWire> createAnEmptyLink(Point p, SimpleWire closestPart, double maxBendLength) {
        Preconditions.checkArgument(closestPart.getAxis().isPointInOrOnEdges(p),
                "Given wire part does not contain link point: part=" + closestPart.getAxis() + ", point=" + p);
        if (!isChained()) {
            //throw new UnexpectedException("wire should be chained");
        }
        if (!hasEmptyLink(p)) {
            Edge partAxis = closestPart.getAxis();

            Point start = partAxis.getStart();
            Point end = partAxis.getEnd();

            SimpleWire.Builder link_builder = new SimpleWire.Builder(closestPart);
            link_builder.setAxis(Edge.of(p.clone(), p.clone()));
            link_builder.setMaxLength(maxBendLength);
            link_builder.setStretchable(closestPart.isMovable()); // link is always stretchable
            link_builder.setMovable(true);
            SimpleWire link = link_builder.build();

            SimpleWire.Builder left_builder = new SimpleWire.Builder(closestPart);
            left_builder.setAxis(Edge.of(start.clone(), link.getAxis().getStart().clone()));
            SimpleWire left = left_builder.build();

            SimpleWire.Builder right_builder = new SimpleWire.Builder(closestPart);
            right_builder.setAxis(Edge.of(link.getAxis().getEnd().clone(), end.clone()));
            SimpleWire right = right_builder.build();

            ListIterator<SimpleWire> i = parts.listIterator(parts.indexOf(closestPart));
            i.next();
            i.remove();

            i.add(left);
            i.add(link);
            i.add(right);

            if (!isChained()) {
//                throw new UnexpectedException("wire should be chained");
            }

            return Lists.newArrayList(left, link, right);
        }

        return Lists.newArrayList();
    }

    public SimpleWire addEmptyLinkToPart(SimpleWire part, Point p) {
        SimpleWire.Builder link_builder = new SimpleWire.Builder(this);
        link_builder.setAxis(Edge.of(p.clone(), p.clone()));
        link_builder.setMaxLength(maxBendLength);
        link_builder.setStretchable(true); // link is always stretchable
        SimpleWire link = link_builder.build();
        int index = indexOf(part);
        parts.add(index > 0 ? index + 1 : index, link);

        return link;
    }

    private boolean crosses(final Point p, final Direction direction) {
        return Iterables.any(Lists.transform(parts, new Function<SimpleWire, Edge>() {
            @Override
            public Edge apply(SimpleWire input) {
                return input.getAxis();
            }
        }), new Predicate<Edge>() {
            @Override
            public boolean apply(Edge input) {
                return Edge.ray(p, direction).cross(input) == Edge.EdgeRelation.SKEW_CROSS;
            }
        });
    }

    /**
     * Скорректировать длину кусочков.
     * @param direction Направление коррекции.
     */
    // TODO вообще-то, нужно еще смотреть, что нам ничего не мешает, однако, если на что-то мешает, это странная ситуация
    // TODO так как коррекция должна вызываться сразу! после деформации.
    // TODO fails to work with no knees
    public void correct(Direction direction) {
        while (!checkLength(direction.toOrientation())) { // we need this, because we can make long parts while correcting the other ones
            for (SimpleWire part : orientationParts(orientationPredicate(direction.toOrientation()))) {
                double length = part.length();
                if (length > part.maxLength) {
                    stretch(part, direction.opposite(), -(length - part.maxLength));
                }
            }
        }
    }

    /**
     * Проверка, является ли переданный отрезок шины максимальным по заданному направлению.
     *
     * @param part Отрезок шины.
     * @param direction Направление поиска.
     * @return true, если отрезок максимальный.
     */
    private boolean isMax(SimpleWire part, Direction direction) {
        Preconditions.checkState(!parts.isEmpty(), "There are no parts in this bus");
        return part.equals(Collections.max(orientationParts(direction),
                axisComparator(direction.getEdgeComparator())));
    }

    public SimpleWire getMaxPart(Direction direction) {
        Preconditions.checkState(!parts.isEmpty(), "There are no parts in this bus");
        return Collections.max(orientationParts(direction),
                axisComparator(direction.getEdgeComparator()));
    }

    public SimpleWire getMinPart(Direction direction) {
        Preconditions.checkState(!parts.isEmpty(), "There are no parts in this bus");
        return Collections.max(orientationParts(direction),
                axisComparator(direction.getEdgeComparator()));
    }

    public Optional<SimpleWire> closestWithSameOrientation(final SimpleWire part) {
        final Edge axis = part.getAxis();
        List<SimpleWire> orientationParts = orientationParts(axis.getOrientation());
        orientationParts.remove(part);
        return axis.closestEdge(orientationParts, SimpleWire::getAxis);
    }

    static Comparator<SimpleWire> axisComparator(final Comparator<Edge> cmp) {
        return (o1, o2) -> {
            if (o1 != null && o2 != null) {
                return cmp.compare(o1.getAxis(), o2.getAxis());
            }

            return 0;
        };
    }

    @Override
    public Wire clone() {
        Wire clone = (Wire) super.clone();
        clone.connections = CollectionUtils.clone(connections, ConnectionPoint.class);
        clone.parts = Lists.newArrayList(CollectionUtils.clone(parts, SimpleWire.class));
        for (SimpleWire part : clone.parts) {
            part.setWire(clone);
        }

        return clone;
    }

    @Override
    public boolean move(double dx, double dy) {
        return moveParts(parts, dx, dy);
    }

    @Override
    public Collection<? extends TopologyElement> getElements() {
        return ImmutableList.copyOf(parts);
    }

    public static class Builder {
        private final Wire WIRE = new Wire();

        public Builder() {
        }

        public Builder(Orientation orientation) {
            WIRE.orientation = orientation;
        }

        public Wire build() {
            return WIRE;
        }

        public void setParts(List<SimpleWire> parts) {
            WIRE.parts = parts;
        }

        public void setWidth(double width) {
            WIRE.width = width;
        }

        public void setWidthAtBorder(double widthAtBorder) {
            WIRE.widthAtBorder = widthAtBorder;
        }

        public void setMaxLength(double maxLength) {
            WIRE.maxLength = maxLength;
        }

        public void setMaxBendLength(double maxBendLength) {
            WIRE.maxBendLength = maxBendLength;
        }

        public void setMaxBendCount(int maxBendCount) {
            WIRE.maxBendCount = maxBendCount;
        }

        public void setTotalBendLength(double totalBendLength) {
            WIRE.totalBendLength = totalBendLength;
        }

        public void setOrientation(Orientation orientation) {
            WIRE.orientation = orientation;
        }

        public void setConnections(Collection<ConnectionPoint> connections) {
            WIRE.connections = connections;
        }

        public Builder setName(String name) {
            WIRE.name = name;
            return this;
        }

    }
}
