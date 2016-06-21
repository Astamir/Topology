package ru.etu.astamir.compression.commands;

import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.BorderPart;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.model.TopologyLayer;
import ru.etu.astamir.model.contacts.Contact;
import ru.etu.astamir.model.regions.ContactWindow;

import java.util.*;

/**
 * Created by amonko on 11/04/16.
 */
public class UpdateBorderWithContactCommand implements Command {
    private Collection<Border> borders;
    private Contact contact;
    private Direction direction;
    private Map<TopologyLayer, Collection<BorderPart>> partsToAdd = new HashMap<>();

    public UpdateBorderWithContactCommand(Collection<Border> bordersToUpdate, Contact changedElement, Direction direction) {
        this.borders = bordersToUpdate;
        this.contact = changedElement;
        this.direction = direction;
    }

    @Override
    public boolean execute() {
        for (ContactWindow window : contact.getContactWindows().values()) {
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
