// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.movement;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.potion.PotionEffectType;
import de.jpx3.intave.util.objectable.Checkable;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.Material;
import de.jpx3.intave.util.data.BlockData;
import java.util.concurrent.ThreadLocalRandom;
import de.jpx3.intave.antipiracy.IIUA;
import de.jpx3.intave.util.calc.LocationHelper;
import de.jpx3.intave.util.calc.PlayerUtils;
import de.jpx3.intave.util.event.CheckableMoveEvent;
import de.jpx3.intave.util.enums.CheatCategory;
import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.util.objectable.IntaveCheck;

public final class ImpossibleMovement extends IntaveCheck
{
    private final IntavePlugin plugin;
    
    public ImpossibleMovement(final IntavePlugin plugin) {
        super("ImpossibleMove", CheatCategory.MOVING);
        this.plugin = plugin;
    }
    
    @Override
    public void onCheckableMove(final CheckableMoveEvent e) {
        if (!this.isActivated()) {
            return;
        }
        if (e.isCancelled()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        if (p.isInsideVehicle()) {
            return;
        }
        if (PlayerUtils.getAllowFlight(p)) {
            return;
        }
        if (e.getCheckable().wasCurrentlyHittet()) {
            return;
        }
        if (LocationHelper.hasStepableNearby(p, e.getTo())) {
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded < 4000L) {
            return;
        }
        final double i = e.getYDirectedMotion();
        final long lastYCol = IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeCollidedWithTopBlock;
        boolean flag = false;
        if (e.getCheckable().getMeta().getVioValues().flagIMPSM && ThreadLocalRandom.current().nextBoolean()) {
            e.getCheckable().getMeta().getVioValues().flagIMPSM = false;
            flag = true;
        }
        if (i < 0.16477 && i > 0.0857 && lastYCol > 300L && !LocationHelper.hasStepableNearby(e.getTo()) && !LocationHelper.hasStepableNearby(e.getFrom())) {
            if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeTeleported < 2000L && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeFlagged > 2000L) {
                return;
            }
            if (PlayerUtils.hasLevitation(p)) {
                return;
            }
            if (IIUA.getCurrentTimeMillis() - e.getCheckable().setupTime < 5000L) {
                return;
            }
            if (BlockData.isPassable(p.getLocation().getBlock()) && !p.getLocation().getBlock().getType().equals((Object)Material.AIR)) {
                return;
            }
            if (BlockData.isPassable(p.getEyeLocation().getBlock()) && !p.getEyeLocation().getBlock().getType().equals((Object)Material.AIR)) {
                return;
            }
            if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnASlime < 4000L) {
                return;
            }
            if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastDamageTakenTimestamp < 900L) {
                return;
            }
            if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeInWater < 1000L) {
                return;
            }
            if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeUsedelytra < 3000L) {
                return;
            }
            if (LocationHelper.hasWoopableBlockNearby(e.getFrom(), (Entity)p) || LocationHelper.hasWoopableBlockNearby(e.getTo(), (Entity)p)) {
                return;
            }
            e.getCheckable().getMeta().getVioValues().flagIMPSM = true;
        }
        if (i < 0.005 && i > 2.0E-4 && IIUA.isPretendingToBeOnGround(p)) {
            flag = true;
        }
        if (flag && this.doBhqFlag(e)) {
            if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeInCobweb < 1000L) {
                return;
            }
            if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeInWater < 1000L) {
                return;
            }
            if (e.getCheckable().getMeta().getVioValues().suspiciousBunnyHops > 1) {
                e.getCheckable().getMeta().getVioValues().suspiciousBunnyHops = 2;
                if (this.plugin.getRetributionManager().markPlayer(p, 1, "ImpossibleMove", CheatCategory.MOVING, "jumped suspiciously")) {
                    e.setCancelled(true);
                }
            }
            else {
                final Checkable.CheckableMeta.ViolationValues vioValues = e.getCheckable().getMeta().getVioValues();
                ++vioValues.suspiciousBunnyHops;
            }
        }
    }
    
    private boolean doBhqFlag(final CheckableMoveEvent e) {
        return !e.getBukkitPlayer().hasPotionEffect(PotionEffectType.JUMP) && !this.isWhitelisted(e.getFrom().getBlock()) && !this.isWhitelisted(e.getTo().getBlock()) && !this.isWhitelisted(e.getFrom().getBlock().getRelative(BlockFace.NORTH)) && !this.isWhitelisted(e.getFrom().getBlock().getRelative(BlockFace.SOUTH)) && !this.isWhitelisted(e.getFrom().getBlock().getRelative(BlockFace.WEST)) && !this.isWhitelisted(e.getFrom().getBlock().getRelative(BlockFace.EAST));
    }
    
    private boolean isWhitelisted(final Block b) {
        return BlockData.isPassable(b) && !b.getType().equals((Object)Material.AIR);
    }
}
