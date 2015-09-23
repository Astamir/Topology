package ru.etu.astamir.gui;


import ru.etu.astamir.common.Utils;
import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.legacy.LegacyTopologyScheme;
import ru.etu.astamir.compression.grid.legacy.LegacyVirtualGrid;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Orientation;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Rectangle;
import ru.etu.astamir.gui.painters.DrawingUtils;
import ru.etu.astamir.gui.painters.WirePainter;
import ru.etu.astamir.model.*;
import ru.etu.astamir.model.legacy.LegacyContact;
import ru.etu.astamir.model.legacy.Bus;
import ru.etu.astamir.model.legacy.DirectedBounds;
import ru.etu.astamir.model.legacy.LegacyGate;
import ru.etu.astamir.model.legacy.LegacyTransistorActiveRegion;
import ru.etu.astamir.model.wires.SimpleWire;
import ru.etu.astamir.model.wires.Wire;

import javax.swing.JFrame;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;


public class Stroke1 extends JFrame {
    Stroke drawingStroke = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 1);
    Line2D line = new Line2D.Double(20, 40, 100, 150);
    Rectangle verticalRectangle = Rectangle.of(Edge.of(50, 50, 50, 200), 8, 8);
    Rectangle horizontalRectangle = Rectangle.of(Point.of(50, 50), Direction.RIGHT, 150, 8, 8);

    Wire wire = new Wire(Orientation.VERTICAL);
    SimpleWire selected_part;
    Direction direction = Direction.DOWN;
    Point selected_point;

    public Stroke1() {
        super();
        wire.setWidth(20);
        wire.setWidthAtBorder(20);
        wire.setFirstPart(Edge.of(300, 300, Direction.UP, 100), 0, 100500, true, true, true);
        wire.addPart(Direction.RIGHT, 150, 100500, true);
        wire.addPart(Direction.DOWN, 200, 100500, true);
        wire.addPart(Direction.LEFT, 300, 100500, true);
        wire.addPart(Direction.UP, 100, 100500, true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == 'd') {
                    direction = direction.clockwise();
                    repaint();
                    return;
                }

                if (e.getKeyChar() == 'r') {
                    wire.removeEmptyParts();
                    repaint();
                    return;
                }

                if (e.getKeyChar() == 'e') {
                    wire.createAnEmptyLink(selected_point, direction);
                    repaint();
                    return;
                }


                if (e.getKeyChar() == KeyEvent.VK_SPACE) {
                    wire.movePart(selected_part, direction, 5);
                }

                if (e.getKeyChar() == '0') {
                    selected_part = wire.getPart(0);
                    repaint();
                    return;
                }

                 if (e.getKeyChar() == '1') {
                    selected_part = wire.getPart(1);
                    repaint();
                    return;
                }

                if (e.getKeyChar() == '2') {
                    selected_part = wire.getPart(2);
                    repaint();
                    return;
                }

                if (e.getKeyChar() == '3') {
                    selected_part = wire.getPart(3);
                    repaint();
                    return;
                }

                if (e.getKeyChar() == '4') {
                    selected_part = wire.getPart(4);
                    repaint();
                    return;
                }

                if (e.getKeyChar() == '5') {
                    selected_part = wire.getPart(5);
                    repaint();
                    return;
                }

                if (e.getKeyChar() == '6') {
                    selected_part = wire.getPart(6);
                    repaint();
                    return;
                }

                if (e.getKeyChar() == '7') {
                    selected_part = wire.getPart(7);

                    repaint();
                    return;
                }

                if (e.getKeyChar() == '8') {
                    selected_part = wire.getPart(8);
                    repaint();
                    return;
                }

                if (e.getKeyChar() == '9') {
                    selected_part = wire.getPart(9);
                    repaint();
                    return;
                }

                repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selected_point = Point.fromPoint2D(e.getPoint());
                repaint();
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {                

            }
        });
    }

    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        WirePainter painter = new WirePainter();
        painter.paint(wire, g, Utils.Functions.SELF_FUNCTION);
        if (selected_part != null) {
            g.drawString(String.valueOf(selected_part.getIndex()), 10, 10);
        }

        g.drawString(direction.toString(), 25, 10);
        g.setColor(Color.red);
        if (selected_point != null) {
            DrawingUtils.drawPoint(selected_point, 5, false, g);
        }

        //g2d.setStroke(drawingStroke);


        /*Border border = new Border(Orientation.HORIZONTAL);
        border.addPart(Edge.of(80, 100, 400, 100), TransistorActiveRegion.class);

        List<BorderPart> deformed = Lists.newArrayList(new BorderPart(Edge.of(200, 150, 200, 250).rotate(), TransistorActiveRegion.class),
                new BorderPart(Edge.of(150, 250, 150, 350).rotate(), TransistorActiveRegion.class));*/
        
        /*if (!drawCombination){
            border.draw(g2d);
            for (BorderPart part : deformed) {
                part.getAxis().draw(g2d);
            }
        }

       // border.draw(g2d);
        
        if (drawCombination) {
            border.overlay(deformed, Direction.UP);
            border.draw(g2d);
        }*/


        /*Border border = new Border(Orientation.VERTICAL);
        border.getParts().add(new BorderPart(Edge.of(100, 100, 100, 300), TransistorActiveRegion.class));
        Bus bus = new Bus(null, null, null, 8);
        bus.setFirstPart(Edge.of(150, 150, 150, 200), 10, 1000, true);
        bus.addPart(Direction.LEFT, 30, 10, 1000, true);
        bus.addPart(Direction.UP, 50, 10, 1000, true);
        bus.movePart(0, Direction.LEFT, 40);
        border.overlay(BorderPart.of(bus), Direction.LEFT);



        Bus bus1 = new Bus(null, null, null, 8);
        bus1.setFirstPart(Edge.of(200, 110, 200, 290), 10, 1000, true);
        border.draw(g2d);

        border.imitate(bus1, Direction.LEFT);


        bus1.draw(g2d);*/
/*
        Bus bus = new Bus(null, new Point(), null, 8);
        bus.setFirstPart(Edge.of(Point.of(100, 100), Direction.RIGHT, 200), 1000, true);
        bus.addPart(Direction.UP, 100, 1000, true);
        bus.addPart(Direction.LEFT, 100, 1000, true);
        bus.addPart(Direction.UP, 0, 1000, true);
        bus.rebuildBounds();

        //bus.move(150, 0);

        bus.draw(g2d);*/

//        int offset = 100;
//        LegacyVirtualGrid grid = new LegacyVirtualGrid();
//        double step = 50;
//
//        // Первая колонка.
//        Bus metalBus = new Bus(ProjectObjectManager.getLayerFactory().createLayerForMaterialType(Material.METAL), Point.of(1,1), Material.METAL, 20);
//        metalBus.setFirstPart(Point.of(100 + offset, offset), Direction.UP, 800, Double.MAX_VALUE, true, true);
//        grid.setElementAt(0, 0, metalBus.getParts().get(0));
//
//        // Вторая колонка.
//
//        TopologyLayer polysilicon = ProjectObjectManager.getLayerFactory().createLayerForMaterialType(Material.POLYSILICON);
//        LegacyTransistorActiveRegion p = new LegacyTransistorActiveRegion(polysilicon, ConductionType.P);
//        Rectangle bound = new Rectangle(150 + offset, 250 + offset, 100 + offset, 100 + offset);
//        p.setDirectedBounds(new DirectedBounds(polysilicon, bound));
//        p.bounds.elements.setElementAt(0,2, new LegacyContact(polysilicon, Point.of(200 +offset, 200+offset), 46, Material.METAL, null, null));
//        LegacyGate gate = new LegacyGate(polysilicon, Point.of(1,1), Material.POLYSILICON, 10);
//        gate.setFirstPart(Point.of(250+offset, bound.getBottom()), Direction.UP, bound.getHeight(), Double.MAX_VALUE, true, true);
//        p.bounds.elements.setElementAt(1, 1, gate.getParts().get(0));
//        p.bounds.elements.setElementAt(2, 0, new LegacyContact(polysilicon, Point.of(300 +offset, 100+offset), 46, Material.METAL, null, null));
//        grid.insertGrid(p.bounds.adjustIndices(), 1, 1);
//
//
//        LegacyTransistorActiveRegion n = new LegacyTransistorActiveRegion(polysilicon, ConductionType.N);
//        bound = new Rectangle(150 + offset, 750 + offset, 100 + offset, 100 + offset);
//        n.setDirectedBounds(new DirectedBounds(polysilicon, bound));
//        n.bounds.elements.setElementAt(0,0, new LegacyContact(polysilicon, Point.of(200 +offset, 600+offset), 46, Material.METAL, null, null));
//        gate = new LegacyGate(polysilicon, Point.of(1,1), Material.POLYSILICON, 10);
//        gate.setFirstPart(Point.of(250+offset, bound.getBottom()), Direction.UP, bound.getHeight(), Double.MAX_VALUE, true, true);
//        n.bounds.elements.setElementAt(1, 1, gate.getParts().get(0));
//        n.bounds.elements.setElementAt(2, 2, new LegacyContact(polysilicon, Point.of(300 +offset, 700+offset), 46, Material.METAL, null, null));
//        grid.insertGrid(n.bounds.adjustIndices(), 1, 8);
//
//
//        Bus polisiliconConnector = new Bus(polysilicon, Point.of(0, 0), Material.POLYSILICON, 8);
//        polisiliconConnector.setFirstPart(Point.of(250 + offset, bound.getBottom()), Direction.DOWN, 200 + offset, Double.MAX_VALUE, true);
//        grid.setElementAt(3,6, polisiliconConnector);
//
//        // top connector
//        polisiliconConnector = new Bus(polysilicon, Point.of(0, 0), Material.POLYSILICON, 10);
//        polisiliconConnector.setFirstPart(Point.of(250 + offset, bound.getTop()), Direction.UP, 50, Double.MAX_VALUE, true);
//        grid.setElementAt(3,11, polisiliconConnector);
//
//        // bot connector
//        polisiliconConnector = new Bus(polysilicon, Point.of(0, 0), Material.POLYSILICON,10);
//        polisiliconConnector.setFirstPart(Point.of(250 + offset, 50 + offset), Direction.DOWN, 50, Double.MAX_VALUE, true);
//        grid.setElementAt(3,1, polisiliconConnector.getParts().get(0));
//
//        // Первая колонка.
//        metalBus = new Bus(ProjectObjectManager.getLayerFactory().createLayerForMaterialType(Material.METAL), Point.of(1,1), Material.METAL, 20);
//        metalBus.setFirstPart(Point.of(400 + offset, offset), Direction.UP, 800, Double.MAX_VALUE, true, true);
//        grid.setElementAt(6, 0, metalBus.getParts().get(0));
//
//        // Первая колонка.
//        Bus contactConnector = new Bus(ProjectObjectManager.getLayerFactory().createLayerForMaterialType(Material.METAL), Point.of(1,1), Material.METAL, 20);
//        contactConnector.setFirstPart(Point.of(300 + offset, 700+ offset), Point.of(300 + offset, 100+ offset), Double.MAX_VALUE, true);
//        grid.setElementAt(4,8, contactConnector.getParts().get(0));
//
//
//        grid.setElementAt(2, 6, new LegacyContact(polysilicon, Point.of(200 +offset,300+offset), 50, Material.METAL, null, null));
//        contactConnector = new Bus(ProjectObjectManager.getLayerFactory().createLayerForMaterialType(Material.METAL), Point.of(1,1), Material.METAL, 20);
//        contactConnector.setFirstPart(Point.of(200 + offset, 300+ offset), Direction.DOWN, 300, Double.MAX_VALUE, true);
//        grid.setElementAt(2,3, contactConnector.getParts().get(0));
//
//
//
//        grid.setElementAt(2, 7, new LegacyContact(polysilicon, Point.of(200 +offset,500+offset), 50, Material.METAL, null, null));
//        contactConnector = new Bus(ProjectObjectManager.getLayerFactory().createLayerForMaterialType(Material.METAL), Point.of(1,1), Material.METAL, 20);
//        contactConnector.setFirstPart(Point.of(200 + offset, 500 + offset), Direction.UP, 300, Double.MAX_VALUE, true);
//        grid.setElementAt(2,11, contactConnector.getParts().get(0));
//
//
//        LegacyTopologyScheme scheme = new LegacyTopologyScheme(ProjectObjectManager.getLayerFactory().createDefaultTopologyLayer(), grid, new Rectangle(offset, 800+offset, 500, 800));
//       // scheme.adjustIndices();
//        scheme.draw(g2d);
//        //n.bounds.adjustIndices();

        /*if (!drawCombination) {
            for (Edge edge : border.getEdges()) {
                edge.draw(g2d);
            }
            part.getAxis().draw(g2d);
        }

        border.overlay(Lists.newArrayList(part), Direction.DOWN);
        if (drawCombination) {
            for (Edge edge : border.getEdges()) {
                edge.draw(g2d);
            }
        }*/

        //if (drawCombination) Polygon.combine(verticalRectangle, horizontalRectangle).draw(g2d);

       /* Bus bus = new Bus(null, null, null, 10);
        bus.setFirstPart(Edge.of(100, 100, Direction.UP, 100), 0, 1000, true);
        //bus.draw(g2d);

        bus.createAnEmptyLink(Point.of(100, 150), Direction.LEFT);
        bus.draw(g2d);*/


        /*GeomUtils.move(bus, Direction.RIGHT, 200);
        bus.draw(g2d);*/
        
    }
    
    public static void main(String args[]) {
        JFrame frame = new Stroke1();
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setVisible(true);
    }
}
