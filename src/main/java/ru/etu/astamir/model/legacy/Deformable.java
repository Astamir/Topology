package ru.etu.astamir.model.legacy;

import com.google.common.collect.ImmutableList;
import ru.etu.astamir.compression.Border;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.Movable;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.technology.Technology;

import java.util.List;

/**
 * Интерфейс говорит о том, что элемент топологии можно деформировать в процессе
 * сжатия или каким-ти иным способом. Деформирование в данном случае - это изменение
 * формы элемента или его геометрических характеристик, а точнее - увеличение кол-ва
 * элементов из которых состоит данный объект.
 */
public interface Deformable extends Movable {
    /**
     * Получение элементов этого деформируемого объекта.
     * @return Последовательность кусочков объекта.
     */
    List<Edged> parts();

    /**
     * Получить кол-во кусочков данного объекта.
     */
    int size();

    /**
     * Деформация объекта.
     *
     * @param point Точка деформации(излома).
     * @param direction Направление деформации(вдавливания).
     * @param half Половина деформации. Определяет какой из двух образовавшихся кусков будет двигаться.
     * @param width Величина деформации.
     */
    void deform(Point point, Direction direction, Direction half, double width);

    /**
     * Выпрямление шины.
     */
    void straighten(List<TopologyElement> elements, Border border, Direction direction, Technology.TechnologicalCharacteristics technology);
}
