package ru.etu.astamir.compression.commands;

import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.wires.Wire;

/**
 * Created with IntelliJ IDEA.
 * User: astamir
 * Date: 06.05.12
 * Time: 15:40
 * To change this template use File | Settings | File Templates.
 */
public class DeformCommand implements Command{
    TopologyElement invoker;
    Wire target;

    Point deformationPoint;
    Direction direction;
    Direction half;
    double d;


    @Override
    public boolean execute() {
        return false;
    }

    @Override
    public boolean unexecute() {
        return false;
    }
}
