package ru.etu.astamir.compression.commands.compression;

import ru.etu.astamir.common.Utils;
import ru.etu.astamir.common.collections.EntitySet;
import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.CompressionUtils;
import ru.etu.astamir.compression.commands.MoveCommand;
import ru.etu.astamir.compression.commands.UpdateBorderWithContactCommand;
import ru.etu.astamir.compression.grid.VirtualGrid;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.Material;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.TopologyLayer;
import ru.etu.astamir.model.connectors.ConnectionUtils;
import ru.etu.astamir.model.contacts.Contact;
import ru.etu.astamir.model.exceptions.UnexpectedException;
import ru.etu.astamir.model.regions.ActiveRegion;
import ru.etu.astamir.model.regions.ContactWindow;
import ru.etu.astamir.model.wires.SimpleWire;
import ru.etu.astamir.model.wires.Wire;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Astamir on 11.01.2015.
 */
public class CompressContactCommand extends CompressCommand {
    private static final Predicate<Contact> DIAGONAL_CONTACTS_ANALYSIS = input -> true; // TODO

    private MoveCommand move;
    private UpdateBorderWithContactCommand updateBorder;

    public CompressContactCommand(VirtualGrid grid, Map<TopologyLayer, Map<Direction, Border>> borders, Direction direction, String elementName) {
        super(grid, borders, elementName, direction);
    }

    @Override
    public boolean execute() {
        TopologyElement element = getElement();
        if (!(element instanceof Contact)) {
            throw new UnexpectedException("Given element={" + element +"} is not a contact, but passed to a contact processing command");
        }

        Contact contact = (Contact) element;

        Collection<Border> affectedBorders = getAffectedBorders();

        // first we have to figure out our moving distance
        ActiveBorder length = getContactMoveDistance(contact, affectedBorders, direction);

        // find out if some other contacts or contours are in the way
        //length = diagonalAnalysis(length, contact, grid.getElementsOfType(Contact.class));

        // find and move connected wires;
        moveConnected(contact, length.getLength());
        move(contact, length.getLength(), affectedBorders);

        return true;
    }

    void moveConnected(Contact contact, double length) {
        Edge center = contact.getCenter();
        if (!center.isPoint()) {
            return; // todo implement for long contacts
        }

        Point connectionPoint = center.getStart();
        // find all connected wires
        Collection<Wire> connectedWires = grid.toElements(contact.getConnectedNames(), Wire.class);
        for (Wire wire : connectedWires) {
            // create an empty link at the connection point if we don't have one already
            Optional<SimpleWire> partO = wire.findPartWithPoint(connectionPoint);
            if (partO.isPresent()) {
                SimpleWire part = partO.get();
                Edge axis = part.getAxis();
                if (axis.isPoint() || axis.getOrientation().equals(direction.toOrientation())) {
                    // just stretch
                    wire.stretchOnly(part, connectionPoint, direction, length);
                } else {
                    // create empty link
                    SimpleWire link;
                    Optional<SimpleWire> l = wire.findLink(connectionPoint);
                    if (!l.isPresent()) {
                        link = wire.addEmptyLinkToPart(part, connectionPoint);
                    } else {
                        link = l.get();
                    }
                    wire.stretchOnly(link, connectionPoint, direction, length);
                }
            }
        }
    }

    void move(Contact contact, double length, Collection<Border> borders) {
        move = new MoveCommand(contact, direction, length);
        move.execute();

        updateBorder = new UpdateBorderWithContactCommand(borders, contact, direction);
        updateBorder.execute();
    }

    private double diagonalAnalysis(double moveLength, Contact contact, Collection<Contact> otherContacts) {
        EntitySet<Contact> contactSet = EntitySet.clone(otherContacts.stream().filter(DIAGONAL_CONTACTS_ANALYSIS).collect(Collectors.toList()));
        if (contactSet.contains(contact)) {
            contactSet.remove(contact);
        }

        double length = -1D;
        for (Contact c : contactSet) {
            double diagonal = diagonalDistance(contact.getCoordinates(), c.getCoordinates());
            length = Utils.assignIfSmaller(length, diagonal, -1D);
        }

        return Utils.assignIfSmaller(moveLength, length, -1D);
    }

    private double diagonalDistance(Collection<Point> one, Collection<Point> another) {
        double diagonal = Utils.LENGTH_NAN;
        for (Point onePoint : one) {
            for (Point anotherPoint : another) {
                diagonal = Utils.assignIfSmaller(diagonal, Point.distance(onePoint, anotherPoint));
            }
        }
        return diagonal;
    }

    private ActiveBorder getContactMoveDistance(Contact contact, Collection<Border> borders, Direction direction) {
        ActiveBorder length = ActiveBorder.NAN;
        boolean insideActiveRegion = !ConnectionUtils.isContainedIn(grid, contact, ActiveRegion.class).isEmpty(); // todo different distances might be
        for (Border border : borders) {
            Border working = CompressionUtils.borderWithoutConnectedElements(contact, border, grid);

            TopologyLayer borderLayer = border.getLayer();
            working.setLayer(borderLayer);

            ActiveBorder movingLength = CompressionUtils.getMovingLength(contact, direction, working);

            length = Utils.assignIfSmaller(length, movingLength); // moving length from the center of contact

            Map<Material, ContactWindow> contactWindows = contact.getContactWindows();
            if (contactWindows.isEmpty()) {
                throw new UnexpectedException("There are no contact windows in contact " + contact);
            }
            // todo look through
            for (Map.Entry<Material, ContactWindow> windowEntry : contactWindows.entrySet()) {
                ContactWindow window = windowEntry.getValue();
                if (borderLayer != null && !borderLayer.equals(window.getLayer())) { // if border has different layer from contact window we skip it
                    continue;
                }

                // todo check for certain elements in the border who might be ignored inspite of being in the same layer.
                length = Utils.assignIfSmaller(length, CompressionUtils.getMovingLength(window, direction, working));
            }
        }
        System.out.println(length);

        return length;
    }

    @Override
    public boolean unexecute() {
        if (move != null && updateBorder != null) {
            return updateBorder.unexecute() & move.unexecute();
        }

        return false;
    }

    @Override
    public String toString() {
        return move != null ? move.toString() : "Moving contact error, move command is null";
    }
}
