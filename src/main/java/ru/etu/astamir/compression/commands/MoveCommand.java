package ru.etu.astamir.compression.commands;

import ru.etu.astamir.common.Utils;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.GeomUtils;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.Movable;
import ru.etu.astamir.common.Pair;

import javax.rmi.CORBA.Util;
import java.util.Collection;
import java.util.Collections;

/**
 * Created with IntelliJ IDEA.
 * User: astamir
 * Date: 06.05.12
 * Time: 15:39
 * To change this template use File | Settings | File Templates.
 */
public class MoveCommand implements DescribableCommand {
    /**
     * Это элемент, который мы двигаем.
     */
    protected TopologyElement source;

    protected double dx = 0.0;
    protected double dy = 0.0;

    protected Collection<Point> old_coordinates = Collections.emptyList();
    
    private MoveCommand(TopologyElement element, double dx, double dy) {
        this.source = element;
        this.dx = dx;
        this.dy = dy;
    }

    private MoveCommand(TopologyElement element, Pair<Double, Double> dPair) {
        this(element, dPair.left, dPair.right);
    }

    public MoveCommand(TopologyElement element, Direction direction, double length) {
        this(element, toDxDy(direction, length));
    }

    public static Pair<Double, Double> toDxDy(Direction direction, double length) {
        double signedD = length * direction.getDirectionSign();
        if (direction.isLeftOrRight()) {
            return Pair.of(signedD, 0.0);
        } else {
            return Pair.of(0.0, signedD);
        }
    }
    
    public TopologyElement getSource() {
        return source;
    }

    @Override
    public boolean execute() {
        if (source instanceof Movable) {
            return ((Movable) source).move(dx, dy);
        }

        old_coordinates = source.getCoordinates();
        source.setCoordinates(GeomUtils.prepareToMove(old_coordinates, dx, dy));
        return true;
    }

    @Override
    public boolean unexecute() {
        if (source instanceof Movable) {
            return ((Movable) source).move(-dx, -dy);
        }
        source.setCoordinates(old_coordinates);
        return true;
    }

    @Override
    public String toString() {
        return getDescription();
    }

    @Override
    public String getDescription() {
        StringBuilder description = new StringBuilder();
        description.append("Moving ").append(source.getClass().getSimpleName());
        description.append("[").append(source.getSymbol()).append("-").append(source.getName()).append("]:");
        description.append(" dx = ").append(dx).append(", dy = ").append(dy);
        return description.toString();
    }
}
