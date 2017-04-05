package ru.etu.astamir.gui.painters;

import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.*;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.model.contacts.Contact;
import ru.etu.astamir.model.regions.ContactWindow;

import java.awt.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Artem Mon'ko
 */
public class ContactPainter implements Painter<Contact> {
    @Override
    public void paint(Contact entity, Graphics graphics, Function<Point, Point> coordinateTranslator) {
        Color color = ProjectObjectManager.getColorCentral().getColor(entity);
        graphics.setColor(color);

        Edge center = entity.getCenter();
        if (coordinateTranslator != null) {
            DrawingUtils.drawEdge(Edge.of(coordinateTranslator.apply(center.getStart()), coordinateTranslator.apply(center.getEnd())), 6, true, graphics);
        } else {
            DrawingUtils.drawEdge(center, 6, true, graphics);
        }

        for (ContactWindow window : entity.getContactWindows().values()) {
            Polygon bounds = Polygon.of(window.getCoordinates().stream().map(coordinateTranslator).collect(Collectors.toList()));
            DrawingUtils.drawPolygon(bounds, 0, false, graphics);
        }
    }
}
