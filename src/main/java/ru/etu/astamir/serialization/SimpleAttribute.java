package ru.etu.astamir.serialization;

import java.util.Map;

/**
 * @author Artem Mon'ko
 */
public class SimpleAttribute implements Attribute{
	String name;

    String value;

    boolean local;

    boolean isReference;

    public SimpleAttribute(String name, String value, boolean local, boolean isReference) {
        this.name = name;
        this.value = value;
        this.local = local;
        this.isReference = isReference;
    }

    public SimpleAttribute(String name, String value, boolean local) {
        this.name = name;
        this.value = value;
        this.local = local;
    }

    public static Attribute of(String name, String value) {
        return new SimpleAttribute(name, value, false);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public boolean isLocal() {
        return local;
    }

    public boolean isReference() {
        return isReference;
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    public void setReference(boolean isReference) {
        this.isReference = isReference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleAttribute that = (SimpleAttribute) o;

        if (local != that.local) return false;
        if (!name.equals(that.name)) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (local ? 1 : 0);
        return result;
    }
}
