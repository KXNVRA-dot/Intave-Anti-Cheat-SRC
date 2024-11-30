// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.mcver;

public interface VersionCFR
{
    iBlockData getBlockData();
    
    default String getName() {
        return "v1_null_null";
    }
    
    default boolean isAllowedToFly(final Object player) {
        return false;
    }
    
    default boolean isAllowedToPassBlocks(final Object player) {
        return false;
    }
    
    default boolean shouldBeExludedFrom(final Object player) {
        return false;
    }
}
