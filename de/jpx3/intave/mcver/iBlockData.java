// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.mcver;

import org.bukkit.block.Block;

public interface iBlockData
{
    boolean isStepable(final Block p0);
    
    boolean isClimbable(final Block p0);
    
    boolean doesAffectMovement(final Block p0);
    
    boolean isPassable(final Block p0);
    
    boolean isLiquid(final Block p0);
    
    boolean isFrozen(final Block p0);
    
    double getYOffset(final Block p0);
}
