package ru.etu.astamir.compression.commands.compression;

import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.BorderPart;
import ru.etu.astamir.compression.grid.VirtualGrid;
import ru.etu.astamir.geom.common.*;
import ru.etu.astamir.model.TopologicalCell;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.TopologyLayer;
import ru.etu.astamir.model.exceptions.UnexpectedException;
import ru.etu.astamir.model.regions.Well;

import java.util.*;

/**
 * @author Artem Mon'ko
 */
public class CompressTopologicalCellCommand extends CompressContourCommand {

    public CompressTopologicalCellCommand(VirtualGrid grid, Map<TopologyLayer, Map<Direction, Border>> borders, String elementName, Direction direction) {
        super(grid, borders, elementName, direction, direction.opposite());
    }


    @Override
    public boolean execute() {
        TopologyElement element = getElement();
        if (!(element instanceof TopologicalCell)) {
            throw new UnexpectedException("Given element={" + element +"} is not a topological cell, but passed to a topological cell processing command");
        }
        TopologicalCell cell = (TopologicalCell) element;
        Collection<Border> borders = getAffectedBorders();

        Rectangle rect = new Rectangle(cell.getBounds()); // TODO any polygon
        Edge edge = rect.getEdge(side);

        double length = 0;
        BorderPart closestBorder = null;
        for (Border border : borders) {
            Optional<BorderPart> closest = border.without(grid.getSymbolsOfClass(Well.class)).getClosestPartWithConstraints(edge, cell.getSymbol(), direction);
            if (closest.isPresent()) {
                BorderPart closestBorderPart = closest.get();
                for (Point coordinate : edge.getPoints()) {
                    double l = border.getMoveDistance(closestBorderPart, cell.getSymbol(), direction, coordinate);
                    if (l < length || length == 0) {
                        length = l;
                        closestBorder = closestBorderPart;
                    }
                }
            }
        }

        move(cell, length, getBordersToUpdate());
        // try to move bulks now ?

        return true;
    }

    private Collection<Border> getBordersToUpdate() {
        List<Border> result = new ArrayList<>();
        for (Map<Direction, Border> borderMap : borders.values()) {
            for (Direction dir : borderMap.keySet()) {
                if (dir.isSameOrientation(direction)) {
                    result.add(borderMap.get(dir));
                }
            }
        }
        return result; // todo
    }
}
