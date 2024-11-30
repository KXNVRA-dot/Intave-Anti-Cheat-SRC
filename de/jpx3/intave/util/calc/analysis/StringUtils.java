// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.calc.analysis;

import java.util.Objects;
import org.bukkit.ChatColor;

public class StringUtils
{
    public static String saveReplace(final String needle, final String haystack, final String toreplace) {
        return haystack.contains(needle) ? haystack.replace(needle, toreplace) : haystack;
    }
    
    public static double equalization(final String string, final String string2) {
        final char[] cA1 = string.trim().toCharArray();
        final char[] cA2 = string2.trim().toCharArray();
        double equalized = 0.0;
        final double length = Math.min(cA1.length, cA2.length);
        if (cA1.length < 1 || cA2.length < 1) {
            return 0.0;
        }
        for (int i = 0; i < length; ++i) {
            if (cA1[i] == cA2[i]) {
                ++equalized;
            }
        }
        return equalized / length;
    }
    
    public static String getOnlyChars(final String string) {
        String string2 = string;
        for (int i = 0; i < string2.length(); ++i) {
            if (!Character.isLetter(string2.charAt(i))) {
                string2 = string.replace(string2.charAt(i), ' ');
            }
        }
        return string2;
    }
    
    public static String getLastColorCodeIn(String string) {
        string = ChatColor.translateAlternateColorCodes('&', string);
        final String defaultColorCode = "f";
        final int lastIndex = string.lastIndexOf("§");
        return (lastIndex < 0 || lastIndex > string.length()) ? "f" : string.substring(lastIndex + 1, lastIndex + 2).replace("§", "").trim();
    }
    
    public static String replaceString(final String source, final String match, final String replace) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(replace);
        Objects.requireNonNull(match);
        return match.equals(replace) ? source : replaceString(source, match, replace, 30, 0, source.length());
    }
    
    private static String replaceString(final String source, final String match, final String replace, int additionalSize, final int startPos, final int endPos) {
        Objects.requireNonNull(source);
        final char match2 = match.charAt(0);
        final int matchLength = match.length();
        if (matchLength == 1 && replace.length() == 1) {
            return source.replace(match2, replace.charAt(0));
        }
        if (matchLength >= replace.length()) {
            additionalSize = 0;
        }
        final int sourceLength = source.length();
        final int lastMatch = endPos - matchLength;
        final StringBuilder sb = new StringBuilder(sourceLength + additionalSize);
        if (startPos > 0) {
            sb.append(source, 0, startPos);
        }
        for (int i = startPos; i < sourceLength; ++i) {
            final char sourceChar = source.charAt(i);
            if (i > lastMatch || sourceChar != match2) {
                sb.append(sourceChar);
            }
            else {
                boolean isMatch = true;
                int sourceMatchPos = i;
                for (int j = 1; j < matchLength; ++j) {
                    ++sourceMatchPos;
                    if (source.charAt(sourceMatchPos) != match.charAt(j)) {
                        isMatch = false;
                        break;
                    }
                }
                if (isMatch) {
                    i = i + matchLength - 1;
                    sb.append(replace);
                }
                else {
                    sb.append(sourceChar);
                }
            }
        }
        return sb.toString();
    }
}
