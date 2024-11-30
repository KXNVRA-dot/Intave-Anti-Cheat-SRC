// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.api.internal.system;

import java.io.File;

public final class ServerInformation
{
    public static int getAvailableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }
    
    public static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }
    
    public static long getMaxAvailableMemory() {
        return Runtime.getRuntime().totalMemory();
    }
    
    public static long getAllowedThreadSize() {
        return Runtime.getRuntime().availableProcessors();
    }
    
    public static long getFreeFileSpace() {
        final File[] roots = File.listRoots();
        long f = 0L;
        for (final File root : roots) {
            f += root.getFreeSpace();
        }
        return f;
    }
    
    public static long getTotalFileSpace() {
        final File[] roots = File.listRoots();
        long f = 0L;
        for (final File root : roots) {
            f += root.getTotalSpace();
        }
        return f;
    }
}
