/******************************************************************
 * File:        UniqueExtendedIterator.java
 * Created by:  Dave Reynolds
 * Created on:  28-Jan-2003
 * 
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: UniqueExtendedIterator.java,v 1.2 2009/08/08 11:25:31 andy_seaborne Exp $
 *****************************************************************/
package ru.etu.astamir.common.collections;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A variant on the closable/extended iterator that filters out
 * duplicate values. There is one complication that the value
 * which filtering is done on might not be the actual value
 * to be returned by the iterator. 
 * 
 * @param <E> type
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2009/08/08 11:25:31 $
 */
public class UniqueIterator<E> extends UnmodifiableIterator<E> {
    private final Set<E> seen = Sets.newHashSet();
    
    private E next;
    
    private final Iterator<E> base;
    
    
    public UniqueIterator(Iterator<E> base) {
        this.base = Preconditions.checkNotNull(base);
    }
    
    private void ensureHasNext() {
        if (!hasNext())
            throw new NoSuchElementException();
    }

    @Override
    public E next() {
        ensureHasNext();
        E result = next;
        next = null;
        return result;
    }
    
    @Override 
    public boolean hasNext() {
        while (next == null && base.hasNext()) next = nextIfNew();
        return next != null;
    }
    
    private E nextIfNew() {
        E value = base.next();
        return seen.add(value) ? value : null;
    }
    
    /**
     * Fabric method
     * @param base
     * @return
     */
    public static <N> UniqueIterator<N> create(Iterator<N> base) {
        return new UniqueIterator<N>(base);
    }
    
    /**
     * Fabric method
     * @param base
     * @return
     */
    public static <N> UniqueIterator<N> create(Iterable<N> base) {
        return new UniqueIterator<N>(base.iterator());
    }
}
