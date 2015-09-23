package ru.etu.astamir.model;

import com.google.common.primitives.Ints;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Топологический слой. По сути определяется материалом, но и
 * не только. У нас бывают всякие металлы нижние и металлы верхние.
 * По идее, мы не можем программно менять параметры слоев, а одлжны
 * их получать из некоторый фабрики, ну или что-то заданное пользователем.
 *
 */
@XmlRootElement
public class TopologyLayer implements Comparable<TopologyLayer>, Serializable, Cloneable {

    public static final int ORDINARY_LAYER = 0;

    public static final int CONTACT_LAYER = 1;

    public static final int METAL_LAYER = 2;

    public static final int ACTIVE_REGION_LAYER = 3;

    public static final int POLYSILICON_LAYER = 4;

    /**
     * Материал слоя.
     */
	@XmlAttribute
    private Material material;

    /**
     * Название слоя.
     */
	@XmlAttribute
    private String name;

    /**
     * Номер слоя. Должна быть таблица номеров слоев.
     */
	@XmlAttribute
    private int number;

    /**
     * Префикс слоя.
     */
	@XmlAttribute
    private String prefix;

    public TopologyLayer(Material material, String name, int number, String prefix) {
        this.material = material;
        this.name = name;
        this.number = number;
        this.prefix = prefix;
    }

    private TopologyLayer() {
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TopologyLayer that = (TopologyLayer) o;

        if (material != that.material) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = material != null ? material.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public TopologyLayer clone() {
        try {
            TopologyLayer clone = (TopologyLayer) super.clone();
            clone.setMaterial(material);
            clone.setName(name);
            clone.setNumber(number);
            clone.setPrefix(prefix);

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError("Can't happen since we are cloneable");
        }
    }

    @Override
    public int compareTo(TopologyLayer o) {
        if (o == null) {
            return -1;
        }

        return Ints.compare(number, o.number);
    }

    @Override
    public String toString() {
        return prefix;
    }
}
