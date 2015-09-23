package ru.etu.astamir.compression.commands;

import ru.etu.astamir.model.regions.Contour;

/**
 * @author Artem Mon'ko
 */
public class MoveContourSideCommand implements Command {
    Contour source;

    public MoveContourSideCommand() {
    }

    @Override
    public boolean execute() {
        return false;
    }

    @Override
    public boolean unexecute() {
        return false;
    }
}
