package ru.etu.astamir.model.technology;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Artem Mon'ko
 */
public class CMOSTechnology implements Technology, Serializable {
    private String name;
    private String description = "";
    private double precision;
    private TechnologicalCharacteristics characteristics;

    public CMOSTechnology(String name, String description, double precision, TechnologicalCharacteristics characteristics) {
        this.name = name;
        this.description = description;
        this.precision = precision;
        this.characteristics = characteristics;
    }

    public CMOSTechnology(String name, TechnologicalCharacteristics characteristics) {
        this(name, "", 0.0, characteristics);
    }

    public CMOSTechnology(String name) {
        this(name, new DefaultTechnologicalCharacteristics());
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public void setCharacteristics(TechnologicalCharacteristics characteristics) {
        this.characteristics = characteristics;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public TechnologicalCharacteristics getCharacteristics() {
        return characteristics;
    }

    @Override
    public double getPrecision() {
        return precision;
    }

}
