// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.event;

import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.util.objectable.Checkable;
import org.bukkit.util.Vector;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class CheckableVelocityEvent extends iIntaveInternalEvent implements Cancellable
{
    private final Player player;
    private Vector velocity;
    private Checkable checkable;
    private boolean cancelled;
    
    public CheckableVelocityEvent(final Player player, final Vector velocity) {
        this.checkable = null;
        this.cancelled = false;
        this.player = player;
        this.velocity = velocity;
    }
    
    public final Checkable getCheckable() {
        if (this.checkable == null) {
            this.checkable = IntavePlugin.getStaticReference().catchCheckable(this.player.getUniqueId());
        }
        return this.checkable;
    }
    
    public final Player getBukkitPlayer() {
        return this.player;
    }
    
    public final Vector getVelocity() {
        return this.velocity;
    }
    
    public final void renewEvent(final Vector vector) {
        this.velocity = vector;
        this.cancelled = false;
    }
    
    public final boolean isCancelled() {
        return this.cancelled;
    }
    
    public final void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
}
