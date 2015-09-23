package ru.etu.astamir.serialization;

import ru.etu.astamir.model.Entity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Artem Mon'ko
 */
public interface AttributeAdapter<E> {
	List<Attribute> getAttributes(E entity);

	E getEntity(Collection<Attribute> attributes);
}
