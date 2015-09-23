package ru.etu.astamir.model.technology;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import ru.etu.astamir.model.Entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Интерфейс описывающий технологические нормы или проще говоря технологию.
 *
 * @author Astamir
 */
public interface Technology {
    /**
     * Название технологии
     * @return
     */
    String getName();

    /**
     * Описание технологии.
     * @return
     */
    String getDescription();

    /**
     * Получить информацию о минимальных расстояниях между элементами.
     * @return
     */
    TechnologicalCharacteristics getCharacteristics();

    /**
     * Масштаб или точность топологии в метрах. например 1.2мкм = 1.2e-6.
     * (200, 100) = (200*precision, 100*precision) реальные координаты.
     * @return
     */
    double getPrecision();



    /**
     * Интерфейс, определяющий минимально возможную длину между
     * различными элементами топологии.
     */
    public interface TechnologicalCharacteristics {
        /**
         * Получить минимально допустимое расстояние между заданными типами элементов.
         *
         * @param one Типа одного элемента
         * @param another Тип второго элемента.
         *
         * @return Минимально допустимое расстояние.
         */
        double getMinDistance(String one, String another);

        /**
         * Получить минимальную ширину элемента. Для симметричных элементов ширина равна высоте.
         *
         * @param key ключ элемента.
         *
         * @return минимальная ширина.
         */
        double getMinWidth(String key);

        double getMinHeight(String key);

        double getOverlap(String one, String another);

        double getInclude(String one, String another);

        Map<String, Double> getSymbols();

        public abstract class Base implements TechnologicalCharacteristics, Serializable {
            protected Table<String, String, Double> distances = HashBasedTable.create();
            protected Table<String, String, Double> overlaps = HashBasedTable.create();
            protected Table<String, String, Double> includes = HashBasedTable.create();
            protected Map<String, Double> widths = new HashMap<>();
            protected Map<String, Double> heights = new HashMap<>();
            protected Map<String, Double> alternativeWidths = Maps.newHashMap();
            protected Map<String, Double> alternativeHeights = Maps.newHashMap();
            protected Map<String, Double> symbols = Maps.newHashMap();
            protected Map<String, String> other = Maps.newHashMap();


            public Table<String, String, Double> getDistances() {
               return distances;
            }

            public Table<String, String, Double> getOverlaps() {
                return overlaps;
            }

            public Table<String, String, Double> getIncludes() {
                return includes;
            }

            public Map<String, Double> getWidths() {
                return widths;
            }

            public Map<String, Double> getHeights() {
                return heights;
            }

            public Map<String, Double> getAlternativeWidths() {
                return alternativeWidths;
            }

            public Map<String, Double> getAlternativeHeights() {
                return alternativeHeights;
            }

            public Map<String, Double> getSymbols() {
                return symbols;
            }

            public Map<String, String> getOther() {
                return other;
            }

            public void setDistances(Table<String, String, Double> distances) {
                this.distances = distances;
            }

            public void setOverlaps(Table<String, String, Double> overlaps) {
                this.overlaps = overlaps;
            }

            public void setIncludes(Table<String, String, Double> includes) {
                this.includes = includes;
            }

            public void setWidths(Map<String, Double> widths) {
                this.widths = widths;
            }

            public void setHeights(Map<String, Double> heights) {
                this.heights = heights;
            }

            public void setAlternativeWidths(Map<String, Double> alternativeWidths) {
                this.alternativeWidths = alternativeWidths;
            }

            public void setAlternativeHeights(Map<String, Double> alternativeHeights) {
                this.alternativeHeights = alternativeHeights;
            }

            public void setSymbols(Map<String, Double> symbols) {
                this.symbols = symbols;
            }

            public void setOther(Map<String, String> other) {
                this.other = other;
            }
        }
    }
}
