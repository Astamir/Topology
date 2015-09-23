package ru.etu.astamir.serialization.adapters;

import com.google.common.base.Optional;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.serialization.Attribute;
import ru.etu.astamir.serialization.AttributeAdapter;
import ru.etu.astamir.serialization.AttributeContainer;
import ru.etu.astamir.serialization.AttributeFactory;

import java.util.*;

/**
 * Created by Astamir on 03.03.14.
 */
public class PointAdapter implements AttributeAdapter<Point> {
    @Override
    public List<Attribute> getAttributes(Point entity) {
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(AttributeFactory.createLocalAttribute("x", String.valueOf(entity.x())));
		attributes.add(AttributeFactory.createLocalAttribute("y", String.valueOf(entity.y())));

        return attributes;
    }

    @Override
    public Point getEntity(Collection<Attribute> attributes) {
        Optional<Attribute> xAtr = AttributeContainer.findAttribute(attributes, "x", true);
        Optional<Attribute> yAtr = AttributeContainer.findAttribute(attributes, "y", true);
        double x = xAtr.isPresent() ? Double.parseDouble(String.valueOf(xAtr.get().getValue())) : 0.0;
        double y = xAtr.isPresent() ? Double.parseDouble(String.valueOf(yAtr.get().getValue())) : 0.0;

        return Point.of(x, y);
    }
}
