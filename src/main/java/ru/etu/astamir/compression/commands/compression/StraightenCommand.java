package ru.etu.astamir.compression.commands.compression;

import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.CompressionUtils;
import ru.etu.astamir.compression.commands.ImitateCommand;
import ru.etu.astamir.compression.grid.Grid;
import ru.etu.astamir.compression.grid.VirtualGrid;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.gui.common.WindowUtils;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.TopologyLayer;
import ru.etu.astamir.model.exceptions.UnexpectedException;
import ru.etu.astamir.model.wires.Wire;
import ru.etu.astamir.model.wires.WireUtils;

import java.util.Map;

/**
 * @author Artem Mon'ko
 */
public class StraightenCommand extends ImitateCommand {
    Grid grid;
    public StraightenCommand(Wire source, Border border, Direction direction, Grid grid) {
        super(source, border, direction);
        this.grid = grid;
    }

    @Override
    protected boolean imitate(boolean allow_deformation) {
        return WireUtils.straighten(source, CompressionUtils.borderWithoutConnectedElements(source, border), direction, grid);
    }
}
