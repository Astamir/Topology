package ru.etu.astamir.serialization;

import java.util.*;

/**
 * Created by Astamir on 03.03.14.
 */
public class ComplexAttribute implements Attribute {
    private String name = String.valueOf(System.identityHashCode(this));

    private List<Attribute> attributes = new ArrayList<>();

    public ComplexAttribute(String name, Collection<Attribute> attributes) {
        this.name = name;
        this.attributes = new ArrayList<>(attributes);
    }

    public ComplexAttribute(String name, Map<String, Attribute> values) {
        this(name, values.values());
    }

	public ComplexAttribute(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Attribute> getValue() {
        return attributes;
    }

    public void addAttribute(Attribute attribute) {
        attributes.add(attribute);
    }

    public void addAllAttributes(Collection<? extends Attribute> attributes) {
        this.attributes.addAll(attributes);
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public boolean isReference() {
        return false;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComplexAttribute that = (ComplexAttribute) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
