package ru.etu.astamir.serialization.adapters;

import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.serialization.Attribute;
import ru.etu.astamir.serialization.AttributeAdapter;
import ru.etu.astamir.serialization.AttributeContainer;
import ru.etu.astamir.serialization.AttributeFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Artem Mon'ko
 */
public class EdgeAdapter implements AttributeAdapter<Edge> {
	PointAdapter pointAdapter = (PointAdapter) AttributeContainer.getInstance().getAdapterFor(Point.class);

	@Override
	public List<Attribute> getAttributes(Edge entity) {
		List<Attribute> result = new ArrayList<>();
		result.add(AttributeFactory.createAttribute("start", pointAdapter.getAttributes(entity.getStart())));
		result.add(AttributeFactory.createAttribute("end", pointAdapter.getAttributes(entity.getEnd())));

		return result;
	}

    @Override
    public Edge getEntity(Collection<Attribute> attributes) {
        Optional<Attribute> startA = AttributeContainer.findAttribute(attributes, "start");
        Optional<Attribute> endA = AttributeContainer.findAttribute(attributes, "end");
        Point start = startA.isPresent() ? AttributeContainer.getEntity(pointAdapter, startA.get()) : null;
        Point end = endA.isPresent() ? AttributeContainer.getEntity(pointAdapter, endA.get()) : null;

        return Edge.of(start, end);
    }
}
