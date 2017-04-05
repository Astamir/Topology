package ru.etu.astamir.common.collections;

import com.google.common.collect.*;
import com.google.common.primitives.Doubles;
import ru.etu.astamir.common.Pair;
import ru.etu.astamir.common.Pairs;
import ru.etu.astamir.common.Utils;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.exceptions.UnexpectedException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
        return elements.stream()
                .map(Utils.Transformers.NAME_FUNCTION)
                .collect(Collectors.toList());
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
        for (ListIterator<V> i = result.listIterator(); i.hasNext(); ) {
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
        for (int i = 0; i < Utils.fact(objects.size()); i++) {
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
        return indices.stream()
                .map(objects::get)
                .collect(Collectors.toList());
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

        Table<C, R, V> transposeTable = Tables.transpose(table);
        if (transposeTable.contains(rowKey, columnKey)) {
            return transposeTable.get(rowKey, columnKey);
        }

        return transposeTable.get(columnKey, rowKey);
    }

    public static <V> Collection<V> filterNullElements(Iterable<V> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static <V> List<List<V>> divideEdgedElements(List<V> list, final Function<V, Edge> toEdges, final Direction dir) {
        list.sort((o1, o2) -> dir.getEdgeComparator().compare(toEdges.apply(o1), toEdges.apply(o2)));

        Multimap<Double, V> map = TreeMultimap.create((o1, o2) -> dir.getDirectionSign() * Doubles.compare(o1, o2), (o1, o2) -> 1);
        for (V element : list) {
            Edge edge = toEdges.apply(element);
            double key = dir.isLeftOrRight() ? edge.getStart().x() : edge.getStart().y();
            map.put(key, element);
        }


        return map.keySet().stream()
                .map(i -> {
                    List<V> column = Lists.newArrayList();
                    column.addAll(map.get(i));
                    return column;
                })
                .collect(Collectors.toList());
    }
}
