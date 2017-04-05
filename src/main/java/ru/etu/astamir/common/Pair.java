package ru.etu.astamir.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;


/**
 * A basic immutable Object pair.
 *
 * <p>
 * #ThreadSafe# if the objects are threadsafe
 * </p>
 *
 * @since Lang 3.0
 * @author Matt Benson
 * @version $Id: Pair.java 967237 2010-07-23 20:08:57Z mbenson $
 */
@XmlRootElement
public final class Pair<L, R> implements Serializable, Cloneable {
    /** Serialization version */
    private static final long serialVersionUID = 4954918890077093841L;

    /** Left object */
	@XmlElement
    public final L left;

    /** Right object */
	@XmlElement
    public final R right;

    /**
     * Create a new Pair instance.
     *
     * @param left
     * @param right
     */
    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

	Pair() {
		left = null;
		right = null;
	}

	/**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Pair<?, ?>)) {
            return false;
        }
        Pair<?, ?> other = (Pair<?, ?>) obj;

        if (left == other.left && right == other.right)
            return true;

        if (left == null || other.left == null || right == null || other.right == null)
            return false;

        return left.equals(other.left) && right.equals(other.right);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((left == null) ? 0 : left.hashCode());
        result = prime * result + ((right == null) ? 0 : right.hashCode());
        return result;
    }

    /**
     * Returns a String representation of the Pair in the form: (L,R)
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        builder.append(left);
        builder.append(",");
        builder.append(right);
        builder.append(")");
        return builder.toString();
    }

    /**
     * Static creation method for a Pair<L, R>.
     *
     * @param <L>
     * @param <R>
     * @param left
     * @param right
     * @return Pair<L, R>(left, right)
     */
    public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair<L, R>(left, right);
    }

    public static <L, R> Pair<L, R> nullPair() {
        return new Pair<>(null, null);
    }
}
