package ru.etu.astamir.serialization.adapters;

import ru.etu.astamir.model.Material;
import ru.etu.astamir.model.TopologyLayer;
import ru.etu.astamir.serialization.Attribute;
import ru.etu.astamir.serialization.AttributeAdapter;
import ru.etu.astamir.serialization.AttributeContainer;
import ru.etu.astamir.serialization.AttributeFactory;

import java.util.*;

/**
 * @author Artem Mon'ko
 */
public class TopologyLayerAdapter implements AttributeAdapter<TopologyLayer> {
    @Override
    public List<Attribute> getAttributes(TopologyLayer entity) {
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(AttributeFactory.createLocalAttribute("id", entity.getName()));
        attributes.add(AttributeFactory.createLocalAttribute("material", entity.getMaterial().name()));
        attributes.add(AttributeFactory.createLocalAttribute("type", String.valueOf(entity.getNumber())));
        attributes.add(AttributeFactory.createLocalAttribute("prefix", entity.getPrefix()));

        return attributes;
    }

    @Override
    public TopologyLayer getEntity(Collection<Attribute> attributes) {
        Optional<Attribute> name = AttributeContainer.findAttribute(attributes, "id");
        Optional<Attribute> material = AttributeContainer.findAttribute(attributes, "material");
        Optional<Attribute> type = AttributeContainer.findAttribute(attributes, "type");
        Optional<Attribute> prefix = AttributeContainer.findAttribute(attributes, "prefix");

        TopologyLayer layer = new TopologyLayer(material.isPresent() ? Material.valueOf(((String) material.get().getValue()).toUpperCase()) : Material.UNKNOWN,
                name.isPresent() ? (String) name.get().getValue() : "",
                type.isPresent() ? Integer.valueOf((String) type.get().getValue()) : -1,
                prefix.isPresent() ? (String) prefix.get().getValue() : "");

        return layer;
    }
}
