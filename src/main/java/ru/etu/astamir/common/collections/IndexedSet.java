package ru.etu.astamir.common.collections;

/*
 * QDS - Quick Data Signalling Library
 * Copyright (C) 2002-2013 Devexperts LLC
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;

/**
 * A collection that contains no duplicate elements according to specified identification strategy.
 * The <b>IndexedSet</b> class implements and obeys general contracts of {@link java.util.Set Set} interface
 * and provides additional benefits over standard implementations:
 * <ul>
 * <li> delegation of element identification to external strategy
 * <li> concurrent asynchronous read access
 * <li> smaller memory footprint and faster performance
 * </ul>
 * <p>
 * The <b>IndexedSet</b> assumes that identity of an element can be represented by a variable number
 * of attributes, therefore it delegates identification to an external strategy - the {@link Indexer}.
 * In order to fulfil contracts of {@link java.util.Map Map} interface and for convenience, the <b>IndexedSet</b>
 * supports concept of <b>explicit key</b> object and also numeric key representation, but these
 * identification means are optional and need not be supported by all strategies.
 * <p>
 * <b>Note that the IndexedSet is not synchronized!</b> Concurrent modifications of <b>IndexedSet</b>
 * from multiple threads must be synchronized externally to preserve data integrity.
 * On the other side, the <b>IndexedSet</b> fully supports concurrent asynchronous read operations,
 * which works during concurrent modification by other thread. In case of concurrent modification
 * each atomic read sees <b>IndexedSet</b> either before or after each atomic write operation.
 * <p>
 * The <b>IndexedSet</b> does not support <b>null</b> values, but it supports <b>null</b> keys
 * if they are supported by corresponding {@link Indexer}. The <b>IndexedSet</b> can be serialized
 * if all elements and the {@link Indexer} are serializable.
 */
public class IndexedSet<K, V> extends AbstractConcurrentSet<V> implements Cloneable, Serializable {
    private static final long serialVersionUID = 0;

    // 'private' fields and methods of IndexedSet class shall be accessed only from within IndexedSet class itself.

    private final Indexer<K, ? super V> indexer;
    private transient volatile Core<K, V> core;

    // ========== Construction and Sizing Operations ==========

    /**
     * Creates new empty set with default indexer {@link Indexer#DEFAULT} and default initial capacity.
     */
    public IndexedSet() {
        this(0);
    }

    /**
     * Creates new empty set with default indexer {@link Indexer#DEFAULT} and specified initial capacity.
     */
    @SuppressWarnings("unchecked")
    public IndexedSet(int initial_capacity) {
        this(Indexer.DEFAULT, initial_capacity);
    }

    /**
     * Creates new empty set with specified indexer and default initial capacity.
     */
    public IndexedSet(Indexer<K, ? super V> indexer) {
        this(indexer, 0);
    }

    /**
     * Creates new empty set with specified indexer and specified initial capacity.
     */
    @SuppressWarnings("unchecked")
    public IndexedSet(Indexer<K, ? super V> indexer, int initial_capacity) {
        if (indexer == null)
            throw new NullPointerException("Indexer is null.");
        this.indexer = indexer;
        this.core = initial_capacity <= 0 ? (Core<K, V>)Core.EMPTY_CORE : new Core(indexer, initial_capacity, GOLDEN_RATIO); // Atomic volatile write.
    }

    /**
     * Creates a new set containing the elements in the specified collection.
     * If specified collection is an {@link IndexedSet}, then new indexed set uses same indexer,
     * otherwise it uses default indexer {@link Indexer#DEFAULT}.
     */
    @SuppressWarnings("unchecked")
    public IndexedSet(Collection<V> c) {
        this(c instanceof IndexedSet ? ((IndexedSet)c).getIndexer() : Indexer.DEFAULT, c);
    }

    /**
     * Creates a new set with specified indexer containing the elements in the specified collection.
     */
    public IndexedSet(Indexer<K, ? super V> indexer, Collection<? extends V> c) {
        this(indexer, c.size());
        addAll(c);
    }

    /**
     * Creates a new set with default indexer containing specified elements.
     */
    public static <V> IndexedSet<V, V> of(V... objs) {
        return new IndexedSet<V, V>(Arrays.asList(objs));
    }

    /**
     * Returns a shallow copy of this set - the values themselves are not cloned.
     */
    public IndexedSet<K, V> clone() {
        try {
            IndexedSet<K, V> result = (IndexedSet<K, V>)super.clone();
            if (result.core != Core.EMPTY_CORE)
                result.core = new Core<K, V>(result.core);
            return result;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    /**
     * Increases the capacity of this set instance, if necessary, to ensure that it
     * can hold at least the number of elements specified by the capacity argument.
     */
    public void ensureCapacity(int capacity) {
        core = core.ensureCapacity(indexer, capacity); // Atomic volatile read and write.
    }

    /**
     * Trims the capacity of this set instance to be the set's current size.
     * An application can use this operation to minimize the storage of this set instance.
     */
    public void trimToSize() {
        core = core.trimToSize(indexer); // Atomic volatile read and write.
    }

    /**
     * Removes all elements from this set.
     */
    @Override
    public void clear() {
        core = core.clear(); // Atomic volatile read and write.
    }

    // ========== Query Operations ==========

    /**
     * Returns indexer used to distinguish and identify elements in this set.
     */
    public Indexer<K, ? super V> getIndexer() {
        return indexer;
    }

    /**
     * Returns the number of elements in this set.
     */
    public int size() {
        return core.size(); // Atomic volatile read.
    }

    /**
     * Returns the element from this set which matches specified value or <b>null</b> if none were found.
     */
    public V getByValue(V value) {
        return core.getByValue(value); // Atomic volatile read.
    }

    /**
     * Returns the element from this set which matches specified key or <b>null</b> if none were found.
     */
    public V getByKey(K key) {
        return core.getByKey(key); // Atomic volatile read.
    }

    /**
     * Returns the element from this set which matches specified key or <b>null</b> if none were found.
     */
    public V getByKey(long key) {
        return core.getByKey(key); // Atomic volatile read.
    }

    /**
     * Returns <b>true</b> if this set contains element which matches specified value.
     * <p>
     * This implementation delegates to ({@link #getByValue(Object) getByValue(value)}&nbsp;!=&nbsp;null) expression.
     * <p>
     * Note, that unlike {@link java.util.HashSet#contains},
     * this method might throw {@link ClassCastException} if value is of the wrong class.
     *
     * @deprecated Use {@link #containsValue} to be explicit about type and intent.
     */
    @SuppressWarnings("unchecked")
    public boolean contains(Object value) {
        return getByValue((V)value) != null;
    }

    /**
     * Returns <b>true</b> if this set contains element which matches specified value.
     * <p>
     * This implementation delegates to ({@link #getByValue(Object) getByValue(value)}&nbsp;!=&nbsp;null) expression.
     */
    public boolean containsValue(V value) {
        return getByValue(value) != null;
    }

    /**
     * Returns <b>true</b> if this set contains element which matches specified key.
     * <p>
     * This implementation delegates to ({@link #getByKey(Object) getByKey(key)}&nbsp;!=&nbsp;null) expression.
     */
    public boolean containsKey(K key) {
        return getByKey(key) != null;
    }

    /**
     * Returns <b>true</b> if this set contains element which matches specified key.
     * <p>
     * This implementation delegates to ({@link #getByKey(Object) getByKey(key)}&nbsp;!=&nbsp;null) expression.
     */
    public boolean containsKey(long key) {
        return getByKey(key) != null;
    }

    /**
     * Returns an iterator over the elements in this set.
     */
    @SuppressWarnings("unchecked")
    public Iterator<V> iterator() {
        return (Iterator<V>)iterator(IndexedIterator.VALUE_FAILFAST);
    }

    /**
     * Returns an iterator over the keys of elements in this set.
     */
    @SuppressWarnings("unchecked")
    public Iterator<K> keyIterator() {
        return (Iterator<K>)iterator(IndexedIterator.KEY_FAILFAST);
    }

    /**
     * Returns an iterator over the entries in this set.
     */
    @SuppressWarnings("unchecked")
    public Iterator<Map.Entry<K, V>> entryIterator() {
        return (Iterator<Map.Entry<K,V>>)iterator(IndexedIterator.ENTRY_FAILFAST);
    }

    /**
     * Returns concurrent iterator over the elements in this set.
     */
    @SuppressWarnings("unchecked")
    public Iterator<V> concurrentIterator() {
        return (Iterator<V>)iterator(IndexedIterator.VALUE_CONCURRENT);
    }

    /**
     * Returns an array containing all of the elements in this set.
     * Obeys the general contract of the {@link Collection#toArray()} method.
     */
    @Override
    public Object[] toArray() {
        return core.toArray(null); // Atomic volatile read.
    }

    /**
     * Returns an array containing all of the elements in this set whose runtime type is that of the specified array.
     * Obeys the general contract of the {@link Collection#toArray(Object[])} method.
     */
    @Override
    public <T> T[] toArray(T[] a) {
        return core.toArray(a); // Atomic volatile read.
    }

    /**
     * Returns static structure statistics of this set.
     */
    public IndexedSetStats getStats() {
        return core.getStats();
    }

    // ========== Modification Operations ==========

    /**
     * Puts specified element into this set and returns previous element that matches specified one.
     */
    public V put(V value) {
        return putImpl(core, value); // Atomic volatile read.
    }

    /**
     * Puts specified element into this set if it is absent and
     * returns current element in the set that matches specified one.
     * This is equivalent to
     * <pre>
     *   if (set.containsValue(value)) {
     *     return set.getByValue(value);
     *   } else {
     *     set.put(value);
     *     return value;
     *   }
     * </pre>
     * except that the action is performed atomically if it is properly synchronized.
     * <p>
     * Note, that unlike {@link java.util.concurrent.ConcurrentMap#putIfAbsent},
     * this method returns specified value (not <b>null</b>) if the value was absent.
     */
    public V putIfAbsentAndGet(V value) {
        Core<K, V> core = this.core; // Atomic volatile read.
        V old_value = core.getByValue(value);
        if (old_value != null)
            return old_value;
        putImpl(core, value);
        return value;
    }

    /**
     * Adds specified element into this set and returns <b>true</b>
     * if this operation has increased the size of this set.
     * <p>
     * This implementation adds value using {@link #put(Object) put(value)} method.
     */
    public boolean add(V value) {
        return put(value) == null;
    }

    /**
     * Removes specified element from this set if it is present and returns
     * <b>true</b> if this operation has decreased the size of this set.
     * <p>
     * This implementation removes value using {@link #removeValue(Object) removeValue(value)} method.
     * <p>
     * Note, that unlike {@link java.util.HashSet#remove},
     * this method might throw {@link ClassCastException} if value is of the wrong class.
     */
    @SuppressWarnings("unchecked")
    public boolean remove(Object value) {
        return removeValue((V)value) != null;
    }

    /**
     * Removes the element from this set which matches specified value if it is present
     * and returns removed element or <b>null</b> if none were found.
     */
    public V removeValue(V value) {
        Core<K, V> core = this.core; // Atomic volatile read.
        V old_value = core.removeValue(value);
        this.core = core; // Atomic volatile write.
        return old_value;
    }

    /**
     * Removes the element from this set which matches specified key if it is present
     * and returns removed element or <b>null</b> if none were found.
     */
    public V removeKey(K key) {
        Core<K, V> core = this.core; // Atomic volatile read.
        V old_value = core.removeKey(key);
        this.core = core; // Atomic volatile write.
        return old_value;
    }

    /**
     * Removes the element from this set which matches specified key if it is present
     * and returns removed element or <b>null</b> if none were found.
     */
    public V removeKey(long key) {
        Core<K, V> core = this.core; // Atomic volatile read.
        V old_value = core.removeKey(key);
        this.core = core; // Atomic volatile write.
        return old_value;
    }


    // ========== Internal Implementation - Helper Instance Methods ==========

    private V putImpl(Core<K, V> core, V value) {
        V old_value;
        if (core.needRehash()) {
            // Rehash shall be done before put in order to move away from EMPTY_CORE and protect from bad magic.
            // However in situ replacement of existing value shall keep old mod_count and avoid rehash.
            if (core == Core.EMPTY_CORE || (old_value = core.put(value, true)) == null) {
                core = core.rehash(indexer, 0);
                old_value = core.put(value, false);
            }
        } else
            old_value = core.put(value, false);
        this.core = core; // Atomic volatile write.
        return old_value;
    }

    private Iterator<?> iterator(int type) {
        Core<K, V> core = this.core; // Atomic volatile read.
        return core.size() == 0 ? IndexedIterator.EMPTY_ITERATOR : new IndexedIterator<K, V>(this, core, type);
    }

    void checkModification(Object check_core, long check_mod_count) {
        Core<K, V> core = this.core; // Atomic volatile read.
        if (check_core != core || check_mod_count != core.getModCount())
            throw new ConcurrentModificationException();
    }

    void removeIterated(Object check_core, long check_mod_count, boolean concurrent, V last_value, int last_index) {
        Core<K, V> core = this.core; // Atomic volatile read.
        if (!concurrent && (check_core != core || check_mod_count != core.getModCount()))
            throw new ConcurrentModificationException();
        if (core.getAt(last_index) == last_value) // Atomic read.
            core.removeAt(last_index, core.getInitialIndexByValue(last_value));
        else if (concurrent)
            core.removeValue(last_value);
        else
            throw new ConcurrentModificationException();
        this.core = core; // Atomic volatile write.
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        writeCore(out);
    }

    void writeCore(ObjectOutputStream out) throws IOException {
        core.writeObjectImpl(out); // Atomic volatile read.
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        core = Core.readObjectImpl(indexer, in); // Atomic volatile write.
    }

    // ========== Internal Implementation - Core ==========

    /**
     * Core class to hold all data of {@link IndexedSet}.
     */
    private static final class Core<K, V> {
        static final int QUALITY_BASE = 6;
        static final Object REMOVED = new Object(); // Marker object for removed values.
        static final Core<?, ?> EMPTY_CORE = new Core(EmptyIndexer.EMPTY_INDEXER, 0, GOLDEN_RATIO); // Empty core for empty sets.
        static {
            EMPTY_CORE.overall_size = EMPTY_CORE.matrix.length; // Special value to trigger rehash before first 'put' operation.
        }

        // 'private' fields and methods of Core class shall be accessed only from within Core class itself.

        private final int magic;
        private final int shift;
        private final Indexer<K, ? super V> indexer;
        private final V[] matrix;

        /**
         * Quality is a complex value that tracks quality of this core's payload,
         * <code>quality = (total_distance_to_all_payload_values &lt;&lt; QUALITY_BASE) + distance_shift</code>,
         * where {@code distance_shift} is a current tolerance to the bad payload distance.
         * Note, that lower value of quality is BETTER.
         * @see #exceed(long)
         */
        private long quality;

        private int payload_size;
        private int overall_size; // payload_size + number_of_REMOVED_values

        private long mod_count; // counts structural changes when elements are added or removed (replace is Ok)
        private long amortized_cost; // total cost of amortization of all removed and rehashed values, see unamortizedCost()

        @SuppressWarnings("unchecked")
        Core(Indexer<K, ? super V> indexer, int capacity, int magic) {
            if (indexer == null)
                throw new NullPointerException("Indexer is null.");
            this.magic = magic;
            shift = getShift(capacity);
            this.indexer = indexer;
            matrix = (V[])new Object[(-1 >>> shift) + 1];
            quality = QUALITY_BASE + 1; // will rehash when avg_dist > 2, see exceed(...)
        }

        /**
         * Clones specified core. Implemented as constructor to keep {@link #matrix} field final.
         */
        Core(Core<K, V> source) {
            magic = source.magic;
            shift = source.shift;
            indexer = source.indexer;
            matrix = source.matrix.clone();
            quality = source.quality;
            payload_size = source.payload_size;
            overall_size = source.overall_size;
            mod_count = source.mod_count;
            amortized_cost = source.amortized_cost;
        }

        /**
         * Returns increment to this core's {@link #quality} that should be added when new payload
         * entry is added at position {@code index} with its initial position at {@code initial_index}.
         */
        private long qualityInc(int index, int initial_index) {
            return ((initial_index - index) & (-1 >>> shift)) << QUALITY_BASE;
        }

        /**
         * Returns true if average distance to payload values exceeds
         * <code>1 &lt;&lt; (distance_shift - QUALITY_BASE)</code>,
         * where only last {@link #QUALITY_BASE} bits of distance_shift parameter are used.
         * Thus, the method can be directly applied using {@link #quality} itself as an
         * {@code distance_shift} argument.
         */
        private boolean exceed(long distance_shift) {
            // mask total distance bits first
            return (quality & (-1L << QUALITY_BASE)) > ((long)payload_size << distance_shift);
        }

        // compute quality tolerance for new instance, so that it rehashes only when it becomes much worse than now
        private void computeTolerance() {
            while (exceed(quality))
                quality++;
            // increment to next tolerance if it is close to exceeding current tolerance
            if ((quality & (-1L << QUALITY_BASE)) * 3 > ((long)payload_size << quality) * 2)
                quality++;
        }

        /**
         * Returns the cost of all put operations to place payload into this core.
         * @see #amortized_cost
         */
        private long unamortizedCost() {
            return (quality >>> QUALITY_BASE) + payload_size;
        }

        private void putValuesIntoEmptyCore(V[] values) {
            for (int i = values.length; --i >= 0;) {
                V value = values[i]; // Atomic read.
                if (value == null || value == REMOVED)
                    continue;
                int index = getInitialIndexByValue(value);
                int initial_index = index;
                while (matrix[index] != null)
                    index = (index - 1) & (-1 >>> shift);
                matrix[index] = value;
                quality += qualityInc(index, initial_index);
                payload_size++;
                overall_size++;
                // Check if there are too many elements in the source.
                // Error may happen either if source state is broken
                // or if elements are added to source concurrently.
                // Ignoring such error here will lead to a dead loop above.
                if (overall_size > (THRESHOLD_UP >>> shift))
                    throw new ConcurrentModificationException("Concurrent modification during rehash");
            }
        }

        private Core<K, V> rehashInternal(Indexer<K, ? super V> indexer, int capacity) {
			/* GENERAL DESCRIPTION OF REHASH ALGORITHM:
			   1. Try to rehash at most 4 times. Twice at regular capacity, once at 2x capacity, and once at 4x capacity.
			   2. First attempt can keep old magic if previous distance is good enough, other attempts use random magic.
			   3. If the first attempt immediately satisfies perfect limits then return.
			   4. If we have to make additional attempts, then the best result is picked.
			      In this case result is considered acceptable if it satisfies consequently worse limits.
			   5. After four attempts the best result is returned even if it is unacceptable by the above rules.
			 */
            capacity = Math.min(Math.max(capacity, payload_size), MAX_CAPACITY);
            long total_cost = amortized_cost + unamortizedCost();
            Core<K, V> result = new Core<K, V>(indexer, capacity, exceed(QUALITY_BASE + 1) ? nextMagic(magic, capacity) : magic);
            result.putValuesIntoEmptyCore(matrix);
            total_cost += result.unamortizedCost();
            if (result.exceed(QUALITY_BASE + 1)) // only if quality is not very good
                for (int k = 0; k < 3; k++) {
                    Core<K, V> other = new Core<K, V>(indexer, capacity, nextMagic(magic, capacity));
                    other.putValuesIntoEmptyCore(matrix);
                    total_cost += other.unamortizedCost();
                    if (other.quality < result.quality) // lower quality is better
                        result = other;
                    if (!result.exceed(QUALITY_BASE + 2 + k))
                        break; // break when we have acceptable quality
                    capacity = Math.min(capacity * 2, MAX_CAPACITY);
                }
            result.computeTolerance();
            // update result stats
            result.mod_count = mod_count;
            result.amortized_cost = total_cost - result.unamortizedCost();
            return result;
        }

        Core<K, V> rehash(Indexer<K, ? super V> indexer, int capacity) {
            long mod_count = this.mod_count; // Atomic read.
            Core<K, V> result = rehashInternal(indexer, capacity);
            if (mod_count != this.mod_count) // Atomic read.
                throw new ConcurrentModificationException("Concurrent modification during rehash");
            return result;
        }

        boolean needRehash() {
            return overall_size > (THRESHOLD_UP >>> shift) || exceed(quality);
        }

        Core<K, V> rehashIfNeeded(Indexer<K, ? super V> indexer, int capacity) {
            return needRehash() ?
                    rehash(indexer, capacity) : this;
        }

        Core<K, V> ensureCapacity(Indexer<K, ? super V> indexer, int capacity) {
            return capacity > (THRESHOLD_UP >>> shift) && shift > MIN_SHIFT ?
                    rehash(indexer, capacity) : this;
        }

        Core<K, V> trimToSize(Indexer<K, ? super V> indexer) {
            return payload_size < (THRESHOLD_DOWN >>> shift) && shift < MAX_SHIFT ?
                    rehash(indexer, 0) : this;
        }

        Core<K, V> clear() {
            if (indexer == EmptyIndexer.EMPTY_INDEXER)
                return this;
            for (int i = matrix.length; --i >= 0;)
                matrix[i] = null;
            mod_count += payload_size;
            amortized_cost += unamortizedCost();
            quality = QUALITY_BASE + 1;
            payload_size = 0;
            overall_size = 0;
            return this;
        }

        int size() {
            return payload_size; // Atomic read.
        }

        long getModCount() {
            return mod_count; // Atomic read.
        }

        int getInitialIndexByValue(V value) {
            return (indexer.hashCodeByValue(value) * magic) >>> shift;
        }

        V getByValue(V value) {
            int index = getInitialIndexByValue(value);
            V test_value;
            while ((test_value = matrix[index]) != null) { // Atomic read.
                if (test_value != REMOVED && indexer.matchesByValue(value, test_value))
                    return test_value;
                index = (index - 1) & (-1 >>> shift);
            }
            return null;
        }

        V getByKey(K key) {
            int index = (indexer.hashCodeByKey(key) * magic) >>> shift;
            V test_value;
            while ((test_value = matrix[index]) != null) { // Atomic read.
                if (test_value != REMOVED && indexer.matchesByKey(key, test_value))
                    return test_value;
                index = (index - 1) & (-1 >>> shift);
            }
            return null;
        }

        V getByKey(long key) {
            int index = (indexer.hashCodeByKey(key) * magic) >>> shift;
            V test_value;
            while ((test_value = matrix[index]) != null) { // Atomic read.
                if (test_value != REMOVED && indexer.matchesByKey(key, test_value))
                    return test_value;
                index = (index - 1) & (-1 >>> shift);
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        V put(V value, boolean replaceOnly) {
            // These are sanity checks - they can be removed once testing completed.
            assert indexer != EmptyIndexer.EMPTY_INDEXER : "Putting into EMPTY core.";
            assert value != REMOVED : "Value is an internal special marker object.";

            if (value == null)
                throw new NullPointerException("Value is null.");
            int index = getInitialIndexByValue(value);
            int initial_index = index;
            int removed_index = -1;
            V test_value;
            while ((test_value = matrix[index]) != null) { // Atomic read.
                if (test_value != REMOVED && indexer.matchesByValue(value, test_value)) {
                    matrix[index] = value;
                    return test_value;
                }
                if (test_value == REMOVED && removed_index < 0)
                    removed_index = index;
                index = (index - 1) & (-1 >>> shift);
            }
            if (replaceOnly)
                return null;
            if (removed_index < 0) {
                matrix[index] = value;
                overall_size++;
            } else
                matrix[index = removed_index] = value;
            quality += qualityInc(index, initial_index);
            payload_size++;
            mod_count++;
            return null;
        }

        V removeValue(V value) {
            int index = getInitialIndexByValue(value);
            int initial_index = index;
            V test_value;
            while ((test_value = matrix[index]) != null) { // Atomic read.
                if (test_value != REMOVED && indexer.matchesByValue(value, test_value)) {
                    removeAt(index, initial_index);
                    return test_value;
                }
                index = (index - 1) & (-1 >>> shift);
            }
            return null;
        }

        V removeKey(K key) {
            int index = (indexer.hashCodeByKey(key) * magic) >>> shift;
            int initial_index = index;
            V test_value;
            while ((test_value = matrix[index]) != null) { // Atomic read.
                if (test_value != REMOVED && indexer.matchesByKey(key, test_value)) {
                    removeAt(index, initial_index);
                    return test_value;
                }
                index = (index - 1) & (-1 >>> shift);
            }
            return null;
        }

        V removeKey(long key) {
            int index = (indexer.hashCodeByKey(key) * magic) >>> shift;
            int initial_index = index;
            V test_value;
            while ((test_value = matrix[index]) != null) { // Atomic read.
                if (test_value != REMOVED && indexer.matchesByKey(key, test_value)) {
                    removeAt(index, initial_index);
                    return test_value;
                }
                index = (index - 1) & (-1 >>> shift);
            }
            return null;
        }

        int getMaxIndex() {
            return matrix.length - 1;
        }

        V getAt(int index) {
            return matrix[index];
        }

        @SuppressWarnings("unchecked")
        void removeAt(int index, int initial_index) {
            matrix[index] = (V)REMOVED;
            quality -= qualityInc(index, initial_index);
            payload_size--;
            if (matrix[(index - 1) & (-1 >>> shift)] == null)
                while (matrix[index] == REMOVED) {
                    matrix[index] = null;
                    overall_size--;
                    index = (index + 1) & (-1 >>> shift);
                }
            mod_count++;
            // we paid twice -- first adding this element, then removing it
            amortized_cost += 2 * ((qualityInc(index, initial_index) >>> QUALITY_BASE) + 1);
        }

        @SuppressWarnings("unchecked")
        <T> T[] toArray(T[] a) {
            // If (a == null) then returned array shall be of exact length, otherwise it can be larger.
            int size = payload_size; // Atomic read.
            Object[] result = a == null ? new Object[size] : a.length >= size ? a :
                    (Object[]) Array.newInstance(a.getClass().getComponentType(), size);
            int n = 0;
            for (int i = matrix.length; --i >= 0;) {
                Object value = matrix[i]; // Atomic read.
                if (value == null || value == REMOVED)
                    continue;
                if (n >= result.length) {
                    // More elements were added concurrently. Enlarge result array.
                    // Do not grow more than twice.
                    // Do not grow more than possible remaining elements (i + 1).
                    // Do not fail when (n == 0).
                    Object[] tmp = (Object[])Array.newInstance(result.getClass().getComponentType(), n + Math.min(n, i) + 1);
                    System.arraycopy(result, 0, tmp, 0, n);
                    result = tmp;
                }
                result[n++] = value;
            }
            if (n < result.length && a == null) {
                // Shrink allocated array to exact size.
                Object[] tmp = new Object[n];
                System.arraycopy(result, 0, tmp, 0, n);
                result = tmp;
            }
            if (n < result.length)
                result[n] = null;
            return (T[])result;
        }

        void writeObjectImpl(ObjectOutputStream out) throws IOException {
            int n = payload_size; // Atomic read.
            // if (n == 0) then empty set, no elements written
            // if (n > 0) then fixed set with exactly n elements written
            // if (n < 0) then dynamic set with approximately (-n) elements plus marker null element written
            out.writeInt(n);
            for (int i = matrix.length; --i >= 0;) {
                Object value = matrix[i]; // Atomic read.
                if (value != null && value != REMOVED && n-- > 0) // Do not write more than n values anyway.
                    out.writeObject(value);
            }
            if (n != 0)
                throw new IOException("Concurrent modification detected.");
        }

        @SuppressWarnings("unchecked")
        static <K, V> Core<K, V> readObjectImpl(Indexer<K, ? super V> indexer, ObjectInputStream in) throws IOException, ClassNotFoundException {
            int n = in.readInt();
            // if (n == 0) then empty set, no elements written
            // if (n > 0) then fixed set with exactly n elements written
            // if (n < 0) then dynamic set with approximately (-n) elements plus marker null element written
            if (n == 0)
                return (Core<K, V>)EMPTY_CORE;
            Core<K, V> core = new Core<K, V>(indexer, Math.abs(n), GOLDEN_RATIO);
            if (n > 0)
                for (int i = 0; i < n; i++) {
                    core = core.rehashIfNeeded(indexer, n); // to protect from bad magic
                    core.put((V)in.readObject(), false);
                }
            else
                for (V value; (value = (V)in.readObject()) != null;) {
                    core = core.rehashIfNeeded(indexer, -n); // to protect from bad magic
                    core.put(value, false);
                }
            return core;
        }

        IndexedSetStats getStats() {
            return new IndexedSetStats(payload_size, matrix.length, quality >>> QUALITY_BASE, amortized_cost + unamortizedCost(), mod_count);
        }
    }

    // ========== Internal Implementation - EmptyIndexer ==========

    /**
     * EmptyIndexer class is used to implement shared EMPTY_CORE instance.
     */
    private static final class EmptyIndexer extends Indexer<Object, Object> {
        static final Indexer<?, ?> EMPTY_INDEXER = new EmptyIndexer();

        private EmptyIndexer() {}

        @Override
        public int hashCodeByValue(Object value) {
            return 0;
        }

        @Override
        public boolean matchesByValue(Object new_value, Object old_value) {
            return false;
        }

        public Object getObjectKey(Object value) {
            return null;
        }

        @Override
        public int hashCodeByKey(Object key) {
            return 0;
        }

        @Override
        public boolean matchesByKey(Object key, Object value) {
            return false;
        }

        @Override
        public long getNumberKey(Object value) {
            return 0;
        }

        @Override
        public int hashCodeByKey(long key) {
            return 0;
        }

        @Override
        public boolean matchesByKey(long key, Object value) {
            return false;
        }
    }

    // ========== Internal Implementation - Iterator ==========

    /**
     * Asynchronous iterator over {@link IndexedSet}.
     */
    private static final class IndexedIterator<K, V> implements Iterator<Object> {
        static final int VALUE_CONCURRENT = 0;
        static final int VALUE_FAILFAST = 1;
        static final int KEY_FAILFAST = 2;
        static final int ENTRY_FAILFAST = 3;

        static final Iterator<?> EMPTY_ITERATOR = new IndexedIterator(new IndexedSet(EmptyIndexer.EMPTY_INDEXER), Core.EMPTY_CORE, VALUE_CONCURRENT);

        // 'private' fields and methods of IndexedIterator class shall be accessed only from within IndexedIterator class itself.

        private final IndexedSet<K, V> set;
        private final Core<K, V> core;
        private final int type;

        private long mod_count;

        private V next_value;
        private int next_index;
        private V last_value;
        private int last_index;

        IndexedIterator(IndexedSet<K, V> set, Core<K, V> core, int type) {
            this.set = set;
            this.core = core;
            this.type = type;
            mod_count = core.getModCount();
            next_index = core.getMaxIndex() + 1;
            fillNext();
        }

        private void fillNext() {
            if (type != VALUE_CONCURRENT)
                set.checkModification(core, mod_count);
            while (--next_index >= 0) {
                next_value = core.getAt(next_index); // Atomic read.
                if (next_value != null && next_value != Core.REMOVED)
                    return;
            }
            next_value = null; // No more elements - clear leftover state.
        }

        public boolean hasNext() {
            return next_value != null;
        }

        public Object next() {
            if (next_value == null)
                throw new NoSuchElementException();
            last_value = next_value;
            last_index = next_index;
            fillNext();
            if (type == KEY_FAILFAST)
                return set.getIndexer().getObjectKey(last_value);
            if (type == ENTRY_FAILFAST)
                return new IndexedEntry<K, V>(set, last_value);
            return last_value;
        }

        public void remove() {
            if (last_value == null)
                throw new IllegalStateException();
            set.removeIterated(core, mod_count, type == VALUE_CONCURRENT, last_value, last_index);
            mod_count = core.getModCount();
            last_value = null;
        }
    }

    // ========== Internal Implementation - Entry ==========

    /**
     * IndexedEntry class is a wrapper to convert indexed API to collections API.
     */
    private static final class IndexedEntry<K, V> implements Map.Entry<K, V> {
        // 'private' fields and methods of IndexedEntry class shall be accessed only from within IndexedEntry class itself.

        private final IndexedSet<K, V> set;
        private V value;

        IndexedEntry(IndexedSet<K, V> set, V value) {
            this.set = set;
            this.value = value;
        }

        public K getKey() {
            return set.getIndexer().getObjectKey(value);
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            if (value == null)
                throw new NullPointerException("Value is null.");
            V old_value = this.value;
            if (!set.getIndexer().matchesByValue(value, old_value))
                throw new IllegalArgumentException("New value does not match old value.");
            set.put(this.value = value);
            return old_value;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof Map.Entry))
                return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>)obj;
            Object key = getKey();
            Object ekey = e.getKey();
            return (key == null ? ekey == null : key.equals(ekey)) && value.equals(e.getValue());
        }

        public int hashCode() {
            K key = getKey();
            return (key == null ? 0 : key.hashCode()) ^ value.hashCode();
        }

        public String toString() {
            return getKey() + "=" + value;
        }
    }

    // ========== Internal Implementation - Helper Static Constants and Methods ==========

	/*
	 * This section contains constants and methods to support matrix-based data structures.
	 * Such data structures and related algorithms are also known as "direct linear probe hashing".
	 */

    static final int THRESHOLD_UP = (int)((1L << 32) * 5 / 9);
    static final int THRESHOLD_DOWN = (int)((1L << 32) * 5 / 27);

    static final int THRESHOLD_ALLOC_UP = (int)((1L << 32) * 4 / 9);
    static final int MAX_SHIFT = 29;
    static final int MIN_SHIFT = 2;
    static final int MAX_CAPACITY = THRESHOLD_ALLOC_UP >>> MIN_SHIFT;

    /**
     * Calculates appropriate 'shift' for specified capacity.
     */
    static int getShift(int capacity) {
        int shift = MAX_SHIFT;
        while ((THRESHOLD_ALLOC_UP >>> shift) < capacity && shift >= MIN_SHIFT)
            shift--;
        if (shift < MIN_SHIFT)
            throw new IllegalArgumentException("Capacity is too large: " + capacity);
        return shift;
    }

    private static final int GOLDEN_RATIO = 0x9E3779B9;
    private static final int MAGIC = 0xC96B5A35;
    private static int magic_seed = (int)(System.currentTimeMillis() * Runtime.getRuntime().freeMemory());

    /**
     * Generates next MAGIC number with proper distribution and difference of bits.
     */
    static int nextMagic(int prev_magic) {
        // Generate next pseudo-random number with lowest bit set to '1'.
        int magic = (magic_seed = magic_seed * MAGIC + 1) | 1;
        // Enforce that any 4 bits are neither '0000' nor '1111'.
        // Start earlier to enforce that highest 2 bits are neither '00' nor '11'.
        for (int i = 31; --i >= 0;) {
            int bits = (magic >> i) & 0x0F;
            if (bits == 0 || bits == 0x0F) {
                magic ^= 1 << i;
                i -= 2;
            }
        }
        // Recover cleared lowest bit.
        if ((magic & 1) == 0)
            magic ^= 3; // Convert '10' (the only possible case) into '01'.
        // Enforce that any 8 bits have at least 1 difference from previous number.
        for (int i = 25; --i >= 0;)
            if ((((magic ^ prev_magic) >> i) & 0xFF) == 0) {
                // Reverse bit i+1 and enforce that bit i+2 differs from it.
                // This may lead to 4-bit (but not longer) sequences of '0' or '1'.
                magic ^= ((magic ^ (magic << 1)) & (4 << i)) ^ (2 << i);
                i -= 6;
            }
        return magic;
    }

    // makes additional magic checks for large capacities (tries several magics for the best one)
    static int nextMagic(int prev_magic, int capacity) {
        int magic = nextMagic(prev_magic);
        if (capacity < 100)
            return magic;
        int steps = capacity < 1000 ? 10 : 15;
        long eval = evaluateContinuedFraction(magic, steps);
        for (int i = 0; i < 3; i++) {
            int m = nextMagic(prev_magic);
            long e = evaluateContinuedFraction(m, steps);
            if (e < eval) {
                magic = m;
                eval = e;
            }
        }
        return magic;
    }

    // evaluates continued fraction representation of magic to see if it has large pairs of neighbouring terms
    private static long evaluateContinuedFraction(int magic, int steps) {
        double d = (double)(magic & 0xFFFFFFFFL) / (1L << 32);
        long result = 1;
        long prev = 1;
        for (int i = 0; i < steps; i++) {
            d = 1 / d;
            long c = (long)d;
            result = Math.max(result, prev * c / (i + 1));
            prev = c;
            d -= c;
        }
        return result;
    }
}
