package ru.etu.astamir.model.legacy;

import com.google.common.collect.ImmutableList;
import ru.etu.astamir.compression.Border;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.GeomUtils;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.model.*;
import ru.etu.astamir.model.technology.Technology;

import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.List;

/**
 * Транзистор
 */
public class Transistor extends LegacyTopologyElement implements Movable, Serializable {

    /**
     * Затвор.
     */
    private LegacyGate gate;

    /**
     * Исток, хотя вроде бы все подключаются к активной области.
     */
    private LegacyContact source;

    /**
     * Сток
     */
    private LegacyContact drain;

    /**
     * Тип проводимости.
     */
    private ConductionType conductionType = ConductionType.P;

    public Transistor(Point coordinates, TopologyLayer layer, ConductionType conductionType) {
        super(coordinates, layer);
        this.conductionType = conductionType;
    }

    public LegacyGate getGate() {
        return gate;
    }

    public void setGate(LegacyGate gate) {
        this.gate = gate;
    }

    public LegacyContact getSource() {
        return source;
    }

    public void setSource(LegacyContact source) {
        this.source = source;
    }

    public LegacyContact getDrain() {
        return drain;
    }

    public void setDrain(LegacyContact drain) {
        this.drain = drain;
    }

    public ConductionType getConductionType() {
        return conductionType;
    }

    public void setConductionType(ConductionType conductionType) {
        this.conductionType = conductionType;
    }

    @Override
    public Polygon getBounds() {
        return gate.getBounds();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void draw(Graphics2D g) {
        // draw a transistor

        gate.draw(g);


    }

    @Override
    // TODO look through
    public boolean move(double dx, double dy) {
        return gate.move(dx, dy);
        // source.move
        // drain.move
    }

    public boolean move(Direction direction, double d) {
        return GeomUtils.move(this, direction, d);
    }


    /* public void setCoordinates(double x, double y) {
       boolean result = true;
       for (Bus gate : gates) {
           result &= gate.setCoordinates(x, y);
       }

       result &= activeRegion.setCoordinates(x, y);

       return result;
   } */

    public double getMinDistance(LegacyTopologyElement element) {
        return LegacyDistanceCharacteristics.getInstance().getMinDistance(Transistor.class, element.getClass());
    }

    public ImmutableList<Bus.BusPart> getParts() {
        return gate.getParts();
    }

    public int size() {
        return gate.size();
    }

    public void deform(Point point, Direction direction, Direction half, double width) {
        gate.deform(point, direction, half, width);
    }

    public void straighten(List<TopologyElement> elements, Border border, Direction direction, Technology.TechnologicalCharacteristics technology) {

    }

    public void straighten(List<LegacyTopologyElement> elements, Border border, Direction direction) {
        gate.straighten(elements, border, direction);
    }
}
