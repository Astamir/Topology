package ru.etu.astamir.compression.commands.compression;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.BorderPart;
import ru.etu.astamir.compression.commands.Command;
import ru.etu.astamir.compression.commands.MoveCommand;
import ru.etu.astamir.compression.commands.UpdateBorderCommand;
import ru.etu.astamir.compression.grid.VirtualGrid;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Orientation;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.TopologyLayer;
import ru.etu.astamir.model.exceptions.UnexpectedException;
import ru.etu.astamir.model.wires.Wire;

import java.util.*;

/**
 * Created by astamir on 8/30/15.
 */
public class StraightenWireCommand extends CompressWireCommand {
    public StraightenWireCommand(VirtualGrid grid, Map<TopologyLayer, Map<Direction, Border>> borders, String element_name, Direction direction) {
        super(grid, borders, element_name, direction);
    }

    @Override
    protected Command createImitateCommand(Wire wire, Border overlay) {
        return new StraightenCommand(wire, overlay, direction, grid);
    }

    @Override
    public Collection<Border> getAffectedBorders() {
        List<Border> result = new ArrayList<>();
        Map<Direction, Border> layer_border = borders.get(getElement().getLayer());
        for (Direction dir : layer_border.keySet()) {
            if (dir.isSameOrientation(direction)) {
                result.add(layer_border.get(dir));
            }
        }
        return result; // todo
    }

    @Override
    public String toString() {
        return "Straighten: " + getElement().getClass().getSimpleName() + "[" + getElement().getSymbol() + "-" + element_name + "]";
    }
}
