package ru.etu.astamir.model.wires;

import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Orientation;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.Flap;
import ru.etu.astamir.model.Material;
import ru.etu.astamir.model.TopologyLayer;
import ru.etu.astamir.model.exceptions.UnexpectedException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Затвор транзистора. По идее ничем от шины не отличается, только разве что более жесткими ограничениями.
 */
public class Gate extends Wire {
    private Map<Flap.Position, Flap> flaps = new HashMap<>(2);

    public Gate(String name, Orientation orientation) {
        super(name, orientation);
    }

    public Gate(Orientation orientation) {
        super();
        this.orientation = orientation;
        createFlaps();
    }

    public Flap getFlap(Flap.Position position) {
        return flaps.get(position);
    }

    public void setFlaps(Map<Flap.Position, Flap> flaps) {
        this.flaps = flaps;
        for (Flap flap : flaps.values()) {
            flap.setLayer(getLayer());
            flap.setMaterial(getMaterial());
        }
        ensureFlapsCoordinates();
    }

    @Override
    public void setLayer(TopologyLayer layer) {
        super.setLayer(layer);
        for (Flap flap : flaps.values()) {
            flap.setLayer(layer);
        }
    }

    @Override
    public void setMaterial(Material material) {
        super.setMaterial(material);
        for (Flap flap : flaps.values()) {
            flap.setMaterial(material);
        }
    }

    private void createFlaps() {
        setFlaps(Flap.createFlaps());
    }

    public Flap getFlap(Point point) {
        Flap startFlap = getFlap(Flap.Position.START);
        Flap endFlap = getFlap(Flap.Position.END);

        return Point.distanceSq(startFlap.getCenter().getStart(), point) <= Point.distanceSq(endFlap.getCenter().getStart(), point) ? startFlap : endFlap;
    }

    public Map<Flap.Position, Flap> getFlaps() {
        return flaps;
    }

    public void ensureFlapsCoordinates() {
        if (isEmpty()) {
            return;
        }
        Flap startFlap = flaps.get(Flap.Position.START);
        if (startFlap != null) {
            SimpleWire first = getFirstPart();
            startFlap.setCenter(Edge.of(first.getAxis().getStart()));
        }

        Flap endFlap = flaps.get(Flap.Position.END);
        if (endFlap != null) {
            SimpleWire last = getLastPart();
            endFlap.setCenter(Edge.of(last.getAxis().getEnd()));
        }
    }

    public Optional<Point> findConnectionPointOnFlaps(Wire wire) {
        if (!WireUtils.isConnected(this, wire)) {
            throw new UnexpectedException("wire " + wire.getName() + " is not connect to gate " + name);
        }

        Flap startFlap = getFlap(Flap.Position.START);
        Point workingPoint = startFlap.getCenter().getStart();
        Optional<SimpleWire> forStart = wire.findPartWithPoint(workingPoint);
        if (forStart.isPresent()) {
            return Optional.of(workingPoint);
        }

        Flap endFlap = getFlap(Flap.Position.END);
        workingPoint = endFlap.getCenter().getStart();
        Optional<SimpleWire> forEnd = wire.findPartWithPoint(workingPoint);
        return forEnd.isPresent() ? Optional.of(workingPoint) : Optional.<Point>empty();
    }

    @Override
    public Gate clone() {
        Gate clone = (Gate) super.clone();
        Map<Flap.Position, Flap> flapMap = new HashMap<>();
        for (Map.Entry<Flap.Position, Flap> flap : flaps.entrySet()) {
            flapMap.put(flap.getKey(), flap.getValue().clone());
        }
        clone.setFlaps(flapMap);
        return clone;
    }
}
