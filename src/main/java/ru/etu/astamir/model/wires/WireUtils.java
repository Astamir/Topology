package ru.etu.astamir.model.wires;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import ru.etu.astamir.common.Pair;
import ru.etu.astamir.common.Utils;
import ru.etu.astamir.common.collections.EntitySet;
import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.BorderPart;
import ru.etu.astamir.compression.commands.Command;
import ru.etu.astamir.compression.grid.Grid;
import ru.etu.astamir.geom.common.*;
import ru.etu.astamir.math.MathUtils;
import ru.etu.astamir.model.ComplexElement;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.connectors.ConnectionPoint;
import ru.etu.astamir.model.connectors.ConnectionUtils;
import ru.etu.astamir.model.connectors.SimpleConnectionPoint;
import ru.etu.astamir.model.contacts.Contact;
import ru.etu.astamir.model.contacts.ContactType;
import ru.etu.astamir.model.legacy.Edged;

import java.util.*;

/**
 * Утилитный класс для различных действий над шинами и контурами.
 *
 * @author Artem Mon'ko
 */
public class WireUtils {
    public static List<Edge> fromPoints(List<Point> points) {
        int length = points.size();
        if (length < 2) {
            return Collections.emptyList();
        }

        ImmutableList<Edge> edges = Polygon.of(points).edges();
        return Lists.newArrayList(edges.subList(0, edges.size() - 1));
    }

    public static Orientation getOrientation(Iterable<Edge> edges) {
        double vertical = 0.0;
        double horizontal = 0.0;
        for (Edge edge : edges) {
            double length = edge.length();
            if (edge.isHorizontal()) {
                horizontal += length;
            } else if (edge.isVertical()) {
                vertical += length;
            } else {
                return Orientation.BOTH;
            }
        }

        return vertical > horizontal ? Orientation.VERTICAL : Orientation.HORIZONTAL;
    }

    public static Point getCommonPoint(SimpleWire one, SimpleWire another) {
        return one.axis.findCommonPoint(another.axis);
    }

    public static Collection<SimpleWire> connect(Wire wire, Contact contact) {
        // first lets check if they are connected formally
        if (!contact.getConnectedNames().contains(wire.getName())) {
            contact.addConnectedElement(wire.getName());
        }
        if (!isConnected(wire, contact)) {
            wire.addConnection(contact);
        }

        if (contact.getType() == ContactType.USUAL || contact.getType() == ContactType.FLAP) {
            List<SimpleWire> connecting_wires = Lists.newArrayList();
            Point working_point = contact.getCenter().getStart();
            final Optional<SimpleWire> part_with_point = wire.findPartWithPoint(working_point);
            if (part_with_point.isPresent()) {
                if (!part_with_point.get().isLink()) {
                    connecting_wires.add(wire.addEmptyLinkToPart(part_with_point.get(), working_point));
                }
            } else {
                // we have our contact far away
                Optional<Point> closest_point = GeomUtils.getClosestPoint(wire.getCoordinates(), working_point);
                if (closest_point.isPresent()) {
                    if (GeomUtils.isOnOneLine(closest_point.get(), working_point)) { // we have to connect with one part
                        Optional<SimpleWire> part = wire.findPartWithPoint(closest_point.get());
                        if (part.isPresent()) {
                            SimpleWire.Builder builder = new SimpleWire.Builder(wire);
                            builder.setDeformable(true);
                            builder.setStretchable(true);
                            builder.setMovable(true);

                            SimpleWire connection;
                            if (wire.isFirstPart(part.get())) {
                                builder.setAxis(Edge.of(working_point.clone(), closest_point.get().clone()));
                                connection = builder.build();
                                wire.getParts().add(0, connection);

                            } else {
                                builder.setAxis(Edge.of(closest_point.get().clone(), working_point.clone()));
                                connection = builder.build();
                                wire.getParts().add(connection);
                            }

                            connecting_wires.add(connection);
                        }
                    }
                    // todo
                }
            }
            return connecting_wires; // connected
        } else {
            // some logic for complex contacts
        }

        return Collections.emptyList();
    }

    public static Optional<Point> getConnectionPoint(Wire one, Wire another) {
        for (SimpleWire ones_part : one.getParts()) {
            for (SimpleWire anothers_part : another.getParts()) {
                final Point common_point = ones_part.getAxis().crossing(anothers_part.getAxis());
                if (common_point != null) {
                    return Optional.of(common_point);
                }
            }
        }

        return Optional.absent();
    }

    public static boolean isConnected(Wire wire, TopologyElement element) {
        if (element == null) {
            return false;
        }
        Collection<String> element_names = element instanceof ComplexElement ? Collections2.transform(Lists.<TopologyElement>newArrayList(Iterables.concat(Collections.singletonList(element), ((ComplexElement) element).getElements())), Utils.Functions.NAME_FUNCTION) : Collections.singletonList(element.getName());
        Collection<String> connected_names = ConnectionUtils.getConnectedNames(wire.getConnections());
        if (element_names.isEmpty()) {
            return false;
        }
        for (String name : connected_names) {
            if (element_names.contains(name)) {
                return true;
            }
        }

        if (element instanceof SimpleWire) {
            return wire.indexOf((SimpleWire) element) >= 0;
        }

        return false;
    }



    public static boolean straighten(Wire wire, Border border, Direction direction, Grid grid) {
        boolean hadBeenChanged;
        do {
            hadBeenChanged = false;
            for (SimpleWire part : wire.orientationParts(direction)) {
                hadBeenChanged |= straighten(wire, part, border, direction, getPreferablePointForStraighten(wire, direction, grid));
            }

            wire.removeEmptyParts();
        } while (hadBeenChanged);
        return false;
    }

    private static Optional<Double> getPreferablePointForStraighten(Wire wire, Direction direction, Grid grid) {
        List<TopologyElement> connected_elements = new ArrayList<>();
        for (ConnectionPoint connection_point : wire.getConnections()) {
            connected_elements.addAll(ConnectionUtils.resolveConnectedElements(connection_point, grid));
        }

        Set<Double> points = new HashSet<>();
        for (TopologyElement connected_element : connected_elements) {
            if (connected_element instanceof Wire) {
                final Optional<Point> connection_point = getConnectionPoint(wire, (Wire) connected_element);
                if (connection_point.isPresent()) {
                    points.add(direction.isLeftOrRight() ? connection_point.get().x() : connection_point.get().y());
                }
            } else if (connected_element instanceof Contact) {
                final Point connection_point = ((Contact) connected_element).getCenter().getStart();
                points.add(direction.getDirectionalComponent(connection_point));
            }
        }

        if (!points.isEmpty()) {
            return Optional.of(points.iterator().next());
        }

        return Optional.absent();
    }


    public static boolean straighten(Wire wire, SimpleWire part, Border border, Direction direction, Optional<Double> preferable) {
        List<SimpleWire> connected = wire.getConnectedParts(part);
        EntitySet<SimpleWire> connected_set = new EntitySet<>(connected);
        for (SimpleWire connected_part : connected) {
            List<SimpleWire> farther_connections = wire.getConnectedParts(connected_part);
            connected_set.addAll(farther_connections);
        }

        connected_set = new EntitySet<>(Iterables.filter(connected_set, Utils.UtilPredicates.OrientationPredicate.forOrientation(direction.getOrthogonalDirection().toOrientation())));

        if (connected_set.isEmpty()) {
            return false;
        }

        Point max_coordinate = Collections.max(wire.getCoordinates(), direction.getVertexComparator());
        SimpleWire max = Collections.max(connected_set, Wire.axisComparator(direction.getEdgeComparator()));
        if (max.equals(part) ) { // if our part is max
            if (max.getAxis().isPointInOrOnEdges(max_coordinate)) {
                return false; // we do nothing
            }

            preferable = Optional.of(direction.getDirectionalComponent(max_coordinate));
        }

        Optional<SimpleWire> max_part = wire.closest(part); // todo get max part

        double to_max_part = max_part.isPresent() ? part.axis.distanceToEdge(max_part.get().axis) : 0.0;
        if (preferable.isPresent()) {
            to_max_part = Utils.round(GeomUtils.distance(part.axis.getConstantComponent(), preferable.get()));
        }
        double to_closest_border = getBorderMoveDistance(wire, part, border, direction);

        if (to_closest_border >=0) {
            to_max_part = to_max_part < to_closest_border ? to_max_part : to_closest_border;
        }

        return wire.movePart(part, direction, to_max_part);
    }

    private static double getBorderMoveDistance(final Wire wire, final SimpleWire part, Border border, Direction direction) {
        final Map<BorderPart, Double> parts = border.getClosestParts(part.getAxis(), wire.getSymbol(), direction);
        if (parts.isEmpty()) {
            return -1;
        }
        List<BorderPart> parts_in_order = Lists.newArrayList(parts.keySet());

        final Collection<BorderPart> connected = Collections2.filter(parts.keySet(), new Predicate<BorderPart>() {
            @Override
            public boolean apply(BorderPart input) {
                TopologyElement element = input.getElement();
                if (element != null) {
                    return isConnected(wire, element);
                }
                return true;
            }
        });

        parts_in_order.removeAll(connected);

        if (!parts_in_order.isEmpty()) {
            final BorderPart min = Collections.min(parts_in_order, new Comparator<BorderPart>() {
                @Override
                public int compare(BorderPart o1, BorderPart o2) {
                    return Doubles.compare(parts.get(o1), parts.get(o2));
                }
            });
            return parts.get(min);
        }

        return -1;
    }

    private static List<Command> imitate(Wire wire, Border overlay, Direction direction) {
        return Collections.emptyList();
    }

    public static void main(String... args) {
        Wire wire = new Wire(Orientation.BOTH);

        wire.setFirstPart(Edge.of(0, 0, 100, 0), 50, 100000, true, true, true);
        wire.addPart(Direction.UP, 150, 3000, true);

        Contact contact = new Contact(Edge.of(new Point(200, 150)));
        Contact contact2 = new Contact(Edge.of(new Point(0, 150)));

        WireUtils.connect(wire, contact);
        WireUtils.connect(wire, contact2);
        System.out.println(wire);
    }

}
