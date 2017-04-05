package ru.etu.astamir.compression.commands.compression;

import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.commands.Command;
import ru.etu.astamir.compression.grid.Grid;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.model.wires.SimpleWire;
import ru.etu.astamir.model.wires.Wire;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Artem Mon'ko
 */
// TODO make it compress command
public class ImitateCommand implements Command {
    protected Wire source;
    protected Border border;
    protected Direction direction;
    protected boolean allowDeformation;
    protected Grid grid;

    private List<SimpleWire> oldParts = new ArrayList<>();

    public ImitateCommand(Wire source, Border border, Direction direction, Grid grid) {
        this(source, border, direction, true, grid);
    }

    public ImitateCommand(Wire source, Border border, Direction direction, boolean allowDeformation, Grid grid) {
        this.source = source;
        this.border = border;
        this.direction = direction;
        this.allowDeformation = allowDeformation;
        this.grid = grid;
    }

    @Override
    public boolean execute() {
        oldParts.clear();
        oldParts.addAll(source.getParts());
        return imitate(allowDeformation);
    }

    protected boolean imitate(boolean allowDeformation) {
        border.imitate(source, direction, allowDeformation, grid);
        return true;
    }

    @Override
    public boolean unexecute() {
        source.setParts(oldParts);
        return true;
    }
}
