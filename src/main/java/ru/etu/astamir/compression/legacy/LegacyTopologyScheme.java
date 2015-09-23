package ru.etu.astamir.compression.legacy;

import com.google.common.collect.ImmutableList;
import ru.etu.astamir.compression.grid.legacy.LegacyVirtualGrid;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Rectangle;
import ru.etu.astamir.model.*;
import ru.etu.astamir.model.contacts.Connector;
import ru.etu.astamir.model.legacy.*;

/**
 * Created by IntelliJ IDEA.
 * User: Astamir
 * Date: 27.03.13
 * Time: 20:40
 * To change this template use File | Settings | File Templates.
 */
public class LegacyTopologyScheme extends DirectedBounds{
    private TopologyLayer layer;


    public LegacyTopologyScheme(TopologyLayer layer, LegacyVirtualGrid grid, Rectangle bounds) {
        super(layer,grid, bounds);

    }

    public void addContact(LegacyContact contact, int x, int y) {

    }

    public void addBus() {

    }

    public void addTransistor() {

    }

    public void addEquipotentialContact() {

    }

    public void connect(Connector source, Bus element) {
        source.connect(element);

    }





    public static LegacyTopologyScheme test() {
        int offset = 100;
        LegacyVirtualGrid grid = new LegacyVirtualGrid();
        double step = 50;

        // Первая колонка.
        Bus metalBus = new Bus(ProjectObjectManager.getLayerFactory().createLayerForMaterialType(Material.METAL), Point.of(1,1), Material.METAL, 50);
        metalBus.setFirstPart(Point.of(100 + offset, offset), Direction.UP, 800, Double.MAX_VALUE, true, true);
        grid.setElementAt(0, 0, metalBus.getParts().get(0));

        // Вторая колонка.
        TopologyLayer polysilicon = ProjectObjectManager.getLayerFactory().createLayerForMaterialType(Material.POLYSILICON);
        LegacyTransistorActiveRegion n = new LegacyTransistorActiveRegion(polysilicon, ConductionType.N);
        Rectangle bound = new Rectangle(150 + offset, 750 + offset, 200 + offset, 200 + offset);
        n.setDirectedBounds(new DirectedBounds(polysilicon, bound));
        n.bounds.elements.setElementAt(0,0, new LegacyContact(polysilicon, Point.of(200 +offset, 600+offset), 46, Material.METAL, null, null));
        LegacyGate gate = new LegacyGate(polysilicon, Point.of(1,1), Material.POLYSILICON, 24);
        gate.setFirstPart(Point.of(250+offset, bound.getBottom()), Direction.UP, bound.getHeight(), Double.MAX_VALUE, true, true);
        n.bounds.elements.setElementAt(1, 1, gate.getParts().get(0));
        n.bounds.elements.setElementAt(2, 2, new LegacyContact(polysilicon, Point.of(300 +offset, 700+offset), 46, Material.METAL, null, null));
        //n.bounds.adjustIndices();

        LegacyTransistorActiveRegion p = new LegacyTransistorActiveRegion(polysilicon, ConductionType.P);
        bound = new Rectangle(150 + offset, 250 + offset, 200 + offset, 200 + offset);
        p.setDirectedBounds(new DirectedBounds(polysilicon, bound));
        p.bounds.elements.setElementAt(0,2, new LegacyContact(polysilicon, Point.of(200 +offset, 200+offset), 46, Material.METAL, null, null));
        gate = new LegacyGate(polysilicon, Point.of(1,1), Material.POLYSILICON, 24);
        gate.setFirstPart(Point.of(250+offset, bound.getBottom()), Direction.UP, bound.getHeight(), Double.MAX_VALUE, true, true);
        p.bounds.elements.setElementAt(1, 1, gate.getParts().get(0));
        p.bounds.elements.setElementAt(2, 0, new LegacyContact(polysilicon, Point.of(300 + offset, 100 + offset), 46, Material.METAL, null, null));
        //n.bounds.adjustIndices();


        return new LegacyTopologyScheme(ProjectObjectManager.getLayerFactory().createDefaultTopologyLayer(), grid, new Rectangle(offset, offset, 500 + offset, 800 + offset));
    }

    @Override
    public ImmutableList<TopologyLayer> affectedLayers() {
        return ImmutableList.of(ProjectObjectManager.getLayerFactory().createLayerForMaterialType(Material.METAL),
                ProjectObjectManager.getLayerFactory().createLayerForMaterialType(Material.POLYSILICON));
    }
}
