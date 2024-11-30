// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.api.external.linked.event;

import org.bukkit.entity.Player;

public final class AsyncIntaveViolationEvent extends iIntaveExternalEvent
{
    private Player punished;
    private String modulename;
    private String category;
    private String message;
    private int vlBefore;
    private int vlAfter;
    
    public AsyncIntaveViolationEvent(final Player punished, final String modulename, final String category, final String message, final int vlBefore, final int vlAfter) {
        this.punished = punished;
        this.modulename = modulename;
        this.category = category;
        this.message = message;
        this.vlBefore = vlBefore;
        this.vlAfter = vlAfter;
    }
    
    public final Player getDetectedPlayer() {
        return this.punished;
    }
    
    public final String getCheckName() {
        return this.modulename;
    }
    
    public final String getCheckCategory() {
        return this.category;
    }
    
    public final String getCheckMessage() {
        return this.message;
    }
    
    public final int getAddedVL() {
        return this.vlAfter - this.vlBefore;
    }
    
    @Deprecated
    public final int getPreVL() {
        return this.vlBefore;
    }
    
    @Deprecated
    public final int getPostVL() {
        return this.vlAfter;
    }
    
    public void renew(final Player punished, final String modulename, final String category, final String message, final int vlAfter, final int vlBefore) {
        this.punished = punished;
        this.modulename = modulename;
        this.category = category;
        this.message = message;
        this.vlBefore = vlBefore;
        this.vlAfter = vlAfter;
        this.setCancelled(false);
    }
}
