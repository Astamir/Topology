package ru.etu.astamir.compression.commands.compression;

import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.BorderPart;
import ru.etu.astamir.compression.commands.MoveContourCommand;
import ru.etu.astamir.compression.commands.UpdateBorderCommand;
import ru.etu.astamir.compression.grid.VirtualGrid;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Rectangle;
import ru.etu.astamir.model.TopologicalCell;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.TopologyLayer;
import ru.etu.astamir.model.connectors.ConnectionUtils;
import ru.etu.astamir.model.exceptions.UnexpectedException;
import ru.etu.astamir.model.regions.ActiveRegion;
import ru.etu.astamir.model.regions.Well;
import ru.etu.astamir.model.regions.Contour;
import ru.etu.astamir.model.technology.Technology;
import ru.etu.astamir.model.wires.Gate;
import ru.etu.astamir.model.wires.SimpleWire;
import ru.etu.astamir.model.wires.WireUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Artem Mon'ko
 */
public class CompressContourCommand extends CompressCommand {
    protected Direction side;

    protected MoveContourCommand move;
    protected UpdateBorderCommand update_border;

    public CompressContourCommand(VirtualGrid grid, Map<TopologyLayer, Map<Direction, Border>> borders, String element_name, Direction direction, Direction side) {
        super(grid, borders, element_name, direction);
        this.side = side;
    }

    @Override
    public boolean execute() {
        TopologyElement element = getElement();
        if (!(element instanceof Contour)) {
            throw new UnexpectedException("Given element={" + element +"} is not a contour, but passed to a contour processing command");
        }

        Contour contour = (Contour) element;

        Collection<Border> affectedBorders = getAffectedBorders();
        Edge edge = new Rectangle(contour.getBounds()).getEdge(side);
        double length = getMoveDistanceForEdge(edge, contour, affectedBorders, direction);
        Optional<Contour> container = grid.getElementsContainer(element_name);
        if (container.isPresent()) {
            Contour cnt = container.get();
            Border container_border = Border.of(direction.getOrthogonalDirection().toOrientation(), affectedBorders.iterator().next().getTechnology(), cnt); // TODO technology
            container_border.setLayer(cnt.getLayer());
            double container_l = getMoveDistanceForEdge(edge, contour, Collections.singleton(container_border), direction);
            length = container_l < length ? container_l : length;
        }

        moveConnected(contour, edge, length);
        move(contour, length, affectedBorders);

        return true;
    }

    protected void moveConnected(Contour contour, Edge side, double length) {
        if (contour instanceof ActiveRegion) {
            List<TopologyElement> gates = contour.getElements().stream().filter(e -> e instanceof Gate).collect(Collectors.toList());
            for (TopologyElement gate : gates) {
                Gate g = (Gate) gate;
                List<Edge> collect = g.getParts().stream().map(SimpleWire::getAxis).collect(Collectors.toList());
                Optional<Point> connectionPoint = WireUtils.getConnectionPoint(collect.toArray(new Edge[collect.size()]), side);
                if (connectionPoint.isPresent()) {
                    Optional<SimpleWire> partWithPoint = g.findPartWithPoint(connectionPoint.get());
                    if (partWithPoint.isPresent()) {
                        g.stretch(partWithPoint.get(), direction, length);
                    }
                }
            }
        }
    }

    protected void move(Contour contour, double length, Collection<Border> affectedBorders) {
        move = new MoveContourCommand(contour, length, direction, side);
        move.execute();

        update_border = new UpdateBorderCommand(affectedBorders, contour, side);
        update_border.execute();
    }

    protected double getMoveDistanceForEdge(Edge edge, Contour contour, Collection<Border> borders, Direction direction) {
        double length = 0.0;
        Technology.TechnologicalCharacteristics technology = borders.isEmpty() ? null : borders.iterator().next().getTechnology();
        BorderPart closest_border = null;
        for (Border border : borders) {
            final Optional<BorderPart> closest = border.getClosestPartWithConstraints(edge, contour.getSymbol(), direction);
            if (closest.isPresent()) {

                BorderPart closest_border_part = closest.get();
                for (Point coordinate : edge.getPoints()) {
                    double l = border.getMoveDistance(closest_border_part, contour.getSymbol(), direction, coordinate);
                    if (l < length || length == 0) {
                        length = l;
                        closest_border = closest_border_part;
                    }
                }
            }
        }

        if (closest_border != null && technology != null) { // check for bulks and topological cell
            if (closest_border.getSymbol().equals(TopologicalCell.DEFAULT_CELL_SYMBOL) && contour instanceof Well) {
                if (Direction.RIGHT.getEdgeComparator().compare(closest_border.getAxis(), edge) < 0) {
                    double min = technology.getMinDistance(contour.getSymbol(), contour.getSymbol());
                    length += 2 * min;
                }
            }
        }

        return length;
    }

    @Override
    public boolean unexecute() {
        if (move != null && update_border != null) {
            return update_border.unexecute() & move.unexecute();
        }
        return false;
    }

    @Override
    public String toString() {
        if (move == null) {
            return "Compress contour command with null move command for" + element_name;
        }
        return move.toString();
    }
}
