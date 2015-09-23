package ru.etu.astamir.compression.commands;

import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.BorderPart;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.model.TopologyElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Artem Mon'ko
 */
public class UpdateBorderCommand implements Command {
    private Collection<Border> borders;
    private TopologyElement element;
    private Direction direction;
    private List<BorderPart> partsToAdd = new ArrayList<>();

    public UpdateBorderCommand(Collection<Border> bordersToUpdate, TopologyElement changedElement, Direction direction) {
        this.borders = bordersToUpdate;
        this.element = changedElement;
        this.direction = direction;
    }

    @Override
    public boolean execute() {
        partsToAdd = BorderPart.of(element, direction);
        for (Border border : borders) {
            border.addParts(partsToAdd);
        }

        return true;
    }

    @Override
    public boolean unexecute() {
        for (Border border : borders) {
            border.getParts().removeAll(partsToAdd);
        }

        partsToAdd.clear();
        return true;
    }
}
