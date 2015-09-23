package ru.etu.astamir.geom.common;

/**
 * Некорректный коэффициент.
 */
class IllegalCoefficientException extends RuntimeException{
    private static final long serialVersionUID = -1L;

    public double coefficient;

    public IllegalCoefficientException(double coefficient) {
        super("illegal coefficient t = " + coefficient);
        this.coefficient = coefficient;
    }

    public IllegalCoefficientException(String s) {
        super(s);
    }

    public IllegalCoefficientException(String message, Throwable cause) {
        super(message, cause);
    }
}
