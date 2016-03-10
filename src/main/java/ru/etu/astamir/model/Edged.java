package ru.etu.astamir.model;

import ru.etu.astamir.geom.common.Edge;

/**
 * Вспомгательный интефейс. Говорит от том, что элемент имеет осевую линию, т.е.
 * элемент двухточечный.
 */
public interface Edged {
    /**
     * Получить осевую линию элемента.
     *
     * @return Осевая линия элемента.
     */
    Edge getAxis();
}
