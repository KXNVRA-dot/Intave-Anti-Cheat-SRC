// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.movement;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import de.jpx3.intave.util.data.MaterialData;
import org.bukkit.util.Vector;
import org.bukkit.block.Block;
import de.jpx3.intave.util.objectable.Checkable;
import java.util.Iterator;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import de.jpx3.intave.module.PersistentDebugTelemetry;
import java.util.concurrent.ThreadLocalRandom;
import de.jpx3.intave.util.calc.PlayerUtils;
import org.bukkit.enchantments.Enchantment;
import de.jpx3.intave.util.data.BlockData;
import org.bukkit.block.BlockFace;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffect;
import de.jpx3.intave.check.combat.Heuristics;
import org.bukkit.entity.Entity;
import de.jpx3.intave.util.calc.LocationHelper;
import de.jpx3.intave.util.calc.MathHelper;
import de.jpx3.intave.util.calc.YawUtil;
import de.jpx3.intave.antipiracy.IIUA;
import de.jpx3.intave.util.event.CheckableMoveEvent;
import org.bukkit.plugin.Plugin;
import java.util.concurrent.ConcurrentHashMap;
import de.jpx3.intave.util.enums.CheatCategory;
import java.util.Map;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.event.Listener;
import de.jpx3.intave.util.objectable.IntaveCheck;

public final class CelerityCheck extends IntaveCheck implements Listener
{
    private final IntavePlugin plugin;
    private final Map<Double, Double> legaljumpspeedpattern;
    
    public CelerityCheck(final IntavePlugin plugin) {
        super("Celerity", CheatCategory.MOVING);
        this.legaljumpspeedpattern = new ConcurrentHashMap<Double, Double>();
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this.plugin);
        this.legaljumpspeedpattern.put(0.42, 0.62);
        this.legaljumpspeedpattern.put(0.333, 0.363);
        this.legaljumpspeedpattern.put(0.248, 0.3559);
        this.legaljumpspeedpattern.put(0.164, 0.349);
        this.legaljumpspeedpattern.put(0.083, 0.343);
        this.legaljumpspeedpattern.put(-0.078, 0.414);
        this.legaljumpspeedpattern.put(-0.155, 0.328);
        this.legaljumpspeedpattern.put(-0.23, 0.323);
        this.legaljumpspeedpattern.put(-0.304, 0.32);
        this.legaljumpspeedpattern.put(-0.376, 0.313);
        this.legaljumpspeedpattern.put(-0.103, 0.312);
    }
    
    @Override
    public void onCheckableMove(final CheckableMoveEvent e) {
        if (e.isMoving() && e.getXZDirectedMotion() > 0.005) {
            this.checkXZSpeed(e);
        }
    }
    
    private void checkXZSpeed(final CheckableMoveEvent e) {
        final Player p = e.getBukkitPlayer();
        if (e.isCancelled()) {
            return;
        }
        if (IIUA.getAllowFlight(p)) {
            return;
        }
        final double TFYawDiff = YawUtil.yawDiff(e.getTo().getYaw(), e.getDirection());
        final boolean isMovingBackwards = MathHelper.diff(TFYawDiff, 0.0) > 130.0 && MathHelper.diff(TFYawDiff, 0.0) < 181.0 && e.getDirection() != -180.0f;
        final Location to2 = e.getTo().clone();
        to2.setY(e.getFrom().getY());
        final double xz = e.getFrom().distance(to2);
        final double y = e.getTo().getY() - e.getFrom().getY();
        final double lastYCol = (double)(IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeCollidedWithTopBlock);
        final boolean ffk = e.getCheckable().getMeta().getSyncedValues().lastXMovement == e.getTo().getX() - e.getFrom().getX() && e.getCheckable().getMeta().getSyncedValues().lastZMovement == e.getTo().getZ() - e.getFrom().getZ() && e.getYDirectedMotion() < -0.078 && e.getCheckable().getMeta().getSyncedValues().lastYmovement < 0.0;
        final boolean velocityFlag = IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastFlagForVelocity < 200L || ffk;
        double maxspeed = 0.69;
        if (e.getYDirectedMotion() < 0.0) {
            maxspeed = 0.34;
        }
        if (IIUA.isPretendingToBeOnGround(p) && e.getCheckable().getMeta().getSyncedValues().lastYmovement == 0.0) {
            maxspeed = ((e.getYDirectedMotion() == 0.5) ? 0.7 : 0.38);
        }
        if (p.isInsideVehicle()) {
            return;
        }
        boolean isInLJS = false;
        for (final double d : this.legaljumpspeedpattern.keySet()) {
            if (e.getCheckable().getMeta().getSyncedValues().ticksInAir > 11L) {
                break;
            }
            if (MathHelper.diff(y, d) < 0.01) {
                maxspeed = this.legaljumpspeedpattern.get(d) + 0.006;
                isInLJS = true;
                break;
            }
        }
        if (MathHelper.diff(e.getYDirectedMotion(), 0.42) < 0.01 && e.getCheckable().getMeta().getSyncedValues().lastYmovement == 0.0) {
            maxspeed = ((LocationHelper.hasWoopableBlockNearby(e.getFrom(), (Entity)p) || LocationHelper.hasWoopableBlockNearby(e.getTo(), (Entity)p)) ? 0.6 : 0.56);
        }
        if (e.getYDirectedMotion() > 0.4 && p.isSprinting() && e.getYDirectedMotion() > e.getCheckable().getMeta().getSyncedValues().lastYmovement && e.isOnGround() && e.getCheckable().getMeta().getSyncedValues().lastYmovement <= 0.0) {
            final float f = e.getTo().getYaw() * 0.017453292f;
            final double maxXmotion = e.getCheckable().getMeta().getSyncedValues().lastXMovement - Heuristics.sin(f, true) * 0.2f;
            final double maxZmotion = e.getCheckable().getMeta().getSyncedValues().lastZMovement + Heuristics.cos(f, true) * 0.2f;
            final double maxXZCMotion = StrictMath.sqrt(maxXmotion * maxXmotion + maxZmotion * maxZmotion) + 0.12;
            if (maxXZCMotion < e.getXZDirectedMotion()) {
                maxspeed = MathHelper.minmax(maxXZCMotion, 0.45, 0.62);
            }
        }
        for (final PotionEffect effect2 : p.getActivePotionEffects()) {
            if (effect2.getType().equals((Object)PotionEffectType.JUMP)) {
                if (e.getCheckable().getMeta().getSyncedValues().ticksInAir > 5L && effect2.getAmplifier() > 3) {
                    maxspeed += 0.14 * (effect2.getAmplifier() + 1);
                    break;
                }
                maxspeed += 0.11 * (effect2.getAmplifier() + 1);
                break;
            }
        }
        if (e.getYDirectedMotion() == 0.5 && e.getCheckable().getMeta().getSyncedValues().lastYmovement == 0.0) {
            e.getCheckable().getMeta().getSyncedValues().ticksOnGround = 0L;
        }
        final double f2 = 0.1;
        if ((e.getCheckable().getMeta().getSyncedValues().ticksOnGround > 8L || IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getVioValues().lastTimeSuspiciousForGroundFly < 1000L) && e.getTo().getY() - f2 / 1.75 <= e.getFrom().getY()) {
            final boolean forward = MathHelper.amount(TFYawDiff) < 3.0 && e.getDirection() != -180.0f;
            final boolean waterBoost = IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeInWater > 2000L;
            maxspeed = (forward ? 0.287 : 0.296);
            maxspeed += (waterBoost ? 0.08 : 0.0);
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeStepableNearby < 400L && maxspeed <= 0.4) {
            maxspeed = 0.6;
        }
        if (p.isSneaking() && e.getCheckable().getMeta().getSyncedValues().ticksOnGround > 4L && e.getCheckable().getMeta().getSyncedValues().ticksSneaking > 3L && e.getYDirectedMotion() <= 0.0 && IIUA.isPretendingToBeOnGround(p)) {
            maxspeed = 0.1372;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeSneakF < 400L) {
            maxspeed = 0.23;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeBlockPlacedUnder < 380L && p.getVelocity().getY() < 0.0) {
            maxspeed = 0.28;
        }
        if (isMovingBackwards && e.getCheckable().getMeta().getSyncedValues().ticksOnGround > 4L && IIUA.isPretendingToBeOnGround(p) && this.plugin.getConfig().getBoolean(this.getConfigPath() + ".use_rotations")) {
            maxspeed = 0.237;
        }
        if (MathHelper.amount(TFYawDiff) > 78.0 && MathHelper.amount(TFYawDiff) < 280.0 && e.getDirection() != -180.0f && e.getCheckable().getMeta().getSyncedValues().ticksOnGround > 4L && e.getYDirectedMotion() == 0.0) {
            maxspeed = 0.234;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnIce > 2000L) {
            final boolean flag1 = MathHelper.diff(MathHelper.amount(TFYawDiff), 45.0) < 3.0 || MathHelper.diff(MathHelper.amount(TFYawDiff), 135.0) < 3.0 || TFYawDiff > 180.0;
            final double xzspeed = e.getXZDirectedMotion();
            double toAdd = flag1 ? 0.12 : 0.0;
            if (e.getCheckable().getMeta().getSyncedValues().ticksOnGround < 4L) {
                toAdd += 0.08;
            }
            final boolean i = xzspeed >= e.getCheckable().getMeta().getSyncedValues().lastXZMovement && xzspeed > 0.31;
            if (p.getItemInHand() != null && e.getCheckable().getMeta().getSyncedValues().isUsingItem) {
                final boolean usable = this.isValidFood(p.getItemInHand().getType(), p) || (p.getItemInHand().getType().equals((Object)Material.BOW) && p.getInventory().contains(Material.ARROW)) || (p.getItemInHand().getType().name().contains("SWORD") && p.isBlocking());
                if (!usable) {
                    e.getCheckable().getMeta().getSyncedValues().isUsingItem = false;
                }
            }
            else if (e.getCheckable().getMeta().getSyncedValues().isUsingItem) {
                e.getCheckable().getMeta().getSyncedValues().isUsingItem = false;
            }
            if ((xzspeed > 0.09 + toAdd || i) && e.getCheckable().getMeta().getSyncedValues().isUsingItem) {
                if (e.getCheckable().getMeta().getVioValues().suspiciousNoSlowdownMoves < 10) {
                    final Checkable.CheckableMeta.ViolationValues vioValues = e.getCheckable().getMeta().getVioValues();
                    ++vioValues.suspiciousNoSlowdownMoves;
                }
                if (i || e.getCheckable().getMeta().getVioValues().suspiciousNoSlowdownMoves > 9) {
                    maxspeed = (IIUA.isPretendingToBeOnGround(p) ? ((toAdd > 0.0) ? (0.11 + toAdd) : ((xz > 0.11) ? (xz - 0.03) : 0.11)) : 0.15);
                }
            }
            else if (e.getCheckable().getMeta().getVioValues().suspiciousNoSlowdownMoves > 0) {
                final Checkable.CheckableMeta.ViolationValues vioValues2 = e.getCheckable().getMeta().getVioValues();
                --vioValues2.suspiciousNoSlowdownMoves;
            }
            if (e.getCheckable().getMeta().getSyncedValues().isUsingItem && e.getYDirectedMotion() > 0.4 && e.getYDirectedMotion() > e.getCheckable().getMeta().getSyncedValues().lastYmovement) {
                maxspeed = 0.28;
            }
        }
        if (e.getCheckable().getMeta().getSyncedValues().ticksOnGround < 9L && MathHelper.diff(e.getYDirectedMotion(), 0.0) < 0.002 && MathHelper.diff(e.getCheckable().getMeta().getSyncedValues().lastYmovement, 0.0) < 0.002 && e.getXZDirectedMotion() > 0.289 && IIUA.isPretendingToBeOnGround(p)) {
            final Checkable.CheckableMeta.ViolationValues vioValues3 = e.getCheckable().getMeta().getVioValues();
            ++vioValues3.suspiciousPostGHITMoves;
            if (e.getCheckable().getMeta().getVioValues().suspiciousPostGHITMoves > 4 || (e.getXZDirectedMotion() > 0.358 && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeStepableNearby > 300L)) {
                final double addicted = p.getActivePotionEffects().stream().filter(potion -> potion.getType().equals((Object)PotionEffectType.SPEED)).mapToDouble(potion -> 0.04 * (potion.getAmplifier() + 1)).sum();
                maxspeed = 0.293 + addicted;
            }
        }
        else if (e.getCheckable().getMeta().getVioValues().suspiciousPostGHITMoves > 0) {
            final Checkable.CheckableMeta.ViolationValues vioValues4 = e.getCheckable().getMeta().getVioValues();
            --vioValues4.suspiciousPostGHITMoves;
        }
        final Location from = e.getFrom().getBlock().getRelative(BlockFace.UP).getRelative(BlockFace.UP).getLocation();
        final Location to3 = e.getTo().getBlock().getRelative(BlockFace.UP).getRelative(BlockFace.UP).getLocation();
        final double d2 = 0.3;
        if (!BlockData.isPassable(from.getBlock()) || !BlockData.isPassable(to3.getBlock()) || !BlockData.isPassable(to3.clone().add(-0.75, 0.0, 0.0).getBlock()) || !BlockData.isPassable(to3.clone().add(0.5, 0.0, 0.0).getBlock()) || !BlockData.isPassable(to3.clone().add(0.0, 0.0, 0.75).getBlock()) || !BlockData.isPassable(to3.clone().add(0.0, 0.0, -0.75).getBlock())) {
            if (p.getVelocity().getY() < 0.0 && !isInLJS) {
                maxspeed = 0.42;
            }
            else {
                maxspeed = 0.615;
            }
        }
        if (e.getCheckable().getMeta().getSyncedValues().ticksOnGround < 3L && e.getCheckable().getMeta().getSyncedValues().ticksOnGround > 0L && BlockData.isPassable(e.getFrom().getBlock()) && BlockData.doesAffectMovement(e.getFrom().getBlock()) && e.getFrom().getY() + 0.05 < e.getTo().getY()) {
            maxspeed = d2;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnIce < 2000L) {
            if (e.getYDirectedMotion() <= 0.0 && IIUA.isPretendingToBeOnGround(p)) {
                final Block under = e.getFrom().getBlock().getRelative(BlockFace.DOWN);
                if (BlockData.isFrozen(under.getRelative(BlockFace.NORTH)) && BlockData.isFrozen(under.getRelative(BlockFace.SOUTH)) && BlockData.isFrozen(under.getRelative(BlockFace.EAST)) && BlockData.isFrozen(under.getRelative(BlockFace.WEST)) && BlockData.isFrozen(under.getRelative(BlockFace.NORTH_EAST)) && BlockData.isFrozen(under.getRelative(BlockFace.NORTH_WEST)) && BlockData.isFrozen(under.getRelative(BlockFace.SOUTH_WEST)) && BlockData.isFrozen(under.getRelative(BlockFace.SOUTH_EAST))) {
                    if (isMovingBackwards && e.getCheckable().getMeta().getSyncedValues().ticksOnGround > 4L && IIUA.isPretendingToBeOnGround(p)) {
                        maxspeed = 0.25;
                    }
                    else if (e.getCheckable().getMeta().getSyncedValues().ticksOnGround > 6L) {
                        maxspeed = 0.34;
                    }
                    else {
                        maxspeed = 0.4;
                    }
                }
                else {
                    maxspeed = Math.max(e.getCheckable().getMeta().getSyncedValues().lastXZMovement * 0.98, 0.505);
                    if (lastYCol < 800.0) {
                        maxspeed = 1.0;
                    }
                }
            }
            else {
                maxspeed = ((e.getYDirectedMotion() >= 0.4) ? 0.68 : Math.max(e.getCheckable().getMeta().getSyncedValues().lastXZMovement * 0.98, 0.58));
            }
            if (BlockData.doesAffectMovement(e.getBukkitPlayer().getEyeLocation().getBlock()) || (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeHeadInHeap < 1200L && e.getCheckable().getMeta().getSyncedValues().lastXZMovement > e.getXZDirectedMotion())) {
                maxspeed = 1.2;
            }
        }
        if (lastYCol < 400 + e.getCheckable().getPing()) {
            if (MathHelper.diff(e.getYDirectedMotion(), 0.2) < 3.0E-4 || e.getYDirectedMotion() < 0.0) {
                if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnIce < 1200L) {
                    maxspeed = 1.5;
                }
                else {
                    maxspeed = 0.671;
                }
            }
            else if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnIce > 600L) {
                if (lastYCol > 50.0 || e.getYDirectedMotion() > 0.3) {
                    if (e.getYDirectedMotion() == 0.0) {
                        maxspeed = Math.max(e.getCheckable().getMeta().getSyncedValues().lastXZMovement * 0.95, (e.getCheckable().getMeta().getSyncedValues().ticksOnGround < 4L) ? 0.4859 : 0.4326);
                    }
                    else {
                        maxspeed = 0.7;
                    }
                }
                else {
                    maxspeed = Math.max(e.getCheckable().getMeta().getSyncedValues().lastXZMovement * 1.4, 0.52);
                }
            }
            else if (BlockData.doesAffectMovement(e.getTo().getBlock().getRelative(BlockFace.UP).getRelative(BlockFace.UP))) {
                if (e.getYDirectedMotion() == 0.0) {
                    maxspeed = 1.4;
                }
                else {
                    maxspeed = 1.5;
                }
            }
            else {
                maxspeed = 1.15;
            }
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnSoulSand < 500L) {
            maxspeed = 0.12;
        }
        if (!this.canSprint(p)) {
            maxspeed *= 0.771;
            p.setSprinting(false);
        }
        final boolean brokeBlockRecently = IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeBlockBreak < 500L;
        if (e.getFrom().getBlock().getType().equals((Object)Material.WEB) && e.getTo().getBlock().getType().equals((Object)Material.WEB) && e.isMoving()) {
            if (brokeBlockRecently) {
                maxspeed = 0.29;
            }
            else if (e.getYDirectedMotion() == 0.0) {
                maxspeed = 0.121;
            }
            else if (e.getYDirectedMotion() < 0.0) {
                maxspeed = 0.09;
            }
            else {
                maxspeed = 0.142;
            }
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnASlime < 2000L) {
            if (isInLJS) {
                if (maxspeed < 0.56) {
                    maxspeed = 0.56;
                }
            }
            else if (e.getFrom().clone().subtract(0.0, 0.5, 0.0).getBlock().getType().equals((Object)Material.SLIME_BLOCK)) {
                maxspeed = 0.5;
            }
            else if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnASlime < 600L) {
                maxspeed = 0.48;
            }
            else if (maxspeed < 0.32) {
                maxspeed = 0.32;
            }
        }
        if (e.getFrom().getBlock().isLiquid() && !e.getCheckable().wasCurrentlyHittet() && e.getCheckable().getMeta().getSyncedValues().ticksInLiquid > 10L && LocationHelper.collidesLiquid((Entity)p)) {
            if (!BlockData.doesAffectMovement(p.getEyeLocation().getBlock()) && IIUA.getDataFromBlock(e.getFrom().getBlock()) == 0) {
                if (e.getCheckable().getMeta().getSyncedValues().ticksInLiquid >= 9L && e.getCheckable().getMeta().getSyncedValues().ticksInLiquid <= 14L) {
                    maxspeed = 0.176;
                }
                else {
                    maxspeed = ((IIUA.getDataFromBlock(e.getFrom().getBlock()) == 0) ? (0.181 + (e.getCheckable().hasWaterPower() ? 0.03 : 0.0)) : 0.203);
                }
                if (p.getInventory().getBoots() != null) {
                    final double boostAdded = p.getInventory().getBoots().getEnchantmentLevel(Enchantment.DEPTH_STRIDER) * ((e.getYDirectedMotion() != 0.0) ? ((e.getYDirectedMotion() < -0.19) ? 0.037 : 0.035) : 0.04);
                    maxspeed += boostAdded;
                }
            }
            else if (!LocationHelper.collidesACLiquit(p.getWorld(), (Entity)p, e.getTo())) {
                maxspeed = 0.2;
                if (p.getInventory().getBoots() != null) {
                    final double boostAdded = p.getInventory().getBoots().getEnchantmentLevel(Enchantment.DEPTH_STRIDER) * ((e.getYDirectedMotion() != 0.0) ? ((e.getYDirectedMotion() < -0.19) ? 0.037 : 0.035) : 0.04);
                    maxspeed += boostAdded;
                }
            }
            else if (IIUA.getDataFromBlock(e.getFrom().getBlock()) == 0) {
                maxspeed = 0.14 + (e.getCheckable().hasWaterPower() ? 0.08 : 0.0);
                if (p.getInventory().getBoots() != null) {
                    maxspeed += 0.1 * p.getInventory().getBoots().getEnchantmentLevel(Enchantment.DEPTH_STRIDER);
                }
            }
            else if (e.getTo().getBlock().getRelative(BlockFace.DOWN).isLiquid()) {
                maxspeed = ((IIUA.getDataFromBlock(e.getFrom().getBlock()) == 0) ? 0.135 : 0.211);
            }
            else {
                maxspeed = 0.4;
            }
        }
        if (e.getBukkitPlayer().getVelocity().getY() >= -0.8 && e.getBukkitPlayer().getVelocity().getY() <= 0.42 && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeSlimeWasNear < 500L) {
            maxspeed = 1.1;
        }
        boolean j = false;
        for (final PotionEffect potion2 : p.getActivePotionEffects()) {
            if (potion2.getType().equals((Object)PotionEffectType.SPEED)) {
                maxspeed += 0.057 * (potion2.getAmplifier() + 1);
                j = true;
            }
            if (potion2.getType().equals((Object)PotionEffectType.JUMP) && !IIUA.isPretendingToBeOnGround(p)) {
                maxspeed += 0.039 * (potion2.getAmplifier() + 1);
            }
        }
        if (!j) {
            if (e.getCheckable().getMeta().getSyncedValues().ticksSinceLastSpeedFX >= 0) {
                if (e.getCheckable().getMeta().getSyncedValues().ticksSinceLastSpeedFX < 10) {
                    final Checkable.CheckableMeta.SyncedValues syncedValues = e.getCheckable().getMeta().getSyncedValues();
                    ++syncedValues.ticksSinceLastSpeedFX;
                }
                else {
                    e.getCheckable().getMeta().getSyncedValues().ticksSinceLastSpeedFX = -1;
                }
            }
        }
        else {
            e.getCheckable().getMeta().getSyncedValues().ticksSinceLastSpeedFX = 0;
        }
        final int ticksSync = e.getCheckable().getMeta().getSyncedValues().ticksSinceLastSpeedFX;
        if (ticksSync < 10 && ticksSync >= 0 && e.getCheckable().getMeta().getSyncedValues().lastXZMovement > xz && xz < 1.0) {
            return;
        }
        if (p.getFallDistance() > 1.27) {
            if (e.getCheckable().getMeta().getSyncedValues().lastTickSpeed > xz && !BlockData.doesAffectMovement(e.getFrom().clone().subtract(0.0, 0.25, 0.0).getBlock())) {
                maxspeed = ((IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnASlime < 1000L) ? 0.47 : 0.353);
            }
            maxspeed += p.getActivePotionEffects().stream().filter(effect -> effect.getType().equals((Object)PotionEffectType.JUMP)).mapToDouble(effect -> 0.01 * (effect.getAmplifier() + 1)).sum();
            if (p.getFallDistance() > 6.0f) {
                maxspeed = ((IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeFlying < 4000L) ? 0.42 : 0.305);
            }
            if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeFlying < 4000L) {
                maxspeed += 0.5;
            }
        }
        if (PlayerUtils.isUsingElytra(p) && e.getYDirectedMotion() < 0.0) {
            maxspeed = Math.max(maxspeed, e.getCheckable().getMeta().getSyncedValues().lastXZMovement * 1.2);
        }
        else if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeUsedelytra < 1000 + e.getCheckable().getPing()) {
            maxspeed *= 2.0;
        }
        else if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeUsedelytra < 2000 + e.getCheckable().getPing()) {
            maxspeed *= 1.25;
        }
        if (e.getCheckable().getMeta().getVioValues().suspiciousLowPackets > 4 && !LocationHelper.hasStepableNearby(p, e.getFrom()) && !LocationHelper.hasStepableNearby(p, e.getTo())) {
            maxspeed = 0.101 + ThreadLocalRandom.current().nextDouble(0.002, 0.01);
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastHitByEntityTimestamp < 1300L && maxspeed <= 0.968) {
            final long lastDamage = IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastHitByEntityTimestamp;
            final long lastKnockHit = IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeHittedWithKnockbackItem;
            if (lastDamage > 200L && lastDamage < 1000L && e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.clone().setY(0).length() < 1.0 && !IIUA.isPretendingToBeOnGround(p)) {
                final double diff = e.getCheckable().getMeta().getSyncedValues().lastXZMovement - e.getMotionSqared();
                boolean flag2 = false;
                if (e.getXZDirectedMotion() > 0.5 && lastKnockHit > 6000L) {
                    flag2 = true;
                }
                if (flag2 && (maxspeed < 0.6 || diff > 0.05)) {
                    maxspeed = ((diff > 0.05 && lastDamage > 250L) ? 0.43 : 0.8);
                }
            }
            else if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastHitByEntityTimestamp < 1200L) {
                maxspeed = Math.max(1.3, e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.clone().setY(0).length() + 0.01);
            }
            else if (!IIUA.isPretendingToBeOnGround(p)) {
                maxspeed = Math.max(1.1, e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.clone().setY(0).length() + 0.01);
            }
            else if (e.getCheckable().getMeta().getSyncedValues().lastVelocityFI != null) {
                maxspeed = Math.max(1.0, e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.clone().setY(0).length());
            }
            else {
                maxspeed = 1.0;
            }
            if (e.getCheckable().getMeta().getSyncedValues().lastVelocity != null) {
                final double vecXZ = e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.getX() + e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.getZ();
                if (vecXZ == 0.0) {
                    if (e.getYDirectedMotion() > 0.0) {
                        maxspeed = Math.max(0.59, e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.clone().setY(0).length() + 0.1);
                    }
                    else {
                        maxspeed = 0.45;
                    }
                }
                else {
                    final Vector moving = e.getMotionVector();
                    final Vector expected = e.getCheckable().getMeta().getSyncedValues().lastVelocity.clone();
                    expected.setY(moving.getY());
                    final double vertory_equality = moving.distance(expected);
                    if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeHittedWithKnockbackItem < 2900L) {
                        final boolean colAny = LocationHelper.collidesAnyFar(p.getWorld(), (Entity)p, e.getFrom()) || LocationHelper.collidesAnyFar(p.getWorld(), (Entity)p, e.getTo());
                        if (vertory_equality < 1.4) {
                            maxspeed = Math.max(1.92, e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.clone().setY(0).length());
                        }
                        else if (colAny) {
                            maxspeed = Math.max(1.9, e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.clone().setY(0).length());
                        }
                        else if (e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.getY() > 0.8 || e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.getY() < -0.2) {
                            maxspeed = Math.max(0.8, e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.clone().setY(0).length());
                        }
                        else {
                            maxspeed = 0.8;
                        }
                    }
                    else if (vertory_equality < 0.6 && maxspeed < 1.0) {
                        maxspeed = Math.max(1.0, e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.clone().setY(0).length() + 0.2);
                    }
                    else if (vertory_equality < 1.0) {
                        maxspeed = Math.max(1.2, e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.clone().setY(0).length() + 0.2);
                    }
                }
            }
        }
        if (velocityFlag && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded > 1000L) {
            maxspeed = 0.29;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastHitByProjectile < 600L) {
            maxspeed = Math.max(1.008, e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.clone().setY(0).lengthSquared());
        }
        if (e.getCheckable().getMeta().getSyncedValues().ticksVelocityMidAir > 1L && e.getCheckable().getMeta().getSyncedValues().hadYVelocityStartMotion && e.getCheckable().getMeta().getTimedValues().lastTimeOnGroundACC - 100L < e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded) {
            if (xz > Math.max(e.getCheckable().getMeta().getSyncedValues().lastXZMovement * 0.97, Math.max(0.5, e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.clone().setY(0).length())) && !LocationHelper.hasStepableNearby(e.getTo())) {
                if (this.plugin.getCheckManager().checkIsLoaded("Knockback")) {
                    ((KnockbackCheck)this.plugin.getCheckManager().getCheck("Knockback")).reverse(e);
                }
            }
            else if (maxspeed < 0.4) {
                maxspeed = 0.39;
            }
        }
        if (e.getCheckable().getMeta().getSyncedValues().itemuseCounter > 35 || (e.getCheckable().getMeta().getSyncedValues().itemuseCounter_last > 35 && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeUsedItem < 100L)) {
            e.setCancelled(true);
            return;
        }
        if (xz > maxspeed) {
            if (isMovingBackwards && xz < 0.25) {
                if (e.getCheckable().getMeta().getHeuristicValues().iSVL < 15) {
                    final Checkable.CheckableMeta.HeuristicValues heuristicValues = e.getCheckable().getMeta().getHeuristicValues();
                    heuristicValues.iSVL += 5;
                }
            }
            else if (e.getCheckable().getMeta().getHeuristicValues().iSVL > 0) {
                final Checkable.CheckableMeta.HeuristicValues heuristicValues2 = e.getCheckable().getMeta().getHeuristicValues();
                --heuristicValues2.iSVL;
            }
            final int percentageTooFast = (int)(xz / maxspeed * 100.0) - 100;
            int vl = (int)MathHelper.map(percentageTooFast, 5.0, 200.0, 5.0, 80.0);
            final long lastSF = IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getVioValues().lastTimeSuspiciousForSpeed;
            if (vl > this.plugin.getConfig().getInt(this.getConfigPath() + ".max_vl-flag")) {
                vl = this.plugin.getConfig().getInt(this.getConfigPath() + ".max_vl-flag");
            }
            else if (vl < 1) {
                vl = 1;
            }
            if (vl < 8 && lastSF < 16000L) {
                vl += (int)(30.0 - MathHelper.map((double)lastSF, 0.0, 16000.0, 0.0, 30.0));
            }
            if (xz < 0.3 && vl > 30) {
                vl = 25;
            }
            e.getCheckable().getMeta().getVioValues().lastTimeSuspiciousForSpeed = IIUA.getCurrentTimeMillis();
            final double meterPerSecond = e.getXZDirectedMotion() * 20.0;
            final String message = "moved faster than expected (" + MathHelper.roundFromDouble(e.getXZDirectedMotion(), 5) + " b/t | " + MathHelper.roundFromDouble(maxspeed, 5) + " b/t | " + MathHelper.roundFromDouble(e.getYDirectedMotion(), 5) + " b/t)";
            if (this.plugin.getRetributionManager().markPlayer(p, vl, "Celerity", CheatCategory.MOVING, message)) {
                if (xz < 0.3 && velocityFlag) {
                    final Location setforwardLocation = e.getFrom().clone().add(e.getCheckable().getMeta().getSyncedValues().lastVector.clone().multiply(1.25));
                    if (!BlockData.doesAffectMovement(setforwardLocation.getBlock())) {
                        PersistentDebugTelemetry.teleport((Entity)p, setforwardLocation, "celerity-positionreset");
                    }
                    else {
                        PersistentDebugTelemetry.teleport((Entity)p, e.getFrom(), "celerity-dispocrct");
                    }
                }
                else {
                    PersistentDebugTelemetry.teleport((Entity)p, e.getFrom(), "celerity-hyperbuffer");
                }
                p.setSprinting(false);
                p.setSneaking(false);
            }
        }
        e.getCheckable().getMeta().getSyncedValues().lastTickSpeed = xz;
    }
    
    private boolean isValidFood(final Material m, final Player p) {
        return MaterialData.isValidFood(m, p);
    }
    
    @EventHandler
    public void on(final PlayerToggleSneakEvent e) {
        if (!this.isActivated()) {
            return;
        }
        final Player p = e.getPlayer();
        if (IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastSneakToggleListenerRefresh > 333L) {
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastSneakToggleListenerRefresh = IIUA.getCurrentTimeMillis();
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().sneakToggles = 0;
        }
        if (this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().sneakToggles > 5) {
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeSneakF = IIUA.getCurrentTimeMillis();
        }
        this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastSneakToggled = IIUA.getCurrentTimeMillis();
        final Checkable.CheckableMeta.SyncedValues syncedValues = this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues();
        ++syncedValues.sneakToggles;
    }
    
    private boolean canSprint(final Player p) {
        return p.getFoodLevel() > 6.0 || !p.hasPotionEffect(PotionEffectType.BLINDNESS) || !p.isSneaking() || p.isBlocking();
    }
}
