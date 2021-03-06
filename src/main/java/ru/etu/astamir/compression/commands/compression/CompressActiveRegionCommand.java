package ru.etu.astamir.compression.commands.compression;

import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.grid.VirtualGrid;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.model.TopologyLayer;

import java.util.Map;

/**
 * Created by amonko on 20/07/16.
 */
public class CompressActiveRegionCommand extends CompressContourCommand {
    public CompressActiveRegionCommand(VirtualGrid grid, Map<TopologyLayer, Map<Direction, Border>> borders, String elementName, Direction direction, Direction side) {
        super(grid, borders, elementName, direction, side);
    }

    @Override
    public boolean execute() {
        return super.execute();
    }
}
