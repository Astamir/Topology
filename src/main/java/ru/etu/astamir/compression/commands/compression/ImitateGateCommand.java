package ru.etu.astamir.compression.commands.compression;

import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.grid.Grid;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.model.wires.Gate;

/**
 * @author Artem Mon'ko
 */
public class ImitateGateCommand extends ImitateCommand {
    public ImitateGateCommand(Gate source, Border border, Direction direction, boolean allowDeformation, Grid grid) {
        super(source, border, direction, allowDeformation, grid);
    }

    @Override
    public boolean execute() {
        boolean success = super.execute();
        ((Gate)source).ensureFlapsCoordinates();
        return success;
    }

    @Override
    public boolean unexecute() {
        boolean success = super.unexecute();
        ((Gate) source).ensureFlapsCoordinates();
        return success;
    }
}
