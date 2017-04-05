package ru.etu.astamir.compression.commands;

import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.model.wires.SimpleWire;

/**
 * @author Artem Mon'ko
 */
public class MoveSimpleWireCommand extends MoveCommand {
    private SimpleWire simpleWire;
    private Direction direction;
    private double length;

    public MoveSimpleWireCommand(SimpleWire element, Direction direction, double length) {
        super(element, direction, length);
        this.simpleWire = element;
        this.direction = direction;
        this.length = length;
    }

    @Override
    public boolean execute() {
        if (simpleWire.getWire() != null) {
            return simpleWire.getWire().movePart(simpleWire, direction, length);
        }

        return super.execute();
    }

    @Override
    public boolean unexecute() {
        return super.unexecute();
    }
}
