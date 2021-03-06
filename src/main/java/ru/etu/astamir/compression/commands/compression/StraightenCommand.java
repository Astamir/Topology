package ru.etu.astamir.compression.commands.compression;

import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.CompressionUtils;
import ru.etu.astamir.compression.grid.Grid;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.model.wires.Wire;
import ru.etu.astamir.model.wires.WireUtils;

/**
 * @author Artem Mon'ko
 */
public class StraightenCommand extends ImitateCommand {
    public StraightenCommand(Wire source, Border border, Direction direction, Grid grid) {
        super(source, border, direction, grid);
    }

    @Override
    protected boolean imitate(boolean allowDeformation) {
        return WireUtils.straighten(source, CompressionUtils.borderWithoutConnectedElements(source, border, grid), direction, grid);
    }
}
