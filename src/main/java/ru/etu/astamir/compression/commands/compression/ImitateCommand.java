package ru.etu.astamir.compression.commands.compression;

import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.commands.Command;
import ru.etu.astamir.compression.grid.Grid;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.Flap;
import ru.etu.astamir.model.wires.SimpleWire;
import ru.etu.astamir.model.wires.Wire;
import ru.etu.astamir.model.wires.WireUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Artem Mon'ko
 */
// TODO make it compress command
public class ImitateCommand implements Command {
    protected Wire source;
    protected Border border;
    protected Direction direction;
    protected boolean allow_deformation;
    protected Grid grid;

    private List<SimpleWire> old_parts = new ArrayList<>();

    public ImitateCommand(Wire source, Border border, Direction direction, Grid grid) {
        this(source, border, direction, true, grid);
    }

    public ImitateCommand(Wire source, Border border, Direction direction, boolean allow_deformation, Grid grid) {
        this.source = source;
        this.border = border;
        this.direction = direction;
        this.allow_deformation = allow_deformation;
        this.grid = grid;
    }

    @Override
    public boolean execute() {
        old_parts.clear();
        old_parts.addAll(source.getParts());
        return imitate(allow_deformation);
    }

    protected boolean imitate(boolean allow_deformation) {
        border.imitate(source, direction, allow_deformation, grid);
        return true;
    }

    @Override
    public boolean unexecute() {
        source.setParts(old_parts);
        return true;
    }
}
