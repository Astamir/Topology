package ru.etu.astamir.common;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

    public static <L, R> boolean containsPair(List<Pair<L, R>> pairs, final Pair<L, R> pair) {
        return Iterables.any(pairs, new Predicate<Pair<L, R>>() {
            @Override
            public boolean apply(Pair<L, R> input) {
                return input.right.equals(pair.right) && input.left.equals(pair.left);
            }
        });
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
        Arrays.sort(pairs, new ComparatorHelper2<F, S>().getComparator(order));
    }

    public static <F extends Comparable<? super F>, S extends Comparable<? super S>> void sort(
            Pair<F, S>[] pairs, Order order) {
        Arrays.sort(pairs, new ComparatorHelper<F, S>().getComparator(order));
    }

    public static <F extends Comparable<F>, S extends Comparable<S>> void sort(
            List<Pair<F, S>> pairs) {
        Pairs.sort(pairs, Order.FirstByIncrease);
    }

    public static <F extends Comparable<F>, S extends Comparable<? super S>> void sort(
            List<Pair<F, S>> pairs, Order order) {
        Collections.sort(pairs, new ComparatorHelper<F, S>().getComparator(order));
    }

    public static <F extends Comparable<F>, S extends Comparable<S>> void sortList(
            List<Pair<F, S>> pairs) {
        Pairs.sortList(pairs, Order.FirstByIncrease);
    }

    public static <F extends Comparable<F>, S extends Comparable<? super S>> void sortList(
            List<Pair<F, S>> pairs, Order order) {
        Collections.sort(pairs, new ComparatorHelper2<F, S>().getComparator(order));
    }
    
    public static <F extends Comparable<F>, S extends Comparable<? super S>> F min(List<Pair<F, S>> pairs) {
        if (pairs.isEmpty()) {
            return null;
        }
        
        return Collections.min(pairs, new ComparatorHelper<F, S>().getComparator(Order.SecondByIncrease)).left;
    }

    private static class ComparatorHelper
            <F extends Comparable<? super F>, S extends Comparable<? super S>> {

        Comparator<Pair<F, S>> getComparator(Order order) {
            switch (order) {
                case FirstByIncrease:
                    return new Comparator<Pair<F, S>>() {
                        @Override
                        public int compare(Pair<F, S> o1, Pair<F, S> o2) {
                            return o1.left.compareTo(o2.left);
                        }
                    };
                case FirstByDecrease:
                    return new Comparator<Pair<F, S>>() {
                        @Override
                        public int compare(Pair<F, S> o1, Pair<F, S> o2) {
                            return o2.left.compareTo(o1.left);
                        }
                    };
                case SecondByIncrease:
                    return new Comparator<Pair<F, S>>() {
                        @Override
                        public int compare(Pair<F, S> o1, Pair<F, S> o2) {
                            return o1.right.compareTo(o2.right);
                        }
                    };

                case SecondByDecrease:
                    return new Comparator<Pair<F, S>>() {
                        @Override
                        public int compare(Pair<F, S> o1, Pair<F, S> o2) {
                            return o2.right.compareTo(o1.right);
                        }
                    };

                default:
                    throw new RuntimeException();
            }
        }
    }

    private static class ComparatorHelper2
            <F extends Comparable<? super F>, S extends Comparable<? super S>> {

        Comparator<Pair<F, S>> getComparator(Order order) {
            switch (order) {
                case FirstByIncrease:
                    return new Comparator<Pair<F, S>>() {
                        @Override
                        public int compare(Pair<F, S> o1, Pair<F, S> o2) {
                            return o1.left.compareTo(o2.left);
                        }
                    };
                case FirstByDecrease:
                    return new Comparator<Pair<F, S>>() {
                        @Override
                        public int compare(Pair<F, S> o1,
                                           Pair<F, S> o2) {
                            return o2.left.compareTo(o1.left);
                        }
                    };
                case SecondByIncrease:
                    return new Comparator<Pair<F, S>>() {
                        @Override
                        public int compare(Pair<F, S> o1,
                                           Pair<F, S> o2) {
                            return o1.right.compareTo(o2.right);
                        }
                    };

                case SecondByDecrease:
                    return new Comparator<Pair<F, S>>() {
                        @Override
                        public int compare(Pair<F, S> o1,
                                           Pair<F, S> o2) {
                            return o2.right.compareTo(o1.right);
                        }
                    };

                default:
                    throw new RuntimeException();
            }
        }
    }

}
