package ru.etu.astamir.compression.commands.compression;

import ru.etu.astamir.common.collections.EntitySet;
import ru.etu.astamir.compression.BorderPart;

import java.util.Map;
import java.util.Set;

/**
 * Created by astamir on 10/24/15.
 */
public class ActiveBorder {
    private BorderPart part;
    double length;

    public ActiveBorder(BorderPart part, double length) {
        this.part = part;
        this.length = length;
    }

    public static ActiveBorder of(BorderPart part, double length) {
        return new ActiveBorder(part, length);
    }

    public BorderPart getPart() {
        return part;
    }

    public double getLength() {
        return length;
    }
}
