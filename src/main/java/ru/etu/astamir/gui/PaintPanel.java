package ru.etu.astamir.gui;

import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.legacy.Bus;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * Created with IntelliJ IDEA.
 * User: astamir
 * Date: 12.07.12
 * Time: 22:23
 * To change this template use File | Settings | File Templates.
 */
public class PaintPanel extends JPanel {
    boolean a = false;
    Bus bus = new Bus(null, new Point(), null, 20);
    Point deformPoint = new Point();
    Direction deformDirection = Direction.RIGHT;
    Direction deformHalf = Direction.RIGHT;
    
    Bus.BusPart selectedPart = null;
    private boolean canCreateEmptyLinks = false;

//    DirectedBounds bounds = new DirectedBounds(null, new Rectangle(100, 500, 400, 400));


    public PaintPanel() {
        super();
        init();
    }

    public void change(KeyEvent e) {
        if (e.getKeyChar() == '4') {
            deformDirection = Direction.LEFT;
            bus.directlyMovePart(bus.getParts().indexOf(selectedPart), deformDirection, 1);
            repaint();
            return;
        }

        if (e.getKeyChar() == '8') {
            deformDirection = Direction.UP;
            bus.directlyMovePart(bus.getParts().indexOf(selectedPart), deformDirection.getOppositeDirection(), 1);
            repaint();
            return;
        }

        if (e.getKeyChar() == '6') {
            deformDirection = Direction.RIGHT;
            bus.directlyMovePart(bus.getParts().indexOf(selectedPart), deformDirection, 1);
            repaint();
            return;
        }

        if (e.getKeyChar() == '2') {
            deformDirection = Direction.DOWN;
            bus.directlyMovePart(bus.getParts().indexOf(selectedPart), deformDirection.getOppositeDirection(), 1);
            repaint();
            return;
        }

        if (e.getKeyChar() == '9') {
            deformHalf = Direction.RIGHT;
            bus.removeEmptyParts();
            repaint();
            return;
        }

        if (e.getKeyChar() == '7') {
            deformHalf = Direction.LEFT;
            repaint();
            return;
        }

        if (e.getKeyChar() == '3') {
            deformHalf = Direction.UP;
            repaint();
            return;
        }
        
        if (e.getKeyChar() == '1') {
            /*Optional<Bus.BusPart> closestPart1 = bus.getClosestPartWithConstraints(deformPoint, deformDirection);
            if (closestPart1.isPresent()) {
                closestPart1.get().setColor(Color.BLUE);
                repaint();
            }*/

            /*Optional<Bus.BusPart> closest = bus.closest(selectedPart);
            if (closest.isPresent()) {
                closest.get().setColor(Color.BLUE);
            }*/
          //  bounds.imitate(deformDirection);
            repaint();

            return;
        }

        if (e.getKeyChar() == '5') {
          //  if (canCreateEmptyLinks) bounds.moveBound(deformHalf, deformDirection, 3);
            bus.stretch(selectedPart, deformDirection.getOppositeDirection(), 1);
            repaint();
            return;
        }



        if (e.getKeyChar() == '0') {
            canCreateEmptyLinks = !canCreateEmptyLinks;
            return;
        }


        if (!a) {
            bus.deform(deformPoint, deformDirection, deformHalf, 40);
        } else {
            bus = new Bus(null, new Point(), null, 20);
            init();
        }

        a = !a;
        repaint();
    }
    
    public void placePoint(MouseEvent e) {       
        deformPoint = Point.of(e.getX(), e.getY());
        if (canCreateEmptyLinks) {
            bus.createAnEmptyLink(deformPoint, deformDirection);
        }
        boolean found = false;
        for (Bus.BusPart part : bus.getParts()) {
            if (part.getBounds().isPointIn(deformPoint) && !found) {
                selectedPart = part;
                selectedPart.setColor(Color.RED);
                found = true;
                break;
            }

            part.setColor(Color.BLACK);
        }

        repaint();
    }
    
    private void init() {
        /*Bus bus = new Bus(null, null, null, 100);
        bus.setFirstPart(Edge.of(400, 500, Direction.RIGHT, 500), 0, 200, true);
        bus.addPart(Direction.DOWN, 200, 0, 200, true);
        bus.draw(graphics);*/
        bus.setMaxBendLength(50);
        bus.setFirstPart(Edge.of(100, 100, Direction.UP, 400), 50, true);
        /*bus.addPart(Direction.UP, 100, 0, 200, true);
        bus.addPart(Direction.UP, 100, 0, 200, true);*/

        //bus.createAnEmptyLink(Point.of(100, 150), Direction.RIGHT);


        bus.setFirstPart(Edge.of(100, 100, Direction.RIGHT, 100), 200, true);
        bus.addPart(Direction.UP, 100, 200, true);
        bus.addPart(Direction.LEFT, 150, 200, true);
        bus.addPart(Direction.UP, 100, 200, true);
        bus.addPart(Direction.RIGHT, 150, 200, true);
      //  bounds.elements.addElement(new LegacyContact(null, Point.of(220, 320), 40, null, null, null));
       // bounds.adjustIndices();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D graphics = (Graphics2D) g;
//        graphics.drawString("dir = " + deformDirection + " half = " + deformHalf, 50, 50);
//       // deformPoint.draw(graphics);
//        bus.draw(graphics);
        Stroke drawingStroke = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 2, new float[]{4}, 1);
        graphics.setStroke(drawingStroke);
        Color green = Color.GREEN;
        Rectangle rectangle = new Rectangle(100, 100, 300, 300);
        graphics.setPaint(new GradientPaint(100, 100, Color.GREEN.darker(), 300, 300, Color.GREEN));
        graphics.fill(rectangle);
        graphics.setPaint(Color.GREEN);
        rectangle.setRect(120, 120, 280, 280);
        graphics.fill(rectangle);



     //   bounds.draw(graphics);
    }


}
