package ru.etu.astamir.common.test;

import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.BorderPart;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Orientation;
import ru.etu.astamir.gui.painters.DrawingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;

/**
 * @author Artem Mon'ko
 */
public class BorderOverlayTest extends JFrame {
    Direction direction = Direction.LEFT;
    Border border = new Border(Orientation.VERTICAL, ProjectObjectManager.getCurrentProject().getTopologies().get("default_topology").getTechnology().getCharacteristics());

    public BorderOverlayTest() {
        super();
//        border.addPart(Edge.of(100, 150, 100, 170), "ZN");
//        border.addPart(Edge.of(100, 170, 160, 170), "ZN");
//        border.addPart(Edge.of(150, 100, 150, 150), "ZN");
//        border.addPart(Edge.of(100, 150, 200, 150), "ZN");
//        border.addPart(Edge.of(200, 150, 200, 200), "ZN");
//        border.addPart(Edge.of(200, 200, 100, 200), "ZN");
//        border.addPart(Edge.of(100, 200, 100, 250), "ZN");

        border.addPart(Edge.of(100, 100, 400, 100), "ZZ");
        border.addPart(Edge.of(400, 100, 400, 400), "ZZ");
        border.addPart(Edge.of(400, 400, 100, 400), "ZZ");
        border.addPart(Edge.of(100, 400, 100, 100), "ZZ");

        border.addPart(Edge.of(220, 130, 220, 160), "ZN");
        border.addPart(Edge.of(220, 160, 240, 160), "ZN");
        border.addPart(Edge.of(240, 160, 240, 130), "ZN");
        border.addPart(Edge.of(240, 130, 220, 130), "ZN");
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == 'd') {
                    direction = direction.clockwise();
                    repaint();
                    return;
                }

                if (e.getKeyChar() == 'r') {
                    repaint();
                    return;
                }

                if (e.getKeyChar() == 'e') {
                    repaint();
                    return;
                }


                if (e.getKeyChar() == KeyEvent.VK_SPACE) {
                    border.overlay(Collections.<BorderPart>emptyList(), direction);
                }

                if (e.getKeyChar() == '0') {
                    repaint();
                    return;
                }

                if (e.getKeyChar() == '1') {
                    repaint();
                    return;
                }

                if (e.getKeyChar() == '2') {
                    repaint();
                    return;
                }

                if (e.getKeyChar() == '3') {
                    repaint();
                    return;
                }

                if (e.getKeyChar() == '4') {
                    repaint();
                    return;
                }

                if (e.getKeyChar() == '5') {
                    repaint();
                    return;
                }

                if (e.getKeyChar() == '6') {
                    repaint();
                    return;
                }

                if (e.getKeyChar() == '7') {

                    repaint();
                    return;
                }

                if (e.getKeyChar() == '8') {
                    repaint();
                    return;
                }

                if (e.getKeyChar() == '9') {
                    repaint();
                    return;
                }

                repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
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
        for (Edge edge : border.getEdges()) {
            DrawingUtils.drawEdge(edge, 2, false, g2d);
        }
    }

    public static void main(String args[]) {
        JFrame frame = new BorderOverlayTest();
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setVisible(true);
    }
}
