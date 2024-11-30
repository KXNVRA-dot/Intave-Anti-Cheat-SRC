// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.combat;

import de.jpx3.intave.util.event.CheckableDamageEntityEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;
import de.jpx3.intave.antipiracy.IIUA;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.plugin.Plugin;
import de.jpx3.intave.util.enums.CheatCategory;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.event.Listener;
import de.jpx3.intave.util.objectable.IntaveCheck;

public final class NoSwingCheck extends IntaveCheck implements Listener
{
    private final IntavePlugin plugin;
    
    public NoSwingCheck(final IntavePlugin plugin) {
        super("NoSwing", CheatCategory.COMBAT);
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this.plugin);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final PlayerAnimationEvent e) {
        final Player p = e.getPlayer();
        this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().swungArm = true;
    }
    
    @Override
    public void onCheckableDamageEntity(final CheckableDamageEntityEvent e) {
        final Player p = e.getBukkitPlayer();
        final boolean hasSwungArm = this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().swungArm;
        if (!hasSwungArm && this.plugin.getRetributionManager().markPlayer(p, 1, "NoSwing", CheatCategory.COMBAT, "did not swing arm")) {
            e.setCancelled(true);
        }
        this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().swungArm = false;
    }
}
