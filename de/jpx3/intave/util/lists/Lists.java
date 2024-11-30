// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.lists;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

public final class Lists
{
    public static <E> List<E> newArrayList(final E... elements) {
        final int capacity = computeArrayListCapacity(elements.length);
        final List<E> list = new ArrayList<E>(capacity);
        Collections.addAll(list, elements);
        return list;
    }
    
    static int computeArrayListCapacity(final int arraySize) {
        return saturatedCast(5L + arraySize + arraySize / 10);
    }
    
    public static int saturatedCast(final long value) {
        return (value > 2147483647L) ? Integer.MAX_VALUE : ((value < -2147483648L) ? Integer.MIN_VALUE : ((int)value));
    }
}
