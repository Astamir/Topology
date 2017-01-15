package ru.etu.astamir.compression.commands;

import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.BorderPart;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.model.TopologyLayer;
import ru.etu.astamir.model.contacts.Contact;
import ru.etu.astamir.model.contacts.Pin;
import ru.etu.astamir.model.regions.ContactWindow;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by amonko on 11/04/16.
 */
public class UpdateBorderWithPinCommand implements Command {
    private Collection<Border> borders;
    private Pin pin;
    private Direction direction;
    private Map<TopologyLayer, Collection<BorderPart>> partsToAdd = new HashMap<>();

    public UpdateBorderWithPinCommand(Collection<Border> bordersToUpdate, Pin changedElement, Direction direction) {
        this.borders = bordersToUpdate;
        this.pin = changedElement;
        this.direction = direction;
    }

    @Override
    public boolean execute() {
        for (ContactWindow window : pin.getContactWindows().values()) {
            partsToAdd.put(window.getLayer(), BorderPart.of(window, Direction.UNDETERMINED));
        }

        for (Border border : borders) {
            Collection<BorderPart> borderParts = partsToAdd.get(border.getLayer());
            border.addParts(borderParts != null ? borderParts : Collections.emptyList());
        }

        return true;
    }

    @Override
    public boolean unexecute() {
        for (Border border : borders) {
            Collection<BorderPart> borderParts = partsToAdd.get(border.getLayer());
            if (borderParts != null) {
                border.getParts().removeAll(borderParts);
            }
        }

        partsToAdd.clear();
        return true;
    }
}
