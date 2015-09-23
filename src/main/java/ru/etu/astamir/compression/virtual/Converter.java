package ru.etu.astamir.compression.virtual;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import ru.etu.astamir.common.Utils;
import ru.etu.astamir.common.collections.CollectionUtils;
import ru.etu.astamir.common.collections.EntitySet;
import ru.etu.astamir.compression.grid.VirtualGrid;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.geom.common.Rectangle;
import ru.etu.astamir.launcher.VirtualTopology;
import ru.etu.astamir.model.ComplexElement;
import ru.etu.astamir.model.Flap;
import ru.etu.astamir.model.TopologicalCell;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.common.Pair;
import ru.etu.astamir.model.connectors.SimpleConnectionPoint;
import ru.etu.astamir.model.contacts.Contact;
import ru.etu.astamir.model.exceptions.UnexpectedException;
import ru.etu.astamir.model.regions.ActiveRegion;
import ru.etu.astamir.model.regions.ContactWindow;
import ru.etu.astamir.model.regions.Contour;
import ru.etu.astamir.model.technology.Technology;
import ru.etu.astamir.model.wires.*;

import java.util.*;

/**
 * Класс предназначен для перевода топологии из реальных в виртуальные коориданты.
 *
 * @author Artem Mon'ko
 */
// todo карманы должны выступать за границу ячейки на сколько то(расстояние от кармана до кармана??)

public class Converter {
    private VirtualTopology topology;

    public Converter(VirtualTopology topology) {
        this.topology = topology;
    }

    public void convert() throws ConvertException {
        final double step = getMaxStep(topology.getVirtual()); // we got our real step
        VirtualGrid real = VirtualGrid.deepCopyOf(topology.getVirtual());
        List<TopologyElement> allElements = Lists.newArrayList(real.getAllElements());
        List<TopologicalCell> cells = getElementsOfType(allElements, TopologicalCell.class);
        if (cells.isEmpty()) {
            // we have to create one
            // but first we have to figure out its boundary
            Polygon coordinates = Polygon.of(Iterables.concat(Lists.transform(allElements, new Function<TopologyElement, Collection<Point>>() {
                @Override
                public Collection<Point> apply(TopologyElement input) {
                    return input.getCoordinates();
                }
            })));
            Polygon bounds = coordinates.getBounds();
            if (bounds.isRectangle()) {
                Rectangle rectangle = Rectangle.of(bounds.vertices());
                rectangle.stretch(1); // just in case
                bounds = rectangle;
            }
            TopologicalCell cell = new TopologicalCell("main_cell", "TC", bounds);
            cells.add(cell);
            real.addElement(cell);
        }
        // todo: check that all topological cells do not intersect


        // todo: make sure that all elements are contained in some topological cell
        connect(real.getAllElements());

        EntitySet<TopologyElement> processedElements = new EntitySet<>();
        for (TopologicalCell cell : cells) {
            processContour(cell, step); // set cell boundary

            for (TopologyElement element : cell.getElements()) {
                if (processedElements.contains(element)) {
                    continue; // skip already processed elements
                }
                if (element instanceof ActiveRegion) {
                    processActiveRegion((ActiveRegion) element, step);
                } else if (element instanceof Contour) {
                    processContour((Contour) element, step);
                } else if (element instanceof Wire) {
                    processWire((Wire) element, step, true);
                } else if (element instanceof Contact) {
                    processContact((Contact) element, step);
                }
                processedElements.add(element);
            }
        }

        topology.setReal(real);
        topology.setMode(VirtualTopology.REAL_MODE);
    }

    private void connect(Collection<TopologyElement> allElements) {
        for (TopologyElement element : allElements) {
            if (element instanceof Contour) {
                Contour contour = (Contour) element;
                Collection<TopologyElement> connectedElements = getConnectedElements(allElements, element);
                contour.addAllElements(connectedElements);
            } else if (element instanceof Wire) {
                // todo look through
                Wire wire = (Wire) element;
                Collection<TopologyElement> connectedElements = getConnectedElements(allElements, element);
                // create empty links
                for (TopologyElement connectedElement : connectedElements) {
                    if (connectedElement instanceof Gate) {

                    }
                    if (connectedElement instanceof Contact) {
                        Contact contact = (Contact) connectedElement;
                        if (contact.getCenter().isPoint()) { // todo implement for long contacts
                            Point center = contact.getCenter().getStart();
                            Optional<SimpleWire> connected_part = wire.findPartWithPoint(center);
                            if (connected_part.isPresent()) {
                                wire.addEmptyLinkToPart(connected_part.get(), center);
                            }
                        }
                    }
                }
                wire.addConnection(new SimpleConnectionPoint(new HashSet<>(CollectionUtils.toNames(connectedElements))));
            } else if (element instanceof Contact) {
                Contact contact = (Contact) element;
                Collection<TopologyElement> connectedElements = getConnectedElements(allElements, element);
                contact.setConnectedElements(new HashSet<>(CollectionUtils.toNames(connectedElements)));
            }
        }
    }

    private <V> List<V> getElementsOfType(Iterable<TopologyElement> elements, final Class<? extends V> type) {
        List<V> result = new ArrayList<>();
        for (TopologyElement element : elements) {
            if (type.isInstance(element)) {
                result.add(type.cast(element));
            }
        }

        return result;
    }

    /**
     * Найти все элементы, так или иначе подсоединенные к заданному
     *
     * @param allElements Все элементы топологии
     * @param element     исследуемый элемент
     * @return коллекция связанный элементов.
     */
    private Collection<TopologyElement> getConnectedElements(Collection<TopologyElement> allElements, final TopologyElement element) {
        List<TopologyElement> connectedElements = new ArrayList<>();

        Predicate<TopologyElement> isConnected = new ConnectedPredicate(element);
        for (TopologyElement elem : allElements) {
            if (elem.equals(element)) {
                continue;
            }
            if (isConnected.apply(elem)) {
                connectedElements.add(elem);
            }
        }

        return connectedElements;
    }

    private static class ConnectedPredicate implements Predicate<TopologyElement> {
        private final TopologyElement element;
        private Predicate<TopologyElement> contour = new Predicate<TopologyElement>() {
            @Override
            public boolean apply(TopologyElement input) {
                return containedInContour(element, input);
            }
        };

        private Predicate<TopologyElement> wire = new Predicate<TopologyElement>() {
            @Override
            public boolean apply(TopologyElement input) {
                return containedInWire((Wire) element, input);
            }
        };

        private Predicate<TopologyElement> contact = new Predicate<TopologyElement>() {
            @Override
            public boolean apply(TopologyElement input) {
                return containedInContact((Contact) element, input);
            }
        };

        private Predicate<TopologyElement> predicate;

        public ConnectedPredicate(TopologyElement element) {
            this.element = element;
            this.predicate = getActivePredicate(element);
        }

        private Predicate<TopologyElement> getActivePredicate(TopologyElement element) {
            if (element instanceof Wire) {
                return wire;
            } else if (element instanceof Contact) {
                return contact;
            } else {
                return contour;
            }
        }

        @Override
        public boolean apply(TopologyElement element) {
            return predicate.apply(element);
        }

        @Override
        public boolean equals(Object o) {
            return predicate.equals(o);
        }
    }

    private static boolean containedInContour(TopologyElement contour, TopologyElement element) {
        Polygon bounds = contour.getBounds();
        return (bounds.intersects(Polygon.of(element.getCoordinates())) || bounds.contains(element.getCoordinates())) && !element.getBounds().contains(bounds);
    }

    private static boolean containedInWire(Wire wire, TopologyElement element) {
        List<Edge> axises = Lists.transform(wire.getParts(), new Function<SimpleWire, Edge>() {
            @Override
            public Edge apply(SimpleWire simpleWire) {
                return simpleWire.getAxis();
            }
        });

        List<Point> coordinates = Lists.newArrayList(element.getCoordinates());
        for (Edge axis : axises) {
            for (Point coordinate : coordinates) {
                if (axis.isPointInOrOnEdges(coordinate)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean containedInContact(Contact contact, TopologyElement element) {
        Edge center = contact.getCenter();
        for (Point coordinate : element.getCoordinates()) {
            if (center.isPointInOrOnEdges(coordinate)) {
                return true;
            }
        }
        return false;
    }

    private void processContour(Contour contour, double step) {
        Polygon coordinates = Polygon.of(contour.getCoordinates());
        coordinates.scale(step);
        contour.setContour(coordinates);
    }

    private void processWire(Wire wire, double step, boolean skipGates) {
        if (skipGates && wire instanceof Gate) {
            return;
        }
        double minWidth = topology.getTechnology().getCharacteristics().getMinWidth(wire.getSymbol());
        for (SimpleWire part : wire.getParts()) {
            Edge axis = part.getAxis();
            axis.scale(step);
            part.setAxis(axis);
            part.setWidth(minWidth / 2);
            part.setWidthAtBorder(minWidth / 2);
        }
    }

    private void processGate(Gate gate, double step) {
        processWire(gate, step, false);

        // set flaps real coordinates and size
        for (Flap flap : gate.getFlaps().values()) {
            processContact(flap, step); // todo
        }
    }

    private void processActiveRegion(ActiveRegion activeRegion, double step) {
        Technology.TechnologicalCharacteristics characteristics = topology.getTechnology().getCharacteristics();

        Polygon bounds = activeRegion.getBounds();
        Collection<TopologyElement> elements = activeRegion.getElements();
        List<Contact> contacts = getElementsOfType(elements, Contact.class);
        if (!contacts.isEmpty()) {

        }

        List<Gate> gates = getElementsOfType(elements, Gate.class);
        if (!gates.isEmpty()) {
            double length = characteristics.getMinWidth("LN_MIN");
            for (Gate gate : gates) {
                processGate(gate, step);
            }
        }
        processContour(activeRegion, step);

        // Set real coordinates to the contour
        // determine the width and orientation of transistor
        // set length and width of all gates
        // what about contacts
    }

    private void processContact(Contact contact, double step) {
        // set real center
        contact.getCenter().scale(step);
        // assign all the contact windows

        Technology.TechnologicalCharacteristics characteristics = topology.getTechnology().getCharacteristics();

//        List<ActiveRegion> activeRegions = getElementsOfType(contact.getConnectedElements(), ActiveRegion.class);
//        if (!activeRegions.isEmpty()) {
//            ActiveRegion activeRegion = activeRegions.get(0);
//
//            Polygon bounds = activeRegion.getBounds();
//            double m1 = characteristics.getMinWidth("M1");
//            double m1_active = characteristics.getMinDistance("M1", activeRegion.getSymbol());
//            Edge center = contact.getCenter();
//            if (!center.isPoint()) {
//                return; // todo fix
//            }
//            final Point p = center.getStart();
//            List<Edge> edges = Lists.newArrayList(Iterables.filter(bounds.edges(), new Predicate<Edge>() {
//                @Override
//                public boolean apply(Edge input) {
//                    return input.isPointInOrOnEdges(p);
//                }
//            }));
//
//            if (!edges.isEmpty()) {
//                List<Direction> directions = Lists.transform(edges, new Function<Edge, Direction>() {
//                    @Override
//                    public Direction apply(Edge input) {
//                        return input.getDirection();
//                    }
//                });
//
//                double d = m1 / 2 + m1_active;
//                for (Direction direction : directions) {
//                    if (direction.isUpOrDown()) {
//                        direction = direction.getOppositeDirection();
//                    }
//                    GeomUtils.move(center, direction, d);
//                }
//            }
//        }

        Collection<ContactWindow> contactWindows = contact.getContactWindows().values();
        for (ContactWindow window : contactWindows) {
            double width = characteristics.getMinWidth(window.getSymbol());
            double height = characteristics.getMinHeight(window.getSymbol());
            if (width == 0 ^ height == 0) {
                width = width + height;
                height = height + width;
            }
            window.setContour(Rectangle.of(contact.getCenter(), width / 2,
                    height / 2));
        }
    }

    private double getMaxStep(VirtualGrid grid) {
        final Technology.TechnologicalCharacteristics characteristics = topology.getTechnology().getCharacteristics();
        if (characteristics == null) { // Although it is odd, but lets check
            throw new UnexpectedException("No Technological Characteristics");
        }

        Set<String> types = Sets.newHashSet();
        for (TopologyElement element : grid.getAllElements()) { // get all the element types from the grid
            types.addAll(extractElementSymbols(element));
        }

        List<Pair<String, String>> chars = CollectionUtils.getAllUniquePairs(Lists.newArrayList(types)); // get all the unique pairs of that types
        for (String type : types) {
            chars.add(Pair.of(type, type));
        }
        List<Double> distances = Lists.transform(chars, new Function<Pair<String, String>, Double>() { // collection of the min distances between the elements
            @Override
            public Double apply(Pair<String, String> input) {
                return characteristics.getMinDistance(input.left, input.right);
            }
        });


        return distances.isEmpty() ? 0.0 : Collections.max(distances);
    }

    private List<String> extractElementSymbols(TopologyElement element) {
        List<String> symbols = Lists.newArrayList();
        if (element instanceof ComplexElement) {
            for (TopologyElement elem : ((ComplexElement) element).getElements()) {
                symbols.addAll(extractElementSymbols(elem));
            }
        } else {
            symbols.add(element.getSymbol());
        }

        return symbols;
    }


}
