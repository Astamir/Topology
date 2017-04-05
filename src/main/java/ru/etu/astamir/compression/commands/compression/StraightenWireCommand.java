package ru.etu.astamir.compression.commands.compression;

import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.commands.Command;
import ru.etu.astamir.compression.grid.VirtualGrid;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.model.TopologyLayer;
import ru.etu.astamir.model.wires.Wire;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by astamir on 8/30/15.
 */
public class StraightenWireCommand extends CompressWireCommand {
    public StraightenWireCommand(VirtualGrid grid, Map<TopologyLayer, Map<Direction, Border>> borders, String elementName, Direction direction) {
        super(grid, borders, elementName, direction);
    }

    @Override
    protected Command createImitateCommand(Wire wire, Border overlay) {
        return new StraightenCommand(wire, overlay, direction, grid);
    }

    @Override
    public Collection<Border> getAffectedBorders() {
        List<Border> result = new ArrayList<>();
        Map<Direction, Border> layerBorder = borders.get(getElement().getLayer());
        for (Direction dir : layerBorder.keySet()) {
            if (dir.isSameOrientation(direction)) {
                result.add(layerBorder.get(dir));
            }
        }
        return result; // todo
    }

    @Override
    public String toString() {
        return "Straighten: " + getElement().getClass().getSimpleName() + "[" + getElement().getSymbol() + "-" + elementName + "]";
    }
}
