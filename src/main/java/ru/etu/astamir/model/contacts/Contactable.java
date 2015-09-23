package ru.etu.astamir.model.contacts;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;

/**
 * Интерфейс говорит о том, что объекту можно подключиться с помощью контакта.
 * Возможно название стоит сменить.
 */
@XmlRootElement
public interface Contactable {
    /**
     * Получить контакты к данному объекту.
     * @return Список контактов к объекту.
     */
    Collection<Contact> getContacts();

    /**
     *  Добавить контакт к данному объекту. Метод должен делать
     *  тщательные проверки по валидации данного действия.
     *
     * @param contact Некоторый контакт, который нужно добавить.
     * @return true, если получилось добавить контакт.
     */
    boolean addContact(Contact contact);
}
