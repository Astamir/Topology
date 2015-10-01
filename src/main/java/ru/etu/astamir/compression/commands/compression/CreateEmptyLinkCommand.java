package ru.etu.astamir.compression.commands.compression;

import ru.etu.astamir.compression.commands.Command;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.wires.SimpleWire;
import ru.etu.astamir.model.wires.Wire;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Artem Mon'ko
 */
public class CreateEmptyLinkCommand implements Command{
    private Wire source;
    private Point linkPoint;
    private Direction direction;
    private List<SimpleWire> oldParts = new ArrayList<>();

    public CreateEmptyLinkCommand(Wire source, Point linkPoint, Direction direction) {
        this.source = source;
        this.linkPoint = linkPoint;
        this.direction = direction;
    }

    @Override
    public boolean execute() {
        oldParts.clear();
        oldParts.addAll(source.getParts());
        return !source.createAnEmptyLink(linkPoint, direction).isEmpty();
    }

    @Override
    public boolean unexecute() {
        source.setParts(oldParts);
        oldParts.clear();
        return true;
    }
}
