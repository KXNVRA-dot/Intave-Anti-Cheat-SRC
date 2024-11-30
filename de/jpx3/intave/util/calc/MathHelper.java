// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.calc;

import java.util.Arrays;
import java.util.List;
import org.bukkit.util.Vector;
import java.math.RoundingMode;
import java.math.BigDecimal;

public final class MathHelper
{
    public static double amount(final double f) {
        return diff(f, 0.0);
    }
    
    public static double diff(final double g, final double q) {
        return (g > q) ? (g - q) : (q - g);
    }
    
    public static float diff(final float g, final float q) {
        return (g > q) ? (g - q) : (q - g);
    }
    
    public static double minmax(final double value, final double min, final double max) {
        return Math.min(max, Math.max(min, value));
    }
    
    public static int minmax(final int value, final int min, final int max) {
        return Math.min(max, Math.max(min, value));
    }
    
    public static long minmax(final long value, final long min, final long max) {
        return Math.min(max, Math.max(min, value));
    }
    
    public static String roundFromDouble(final double d, int length) {
        --length;
        return BigDecimal.valueOf(d).setScale(length, RoundingMode.HALF_UP).toPlainString();
    }
    
    public static String shortVector(final Vector vector, String trenner) {
        trenner += " ";
        final int numeric_lenght = 5;
        return roundFromDouble(vector.getX(), 5) + trenner + roundFromDouble(vector.getY(), 5) + trenner + roundFromDouble(vector.getZ(), 5);
    }
    
    public static String getPercentUsing(final double hundredpercent, final double div, final int splitafter) {
        String percent = "0.00%";
        if (hundredpercent <= 0.0) {
            return percent;
        }
        final double first = div / hundredpercent;
        final double end = first * 100.0;
        percent = "" + end;
        return (splitafter != 0) ? ((percent.length() >= splitafter) ? (percent.substring(0, splitafter) + "") : (percent + "")) : (percent + "");
    }
    
    public static double map(final double currentValue, final double min, final double max, final double min2, final double max2) {
        return (currentValue - min) / (max - min) * (max2 - min2) + min2;
    }
    
    public static double[] fromList(final List<Double> doubles) {
        final double[] target = new double[doubles.size()];
        Arrays.setAll(target, doubles::get);
        return target;
    }
    
    public static boolean isBetween(final double number, final double d1, final double d2) {
        return number > d1 && number < d2;
    }
    
    private double getAllIn(final List<Float> list) {
        return list.stream().mapToDouble(value -> value).sum();
    }
}
