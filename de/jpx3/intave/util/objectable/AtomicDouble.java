// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.objectable;

public final class AtomicDouble
{
    private double value;
    
    public double getAndSet(final int i) {
        final double tempValue = this.value - 1.0 + 1.0;
        this.value = i;
        return tempValue;
    }
    
    public double addAndGet(final double v) {
        return this.value += v;
    }
}
