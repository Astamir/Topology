package ru.etu.astamir.model.exceptions;

import ru.etu.astamir.model.legacy.LegacyTopologyElement;
import ru.etu.astamir.model.legacy.AbstractRegion;

/**
 * Исключение, означающее, что в какую то область пытаются добавить элемент,
 * который она не может содержать.
 */
public class UnacceptableElementException extends RuntimeException {
    private AbstractRegion region;
    private LegacyTopologyElement element;

    public UnacceptableElementException(AbstractRegion region, LegacyTopologyElement element) {
        super("Region : " + region + " does not accept this element : " + element);
    }

    public UnacceptableElementException(String message) {
        super(message);
    }

    public AbstractRegion getRegion() {
        return region;
    }

    public void setRegion(AbstractRegion region) {
        this.region = region;
    }

    public LegacyTopologyElement getElement() {
        return element;
    }

    public void setElement(LegacyTopologyElement element) {
        this.element = element;
    }
}
