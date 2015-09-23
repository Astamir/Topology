package ru.etu.astamir.model.legacy;

import com.google.common.collect.Lists;
import ru.etu.astamir.compression.grid.legacy.LegacyVirtualGrid;
import ru.etu.astamir.geom.common.Orientation;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.model.ConductionType;
import ru.etu.astamir.model.TopologyLayer;
import ru.etu.astamir.model.legacy.LegacyContact;
import ru.etu.astamir.model.legacy.LegacyTopologyElement;
import ru.etu.astamir.model.legacy.Transistor;
import ru.etu.astamir.model.legacy.ConductionRegion;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Активная область транзистора.
 */
public class LegacyTransistorActiveRegion extends ConductionRegion {
    List<Transistor> transistors = Lists.newArrayList();
    List<LegacyContact> contacts = Lists.newArrayList();
    /**
     * Ориентация затворов.
     */
    Orientation orientation = Orientation.VERTICAL;

    public LegacyTransistorActiveRegion(TopologyLayer layer, ConductionType type) {
        super(layer, type);
    }

    public LegacyTransistorActiveRegion(int x, int y, TopologyLayer layer, ConductionType type) {
        super(x, y, layer, type);
    }

    public LegacyTransistorActiveRegion(Point coordinates, TopologyLayer layer, ConductionType type) {
        super(coordinates, layer, type);
    }

    public LegacyTransistorActiveRegion(Point coordinates, TopologyLayer layer, Polygon bounds, ConductionType type) {
        super(coordinates, layer, bounds, type);
    }

    public List<LegacyContact> getContacts() {
        return contacts;
    }

    public void addContact(int columnIndex, int rowIndex) {
        // TODO мы знаем, что это контакт к активной области. Пока для упрощения сами тут создаим контакт.
    }

    public void setStep(double step) {
        // TODO от этого зависят начальные реальные координаты элементов.
    }

    public void setElementCount(int columnCount, int rowCount) {
        // TODO кол-во элементов. для того, чтоюы заранее разбивать затворы и границы.
    }

    public void addTransistor(int index) {
        // TODO пока что добавляем транзистор во всю ширину(или длину).
    }

    LegacyVirtualGrid getGrid() {
        return bounds.elements; // TODO нужно где-то обязательно добавлять границы (т.е. разбить их и заного добавить или что то в этом духе).
    }


    @Override
    public void draw(Graphics2D g) {
        // отобразить границу.
        Graphics2D graphics = (Graphics2D) g.create();
        graphics.setColor(Color.RED);
        super.draw(graphics);
        //getBounds().draw(graphics);
        graphics.dispose();


      //  grid.draw(g);
    }

    @Override
    public boolean accept(LegacyTopologyElement element) {
        return super.accept(element); // TODO
    }

}
