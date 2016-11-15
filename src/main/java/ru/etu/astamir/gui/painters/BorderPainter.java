package ru.etu.astamir.gui.painters;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.BorderPart;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.*;
import ru.etu.astamir.geom.common.Point;

import java.awt.*;

/**
 * @author Artem Mon'ko
 */
public class BorderPainter implements Painter<Border> {

    @Override
    public void paint(Border border, Graphics graphics, Function<Point, Point> coordinateTranslator) {
        Stroke drawingStroke = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 1);
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setStroke(drawingStroke);
        for (BorderPart part : border.getParts()) {
            Color color = ProjectObjectManager.getColorCentral().getColor(part.getSymbol());
            graphics.setColor(color);
           // DrawingUtils.drawEdge(part.getAxis(), 0, g2d, coordinateTranslator);
            Point axisCenter = coordinateTranslator.apply(part.getAxis().getCenter());
            //g2d.drawString(String.valueOf(System.identityHashCode(part)), (float) axisCenter.x(), (float) axisCenter.y());
        }
    }
}
