// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.calc;

import java.util.Collection;
import java.util.List;

public final class BalanceUtils
{
    public static double getPacketBalanceFrom(final List<Integer> arrayList) {
        return (arrayList.stream().mapToDouble(i -> i).sum() / arrayList.size() - 1.0) / 4.0;
    }
    
    public static double getSquaredBalanceFrom(final List<Integer> arrayList) {
        return arrayList.stream().mapToDouble(i -> i).sum() / arrayList.size();
    }
    
    public static double getSquaredBalanceFromLong(final List<Long> arrayList) {
        return arrayList.stream().mapToDouble(i -> i).sum() / arrayList.size();
    }
    
    public static double getSquaredBalanceFromDouble(final List<Double> arrayList) {
        return arrayList.stream().mapToDouble(i -> i).sum() / arrayList.size();
    }
    
    public static double getSquaredBalanceFromDouble(final Collection<Double> arrayList) {
        return arrayList.stream().mapToDouble(i -> i).sum() / arrayList.size();
    }
}
