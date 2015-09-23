package ru.etu.astamir.model.legacy;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.io.File;

/**
 * Класс, расчитывающий минимально возможную длину между
 * различными элементами топологии. Он должен кушать какой-то
 * проперти файлик и пихать его в мозги.
 */
public class LegacyDistanceCharacteristics {

    private static Table<Class<? extends LegacyTopologyElement>, Class<? extends LegacyTopologyElement>, Double> distancies =
            HashBasedTable.create();

    private static File distanciesFile = new File("distances.properties");

    private static LegacyDistanceCharacteristics instance;

    private static double k = 5;

    // TODO пробегаться по списку классов и создавать всевозможные штуки. а потом заменить
    static {
        distancies.put(LegacyTransistorActiveRegion.class, LegacyContact.class, 5.0 * k);

        distancies.put(LegacyContact.class, LegacyContact.class, 7.0 * k);

        distancies.put(LegacyTransistorActiveRegion.class, LegacyGate.class, 8.0 * k);

        distancies.put(LegacyContact.class, LegacyGate.class, 6.0 * k);

        distancies.put(LegacyTransistorActiveRegion.class, Transistor.class, 10.0 * k);

        distancies.put(LegacyTransistorActiveRegion.class, LegacyTransistorActiveRegion.class, 1.0 * k);

        distancies.put(LegacyContact.class, Transistor.class, 7.0 * k);

        distancies.put(LegacyTransistorActiveRegion.class, Bus.class, 2.5 * k);

        distancies.put(LegacyContact.class, Bus.class, 10.0 * k);

        distancies.put(Bus.class, Bus.class, 5.0 * k);

        distancies.put(Bus.class, LegacyGate.class, 4.0 * k);

        distancies.put(LegacyGate.class, LegacyGate.class, 2.0 * k);

      //  distancies.put(TransistorActiveRegion.class, Bus.BusPart.class, 10.0 * k);

      //  distancies.put(LegacyContact.class, Bus.BusPart.class, 7.0 * k);

       // distancies.put(Bus.BusPart.class, Bus.BusPart.class, 2.0 * k);

       // distancies.put(Bus.BusPart.class, LegacyGate.class, 2.0 * k);

        distancies.put(LegacyGate.class, LegacyGate.class, 2.0 * k);

        distancies.put(Transistor.class, LegacyGate.class, 2.0 * k);

       // distancies.put(Bus.class, Bus.BusPart.class, 2.0 * k);
    }

    /**
     *  Убиваем конструктор по умолчанию.
     */
    private LegacyDistanceCharacteristics() {
    }

    public static LegacyDistanceCharacteristics getInstance() {
        if (instance == null) {
            instance = new LegacyDistanceCharacteristics();
        }

        return instance;
    }

    public static void init() {
        // Читаем файлик, если не нашли, надо загрузить какие-нибудь
        // осмысленные значения по умолчанию.
    }

    public double getMinDistance(Class<? extends LegacyTopologyElement> one,
                                        Class<? extends LegacyTopologyElement> another) {

        Double strict = distancies.get(one, another);
        return strict != null ? strict : distancies.get(another, one);
    }

    private void loadDefaults() {

    }
}
