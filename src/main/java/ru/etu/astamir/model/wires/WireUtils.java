package ru.etu.astamir.model.wires;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import ru.etu.astamir.common.Utils;
import ru.etu.astamir.common.collections.EntitySet;
import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.BorderPart;
import ru.etu.astamir.compression.commands.Command;
import ru.etu.astamir.compression.commands.MoveCommand;
import ru.etu.astamir.compression.commands.MoveSimpleWireCommand;
import ru.etu.astamir.compression.commands.compression.SimpleCompressCommand;
import ru.etu.astamir.compression.grid.Grid;
import ru.etu.astamir.geom.common.*;
import ru.etu.astamir.math.MathUtils;
import ru.etu.astamir.model.ComplexElement;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.connectors.ConnectionUtils;
import ru.etu.astamir.model.contacts.Contact;
import ru.etu.astamir.model.contacts.ContactType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Утилитный класс для различных действий над шинами и контурами.
 *
 * @author Artem Mon'ko
 */
public class WireUtils {
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

    public static Point getCommonPoint(SimpleWire one, SimpleWire another) {
        return one.axis.findCommonPoint(another.axis);
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

        return Optional.empty();
    }

    public static Optional<Point> getConnectionPoint(Edge[] one, Edge... another) {
        if (another.length == 0) {
            return Optional.empty();
        }
        for (Edge o : one) {
            for (Edge a : another) {
                Point crossing = o.crossing(a);
                if (crossing != null) {
                    return Optional.of(crossing);
                }
            }
        }

        return Optional.empty();
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
        wire.removeEmptyParts(false);
        boolean hadBeenChanged;
        do {
            hadBeenChanged = false;
            Optional<Double> connectionPoints = getConnectionPoints(wire, direction, grid);
            for (SimpleWire part : wire.orientationParts(direction)) {
                hadBeenChanged |= straighten(wire, part, border, direction, connectionPoints);
            }

            wire.removeEmptyParts(false);
        } while (hadBeenChanged);
        return false;
    }

    private static Optional<Double> getConnectionPoints(Wire wire, Direction direction, Grid grid) {
        List<Double> points = new ArrayList<>();
        for (TopologyElement connected_element : ConnectionUtils.getConnectedElementsForWire(wire, grid)) {
            if (connected_element instanceof Contact) {
                final Point connection_point = ((Contact) connected_element).getCenter().getStart();
                points.add(direction.getDirectionalComponent(connection_point));
            }
        }

        if (points.size() == 1) {
            return Optional.of(points.iterator().next());
        } else if (!points.isEmpty()) {
            double d = points.get(0);
            for (int i = 1; i < points.size(); i++) {
                double next = points.get(i);
                if (MathUtils.compare(d, next, MathUtils.getPrecision(MathUtils.EPS)) != 0) {
                    return Optional.empty();
                }
                d = next;
            }

            return Optional.of(d);
        }

        return Optional.empty();
    }

    public static boolean straighten(Wire wire, SimpleWire part, Border border, Direction direction, Optional<Double> preferable) {
        List<SimpleWire> connected = wire.getConnectedParts(part);
        EntitySet<SimpleWire> connected_set = new EntitySet<>(connected);
        for (SimpleWire connected_part : connected) {
            List<SimpleWire> farther_connections = wire.getConnectedParts(connected_part);
            connected_set.addAll(farther_connections);
        }

        // we only need connected parts with same orientation that given part.
        // we need this connected parts to find some maximum boundary for given part, it cant move farther than that
        connected_set = new EntitySet<>(Iterables.filter(connected_set,
                Utils.UtilPredicates.OrientationPredicate.forOrientation(part.axis.getOrientation())));

        if (connected_set.isEmpty()) { // means we only have one part in this wire, no need to straighten it
            return false;
        }

        Optional<SimpleWire> closest_part = wire.closestWithSameOrientation(part);
        double to_closest_part = isMin(wire, part, direction) ? 0.0 : closest_part.isPresent() ? part.axis.distanceToEdge(closest_part.get().axis) : 0.0;

        if (preferable.isPresent()) {
            to_closest_part = Utils.round(GeomUtils.distance(part.axis.getConstantComponent(), preferable.get()));
        }
        double to_closest_border = getBorderMoveDistance(wire, part, border, direction);

        if (to_closest_border >=0) {
            to_closest_part = to_closest_part < to_closest_border ? to_closest_part : to_closest_border;
        }

        return wire.movePart(part, direction, to_closest_part);
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

    public static List<SimpleCompressCommand> moveSimpleWire(SimpleWire wire, Direction direction, double length, BorderPart part) {
        List<SimpleCompressCommand> commands = new ArrayList<>();
        if (wire.getWire() != null) { // part of a complex wire
            commands.add(new SimpleCompressCommand(new MoveSimpleWireCommand(wire, direction, length), part));
        } else {
            commands.add(new SimpleCompressCommand(new MoveCommand(wire, direction, length), part));
        }
        return commands;
    }

    public static List<Command> imitate(Wire wire, Border overlay, boolean deformation_allowed, Direction direction) {
        //bus.correctBus();
        List<Command> commands = new LinkedList<>();
//        if (!wire.isChained()) {
//            wire.ensureChained();
//        }
        wire.round();
        if (deformation_allowed)
            createEmptyLinks(wire, overlay, direction);

        for (SimpleWire part : wire.getParts()) {
            if (!part.getAxis().getOrientation().isOrthogonal(direction.toOrientation()) || part.getAxis().isPoint()) {
                continue;
            }
            Optional<BorderPart> closestPart = overlay.getClosestPartWithConstraints(part.getAxis(), wire.getSymbol(), direction);
            if (closestPart.isPresent()) {
                double distToMove = overlay.getMoveDistance(closestPart.get(), wire.getSymbol(),
                        direction, part.getAxis().getStart());

                commands.addAll(moveSimpleWire(part, direction, distToMove, closestPart.get()));
            }
        }

        //wire.removeEmptyParts();
        return commands;
    }

    private static void createEmptyLinks(Wire bus, Border border, Direction direction) {
        List<BorderPart> nonOrientParts = border.getParts().stream().filter(border.orientation().negate()).collect(Collectors.toList());
        //boolean wasEmptyPartsOnEdges = bus.hasEmptyPartsOnEdges();
        for (BorderPart part : nonOrientParts) {
            double min = border.getMinDistance(part, bus.getSymbol());
            Point one = part.getAxis().getStart().clone();
            GeomUtils.move(one, direction.clockwise(), min);
            Point another = part.getAxis().getStart().clone();
            GeomUtils.move(another, direction.counterClockwise(), min);

            bus.createAnEmptyLink(one, direction.opposite());
            bus.createAnEmptyLink(another, direction.opposite());
        }


        //bus.removeEmptyPartsOnEdges(); // handle more carefully
        //bus.removeEmptyParts(!wasEmptyPartsOnEdges);
        // todo remove empty links on 5the edges ?
    }

    public static boolean isMin(Wire wire, SimpleWire part, Direction dir) {
        boolean min = true;
        double constant = part.getAxis().getConstantComponent();
        for (SimpleWire p : wire.getParts()) {
            Edge axis = p.getAxis();
            double start = dir.getDirectionalComponent(axis.getStart());
            double end = dir.getDirectionalComponent(axis.getEnd());
            if (dir.isUpOrRight()) {
                if (start > constant || end > constant) {
                    min = false;
                }
            } else {
                if (start < constant || end < constant) {
                    min = false;
                }
            }
        }

        return min;
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
