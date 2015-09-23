package ru.etu.astamir.compression.commands;

import ru.etu.astamir.compression.Border;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.Flap;
import ru.etu.astamir.model.wires.SimpleWire;
import ru.etu.astamir.model.wires.Wire;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Artem Mon'ko
 */
public class ImitateCommand implements Command {
    protected Wire source;
    protected Border border;
    protected Direction direction;
    protected boolean allow_deformation;

    private List<SimpleWire> old_parts = new ArrayList<>();

    public ImitateCommand(Wire source, Border border, Direction direction) {
        this(source, border, direction, true);
    }

    public ImitateCommand(Wire source, Border border, Direction direction, boolean allow_deformation) {
        this.source = source;
        this.border = border;
        this.direction = direction;
        this.allow_deformation = allow_deformation;
    }

    @Override
    public boolean execute() {
        old_parts.clear();
        old_parts.addAll(source.getParts());
        return imitate(allow_deformation);
    }

    protected boolean imitate(boolean allow_deformation) {
        border.imitate(source, direction, allow_deformation);
        return true;
    }

    @Override
    public boolean unexecute() {
        source.setParts(old_parts);
        return true;
    }
}
