// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.world;

import java.util.stream.Stream;
import org.bukkit.potion.PotionEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import de.jpx3.intave.util.calc.PlayerUtils;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.EventHandler;
import com.comphenix.protocol.utility.MinecraftVersion;
import de.jpx3.intave.api.internal.ProtocolLibAdapter;
import org.bukkit.potion.PotionEffectType;
import de.jpx3.intave.antipiracy.IIUA;
import org.bukkit.Difficulty;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.plugin.Plugin;
import de.jpx3.intave.util.enums.CheatCategory;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.event.Listener;
import de.jpx3.intave.util.objectable.IntaveCheck;

public final class InvalidAbilities extends IntaveCheck implements Listener
{
    private final IntavePlugin plugin;
    
    public InvalidAbilities(final IntavePlugin plugin) {
        super("InvalidAbilities", CheatCategory.WORLD);
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this.plugin);
    }
    
    @EventHandler
    private void on(final EntityRegainHealthEvent e) {
        if (e.getEntity() instanceof Player) {
            if (!this.isActivated() || e.getEntity().getWorld().getDifficulty() == Difficulty.PEACEFUL) {
                return;
            }
            final Player p = (Player)e.getEntity();
            final long diff = IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastRegainEvent;
            final double min = 3200.0;
            if (p.hasPotionEffect(PotionEffectType.REGENERATION)) {
                return;
            }
            if (p.hasPotionEffect(PotionEffectType.HEAL)) {
                return;
            }
            if (p.hasPotionEffect(PotionEffectType.HEALTH_BOOST)) {
                return;
            }
            if (IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastInstandHeathPotion < 100L) {
                return;
            }
            if (diff < min && !ProtocolLibAdapter.getProtocolManager().getMinecraftVersion().isAtLeast(MinecraftVersion.FROSTBURN_UPDATE) && this.plugin.getRetributionManager().markPlayer(p, (diff < 1000L) ? 4 : 1, "InvalidAbilities", CheatCategory.WORLD, "tried to regain health too fast (" + diff + "ms / 3200ms)")) {
                e.setCancelled(true);
            }
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastRegainEvent = IIUA.getCurrentTimeMillis();
        }
    }
    
    @EventHandler
    public void on(final PotionSplashEvent e) {
        e.getPotion().getEffects().stream().filter(effect -> effect.getType().equals((Object)PotionEffectType.HEAL) || effect.getType().equals((Object)PotionEffectType.HEALTH_BOOST)).flatMap(effect -> e.getAffectedEntities().stream()).filter(entity -> entity instanceof Player).forEach(entity -> this.plugin.catchCheckable(entity.getUniqueId()).getMeta().getTimedValues().lastInstandHeathPotion = IIUA.getCurrentTimeMillis());
    }
    
    @EventHandler
    public void on(final PlayerItemConsumeEvent e) {
        if (!this.isActivated()) {
            return;
        }
        final Player p = e.getPlayer();
        final long cTime = IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeRightClick;
        if (this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues().suspiciousNoSlowdownMoves > 8) {
            PlayerUtils.suspendSlot(p);
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().isUsingItem = false;
            e.setCancelled(true);
            return;
        }
        if (cTime < 1200L && this.plugin.getRetributionManager().markPlayer(p, (cTime < 500L) ? 2 : 1, "InvalidAbilities", CheatCategory.WORLD, "tried to consume item too fast (" + cTime + "ms / 1500ms)")) {
            PlayerUtils.suspendSlot(p);
            e.setCancelled(true);
        }
        this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeRightClick = IIUA.getCurrentTimeMillis();
    }
    
    @EventHandler
    public void on(final PlayerInteractEvent e) {
        if (e.getAction().equals((Object)Action.RIGHT_CLICK_BLOCK) || e.getAction().equals((Object)Action.RIGHT_CLICK_AIR)) {
            this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getTimedValues().lastTimeRightClick = IIUA.getCurrentTimeMillis();
        }
    }
}
