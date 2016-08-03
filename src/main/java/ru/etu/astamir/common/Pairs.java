package ru.etu.astamir.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Утилитный класс для работы с парами.
 */
public final class Pairs {
    private Pairs() {

    }

    /**
     * Последовательность, в которой необходимо отсортировать.
     *
     */
    public enum Order {
        FirstByIncrease, FirstByDecrease, SecondByIncrease, SecondByDecrease
    }

    private static final Predicate<Pair> CONTAINS_PREDICATE = pair -> pair.right.equals(pair.right) && pair.left.equals(pair.left);

    public static <L, R> boolean containsPair(List<Pair<L, R>> pairs, final Pair<L, R> pair) {
        return pairs.stream().anyMatch(CONTAINS_PREDICATE);
    }

    @SuppressWarnings("unchecked")
    public static <F, S> Pair<F, S>[] newArray(int length) {
        return new Pair[length];
    }

    public static <F extends Comparable<? super F>, S extends Comparable<? super S>> void sort(
            Pair<F, S>[] pairs) {
        Pairs.sort(pairs, Order.FirstByIncrease);
    }

    public static <F extends Comparable<? super F>, S extends Comparable<? super S>> void sortArray(
            Pair<F, S>[] pairs) {
        Pairs.sortArray(pairs, Order.FirstByIncrease);
    }

    public static <F extends Comparable<? super F>, S extends Comparable<? super S>> void sortArray(
            Pair<F, S>[] pairs, Order order) {
        Arrays.sort(pairs, getComparator(order));
    }

    public static <F extends Comparable<? super F>, S extends Comparable<? super S>> void sort(
            Pair<F, S>[] pairs, Order order) {
        Arrays.sort(pairs, getComparator(order));
    }

    public static <F extends Comparable<F>, S extends Comparable<S>> void sort(
            List<Pair<F, S>> pairs) {
        Pairs.sort(pairs, Order.FirstByIncrease);
    }

    public static <F extends Comparable<F>, S extends Comparable<? super S>> void sort(
            List<Pair<F, S>> pairs, Order order) {
        Collections.sort(pairs, getComparator(order));
    }

    public static <F extends Comparable<F>, S extends Comparable<S>> void sortList(
            List<Pair<F, S>> pairs) {
        Pairs.sortList(pairs, Order.FirstByIncrease);
    }

    public static <F extends Comparable<F>, S extends Comparable<? super S>> void sortList(
            List<Pair<F, S>> pairs, Order order) {
        Collections.sort(pairs, getComparator(order));
    }
    
    public static <F extends Comparable<F>, S extends Comparable<? super S>> F min(List<Pair<F, S>> pairs) {
        if (pairs.isEmpty()) {
            return null;
        }
        
        return Collections.min(pairs, getComparator(Order.SecondByIncrease)).left;
    }

    static <F extends Comparable<? super F>, S extends Comparable<? super S>> Comparator<Pair<F, S>> getComparator(Order order) {
        switch (order) {
            case FirstByIncrease:
                return (o1, o2) -> o1.left.compareTo(o2.left);
            case FirstByDecrease:
                return (o1, o2) -> o2.left.compareTo(o1.left);
            case SecondByIncrease:
                return (o1, o2) -> o1.right.compareTo(o2.right);
            case SecondByDecrease:
                return (o1, o2) -> o2.right.compareTo(o1.right);

            default:
                throw new RuntimeException();
        }
    }
}
