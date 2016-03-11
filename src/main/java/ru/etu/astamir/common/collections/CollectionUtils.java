package ru.etu.astamir.common.collections;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.*;
import com.google.common.primitives.Doubles;
import ru.etu.astamir.common.Utils;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.model.Entity;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.common.Pair;
import ru.etu.astamir.common.Pairs;
import ru.etu.astamir.model.exceptions.UnexpectedException;
import ru.etu.astamir.model.regions.ActiveRegion;
import ru.etu.astamir.model.wires.SimpleWire;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Artem Mon'ko
 */
public class CollectionUtils {
    public static <V extends Cloneable> Collection<V> clone(Collection<V> collection, Class<V> type) {
        List<V> cloneList = new ArrayList<>();
        try {
            Method cloneMethod = type.getMethod("clone");
            cloneMethod.setAccessible(true);
            for (V element : collection) {
                cloneList.add((V) cloneMethod.invoke(element));
            }
        } catch (NoSuchMethodException e) {
            throw new UnexpectedException("We have no method clone in " + type.getSimpleName(), e);
        } catch (InvocationTargetException e) {
            throw new UnexpectedException("Should not happen, but it might i guess", e);
        } catch (IllegalAccessException e) {
            throw new UnexpectedException("this is weird since we made that method accessible", e);
        }

        return cloneList;
    }

    public static <V> Collection<V> cast(Collection collection, Class<V> clazz) {
        List<V> result = new ArrayList<>();
        for (Object element : collection) {
            if (clazz.isInstance(element)) {
                result.add((V) element);
            }
        }

        return result;
    }

    public static <V extends TopologyElement> Collection<String> toNames(Collection<V> elements) {
        return Collections2.transform(elements, Utils.Functions.NAME_FUNCTION);
    }

    public static <V extends Comparable<V>> List<Pair<V, V>> getAllUniquePairs(List<V> items) {
        List<Pair<V, V>> uniquePairs = Lists.newArrayList();
        for (V upperItem : items) {
            for (V lowerItem : items) {
                Pair<V, V> pair = Pair.of(upperItem, lowerItem);
                if (!Pairs.containsPair(uniquePairs, pair)) {
                    uniquePairs.add(pair);
                }
            }
        }

        return uniquePairs;
    }

    public static <V> List<? extends V> removeIdenticalSequences(List<? extends V> list) {
        List<V> result = Lists.newArrayList(list);
        for (ListIterator<V> i = result.listIterator(); i.hasNext();) {
            V part = i.next();

            while (i.hasNext()) {
                V next = i.next();
                if (next.equals(part)) {
                    i.remove();
                } else {
                    i.previous();
                    break;
                }
            }
        }

        return result;
    }


    public static <V> List<List<V>> getPermutations(List<V> objects) {
        List<List<V>> result = Lists.newArrayList();
        List<Integer> curPermutation = firstPermutation(objects.size());
        for (int i = 0; i < fact(objects.size()); i++) {
            result.add(listByIndices(objects, curPermutation));
            curPermutation = nextPermutation(curPermutation);
        }

        return result;
    }

    private static List<Integer> firstPermutation(int size) {
        List<Integer> result = Lists.newArrayList();
        for (int i = 0; i < size; i++) {
            result.add(i);
        }

        return result;
    }

    private static <V> List<V> listByIndices(List<V> objects, List<Integer> indices) {
        List<V> result = Lists.newArrayList();
        for (int index : indices) {
            result.add(objects.get(index));
        }

        return result;
    }

    public static int fact(int n) {
        int fact = 1;
        for (int i = 1; i <= n; i++) {
            fact *= i;
        }

        return fact;
    }


    private static List<Integer> nextPermutation(List<Integer> currentPermutation) {
        List<Integer> nextPermutation = Lists.newArrayList(currentPermutation);
        int size = nextPermutation.size();
        if (size > 0) {
            int min = nextPermutation.get(size - 1);
            for (int i = size - 2; i >= 0; i--) {
                int cur = nextPermutation.get(i);
                if (cur > min) {
                    min = cur;
                } else {
                    for (int k = size - 1; k >= 0; k--) {
                        int toSwapWithCur = nextPermutation.get(k);
                        if (toSwapWithCur > cur) {
                            Collections.swap(nextPermutation, i, k);
                            break;
                        }
                    }

                    List<Integer> toSort = Lists.newArrayList(nextPermutation.subList(i + 1, size - 1));
                    nextPermutation.removeAll(toSort);
                    Collections.sort(toSort);
                    nextPermutation.addAll(toSort);

                    break;
                }
            }
        }

        return nextPermutation;
    }

    public static <R, C, V> V getMirrorFromTable(Table<R, C, V> table, R rowKey, C columnKey) {
        if (table.contains(rowKey, columnKey)) {
            return table.get(rowKey, columnKey);
        } else if (table.contains(columnKey, rowKey)) {
            return table.get(columnKey, rowKey);
        }

        Table<C, R, V> transpose_table = Tables.transpose(table);
        if (transpose_table.contains(rowKey, columnKey)) {
            return transpose_table.get(rowKey, columnKey);
        }

        return transpose_table.get(columnKey, rowKey);
    }

    public static <V> Collection<V> filterNullElements(Iterable<V> iterable) {
        return Lists.newArrayList(Iterables.filter(iterable, Predicates.notNull()));
    }

    public static <V> List<List<V>> divideEdgedElements(List<V> list, final Function<V, Edge> to_edges, final Direction dir) {
        Collections.sort(list, new Comparator<V>() {
            @Override
            public int compare(V o1, V o2) {
                return dir.getEdgeComparator().compare(to_edges.apply(o1), to_edges.apply(o2));
            }
        });

        Multimap<Double, V> map = TreeMultimap.create(new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
                return dir.getDirectionSign() * Doubles.compare(o1, o2);
            }
        }, new Comparator<V>() {
            @Override
            public int compare(V o1, V o2) {
                return 1;
            }
        });
        for (V element : list) {
            Edge edge = to_edges.apply(element);
            double key = dir.isLeftOrRight() ? edge.getStart().x() : edge.getStart().y();
            map.put(key, element);
        }

        List<List<V>> result = Lists.newArrayList();
        for (Double i : map.keySet()) {
            List<V> column = Lists.newArrayList();
            column.addAll(map.get(i));
            result.add(column);
        }

        return result;
    }
}
