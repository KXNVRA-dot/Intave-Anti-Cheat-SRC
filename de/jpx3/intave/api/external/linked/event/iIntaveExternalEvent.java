// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.api.external.linked.event;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class iIntaveExternalEvent extends Event implements Cancellable
{
    private static final HandlerList handlers;
    private boolean cancelled;
    
    public iIntaveExternalEvent() {
        this.cancelled = false;
    }
    
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    public void setCancelled(final boolean b) {
        this.cancelled = b;
    }
    
    public HandlerList getHandlers() {
        return iIntaveExternalEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return iIntaveExternalEvent.handlers;
    }
    
    static {
        handlers = new HandlerList();
    }
}
