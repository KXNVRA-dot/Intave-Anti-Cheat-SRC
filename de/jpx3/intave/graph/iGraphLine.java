// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.graph;

import java.util.Arrays;
import de.jpx3.intave.api.external.linked.exceptions.IntaveInternalException;
import java.util.function.Consumer;
import java.util.function.Function;
import de.jpx3.intave.util.calc.MathHelper;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.awt.Color;

public final class iGraphLine
{
    private Color color;
    private Map<Integer, Double> values;
    private final List<Integer> cachedList;
    
    public iGraphLine(final Double... points) {
        this.color = Color.WHITE;
        this.values = new ConcurrentHashMap<Integer, Double>();
        this.cachedList = new ArrayList<Integer>();
        int i = 0;
        for (final double point : points) {
            this.values.put(++i, point);
        }
    }
    
    public iGraphLine(final List<Double> points) {
        this.color = Color.WHITE;
        this.values = new ConcurrentHashMap<Integer, Double>();
        this.cachedList = new ArrayList<Integer>();
        int i = 0;
        for (final double point : points) {
            this.values.put(++i, point);
        }
    }
    
    public iGraphLine(final Map<Integer, Double> values) {
        this.color = Color.WHITE;
        this.values = new ConcurrentHashMap<Integer, Double>();
        this.cachedList = new ArrayList<Integer>();
        this.values = values;
    }
    
    public final void setColor(final Color color) {
        this.color = color;
    }
    
    public final Color getColor() {
        return this.color;
    }
    
    public final List<Integer> getIndexesForValue(final double value) {
        return this.getIndexesForValue(value, 0.0);
    }
    
    public final List<Integer> getIndexesForValue(final double value, final double range) {
        this.cachedList.clear();
        this.values.entrySet().stream().filter(localD -> MathHelper.diff(localD.getValue(), value) <= range).map((Function<? super Object, ?>)Map.Entry::getKey).forEach(this.cachedList::add);
        return this.cachedList;
    }
    
    public final double getHighestValue() {
        if (this.values.isEmpty()) {
            return 0.0;
        }
        double high = 0.0;
        for (final double index : this.values.values()) {
            high = Math.max(high, index);
        }
        return high;
    }
    
    public final double getValueAt(final int index) {
        if (index > this.getHighestIndex()) {
            throw new IntaveInternalException(new IndexOutOfBoundsException("Given index is greater than the maximum index"));
        }
        if (index < this.getLowestIndex()) {
            throw new IntaveInternalException(new IndexOutOfBoundsException("Given index is smaller than the minimum index"));
        }
        if (!this.values.containsKey(index)) {
            throw new IntaveInternalException(new NullPointerException(Arrays.toString(this.values.keySet().toArray()) + " does not contain index " + index));
        }
        return this.values.get(index);
    }
    
    public final Map<Integer, Double> getMap() {
        return this.values;
    }
    
    public final int getHighestIndex() {
        int high = 0;
        for (final int index : this.values.keySet()) {
            high = Math.max(high, index);
        }
        return high;
    }
    
    public final int getLowestIndex() {
        int low = Integer.MAX_VALUE;
        for (final int index : this.values.keySet()) {
            low = Math.min(low, index);
        }
        return low;
    }
}
