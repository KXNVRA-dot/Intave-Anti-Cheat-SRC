// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.event;

import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.util.objectable.Checkable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class CheckableDamageEntityEvent extends iIntaveInternalEvent implements Cancellable
{
    private boolean isCancelled;
    private Player damager;
    private Entity entity;
    
    public CheckableDamageEntityEvent(final Player damager, final Entity entity) {
        this.damager = damager;
        this.entity = entity;
        this.isCancelled = false;
    }
    
    public Player getBukkitPlayer() {
        return this.damager;
    }
    
    public Checkable getCheckable() {
        return IntavePlugin.getStaticReference().catchCheckable(this.damager.getUniqueId());
    }
    
    public Entity getEntity() {
        return this.entity;
    }
    
    public void renew(final Player damager, final Entity entity) {
        this.damager = damager;
        this.entity = entity;
        this.setCancelled(false);
    }
    
    public final boolean isCancelled() {
        return this.isCancelled;
    }
    
    public final void setCancelled(final boolean cancelled) {
        this.isCancelled = cancelled;
    }
}
