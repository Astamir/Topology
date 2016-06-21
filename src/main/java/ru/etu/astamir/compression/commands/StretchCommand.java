package ru.etu.astamir.compression.commands;

import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.Stretchable;
import ru.etu.astamir.model.TopologyElement;

/**
 * @author Artem Mon'ko
 */
public class StretchCommand<T extends TopologyElement & Stretchable> implements DescribableCommand {

    /**
     * Target element for stretching
     */
    private T source;

    private Point stretchPoint;
    private Direction direction;
    private double length;

    private StretchCommand(T element, Point stretchPoint, Direction direction, double length) {
        this.source = element;
        this.stretchPoint = stretchPoint;
        this.direction = direction;
        this.length = length;
    }

    public TopologyElement getSource() {
        return source;
    }

    @Override
    public boolean execute() {
        return source.stretch(direction, length, stretchPoint);
    }

    @Override
    public boolean unexecute() {
        return source.stretch(direction.opposite(), length, stretchPoint);
    }

    @Override
    public String toString() {
        return getDescription();
    }

    @Override
    public String getDescription() {
        StringBuilder description = new StringBuilder();
        description.append("Stretching ").append(source.getClass().getSimpleName());
        description.append("[").append(source.getSymbol()).append("-").append(source.getName()).append("]:");
        description.append(" source = ").append(source.getAxis()).append(", point = ").append(stretchPoint).append(", dir = ").append(direction).append(", len = ").append(length);
        return description.toString();
    }
}
