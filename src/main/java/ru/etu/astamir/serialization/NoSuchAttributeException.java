package ru.etu.astamir.serialization;

import com.google.common.base.Preconditions;

/**
 * @author Artem Mon'ko
 */
public class NoSuchAttributeException extends Exception {
    public NoSuchAttributeException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchAttributeException(String name) {
        super("Attribute with id: " + name + "does not exist");
    }
}
