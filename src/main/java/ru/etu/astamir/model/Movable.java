package ru.etu.astamir.model;

import ru.etu.astamir.geom.common.Direction;

/**
 * Интерфейс говорит о том, что элемент топологии может перемещаться
 * по топологии. За перемещением должна следить виртуальная сетка, т.к.
 * при перемещении объекта могут нарушится проектные нормы или электрические
 * характеристики схемы.
 */
public interface Movable {
    /**
     * Перемещение объекта с заданным смещением по двум координатам.
     *
     * @param dx Смещение по оси абсцисс.
     * @param dy Смещение по оси ординат.
     * @return true, если получилось сместить объект.
     */
    boolean move(double dx, double dy);
}
