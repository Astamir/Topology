package ru.etu.astamir.model.legacy;

import com.google.common.base.*;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import ru.etu.astamir.common.Pair;
import ru.etu.astamir.common.collections.UniqueIterator;
import ru.etu.astamir.compression.Border;
import ru.etu.astamir.geom.common.*;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.geom.common.Rectangle;
import ru.etu.astamir.model.*;
import ru.etu.astamir.model.contacts.Contact;
import ru.etu.astamir.model.contacts.Contactable;
import ru.etu.astamir.model.exceptions.UnexpectedException;
import ru.etu.astamir.model.technology.Technology;

import javax.xml.bind.annotation.*;
import java.awt.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;

/**
 * Класс элементарной шины. Представляется точками на осевой линии и шириной.
 *
 * @version 1.0
 */
// TODO таки доделать объединение границ.
//@XmlRootElement
public class Bus extends LegacyTopologyElement implements Contactable, Movable, Serializable, Cloneable {
    /**
     * Это расстояние от осевой линии.
     */
    @XmlAttribute
    double width;

    /**
     * Расстояние от осевой линии на границе шины.
     */
    @XmlAttribute
    double widthAtBorder;

    /**
     * Максимальная протяженность шины.
     *
     * Double.MAX_VALUE если длина шины не ограничена.
     */
    @XmlAttribute
    double maxLength = Double.MAX_VALUE;

    /**
     * Максимальная длина коленца.
     */
    @XmlAttribute
    double maxBendLength = Double.MAX_VALUE;

    /**
     * Миксимальное число коленец.
     */
    @XmlAttribute
    int maxBendCount = Integer.MAX_VALUE;

    /**
     * Максимальная общая длина коленец. Сумма длин всех коленец не должна превышать это значение.
     */
    @XmlAttribute
    double totalBendLength = Double.MAX_VALUE;

    /**
     * Ориентация шины.
     */
    @XmlAttribute
    Orientation orientation = Orientation.VERTICAL;

    /**
     *  Контакты к шине.
     */
    @XmlTransient
    List<Contact> contacts = Lists.newArrayList();

    /**
     * Кусочки шины.
     */
    @XmlElementWrapper
    protected List<BusPart> parts = Lists.newArrayList();

    Polygon bounds;

    Material material = Material.UNKNOWN;

    boolean axisVisible = true;
    
    Class<? extends LegacyTopologyElement> clazz = getClass();

    protected Bus() {
        // TODO fill in something
    }

    public Bus(TopologyLayer layer, Point gridCoordinates, Material material, double width, double widthAtBorder,
               double maxLength, Orientation orientation, List<BusPart> parts, List<Contact> contacts) {
        super(gridCoordinates, layer);
        this.material = material;
        this.width = width;
        this.widthAtBorder = widthAtBorder;
        this.maxLength = maxLength;
        this.orientation = orientation;
        this.contacts = contacts;
        setParts(parts);
    }

    public Bus(TopologyLayer layer, Point gridCoordinates, Material material, double width) {
        this(layer, gridCoordinates, material, width, width, Double.MAX_VALUE, Orientation.HORIZONTAL, new ArrayList<BusPart>(),
                new ArrayList<Contact>());
    }     

    public void setFirstPart(Point start, Point end, double maxLength, boolean stretchable) {
        BusPart part = new BusPart(start, end, maxLength, stretchable, true);
        this.orientation = Edge.of(start, end).getOrientation();
        setParts(part);
    }

    public void setFirstPart(Edge axis, double maxLength, boolean stretchable) {
        BusPart part = new BusPart(axis, maxLength, stretchable, true);
        this.orientation = axis.getOrientation();
        setParts(part);
    }

    public void setFirstPart(Edge axis, double maxLength, boolean stretchable, boolean movable) {
        BusPart part = new BusPart(axis, maxLength, stretchable, movable);
        this.orientation = axis.getOrientation();
        setParts(part);
    }

    /**
     * Установка первого элемента шины. Первый элемент шины задает ее будущуюю ориентацию.
     *
     * @param start Точка начала.
     * @param direction Направление.
     * @param length Длина кусочка.
     * @param maxLength Максимальная длина кусочка.
     * @param stretchable Возможность куска растягиваться.
     * @param movable Возможность кусочка менять свое местоположение.
     */
    public void setFirstPart(Point start, Direction direction, double length, double maxLength,
                             boolean stretchable, boolean movable) {
        BusPart part = new BusPart(start, direction, length, maxLength, stretchable, movable);
        this.orientation = direction.toOrientation();
        setParts(part);
    }

    public void setFirstPart(Point start, Direction direction, double length, double maxLength,
                             boolean stretchable) {
        this.setFirstPart(start, direction, length, maxLength, stretchable, true);
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
    public void addPart(Direction direction, double length, double maxLength, boolean stretchable,
                        boolean movable) {
        if (parts.isEmpty()) {
            throw new IllegalStateException("Bus must have at least one part");
        }

        BusPart lastPart = parts.get(parts.size() - 1);
        Direction lastDirection = lastPart.getAxis().getDirection();
        if (lastDirection.isReverse(direction)) {
            throw new UnexpectedException("Direction can not be reverse");
        }

        Point start = lastPart.getAxis().getEnd();
        if (lastDirection == direction) {
            start = (Point) lastPart.getAxis().getEnd().clone();
            BusPart part = new BusPart(Edge.of(lastPart.getAxis().getEnd(), start),
                    maxLength, stretchable, movable);
            parts.add(part);
            return;
        }

        Edge partAxis = Edge.of(start, direction, length);
        if (partAxis.getEnd().equals(parts.get(0).getAxis().getStart())) {
            partAxis.setEnd(parts.get(0).getAxis().getStart());
        }
        BusPart part = new BusPart(partAxis, maxLength, stretchable, movable);
        parts.add(part);
        ensureIndices();
        rebuildBounds();
    }

    public void addPart(Direction direction, double length, double maxLength, boolean stretchable) {
        this.addPart(direction, length, maxLength, stretchable, true);
    }

    public BusPart getLastPart() {
        return parts.get(parts.size() - 1);
    }

    public BusPart getFirstPart() {
        return parts.get(0);
    }

    public boolean hasParts() {
        return size() > 0;
    }


    // TODO нестабильно
    Polygon addPartToBounds(Polygon bounds, BusPart part) {
        Rectangle partBounds = part.getBounds();

        // 1. find pure crossing point and add it.
        // 2. find that junction common point and do not touch it
        // 3. delete all remaining common points

        List<Point> currentPoints = Lists.newArrayList(bounds.vertices());

        for (Point point : partBounds.vertices()) {
            boolean good = true;
            for (Edge edge : bounds.edges()) {
                if (edge.isPointInOrOnEdges(point)) {
                    good = false;
                }
            }

            if (good) {
                currentPoints.add((Point) point.clone());
            }
        }

        for (Point point : bounds.vertices()) {
            for (Edge edge : partBounds.edges()) {
                if (edge.isPointIn(point)) {
                    currentPoints.remove(point.clone());
                }
            }
        }

        for (Edge edge : partBounds.edges()) {
            for (Edge e : bounds.edges())
                if (e.pureCross(edge)) {
                    currentPoints.add(e.crossing(edge));
                }
        }

        Polygon polygon = Polygon.of(Lists.newArrayList(UniqueIterator.create(currentPoints)));
        //polygon.walk();
        return polygon;
    }

    Polygon addPartToBounds1(Polygon bounds, BusPart part) {
        Polygon polygon = Polygon.of(bounds.vertices());
        polygon.addAll(part.getBounds().vertices());
        return polygon;
    }

    public boolean isDeformed() {
        return parts.size() > 1;
    }

    @Override
    @XmlTransient
    public List<Contact> getContacts() {
        return contacts;
    }

    @Override
    public boolean addContact(Contact contact) {
        return contacts.add(contact);
    }

    public double getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(double maxLength) {
        this.maxLength = maxLength;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public boolean isAxisVisible() {
        return axisVisible;
    }

    public void setAxisVisible(boolean axisVisible) {
        this.axisVisible = axisVisible;
    }

    public double getWidthAtBorder() {
        return widthAtBorder;
    }

    public void setWidthAtBorder(double widthAtBorder) {
        this.widthAtBorder = widthAtBorder;
    }

    public ImmutableList<BusPart> getParts() {
        ImmutableList.Builder<BusPart> builder = ImmutableList.builder();
        return builder.addAll(parts).build();
    }

    private void setParts(List<BusPart> parts) {
        this.parts.clear();
        this.parts.addAll(parts);

        ensureIndices();
        rebuildBounds();
    }

    public void setParts(BusPart... parts) {
        this.parts = Lists.newArrayList(parts);

        ensureIndices();
        rebuildBounds();
    }

    public void setParts(Collection<BusPart> parts) {
        this.parts = Lists.newArrayList(parts);

        ensureIndices();
        rebuildBounds();
    }


    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public double getMaxBendLength() {
        return maxBendLength;
    }

    public void setMaxBendLength(double maxBendLength) {
        this.maxBendLength = maxBendLength;
    }

    public void setTotalBendLength(double totalBendLength) {
        this.totalBendLength = totalBendLength;
    }

    public double getLength() {
        double length = 0.0;
        for (BusPart part : parts) {
            length += part.length();
        }

        return length;
    }

    public double getBoundsLength() {
        double length = 0.0;
        for (BusPart part : parts) {
            length += part.boundsLength();
        }

        return length;
    }

    public Orientation getOrientation() {
        return orientation;
    }
    
    public int indexOf(BusPart part) {
        ensureIndices();
        int index = parts.indexOf(part);
        return index != part.index ? part.index : index;
    }

    public int size() {
        return parts.size();
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
    static boolean moveParts(Collection<BusPart> partsToMove, double dx, double dy) {
        List<Point> vertices = Lists.newArrayList();
        for (BusPart part : partsToMove) {
            Point start = part.getAxis().getStart();
            Point end = part.getAxis().getEnd();

            if (start.equals(end)) {
                for (Iterator<Point> i = vertices.iterator(); i.hasNext(); ) {
                    Point point = i.next();
                    if (point.equals(start)) {
                        i.remove();
                    }
                }

                vertices.add(start);
                vertices.add(end);
                continue;
            }

            if (!vertices.contains(start)) {
                vertices.add(start);
            }


            if (!vertices.contains(end) || part.isLast()) { // we always add an end vertex of the last part
                vertices.add(end);
            }

        }


        for (BusPart part : partsToMove) {
            Point start = part.getAxis().getStart();
            Point end = part.getAxis().getEnd();
            if (vertices.contains(start)) {
                start.move(dx, dy);
                vertices.remove(start);
            }

            if (vertices.contains(end)) {
                end.move(dx, dy);
                vertices.remove(end);
            }

            part.rebuildBounds();
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
    public static boolean moveParts(Collection<BusPart> partsToMove, Direction direction, double width) {
        double signedD = width * direction.getDirectionSign();
        if (direction.isLeftOrRight()) {
            return moveParts(partsToMove, signedD, 0);
        } else {
            return moveParts(partsToMove, 0, signedD);
        }
    }

    @Override
    public boolean move(double dx, double dy) {
        final boolean move = moveParts(parts, dx, dy);
        if (move) {
            rebuildBounds();
        }

        return move;
    }

    private Set<Point> getVertices() {
        Set<Point> vertices = Sets.newHashSet();
        for (BusPart part : parts) {
            vertices.add(part.getAxis().getStart());
            vertices.add(part.getAxis().getEnd());
        }

        return vertices;
    }

    /**
     * Получение всех соседних кусоков заданного.
     *
     * @param part Собственно заданный кусочек.
     * @return Список прямых соседий заданного кусочка.
     */
    public List<BusPart> getConnectedParts(BusPart part) {
        int index = indexOf(part);
        Preconditions.checkElementIndex(index, size(), "bus does not contain given part : " + part);
        
        return getConnectedParts(index);
    }

    public List<BusPart> getConnectedParts(int index) {
        int size = parts.size();
        if (size <= 1) {
            return Lists.newArrayList();
        }

        if (index == 0) {
            return Lists.newArrayList(parts.get(index + 1));
        }
        
        if (index == size - 1) {
            return Lists.newArrayList(parts.get(index - 1));
        }
        
        if (size >= 3) {
            return Lists.newArrayList(parts.get(index - 1), parts.get(index + 1));
        }
        
        return Lists.newArrayList();
    }

    // TODO доделать расчет длины перемещения
    @Deprecated
    boolean movePart(int partIndex, Direction direction, double width) {
        if (width == 0) {
            return true;
        }

        if (partIndex < 0 || partIndex > parts.size() - 1) {
            return false;
        }

        BusPart part = parts.get(partIndex);
        if (!part.movable) {
            return false; // we can't even move this part.
        }

        Direction partDirection = part.getAxis().getDirection();
        List<BusPart> connectedParts = getConnectedParts(part);
        if (direction.isOrthogonal(partDirection)) {
            // firstly, we have to be sure that all connected parts can be stretched
            boolean canStretch = Iterables.all(connectedParts, new Predicate<BusPart>() {
                @Override
                public boolean apply(BusPart input) {
                    return input.stretchable;
                }
            });

            if (canStretch) { // if we actually can stretch connected parts
                // we have to find out real moving length.
                double correctedWidth = direction.getDirectionSign() * width;
                for (BusPart connectedPart : connectedParts) {
                    double length = connectedPart.length();
                    final double maxLength = connectedPart.maxLength;
                    //TODO
                }

                if (correctedWidth != 0) {
                    part.moveDirectly(direction, correctedWidth); // if direction is orthogonal we just moving the part
                    rebuildBounds();
                    return true;
                }

                part.moveDirectly(direction, width); // if direction is orthogonal we just moving the part
                rebuildBounds();
                return true;
            }

            return false;
        }
        
        // in this case we have to move some connected parts too.
        List<BusPart> partsToMove = Lists.newArrayList(connectedParts);
        partsToMove.add(part);
        moveParts(partsToMove, direction, width);

        rebuildBounds();

        // TODO проверки на максимальную длину
        return true;
    }

    @Deprecated
    boolean movePart(BusPart part, Direction direction, double width) {
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
    public boolean directlyMovePart(int partIndex, Direction direction, double width) {
        if (width == 0) {
            return true;
        }

        if (partIndex < 0 || partIndex > parts.size() - 1) {
            return false;
        }

        BusPart part = parts.get(partIndex);
        if (!part.movable) {
            return false; // we can't even move this part.
        }

        Direction partDirection = part.getAxis().getDirection();
        List<BusPart> connectedParts = getConnectedParts(part);
        if (direction.isOrthogonal(partDirection)) {
            // firstly, we have to be sure that all connected parts can be stretched
            boolean canStretch = Iterables.all(connectedParts, new Predicate<BusPart>() {
                @Override
                public boolean apply(BusPart input) {
                    return input.stretchable;
                }
            });

            if (canStretch /*&& !isFlapAttached(partIndex)*/) { // if we actually can stretch connected parts
                part.moveDirectly(direction, width); // if direction is orthogonal we just moving the part
                rebuildBounds();
                return true;
            }

            return false;
        }

        // in this case we have to move some connected parts too.
        List<BusPart> partsToMove = Lists.newArrayList(connectedParts);
        boolean canMove = Iterables.all(partsToMove, new Predicate<BusPart>() {
            @Override
            public boolean apply(BusPart input) {
                return input.movable && Iterables.all(getConnectedParts(input), new Predicate<BusPart>() {
                    @Override
                    public boolean apply(BusPart input) {
                        return input.stretchable;
                    }
                });
            }
        });
        if (canMove) {
            partsToMove.add(part);
            moveParts(partsToMove, direction, width);
            rebuildBounds();

            return true;
        }

        return false;
    }

    public boolean isFlapAttached(int partIndex) {
        return (partIndex == 0 && hasFlap(Flap.Position.START)) || (partIndex == size() - 1 && hasFlap(Flap.Position.END));
    }

    public boolean hasFlap(final Flap.Position position) {
        return Iterables.any(getContacts(), new Predicate<Contact>() {
            @Override
            public boolean apply(Contact input) {
                return input instanceof Flap && ((Flap) input).getPosition() == position;
            }
        });
    }

    /**
     *
     * @param part
     * @param direction
     * @param width
     * @see #directlyMovePart(int, ru.etu.astamir.geom.common.Direction, double)
     * @return
     */
    public boolean directlyMovePart(BusPart part, Direction direction, double width) {
        int index = parts.indexOf(part);
        return directlyMovePart(index, direction, width);
    }

    /**
     * Проверка кусочков шины на нарушение максимальной или минимальной длины.
     *
     * @return false, если размеры хотя бы одного кусчка шины нарушают заданные им ограничения.
     */
    private boolean checkLength() {
        for (BusPart part : parts) {
            if (part.length() > part.getMaxLength()) {
                return false;
            }
        }

        return true;
    }

    private boolean checkLength(Orientation orientation) {
        for (BusPart part : orientationParts(orientation)) {
            if (part.length() > part.getMaxLength()) {
                return false;
            }
        }

        return true;
    }

    public void deform(final Point p, final Direction direction, Direction half, double width) {
        // first we have to figure out which bus part to deform
        List<Pair<BusPart, Double>> partsToDeform = Lists.newArrayList();

        // we have to look through all parts to find closest to our deform point.
        for (final BusPart part : parts) {
            final Edge partAxis = part.getAxis();

            Edge ray = Edge.of(p, direction, Float.MAX_VALUE);
            Edge.EdgeRelation relation = ray.cross(partAxis);
            Point deformPoint = ray.crossing(partAxis);

            if (relation == Edge.EdgeRelation.COLLINEAR && ray.overlays(partAxis)) {
                // now we have to stretch this part, and therefore all parts connected to it, if we can of course.
                if (part.stretchable) {
                    stretch(part, direction, width);
                    return;
                }
            } else if (relation == Edge.EdgeRelation.SKEW_CROSS) {
                if (part.deformable) { // if we can deform this part, we add to deform part candidates
                    partsToDeform.add(Pair.of(part, Point.distance(p, deformPoint)));
                }
            }
        }

        if (!partsToDeform.isEmpty()) { // figuring out closest part
            BusPart part = Collections.min(partsToDeform, new Comparator<Pair<BusPart, Double>>() {
                @Override
                public int compare(Pair<BusPart, Double> pair1, Pair<BusPart, Double> pair2) {
                    if (pair1 != null && pair2 != null) {
                        return Doubles.compare(pair1.right, pair2.right);
                    }

                    return 0;
                }
            }).left;
            
            int partIndex = indexOf(part);

            Edge partAxis = part.getAxis();
            Point deformPoint = getDeformPoint(partAxis, p, direction);
            
            
            Point start = partAxis.getStart();
            Point end = partAxis.getEnd();
            // Creating an empty link. We can't use method here cause we need to move parts later.
            BusPart link = new BusPart(part, Edge.of((Point) deformPoint.clone(), (Point)deformPoint.clone()));
            link.setStretchable(true);
            BusPart left = new BusPart(part, Edge.of(start, link.getAxis().getStart()));
            BusPart right = new BusPart(part, Edge.of(link.getAxis().getEnd(), end));

            // adding our empty link.
            ListIterator<BusPart> i = parts.listIterator(partIndex);
            i.next();
            i.remove();
            i.add(left);
            i.add(link);
            i.add(right);

            if (direction.isUpOrRight()) {
                if (half == Direction.RIGHT) {
                    directlyMovePart(parts.indexOf(right), direction, width);
                } else {
                    directlyMovePart(parts.indexOf(left), direction, width);
                }
            } else {
                if (half == Direction.RIGHT) {
                    directlyMovePart(parts.indexOf(left), direction, width);
                } else {
                    directlyMovePart(parts.indexOf(right), direction, width);
                }
            }

            ensureIndices();
        }
    }

    public void straighten(List<TopologyElement> elements, Border border, Direction direction, Technology.TechnologicalCharacteristics technology) {

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
    public boolean stretch(BusPart part, Direction direction, double length) {
        if (!part.stretchable) {
            return false; // we can't even stretch it.
        }

        Edge axis = part.getAxis();
        Direction axisDirection = axis.getDirection();
        if (!axisDirection.isSameOrientation(direction)) {
            return false;
        }

        int index = parts.indexOf(part);
        if (index < 0) {
            return false;
        }

        // all we have to do is to call movePart on the connected part if it exists, or simply stretch otherwise.
        final Point workingPoint = (axisDirection == direction) ? axis.getEnd() : axis.getStart();
        List<BusPart> connectedParts = getConnectedParts(index);
        Optional<BusPart> startingPart = Iterables.tryFind(connectedParts, new Predicate<BusPart>() {
            @Override
            public boolean apply(BusPart input) {
                return input.getAxis().isOnEdges(workingPoint);
            }
        });
        if (startingPart.isPresent()) {
            int ourIndex = indexOf(part);
            int startingIndex = indexOf(startingPart.get());
            if (ourIndex < startingIndex) {
                moveParts(Lists.newArrayList(parts.listIterator(startingIndex)), direction, length);
            } else {
                moveParts(Lists.newArrayList(parts.subList(0, ourIndex)), direction, length);
            }

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
    private void stretchOnly(BusPart part, Direction direction, double length) {
        Edge axis = part.getAxis();
        axis.stretch(direction, length);
        rebuildBounds();
    }

    Predicate<BusPart> orientationPredicate() {
        return new Predicate<BusPart>() {
            @Override
            public boolean apply(BusPart input) {
                return input.getAxis().getOrientation() == orientation;
            }
        };
    }

    Predicate<BusPart> orientationPredicate(final Orientation orientation) {
        return new Predicate<BusPart>() {
            @Override
            public boolean apply(BusPart input) {
                return input.getAxis().getOrientation() == orientation;
            }
        };
    }

    public List<BusPart> orientationParts() {
        return orientationParts(orientationPredicate());
    }

    List<BusPart> orientationParts(Predicate<BusPart> cmp) {
        return Lists.newArrayList(Iterables.filter(parts, cmp));
    }

    List<BusPart> orientationParts(Direction dir) {
        return Lists.newArrayList(Iterables.filter(parts, orientationPredicate(dir.getOrthogonalDirection().toOrientation())));
    }

    List<BusPart> orientationParts(Orientation orientation) {
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
    public Optional<BusPart> getClosestPart(Point point, Direction direction) {
        return getClosestPart(point, direction, orientationPredicate(direction.getOrthogonalDirection().toOrientation()));
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
    public Optional<BusPart> getClosestPart(Point point, Direction direction, Predicate<BusPart> predicate) {
        Edge ray = Edge.ray(point, direction);
        List<Pair<BusPart, Double>> distances = Lists.newArrayList();
        for (BusPart part : orientationParts(predicate)) {
            Edge axis = part.getAxis();
            if (axis.cross(ray) == Edge.EdgeRelation.SKEW_CROSS) {
                distances.add(Pair.of(part, axis.distanceToPoint(point)));
            }
        }

        if (distances.isEmpty()) {
            return Optional.absent();
        }

        return Optional.of(Collections.min(distances, new Comparator<Pair<BusPart, Double>>() {
            @Override
            public int compare(Pair<BusPart, Double> o1, Pair<BusPart, Double> o2) {
                return o1.right.compareTo(o2.right);
            }
        }).left);
    }

    /**
     * Удаляет одинаковые, подряд идущие куски шины.
     */
    private void removeIdenticalParts() {
        for (ListIterator<BusPart> i = parts.listIterator(); i.hasNext();) {
            BusPart part = i.next();

            while (i.hasNext()) {
                BusPart next = i.next();
                if (next.getAxis().equals(part.getAxis())) {
                    i.remove();
                    part.index = -1;
                } else {
                    i.previous();
                    break;
                }
            }
        }

        connectParts();
        ensureIndices();
        rebuildBounds();
    }

    public void removeInfidelParts() {
        for (Iterator<BusPart> it = parts.iterator();it.hasNext();) {
            BusPart part = it.next();
            Direction curDir = part.getAxis().getDirection();
            if (it.hasNext()) {
                BusPart next = it.next();
                Direction nextDir = next.getAxis().getDirection();
                if (nextDir.isReverse(curDir)) {
                    if (it.hasNext()) {
                        it.remove();
                        BusPart connector = it.next();
                        part.getAxis().setEnd(connector.getAxis().getStart());
                        part.rebuildBounds();
                    }
                }
            }
        }
    }

    private void connectParts() {
        Point end = null;
        for (BusPart part : parts) {
            Edge axis = part.getAxis();
            if (end != null && end != axis.getStart()) {
                axis.setStart(end);
            }

            end = axis.getEnd();
        }
    }


    /**
     * Удаляет "пустые" отрезки шины, соединяя соседнии отрезки между собой.
     */
    public List<BusPart> removeEmptyParts() {
        //removeIdenticalParts();
        List<BusPart> unitedParts = Lists.newArrayList();
        for (ListIterator<BusPart> i = parts.listIterator(); i.hasNext();) {
            BusPart part = i.next();
            if (part.getAxis().isPoint()) {
                // we found an empty link.
                List<BusPart> connectedParts = getConnectedParts(part);
                if (connectedParts.size() <= 1) {
                    i.remove();
                    part.index = - 1;
                    continue;
                }

                Point commonPoint = part.getAxis().getStart();
                BusPart one = connectedParts.get(0);
                BusPart another = connectedParts.get(1);

                Edge newEdge = Edge.of(one.getAxis().getOtherPoint(commonPoint),
                        another.getAxis().getOtherPoint(commonPoint));


                BusPart unitedPart = new BusPart(newEdge, one.maxLength, one.stretchable, one.movable);
                unitedParts.add(unitedPart);

                i.previous();
                BusPart previous = i.previous();
                i.remove();
                previous.index = -1;

                BusPart next = i.next();
                i.remove();
                next.index = -1;

                next = i.next();
                i.remove();
                next.index = -1;

                i.add(unitedPart);
            }
        }

        ensureIndices();
        rebuildBounds();

        return unitedParts;
    }


    @Deprecated
    public void correctBus() {
        for (BusPart part : parts) {
            part.getAxis().correct();
        }

        rebuildBounds();
    }



    private boolean hasEmptyLink(Point p) {
        for (BusPart part : parts) {
            Edge partAxis = part.getAxis();
            if (partAxis.isPoint() && partAxis.getStart().equals(p)) {
                return true;
            }
        }

        return false;
    }

    public List<BusPart> createAnEmptyLink(Point p, Direction direction) {
        Optional<BusPart> closestPart = getClosestPart(p, direction);
        if (closestPart.isPresent()) {
            BusPart part = closestPart.get();
            if (part.deformable) { // only in that case we can create an empty link
                return createAnEmptyLink(getDeformPoint(part.getAxis(), p, direction), part, maxBendLength);
            }
        }
        
        return Lists.newArrayList();
    }


    protected List<BusPart> createAnEmptyLink(Point p, BusPart closestPart, double maxBendLength) {
        Preconditions.checkArgument(closestPart.getAxis().isPointInOrOnEdges(p),
                "Given bus part does not contain link point");
        if (!hasEmptyLink(p)) {
            Edge partAxis = closestPart.getAxis();

            Point start = partAxis.getStart();
            Point end = partAxis.getEnd();

            BusPart link = new BusPart(closestPart, Edge.of((Point) p.clone(), (Point)p.clone()));
            link.setMaxLength(maxBendLength);
            link.setStretchable(closestPart.movable); // link is always stretchable

            BusPart left = new BusPart(closestPart, Edge.of(start, link.getAxis().getStart()));
            BusPart right = new BusPart(closestPart, Edge.of(link.getAxis().getEnd(), end));

            ListIterator<BusPart> i = parts.listIterator(parts.indexOf(closestPart));
            BusPart next = i.next();
            i.remove();
            next.index = -1;

            i.add(left);
            i.add(link);
            i.add(right);

            ensureIndices();
            return Lists.newArrayList(left, link, right);
        }
        
        return Lists.newArrayList();
    }

    // TODO look thorugh
    public boolean straighten(BusPart part, Border border, Direction direction, Double... additionalParams) {
//        List<BusPart> allConnectedParts = Lists.newArrayList(part);
//        List<BusPart> connectedParts = getConnectedParts(part);
//        for (BusPart connectedPart : connectedParts) {
//            List<BusPart> connectedParts1 = getConnectedParts(connectedPart);
//            connectedParts1.remove(part);
//            allConnectedParts.addAll(connectedParts1);
//        }
//
//        Collections.sort(allConnectedParts, axisComparator(direction.getEdgeComparator()));
//        if (allConnectedParts.indexOf(part) == allConnectedParts.size() - 1) { // if our part is max
//            return false; // we do nothing
//        }
//
//        BusPart maxPart = allConnectedParts.get(allConnectedParts.indexOf(part) + 1);
//
//        Edge partAxis = part.getAxis();
//        Point point = partAxis.getStart();
//        double d = BorderPart.of(maxPart).getMoveDistanceWithoutConstraints(direction, point); // dToMaxPart
//        Optional<BorderPart> closestPart = border.getClosestPartWithoutConstraints(partAxis, direction); // TODO доделать.
//        if (closestPart.isPresent()) {
//            d = Math.min(d, closestPart.get().getMoveDistance(part.getClass(), direction, point)); // TODO костыль
//        }
//
//        if (additionalParams.length > 0) { // if we have some additional params, we should consider them here.
//            double min = Collections.min(Lists.newArrayList(additionalParams));
//            d = Math.min(d, min);
//        }
//
//        directlyMovePart(parts.indexOf(part), direction, d);

        return true; //d > 0;
    }

    public void straighten(List<LegacyTopologyElement> elements, Border border, Direction direction) {
        // so, we got our border, and pretty much all the elements on the grid. Although it would
        // be preferably to get only some elements from the index forward(or backward) of our bus.
        // Since we move every part anyway, we can just use ray method and apply it to all the elements to see
        // if we may collide with some of them.
//        boolean hadBeenChanged;
//        do {
//            hadBeenChanged = false;
//            for (BusPart part : orientationParts(direction)) { // now that's tricky
//                Edge partAxis = part.getAxis();
//
//                List<Double> distToElems = Lists.newArrayList();
//
//                for (LegacyTopologyElement element : elements) {
//                    if (element instanceof Deformable) {
//                        Deformable deformable = (Deformable) element;
//                        Border b = new Border(direction.getOrthogonalDirection().toOrientation());
//                        b.overlay(BorderPart.of(deformable), direction);
//                        Optional<BorderPart> closestPart = b.getClosestPartWithoutConstraints(partAxis, direction);
//                        if (closestPart.isPresent()) {
//                            distToElems.add(closestPart.get().getMoveDistance(part.getActualClass(), direction, partAxis.getStart()));
//                        }
//                    } else {
//                        Polygon bounds = element.getBounds();
//                        Border b = Border.of(direction.getOrthogonalDirection().toOrientation(), bounds.edges(), element.getActualClass());
//                        Optional<BorderPart> closestPart = b.getClosestPartWithoutConstraints(partAxis, direction);
//                        if (closestPart.isPresent()) {
//                            distToElems.add(closestPart.get().getMoveDistance(part.getActualClass(), direction, partAxis.getStart()));
//                        }
//                    }
//                }
//
//                hadBeenChanged |= straighten(part, border, direction, distToElems.toArray(new Double[distToElems.size()]));
//            }
//
//            removeEmptyParts();
//        } while (hadBeenChanged);
    }

    // TODO look through
    public List<BusPart> imitate(Border border, Direction direction) {
        //  bus.correctBus();
        List<BusPart> emptyLinks = createEmptyLinks(border, direction);
//        for (Bus.BusPart part : parts) {
//            if (!part.getAxis().getOrientation().isOrthogonal(direction.toOrientation()) || part.getAxis().isPoint()) {
//                continue;
//            }
//
//            Optional<BorderPart> closestPart = border.getClosestPartWithConstraints(part.getAxis(), getClass(), direction);
//            if (closestPart.isPresent()) {
//                double distToMove = closestPart.get().getMoveDistance(getClass(),
//                        direction, part.getAxis().getStart());
//
//                directlyMovePart(part, direction, distToMove);
//            }
//        }
//
//        emptyLinks.addAll(removeEmptyParts());
        return emptyLinks;
    }

    // TODO look through
    private List<BusPart> createEmptyLinks(Border border, Direction direction) {
//        List<BorderPart> nonOrientParts =
//                Lists.newArrayList(Iterables.filter(border.getParts(), Predicates.not(border.orientationPredicate())));

        List<BusPart> newParts = Lists.newArrayList();
//
//        for (BorderPart part : nonOrientParts) {
//            double min = part.getMinDistance(getClass());
//            //Point one = (Point) part.getAxis().getStart().clone();
//            Edge axis = (Edge) part.getAxis().clone();
//            axis.correct();
//            Point one = (Point) (direction.isUpOrRight() ? axis.getStart() : axis.getEnd()).clone();
//            boolean oneIsGood = !isEndOrStart(one, direction.getOppositeDirection()) && crosses(one, direction.getOppositeDirection());
//            GeomUtils.move(one, direction.clockwise(), min);
//            oneIsGood &= !isEndOrStart(one, direction.getOppositeDirection());
//            //Point another = (Point) part.getAxis().getStart().clone();
//            Point another = (Point) (direction.isUpOrRight() ? axis.getStart() : axis.getEnd()).clone();
//            boolean anotherIsGood = !isEndOrStart(another, direction.getOppositeDirection()) && crosses(another, direction.getOppositeDirection());
//            GeomUtils.move(another, direction.counterClockwise(), min);
//            anotherIsGood &= !isEndOrStart(another, direction.getOppositeDirection());
//
//            if (oneIsGood) newParts.addAll(createAnEmptyLink(one, direction.getOppositeDirection()));
//            if (anotherIsGood) newParts.addAll(createAnEmptyLink(another, direction.getOppositeDirection()));
//        }

        return newParts;
    }
    
    private boolean crosses(final Point p, final Direction direction) {
        return Iterables.any(Lists.transform(parts, new Function<BusPart, Edge>() {
            @Override
            public Edge apply(BusPart input) {
                return input.getAxis();
            }
        }), new Predicate<Edge>() {
            @Override
            public boolean apply(Edge input) {
                return Edge.ray(p, direction).cross(input) == Edge.EdgeRelation.SKEW_CROSS;
            }
        });
    }
    
    private boolean isEndOrStart(Point p, Direction direction) {
        if (size() < 1) {
            throw new UnexpectedException();
        }

        if (size() == 1) {
            Edge axis = parts.get(0).getAxis();
            Edge ray = Edge.ray(p, direction);
            return ray.cross(axis) == Edge.EdgeRelation.SKEW_CROSS && axis.isOnEdges(ray.crossing(axis));
        }

        Edge first = parts.get(0).getAxis();
        first.correct();
        Edge last = parts.get(size() - 1).getAxis();

        Edge ray = Edge.ray(p, direction);

        boolean isFirst = false;
        if (ray.cross(first) == Edge.EdgeRelation.SKEW_CROSS) {
            isFirst = first.getStart().equals(ray.crossing(first));
        }

        if (ray.cross(last) == Edge.EdgeRelation.SKEW_CROSS) {
            return isFirst || last.getEnd().equals(ray.crossing(last));
        }

        return false;
    }

    /**
     * Скорректировать длину кусочков.
     * @param direction Направление коррекции.
     */
    // TODO вообще-то, нужно еще смотреть, что нам ничего не мешает, однако, если на что-то мешает, это странная ситуация
    // TODO так как коррекция должна вызываться сразу! после деформации.
    private void correct(Direction direction) {
        while (!checkLength(direction.toOrientation())) { // we need this, because we can make long parts while correcting the other ones
            for (BusPart part : orientationParts(orientationPredicate(direction.toOrientation()))) {
                double length = part.length();
                if (length > part.maxLength) {
                    part.stretchDirectly(direction.getOppositeDirection(), -(length - part.maxLength));
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
    private boolean isMax(BusPart part, Direction direction) {
        Preconditions.checkState(!parts.isEmpty(), "There are no parts in this bus");
        return part.equals(Collections.max(orientationParts(direction),
                axisComparator(direction.getEdgeComparator())));
    }

    public BusPart getMaxPart(Direction direction) {
        Preconditions.checkState(!parts.isEmpty(), "There are no parts in this bus");
        return Collections.max(orientationParts(direction),
                axisComparator(direction.getEdgeComparator()));
    }

    public BusPart getMinPart(Direction direction) {
        Preconditions.checkState(!parts.isEmpty(), "There are no parts in this bus");
        return Collections.max(orientationParts(direction),
                axisComparator(direction.getEdgeComparator()));
    }

    public Optional<BusPart> closest(final BusPart part) {
        final Edge axis = part.getAxis();
        List<BusPart> orientationParts = orientationParts(axis.getOrientation());
        orientationParts.remove(part);
        return axis.closestEdge(orientationParts, new Function<BusPart, Edge>() {
            @Override
            public Edge apply(BusPart input) {
                return input.getAxis();
            }
        });        
    }

    static Comparator<BusPart> axisComparator(final Comparator<Edge> cmp) {
        return new Comparator<BusPart>() {
            @Override
            public int compare(BusPart o1, BusPart o2) {
                if (o1 != null && o2 != null) {
                    return cmp.compare(o1.getAxis(), o2.getAxis());
                }

                return 0;
            }
        };
    }

    /**
     * Установка всем кусочкам шины индексов в соответсвии их следовании в массиве.
     */
    private void ensureIndices() {
        int index = 0;
        for (BusPart part : parts) {
            part.index = index;
            index++;
        }
    }

    @Override
    public Polygon getBounds() {
        return bounds;
    }

    private void rebuildPartBounds() {
        for (BusPart part : parts) {
            part.rebuildBounds();
        }
    }

    public void rebuildBounds() {
        if (parts.isEmpty()) {
            return;
        }

        rebuildPartBounds(); // TODO убрать

        Polygon newBounds = parts.get(0).getBounds();
        for (int i = 1; i < parts.size(); i++) {
            newBounds = addPartToBounds1(newBounds, parts.get(i));
        }

        bounds = newBounds;
        //return newBounds;
    }

    @Override
    public void draw(Graphics2D g) {
        if (getMaterial() == Material.METAL) {
            System.out.println("metal");
        }
        Graphics2D graphics = (Graphics2D) g.create();
        try {
            rebuildBounds();
        } catch (UnexpectedException e) {
            e.printStackTrace();
        }
       // graphics.setStroke(StrokeFactory.dashedStroke());
        int k = 0;
        for (BusPart part : parts) {
            //part.drawAxis(graphics);
            part.draw(graphics);
            Edge axis = part.getAxis();
//            Point center = part.getAxis().getCenter();
//            graphics.drawString(String.valueOf(k), center.intX(), center.intY());
            //GeomUtils.triangle(axis.getEnd(), 10, axis.getDirection()).draw(graphics);
            k++;
        }

       /* if (getMaterial() == Material.METAL) {
            bounds.drawPoints(graphics);
        }*/
        //bounds.draw(graphics);

        graphics.dispose();
    }

    public void drawAxis(Graphics2D g) {
        Graphics2D graphics = (Graphics2D) g.create();
        for (BusPart part : parts) {
            part.drawAxis(graphics);
        }

        graphics.dispose();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        /*@Override
        protected Object clone() {
            try {
                BusPart clone = (BusPart) super.clone();
                clone.setAxis((Edge) axis.clone());
                clone.setMaxLength(maxLength);
                clone.setStretchable(stretchable);
                clone.setDeformable(deformable);
                clone.setMovable(movable);
                clone.index = index;
                clone.rebuildBounds();

                return clone;
            } catch (CloneNotSupportedException e) {
                throw new InternalError();
            }
        }*/

        return null;
    }

    /**
     * Часть шины. Объект, который представляет собой
     * осевую линию и прямоугольную границу определяемую
     * некоторым расстоянием от оси.
     *
     * @version 1.0
     * @author astamir
     */
    // TODO deformable
    @XmlRootElement
    public class BusPart extends LegacyTopologyElement implements Movable, Cloneable, Serializable {

        /**
         * Осевая линия в реальных координатах.
         */
        private Edge axis;

        /**
         * Индекс этого кусочка в шине.
         */
        private int index = -1;

        /**
         * Максимальная протяженность шины.
         */
        private double maxLength;

        /**
         * Говорит о том, может ли данный фрагмент шины растягиваться.
         */
        private boolean stretchable;

        /**
         * Говорит о том, может ли данный кусочек шины менять свое местоположение в реальных координатах.
         */
        private boolean movable = true;

        /**
         * Говорит о том, можно ли данный кусок шины деформировать.
         */
        private boolean deformable = true;


        /**
         * Реальные размеры куска шины.
         */
        private Rectangle bounds;

        private BusPart(Point start, Point end, double maxLength,
                       boolean stretchable, boolean movable) {
            super(Bus.this.getLayer());
            this.axis = Edge.of(start, end);
            this.maxLength = maxLength;
            this.stretchable = stretchable;
            this.movable = movable;
            this.bounds = Rectangle.of(axis, width, widthAtBorder);
        }

        private BusPart(Edge axis, double maxLength,
                       boolean stretchable, boolean movable) {
            super(Bus.this.getLayer());
            this.axis = axis;
            this.maxLength = maxLength;
            this.stretchable = stretchable;
            this.movable = movable;
            this.bounds = Rectangle.of(axis, width, widthAtBorder);
        }

        private BusPart(Point start, Direction direction, double length, double maxLength,
                       boolean stretchable, boolean movable) {
            super(Bus.this.getLayer());
            this.axis = Edge.of(start, direction, length);
            this.maxLength = maxLength;
            this.stretchable = stretchable;
            this.movable = movable;
            this.bounds = Rectangle.of(axis, width, widthAtBorder);
        }


        private BusPart(Point start, Point end) {
            this(start, end, Double.MAX_VALUE, true, true);
        }

        private BusPart(Point start, Direction dir, double length) {
            this(Edge.of(start, dir, length));
        }

        public BusPart(Edge axis) {
            this(axis, Double.MAX_VALUE, true, true);
        }

        private BusPart(BusPart part, Edge axis) {
            super(part.getLayer());
            this.axis = axis;
            this.maxLength = part.maxLength;
            this.stretchable = part.stretchable;
            this.movable = part.movable;
            this.bounds = Rectangle.of(axis, width, widthAtBorder);
        }
        
        public Bus getParent() {
            return Bus.this;
        }

        @Override
        @XmlElement
        public Material getMaterial() {
            return Bus.this.getMaterial();
        }

        @Override
        public void setMaterial(Material material) {
            // we don't need to set anything cause we call parent getter anyways.
        }

        @Override
        @XmlTransient
        public ConductionType getConductionType() {
            return Bus.this.getConductionType();
        }

        @Override
        public void setConductionType(ConductionType conductionType) {
            // we don't need to set anything cause we call parent getter anyways.
        }

        @Override
        @XmlTransient
        public TopologyLayer getLayer() {
            return Bus.this.getLayer();
        }

        @Override
        public void setLayer(TopologyLayer layer) {
            // we don't need to set anything cause we call parent getter anyways.
        }

        @Override
        @XmlTransient
        public Color getColor() {
            return Bus.this.getColor();
        }

        @Override
        public void setColor(Color color) {
            // we don't need to set anything cause we call parent getter anyways.
        }

        @Override
        @XmlTransient
        public Stroke getStroke() {
            return Bus.this.getStroke();
        }

        @Override
        public void setStroke(Stroke stroke) {
            // we don't need to set anything cause we call parent getter anyways.
        }

        @Override
        @XmlTransient
        public Stroke getSketchStroke() {
            return Bus.this.getSketchStroke();
        }

        @Override
        public void setSketchStroke(Stroke sketchStroke) {
            // we don't need to set anything cause we call parent getter anyways.
        }

        @XmlElement
        public List<Contact> getContacts() {
            return Bus.this.getContacts();
        }

        @Override
        @XmlElement
        public Class<? extends LegacyTopologyElement> getActualClass() {
            Class<? extends LegacyTopologyElement> actualClass = super.getActualClass();
            return actualClass.equals(getClass()) ? getParentClass() : actualClass;
        }

        @XmlElement
        public boolean isMovable() {
            return movable;
        }

        public void setMovable(boolean movable) {
            this.movable = movable;
        }

        @XmlElement
        public Edge getAxis() {
           return axis;
        }

        @XmlTransient
        public BusPart getElement() {
            return this;
        }

        public void setAxis(Edge axis) {
            this.axis = axis;
            rebuildPartBounds();
        }

        @XmlElement
        public double getWidth() {
            return width;
        }

        @XmlElement
        public double getMaxLength() {
            return maxLength;
        }

        public void setMaxLength(double maxLength) {
            this.maxLength = maxLength;
        }

        @XmlElement
        public boolean isStretchable() {
            return stretchable;
        }

        public void setStretchable(boolean stretchable) {
            this.stretchable = stretchable;
        }

        @XmlElement
        public boolean isAxisVisible() {
            return axisVisible;
        }

        @XmlElement
        public int index() {
            ensureIndices();
            return index;
            //return Bus.this.parts.indexOf(this);
        }

		public void setIndex(int index) {
			this.index = index;
		}

        @XmlElement
        public boolean isDeformable() {
            return deformable;
        }

        public void setDeformable(boolean deformable) {
            this.deformable = deformable;
        }

        @XmlElement
        public double getWidthAtBorder() {
            return widthAtBorder;
        }

        public Orientation getOrientation() {
            return axis.getOrientation();
        }

        public Class<? extends LegacyTopologyElement> getParentClass() {
            return Bus.this.getClass();
        }

        public double length() {
            return axis.length();
        }

        public double boundsLength() {
            return axis.isHorizontal() ? bounds.getWidth() : bounds.getHeight();
        }

        public boolean isLast() {
            return index == size() - 1;
        }

        public int busSize() {
            return size();
        }

        /**
         * Перемещение куска шины независимо от шины. Использовать аккуратно.
         *
         * @param dx
         * @param dy
         */
        private void moveDirectly(double dx, double dy) {
            axis.move(dx, dy);
            bounds.move(dx, dy);
        }

        /**
         * @see #moveDirectly(double, double)
         * @param direction
         * @param d
         */
        private void moveDirectly(Direction direction, double d) {
            double signedD = d * direction.getDirectionSign();
            if (direction.isLeftOrRight()) {
                moveDirectly(signedD, 0);
            } else {
                moveDirectly(0, signedD);
            }
        }

        @Override
        @Deprecated
        public boolean move(double dx, double dy) {
            Preconditions.checkArgument(dx == 0 || dy == 0, "One of the parameters should be zero"); // TODO потом убрать, когда появятся мементы.
            // here we have to call Bus's method to properly move our part.
            return move(Direction.of(Orientation.HORIZONTAL, dx), dx) && move(Direction.of(Orientation.VERTICAL, dy), dy);
        }

        public boolean move(Direction direction, double d) {
            return Bus.this.directlyMovePart(this, direction, d);
        }

        /**
         * Растягивание шины напрямую (без проверки на максимальную длину куска).
         *
         * @param dir
         * @param length
         * @return
         */
        public boolean stretchDirectly(Direction dir, double length) {
            return stretchable && stretch(this, dir, length);
        }

        @Override
        @XmlElement
        public Rectangle getBounds() {
            return bounds;
        }

        @Override
        public void setBounds(Polygon bounds) {
            Preconditions.checkArgument(bounds instanceof Rectangle, "bounds for bus part should be a rectangle.");
            this.bounds = (Rectangle) bounds;
        }

        Polygon buildBounds() {
            return Rectangle.of(axis, width, widthAtBorder);
        }

        void rebuildBounds() {
            setBounds(buildBounds());
        }

        /**
         * Нарисовать этот кусочек. Вообще говоря, этот метод не стоит вызывать напрямую.
         * Просто реализуем интерфейс.
         * @param g
         */
        @Override
        public void draw(Graphics2D g) {
//            for (Contact contact : getContacts()) {
//                contact.setColor(getColor());
//            }
            Graphics2D graphics = (Graphics2D) g.create();
            graphics.setColor(getColor());
            // draw the axis
            if (axisVisible) {
                drawAxis(graphics);
             //   graphics.drawString(index + "", axis.getStart().intX(), axis.getStart().intY());
//                if (isLast()) {
//                    graphics.drawString(index+ 1 + "", axis.getEnd().intX(), axis.getEnd().intY());
//                }
               // graphics.drawString(String.valueOf(index), axis.getCenter().intX(), axis.getCenter().intY());
            }

            // draw bounds
            graphics.setStroke(getStroke());
            bounds.draw(graphics);

            graphics.dispose();
        }

        /**
         * Нарисовать только осевую линию.
         *
         * @param g
         */
        public void drawAxis(Graphics2D g) {
            Graphics2D graphics = (Graphics2D) g.create();
            graphics.setStroke(getSketchStroke());
            graphics.setColor(getColor());
            axis.draw(graphics);
            graphics.dispose();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BusPart busPart = (BusPart) o;

            if (deformable != busPart.deformable) return false;
            if (index != busPart.index) return false;
            if (Double.compare(busPart.maxLength, maxLength) != 0) return false;
            if (movable != busPart.movable) return false;
            if (stretchable != busPart.stretchable) return false;
            if (axis != null ? !axis.equals(busPart.axis) : busPart.axis != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            result = axis != null ? axis.hashCode() : 0;
            result = 31 * result + index;
            temp = maxLength != +0.0d ? Double.doubleToLongBits(maxLength) : 0L;
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            result = 31 * result + (stretchable ? 1 : 0);
            result = 31 * result + (movable ? 1 : 0);
            result = 31 * result + (deformable ? 1 : 0);
            return result;
        }

        @Override
        public String toString() {
            return "BusPart{axis=" + axis +
                    "} " + axis.getOrientation() + ", index = " + index + ", movable = " + movable + ", stretch = " + stretchable;
        }

        @Override
        protected Object clone() {
            try {
                BusPart clone = (BusPart) super.clone();
                clone.setAxis((Edge) axis.clone());
                clone.setMaxLength(maxLength);
                clone.setStretchable(stretchable);
                clone.setDeformable(deformable);
                clone.setMovable(movable);
                clone.index = index;
                clone.rebuildBounds();

                return clone;
            } catch (CloneNotSupportedException e) {
                throw new InternalError();
            }
        }
    }

    public static void main(String[] args) {
        /*double length = -10.0;
        final double minLength = 2.0;
        final double maxLength = 12.0;

        double width = 5;
        double correctedWidth = width;
        System.out.println("width = " + width);

        if (Math.abs(width) > Math.abs(length) && Doubles.compare(Math.signum(width), Math.signum(length)) != 0) {
            System.out.println("Частный случай");
        }


        double newLength = length + width;
        System.out.println("new l = " + newLength);
        if (Math.abs(newLength) < minLength) {
            System.out.println("newLength < minLength");
            correctedWidth = (Math.abs(length) - minLength) * Math.signum(width);
        } else if (Math.abs(newLength) > maxLength) {
            correctedWidth = (maxLength - Math.abs(length)) * Math.signum(width);
        }

        System.out.println("correctedWidth = " + correctedWidth);
        System.out.println("newLength = " + (length + correctedWidth));*/

        Bus bus = new Bus(null, null, null, 10);
        bus.setFirstPart(Edge.of(0, 0, 100, 0), 100, true);
        bus.createAnEmptyLink(Point.of(20, 20), Direction.DOWN);
        bus.createAnEmptyLink(Point.of(20, 20), Direction.DOWN);
        bus.createAnEmptyLink(Point.of(20, 20), Direction.DOWN);
        bus.createAnEmptyLink(Point.of(20, 20), Direction.DOWN);
        bus.createAnEmptyLink(Point.of(20, 20), Direction.DOWN);

        bus.removeIdenticalParts();

        System.out.println(bus.parts);
    }
    
}

