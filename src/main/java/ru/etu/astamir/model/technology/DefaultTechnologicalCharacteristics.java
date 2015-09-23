package ru.etu.astamir.model.technology;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import ru.etu.astamir.common.collections.CollectionUtils;
import ru.etu.astamir.model.TopologicalCell;
import ru.etu.astamir.model.exceptions.UnexpectedException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Простая реализация характеристик с помощью таблицы
 *
 * @author Astamir
 */
public class DefaultTechnologicalCharacteristics extends Technology.TechnologicalCharacteristics.Base implements Serializable {
    public DefaultTechnologicalCharacteristics() {
    }

    @Override
    public double getMinDistance(String one, String another) {
        Double distance = CollectionUtils.getMirrorFromTable(distances, one, another);

        if (distance == null) {
            return 0;//throw new UnexpectedException("no min distance for pair [" + one + ", " + another + "]");
        }

        return distance;
    }

    @Override
    public double getMinWidth(String key) {
        if (widths.containsKey(key)) {
            return widths.get(key);
        } else if (alternativeWidths.containsKey(key)) {
            return alternativeWidths.get(key);
        }

        return 0;
    }

    @Override
    public double getMinHeight(String key) {
        if (heights.containsKey(key)) {
            return heights.get(key);
        } else if (alternativeHeights.containsKey(key)) {
            return alternativeHeights.get(key);
        }

        return 0;
    }

    @Override
    public double getOverlap(String one, String another) {
        Double overlap = CollectionUtils.getMirrorFromTable(overlaps, one, another);
        return overlap != null ? overlap : 0.0;
    }

    @Override
    public double getInclude(String one, String another) {
        Double include = CollectionUtils.getMirrorFromTable(includes, one, another);
        return include != null ? include : 0.0;
    }

    public Map<String, Double> getSymbols() {
        return symbols;
    }

    public Map<String, String> getOther() {
        return other;
    }
}
