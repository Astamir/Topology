package ru.etu.astamir.model.exceptions;

/**
 * Неверная длинна какого-то протяженного элемента.
 */
public class IllegalLengthException extends RuntimeException {
    public IllegalLengthException(String message) {
        super(message);
    }

    public IllegalLengthException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalLengthException(Throwable cause) {
        super(cause);
    }

    public IllegalLengthException() {
        super();
    }
}
