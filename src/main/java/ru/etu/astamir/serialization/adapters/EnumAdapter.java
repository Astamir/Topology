package ru.etu.astamir.serialization.adapters;

import ru.etu.astamir.model.exceptions.UnexpectedException;
import ru.etu.astamir.serialization.Attribute;
import ru.etu.astamir.serialization.AttributeAdapter;
import ru.etu.astamir.serialization.AttributeFactory;

import java.util.*;

/**
 * Try not to use this to often, Artem
 * @author Artem Mon'ko
 *
 */
public class EnumAdapter<V extends Enum<V>> implements AttributeAdapter<Enum<V>> {
	Class<V> clazz;

	public EnumAdapter(Class<V> clazz) {
		this.clazz = clazz;
	}

	@Override
	public List<Attribute> getAttributes(Enum<V> entity) {
		List<Attribute> attributeMap = new ArrayList<>();
		String name = entity.getClass().getSimpleName();
		attributeMap.add(AttributeFactory.createAttribute(name, entity.name()));

		return attributeMap;
	}

    @Override
    public Enum<V> getEntity(Collection<Attribute> attributes) {
        for (Attribute attribute : attributes) { // get first value
            return Enum.valueOf(clazz, (String) attribute.getValue());
        }

        return null; // if there is no value at all
    }
}
