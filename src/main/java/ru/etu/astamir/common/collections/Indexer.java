package ru.etu.astamir.common.collections;


import java.io.Serializable;

/**
 * A strategy that distinguishes and identifies elements in an {@link IndexedSet} and {@link IndexedMap}.
 * The <b>Indexer</b> defines 3 equivalent ways to identify elements:
 * <ul>
 * <li> <b>by value</b> - mandatory and primary method to identify element by itself
 * <li> <b>by object key</b> - optional method that identifies elements using object key
 * <li> <b>by number key</b> - optional method that identifies elements using number key
 * </ul>
 * <p>
 * The <b>Indexer</b> is not restricted to use <b>explicit key</b> concept for identification.
 * Identity of an element may be defined by a number of attributes, specified in a value itself,
 * in a template object, in a formal key object, or encoded in a number key. The <b>Indexer</b>
 * may use all these ways interchangeable to distinguish and identify elements.
 * <p>
 * Being a strategy, the <b>Indexer</b> is required to be stateless, concurrent and thread-safe.
 * <p>
 * The <b>Indexer</b> is an abstract class with a sole abstract method that shall be implemented
 * in order to use identification using explicit object key - no other methods are required to be
 * overridden in such simple cases. There is another abstract class - {@link NumberKeyIndexer} -
 * which is similarly designed with sole abstract method to simplify identification using explicit
 * number key. The third similar class - {@link IdentityIndexer} - is a similar extension of basic
 * <b>Indexer</b> for cases when explicit object keys must be compared by reference rather than
 * using their {@link Object#equals(Object) equals} method.
 * <p>
 * The <b>Indexer</b> itself is not <b>serializable</b>. However, concrete subclasses
 * shall be <b>serializable</b> in order to support serialization of indexed set and map.
 */
public abstract class Indexer<K, V> {

    /**
     * Default strategy that treats values as their own keys <b>(key&nbsp;==&nbsp;value)</b> and delegates to
     * {@link Object#hashCode() Object.hashCode()} and {@link Object#equals(Object) Object.equals(Object)}
     * methods as appropriate. This strategy does not support primitive keys.
     * <p>
     * This strategy basically turns {@link IndexedSet} into plain hash set of objects and {@link IndexedMap}
     * into a self-reference mapping.
     */
    @SuppressWarnings("rawtypes")
    public static final Indexer DEFAULT = new DefaultIndexer();

    // ========== Instance API ==========

    /**
     * Sole constructor; for invocation by subclass constructors, typically implicit.
     * <p>
     * This implementation does nothing.
     */
    protected Indexer() {}


    // ========== Value Operations ==========

    /**
     * Returns hash code for specified value; called when performing value-based operations, including <b>rehash</b>.
     * <p>
     * This implementation delegates to <code>{@link #hashCodeByKey(Object) hashCodeByKey}({@link #getObjectKey(Object) getObjectKey}(value))</code> expression.
     */
    public int hashCodeByValue(V value) {
        return hashCodeByKey(getObjectKey(value));
    }

    /**
     * Determines if specified new value matches specified old value; called when performing value-based operations.
     * <p>
     * This implementation delegates to <code>{@link #matchesByKey(Object, Object) matchesByKey}({@link #getObjectKey(Object) getObjectKey}(new_value),&nbsp;old_value)</code> expression.
     */
    public boolean matchesByValue(V new_value, V old_value) {
        return matchesByKey(getObjectKey(new_value), old_value);
    }


    // ========== Object Key Operations ==========

    /**
     * Returns object key for specified value to be used for hashing and identification;
     * called when explicit object key is needed or when other methods delegate operations as specified.
     *
     * @throws UnsupportedOperationException if this strategy does not support object keys.
     */
    public abstract K getObjectKey(V value);

    /**
     * Returns hash code for specified object key; called when performing operations using object keys.
     * <p>
     * This implementation delegates to <code>(key&nbsp;==&nbsp;null&nbsp;?&nbsp;0&nbsp;:&nbsp;key.{@link Object#hashCode() hashCode}())</code> expression.
     *
     * @throws UnsupportedOperationException if this strategy does not support object keys.
     */
    public int hashCodeByKey(K key) {
        return key == null ? 0 : key.hashCode();
    }

    /**
     * Determines if specified object key matches specified value; called when performing operations using object keys.
     * <p>
     * This implementation delegates to <code>(key&nbsp;==&nbsp;null&nbsp;?&nbsp;{@link #getObjectKey(Object) getObjectKey}(value)&nbsp;==&nbsp;null&nbsp;:&nbsp;key.{@link Object#equals(Object) equals}({@link #getObjectKey(Object) getObjectKey}(value)))</code> expression.
     *
     * @throws UnsupportedOperationException if this strategy does not support object keys.
     */
    public boolean matchesByKey(K key, V value) {
        return key == null ? getObjectKey(value) == null : key.equals(getObjectKey(value));
    }


    // ========== Number Key Operations ==========

    /**
     * Returns number key for specified value to be used for hashing and identification;
     * called when explicit number key is needed or when other methods delegate operations as specified.
     * <p>
     * This implementation throws {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException if this strategy does not support number keys.
     */
    public long getNumberKey(V value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns hash code for specified number key; called when performing operations using number keys.
     * <p>
     * This implementation throws {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException if this strategy does not support number keys.
     */
    public int hashCodeByKey(long key) {
        throw new UnsupportedOperationException();
    }

    /**
     * Determines if specified number key matches specified value; called when performing operations using number keys.
     * <p>
     * This implementation throws {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException if this strategy does not support number keys.
     */
    public boolean matchesByKey(long key, V value) {
        throw new UnsupportedOperationException();
    }


    // ========== Standard Subclasses ==========

    /**
     * Default strategy that treats values as their own keys <b>(key&nbsp;==&nbsp;value)</b>.
     */
    @SuppressWarnings("rawtypes")
    private static final class DefaultIndexer extends Indexer implements Serializable {
        private static final long serialVersionUID = 0;

        DefaultIndexer() {}

        @Override
        public Object getObjectKey(Object value) {
            return value;
        }

        @SuppressWarnings("ReadResolveAndWriteReplaceProtected")
        public Object readResolve() {
            return DEFAULT;
        }
    }
}
