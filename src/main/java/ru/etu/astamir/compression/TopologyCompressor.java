package ru.etu.astamir.compression;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import ru.etu.astamir.compression.commands.*;
import ru.etu.astamir.compression.commands.compression.*;
import ru.etu.astamir.compression.grid.VirtualGrid;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.*;
import ru.etu.astamir.launcher.Topology;
import ru.etu.astamir.model.*;
import ru.etu.astamir.model.contacts.Contact;
import ru.etu.astamir.model.exceptions.UnexpectedException;
import ru.etu.astamir.model.regions.Contour;
import ru.etu.astamir.model.wires.Wire;

import java.util.*;

/**
 * @author Artem Mon'ko
 */
public class TopologyCompressor {
    Topology topology;
    public CommandManager commands = new CommandManager(); // public for debug
    //VirtualGrid working_grid;
    int mode;
    public Map<TopologyLayer, Map<Direction, Border>> borders = new HashMap<>(); // public for debug
    private Map<Entity, Integer> processed_elements = new HashMap<>();
    private Multimap<String, Direction> processed_contours = ArrayListMultimap.create();

    private enum CompressionStage {
        UNCOMPRESSED, COMPRESSED, PARTIALLY_COMPRESSED;
    }

    CompressionStage stage = CompressionStage.UNCOMPRESSED;

    public TopologyCompressor(Topology topology) {
        this.topology = Preconditions.checkNotNull(topology);
      //  this.working_grid = Preconditions.checkNotNull(topology.getGrid());
    }

    /**
     *
     * @return топология после сжатия
     */
    public void compress() {
        commands.clear();

        prepareBorders();
        compress(Direction.LEFT);
        compress(Direction.RIGHT);
        straightenWires(Direction.LEFT);
     // compress(Direction.UP);
//        compress(Direction.DOWN);
//        straightenWires(Direction.UP);
        // todo compressing wires for another direction
        // compressing active regions
        // correcting wires
        stage = CompressionStage.COMPRESSED;
    }

    public VirtualGrid step(int steps) {
        int step_counter = 0;
        while (step_counter < steps) {
            commands.executeNext();
            step_counter++;
        }
        return topology.getGrid();
    }

    void prepareBorders() {
        Collection<TopologyLayer> availableLayers = ProjectObjectManager.getLayerFactory().getAvailableLayers();

        Optional<TopologicalCell> cell = findFirst(TopologicalCell.class);
        if (!cell.isPresent()) {
            throw new UnexpectedException("There should be at least one topological cell in working grid");
        }
        TopologicalCell topologicalCell = cell.get();

        for (TopologyLayer layer : availableLayers) {
            Map<Direction, Border> border_map = new HashMap<>();
            for (Direction dir : Direction.all()) {
                Border border = new Border(dir.toOrientation().getOppositeOrientation(), topology.getTechnology().getCharacteristics(), Lists.newArrayList(new BorderPart(topologicalCell.get(dir), topologicalCell, topologicalCell.getSymbol())));
                border.setLayer(layer);
                border_map.put(dir, border);
            }
            borders.put(layer, border_map);
        }
    }

    void compress(Direction direction) {
        processed_elements.clear();
        processed_contours.clear();


        for (List<TopologyElement> column : topology.getGrid().walk(direction)) {
                clearProcessedContours();
                for (TopologyElement element : column) {
                    if (element instanceof TopologicalCell) {
                        continue; // do not change topological cell
                    }

                    processElement(element, CompressionUtils.getAffectedBorders(element, borders, direction), direction);
                }
            }

            fitTopologicalCell(direction);
    }

    void straightenWires(Direction direction) {
        processed_elements.clear();
        for (List<TopologyElement> column : topology.getGrid().walk(direction.opposite())) {
            clearProcessedContours();
            for (TopologyElement element : column) {
                if (element instanceof Wire) {
                    if (!isElementProcessed(element, direction)) {
                        commands.addCommand(new StraightenWireCommand(topology.getGrid(), borders, element.getName(), direction));
                        updateProcessedStatus(element, direction);
                    }
                }
            }
        }

    }

    private boolean isElementProcessed(TopologyElement element, Direction direction) {
        if (!processed_elements.containsKey(element)) {
            processed_elements.put(element, 0);
            return false;
        }

        int processed = processed_elements.get(element);
        int max_processed = 1;
        if (element instanceof Wire) {
            Wire wire = (Wire) element;
            if (!wire.getOrientation().isOrthogonal(direction.toOrientation())) {
                long count = wire.getParts().stream().filter(part -> part.getAxis().getOrientation().isOrthogonal(wire.getOrientation())).count();
                if (count > 0) {
                    max_processed = (int) count;
                }
            }
        }
        return processed >= max_processed;
    }

    private void clearProcessedContours() {
        for (Entity elem : processed_elements.keySet()) {
            if (elem instanceof Contour) {
                int processed = processed_elements.get(elem);
                processed = processed == 0 ? processed : --processed;
                processed_elements.put(elem, processed);
            }
        }
    }

    private void processElement(TopologyElement element, Collection<Border> affectedBorders, final Direction compressionDirection) {
        if (!isElementProcessed(element, compressionDirection)) {
            if (element instanceof Contact) {
                processContact((Contact) element, affectedBorders, compressionDirection, Collections.<Contact>emptyList());
            } else if (element instanceof Wire) {
                //Border border = affectedBorders.iterator().next();
                //processWire((Wire) element, border, compressionDirection);
                final CompressWireCommand command = new CompressWireCommand(topology.getGrid(), borders, element.getName(), compressionDirection);
                if (compressionDirection == Direction.RIGHT) {
                    command.setGateDeformationAllowed(false);
                }
                commands.addCommand(command);
            } else if (element instanceof Contour) {
                processContour((Contour) element, affectedBorders, compressionDirection);
            } else {

            }
            updateProcessedStatus(element, compressionDirection);
        }
    }

    private void updateProcessedStatus(TopologyElement element, Direction direction) {
        if (!processed_elements.containsKey(element)) {
            processed_elements.put(element, 1);
        }

        int processed = processed_elements.get(element);
        processed_elements.put(element, ++processed);
    }

    private static double getMoveDistance(TopologyElement element, Collection<Border> borders, TopologyElement... additional) {

        return 0;
    }

    private double getContactMoveDistance(Contact contact, Collection<Border> borders, Direction direction, Collection<Contact> otherContacts) {
        double length = 0.0;
        Collection<Point> coordinates = contact.getCoordinates();
        for (Border border : borders) {
            for (Point coordinate : coordinates) {
                final Optional<BorderPart> closest = border.getClosestPartWithConstraints(contact.getCenter(), contact.getSymbol(), direction);
                if (closest.isPresent()) {
                    double l = border.getMoveDistance(closest.get(), contact.getSymbol(), direction, coordinate);
                    length = l < length || length == 0 ? l : length;
                }
            }
        }
        return length;
    }

    private double getMoveDistanceForEdge(Edge edge, String symbol, Collection<Border> borders, Direction direction) {
        double length = 0.0;
        for (Border border : borders) {
            final Optional<BorderPart> closest = border.getClosestPartWithConstraints(edge, symbol, direction);
            if (closest.isPresent()) {
                for (Point coordinate : edge.getPoints()) {
                    double l = border.getMoveDistance(closest.get(), symbol, direction, coordinate);
                    length = l < length || length == 0 ? l : length;
                }
            }
        }
        return length;
    }

    /**
     * Обработка контакта. Тут просто передвигаем в направлении сжатия на сколько может.
     * Может мешать частокол и другие контакты(диагональные расстояния)
     */
    void processContact(Contact contact, Collection<Border> affectedBorders, Direction direction, Collection<Contact> otherContacts) {
        // first we have to figure out our moving distance
//        double length = getContactMoveDistance(contact, affectedBorders, direction, otherContacts);
//
//        // find out if some other contacts are in the way
//        length = diagonalAnalysis(length, contact, otherContacts);
//
//        move(contact, direction, length, affectedBorders);
        commands.addCommand(new CompressContactCommand(topology.getGrid(), borders, direction, contact.getName()));
    }

    private double diagonalAnalysis(double moveLength, Contact contact, Collection<Contact> otherContacts) {
        return moveLength; // todo implement
    }

    void processWire(Wire wire, Border border, Direction direction) {
        commands.addCommand(new CompositeCommand(new ImitateCommand(wire, border, direction, topology.getGrid())/*, new UpdateBorderCommand(Collections.singletonList(border), wire, direction)*/));
        new UpdateBorderCommand(Collections.singletonList(border), wire, direction).execute();

    }

    void processActiveRegion() {

    }

    void processGate() {

    }

    void processContour(Contour contour, Collection<Border> affectedBorders, Direction direction) {
        Direction side = direction;
        if (processed_contours.containsEntry(contour.getName(), direction)) {
            side = side.opposite();
        }

//        double move = getMoveDistanceForEdge(new Rectangle(contour.getBounds()).getEdge(direction), contour.getSymbol(), borders, direction);
//        commands.addCommand(new CompositeCommand(new MoveContourCommand(contour, move, direction, side)/*, new UpdateBorderCommand(borders, contour, direction)*/));
//        new UpdateBorderCommand(borders, contour, direction).execute();
        commands.addCommand(new CompressContourCommand(topology.getGrid(), borders, contour.getName(), direction, side));
        processed_contours.put(contour.getName(), side);
    }

    void fitTopologicalCell(Direction direction) {
        Optional<TopologicalCell> first = findFirst(TopologicalCell.class);
        if (!first.isPresent()) {
            throw new UnexpectedException("no topological cell ?");
        }

        TopologicalCell cell = first.get();
        commands.addCommand(new CompressTopologicalCellCommand(topology.getGrid(), borders, cell.getName(), direction));
    }

    private <V extends TopologyElement> Optional<V> findFirst(final Class<V> clazz) {
        for (TopologyElement element : topology.getGrid().getAllElements()) {
            if (clazz.isInstance(element)) {
                return Optional.of((V) element);
            }
        }

        return Optional.empty();
    }

    void move(TopologyElement element, Direction direction, double length, Collection<Border> borders) {
        commands.addCommand(new CompositeCommand(new MoveCommand(element, direction, length)/*, new UpdateBorderCommand(borders, element, direction)*/));
        new UpdateBorderCommand(borders, element, direction).execute();
    }
}
