package ru.etu.astamir.gui;

import com.google.common.collect.Lists;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.geom.common.Rectangle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.stream.Collectors;

/**
 * @author Artem Mon'ko
 */
public class PolygonPanel extends JFrame {
    Edge edge = Edge.of(100, 100, 200, 100);
    Polygon p = new Polygon(Rectangle.of(edge, 20, 20).vertices());
    Direction dir = Direction.RIGHT;
    int i = 0;

    public PolygonPanel() {
        super();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                dir = dir.counterClockwise();
                edge = Edge.of(edge.getEnd(), dir, 150);
                p = addPartToBounds(p, Rectangle.of(edge, 20, 20));
                i++;
                repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
            }
        });
    }

    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        for (ru.etu.astamir.geom.common.Point point : p.vertices()) {
            g2d.drawOval(point.intX(), point.intY(), 3, 3);
        }
        //g2d.draw(p.toAWTPolygon());
    }

    Polygon addPartToBounds(Polygon bounds, Rectangle partBounds) {
        // 1. find pure crossing point and add it.
        // 2. find that junction common point and do not touch it
        // 3. delete all remaining common points

        java.util.List<ru.etu.astamir.geom.common.Point> currentPoints = Lists.newArrayList(bounds.vertices());

        for (ru.etu.astamir.geom.common.Point point : partBounds.vertices()) {
            boolean good = true;
            for (Edge edge : bounds.edges()) {
                if (edge.isPointInOrOnEdges(point)) {
                    good = false;
                }
            }

            if (good) {
                currentPoints.add((ru.etu.astamir.geom.common.Point) point.clone());
            }
        }

        for (ru.etu.astamir.geom.common.Point point : bounds.vertices()) {
            for (Edge edge : partBounds.edges()) {
                if (edge.isPointIn(point)) {
                    currentPoints.remove(point.clone());
                }
            }
        }

        for (Edge edge : partBounds.edges()) {
            for (Edge e : bounds.edges())
                if (e.pureCross(edge)) {
                    currentPoints.add(e.crossing(edge));
                }
        }

        Polygon polygon = Polygon.of(currentPoints.stream().distinct().collect(Collectors.toList()));
//        polygon.walk();
        return polygon;
    }

    public static void main(String args[]) {
        JFrame frame = new PolygonPanel();
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setVisible(true);
    }
}
