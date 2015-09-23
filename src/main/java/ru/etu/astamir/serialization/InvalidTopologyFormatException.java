package ru.etu.astamir.serialization;

/**
 * Паническое исключение на тот случай если совсем не получилось распознать файл с топологией.
 */
public class InvalidTopologyFormatException extends Exception {
    public InvalidTopologyFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidTopologyFormatException(String message) {
        super(message);
    }

    public InvalidTopologyFormatException() {
        super("Unknown topology format");
    }
}
