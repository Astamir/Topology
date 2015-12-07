package ru.etu.astamir.compression.commands.compression;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import ru.etu.astamir.common.Pair;
import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.commands.Command;
import ru.etu.astamir.compression.commands.UpdateBorderCommand;
import ru.etu.astamir.compression.grid.VirtualGrid;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.Flap;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.TopologyLayer;
import ru.etu.astamir.model.connectors.ConnectionPoint;
import ru.etu.astamir.model.connectors.SimpleConnectionPoint;
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
    UpdateBorderCommand update_border;
    Wire wire;
    List<Pair<SimpleWire, Point>> connected_wires_to_stretch = Lists.newArrayList();
    Map<SimpleWire, Flap> connected_to_flaps = new HashMap<>();
    boolean gate_deformation_allowed = true;

    public CompressWireCommand(VirtualGrid grid, Map<TopologyLayer, Map<Direction, Border>> borders, String element_name, Direction direction) {
        super(grid, borders, element_name, direction);
    }

    public void setGateDeformationAllowed(boolean gate_deformation_allowed) {
        this.gate_deformation_allowed = gate_deformation_allowed;
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
        update_border = new UpdateBorderCommand(affectedBorders, wire, direction);
        update_border.execute();

        return false;
    }

    protected Command createImitateCommand(Wire wire, Border overlay) {
        return wire instanceof Gate ? new ImitateGateCommand((Gate) wire, overlay, direction, gate_deformation_allowed, grid) : new ImitateCommand(wire, overlay, direction, grid);
    }

    protected void moveConnected() {
        for (Pair<SimpleWire, Point> wire_pair : connected_wires_to_stretch) {
            if (connected_to_flaps.containsKey(wire_pair.left)) {
                Flap flap = connected_to_flaps.get(wire_pair.left);
                Point working_point = flap.getCenter().getStart();
                double distance = Point.distance(working_point, wire_pair.right);

                wire_pair.left.getWire().stretchOnly(wire_pair.left, wire_pair.right, direction, distance);
            }
        }
    }

    /**
     * Подготовка соединенных элементов для последующего перемещения.
     */
    protected void handleConnections() {
        Set<String> connected_names = new HashSet<>();
        for (ConnectionPoint connection_point : wire.getConnections()) {
            if (connection_point instanceof SimpleConnectionPoint) {
                connected_names.addAll(connection_point.getConnectedNames());
            } else {
                connected_names.add(connection_point.getName());
            }
        }

        final Collection<TopologyElement> connected_elements = grid.toElements(connected_names, TopologyElement.class);
        for (TopologyElement connected_element : connected_elements) {
            if (connected_element instanceof Contact) {
                handleConnectedContact((Contact) connected_element);
            } else if (connected_element instanceof Wire) {
                handleConnectedWire((Wire) connected_element);
            }
        }
    }

    void handleConnectedContact(Contact connected_contact) {
        if (connected_contact.getType() == ContactType.FLAP) {
            if (wire instanceof Gate) {
                // move flap
            } else {
                // connect wire with flap with empty link or move connect it later with actual wire
            }
        } else {
            // move or connect
        }
    }

    void handleConnectedWire(Wire connected_wire) {
        if (wire instanceof Gate) {
            handleConnectedWireAndGate((Gate) wire, connected_wire);
        } else if (connected_wire instanceof Gate) {
            handleConnectedWireAndGate((Gate) connected_wire, wire);
        }
    }

    void handleConnectedWireAndGate(Gate gate, Wire wire) {
        Optional<Point> on_flap = gate.findConnectionPointOnFlaps(wire);
        if (on_flap.isPresent()) {
            // So our wire connected to gate's flap. We have to figure out length to to stretch by the change of the correspondent flap's position
            Optional<SimpleWire> found_part = wire.findPartWithPoint(on_flap.get());
            if (!found_part.isPresent()) {
                throw new UnexpectedException("we found connection point but not the part ??");
            }

            SimpleWire part = found_part.get();

            Flap flap = gate.getFlap(on_flap.get());
            if (!part.isLink() && part.getAxis().getOrientation().isOrthogonal(direction.toOrientation())) {
                // we have to connect them
                part = WireUtils.connect(wire, flap).iterator().next();
            }
            connected_to_flaps.put(part, flap);
            connected_wires_to_stretch.add(Pair.of(part, on_flap.get()));
        }
        // wire could also be connected to the gate directly
        final Optional<Point> connection_point = WireUtils.getConnectionPoint(gate, wire);
        // todo figure out how to track connection point
    }

    @Override
    public boolean unexecute() {
        return false;
    }

    @Override
    public String toString() {
        if (wire == null) {
            return "CompressWireCommand with null element" + element_name;
        }
        return "Moving " + wire.getClass().getSimpleName() +"[" + wire.getSymbol()+"], deformation = " + gate_deformation_allowed;
    }
}
