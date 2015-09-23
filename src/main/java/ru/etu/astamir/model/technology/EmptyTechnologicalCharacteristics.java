package ru.etu.astamir.model.technology;

import java.util.HashMap;
import java.util.Map;

/**
 * "Пустая" технология. Возвращает 0 на все методы. Использовать, когда мы не знаем технологию или нам все равно.
 */
public class EmptyTechnologicalCharacteristics implements Technology.TechnologicalCharacteristics {
    @Override
    public double getMinDistance(String one, String another) {
        return 0;
    }

    @Override
    public double getMinWidth(String key) {
        return 0;
    }

    @Override
    public double getMinHeight(String key) {
        return 0;
    }

    @Override
    public double getOverlap(String one, String another) {
        return 0;
    }

    @Override
    public double getInclude(String one, String another) {
        return 0;
    }

    @Override
    public Map<String, Double> getSymbols() {
        return new HashMap<>();
    }
}
