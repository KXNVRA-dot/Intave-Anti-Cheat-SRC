// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.api.external;

import org.bukkit.entity.Player;

public interface TelicCheckMirror
{
    String getName();
    
    String getCategory();
    
    int getViolationLevelOf(final Player p0);
    
    boolean isEnabled();
    
    @Deprecated
    void enable();
    
    @Deprecated
    void disable();
}
