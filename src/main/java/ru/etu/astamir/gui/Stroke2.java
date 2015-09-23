package ru.etu.astamir.gui;


import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.grid.legacy.LegacyVirtualGrid;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Orientation;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Rectangle;
import ru.etu.astamir.model.*;
import ru.etu.astamir.model.legacy.LegacyGate;
import ru.etu.astamir.model.legacy.LegacyTransistorActiveRegion;
import ru.etu.astamir.model.legacy.LegacyContact;
import ru.etu.astamir.model.legacy.DirectedBounds;
import ru.etu.astamir.serialization.adapters.LegacyGridAdapter;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.io.File;
import java.util.ArrayList;


public class Stroke2 extends JFrame {
    Stroke drawingStroke = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 1);
    Line2D line = new Line2D.Double(20, 40, 100, 150);
    Rectangle verticalRectangle = Rectangle.of(Edge.of(50, 50, 50, 200), 8, 8);
    Rectangle horizontalRectangle = Rectangle.of(Point.of(50, 50), Direction.RIGHT, 150, 8, 8);

    Border border = new Border(Orientation.HORIZONTAL, null);

    LegacyVirtualGrid grid = new LegacyVirtualGrid();

    boolean drawCombination = false;
    boolean allowedToDrag = false;
    int oldX;
    int oldY;
    LegacyContact contactA;
    private LegacyContact contactB;

    public Stroke2() {
        super();
        int offset = 100;
        TopologyLayer polysilicon = ProjectObjectManager.getLayerFactory().createLayerForMaterialType(Material.POLYSILICON);
        LegacyTransistorActiveRegion p = new LegacyTransistorActiveRegion(polysilicon, ConductionType.P);
        Rectangle bound = new Rectangle(150 + offset, 250 + offset, 100 + offset, 100 + offset);
        p.setDirectedBounds(new DirectedBounds(polysilicon, bound));
        contactA = new LegacyContact(polysilicon, Point.of(200 + offset, 200 + offset), 46, Material.METAL, null, null);
        p.bounds.elements.setElementAt(0, 2, contactA);
        LegacyGate gate = new LegacyGate(polysilicon, Point.of(1, 1), Material.POLYSILICON, 10);
        gate.setFirstPart(Point.of(250 + offset, bound.getBottom()), Direction.UP, bound.getHeight(), Double.MAX_VALUE, true, true);
        p.bounds.elements.setElementAt(1, 1, gate.getParts().get(0));
        contactB = new LegacyContact(polysilicon, Point.of(300 + offset, 100 + offset), 46, Material.METAL, null, null);
//        contactB = new Contact1(Edge.of(300, 300, 500, 500));
        p.bounds.elements.setElementAt(2, 0, contactB);
        grid.insertGrid(p.bounds.adjustIndices(), 1, 1);

//        grid.addElement(contactA);
//        grid.addElement(contactB);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_SPACE) {
                    JFileChooser fileChooser = new JFileChooser();
                    if (fileChooser.showOpenDialog(Stroke2.this) == JFileChooser.APPROVE_OPTION) {
                        File file = fileChooser.getSelectedFile();
                        JAXBContext jaxbContext = null;
                        try {
                            jaxbContext = JAXBContext.newInstance(LegacyVirtualGrid.class, LegacyContact.class, ArrayList.class);
                            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                            grid = (LegacyVirtualGrid) jaxbUnmarshaller.unmarshal(file);
                            grid.resolveLinks();
                        } catch (JAXBException e1) {
                            e1.printStackTrace();
                        }
                    }
                }

                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    try {

                        JFileChooser fileChooser = new JFileChooser();
                        if (fileChooser.showSaveDialog(Stroke2.this) == JFileChooser.APPROVE_OPTION) {
                            File file = fileChooser.getSelectedFile();
							LegacyGridAdapter adapter = new LegacyGridAdapter(grid);
							adapter.marshall(file);
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }

                repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                oldX = e.getX();
                oldY = e.getY();
                allowedToDrag = verticalRectangle.isPointIn(Point.of(e.getX(), e.getY()));
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (allowedToDrag) {
                    int newX = e.getX();
                    int newY = e.getY();
                    verticalRectangle.move(newX - oldX, newY - oldY);
                    oldX = newX;
                    oldY = newY;
                    repaint();
                }
            }
        });
    }

    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        // Вторая колонка.


        grid.draw(g2d);
    }

    public static void main(String args[]) {
        JFrame frame = new Stroke2();
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setVisible(true);
    }
}
