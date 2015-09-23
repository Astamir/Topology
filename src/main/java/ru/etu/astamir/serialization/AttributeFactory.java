package ru.etu.astamir.serialization;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Astamir on 03.03.14.
 */
public class AttributeFactory {
    public static Attribute createAttribute(String name, String value) {
        return new SimpleAttribute(name, value, false);
    }

    public static Attribute createReferenceAttribute(String name, String value) {
        return new SimpleAttribute(name, value, false, true);
    }

    public static Attribute createAttribute(String name, String value, boolean local) {
        return new SimpleAttribute(name, value, local);
    }

    public static Attribute createLocalAttribute(String name, String value) {
        return new SimpleAttribute(name, value, true);
    }

    public static ComplexAttribute createComplexAttribute(String name) {
        return new ComplexAttribute(name);
    }

    public static Attribute createAttribute(String name, Map<String, Attribute> attributes) {
        return new ComplexAttribute(name, attributes);
    }

    public static Attribute createAttribute(String name, Collection<Attribute> attributes) {
        return new ComplexAttribute(name, attributes);
    }
}
