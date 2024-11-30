// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.combat;

import org.bukkit.event.EventHandler;
import de.jpx3.intave.util.objectable.Checkable;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import de.jpx3.intave.antipiracy.IIUA;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import de.jpx3.intave.util.enums.CheatCategory;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.event.Listener;
import de.jpx3.intave.util.objectable.IntaveCheck;

public final class ClickSpeedCheck extends IntaveCheck implements Listener
{
    private final IntavePlugin plugin;
    
    public ClickSpeedCheck(final IntavePlugin plugin) {
        super("ClickSpeed", CheatCategory.COMBAT);
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this.plugin);
    }
    
    @EventHandler
    public void on(final PlayerInteractEvent e) {
        if (!e.getAction().equals((Object)Action.LEFT_CLICK_AIR)) {
            return;
        }
        final Player p = e.getPlayer();
        final long lastCPSCounterResett = IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastCPSCounterResett;
        int ccts = this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().clickCounter;
        final int lccts = this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().clickCounter_last;
        final int maxCPS = this.plugin.getConfig().getInt(this.getConfigPath() + ".maximum_cps");
        if (ccts > 200) {
            p.kickPlayer("Clicking too fast");
            return;
        }
        if (lastCPSCounterResett > 1000L) {
            if (p.getItemInHand() != null && p.getItemInHand().getType().equals((Object)Material.FISHING_ROD)) {
                ccts /= 2;
            }
            if (ccts > maxCPS && this.isActivated() && this.plugin.getRetributionManager().markPlayer(p, 10, "ClickSpeed", CheatCategory.COMBAT, "made " + ccts + " hits in a second.")) {
                e.setCancelled(true);
            }
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().clickCounter_last = this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().clickCounter;
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().clickCounter = 0;
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastCPSCounterResett = IIUA.getCurrentTimeMillis();
        }
        final Checkable.CheckableMeta.SyncedValues syncedValues = this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues();
        ++syncedValues.clickCounter;
    }
    
    @EventHandler
    public void on2(final PlayerInteractEvent e) {
        if (!this.isActivated()) {
            return;
        }
        final Player p = e.getPlayer();
        final int maxCPS = this.plugin.getConfig().getInt(this.getConfigPath() + ".maximum_cps");
        if (e.getAction().equals((Object)Action.LEFT_CLICK_AIR) && maxCPS <= 16) {
            if (this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getHeuristicValues().lastHitsTimestamps.size() < 5) {
                this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getHeuristicValues().lastHitsTimestamps.add(IIUA.getCurrentTimeMillis());
            }
            else {
                this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getHeuristicValues().lastHitsTimestamps.remove(0);
                this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getHeuristicValues().lastHitsTimestamps.add(IIUA.getCurrentTimeMillis());
            }
            if (this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getHeuristicValues().lastHitsTimestamps.size() >= 5) {
                if (p.getItemInHand() != null && p.getItemInHand().getType().equals((Object)Material.FISHING_ROD)) {
                    return;
                }
                final long diff = this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getHeuristicValues().lastHitsTimestamps.get(4) - this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getHeuristicValues().lastHitsTimestamps.get(0);
                if ((diff == 200L || diff < 20L) && this.isActivated() && this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getPing() < 100) {
                    if ((IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getHeuristicValues().lastBLiDFlag < 10000L & this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getHeuristicValues().lastBLiDFlag > 0L) && this.plugin.getRetributionManager().markPlayer(p, 1, "ClickSpeed", CheatCategory.COMBAT, "clicking suspiciously fast")) {
                        e.setCancelled(true);
                    }
                    this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getHeuristicValues().lastBLiDFlag = IIUA.getCurrentTimeMillis();
                    return;
                }
                if (diff < 200L) {
                    e.setCancelled(true);
                }
            }
        }
    }
}
