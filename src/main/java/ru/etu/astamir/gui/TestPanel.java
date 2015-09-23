package ru.etu.astamir.gui;

import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.legacy.LegacyTopologyScheme;
import ru.etu.astamir.compression.legacy.TopologyCompressor;
import ru.etu.astamir.compression.grid.legacy.LegacyVirtualGrid;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Rectangle;
import ru.etu.astamir.model.*;
import ru.etu.astamir.model.legacy.*;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Created with IntelliJ IDEA.
 * User: astamir
 * Date: 12.07.12
 * Time: 22:23
 * To change this template use File | Settings | File Templates.
 */
public class TestPanel extends JPanel {
    LegacyTransistorActiveRegion region = new LegacyTransistorActiveRegion(null, ConductionType.P);
    Transistor t1 = new Transistor(Point.of(1, 0), null, ConductionType.N);
    Transistor t2 = new Transistor(Point.of(1, 0), null, ConductionType.N);

    double dx = 0;
    double dy = 0;


    LegacyTopologyScheme scheme;
    TopologyCompressor compressor;

    Graphics2D graphics2D;
    int steps = 0;
    double zoom = 3.0;
    double zoom1 = 1.0;

    Border border;

    public TestPanel() {
        super();
        init4();
    }

    public void change(KeyEvent e) {
        steps++;
        System.out.println("step = " + steps);
        if (steps > 14) {
            steps = 0;
        }
        init4();
        /*TransistorCompressor compressor = new TransistorCompressor(region);

        compressor.compress(steps);
        border = compressor.borders.get(Direction.RIGHT);*/
        compressor = new TopologyCompressor(scheme);

        compressor.moveElements(Direction.LEFT, steps, graphics2D);
        if (steps > 12)    compressor.straightenGates(Direction.RIGHT);
        if (steps > 13) compressor.moveBound(Direction.LEFT);
//        if (steps > 22) {
//            compressor.moveElements(Direction.RIGHT, steps - 22, graphics2D);
//        }

        /*if (steps > 44) {
            compressor.straightenGates(Direction.LEFT);
            compressor.moveBound(Direction.RIGHT);
        }*/


        border = compressor.currentBorder;
        repaint();
        System.out.println("---------------------------------------------------");
    }

    public void zoom(MouseWheelEvent ev) {
        double wheelRotation = -1 * ev.getWheelRotation() * 0.1;
        double v = zoom1 + wheelRotation / 10;
        zoom1 = v <= 0.1 ? 0.1 : v;
        repaint();
    }

    public void drag(MouseEvent e, double lastX, double lastY) {
        dx = e.getX() - lastX;
        dy = e.getY() - lastY;
        repaint();
    }



    public void placePoint(MouseEvent e) {
        repaint();
    }

    private void init() {
        region = new LegacyTransistorActiveRegion(null, ConductionType.P);
        Transistor t1 = new Transistor(Point.of(1, 0), null, ConductionType.N);
        Transistor t2 = new Transistor(Point.of(1, 0), null, ConductionType.N);
        region.setBounds(new Rectangle(30 * zoom, 200  * zoom, 175 * zoom, 150 * zoom));
        region.grid.setElementAt(0, 4, new LegacyContact(null, Point.of(55 * zoom, 145 * zoom), 30 * zoom, null, null, null));

        LegacyGate gate = new LegacyGate(null, t1.getCoordinates(), null, 1 * zoom);
        gate.setColor(Color.BLUE);
        gate.setFirstPart(Point.of(85 * zoom, 50 * zoom), Direction.UP, 150 * zoom, 40, true);
        t1.setGate(gate);
        region.grid.setElementAt(1, 3, t1);

        region.grid.setElementAt(2, 2, new LegacyContact(null, Point.of(115 * zoom, 105 * zoom), 30 * zoom, null, null, null));

        gate = new LegacyGate(null, t2.getCoordinates(), null,1 * zoom);
        gate.setColor(Color.BLUE);
        gate.setFirstPart(Point.of(150 * zoom, 50 * zoom), Direction.UP, 150 * zoom, 40, true);
        t2.setGate(gate);
        region.grid.setElementAt(3, 1, t2);

        region.grid.setElementAt(4, 0, new LegacyContact(null, Point.of(175 * zoom, 70 * zoom), 30 * zoom, null, null, null));
        region.grid.setElementAt(4, 4, new LegacyContact(null, Point.of(175 * zoom, 165 * zoom), 30 * zoom, null, null, null));
    }

    private void init1() {
        region = new LegacyTransistorActiveRegion(null, ConductionType.P);
        Transistor t1 = new Transistor(Point.of(1, 0), null, ConductionType.N);
        Transistor t2 = new Transistor(Point.of(1, 0), null, ConductionType.N);
        region.setBounds(new Rectangle(5 * zoom, 19*5  * zoom, 28*5 * zoom, 90 * zoom));
        region.grid.setElementAt(0, 0, new LegacyContact(null, Point.of(25 * zoom, 30 * zoom), 30 * zoom, null, null, null));

        LegacyGate gate = new LegacyGate(null, t1.getCoordinates(), null, 1 * zoom);
        gate.setColor(Color.BLUE);
        gate.setFirstPart(Point.of(50 * zoom, 5 * zoom), Direction.UP, 90 * zoom, 100, true);
        t1.setGate(gate);
        region.grid.setElementAt(1, 1, t1);

        region.grid.setElementAt(2, 4, new LegacyContact(null, Point.of(70 * zoom, 75 * zoom), 30 * zoom, null, null, null));

        gate = new LegacyGate(null, t2.getCoordinates(), null, 1 * zoom);
        gate.setColor(Color.BLUE);
        gate.setFirstPart(Point.of(95 * zoom, 5 * zoom), Direction.UP, 90 * zoom, 100, true);
        t2.setGate(gate);
        region.grid.setElementAt(3, 3, t2);

        region.grid.setElementAt(4, 0, new LegacyContact(null, Point.of(120 * zoom, 30 * zoom), 30 * zoom, null, null, null));
    }

    private void init2() {
        region = new LegacyTransistorActiveRegion(null, ConductionType.P);
        Transistor t1 = new Transistor(Point.of(1, 0), null, ConductionType.N);
        region.setBounds(new Rectangle(5 * zoom, 19*5  * zoom, 28*5 * zoom, 90 * zoom));
        region.grid.setElementAt(0, 0, new LegacyContact(null, Point.of(25 * zoom, 22 * zoom), 30 * zoom, null, null, null));

        LegacyGate gate = new LegacyGate(null, t1.getCoordinates(), null, 1 * zoom);
        gate.setColor(Color.BLUE);
        gate.setFirstPart(Point.of(50 * zoom, 5 * zoom), Direction.UP, 90 * zoom, 1000, true);
        t1.setGate(gate);
        region.grid.setElementAt(1, 1, t1);

        Bus metalBus = new Bus(null, new Point(0, 0), null, 4 * zoom);
        metalBus.setMaterial(Material.METAL);
        //metalBus.setColor(Color.GREEN);
        metalBus.setFirstPart(Point.of(95 * zoom, 5 * zoom), Direction.UP, 90 * zoom, 1000, true);
        //t2.setGate(gate);
        region.grid.setElementAt(2, 2, metalBus);

        region.grid.setElementAt(3, 3, new LegacyContact(null, Point.of(120 * zoom, 78 * zoom), 30 * zoom, null, null, null));
    }

    private void init4() {
        int offset = 100;
        LegacyVirtualGrid grid = new LegacyVirtualGrid();
        double step = 50;

        // Первая колонка.
        Bus metalBus = new Bus(ProjectObjectManager.getLayerFactory().createLayerForMaterialType(Material.METAL), Point.of(1,1), Material.METAL, 20);
        metalBus.setFirstPart(Point.of(100 + offset, offset), Direction.UP, 800, Double.MAX_VALUE, true, true);
        grid.setElementAt(0, 0, metalBus.getParts().get(0));

        // Вторая колонка.

        TopologyLayer polysilicon = ProjectObjectManager.getLayerFactory().createLayerForMaterialType(Material.POLYSILICON);
        LegacyTransistorActiveRegion p = new LegacyTransistorActiveRegion(polysilicon, ConductionType.P);
        Rectangle bound = new Rectangle(150 + offset, 250 + offset, 100 + offset, 100 + offset);
        DirectedBounds b = new DirectedBounds(polysilicon, bound);
        b.setBoundsClass(LegacyTransistorActiveRegion.class);
        p.setDirectedBounds(b);
        LegacyContact p1c = new LegacyContact(polysilicon, Point.of(200 + offset, 200 + offset), 46, Material.METAL, null, null);
        p.bounds.elements.setElementAt(0,2, p1c);
        LegacyGate gate = new LegacyGate(polysilicon, Point.of(1,1), Material.POLYSILICON, 10);
        Point botGateStart = Point.of(250+offset, bound.getBottom() - 20);
        Point botGateEnd = Point.of(250+offset, bound.getBottom() - 20 +bound.getHeight() + 40 );
        gate.setFirstPart(botGateStart, botGateEnd, Double.MAX_VALUE, true);
        p.bounds.elements.setElementAt(1, 1, gate/*.getParts().get(0)*/);
        LegacyContact p2c = new LegacyContact(polysilicon, Point.of(300 + offset, 100 + offset), 46, Material.METAL, null, null);
        p.bounds.elements.setElementAt(2, 0, p2c);
        grid.insertGrid(p.bounds.adjustIndices(), 1, 1);

        LegacyTransistorActiveRegion n = new LegacyTransistorActiveRegion(polysilicon, ConductionType.N);
        bound = new Rectangle(150 + offset, 750 + offset, 100 + offset, 100 + offset);
        b = new DirectedBounds(polysilicon, bound);
        b.setBoundsClass(LegacyTransistorActiveRegion.class);
        n.setDirectedBounds(b);
        LegacyContact n1c = new LegacyContact(polysilicon, Point.of(200 + offset, 600 + offset), 46, Material.METAL, null, null);
        n.bounds.elements.setElementAt(0,0, n1c);
        gate = new LegacyGate(polysilicon, Point.of(1,1), Material.POLYSILICON, 10);
        //gate.setColor(Color.CYAN);
        Point topGateStart = Point.of(250 + offset, bound.getBottom() - 20);
        gate.setFirstPart(topGateStart, Direction.UP, bound.getHeight() + 40, Double.MAX_VALUE, true, true);
        Point topGateEnd = gate.getParts().get(0).getAxis().getEnd();
        n.bounds.elements.setElementAt(1, 1, gate/*.getParts().get(0)*/);
        LegacyContact n2c = new LegacyContact(polysilicon, Point.of(300 + offset, 700 + offset), 46, Material.METAL, null, null);
        n.bounds.elements.setElementAt(2, 2, n2c);
        grid.insertGrid(n.bounds.adjustIndices(), 1, 10);




        Bus polisiliconConnector = new LegacyGate(polysilicon, Point.of(0, 0), Material.POLYSILICON, 10);
        polisiliconConnector.setFirstPart(botGateEnd, Point.of(botGateEnd.x() + 0.001, botGateEnd.y()), Double.MAX_VALUE, true);
        polisiliconConnector.addPart(Direction.UP, 160 + offset, Double.MAX_VALUE, true);
        polisiliconConnector.addPart(Direction.LEFT, 0.001, Double.MAX_VALUE, true);
        polisiliconConnector.getLastPart().getAxis().setEnd(topGateStart);
        polisiliconConnector.setColor(Color.BLUE);
        grid.setElementAt(3, 15, polisiliconConnector/*.getParts().get(0)*/);
        gate.connectToEndFlap(polisiliconConnector);

        // top connector
        polisiliconConnector = new LegacyGate(polysilicon, Point.of(0, 0), Material.POLYSILICON, 10);
        polisiliconConnector.setColor(Color.ORANGE);
        //polisiliconConnector.setFirstPart(Point.of(250 + offset, bound.getTop() + 20), Direction.UP, 50, Double.MAX_VALUE, true);
        polisiliconConnector.setFirstPart(topGateEnd, Point.of(topGateEnd.x() + 0.001, topGateEnd.y()), Double.MAX_VALUE, true);
        polisiliconConnector.addPart(Direction.UP, 30, Double.MAX_VALUE, true);
        grid.setElementAt(3,0, polisiliconConnector/*.getParts().get(0)*/);
     //
        // bot connector
        polisiliconConnector = new LegacyGate(polysilicon, Point.of(0, 0), Material.POLYSILICON,10);
        polisiliconConnector.setColor(Color.ORANGE);
        polisiliconConnector.setFirstPart(botGateStart, Point.of(botGateStart.x() + 0, botGateStart.y()) , Double.MAX_VALUE, true);
        polisiliconConnector.addPart(Direction.DOWN, 30, Double.MAX_VALUE, true);
        grid.setElementAt(3,1, polisiliconConnector/*.getParts().get(1)*/);

        ///----------------------------------------------

        // Первая колонка.
        metalBus = new Bus(ProjectObjectManager.getLayerFactory().createLayerForMaterialType(Material.METAL), Point.of(1,1), Material.METAL, 20);
        metalBus.setFirstPart(Point.of(400 + offset, offset), Direction.UP, 800, Double.MAX_VALUE, true, true);
        grid.setElementAt(6, 0, metalBus.getParts().get(0));

        // Первая колонка.
        Bus contactConnector = new Bus(ProjectObjectManager.getLayerFactory().createLayerForMaterialType(Material.METAL), Point.of(1,1), Material.METAL, 20);
        contactConnector.setFirstPart(Point.of(300 + offset, 700+ offset), Point.of(300 + offset, 100+ offset), Double.MAX_VALUE, true);
        contactConnector.setColor(Color.BLUE);
        grid.setElementAt(4,1, contactConnector.getParts().get(0));
        p2c.addContactable(contactConnector);
        n2c.addContactable(contactConnector);



        LegacyContact c1 = new LegacyContact(polysilicon, Point.of(200 + offset, 300 + offset), 50, Material.METAL, null, null);
        grid.setElementAt(2, 6, c1);
        contactConnector = new Bus(ProjectObjectManager.getLayerFactory().createLayerForMaterialType(Material.METAL), Point.of(1,1), Material.METAL, 20);
        contactConnector.setColor(Color.ORANGE);
        c1.addContactable(contactConnector);
        p1c.addContactable(contactConnector);
        contactConnector.setFirstPart(Point.of(200 + offset, 300+ offset), Direction.DOWN, 300, Double.MAX_VALUE, true);
        grid.setElementAt(2,3, contactConnector.getParts().get(0));


        LegacyContact c2 = new LegacyContact(polysilicon, Point.of(200 + offset, 500 + offset), 50, Material.METAL, null, null);
        grid.setElementAt(2, 8, c2);
        contactConnector = new Bus(ProjectObjectManager.getLayerFactory().createLayerForMaterialType(Material.METAL), Point.of(1,1), Material.METAL, 20);
        contactConnector.setColor(Color.CYAN);
        c2.addContactable(contactConnector);
        n1c.addContactable(contactConnector);
        contactConnector.setFirstPart(Point.of(200 + offset, 500 + offset), Direction.UP, 300, Double.MAX_VALUE, true);
        grid.setElementAt(2,7, contactConnector.getParts().get(0));


        scheme = new LegacyTopologyScheme(ProjectObjectManager.getLayerFactory().createDefaultTopologyLayer(), grid, new Rectangle(offset, 800+offset, 500, 800));
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        graphics2D = (Graphics2D) g.create();
        graphics2D.scale(zoom1, zoom1);
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON) ;

        hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        graphics2D.setRenderingHints(hints);
       // graphics2D.setTransform(AffineTransform.getTranslateInstance(dx, dy));

       // if (border != null) border.draw(graphics2D);
        scheme.draw(graphics2D);
//       region.draw(graphics2D);
//        if (border != null) border.draw(graphics2D);
        if (compressor != null) {
           // Border metal = compressor.borders.get(LayerFactory.createLayerForMaterialType(Material.POLYSILICON)).get(Direction.LEFT);
        //    metal.draw(graphics2D);
        }
        graphics2D.dispose();
    }


}
