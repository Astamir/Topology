package ru.etu.astamir.compression.commands.compression;

import com.google.common.collect.Lists;
import ru.etu.astamir.common.Pair;
import ru.etu.astamir.common.collections.EntitySet;
import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.commands.Command;
import ru.etu.astamir.compression.commands.UpdateBorderCommand;
import ru.etu.astamir.compression.grid.VirtualGrid;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.Flap;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.TopologyLayer;
import ru.etu.astamir.model.connectors.ConnectionUtils;
import ru.etu.astamir.model.contacts.Contact;
import ru.etu.astamir.model.contacts.ContactType;
import ru.etu.astamir.model.exceptions.UnexpectedException;
import ru.etu.astamir.model.wires.Gate;
import ru.etu.astamir.model.wires.SimpleWire;
import ru.etu.astamir.model.wires.Wire;
import ru.etu.astamir.model.wires.WireUtils;

import java.util.*;

/**
 * Created by Astamir on 13.01.2015.
 */
public class CompressWireCommand extends CompressCommand {
    Command imitate;
    UpdateBorderCommand updateBorder;
    Wire wire;
    List<Pair<SimpleWire, Point>> connectedWiresToStretch = Lists.newArrayList();
    Map<SimpleWire, Flap> connectedToFlaps = new HashMap<>();
    boolean gateDeformationAllowed = true;

    public CompressWireCommand(VirtualGrid grid, Map<TopologyLayer, Map<Direction, Border>> borders, String elementName, Direction direction) {
        super(grid, borders, elementName, direction);
    }

    public void setGateDeformationAllowed(boolean gateDeformationAllowed) {
        this.gateDeformationAllowed = gateDeformationAllowed;
    }

    @Override
    public boolean execute() {
        TopologyElement element = getElement();
        if (!(element instanceof Wire)) {
            throw new UnexpectedException("Given element={" + element +"} is not a wire, but passed to a wire processing command");
        }

        wire = (Wire) element;

        Collection<Border> affectedBorders = getAffectedBorders();

        handleConnections();
        imitate = createImitateCommand(wire, affectedBorders.iterator().next());
        imitate.execute();
        moveConnected();
        updateBorder = new UpdateBorderCommand(affectedBorders, wire, direction);
        updateBorder.execute();

        return false;
    }

    protected Command createImitateCommand(Wire wire, Border overlay) {
        return wire instanceof Gate ? new ImitateGateCommand((Gate) wire, overlay, direction, gateDeformationAllowed, grid) : new ImitateCommand(wire, overlay, direction, grid);
    }

    protected void moveConnected() {
        for (Pair<SimpleWire, Point> wirePair : connectedWiresToStretch) {
            if (connectedToFlaps.containsKey(wirePair.left)) {
                Flap flap = connectedToFlaps.get(wirePair.left);
                Point workingPoint = flap.getCenter().getStart();
                double distance = Point.distance(workingPoint, wirePair.right);

                wirePair.left.getWire().stretchOnly(wirePair.left, wirePair.right, direction, distance);
            }
        }
    }

    /**
     * Подготовка соединенных элементов для последующего перемещения.
     */
    protected void handleConnections() {
        EntitySet<TopologyElement> connectedElements = ConnectionUtils.getConnectedElementsForWire(wire, grid);
        for (TopologyElement connectedElement : connectedElements) {
            if (connectedElement instanceof Contact) {
                handleConnectedContact((Contact) connectedElement);
            } else if (connectedElement instanceof Wire) {
                handleConnectedWire((Wire) connectedElement);
            }
        }
    }

    void handleConnectedContact(Contact connectedContact) {
        if (connectedContact.getType() == ContactType.FLAP) {
            if (wire instanceof Gate) {
                // move flap
            } else {
                // connect wire with flap with empty link or move connect it later with actual wire
            }
        } else {
            // move or connect
        }
    }

    void handleConnectedWire(Wire connectedWire) {
        if (wire instanceof Gate) {
            handleConnectedWireAndGate((Gate) wire, connectedWire);
        } else if (connectedWire instanceof Gate) {
            handleConnectedWireAndGate((Gate) connectedWire, wire);
        }
    }

    void handleConnectedWireAndGate(Gate gate, Wire wire) {
        Optional<Point> onFlaps = gate.findConnectionPointOnFlaps(wire);
        if (onFlaps.isPresent()) {
            // So our wire connected to gate's flap. We have to figure out length to to stretch by the change of the correspondent flap's position
            Optional<SimpleWire> foundPart = wire.findPartWithPoint(onFlaps.get());
            if (!foundPart.isPresent()) {
                throw new UnexpectedException("we found connection point but not the part ??");
            }

            SimpleWire part = foundPart.get();

            Flap flap = gate.getFlap(onFlaps.get());
            if (!part.isLink() && part.getAxis().getOrientation().isOrthogonal(direction.toOrientation())) {
                // we have to connect them
                part = WireUtils.connect(wire, flap).iterator().next();
            }
            connectedToFlaps.put(part, flap);
            connectedWiresToStretch.add(Pair.of(part, onFlaps.get()));
        }
        // wire could also be connected to the gate directly
        final Optional<Point> connectionPoint = WireUtils.getConnectionPoint(gate, wire);
        // todo figure out how to track connection point
    }

    @Override
    public boolean unexecute() {
        return false;
    }

    @Override
    public String toString() {
        if (wire == null) {
            return "CompressWireCommand with null element" + elementName;
        }
        return "Moving " + wire.getClass().getSimpleName() +"[" + wire.getSymbol()+"], deformation = " + gateDeformationAllowed;
    }
}
