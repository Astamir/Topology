package ru.etu.astamir.compression;

import com.google.common.base.*;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import ru.etu.astamir.common.Pair;
import ru.etu.astamir.common.Utils;
import ru.etu.astamir.common.collections.CollectionUtils;
import ru.etu.astamir.common.collections.UniqueIterator;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.*;
import ru.etu.astamir.model.TopologicalCell;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.TopologyLayer;
import ru.etu.astamir.model.exceptions.UnexpectedException;
import ru.etu.astamir.model.technology.Technology;
import ru.etu.astamir.model.wires.SimpleWire;
import ru.etu.astamir.model.wires.Wire;

import java.util.*;

/**
 * Частокол. Набор отрезков с разными классами элементов.
 * Возможно в будущем придется пихать сюда сами элементы,
 * в случае с диагональными расстояниями от контактов ?
 */
// TODO FIXME !!!!
public class Border {

    TopologyLayer layer;

    /**
     * Ориентация частокола. Частокол может быть вертикальным или горизонтальным.
     * НО добавлять можно элементы разной ориентации (будем толерантны).
     */
    private final Orientation orientation;

    /**
     * Элементы частокола.
     */
    private List<BorderPart> parts = Lists.newArrayList();

    private Technology.TechnologicalCharacteristics technology;


    public Border(Orientation orientation, Technology.TechnologicalCharacteristics technology) {
        this.orientation = Preconditions.checkNotNull(orientation);
        this.technology = Preconditions.checkNotNull(technology);
    }

    public Border(Orientation orientation, Technology.TechnologicalCharacteristics technology, Collection<BorderPart> parts) {
        this.orientation = Preconditions.checkNotNull(orientation);
        this.technology = Preconditions.checkNotNull(technology);
        setParts(parts);
    }

    public static Border combine(Border... borders) {
        Orientation orientation = Orientation.BOTH;
        Technology.TechnologicalCharacteristics technology = null;
        List<BorderPart> parts = new ArrayList<>();
        for (Border border : borders) {
            if (technology == null && border.technology != null) {
                technology = border.technology;
            }

            if (orientation == Orientation.BOTH && border.orientation != Orientation.BOTH) {
                orientation = border.orientation;
            }

            parts.addAll(border.parts);
        }

        return new Border(orientation, technology, parts);
    }
    
    public static Border of(Orientation orientation, Technology.TechnologicalCharacteristics technology, List<Edge> edges, String symbol) {
        Border border = new Border(orientation, technology);
        List<BorderPart> parts = Lists.newArrayList();
        for (Edge edge : edges) {
            parts.add(new BorderPart(edge, null, symbol));
        }

        border.parts = parts;
        return border;
    }

    public static Border of(Orientation orientation, Technology.TechnologicalCharacteristics technology, TopologyElement element) {
        return new Border(orientation, technology, BorderPart.of(element));
    }

    public static Border emptyBorder(Orientation orientation, Technology.TechnologicalCharacteristics technology) {
        return new Border(orientation, technology);
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public List<BorderPart> getParts() {
        return parts;
    }

    public void addParts(Collection<BorderPart> parts) {
        this.parts.addAll(parts);
    }

    public List<Edge> getEdges() {
        return Lists.transform(parts, new Function<BorderPart, Edge>() {
            @Override
            public Edge apply(BorderPart input) {
                return input.getAxis();
            }
        });
    }

    public Technology.TechnologicalCharacteristics getTechnology() {
        return technology;
    }

    public void setTechnology(Technology.TechnologicalCharacteristics technology) {
        this.technology = Preconditions.checkNotNull(technology);
    }

    public TopologyLayer getLayer() {
        return layer;
    }

    public void setLayer(TopologyLayer layer) {
        this.layer = layer;
    }

    public void addPart(Edge axis, String symbol) {
        parts.add(new BorderPart(axis, symbol));
    }

    public void setParts(Collection<BorderPart> parts) {
        this.parts.clear();
        this.parts.addAll(parts);
    }

    public Predicate<BorderPart> orientationPredicate() {
        return new Predicate<BorderPart>() {
            @Override
            public boolean apply(BorderPart input) {
                return input.getAxis().getOrientation() == orientation;
            }
        };
    }

    public double getMinDistance(BorderPart part, String symbol) {
        Preconditions.checkNotNull(part.getSymbol(), "part " + part + " has null symbol");
        if (part.getSymbol().equals(TopologicalCell.DEFAULT_CELL_SYMBOL)) {
            return technology.getMinDistance(symbol, symbol);
        }
        if (symbol.equals(TopologicalCell.DEFAULT_CELL_SYMBOL)) {
            return technology.getMinDistance(part.getSymbol(), part.getSymbol());
        }
        return technology.getMinDistance(symbol, part.getSymbol());
    }

    public double getMoveDistance(BorderPart part, String symbol, Direction dir, Point point) {
        //Preconditions.checkArgument(axis.getDirection().isOrthogonal(dir));
        double min = getMinDistance(part, symbol);
        return Utils.round(Math.abs(part.getAxis().distanceToPoint(point) - min));
    }

    public double getMoveDistance(SimpleWire wire, Direction dir) {
        Preconditions.checkNotNull(wire);
        return getMoveDistance(wire.getAxis(), wire.getSymbol() != null ? wire.getSymbol() : wire.getWire().getSymbol(), dir);
    }

    public double getMoveDistance(Edge axis, String symbol, Direction dir) {
        double d = 0.0;
        Optional<BorderPart> closest_part = getClosestPartWithConstraints(axis, symbol, dir);
        if (closest_part.isPresent()) {
            double start = getMoveDistance(closest_part.get(), symbol, dir, axis.getStart());
            double end = getMoveDistance(closest_part.get(), symbol, dir, axis.getEnd());

            return Math.min(start, end);
        }
        return d;
    }

    // todo connecting part type
    public static List<BorderPart> singleOverlay(BorderPart was, BorderPart added, final Orientation orientation, Direction dir) {
        List<BorderPart> result = Lists.newArrayList();

        // preparing our parts
        was.correct();
        added.correct();
        
        Edge addedAxis = added.getAxis();
        Edge wasAxis = was.getAxis();
        
        Edge ourTopRay = Edge.ray(wasAxis.getEnd(), dir);
        Edge ourBotRay = Edge.ray(wasAxis.getStart(), dir);

        boolean topCross = addedAxis.cross(ourTopRay) == Edge.EdgeRelation.SKEW_CROSS;
        if (topCross) {
            result.add(new BorderPart(Edge.of(addedAxis.crossing(ourTopRay), addedAxis.getEnd()), added.getElement(), added.getSymbol()));
        }

        boolean botCross = addedAxis.cross(ourBotRay) == Edge.EdgeRelation.SKEW_CROSS;
        if (botCross) {
            result.add(new BorderPart(Edge.of(addedAxis.getStart(), addedAxis.crossing(ourBotRay)), added.getElement(), added.getSymbol()));
        }        
        
        if (!topCross && !botCross) {
            Edge reverseRay = Edge.ray(addedAxis.getStart(), dir.getOppositeDirection());
            if (reverseRay.cross(wasAxis) == Edge.EdgeRelation.SKEW_CROSS) {
                return Lists.newArrayList();
            } else {
                return Lists.newArrayList(added);
            }
        }

        return result;
    }

    /**
     *
     * @param newParts
     * @param dir
     */
    public void overlay(List<BorderPart> newParts, Direction dir) {
        List<BorderPart> result = Lists.newArrayList();

        final Predicate<BorderPart> orientationPredicate = orientationPredicate();
        List<BorderPart> allParts = Lists.newArrayList(Iterables.filter(Iterables.concat(newParts, parts),
                orientationPredicate)); // we need only parts of border's orientation to work with.

        List<List<BorderPart>> columns = CollectionUtils.divideEdgedElements(allParts, Utils.Functions.BORDER_PART_AXIS_FUNCTION, dir); // lets divide our parts into sorted columns.
        int columnSize = columns.size();
        for (int i = 0; i < columnSize; i++) {
            // find i-st column
            List<BorderPart> column = columns.get(i);
            result.addAll(column); // this column is ok, we can add it.

            // now we have to cut added parts out of all remaining ones.
            for (BorderPart part : column) {
                for (int j = i + 1; j < columnSize; j++) {
                    for (ListIterator<BorderPart> k = columns.get(j).listIterator(); k.hasNext();) {
                        BorderPart burningPart = k.next();
                        k.remove();
                        List<BorderPart> overlay = singleOverlay(part, burningPart, orientation, dir);
                        for (BorderPart o : overlay) {
                            k.add(o);
                        }                        
                    }
                }
            }
        }

        parts = correctParts(roundParts(result)); // now we got to correct all new parts. although it seems to me that they are already good.
        connectParts(dir); // and we connect all the parts, since we were working only with orientational ones.
    }

    public Border without(Collection<String> symbols) {
        Border copy = new Border(orientation, technology, parts);
        for (Iterator<BorderPart> i = copy.parts.iterator(); i.hasNext();) {
            BorderPart next = i.next();
            if (symbols.contains(next.getSymbol()) || (next.getElement() != null && symbols.contains(next.getElement().getSymbol()))) {
                i.remove();
            }
        }

        return copy;
    }


    private static List<BorderPart> roundParts(List<BorderPart> parts) {
        for (BorderPart part : parts) {
            part.getAxis().getStart().round();
            part.getAxis().getEnd().round();
        }

        return parts;
    }

    /**
     * Получить огибающую линию
     * @param direction
     * @return
     */
    public Border getOverlay(Direction direction) {
        Border border = new Border(orientation, technology);
        border.setParts(parts);
        border.overlay(Collections.<BorderPart>emptyList(), direction);
        return border;
    }


    /**
     * Соединяет элементы частокола. Предполагается, что
     * все элементы скорректированы.
     *
     * (Пока останется так, но можно запихивать соединительные
     * части в конец списка, тем самым не возвращаясь на шаг назад.)
     */
    // TODO классы соединительных частей
    private void connectParts1(Direction direction) {
        Collections.sort(parts, BorderPart.getAxisComparator(orientation == Orientation.VERTICAL ?
                Direction.UP.getEdgeComparator(false) : Direction.RIGHT.getEdgeComparator(false)));
        Class<? extends TopologyElement> lastClass;
        String lastSymbol = null;
        for (ListIterator<BorderPart> i = parts.listIterator(); i.hasNext();) {
            BorderPart cur = i.next();
            TopologyElement element = cur.getElement();
            lastClass = element != null ? element.getClass() : null;
            if (lastClass == null) {
                lastClass = ProjectObjectManager.getElementFactory().getEntityClass(lastSymbol);
            }
            if (i.hasNext()) {
                BorderPart next = i.next();
                i.previous();
                i.add(new BorderPart(Edge.of(cur.getAxis().getEnd(), next.getAxis().getStart()), null, lastSymbol));
            }
        }
    }

    private void connectParts(final Direction direction) {
        Collections.sort(parts, BorderPart.getAxisComparator(direction.getEdgeComparator(false)));
        List<BorderPart> list = Lists.newArrayList(parts);

        for (int i = 0; i < list.size(); i++) {
            BorderPart part = list.get(i);

            boolean startConnected = false;
            boolean endConnected = false;
            for (int j = i + 1; j < list.size(); j++) {
                BorderPart next = list.get(j);
                Edge.EdgeRelation startRelation = Edge.ray(part.getAxis().getStart(), direction).cross(next.getAxis());
                if (startRelation == Edge.EdgeRelation.SKEW_CROSS && !startConnected) {
                    Point startPoint = Edge.ray(part.getAxis().getStart(), direction).crossing(next.getAxis());
                    parts.add(parts.indexOf(next) + 1, new BorderPart(Edge.of(startPoint, part.getAxis().getStart()), part.getElement(), part.getSymbol()));
                    startConnected = true;
                }

                Edge.EdgeRelation endRelation = Edge.ray(part.getAxis().getEnd(), direction).cross(next.getAxis());
                if (endRelation == Edge.EdgeRelation.SKEW_CROSS && !endConnected) {
                    Point endPoint = Edge.ray(part.getAxis().getEnd(), direction).crossing(next.getAxis());
                    parts.add(parts.indexOf(next) + 1, new BorderPart(Edge.of(endPoint, part.getAxis().getEnd()), part.getElement(), part.getSymbol()));
                    endConnected = true;
                }

                if (startConnected && endConnected) {
                    break;
                }
            }
        }
    }

    /**
     * Удаляет все вырожденные отрезки и переворачивает те, которые
     * направлены вниз или влево.
     *
     * @param parts
     * @return
     */
    private List<BorderPart> correctParts(List<BorderPart> parts) {
        for (BorderPart part : parts) {
            Edge axis = part.getAxis();
            if (!axis.getDirection().isUpOrRight()) {
                axis.reverse();
            }
        } // reversing all down or left oriented parts

        return Lists.newArrayList(UniqueIterator.create(Iterators.filter(UniqueIterator.create(parts.iterator()), new Predicate<BorderPart>() {
            @Override
            public boolean apply(BorderPart input) {
                return !input.getAxis().isPoint();
            }
        })));
    }

    /**
     * Получает соритрованые пачки кусков частокола.
     * 
     * @param unsortedParts 
     * @param dir
     * @return
     */
    private List<List<BorderPart>> divideParts(List<BorderPart> unsortedParts, Direction dir) {
        List<List<BorderPart>> result = Lists.newArrayList();        
        Collections.sort(unsortedParts, BorderPart.getAxisComparator(dir.getEdgeComparator()));
        int size = unsortedParts.size();
        for (int i = 0; i < size; i++) {
            List<BorderPart> parts = Lists.newArrayList();
            BorderPart curPart = unsortedParts.get(i);
            parts.add(curPart);
            for (int j = i + 1; j < size; j++) {
                BorderPart nextPart = unsortedParts.get(j);
                if (BorderPart.getAxisComparator(dir.getEdgeComparator()).compare(curPart, nextPart) == 0) {
                    parts.add(nextPart);
                } else {
                    break;
                }
            }
            
            result.add(parts);
        }
        
        return result;        
    }

    public Optional<BorderPart> getClosestPartWithConstraints(Point point, String symbol, Direction direction) {
        Edge ray = Edge.ray(point, direction);
        List<Pair<BorderPart, Double>> distances = Lists.newArrayList();
        double sign = direction.getDirectionSign();

        for (BorderPart part : orientationParts()) {
            double min = sign * getMinDistance(part, symbol);
            Edge axis = part.getAxis();
            if (axis.cross(ray) == Edge.EdgeRelation.SKEW_CROSS) {
                distances.add(Pair.of(part, axis.distanceToPoint(point) + min));
            }
        }

        if (distances.isEmpty()) {
            return Optional.absent();
        }
        
        return Optional.of(Collections.min(distances, new Comparator<Pair<BorderPart, Double>>() {
            @Override
            public int compare(Pair<BorderPart, Double> o1, Pair<BorderPart, Double> o2) {
                return o1.right.compareTo(o2.right);
            }
        }).left);
    }

    public Optional<BorderPart> getClosestPartWithConstraints(Edge edge, String symbol, Direction direction) {
        Edge topRay = Edge.ray(edge.getStart(), direction);
        Edge botRay = Edge.ray(edge.getEnd(), direction);
        double sign = direction.getDirectionSign();

        List<Pair<BorderPart, Double>> distances = Lists.newArrayList();
        for (BorderPart part : orientationParts(direction)) {
            double min = sign * getMinDistance(part, symbol);
            Edge axis = part.getAxis();
            if (axis.cross(topRay) == Edge.EdgeRelation.SKEW_CROSS) {
                distances.add(Pair.of(part, Math.abs(axis.distanceToPoint(edge.getStart()) + min)));
            }

            if (axis.cross(botRay) == Edge.EdgeRelation.SKEW_CROSS) {
                distances.add(Pair.of(part, Math.abs(axis.distanceToPoint(edge.getEnd()) + min)));
            }

            Edge bRay = Edge.ray(axis.getStart(), direction.getOppositeDirection());
            Edge tRay = Edge.ray(axis.getEnd(), direction.getOppositeDirection());

            if (bRay.cross(edge) == Edge.EdgeRelation.SKEW_CROSS) {
                distances.add(Pair.of(part, Math.abs(edge.distanceToPoint(axis.getStart()) + min)));
            }

            if (tRay.cross(edge) == Edge.EdgeRelation.SKEW_CROSS) {
                distances.add(Pair.of(part, Math.abs(edge.distanceToPoint(axis.getEnd()) + min)));
            }
        }

        if (distances.isEmpty()) {
            return Optional.absent();
        }

        return Optional.of(Collections.min(distances, new Comparator<Pair<BorderPart, Double>>() {
            @Override
            public int compare(Pair<BorderPart, Double> o1, Pair<BorderPart, Double> o2) {
                return o1.right.compareTo(o2.right);
            }
        }).left);
    }

    public Map<BorderPart, Double> getClosestParts(Edge edge, String symbol, Direction direction) {
        Edge topRay = Edge.ray(edge.getStart(), direction);
        Edge botRay = Edge.ray(edge.getEnd(), direction);
        double sign = direction.getDirectionSign();

        Map<BorderPart, Double> distances = new HashMap<>();
        for (BorderPart part : orientationParts(direction)) {
            double min = sign * getMinDistance(part, symbol);
            Edge axis = part.getAxis();
            if (axis.cross(topRay) == Edge.EdgeRelation.SKEW_CROSS) {
                distances.put(part, Math.abs(axis.distanceToPoint(edge.getStart()) + min));
            }

            if (axis.cross(botRay) == Edge.EdgeRelation.SKEW_CROSS) {
                distances.put(part, Math.abs(axis.distanceToPoint(edge.getEnd()) + min));
            }

            Edge bRay = Edge.ray(axis.getStart(), direction.getOppositeDirection());
            Edge tRay = Edge.ray(axis.getEnd(), direction.getOppositeDirection());

            if (bRay.cross(edge) == Edge.EdgeRelation.SKEW_CROSS) {
                distances.put(part, Math.abs(edge.distanceToPoint(axis.getStart()) + min));
            }

            if (tRay.cross(edge) == Edge.EdgeRelation.SKEW_CROSS) {
                distances.put(part, Math.abs(edge.distanceToPoint(axis.getEnd()) + min));
            }
        }

        return distances;
    }

    public Optional<BorderPart> getClosestPartWithoutConstraints(Edge edge, Direction direction) {
        Edge topRay = Edge.ray(edge.getStart(), direction);
        Edge botRay = Edge.ray(edge.getEnd(), direction);

        List<Pair<BorderPart, Double>> distances = Lists.newArrayList();
        for (BorderPart part : orientationParts()) {
            Edge axis = part.getAxis();
            if (axis.cross(topRay) == Edge.EdgeRelation.SKEW_CROSS) {
                distances.add(Pair.of(part, axis.distanceToPoint(edge.getStart())));
            }

            if (axis.cross(botRay) == Edge.EdgeRelation.SKEW_CROSS) {
                distances.add(Pair.of(part, axis.distanceToPoint(edge.getEnd())));
            }

            Edge bRay = Edge.ray(axis.getStart(), direction.getOppositeDirection());
            Edge tRay = Edge.ray(axis.getEnd(), direction.getOppositeDirection());

            if (bRay.cross(edge) == Edge.EdgeRelation.SKEW_CROSS) {
                distances.add(Pair.of(part, edge.distanceToPoint(axis.getStart())));
            }

            if (tRay.cross(edge) == Edge.EdgeRelation.SKEW_CROSS) {
                distances.add(Pair.of(part, edge.distanceToPoint(axis.getEnd())));
            }
        }

        if (distances.isEmpty()) {
            return Optional.absent();
        }

        return Optional.of(Collections.min(distances, new Comparator<Pair<BorderPart, Double>>() {
            @Override
            public int compare(Pair<BorderPart, Double> o1, Pair<BorderPart, Double> o2) {
                return o1.right.compareTo(o2.right);
            }
        }).left);
    }

    public Optional<BorderPart> getMaxPart(Direction direction) {
        List<BorderPart> orientationParts = orientationParts();
        if (orientationParts.isEmpty()) {
            return Optional.absent();
        }

        Comparator<BorderPart> cmp = BorderPart.getAxisComparator(direction.getEdgeComparator());

        return Optional.of(Collections.max(orientationParts, cmp));
    }
    
    public List<BorderPart> orientationParts() {
        return Lists.newArrayList(Iterables.filter(parts, new Predicate<BorderPart>() {
            @Override
            public boolean apply(BorderPart input) {
                return input.getAxis().getOrientation() == orientation;
            }
        }));
    }

    public List<BorderPart> orientationParts(final Direction direction) {
        return Lists.newArrayList(Iterables.filter(parts, new Predicate<BorderPart>() {
            @Override
            public boolean apply(BorderPart input) {
                return input.getAxis().getOrientation() == direction.getOrthogonalDirection().toOrientation();
            }
        }));
    }

    // TODO
    public static void imitate(Wire bus, Border border, Direction direction, boolean deformation_allowed) {
        //bus.correctBus();
        if (!bus.isChained()) {
            bus.ensureChained();
        }
        bus.round();
        if (deformation_allowed)
            border.createEmptyLinks(bus, direction);

        for (SimpleWire part : bus.getParts()) {
            if (!part.getAxis().getOrientation().isOrthogonal(direction.toOrientation()) || part.getAxis().isPoint()) {
                continue;
            }
            Optional<BorderPart> closestPart = border.getClosestPartWithConstraints(part.getAxis(), bus.getSymbol(), direction);
            if (closestPart.isPresent()) {
                double distToMove = border.getMoveDistance(closestPart.get(), bus.getSymbol(),
                        direction, part.getAxis().getStart());

                bus.movePart(part, direction, distToMove);
            }
        }

        bus.removeEmptyParts();
    }

    public void imitate(Wire bus, Direction direction) {
        imitate(bus, direction, true);
    }

    public void imitate(Wire bus, Direction direction, boolean deformation_allowed) {
        Border copy = CompressionUtils.borderWithoutConnectedElements(bus, this);
        imitate(bus, copy.getOverlay(direction), direction, deformation_allowed);
        if (!bus.isConnected()) {
            throw new UnexpectedException("not connected");
        }
    }

    // TODO
    private void createEmptyLinks(Wire bus, Direction direction) {
        List<BorderPart> nonOrientParts =
                Lists.newArrayList(Iterables.filter(parts, Predicates.not(orientationPredicate())));
        //boolean wasEmptyPartsOnEdges = bus.hasEmptyPartsOnEdges();
        for (BorderPart part : nonOrientParts) {
            double min = getMinDistance(part, bus.getSymbol());
            Point one = part.getAxis().getStart().clone();
            GeomUtils.move(one, direction.clockwise(), min);
            Point another = part.getAxis().getStart().clone();
            GeomUtils.move(another, direction.counterClockwise(), min);
            
            bus.createAnEmptyLink(one, direction.getOppositeDirection());
            bus.createAnEmptyLink(another, direction.getOppositeDirection());
        }


        //bus.removeEmptyPartsOnEdges(); // handle more carefully
        //bus.removeEmptyParts(!wasEmptyPartsOnEdges);
        // todo remove empty links on 5the edges ?
    }
}
