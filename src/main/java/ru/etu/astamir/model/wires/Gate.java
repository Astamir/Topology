package ru.etu.astamir.model.wires;

import com.google.common.base.Optional;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Orientation;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.Flap;
import ru.etu.astamir.model.exceptions.UnexpectedException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
        ensureFlapsCoordinates();
    }

    private void createFlaps() {
        setFlaps(Flap.createFlaps());
    }

    public Flap getFlap(Point point) {
        Flap start_flap = getFlap(Flap.Position.START);
        Flap end_flap = getFlap(Flap.Position.END);

        return Point.distanceSq(start_flap.getCenter().getStart(), point) <= Point.distanceSq(end_flap.getCenter().getStart(), point) ? start_flap : end_flap;
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

        Flap start_flap = getFlap(Flap.Position.START);
        Point working_point = start_flap.getCenter().getStart();
        Optional<SimpleWire> for_start = wire.findPartWithPoint(working_point);
        if (for_start.isPresent()) {
            return Optional.of(working_point);
        }

        Flap end_flap = getFlap(Flap.Position.END);
        working_point = end_flap.getCenter().getStart();
        Optional<SimpleWire> for_end = wire.findPartWithPoint(working_point);
        return for_end.isPresent() ? Optional.of(working_point) : Optional.<Point>absent();
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
