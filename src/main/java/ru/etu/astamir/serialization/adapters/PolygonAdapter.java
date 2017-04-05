package ru.etu.astamir.serialization.adapters;

import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.model.exceptions.UnexpectedException;
import ru.etu.astamir.serialization.*;

import java.util.*;

/**
 * @author Artem Mon'ko
 */
public class PolygonAdapter implements AttributeAdapter<Polygon> {
    PointAdapter pointAdapter = (PointAdapter) AttributeContainer.getInstance().getAdapterFor(Point.class);

    @Override
    public List<Attribute> getAttributes(Polygon entity) {
        List<Attribute> attributes = new ArrayList<>();
        List<Attribute> vertices = new ArrayList<>();
        for (int i = 0; i < entity.vertices().size(); i++) {
            Point vertex = entity.vertices().get(i);
            vertices.add(AttributeFactory.createAttribute("vertex", pointAdapter.getAttributes(vertex)));
        }

        attributes.add(AttributeFactory.createAttribute("vertices", vertices));

        return attributes;
    }

    @Override
    public Polygon getEntity(Collection<Attribute> attributes) {
        Polygon polygon = new Polygon();
        Optional<Attribute> vertices = AttributeContainer.findAttribute(attributes, "vertices");
        List<Point> points = new ArrayList<>();
        if (vertices.isPresent()) {
            ComplexAttribute v = (ComplexAttribute) vertices.get();
            for (Attribute attribute : AttributeContainer.findAllAttribute("vertex", v.getValue())) {
                ComplexAttribute complexAttribute = (ComplexAttribute) attribute;
                points.add(pointAdapter.getEntity(complexAttribute.getValue()));
            }
        } else {
            throw new UnexpectedException("polygon doesn't have any vertices ?");
        }

        polygon.setVertices(points);

        return polygon;
    }
}
