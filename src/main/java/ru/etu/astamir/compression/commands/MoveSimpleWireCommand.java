package ru.etu.astamir.compression.commands;

import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.wires.SimpleWire;

/**
 * @author Artem Mon'ko
 */
public class MoveSimpleWireCommand extends MoveCommand {
    private SimpleWire simple_wire;
    private Direction direction;
    private double length;

    public MoveSimpleWireCommand(SimpleWire element, Direction direction, double length) {
        super(element, direction, length);
        this.simple_wire = element;
        this.direction = direction;
        this.length = length;
    }

    @Override
    public boolean execute() {
        if (simple_wire.getWire() != null) {
            return simple_wire.getWire().movePart(simple_wire, direction, length);
        }

        return super.execute();
    }

    @Override
    public boolean unexecute() {
        return super.unexecute();
    }
}
