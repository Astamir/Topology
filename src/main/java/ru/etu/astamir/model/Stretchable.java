package ru.etu.astamir.model;

import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Point;

/**
 * Элемент, который может растягиваться.
 * @author Artem Mon'ko
 */
public interface Stretchable {

    /**
     * Stretch the element directly by the given length in the given direction.
     * Stretch point is the point which will change. (the other point is fixed)
     * @param direction Direction in which element will be stretched
     * @param stretchPoint point which will change after the stretch
     * @return true if the stretch was successful
     */
    boolean stretch(Direction direction, double length, Point stretchPoint);
}
