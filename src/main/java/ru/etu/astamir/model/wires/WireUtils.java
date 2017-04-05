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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
            List<SimpleWire> connectingWires = Lists.newArrayList();
            Point workingPoint = contact.getCenter().getStart();
            final Optional<SimpleWire> partWithPoint = wire.findPartWithPoint(workingPoint);
            if (partWithPoint.isPresent()) {
                if (!partWithPoint.get().isLink()) {
                    connectingWires.add(wire.addEmptyLinkToPart(partWithPoint.get(), workingPoint));
                }
            } else {
                // we have our contact far away
                Optional<Point> closestPoint = GeomUtils.getClosestPoint(wire.getCoordinates(), workingPoint);
                if (closestPoint.isPresent()) {
                    if (GeomUtils.isOnOneLine(closestPoint.get(), workingPoint)) { // we have to connect with one part
                        Optional<SimpleWire> part = wire.findPartWithPoint(closestPoint.get());
                        if (part.isPresent()) {
                            SimpleWire.Builder builder = new SimpleWire.Builder(wire);
                            builder.setDeformable(true);
                            builder.setStretchable(true);
                            builder.setMovable(true);

                            SimpleWire connection;
                            if (wire.isFirstPart(part.get())) {
                                builder.setAxis(Edge.of(workingPoint.clone(), closestPoint.get().clone()));
                                connection = builder.build();
                                wire.getParts().add(0, connection);

                            } else {
                                builder.setAxis(Edge.of(closestPoint.get().clone(), workingPoint.clone()));
                                connection = builder.build();
                                wire.getParts().add(connection);
                            }

                            connectingWires.add(connection);
                        }
                    }
                    // todo
                }
            }
            return connectingWires; // connected
        } else {
            // some logic for complex contacts
        }

        return Collections.emptyList();
    }

    public static Point getCommonPoint(SimpleWire one, SimpleWire another) {
        return one.axis.findCommonPoint(another.axis);
    }

    public static Optional<Point> getConnectionPoint(Wire one, Wire another) {
        for (SimpleWire onesPart : one.getParts()) {
            for (SimpleWire anothersPart : another.getParts()) {
                final Point commonPoint = onesPart.getAxis().crossing(anothersPart.getAxis());
                if (commonPoint != null) {
                    return Optional.of(commonPoint);
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

        Collection<String> elementNames = element instanceof ComplexElement ?
                Stream.concat(Stream.of(element), ((ComplexElement) element).getElements().stream()).map(Utils.Transformers.NAME_FUNCTION).collect(Collectors.toList()) :
                Collections.singletonList(element.getName());
        Collection<String> connectedNames = ConnectionUtils.getConnectedNames(wire.getConnections());
        if (elementNames.isEmpty()) {
            return false;
        }
        for (String name : connectedNames) {
            if (elementNames.contains(name)) {
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
        for (TopologyElement connectedElement : ConnectionUtils.getConnectedElementsForWire(wire, grid)) {
            if (connectedElement instanceof Contact) {
                final Point connectionPoint = ((Contact) connectedElement).getCenter().getStart();
                points.add(direction.getDirectionalComponent(connectionPoint));
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
        EntitySet<SimpleWire> connectedSet = new EntitySet<>(connected);
        for (SimpleWire connectedPart : connected) {
            List<SimpleWire> fartherConnections = wire.getConnectedParts(connectedPart);
            connectedSet.addAll(fartherConnections);
        }

        // we only need connected parts with same orientation that given part.
        // we need this connected parts to find some maximum boundary for given part, it cant move farther than that

        connectedSet = new EntitySet<>(connectedSet.stream().filter(Utils.UtilPredicates.OrientationPredicate.forOrientation(part.axis.getOrientation())).collect(Collectors.toList()));

        if (connectedSet.isEmpty()) { // means we only have one part in this wire, no need to straighten it
            return false;
        }

        Optional<SimpleWire> closestPart = wire.closestWithSameOrientation(part);
        double toClosestPart = isMin(wire, part, direction) ? 0.0 : closestPart.isPresent() ? part.axis.distanceToEdge(closestPart.get().axis) : 0.0;

        if (preferable.isPresent()) {
            toClosestPart = GeomUtils.distance(part.axis.constant(), preferable.get());
        }
        double toClosestBorder = getBorderMoveDistance(wire, part, border, direction);

        if (toClosestBorder >=0) {
            toClosestPart = toClosestPart < toClosestBorder ? toClosestPart : toClosestBorder;
        }

        return wire.movePart(part, direction, toClosestPart);
    }

    private static double getBorderMoveDistance(final Wire wire, final SimpleWire part, Border border, Direction direction) {
        final Map<BorderPart, Double> parts = border.getClosestParts(part.getAxis(), wire.getSymbol(), direction);
        if (parts.isEmpty()) {
            return -1;
        }
        List<BorderPart> partsInOrder = Lists.newArrayList(parts.keySet());

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

        partsInOrder.removeAll(connected);

        if (!partsInOrder.isEmpty()) {
            final BorderPart min = Collections.min(partsInOrder, new Comparator<BorderPart>() {
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

    public static List<Command> imitate(Wire wire, Border overlay, boolean deformationAllowed, Direction direction) {
        //bus.correctBus();
        List<Command> commands = new LinkedList<>();
//        if (!wire.isChained()) {
//            wire.ensureChained();
//        }
        if (deformationAllowed)
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

    private void fixWire(Wire wire) {
        if (!wire.isChained() || !wire.isOrthogonal()) {

        }

        wire.removeEmptyParts(false);
    }

    public static boolean isMin(Wire wire, SimpleWire part, Direction dir) {
        boolean min = true;
        double constant = part.getAxis().constant();
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
