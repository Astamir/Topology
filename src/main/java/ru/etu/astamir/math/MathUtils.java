package ru.etu.astamir.math;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: astamir
 * Date: 11/12/12
 * Time: 12:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class MathUtils {
    public static final double EPS = 5.0E-8D;
    private static final double STANDARD_PRECISION_MULTIPLIER = 1.0E7D;
    public static final int MAX_PRECISION = 7;

    public static int compare(double a, double b) {
        return compare(a, b, EPS);
    }

    public static int compare(double a, double b, double eps) {
        return a > b + eps?1:(a < b - eps?-1:(Double.isNaN(a)?1:0) - (Double.isNaN(b)?1:0));
    }

    public static boolean haveDifferentSigns(double a, double b) {
        return isNegative(a) && isPositive(b) || isPositive(a) && isNegative(b);
    }

    public static boolean isZero(double a) {
        return compare(a, 0.0D, EPS) == 0;
    }

    public static boolean isPositive(double a) {
        return compare(a, 0.0D, EPS) > 0;
    }

    public static boolean isNegative(double a) {
        return compare(a, 0.0D, EPS) < 0;
    }

    public static boolean equals(double a, double b) {
        return compare(a, b) == 0;
    }

    public static double round(double v) {
        return roundByPrecisionMultiplier(v, STANDARD_PRECISION_MULTIPLIER);
    }

    public static double round3(double v) {
        return round(v, 3);
    }

    public static double round6(double v) {
        return round(v, 6);
    }

    public static double round(double value, int precision) {
        switch(precision) {
            case 0:
                return roundByPrecisionMultiplier(value, 1.0D);
            case 1:
                return roundByPrecisionMultiplier(value, 10.0D);
            case 2:
                return roundByPrecisionMultiplier(value, 100.0D);
            case 3:
                return roundByPrecisionMultiplier(value, 1000.0D);
            case 4:
                return roundByPrecisionMultiplier(value, 10000.0D);
            case 5:
                return roundByPrecisionMultiplier(value, 100000.0D);
            case 6:
                return roundByPrecisionMultiplier(value, 1000000.0D);
            case 7:
                return roundByPrecisionMultiplier(value, STANDARD_PRECISION_MULTIPLIER);
            default:
                return roundByPrecisionMultiplier(value, Math.pow(10.0D, (double)precision));
        }
    }

    private static double roundByPrecisionMultiplier(double value, double precisionMultiplier) {
        return Math.floor(value * precisionMultiplier + 0.5D) / precisionMultiplier;
    }

    public static int getPrecision(double v) {
        for(int i = 0; i < MAX_PRECISION; ++i) {
            if(equals(v, Math.floor(v + 0.5D))) {
                return i;
            }

            v *= 10.0D;
        }

        return MAX_PRECISION;
    }

    public static int compare(Comparable c1, Comparable c2) {
        return c1 == c2?0:(c1 == null?-1:(c2 == null?1:c1.compareTo(c2)));
    }

    public static boolean equals(Object o1, Object o2) {
        return o1 == o2 || o1 != null && o2 != null && o1.equals(o2);
    }

    public static int hashCode(Object o) {
        return o == null?0:o.hashCode();
    }

    public static int hashCodeForLong(long value) {
        return (int)(value ^ value >>> 32);
    }

    private MathUtils() {}
}
