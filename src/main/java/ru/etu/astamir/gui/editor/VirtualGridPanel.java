package ru.etu.astamir.gui.editor;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.grid.VirtualGrid;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.gui.painters.*;
import ru.etu.astamir.gui.painters.Painter;
import ru.etu.astamir.gui.widgets.PlayerPanel;
import ru.etu.astamir.launcher.Topology;
import ru.etu.astamir.launcher.VirtualTopology;
import ru.etu.astamir.model.TopologyElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VirtualGridPanel extends JPanel {
    private static final int OFFSET = -100;
    private static final Insets COORDINATE_WINDOW = new Insets(500, 500, 500, 500);

    ElementModel model;
    double step = 10;
    private TopologyElement selectedElement;

    private ZoomAndDragHandler handler = new ZoomAndDragHandler(COORDINATE_WINDOW);
    JLabel coordinates;

    public enum TopologyMode {
        VIRTUAL, REAL
    }

    private TopologyMode mode = TopologyMode.VIRTUAL;

    private Collection<Border> borders_to_paint = Lists.newArrayList();
    private Point point;

    public VirtualGridPanel(VirtualTopology topology, int step) {
        this.step = step;
        //this.topology = topology;

        model = new VirtualElementModel(topology);
        coordinates = new JLabel();
        handler.install(this, coordinates, true);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point point = goBack(Point.fromPoint2D(e.getPoint()));
                VirtualGridPanel.this.point = point;
                Collection<TopologyElement> elements = model.grid().findElements(point);
                if (!elements.isEmpty()) {
                    selectedElement = elements.iterator().next();
                }
                repaint();
            }
        });

        setBackground(Color.WHITE);

        //addMouseMotionListener(new CoordinateTracker(coordinates));
        add(coordinates);
    }

    public void setModel(ElementModel model) {
        this.model = model;
        repaint();
    }

    /**
     * Это только для отрисовки
     *
     * @param coordinate
     * @return
     */
    Point toGridCoordinates(Point coordinate) {
        return mode == TopologyMode.VIRTUAL ? Point.of(coordinate.x() * step, -coordinate.y() * step) : Point.of(coordinate.x(), -coordinate.y());
    }

    Point goBack(Point coordinate) {
        return Point.of(coordinate.x() / step, -coordinate.y() / step);
    }

    private Point toRealCoordinates(Point coordinate) {
        Point clone = (Point) coordinate.clone();
        return mode == TopologyMode.VIRTUAL ? clone.multiply(step) : clone;
    }

    public void setBorders(Collection<Border> borders_to_paint) {
        this.borders_to_paint = borders_to_paint;
    }

    private List<Point> convert(Collection<Point> coordinates) {
        List<Point> result = new ArrayList<>();
        for (Point coordinate : coordinates) {
            Point closestPoint = toGridCoordinates(coordinate);
            if (closestPoint != null)
                result.add(closestPoint);
        }
        return result;
    }

    private void drawElements(Graphics g) {
        Graphics2D graphics2D = null;
        try {
            graphics2D = (Graphics2D) g.create();

            PainterCentral painters = ProjectObjectManager.getPainterCentral();
            for (TopologyElement elem : model.getAllElements()) {
                Painter<TopologyElement> painter = painters.getEntityPainter(elem);
                painter.paint(elem, graphics2D, new Function<Point, Point>() {
                    @Override
                    public Point apply(Point input) {
                        return toGridCoordinates(input);
                    }
                });
            }
        } finally {
            if (graphics2D != null)
                graphics2D.dispose();
        }
    }

    private List<Edge> toEdges(List<Point> points) {
        ru.etu.astamir.geom.common.Polygon polygon = new Polygon(points);
        return polygon.edges();
    }


    private void drawGrid(Graphics g) {
        Graphics2D graphics2D = null;
        try {
            graphics2D = (Graphics2D) g.create();

            int width = getWidth() + COORDINATE_WINDOW.left + COORDINATE_WINDOW.right;
            int height = getHeight() + COORDINATE_WINDOW.top + COORDINATE_WINDOW.bottom;

            //graphics2D.drawRect(-COORDINATE_WINDOW.left, -COORDINATE_WINDOW.top, width, height);

            //graphics2D.drawLine(-COORDINATE_WINDOW.left, 0, -COORDINATE_WINDOW.left + width, 0);
           // graphics2D.drawLine(0, -COORDINATE_WINDOW.bottom, 0, -COORDINATE_WINDOW.bottom + height);

            for (int i = -COORDINATE_WINDOW.left; i < width; i += step) {
                for (int j = -COORDINATE_WINDOW.bottom; j < height; j += step) {
                    graphics2D.setColor(Color.GRAY);
                    DrawingUtils.drawPoint(Point.of(i, j), 2, false, graphics2D);
                }
            }
        } finally {
            if (graphics2D != null)
                graphics2D.dispose();
        }
    }

    // todo
    private Point findClosestPoint(Point p) {
        if (p == null) {
            return null;
        }
        Point ap = Point.minus(p, Point.of(handler.getDx(), handler.getDy()));
        ap.multiply(1d / step);

        return Point.of(Math.round(ap.x()), Math.round(ap.y()));
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setTransform(handler.getAffineTransform());

        drawGrid(graphics2D);
        drawElements(graphics2D);
        if (selectedElement != null) {
            graphics2D.drawString(selectedElement.getSymbol(), 0, 0);
        }
        if (point != null) {
            DrawingUtils.drawPoint(toGridCoordinates(point), 8, false, graphics2D);
        }
        for (Border border : borders_to_paint) {
            BorderPainter painter = new BorderPainter();
            painter.paint(border, graphics2D, new Function<Point, Point>() {
                @Override
                public Point apply(Point input) {
                    return Point.of(input.x() * step, -input.y() * step);
                }
            });
        }
    }

    private class ZoomAndDragHandler implements MouseMotionListener, MouseWheelListener, MouseListener {
        double currentMouseX;
        double currentMouseY;

        double pressedX;
        double pressedY;

        double oldPressedX;
        double oldPressedY;

        double dx;
        double dy;

        double oldDx;
        double oldDy;

        double currentX;
        double currentY;

        double scale = 1;
        double oldScale = 1;

        boolean allowZooming;

        JPanel panel;

        Insets constraints;

        JLabel label;

        public ZoomAndDragHandler(Insets constraints) {
            this.constraints = constraints;
        }

        public void install(JPanel panel, JLabel coordinate_label, boolean allowZooming) {
            this.panel = panel;
            this.allowZooming = allowZooming;
            this.label = coordinate_label;
            panel.addMouseListener(this);
            panel.addMouseMotionListener(this);
            if (allowZooming) {
                panel.addMouseWheelListener(this);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {
            oldPressedX = pressedX;
            oldPressedY = pressedY;

            pressedX = e.getX();
            pressedY = e.getY();
            // panel.repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            oldDx = dx;
            oldDy = dy;
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

        @Override
        public void mouseDragged(MouseEvent e) {
            double newDx = oldDx + e.getX() - pressedX;
            double newDy = oldDy + e.getY() - pressedY;
            if (newDx >= constraints.right || newDx <= -constraints.left) {
                // stop the move;
                newDx = newDx < 0 ? -constraints.left : constraints.right;
            }

            if (newDy >= constraints.bottom || newDy <= -constraints.top) {
                // stop the move;
                newDy = newDy < 0 ? -constraints.top : constraints.bottom;
            }

            dx = newDx;
            dy = newDy;

//            Point2D adjPreviousPoint = getTranslatedPoint(pressedX, pressedY);
//            Point2D adjNewPoint = getTranslatedPoint(e.getX(), e.getY());
//
//            double newX = adjNewPoint.getX() - adjPreviousPoint.getX();
//            double newY = adjNewPoint.getY() - adjPreviousPoint.getY();
//
//            pressedX = e.getX();
//            pressedY = e.getY();
//
//            currentX += newX;
//            currentY += newY;

            panel.repaint();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            currentMouseX = e.getX();
            currentMouseY = e.getY();
            Point point = goBack(Point.fromPoint2D(getTranslatedPoint(e.getPoint().x, e.getPoint().y)));

            label.setText(point.toString());
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            oldScale = scale;
            scale += -.1 * e.getPreciseWheelRotation();
            panel.repaint();

        }

        AffineTransform getAffineTransform() {
            AffineTransform tx = new AffineTransform();

            if (allowZooming) {
                double centerX = (double) panel.getWidth() / 2;
                double centerY = (double) panel.getHeight() / 2;

                tx.translate(centerX, centerY);
                tx.scale(scale, scale);
                tx.translate(currentX, currentY);
            }


            tx.translate(dx, dy);

            return tx;
        }

        private Point2D getTranslatedPoint(double panelX, double panelY) {
            AffineTransform tx = getAffineTransform();
            Point2D point2d = new Point2D.Double(panelX, panelY);
            try {
                return tx.inverseTransform(point2d, null);
            } catch (NoninvertibleTransformException ex) {
                ex.printStackTrace();
                return null;
            }
        }

        public double getDx() {
            return dx;
        }

        public double getDy() {
            return dy;
        }
    }

    private class CoordinateTracker implements MouseMotionListener {
        private JLabel label;

        public CoordinateTracker(JLabel label) {
            this.label = label;
        }

        @Override
        public void mouseDragged(MouseEvent e) {

        }

        @Override
        public void mouseMoved(MouseEvent e) {
            label.setText((Point.fromPoint2D(e.getPoint())).toString());
        }
    }
}
