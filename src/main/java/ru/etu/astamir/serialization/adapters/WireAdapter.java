package ru.etu.astamir.serialization.adapters;

import ru.etu.astamir.model.wires.Wire;
import ru.etu.astamir.serialization.Attribute;
import ru.etu.astamir.serialization.AttributeAdapter;

import java.util.*;

/**
 * @author Artem Mon'ko
 */
public class WireAdapter implements AttributeAdapter<Wire> {
	@Override
	public List<Attribute> getAttributes(Wire entity) {
		List<Attribute> attributes = new ArrayList<>();
		return attributes;
	}

    @Override
    public Wire getEntity(Collection<Attribute> attributes) {
        return null;
    }
}
