package ru.etu.astamir.compression.commands;

import ru.etu.astamir.common.Utils;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Rectangle;
import ru.etu.astamir.model.regions.Contour;

/**
 * Created by Astamir on 13.12.2014.
 */
public class MoveContourCommand implements Command {
    private Contour contour;
    private double length;
    private Direction direction;
    private Direction side;

    public MoveContourCommand(Contour contour, double length, Direction direction, Direction side) {
        this.contour = contour;
        this.length = length;
        this.direction = direction;
        this.side = side;
    }

    @Override
    public boolean execute() {
        Rectangle rect = new Rectangle(contour.getBounds());
        rect.moveEdge(rect.getEdge(side), direction, length);
        contour.setContour(rect);
        return true;
    }

    @Override
    public boolean unexecute() {
        return false;
    }

    @Override
    public String toString() {
        return "Moving the " + side + " side of the " + contour.getClass().getSimpleName() + "[" + contour.getSymbol() + "] to the " + direction + " by " + length + "pt";
    }
}
