package ru.etu.astamir.serialization.adapters;

import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.model.exceptions.UnexpectedException;
import ru.etu.astamir.model.wires.SimpleWire;
import ru.etu.astamir.model.wires.Wire;
import ru.etu.astamir.serialization.*;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Artem Mon'ko
 */
public class SimpleWireAdapter1 implements AttributeAdapter<SimpleWire> {
    EdgeAdapter edgeAdapter = new EdgeAdapter();
    PolygonAdapter polygonAdapter = new PolygonAdapter();

	@Override
	public List<Attribute> getAttributes(SimpleWire entity) {
//		Map<String, Attribute> attributes = new LinkedHashMap<>();
//        attributes.putAll(AttributeContainer.getAttributesForTopologyElement(entity));
//
//        Polygon bounds = entity.getBounds();
//        Wire wire = entity.getWire();
//
//        AttributeContainer.putAttribute(attributes, "axis", edgeAdapter.getAttributes(entity.getAxis()));
//
//        AttributeContainer.putAttribute(attributes, "index", entity.getIndex());
//
//        AttributeContainer.putAttribute(attributes, "width", entity.getWidth());
//
//        AttributeContainer.putAttribute(attributes, "widthAtBorder", entity.getWidthAtBorder());
//
//        AttributeContainer.putAttribute(attributes, "maxLength", entity.getMaxLength());
//
//        AttributeContainer.putAttribute(attributes, "stretchable", entity.isStretchable());
//        AttributeContainer.putAttribute(attributes, "movable", entity.isMovable());
//        AttributeContainer.putAttribute(attributes, "deformable", entity.isDeformable());
//        if (wire != null) {
//            AttributeContainer.putAttribute(attributes, "wire", wire.getName());
//        }
//        AttributeContainer.putAttribute(attributes, "bounds", polygonAdapter.getAttributes(bounds));

		return null;
	}

    @Override
    public SimpleWire getEntity(Collection<Attribute> attributes) {
        return null;
    }

//    @Override
//	public SimpleWire getEntity(Map<String, Attribute> attributes)  {
//        SimpleWire wire = null;
//        try {
//            String id = AttributeContainer.getAttributeValue(attributes, "id");
//            Edge axis = edgeAdapter.getEntity(AttributeContainer.getComplexAttributeValue(null, "axis"));
//            wire = new SimpleWire(id, axis);
//            AttributeContainer.setAttributesForTopologyElement(attributes, wire);
//        } catch (NoSuchAttributeException e) {
//            throw new UnexpectedException("There is no id ?", e);
//        }
//
//		return wire;
//	}
}
