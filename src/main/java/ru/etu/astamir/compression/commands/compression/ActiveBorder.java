package ru.etu.astamir.compression.commands.compression;

import com.google.common.primitives.Doubles;
import ru.etu.astamir.common.Utils;
import ru.etu.astamir.compression.BorderPart;

/**
 * Created by astamir on 10/24/15.
 */
public class ActiveBorder implements Comparable<ActiveBorder> {
    public static final ActiveBorder NAN = new ActiveBorder(null, Utils.LENGTH_NAN);
    private BorderPart part;
    private double length;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActiveBorder that = (ActiveBorder) o;

        if (Double.compare(that.length, length) != 0) return false;
        return !(part != null ? !part.equals(that.part) : that.part != null);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = part != null ? part.hashCode() : 0;
        temp = Double.doubleToLongBits(length);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public int compareTo(ActiveBorder o) {
        return Doubles.compare(length, o.length);
    }

    @Override
    public String toString() {
        return part.getSymbol() + ": " + length;
    }
}
