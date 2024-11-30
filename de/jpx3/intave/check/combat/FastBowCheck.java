// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.combat;

import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.EventHandler;
import de.jpx3.intave.antipiracy.IIUA;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.plugin.Plugin;
import de.jpx3.intave.util.enums.CheatCategory;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.event.Listener;
import de.jpx3.intave.util.objectable.IntaveCheck;

public final class FastBowCheck extends IntaveCheck implements Listener
{
    private final IntavePlugin plugin;
    
    public FastBowCheck(final IntavePlugin plugin) {
        super("FastBow", CheatCategory.COMBAT);
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)plugin);
    }
    
    @EventHandler
    public void on(final EntityShootBowEvent e) {
        if (!this.isActivated()) {
            return;
        }
        if (e.getEntity() instanceof Player) {
            final Player p = (Player)e.getEntity();
            final double force = e.getForce();
            final long lastTimeStartedBowPulling = IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeStartedBowPulling;
            final double pullBackSpeed = force / lastTimeStartedBowPulling;
            if ((pullBackSpeed >= 0.01 || pullBackSpeed == Double.POSITIVE_INFINITY) && this.plugin.getRetributionManager().markPlayer(p, 1, "FastBow", CheatCategory.COMBAT, "pulled bow back too fast (" + force + "/" + (int)(lastTimeStartedBowPulling / 50L) + ")")) {
                e.setCancelled(true);
            }
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeShotArrow = IIUA.getCurrentTimeMillis();
        }
    }
    
    @EventHandler
    public void on(final PlayerInteractEvent e) {
        if (!this.isActivated()) {
            return;
        }
        if ((e.getAction().equals((Object)Action.RIGHT_CLICK_BLOCK) || e.getAction().equals((Object)Action.RIGHT_CLICK_AIR)) && e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getType().equals((Object)Material.BOW)) {
            this.plugin.catchCheckable(IIUA.getUUIDFrom(e.getPlayer())).getMeta().getTimedValues().lastTimeStartedBowPulling = IIUA.getCurrentTimeMillis();
        }
    }
}
