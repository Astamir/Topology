package ru.etu.astamir.model;

import com.google.common.base.Preconditions;

import javax.xml.bind.annotation.XmlID;
import java.io.Serializable;

/**
 * Некая сущность, с уникальным именем в рамках типа объекта(class).
 */
public abstract class Entity implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    @XmlID
    protected String name;

    private String description;

    protected Entity() {
        this.name = String.valueOf(System.identityHashCode(this));
    }

    protected Entity(String name) {
        this.name = name;        
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Preconditions.checkNotNull(name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError("cant happen, since we're clonable");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entity entity = (Entity) o;

        if (name != null ? !name.equals(entity.name) : entity.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
