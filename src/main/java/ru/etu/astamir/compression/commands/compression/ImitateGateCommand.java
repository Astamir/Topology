package ru.etu.astamir.compression.commands.compression;

import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.commands.ImitateCommand;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.model.wires.Gate;
import ru.etu.astamir.model.wires.Wire;

/**
 * @author Artem Mon'ko
 */
public class ImitateGateCommand extends ImitateCommand {
    public ImitateGateCommand(Gate source, Border border, Direction direction, boolean allow_deformation) {
        super(source, border, direction, allow_deformation);
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
