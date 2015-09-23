package ru.etu.astamir.model.contacts;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Тип контакта. Видимо тут должны быть описаны свойства
 * типа контакта, чтобы можно было добавить новый тип.
 * Или же превратить этот класс в enum.
 */
@XmlRootElement
public enum ContactType {
    USUAL, COMPLEX, EQUIPOTENTIAL, FLAP;


    /**
     * Получить тип контакта из некоторого сокращенного названия.
     *
     * @param name
     * @return
     */
    public static ContactType forName(String name) {
        return USUAL;
    }
}
