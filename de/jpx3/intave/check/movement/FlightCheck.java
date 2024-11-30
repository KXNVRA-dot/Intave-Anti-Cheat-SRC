// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.movement;

import de.jpx3.intave.util.event.CheckableDamageEntityEvent;
import org.bukkit.entity.EntityType;
import de.jpx3.intave.util.event.CheckableVelocityEvent;
import org.bukkit.Location;
import java.util.Objects;
import org.bukkit.potion.PotionEffect;
import de.jpx3.intave.module.CrossVersionSupply;
import de.jpx3.intave.util.objectable.Checkable;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;
import de.jpx3.intave.util.calc.MathHelper;
import java.lang.reflect.InvocationTargetException;
import com.comphenix.protocol.utility.MinecraftVersion;
import de.jpx3.intave.api.internal.ProtocolLibAdapter;
import de.jpx3.intave.util.data.BlockData;
import org.bukkit.block.BlockFace;
import de.jpx3.intave.util.calc.PotionUtil;
import org.bukkit.potion.PotionEffectType;
import de.jpx3.intave.util.calc.PlayerUtils;
import de.jpx3.intave.util.calc.BalanceUtils;
import java.util.ArrayList;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import de.jpx3.intave.util.calc.LocationHelper;
import org.bukkit.Material;
import de.jpx3.intave.antipiracy.IIUA;
import de.jpx3.intave.util.event.CheckableMoveEvent;
import java.util.concurrent.ConcurrentHashMap;
import de.jpx3.intave.util.enums.CheatCategory;
import java.util.List;
import java.util.Map;
import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.util.objectable.IntaveCheck;

public final class FlightCheck extends IntaveCheck
{
    private final IntavePlugin plugin;
    private static final boolean debug = false;
    private final Map<String, List<Long>> performanceMap;
    
    public FlightCheck(final IntavePlugin plugin) {
        super("Flight", CheatCategory.MOVING);
        this.performanceMap = new ConcurrentHashMap<String, List<Long>>();
        this.plugin = plugin;
    }
    
    @Override
    public void onCheckableMove(final CheckableMoveEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeTeleported < 150L && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeFlagged > 150L) {
            return;
        }
        if (e.getYDirectedMotion() > 0.0) {
            this.checkStepMovement(e);
        }
        if (e.getYDirectedMotion() >= e.getCheckable().getMeta().getSyncedValues().lastYmovement) {
            this.checkSpider(e);
        }
        if (e.getYDirectedMotion() < 0.0 && e.getCheckable().getMeta().getSyncedValues().lastYmovement >= 0.0) {
            this.checkYPort(e);
        }
        if (e.getYDirectedMotion() != 0.0 || e.getCheckable().getMeta().getSyncedValues().lastYmovement != 0.0) {
            this.checkYVelocity(e);
        }
        this.checkYIgnoringOG(e);
        if (LocationHelper.collides(p.getWorld(), (Entity)p, Material.WEB)) {
            this.checkWeb(e);
        }
        if (e.getYDirectedMotion() == e.getCheckable().getMeta().getSyncedValues().lastYmovement) {
            this.checkOnGroundReleated(e);
        }
        this.checkGlide(e);
        this.checkGravity(e);
        if (e.getYDirectedMotion() < 0.0) {
            this.checkGlide2(e);
        }
        this.liquidCheck(e);
        if (e.getYDirectedMotion() >= 0.0) {
            if (e.getCache().getClonedRemoving(e.getFrom(), 0.0, 0.25, 0.0).getBlock().getType().equals((Object)Material.SLIME_BLOCK)) {
                this.checkSlime(e);
            }
            this.checkGHop(e);
            this.checkCritical(e);
            if (LocationHelper.collidesU(p.getWorld(), (Entity)p, e.getTo(), Material.LADDER, 0.0)) {
                this.checkClimbableYSpeed(e);
            }
            this.checkWater2(e);
        }
        this.checkWater(e);
    }
    
    private void addValue(final String name, final long currentDur) {
        if (!this.performanceMap.containsKey(name)) {
            this.performanceMap.put(name, new ArrayList<Long>());
        }
        this.addNumberAndGetBalance(this.performanceMap.get(name), currentDur, 400);
    }
    
    private double addNumberAndGetBalance(final List<Long> longs, final long toAdd, final int maxSize) {
        return BalanceUtils.getSquaredBalanceFromLong(this.addDynamic(longs, toAdd, maxSize));
    }
    
    private <T> List<T> addDynamic(final List<T> list, final T toAdd, final double maxSize) {
        list.add(toAdd);
        if (list.size() > maxSize) {
            list.remove(0);
        }
        return list;
    }
    
    private void checkGHop(final CheckableMoveEvent e) {
        final Player p = e.getBukkitPlayer();
        if (!e.getMoveFlagData().shouldCheck()) {
            return;
        }
        if (e.isCancelled()) {
            return;
        }
        if (PlayerUtils.getAllowFlight(p, false, false, true) || LocationHelper.hasLiquitNearby(e.getTo()) || IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnASlime < 1000L) {
            return;
        }
        if (e.getCheckable().getMeta().getSyncedValues().lastYmovement > e.getYDirectedMotion()) {
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastFlightToggle < 500L) {
            return;
        }
        if (!LocationHelper.collidesStepable(p.getWorld(), (Entity)p, e.getTo()) && !LocationHelper.collidesStepable(p.getWorld(), (Entity)p, e.getFrom()) && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeCollidedWithTopBlock > 700L) {
            double displayMinY = 0.42 + PotionUtil.getPotionEffectAmplifier(p, PotionEffectType.JUMP) * 0.1;
            double minY = ((e.getXZDirectedMotion() > 0.2) ? 0.4 : 0.3874) + PotionUtil.getPotionEffectAmplifier(p, PotionEffectType.JUMP) * 0.1;
            if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded < 4000L) {
                minY = Math.min(minY, Math.max(0.0, e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.getY() - 0.03));
                displayMinY = e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.getY();
            }
            if (PotionUtil.getPotionEffectAmplifier(p, PotionEffectType.JUMP) > 10.0) {
                return;
            }
            if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeInWater < 1000L) {
                return;
            }
            if (BlockData.isClimbable(e.getFrom().getBlock().getRelative(BlockFace.DOWN))) {
                minY = 0.358;
                displayMinY = 0.42;
            }
            if (PlayerUtils.hasLevitation(p)) {
                minY = 0.005;
                displayMinY = 0.112;
            }
            if (LocationHelper.hasStepableNearby(e.getTo()) || LocationHelper.hasStepableNearby(e.getFrom()) || p.isInsideVehicle()) {
                minY = 0.1;
                displayMinY = 0.1;
            }
            if (LocationHelper.hasWoopableBlockNearby(e.getFrom(), (Entity)p) || LocationHelper.hasWoopableBlockNearby(e.getTo(), (Entity)p)) {
                minY = 0.06;
                displayMinY = 0.06;
            }
            if (LocationHelper.collidesStepable(p.getWorld(), (Entity)p, e.getTo().getBlock().getRelative(BlockFace.UP).getLocation())) {
                return;
            }
            if (!BlockData.doesAffectMovement(e.getFrom().getBlock()) && e.getYDirectedMotion() < minY && e.getYDirectedMotion() > 0.0) {
                if (this.plugin.getRetributionManager().markPlayer(p, 10, "Flight", CheatCategory.MOVING, this.getFlagMessage("*5", e.getYDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastYmovement, displayMinY))) {
                    e.getMoveFlagData().setPullSilent(true);
                    e.getMoveFlagData().setPullYMotionPositive(0.0);
                }
                e.getMoveFlagData().setShouldCheck(false);
            }
        }
    }
    
    private void checkCritical(final CheckableMoveEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (!e.getMoveFlagData().shouldCheck()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        if (!ProtocolLibAdapter.getProtocolManager().getMinecraftVersion().isAtLeast(MinecraftVersion.COMBAT_UPDATE)) {
            return;
        }
        if (PlayerUtils.isUsingElytra(p)) {
            try {
                final boolean usingIt = (boolean)p.getClass().getMethod("isGliding", (Class<?>[])new Class[0]).invoke(p, new Object[0]);
                final double difference = e.getYDirectedMotion() - e.getCheckable().getMeta().getSyncedValues().lastYmovement;
                if ((difference < -0.08 || difference > 0.025 || e.getYDirectedMotion() > 0.1) && usingIt && !LocationHelper.collidesAnyValidFar(p.getWorld(), (Entity)p, e.getCheckable().getVerifiedLocation()) && !LocationHelper.collidesAnyValidFar(p.getWorld(), (Entity)p, LocationHelper.getRelative(LocationHelper.getBlockAt(e.getCheckable().getVerifiedLocation()), BlockFace.DOWN).getLocation())) {
                    if (this.plugin.getRetributionManager().markPlayer(p, 10, "Flight", CheatCategory.MOVING, this.getFlagMessage("*5612", e.getYDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastYmovement, e.getCheckable().getMeta().getSyncedValues().lastYmovement - 0.1337))) {
                        e.getMoveFlagData().setPullSilent(true);
                        e.getMoveFlagData().setPullYMotion(-0.4);
                    }
                    e.getMoveFlagData().setShouldCheck(false);
                }
            }
            catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ex2) {
                final ReflectiveOperationException ex;
                final ReflectiveOperationException e2 = ex;
                e2.printStackTrace();
            }
        }
    }
    
    private void checkSlime(final CheckableMoveEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (!e.getMoveFlagData().shouldCheck()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        if (PlayerUtils.getAllowFlight(p, true, true, true, false, true)) {
            return;
        }
        if (!LocationHelper.hasStepableNearby(p, e.getCheckable().getVerifiedLocation()) && this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().lastYMotions.size() >= 5) {
            boolean seen = false;
            double best = 0.0;
            for (final Double d : this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().lastYMotions) {
                final double v = d;
                if (v <= 0.0 && (!seen || Double.compare(v, best) < 0)) {
                    seen = true;
                    best = v;
                }
            }
            final double smallest = seen ? best : 0.0;
            if (e.getCache().getClonedRemoving(e.getFrom(), 0.0, 0.25, 0.0).getBlock().getType().equals((Object)Material.SLIME_BLOCK)) {
                if (e.getCheckable().getMeta().getSyncedValues().ticksOnGround <= 3L && e.getCheckable().getMeta().getSyncedValues().ticksInAir <= 1L) {
                    final double reversedHopMotion = MathHelper.diff(MathHelper.amount(smallest), e.getYDirectedMotion());
                    if (reversedHopMotion > 0.05 && e.getYDirectedMotion() > 0.42 && MathHelper.amount(smallest) <= MathHelper.amount(e.getYDirectedMotion())) {
                        if (this.plugin.getRetributionManager().markPlayer(p, ((e.getYDirectedMotion() > 0.56) ? 50 : 10) + ThreadLocalRandom.current().nextInt(1, 4), "Flight", CheatCategory.MOVING, this.getFlagMessage("*6", e.getYDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastYmovement, MathHelper.amount(smallest)))) {
                            e.setCancelled(true);
                        }
                        e.getMoveFlagData().setShouldCheck(false);
                    }
                }
                else {
                    final double max = 0.42 + p.getActivePotionEffects().stream().filter(potionEffect -> potionEffect.getType().equals((Object)PotionEffectType.JUMP)).mapToDouble(potionEffect -> (potionEffect.getAmplifier() + 1) * 0.1).sum();
                    if (e.getYDirectedMotion() > max) {
                        if (this.plugin.getRetributionManager().markPlayer(p, 58 + ThreadLocalRandom.current().nextInt(1, 4), "Flight", CheatCategory.MOVING, "tried to jump on a slimeblock too high (" + MathHelper.roundFromDouble(e.getYDirectedMotion(), 4) + "/" + MathHelper.roundFromDouble(max, 4) + ")")) {
                            e.setCancelled(true);
                        }
                        e.getMoveFlagData().setShouldCheck(false);
                    }
                }
            }
        }
    }
    
    private void checkYPort(final CheckableMoveEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (!e.getMoveFlagData().shouldCheck()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        if (PlayerUtils.getAllowFlight(p)) {
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeClimbedALadder < 200L) {
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded < 1000L) {
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeTeleported < 1000L) {
            return;
        }
        if (e.getYDirectedMotion() < 0.0 && !p.isInsideVehicle() && e.getCheckable().getMeta().getSyncedValues().lastYmovement >= 0.0 && !LocationHelper.collidesAnyXZAxis(p.getWorld(), (Entity)p, e.getFrom())) {
            if (MathHelper.amount(e.getCheckable().getMeta().getSyncedValues().lastYmovement) > 0.0) {
                if (e.getCheckable().getMeta().getVioValues().suspiciousYDropVL < 5) {}
            }
            else if (e.getCheckable().getMeta().getVioValues().suspiciousYDropVL > 0) {
                final Checkable.CheckableMeta.ViolationValues vioValues = e.getCheckable().getMeta().getVioValues();
                --vioValues.suspiciousYDropVL;
            }
            if ((MathHelper.diff(0.0, e.getCheckable().getMeta().getSyncedValues().lastYmovement) > 0.1 || (MathHelper.diff(0.0, e.getCheckable().getMeta().getSyncedValues().lastYmovement) < 0.01 && MathHelper.diff(0.0, e.getCheckable().getMeta().getSyncedValues().lastYmovement) > 0.0) || e.getCheckable().getMeta().getVioValues().suspiciousYDropVL > 4) && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeCollidedWithTopBlock > 1000L && MathHelper.amount(MathHelper.diff((e.getCheckable().getMeta().getSyncedValues().lastYmovement - 0.08) * 0.98, e.getYDirectedMotion())) > 0.001) {
                if (LocationHelper.collidesFar(p.getWorld(), (Entity)p, e.getFrom(), Material.PISTON_EXTENSION, Material.PISTON_MOVING_PIECE)) {
                    return;
                }
                if (LocationHelper.collidesFar(p.getWorld(), (Entity)p, e.getFrom(), Material.PISTON_EXTENSION, Material.PISTON_MOVING_PIECE)) {
                    return;
                }
                if (LocationHelper.collidesStepable(p.getWorld(), (Entity)p, e.getTo()) || LocationHelper.collidesStepable(p.getWorld(), (Entity)p, e.getCache().getCloned(e.getTo()).add(0.0, 1.0, 0.0))) {
                    return;
                }
                if (LocationHelper.collidesLiquid((Entity)p, e.getFrom().subtract(0.0, 0.5, 0.0))) {
                    return;
                }
                if (this.plugin.getRetributionManager().markPlayer(p, 10, "Flight", CheatCategory.MOVING, this.getFlagMessage("*84", e.getYDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastYmovement, (e.getCheckable().getMeta().getSyncedValues().lastYmovement - 0.08) * 0.98))) {
                    e.getMoveFlagData().setPullSilent(true);
                    final double expected = (e.getCheckable().getMeta().getSyncedValues().lastYmovement - 0.08 > 0.0) ? -0.078 : (e.getCheckable().getMeta().getSyncedValues().lastYmovement - 0.0784);
                    e.getMoveFlagData().setPullYMotion(expected);
                }
                e.getMoveFlagData().setShouldCheck(false);
            }
        }
    }
    
    private void checkClimbableYSpeed(final CheckableMoveEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (!e.getMoveFlagData().shouldCheck()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        if (IIUA.getAllowFlight(p)) {
            return;
        }
        final double dis = MathHelper.amount(e.getYDirectedMotion());
        if (LocationHelper.collidesU(p.getWorld(), (Entity)p, e.getTo(), Material.LADDER, 0.0) && LocationHelper.onlyCollidesYFar(p.getWorld(), (Entity)p, e.getTo(), Material.LADDER, Material.AIR) && LocationHelper.onlyCollidesYFar(p.getWorld(), (Entity)p, e.getFrom(), Material.LADDER, Material.AIR)) {
            if (e.getYDirectedMotion() > 0.0 && e.getYDirectedMotion() >= e.getCheckable().getMeta().getSyncedValues().lastYmovement) {
                if (dis > 0.119) {
                    if (e.getCheckable().getMeta().getVioValues().suspiciousFastClimbs < 10) {
                        final Checkable.CheckableMeta.ViolationValues vioValues = e.getCheckable().getMeta().getVioValues();
                        vioValues.suspiciousFastClimbs += 2;
                    }
                }
                else if (e.getCheckable().getMeta().getVioValues().suspiciousFastClimbs > 0) {
                    final Checkable.CheckableMeta.ViolationValues vioValues2 = e.getCheckable().getMeta().getVioValues();
                    --vioValues2.suspiciousFastClimbs;
                }
                if (dis > 0.155 || (e.getCheckable().getMeta().getVioValues().suspiciousFastClimbs > 8 && dis > 0.119)) {
                    if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastDamageTakenTimestamp < 300L && dis < 0.4) {
                        return;
                    }
                    if (this.plugin.getRetributionManager().markPlayer(p, 10, "Flight", CheatCategory.MOVING, this.getFlagMessage("*7", e.getYDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastYmovement, 0.119))) {
                        e.getMoveFlagData().setPullSilent(true);
                        e.getMoveFlagData().setPullYMotion(-0.119);
                    }
                    e.getMoveFlagData().setShouldCheck(false);
                }
            }
            else if (e.getCheckable().getMeta().getVioValues().suspiciousFastClimbs > 0) {
                final Checkable.CheckableMeta.ViolationValues vioValues3 = e.getCheckable().getMeta().getVioValues();
                --vioValues3.suspiciousFastClimbs;
            }
        }
        else if (e.getCheckable().getMeta().getVioValues().suspiciousFastClimbs > 0) {
            final Checkable.CheckableMeta.ViolationValues vioValues4 = e.getCheckable().getMeta().getVioValues();
            --vioValues4.suspiciousFastClimbs;
        }
    }
    
    private void checkYIgnoringOG(final CheckableMoveEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (!e.getMoveFlagData().shouldCheck()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        final double lastYMovement = e.getCheckable().getMeta().getSyncedValues().lastYmovement;
        if (e.getYDirectedMotion() < -0.078400001526 && !p.isSleeping() && e.getCheckable().getMeta().getSyncedValues().lastYmovement >= 0.07 && p.getFallDistance() == 0.0f) {
            if (p.isInsideVehicle()) {
                return;
            }
            if (LocationHelper.collidesFar(p.getWorld(), (Entity)p, e.getFrom(), Material.SLIME_BLOCK, Material.WEB)) {
                return;
            }
            if (PlayerUtils.getAllowFlight(p, true, true, true, true, true)) {
                return;
            }
            if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeClimbedALadder < 200L && e.getCheckable().getMeta().getSyncedValues().ticksInAir < 3L) {
                return;
            }
            if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastFlagForVelocity < 400L) {
                return;
            }
            if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnASlime < 2000L) {
                return;
            }
            if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeHittedWithKnockbackItem < 600L) {
                return;
            }
            if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastDamageTakenTimestamp < 1200L) {
                return;
            }
            if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeInWater < 1200L) {
                return;
            }
            final boolean collidesWaterFar = LocationHelper.collidesFar(p.getWorld(), (Entity)p, e.getFrom(), Material.WATER, Material.LAVA, Material.STATIONARY_WATER, Material.STATIONARY_LAVA);
            if (!collidesWaterFar) {
                if (this.plugin.getRetributionManager().markPlayer(p, 5 + ThreadLocalRandom.current().nextInt(1, 3), "Flight", CheatCategory.MOVING, this.getFlagMessage("*8", e.getYDirectedMotion(), lastYMovement, (lastYMovement - 0.08) * 0.98))) {
                    e.getMoveFlagData().setPullSilent(true);
                    e.getMoveFlagData().setPullYMotion((lastYMovement - 0.08) * 0.98);
                }
                e.getMoveFlagData().setShouldCheck(false);
            }
        }
    }
    
    private void checkYVelocity(final CheckableMoveEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (!e.getMoveFlagData().shouldCheck()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        if (PlayerUtils.getAllowFlight(p, false, false, true, false, true)) {
            return;
        }
        if (PlayerUtils.hasLevitation(p)) {
            return;
        }
        if (p.isInsideVehicle()) {
            return;
        }
        if (LocationHelper.hasStepableNearby(p, e.getTo())) {
            return;
        }
        if (LocationHelper.collidesLiquid((Entity)p) || LocationHelper.hasLiquitNearby(e.getTo()) || LocationHelper.collidesLiquid((Entity)p, e.getTo()) || LocationHelper.hasLiquitNearby(e.getFrom())) {
            return;
        }
        if (e.getCheckable().getMeta().getSyncedValues().ticksInLiquid > 0L) {
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeClimbedALadder < 200L && e.getCheckable().getMeta().getSyncedValues().ticksInAir < 3L) {
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastFlagForVelocity < 700L) {
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeHittedWithKnockbackItem < 600L) {
            return;
        }
        if (p.hasPotionEffect(PotionEffectType.JUMP) && !e.getCheckable().getMeta().getSyncedValues().hadYVelocityStartMotion && this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastDamageTakenTimestamp < 400L) {
            return;
        }
        final double lastYMovement = e.getCheckable().getMeta().getSyncedValues().lastYmovement;
        final double yMovement = e.getYDirectedMotion();
        if (LocationHelper.collidesFar(p.getWorld(), (Entity)p, e.getFrom(), Material.PISTON_EXTENSION, Material.PISTON_MOVING_PIECE)) {
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastFlightToggle < 500L) {
            return;
        }
        final double solve_error = Math.abs((((lastYMovement > 0.0 && e.getYDirectedMotion() < 0.0) ? 0.0 : lastYMovement) - 0.08) * 0.98 - e.getYDirectedMotion());
        final double maxSolve = (e.getCheckable().getMeta().getSyncedValues().isInSlimeJump || p.hasPotionEffect(PotionEffectType.JUMP)) ? ((e.getYDirectedMotion() > 0.0) ? 0.005 : 0.08) : 0.003125;
        if (e.getYDirectedMotion() <= 0.0 && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeInWater > 1000L && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded > 1000L && !LocationHelper.isOnGroundAccurate(e.getTo(), (Entity)p) && e.getYDirectedMotion() < e.getCheckable().getMeta().getSyncedValues().lastYmovement) {
            if ((p.hasPotionEffect(PotionEffectType.JUMP) || e.getCheckable().getMeta().getSyncedValues().isInSlimeJump) && e.getYDirectedMotion() < 0.0 && lastYMovement >= 0.0) {
                return;
            }
            if (MathHelper.amount(solve_error) > 3333.0 && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeCollidedWithTopBlock > 1000L && (MathHelper.diff(e.getYDirectedMotion(), -0.072) > 0.001 || MathHelper.diff(e.getYDirectedMotion(), -0.078) > 0.001 || LocationHelper.isOnGroundR(e.getTo())) && e.getCheckable().getMeta().getSyncedValues().ticksInAir > 1L && !LocationHelper.collidesFar(p.getWorld(), (Entity)p, e.getFrom(), Material.WEB) && !LocationHelper.collidesFar(p.getWorld(), (Entity)p, e.getTo(), Material.SLIME_BLOCK) && !e.isOnGround()) {
                if (e.getCheckable().getMeta().getSyncedValues().lastVelocityFI != null && e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.getY() > 0.7) {
                    return;
                }
                if (MathHelper.diff(e.getYDirectedMotion(), -0.1552320045) < 1.0E-8 && MathHelper.diff(e.getCheckable().getMeta().getSyncedValues().lastYmovement, -0.09395507) < 1.0E-7) {
                    return;
                }
                if (solve_error < 1.0E-4 && e.getCheckable().getMeta().getSyncedValues().ticksSinceLastMovementCorrection < 1L) {
                    return;
                }
                if (this.plugin.getRetributionManager().markPlayer(p, 15 + ThreadLocalRandom.current().nextInt(-5, 5), "Flight", CheatCategory.MOVING, this.getFlagMessage("*12/3", yMovement, lastYMovement, (lastYMovement - 0.08) * 0.98))) {
                    e.getMoveFlagData().setPullSilent(true);
                    e.getMoveFlagData().setPullYMotion(lastYMovement - 0.08);
                }
                e.getMoveFlagData().setShouldCheck(false);
                return;
            }
            else if (solve_error > Math.max(maxSolve, 0.038) && !LocationHelper.collidesAnyXZAxis(p.getWorld(), (Entity)p, e.getFrom()) && !LocationHelper.isOnGroundAccurate(e.getTo(), (Entity)p) && !LocationHelper.collidesFar(p.getWorld(), (Entity)p, e.getTo(), Material.SLIME_BLOCK) && !CrossVersionSupply.getCurrentCFR().getBlockData().isStepable(e.getCache().getClonedAdding(e.getFrom(), 0.0, p.getEyeHeight() + 0.7, 0.0).getBlock())) {
                if (this.plugin.getRetributionManager().markPlayer(p, 15 + ThreadLocalRandom.current().nextInt(-5, 5), "Flight", CheatCategory.MOVING, this.getFlagMessage("*12/2", yMovement, lastYMovement, lastYMovement - 0.0785))) {
                    e.getMoveFlagData().setPullSilent(true);
                    e.getMoveFlagData().setPullYMotion(lastYMovement - 0.0785);
                }
                e.getMoveFlagData().setShouldCheck(false);
            }
        }
        if (e.getYDirectedMotion() <= 0.0 || (e.getCheckable().getMeta().getSyncedValues().ticksVelocityMidAir <= 2L && e.getCheckable().getMeta().getSyncedValues().ticksVelocityMidAir > 0L) || LocationHelper.isOnGroundAccurate(e.getCache().getClonedAdding(e.getTo(), 0.0, 0.5, 0.0), (Entity)p) || LocationHelper.isOnGroundAccurate(e.getTo(), (Entity)p) || e.getYDirectedMotion() >= e.getCheckable().getMeta().getSyncedValues().lastYmovement || Math.abs(solve_error) <= maxSolve || IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeCollidedWithTopBlock <= 200 + e.getCheckable().getPing() || LocationHelper.isOnGroundAccurate(e.getFrom(), (Entity)p) || LocationHelper.isOnGroundAccurate(e.getTo(), (Entity)p) || LocationHelper.collidesFar(p.getWorld(), (Entity)p, e.getFrom(), Material.WEB) || IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded <= 250 + e.getCheckable().getPing()) {
            if (e.getYDirectedMotion() < 0.0 && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded > 1000L && (!LocationHelper.isOnGroundAccurate(e.getTo(), (Entity)p) || e.getYDirectedMotion() < e.getCheckable().getMeta().getSyncedValues().lastYmovement) && !LocationHelper.isOnGroundAccurate(e.getFrom(), (Entity)p) && !LocationHelper.isOnGroundAccurate(e.getCheckable().getMeta().getLocationValues().penaltyLocation, (Entity)p) && e.getCheckable().getMeta().getSyncedValues().lastYmovement < 0.0 && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeCollidedWithTopBlock > 1000L && e.getYDirectedMotion() > -4.0 && !LocationHelper.isOnGroundAccurate(e.getFrom(), (Entity)p) && !LocationHelper.isOnGroundAccurate(e.getTo(), (Entity)p) && !LocationHelper.collidesFar(p.getWorld(), (Entity)p, e.getFrom(), Material.WEB)) {
                boolean fp = false;
                if ((!LocationHelper.isOnGround(e.getCheckable().getVerifiedLocation()) && LocationHelper.collidesAnyValidFar(p.getWorld(), (Entity)p, e.getFrom())) || (LocationHelper.collidesAnyXZAxis(p.getWorld(), (Entity)p, e.getFrom()) && BlockData.doesAffectMovement(e.getFrom().getBlock().getRelative(BlockFace.DOWN)))) {
                    fp = true;
                }
                if (LocationHelper.hasWoopableBlockNearby(e.getFrom(), (Entity)p) || LocationHelper.hasWoopableBlockNearby(e.getTo(), (Entity)p)) {
                    fp = true;
                }
                if (LocationHelper.collidesAnyFar(p.getWorld(), (Entity)p, e.getTo())) {
                    fp = true;
                }
                if (!fp && Math.abs(solve_error) > maxSolve && e.getCheckable().getMeta().getSyncedValues().ticksInAir > 1L && e.getCheckable().getMeta().getSyncedValues().ticksOnGround < 1L && e.getCheckable().getMeta().getVioValues().glideVL < 4) {
                    final Checkable.CheckableMeta.ViolationValues vioValues = e.getCheckable().getMeta().getVioValues();
                    ++vioValues.glideVL;
                }
                else if (e.getCheckable().getMeta().getVioValues().glideVL > 0) {
                    final Checkable.CheckableMeta.ViolationValues vioValues2 = e.getCheckable().getMeta().getVioValues();
                    --vioValues2.glideVL;
                }
                if ((e.getCheckable().getMeta().getVioValues().glideVL > 2 && !fp && Math.abs(solve_error) > maxSolve) || (MathHelper.amount(solve_error) > maxSolve && !fp && e.getCheckable().getMeta().getSyncedValues().ticksInAir > 1L)) {
                    int vl = 10;
                    if (MathHelper.diff(e.getYDirectedMotion(), -0.0784) < 2.0E-4) {
                        vl = 19;
                    }
                    if (this.plugin.getRetributionManager().markPlayer(p, vl + ThreadLocalRandom.current().nextInt(0, 5), "Flight", CheatCategory.MOVING, this.getFlagMessage("*12/5", yMovement, lastYMovement, (lastYMovement - 0.08) * 0.98))) {
                        e.getMoveFlagData().setPullSilent(true);
                        e.getMoveFlagData().setPullYMotion((lastYMovement - 0.08) * 0.98);
                    }
                    e.getMoveFlagData().setShouldCheck(false);
                    return;
                }
            }
            if (lastYMovement > 0.0 && yMovement >= 0.0 && !LocationHelper.isOnGroundAccurate(e.getFrom(), (Entity)p) && !LocationHelper.isOnGroundAccurate(e.getTo(), (Entity)p) && IIUA.getCurrentTimeMillis() - e.getCheckable().setupTime > 7000L && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeCollidedWithTopBlock > 300L && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnASlime > 6000L) {
                double normal = 0.0768;
                double allowedPosDiffuse = 0.01;
                if (e.getCheckable().wasCurrentlyHittet()) {
                    allowedPosDiffuse += 0.0;
                }
                for (final PotionEffect effect : p.getActivePotionEffects()) {
                    if (Objects.equals(effect.getType(), PotionEffectType.JUMP)) {
                        normal += 0.02 * (effect.getAmplifier() + 1);
                    }
                }
                final double movementvalidation = lastYMovement - normal - yMovement;
                if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded < 700L && e.getCheckable().getMeta().getSyncedValues().lastVelocityFI != null && movementvalidation > 0.012 && e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.getY() < 0.5 && !LocationHelper.isOnGroundR(e.getFrom()) && !LocationHelper.isOnGroundR(e.getTo()) && !LocationHelper.isOnGround(e.getFrom())) {
                    if (this.plugin.getRetributionManager().markPlayer(p, 30 + ThreadLocalRandom.current().nextInt(-5, 5), "Flight", CheatCategory.MOVING, this.getFlagMessage("*11", yMovement, lastYMovement, lastYMovement + normal))) {
                        final Location d = e.getCache().getCloned(e.getFrom());
                        d.setY(d.getY() - normal);
                        if (!BlockData.doesAffectMovement(d.getBlock())) {
                            e.getMoveFlagData().setPullSilent(true);
                            e.getMoveFlagData().setPullYMotion(lastYMovement - normal);
                        }
                        else {
                            e.setCancelled(true);
                        }
                    }
                    e.getMoveFlagData().setShouldCheck(false);
                    return;
                }
                if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastHitByEntityTimestamp < 700L) {
                    return;
                }
                if (LocationHelper.collidesFar(p.getWorld(), (Entity)p, e.getTo(), Material.CACTUS, Material.WEB)) {
                    return;
                }
                if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastHitByEntityTimestamp < 1400L && e.getCheckable().getMeta().getSyncedValues().ticksOnGround < 1L) {
                    if (movementvalidation < -0.0) {
                        final Checkable.CheckableMeta.ViolationValues vioValues3 = e.getCheckable().getMeta().getVioValues();
                        ++vioValues3.suspiciousMovementDiffsPostHit;
                    }
                }
                else {
                    e.getCheckable().getMeta().getVioValues().suspiciousMovementDiffsPostHit = 0;
                }
                if ((e.getCheckable().getMeta().getVioValues().suspiciousMovementDiffsPostHit > 3 || movementvalidation > allowedPosDiffuse) && e.getYDirectedMotion() != 0.25 && !p.hasPotionEffect(PotionEffectType.JUMP)) {
                    if (MathHelper.diff(e.getYDirectedMotion(), 0.033) < 0.002 && LocationHelper.collidesAnyXZAxis(p.getWorld(), (Entity)p, e.getTo())) {
                        return;
                    }
                    if (MathHelper.diff(lastYMovement, 0.5) < 0.002 && MathHelper.diff(yMovement, 0.0) < 0.002) {
                        return;
                    }
                    if (e.getCheckable().getMeta().getSyncedValues().lastVelocityFI != null && e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.getY() > 0.6) {
                        return;
                    }
                    if (this.plugin.getRetributionManager().markPlayer(p, 35 + ThreadLocalRandom.current().nextInt(-5, 5), "Flight", CheatCategory.MOVING, this.getFlagMessage("*12", yMovement, lastYMovement, (lastYMovement - 0.08) * 0.98))) {
                        final Location d = e.getCache().getCloned(e.getFrom());
                        d.setY(d.getY() + normal);
                        if (!BlockData.doesAffectMovement(d.getBlock())) {
                            e.getMoveFlagData().setPullSilent(true);
                            e.getMoveFlagData().setPullYMotion(lastYMovement + normal);
                        }
                        else {
                            e.setCancelled(true);
                        }
                    }
                    e.getMoveFlagData().setShouldCheck(false);
                }
            }
            return;
        }
        if (MathHelper.diff(e.getYDirectedMotion(), 0.33319999) < 3.0E-5 || (MathHelper.diff(e.getYDirectedMotion(), 5.0E-4) < 2.0E-4 && MathHelper.diff(lastYMovement, 0.1207) < 2.0E-4)) {
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeInWater < 500L && e.getYDirectedMotion() < 0.1 && e.getYDirectedMotion() > -0.1) {
            return;
        }
        if (CrossVersionSupply.getCurrentCFR().getBlockData().isStepable(e.getCache().getClonedAdding(e.getTo(), 0.0, 2.0, 0.0).getBlock())) {
            return;
        }
        if (this.plugin.getRetributionManager().markPlayer(p, 34 + ThreadLocalRandom.current().nextInt(0, 5), "Flight", CheatCategory.MOVING, this.getFlagMessage("*12/4", yMovement, lastYMovement, (lastYMovement - 0.08) * 0.98))) {
            e.getMoveFlagData().setPullSilent(true);
            final double expected = ((lastYMovement - 0.08) * 0.98 > 0.0) ? -0.078 : ((lastYMovement - 0.08) * 0.98);
            e.getMoveFlagData().setPullYMotion(expected);
        }
        e.getMoveFlagData().setShouldCheck(false);
    }
    
    private void checkWeb(final CheckableMoveEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (!e.getMoveFlagData().shouldCheck()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        if (PlayerUtils.getAllowFlight(p, true, true, true, true, false)) {
            return;
        }
        if (e.isMoving() && LocationHelper.collides(p.getWorld(), (Entity)p, Material.WEB) && LocationHelper.onlyCollides(p.getWorld(), (Entity)p, e.getTo(), Material.WEB, Material.AIR) && LocationHelper.onlyCollides(p.getWorld(), (Entity)p, e.getFrom(), Material.WEB, Material.AIR)) {
            if (e.getYDirectedMotion() >= 0.0 && e.getCache().getCloned(e.getFrom()).subtract(0.0, 0.0785, 0.0).getBlock().getType().equals((Object)Material.WEB)) {
                if (e.getCheckable().getMeta().getVioValues().webFlyVL > 4) {
                    if (this.plugin.getRetributionManager().markPlayer(p, 30 + ThreadLocalRandom.current().nextInt(1, 5), "Flight", CheatCategory.MOVING, this.getFlagMessage("*14", e.getYDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastYmovement, e.getCheckable().getMeta().getSyncedValues().lastYmovement - 0.0785))) {
                        e.getMoveFlagData().setPullSilent(true);
                        e.getMoveFlagData().setPullYMotion(-0.1);
                    }
                    e.getMoveFlagData().setShouldCheck(false);
                }
                if (e.getCheckable().getMeta().getVioValues().webFlyVL < 100) {
                    final Checkable.CheckableMeta.ViolationValues vioValues = e.getCheckable().getMeta().getVioValues();
                    vioValues.webFlyVL += 2;
                }
            }
            else if (e.getCheckable().getMeta().getVioValues().webFlyVL > 0) {
                final Checkable.CheckableMeta.ViolationValues vioValues2 = e.getCheckable().getMeta().getVioValues();
                --vioValues2.webFlyVL;
            }
        }
        else if (e.getCheckable().getMeta().getVioValues().webFlyVL > 1) {
            final Checkable.CheckableMeta.ViolationValues vioValues3 = e.getCheckable().getMeta().getVioValues();
            vioValues3.webFlyVL -= 2;
        }
        if (e.getFrom().getBlock().getType().equals((Object)Material.WEB) && e.getTo().getBlock().getType().equals((Object)Material.WEB)) {
            if (e.getTo().getX() == e.getFrom().getX() && e.getTo().getZ() == e.getFrom().getZ()) {
                return;
            }
            final double yMotion = e.getYDirectedMotion();
            if (yMotion > 0.0 && BlockData.isPassable(e.getCache().getCloned(e.getTo()).subtract(0.0, 0.1, 0.0).getBlock()) && e.getBukkitPlayer().getVelocity().getY() < 0.0) {
                if (this.plugin.getRetributionManager().markPlayer(p, 30 + ThreadLocalRandom.current().nextInt(1, 5), "Flight", CheatCategory.MOVING, this.getFlagMessage("*15", MathHelper.amount(e.getYDirectedMotion()), e.getCheckable().getMeta().getSyncedValues().lastYmovement, e.getCheckable().getMeta().getSyncedValues().lastYmovement - 0.0785))) {
                    e.getMoveFlagData().setPullSilent(true);
                    e.getMoveFlagData().setPullYMotion(0.1);
                }
                e.getMoveFlagData().setShouldCheck(false);
                return;
            }
            if (MathHelper.diff(yMotion, -0.0273) < 0.0 || yMotion > 0.1) {
                if (this.plugin.getRetributionManager().markPlayer(p, 30 + ThreadLocalRandom.current().nextInt(1, 5), "Flight", CheatCategory.MOVING, this.getFlagMessage("*16", e.getYDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastYmovement, e.getCheckable().getMeta().getSyncedValues().lastYmovement - 0.0785))) {
                    e.getMoveFlagData().setBukkitCancelled(true);
                }
                e.getMoveFlagData().setShouldCheck(false);
            }
        }
    }
    
    private void checkOnGroundReleated(final CheckableMoveEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (!e.getMoveFlagData().shouldCheck()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        final double yDiff = e.getTo().getY() - e.getFrom().getY();
        final double lastYDiff = e.getCheckable().getMeta().getSyncedValues().lastYmovement;
        if (PlayerUtils.getAllowFlight(p, true, true, true)) {
            return;
        }
        if (lastYDiff == yDiff && !IIUA.isPretendingToBeOnGround(p) && !p.isInsideVehicle() && !LocationHelper.collidesFar(p.getWorld(), (Entity)p, e.getFrom(), Material.WEB) && e.getCheckable().getMeta().getSyncedValues().ticksInAir_experimental > 4L && (e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.getY() != e.getYDirectedMotion() || IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded > 500L)) {
            if (e.getCheckable().getMeta().getVioValues().suspiciousOnGroundMovements < 12) {
                final Checkable.CheckableMeta.ViolationValues vioValues = e.getCheckable().getMeta().getVioValues();
                vioValues.suspiciousOnGroundMovements += 4;
            }
            else {
                if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeFlagged < 500L) {
                    return;
                }
                if (LocationHelper.collidesLiquid((Entity)p)) {
                    return;
                }
                if (BlockData.isClimbable(e.getTo().getBlock())) {
                    return;
                }
                if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastGameModeChange < 500L) {
                    return;
                }
                if (PlayerUtils.hasLevitation(p)) {
                    return;
                }
                if (LocationHelper.hasWoopableBlockNearby(e.getTo(), (Entity)p)) {
                    return;
                }
                final double jumpBoost = p.getActivePotionEffects().stream().filter(effect -> effect.getType().equals((Object)PotionEffectType.JUMP)).mapToDouble(effect -> 0.1 * (effect.getAmplifier() + 1)).sum();
                if (this.plugin.getRetributionManager().markPlayer(p, 30, "Flight", CheatCategory.MOVING, this.getFlagMessage("*17", e.getYDirectedMotion(), lastYDiff, 0.42 + jumpBoost))) {
                    e.getCheckable().getMeta().getVioValues().lastTimeSuspiciousForGroundFly = IIUA.getCurrentTimeMillis();
                    e.getMoveFlagData().setPullSilent(true);
                    e.getMoveFlagData().setPullYMotion((e.getCheckable().getMeta().getSyncedValues().lastYmovement - 0.08) * 0.98);
                }
                e.getMoveFlagData().setShouldCheck(false);
            }
        }
    }
    
    private void checkOnGroundHop(final CheckableMoveEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (!e.getMoveFlagData().shouldCheck()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastHitByEntityTimestamp < 500L) {
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastDamageTakenTimestamp < 500L) {
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeCollidedWithTopBlock < 1000L) {
            return;
        }
        final double yMovement = e.getYDirectedMotion();
        if (yMovement == 0.0 || MathHelper.diff(yMovement, 0.42) < 0.01 || PotionUtil.getPotionEffectAmplifier(p, PotionEffectType.JUMP) > 100.0) {
            return;
        }
        if (LocationHelper.hasStepableNearby(p, e.getFrom()) || LocationHelper.hasStepableNearby(p, e.getTo()) || LocationHelper.hasLiquitNearby(e.getTo())) {
            return;
        }
        if (e.getCheckable().getMeta().getSyncedValues().ticksInLiquid > 0L) {
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnASlime < 2000L) {
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastDamageTakenTimestamp < 1000L) {
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeClimbedALadder < 1400L || BlockData.isClimbable(e.getTo().getBlock())) {
            return;
        }
        if (p.getVelocity().getY() > 0.0 && e.getYDirectedMotion() > 0.0 && IIUA.isPretendingToBeOnGround(p) && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeClimbedALadder > 200L) {
            if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getVioValues().lastTimeSuspiciousForVelocityFly < 20000L) {
                if (this.plugin.getRetributionManager().markPlayer(p, 12 + ThreadLocalRandom.current().nextInt(1, 5), "Flight", CheatCategory.MOVING, this.getFlagMessage("*19", yMovement, e.getCheckable().getMeta().getSyncedValues().lastYmovement, 0.0))) {
                    e.setCancelled(true);
                }
                e.getMoveFlagData().setShouldCheck(false);
            }
            e.getCheckable().getMeta().getVioValues().lastTimeSuspiciousForVelocityFly = IIUA.getCurrentTimeMillis();
        }
    }
    
    private void checkGravity(final CheckableMoveEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (!e.getMoveFlagData().shouldCheck()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        if (IIUA.getAllowFlight(p) || e.isCancelled()) {
            return;
        }
        final double solve_error = e.getYDirectedMotion() / 0.98 + 0.08 - e.getCheckable().getMeta().getSyncedValues().lastYmovement;
        if (e.getCheckable().getMeta().getSyncedValues().lastYmovement > e.getYDirectedMotion() + 1.0E-5 && solve_error < (e.getCheckable().getMeta().getSyncedValues().isInSlimeJump ? -0.08 : -0.02)) {
            if (e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.getY() <= 0.0 && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded < 1000 + e.getCheckable().getPing()) {
                return;
            }
            if (BlockData.isLiquid(e.getFrom().getBlock()) || BlockData.isLiquid(LocationHelper.getRelative(e.getFrom().getBlock(), BlockFace.DOWN))) {
                return;
            }
            if (!p.isInsideVehicle() && (e.getYDirectedMotion() < -0.201 || (e.getCheckable().getMeta().getSyncedValues().lastYmovement > 0.2 && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeCollidedWithTopBlock > 1000L && e.getYDirectedMotion() < 0.0)) && IIUA.getCurrentTimeMillis() - e.getCheckable().setupTime > 3000L) {
                if (LocationHelper.collidesEFar(p.getWorld(), (Entity)p, e.getFrom(), Material.PISTON_EXTENSION, Material.PISTON_MOVING_PIECE, Material.PISTON_BASE, Material.PISTON_STICKY_BASE)) {
                    return;
                }
                if (LocationHelper.collidesEFar(p.getWorld(), (Entity)p, e.getTo(), Material.PISTON_EXTENSION, Material.PISTON_MOVING_PIECE, Material.PISTON_BASE, Material.PISTON_STICKY_BASE)) {
                    return;
                }
                if (LocationHelper.collidesStepableIgnoringTopBlock(p.getWorld(), (Entity)p, e.getFrom()) || LocationHelper.collidesStepableIgnoringTopBlock(p.getWorld(), (Entity)p, e.getTo().clone().add(0.0, 1.0, 0.0))) {
                    return;
                }
                if (LocationHelper.hasWoopableBlockNearby(e.getTo(), (Entity)p)) {
                    return;
                }
                if (this.plugin.getRetributionManager().markPlayer(p, ThreadLocalRandom.current().nextInt(20, 30), "Flight", CheatCategory.MOVING, this.getFlagMessage("*97", e.getYDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastYmovement, (e.getCheckable().getMeta().getSyncedValues().lastYmovement - 0.08) * 0.98))) {
                    e.getCheckable().getMeta().getSyncedValues().hadGoodVelocity = false;
                    e.getMoveFlagData().setPullSilent(true);
                    e.getMoveFlagData().setPullYMotion((e.getCheckable().getMeta().getSyncedValues().lastYmovement - 0.08) * 0.98);
                }
                e.getMoveFlagData().setShouldCheck(false);
            }
        }
    }
    
    private void checkGlide2(final CheckableMoveEvent e) {
        final Player p = e.getBukkitPlayer();
        if (!e.getMoveFlagData().shouldCheck()) {
            return;
        }
        if (p.isInsideVehicle() || e.isCancelled()) {
            return;
        }
        if (PlayerUtils.getAllowFlight(p, false, true, true)) {
            return;
        }
        if (e.getYDirectedMotion() < -2.0) {
            return;
        }
        final double ydiff = e.getYDirectedMotion();
        final double lastYDiff = e.getCheckable().getMeta().getSyncedValues().lastYmovement;
        if (e.isOnGround()) {
            return;
        }
        if (lastYDiff < 0.0 && ydiff < 0.0 && ydiff > lastYDiff && e.getCheckable().getMeta().getSyncedValues().ticksInAir > 1L) {
            final boolean og = LocationHelper.isOnGroundLessAccurate(e.getTo(), 0.5) || LocationHelper.isOnGroundLessAccurate(e.getFrom(), 0.5) || LocationHelper.isOnGroundF(e.getTo()) || LocationHelper.isOnGroundF(e.getFrom()) || LocationHelper.collidesAnyStepable(e.getTo().getWorld(), (Entity)p, e.getTo(), 0.2) || LocationHelper.collidesAnyStepable(e.getFrom().getWorld(), (Entity)p, e.getFrom(), 0.2) || LocationHelper.hasLiquitNearby(e.getTo());
            if (og) {
                return;
            }
            if (LocationHelper.hasWoopableBlockNearby(e.getTo(), (Entity)p)) {
                return;
            }
            final double expected = (e.getCheckable().getMeta().getSyncedValues().lastYmovement - 0.08) * 0.98;
            if (this.plugin.getRetributionManager().markPlayer(p, 35, "Flight", CheatCategory.MOVING, this.getFlagMessage("*29/3", e.getYDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastYmovement, expected))) {
                e.getMoveFlagData().setPullYMotion(expected);
                e.getMoveFlagData().setPullSilent(true);
            }
            e.getMoveFlagData().setShouldCheck(false);
        }
    }
    
    private void checkGlide(final CheckableMoveEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (!e.getMoveFlagData().shouldCheck()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        if (p.isInsideVehicle()) {
            return;
        }
        if (PlayerUtils.hasLevitation(p)) {
            return;
        }
        if (PlayerUtils.getAllowFlight(p, false, true, true)) {
            return;
        }
        if (e.getYDirectedMotion() < -2.0) {
            return;
        }
        if (IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastHitByEntityTimestamp < 900L) {
            return;
        }
        final double ydiff = e.getYDirectedMotion();
        final double lastYDiff = e.getCheckable().getMeta().getSyncedValues().lastYmovement;
        final double movementFailture = lastYDiff - 0.0753 - ydiff + MathHelper.map(p.getFallDistance(), 12.0, 92.0, 0.015, 0.025);
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnASlime < 2000L) {
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeInWater < 2000L) {
            return;
        }
        if (movementFailture > 0.22 && lastYDiff < 0.0 && !BlockData.doesAffectMovement(e.getTo().getBlock().getRelative(BlockFace.UP)) && MathHelper.diff(e.getYDirectedMotion(), 0.0) > 0.01 && MathHelper.diff(movementFailture, 0.4382) > 0.002) {
            if (this.plugin.getRetributionManager().markPlayer(p, (int)(MathHelper.map(movementFailture, 0.22, 2.0, 20.0, 100.0) + ThreadLocalRandom.current().nextInt(1, 5)), "Flight", CheatCategory.MOVING, this.getFlagMessage("*21", ydiff, lastYDiff, (lastYDiff - 0.08) * 0.98))) {
                e.getMoveFlagData().setPullSilent(true);
                e.getMoveFlagData().setPullYMotion((e.getCheckable().getMeta().getSyncedValues().lastYmovement - 0.08) * 0.98);
            }
            e.getMoveFlagData().setShouldCheck(false);
        }
        if (LocationHelper.collidesStepable(p.getWorld(), (Entity)p, e.getTo()) || LocationHelper.collidesStepable(p.getWorld(), (Entity)p, e.getFrom()) || LocationHelper.collidesFar(p.getWorld(), (Entity)p, e.getTo(), Material.WEB) || LocationHelper.collidesLiquid((Entity)p, e.getFrom()) || LocationHelper.collidesLiquid((Entity)p, e.getTo()) || LocationHelper.hasWoopableBlockNearby(e.getFrom(), (Entity)p) || LocationHelper.hasWoopableBlockNearby(e.getTo(), (Entity)p) || LocationHelper.isOnGroundAccurate(e.getFrom(), (Entity)p)) {
            return;
        }
        if (IIUA.isPretendingToBeOnGround(p) || ydiff > 0.0 || LocationHelper.isOnGround(e.getTo())) {
            if (e.getCheckable().getMeta().getVioValues().suspiciousFallMoves > 0) {
                final Checkable.CheckableMeta.ViolationValues vioValues = e.getCheckable().getMeta().getVioValues();
                --vioValues.suspiciousFallMoves;
            }
            return;
        }
        if (movementFailture < -0.028) {
            final Checkable.CheckableMeta.ViolationValues vioValues2 = e.getCheckable().getMeta().getVioValues();
            vioValues2.suspiciousFallMoves += 2;
        }
        else if (e.getCheckable().getMeta().getVioValues().suspiciousFallMoves > 0) {
            final Checkable.CheckableMeta.ViolationValues vioValues3 = e.getCheckable().getMeta().getVioValues();
            --vioValues3.suspiciousFallMoves;
        }
        if (e.getCheckable().getMeta().getVioValues().suspiciousFallMoves > 2 || ydiff > 0.0) {
            if (this.plugin.getRetributionManager().markPlayer(p, 27 + ThreadLocalRandom.current().nextInt(1, 9), "Flight", CheatCategory.MOVING, this.getFlagMessage("*22", ydiff, lastYDiff, lastYDiff - 0.0785))) {
                final Location d = e.getCache().getCloned(e.getFrom());
                d.setY(d.getY() - 0.0753 + MathHelper.map(p.getFallDistance(), 12.0, 92.0, 0.015, 0.025));
                if (!BlockData.doesAffectMovement(d.getBlock())) {
                    e.getMoveFlagData().setPullSilent(true);
                    e.getMoveFlagData().setPullYMotion((e.getCheckable().getMeta().getSyncedValues().lastYmovement - 0.08) * 0.98);
                }
                else {
                    this.plugin.getRetributionManager().handleLocationCorrection(p, true);
                }
            }
            e.getMoveFlagData().setShouldCheck(false);
        }
    }
    
    private void checkWater(final CheckableMoveEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (!e.getMoveFlagData().shouldCheck()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        if (PlayerUtils.getAllowFlight(p, false, true, false, true, true)) {
            return;
        }
        final boolean fullyInWater = !LocationHelper.collidesACLiquit(p.getWorld(), (Entity)p, e.getFrom()) && LocationHelper.onlyCollides(e.getTo().getWorld(), (Entity)e.getBukkitPlayer(), e.getTo(), Material.AIR, Material.WATER, Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA);
        if (e.getYDirectedMotion() == 0.34 && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getVioValues().lastTimeSuspiciousForJesus > 2000L) {
            e.getCheckable().getMeta().getVioValues().lastTimeSuspiciousForJesus = IIUA.getCurrentTimeMillis();
            return;
        }
        final boolean liquidCollide = LocationHelper.collidesLiquid((Entity)p, e.getFrom()) && LocationHelper.collidesLiquid((Entity)p, e.getTo());
        if (fullyInWater && liquidCollide && BlockData.isLiquid(e.getFrom().getBlock()) && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded > 1000L) {
            final double max = (e.getCheckable().getMeta().getSyncedValues().ticksInLiquid > 20L) ? (e.getCheckable().hasWaterPower() ? 0.2 : 0.15) : 0.25;
            if (e.getYDirectedMotion() > max && IIUA.getDataFromBlock(e.getCheckable().getVerifiedLocation().getBlock()) == 0) {
                e.getCheckable().getMeta().getVioValues().lastTimeSuspiciousForJesus = IIUA.getCurrentTimeMillis();
                if (this.plugin.getRetributionManager().markPlayer(p, 19 + ThreadLocalRandom.current().nextInt(1, 5), "Flight", CheatCategory.MOVING, this.getFlagMessage("*23", e.getYDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastYmovement, -0.19))) {
                    e.getMoveFlagData().setPullSilent(true);
                    e.getMoveFlagData().setPullYMotion(-0.7);
                }
                e.getMoveFlagData().setShouldCheck(false);
            }
        }
        if (!LocationHelper.hasLiquitNearby(e.getTo().getBlock().getRelative(BlockFace.UP).getLocation()) && liquidCollide && fullyInWater) {
            final double d = MathHelper.diff(e.getCheckable().getMeta().getSyncedValues().lastYmovement, e.getYDirectedMotion());
            if (e.getYDirectedMotion() <= 0.995 && (MathHelper.diff(d, 0.0) >= 0.1 || (MathHelper.diff(d, 0.0) < 1.0E-4 && !e.getCheckable().hasWaterPower() && e.getCheckable().getMeta().getSyncedValues().ticksInLiquitFloating > 5L && e.getTo().getBlock().getRelative(BlockFace.DOWN).isLiquid()))) {
                if (e.getCheckable().getMeta().getVioValues().suspiciousJesusMoves < 10) {
                    final Checkable.CheckableMeta.ViolationValues vioValues = e.getCheckable().getMeta().getVioValues();
                    ++vioValues.suspiciousJesusMoves;
                }
                if (e.getCheckable().getMeta().getVioValues().suspiciousJesusMoves > 3 && ThreadLocalRandom.current().nextBoolean()) {
                    e.getCheckable().getMeta().getVioValues().lastTimeSuspiciousForJesus = IIUA.getCurrentTimeMillis();
                    if (this.plugin.getRetributionManager().markPlayer(p, 5 + ThreadLocalRandom.current().nextInt(1, 5), "Flight", CheatCategory.MOVING, this.getFlagMessage("*25", e.getYDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastYmovement, -MathHelper.map(e.getCheckable().getMeta().getVioValues().suspiciousJesusMoves, 1.0, 5.0, 0.03 + ThreadLocalRandom.current().nextDouble(0.005, 0.03), 0.1)))) {
                        e.getMoveFlagData().setPullSilent(true);
                        e.getMoveFlagData().setPullYMotion(-1.0);
                    }
                    e.getMoveFlagData().setShouldCheck(false);
                }
            }
            else if (e.getCheckable().getMeta().getVioValues().suspiciousJesusMoves > 0) {
                final Checkable.CheckableMeta.ViolationValues vioValues2 = e.getCheckable().getMeta().getVioValues();
                --vioValues2.suspiciousJesusMoves;
            }
        }
    }
    
    private void checkWater2(final CheckableMoveEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (!e.getMoveFlagData().shouldCheck()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        if (PlayerUtils.getAllowFlight(p, false, true, false, true, true)) {
            return;
        }
        final boolean fullyInWater = LocationHelper.collidesACLiquit(p.getWorld(), (Entity)p, e.getFrom()) && LocationHelper.onlyCollidesFar(e.getTo().getWorld(), (Entity)e.getBukkitPlayer(), e.getTo(), Material.AIR, Material.WATER, Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA);
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded < 1000L) {
            return;
        }
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).isLiquid() && fullyInWater && LocationHelper.collidesLiquid((Entity)p, e.getCache().getCloned(e.getFrom()).subtract(0.0, 0.2, 0.0)) && LocationHelper.collidesLiquid((Entity)p, e.getCache().getCloned(e.getTo()).subtract(0.0, 0.2, 0.0))) {
            if (e.getYDirectedMotion() == 0.0 && e.isMoving() && e.getCheckable().getMeta().getSyncedValues().lastYmovement == 0.0) {
                if (this.plugin.getRetributionManager().markPlayer(p, 49 + ThreadLocalRandom.current().nextInt(1, 5), "Flight", CheatCategory.MOVING, this.getFlagMessage("*26", e.getYDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastYmovement, -MathHelper.map(e.getCheckable().getMeta().getVioValues().suspiciousJesusMoves, 1.0, 5.0, 0.03 + ThreadLocalRandom.current().nextDouble(0.005, 0.03), 0.1)))) {
                    e.getMoveFlagData().setPullSilent(true);
                    e.getMoveFlagData().setPullYMotion(-0.23);
                }
                e.getMoveFlagData().setShouldCheck(false);
            }
            if (!BlockData.doesAffectMovement(e.getTo().getBlock()) && !BlockData.isLiquid(e.getTo().getBlock()) && IIUA.getDataFromBlock(e.getFrom().getBlock()) < 2 && IIUA.getDataFromBlock(e.getTo().getBlock()) < 2 && !e.getCheckable().hasWaterPower() && e.getYDirectedMotion() > 0.0) {
                if (this.plugin.getRetributionManager().markPlayer(p, 49 + ThreadLocalRandom.current().nextInt(1, 5), "Flight", CheatCategory.MOVING, this.getFlagMessage("*27", e.getYDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastYmovement, -MathHelper.map(e.getCheckable().getMeta().getVioValues().suspiciousJesusMoves, 1.0, 5.0, 0.03 + ThreadLocalRandom.current().nextDouble(0.005, 0.03), 0.1)))) {
                    e.getMoveFlagData().setPullSilent(true);
                    e.getMoveFlagData().setPullYMotion(-0.23);
                }
                e.getMoveFlagData().setShouldCheck(false);
            }
        }
    }
    
    private void liquidCheck(final CheckableMoveEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (!e.getMoveFlagData().shouldCheck()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        if (PlayerUtils.getAllowFlight(p, false, false, false, true, false)) {
            return;
        }
        final boolean isInWater = LocationHelper.collidesNearACLiquit(p.getWorld(), (Entity)p, e.getFrom());
        final boolean collidesNearby = LocationHelper.collidesAnySMRXZAxis(p.getWorld(), (Entity)p, e.getTo()) && !BlockData.isLiquid(e.getCache().getCloned(e.getTo()).add(0.0, 1.6, 0.0).getBlock());
        final double yDiffDiff = e.getYDirectedMotion() - e.getCheckable().getMeta().getSyncedValues().lastYmovement;
        if (isInWater) {}
        if (e.getYDirectedMotion() < 0.136 && e.getCheckable().getMeta().getSyncedValues().lastYmovement < 0.136 && e.getCheckable().getMeta().getSyncedValues().lastYmovement > 0.02 && !collidesNearby && isInWater) {
            final boolean fullyInWater = LocationHelper.onlyCollidesFar(e.getTo().getWorld(), (Entity)e.getBukkitPlayer(), e.getTo(), Material.AIR, Material.WATER, Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA);
            final boolean fc = e.getYDirectedMotion() != 0.0 && (e.getYDirectedMotion() > 0.09 || fullyInWater);
            if (Math.abs(yDiffDiff) > 0.2 || (Math.abs(yDiffDiff) < 1.0E-4 - (e.getCheckable().hasWaterPower() ? 9.9E-5 : 0.0) && fc)) {
                if (e.getCheckable().getMeta().getVioValues().stableWaterYMotionVL > 40) {
                    if (this.plugin.getRetributionManager().markPlayer(p, 5, "Flight", CheatCategory.MOVING, this.getFlagMessage("*122/3", e.getYDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastYmovement, -0.0941 - ThreadLocalRandom.current().nextDouble(0.1, 0.3)))) {
                        e.getMoveFlagData().setPullSilent(true);
                        e.getMoveFlagData().setPullYMotion(-0.5);
                    }
                    e.getMoveFlagData().setShouldCheck(false);
                }
                if (e.getCheckable().getMeta().getVioValues().stableWaterYMotionVL < 40) {
                    final Checkable.CheckableMeta.ViolationValues vioValues = e.getCheckable().getMeta().getVioValues();
                    vioValues.stableWaterYMotionVL += 10;
                }
            }
            else if (e.getCheckable().getMeta().getVioValues().stableWaterYMotionVL > 0) {
                final Checkable.CheckableMeta.ViolationValues vioValues2 = e.getCheckable().getMeta().getVioValues();
                --vioValues2.stableWaterYMotionVL;
            }
        }
        else if (e.getCheckable().getMeta().getVioValues().stableWaterYMotionVL > 0) {
            final Checkable.CheckableMeta.ViolationValues vioValues3 = e.getCheckable().getMeta().getVioValues();
            --vioValues3.stableWaterYMotionVL;
        }
        if (isInWater && e.getCheckable().getMeta().getSyncedValues().lastYmovement < -0.105 - (e.getCheckable().hasWaterPower() ? 0.09 : 0.0) && !LocationHelper.collidesStepable(p.getWorld(), (Entity)p, e.getFrom()) && BlockData.isLiquid(e.getCache().getCloned(e.getFrom()).subtract(0.0, 0.3, 0.0).getBlock()) && e.getYDirectedMotion() > 0.02 && e.getYDirectedMotion() < 0.3) {
            if (this.plugin.getRetributionManager().markPlayer(p, 10, "Flight", CheatCategory.MOVING, this.getFlagMessage("*122/8", e.getYDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastYmovement, -0.0941 - ThreadLocalRandom.current().nextDouble(0.1, 0.3)))) {
                e.getMoveFlagData().setPullSilent(true);
                e.getMoveFlagData().setPullYMotion(-ThreadLocalRandom.current().nextDouble(0.1, 0.5));
            }
            e.getMoveFlagData().setShouldCheck(false);
        }
        if (e.getYDirectedMotion() > 0.135 && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded > 1000L && e.getCheckable().getMeta().getSyncedValues().lastYmovement < 0.1 && !collidesNearby && isInWater) {
            final boolean cLT = LocationHelper.collidesAnySMRXZAxis(p.getWorld(), (Entity)p, e.getFrom()) && !BlockData.isLiquid(e.getCache().getCloned(e.getFrom()).add(0.0, 1.6, 0.0).getBlock());
            if (!cLT) {
                if (this.plugin.getRetributionManager().markPlayer(p, 37, "Flight", CheatCategory.MOVING, this.getFlagMessage("*122/1", e.getYDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastYmovement, -0.0941 - ThreadLocalRandom.current().nextDouble(0.1, 0.3)))) {
                    e.getMoveFlagData().setPullSilent(true);
                    e.getMoveFlagData().setPullYMotion(-0.1);
                }
                e.getMoveFlagData().setShouldCheck(false);
            }
        }
        if (!BlockData.isLiquid(e.getTo().getBlock()) && BlockData.isLiquid(e.getFrom().getBlock()) && e.getTo().getBlock().getY() > e.getFrom().getBlock().getY()) {
            final boolean fullyInWater = LocationHelper.onlyCollidesFar(e.getFrom().getWorld(), (Entity)e.getBukkitPlayer(), e.getFrom(), Material.WATER, Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA);
            if (!collidesNearby && fullyInWater && e.getCheckable().getMeta().getSyncedValues().lastYmovement < 0.2 && isInWater) {
                if (this.plugin.getRetributionManager().markPlayer(p, 38, "Flight", CheatCategory.MOVING, this.getFlagMessage("*122/2", e.getYDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastYmovement, -0.0941 - ThreadLocalRandom.current().nextDouble(0.1, 0.3)))) {
                    e.getMoveFlagData().setPullSilent(true);
                    e.getMoveFlagData().setPullYMotion(-0.1);
                }
                e.getMoveFlagData().setShouldCheck(false);
            }
        }
        if (e.getYDirectedMotion() == 0.0 && e.isMoving() && e.getCheckable().getMeta().getSyncedValues().lastYmovement <= 0.0 && LocationHelper.collidesNearACLiquit(p.getWorld(), (Entity)p, e.getCache().getCloned(e.getTo()).subtract(0.0, 0.35, 0.0)) && LocationHelper.onlyCollides(e.getTo().getWorld(), (Entity)p, e.getCache().getCloned(e.getTo()).subtract(0.0, 0.15, 0.0), Material.AIR, Material.WATER, Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA) && LocationHelper.onlyCollides(e.getFrom().getWorld(), (Entity)p, e.getCache().getCloned(e.getFrom()).subtract(0.0, 0.15, 0.0), Material.AIR, Material.WATER, Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA)) {
            if (this.plugin.getRetributionManager().markPlayer(p, 5, "Flight", CheatCategory.MOVING, this.getFlagMessage("*122/5", e.getYDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastYmovement, -0.0941 - ThreadLocalRandom.current().nextDouble(0.1, 0.3)))) {
                e.getMoveFlagData().setPullSilent(true);
                e.getMoveFlagData().setPullYMotion(-0.0941 - ThreadLocalRandom.current().nextDouble(0.1, 0.3));
            }
            e.getMoveFlagData().setShouldCheck(false);
        }
        if ((e.getTo().getBlock().getType().equals((Object)Material.WEB) || e.getCache().getCloned(e.getTo()).add(0.0, 0.8, 0.0).getBlock().getType().equals((Object)Material.WEB)) && LocationHelper.unpassableCollideCode(e.getCache().getCloned(e.getTo()).add(0.0, 0.75, 0.0)) > 3 && BlockData.isLiquid(e.getCache().getCloned(e.getTo()).subtract(0.0, 0.75, 0.0).getBlock())) {
            if (e.isMoving() && e.getYDirectedMotion() < -0.02) {
                e.getCheckable().getMeta().getSyncedValues().lookPacketsInWebCounter = 0;
            }
            else if (e.isRotating()) {
                final int vl = ++e.getCheckable().getMeta().getSyncedValues().lookPacketsInWebCounter;
                if (vl > 40) {
                    if (this.plugin.getRetributionManager().markPlayer(p, 5, "Flight", CheatCategory.MOVING, this.getFlagMessage("*122/6", e.getYDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastYmovement, -0.0784))) {
                        e.getMoveFlagData().setPullSilent(true);
                        e.getMoveFlagData().setPullYMotion(-0.0625);
                    }
                    e.getMoveFlagData().setShouldCheck(false);
                }
            }
        }
    }
    
    private void checkSpider(final CheckableMoveEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (!e.getMoveFlagData().shouldCheck()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        final boolean doesCollide = LocationHelper.collidesAnyXZAxisNY(e.getTo().getWorld(), (Entity)p, e.getFrom());
        if (p.isInsideVehicle()) {
            return;
        }
        if (LocationHelper.hasWoopableBlockNearby(e.getFrom(), (Entity)p)) {
            return;
        }
        if (PlayerUtils.getAllowFlight(p, false, false, false, false, true)) {
            return;
        }
        if (PlayerUtils.hasLevitation(p)) {
            return;
        }
        if (LocationHelper.hasStepableNoTopNearby(e.getFrom()) && e.getXZDirectedMotion() > 0.48 && e.getYDirectedMotion() > 0.4) {
            return;
        }
        if (!e.getCheckable().getMeta().getSyncedValues().hadYVelocity29F && MathHelper.diff(e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.getY(), e.getYDirectedMotion()) < 0.001) {
            e.getCheckable().getMeta().getSyncedValues().hadYVelocity29F = true;
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastFlightToggle < 500L) {
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastNearbyBlockPhysicsEvent < 400 + e.getCheckable().getPing() && e.getCheckable().getMeta().getSyncedValues().lastYmovement >= -0.2 && e.getYDirectedMotion() >= 0.0) {
            return;
        }
        final double g = 0.0;
        if (e.getCheckable().getMeta().getSyncedValues().lastYmovement > 0.0 && e.getYDirectedMotion() > 0.0 && e.getYDirectedMotion() >= e.getCheckable().getMeta().getSyncedValues().lastYmovement && !LocationHelper.collidesLiquid((Entity)p, e.getTo()) && !LocationHelper.isOnGroundAccurate(e.getFrom(), (Entity)p) && !LocationHelper.isOnGroundAccurate(e.getTo(), (Entity)p) && !LocationHelper.hasLiquitNearby(e.getFrom()) && !LocationHelper.collidesLiquid((Entity)p, e.getTo())) {
            boolean flag = !LocationHelper.collidesXZAxis(p.getWorld(), (Entity)p, e.getTo(), Material.SNOW) || String.valueOf(e.getYDirectedMotion()).length() > 5;
            if (LocationHelper.collidesAnyStepable(e.getTo().getWorld(), (Entity)p, e.getTo(), 0.0)) {
                if (!BlockData.isPassable(e.getCache().getCloned(e.getFrom()).subtract(0.0, 0.2, 0.0).getBlock()) || BlockData.isStepable(e.getFrom().getBlock().getRelative(BlockFace.DOWN), e.getFrom()) || BlockData.isLiquid(e.getFrom().getBlock().getRelative(BlockFace.DOWN)) || e.getYDirectedMotion() == e.getCheckable().getMeta().getSyncedValues().lastYmovement || (e.getYDirectedMotion() >= 0.5 && e.getCheckable().getMeta().getSyncedValues().lastYmovement > 0.2)) {
                    flag = false;
                }
                if (BlockData.isClimbable(e.getFrom().getBlock().getRelative(BlockFace.DOWN))) {
                    flag = false;
                }
            }
            if (BlockData.doesAffectMovement(e.getFrom().getBlock().getRelative(BlockFace.DOWN)) && LocationHelper.collidesStepable(p.getWorld(), (Entity)p, e.getFrom()) && e.getYDirectedMotion() > 0.419 && MathHelper.diff(e.getYDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastYmovement) > 0.01) {
                flag = false;
            }
            if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded < 800 + e.getCheckable().getPing()) {
                final double xD = MathHelper.diff(e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.getY(), e.getYDirectedMotion());
                if (Math.abs(xD) < 1.0E-9) {
                    flag = false;
                }
            }
            if (BlockData.isLiquid(e.getFrom().getBlock()) || BlockData.isLiquid(e.getTo().getBlock())) {
                flag = false;
            }
            else if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeInWater < 1000L && e.getYDirectedMotion() < 0.3) {
                flag = false;
            }
            if (flag) {
                if (this.plugin.getRetributionManager().markPlayer(p, 40, "Flight", CheatCategory.MOVING, this.getFlagMessage("*29/2", e.getYDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastYmovement, (e.getCheckable().getMeta().getSyncedValues().lastYmovement - 0.08) * 0.98))) {
                    e.getMoveFlagData().setPullSilent(true);
                    e.getMoveFlagData().setPullYMotion((e.getCheckable().getMeta().getSyncedValues().lastYmovement - 0.08) * 0.98);
                }
                return;
            }
        }
        final boolean onGround = e.isOnGround();
        if (e.getYDirectedMotion() == e.getCheckable().getMeta().getSyncedValues().lastYmovement && LocationHelper.hasLiquitNearby(e.getTo())) {
            boolean isBeneathBlockInWater = false;
            if (BlockData.doesAffectMovement(e.getCache().getCloned(e.getTo()).add(0.0, 1.6, 0.0).getBlock())) {
                isBeneathBlockInWater = true;
            }
            if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeCollidedWithTopBlock < 500 + e.getCheckable().getPing()) {
                isBeneathBlockInWater = true;
            }
            if (!onGround && e.isMoving() && !isBeneathBlockInWater && !LocationHelper.collidesStepable(p.getWorld(), (Entity)p, e.getFrom()) && IIUA.getCurrentTimeMillis() - e.getCheckable().setupTime > 3000L) {
                if (LocationHelper.collides(p.getWorld(), (Entity)p, e.getFrom(), Material.CACTUS) || LocationHelper.isInsideUnpassable(p.getWorld(), (Entity)p, e.getTo())) {
                    return;
                }
                if (this.plugin.getRetributionManager().markPlayer(p, 40, "Flight", CheatCategory.MOVING, this.getFlagMessage("*29/4", e.getYDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastYmovement, (e.getYDirectedMotion() > 0.0) ? 0.0 : ((e.getCheckable().getMeta().getSyncedValues().lastYmovement - 0.08) * 0.98)))) {
                    e.getMoveFlagData().setPullSilent(true);
                    e.getMoveFlagData().setPullYMotion((e.getCheckable().getMeta().getSyncedValues().lastYmovement - 0.08) * 0.98);
                }
                e.getMoveFlagData().setShouldCheck(false);
                return;
            }
        }
        if (e.getYDirectedMotion() >= e.getCheckable().getMeta().getSyncedValues().lastYmovement && (e.getYDirectedMotion() > 0.0 || (e.getCheckable().getMeta().getSyncedValues().lastYmovement < 0.0 && e.getYDirectedMotion() == 0.0)) && !onGround) {
            if (LocationHelper.collidesLiquid((Entity)p, e.getTo()) || LocationHelper.hasLiquitNearby(e.getTo())) {
                return;
            }
            if (LocationHelper.collidesXZAxisFar(p.getWorld(), (Entity)p, e.getFrom(), Material.WEB)) {
                return;
            }
            if (LocationHelper.collides(p.getWorld(), (Entity)p, e.getFrom(), Material.CACTUS)) {
                return;
            }
            if (!doesCollide && LocationHelper.isOnGroundLessAccurate(e.getFrom(), e.getXZDirectedMotion() + 0.08)) {
                return;
            }
            if ((e.getYDirectedMotion() > 0.44 || MathHelper.diff(e.getYDirectedMotion(), 0.4203) <= 1.0E-4) && e.getCheckable().getMeta().getSyncedValues().lastYmovement < -0.5 && LocationHelper.isOnGroundAccurate(e.getTo(), (Entity)p, 0.4)) {
                return;
            }
            int i = 0;
            for (Location f = e.getCache().getCloned(e.getFrom()); BlockData.isPassable(f.getBlock()) && i < 50; f = f.subtract(0.0, 0.05, 0.0), ++i) {}
            Location f;
            final double expected = (e.getCheckable().getMeta().getSyncedValues().ticksSinceLastMovementCorrection < 1L) ? ((e.getCheckable().getMeta().getSyncedValues().lastYmovement - 0.08) * 0.98) : ((f.getY() - e.getFrom().getY() < -0.19) ? ((e.getCheckable().getMeta().getSyncedValues().lastYmovement - 0.08) * 0.98) : (f.getY() - e.getFrom().getY()));
            final boolean blockPlaceSuspiciousFlag = LocationHelper.collidesAny(p.getWorld(), (Entity)p, e.getFrom(), 0.0, (p.getFallDistance() < 0.1 && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeBlockPlaced < 400L) ? 0.01 : -0.001);
            if (LocationHelper.collidesXZAxis(p.getWorld(), (Entity)p, e.getTo(), Material.CARPET) && e.getYDirectedMotion() == 0.0) {
                return;
            }
            if (BlockData.isStepable(e.getFrom().getBlock(), e.getTo())) {
                return;
            }
            if (MathHelper.diff(0.395919621, e.getYDirectedMotion()) < 1.0E-6 && LocationHelper.collidesStepable(p.getWorld(), (Entity)p, e.getFrom())) {
                return;
            }
            if (e.getYDirectedMotion() == 0.0 && LocationHelper.collidesStepable(p.getWorld(), (Entity)p, e.getTo()) && e.getCheckable().getMeta().getSyncedValues().lastYmovement < -0.34) {
                return;
            }
            if (!e.isOnGround() && LocationHelper.collidesStepable(p.getWorld(), (Entity)p, e.getTo()) && e.getCheckable().getMeta().getSyncedValues().lastYmovement < -0.34 && LocationHelper.isOnGroundR(e.getTo())) {
                return;
            }
            if (LocationHelper.collides(p.getWorld(), (Entity)p, Material.SNOW) && MathHelper.diff(e.getYDirectedMotion(), 0.2336320060424839) < 1.0E-9) {
                return;
            }
            if (LocationHelper.collidesAnyStepable(e.getTo().getWorld(), (Entity)p, e.getTo(), 0.0) && (!BlockData.isPassable(e.getFrom().getBlock().getRelative(BlockFace.DOWN)) || BlockData.isStepable(e.getFrom().getBlock().getRelative(BlockFace.DOWN), e.getTo())) && e.getYDirectedMotion() < 0.2686) {
                return;
            }
            if ((e.getCheckable().getMeta().getSyncedValues().lastYmovement > -0.01 && e.getCheckable().getMeta().getSyncedValues().lastYmovement < 0.0 && BlockData.isClimbable(LocationHelper.getRelative(LocationHelper.getBlockAt(e.getTo()), BlockFace.DOWN))) || (PlayerUtils.isUsingElytra(p) && e.getYDirectedMotion() <= 0.0)) {
                return;
            }
            if (e.getCheckable().getMeta().getSyncedValues().lastYmovement < -0.4 && e.getTo().getY() % 0.5 == 0.0 && LocationHelper.collidesAnyXZAxis(p.getWorld(), (Entity)p, e.getFrom())) {
                return;
            }
            if (this.plugin.getRetributionManager().markPlayer(p, blockPlaceSuspiciousFlag ? 30 : ((e.getCheckable().getMeta().getSyncedValues().ticksSinceLastMovementCorrection > 1L) ? 40 : 5), "Flight", CheatCategory.MOVING, this.getFlagMessage("*29", (e.getCheckable().getMeta().getSyncedValues().ticksSinceLastMovementCorrection > 1L) ? e.getYDirectedMotion() : 0.0, e.getCheckable().getMeta().getSyncedValues().lastYmovement, expected))) {
                e.getMoveFlagData().setPullSilent(true);
                e.getMoveFlagData().setPullYMotion(expected);
            }
            e.getMoveFlagData().setShouldCheck(false);
        }
    }
    
    @Override
    public void onCheckableVelocity(final CheckableVelocityEvent e) {
        e.getCheckable().getMeta().getSyncedValues().hadYVelocity29F = false;
    }
    
    private void sensitiveDamageCheck(final CheckableMoveEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (!e.getMoveFlagData().shouldCheck()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        if (PlayerUtils.getAllowFlight(p, true, false, true, true, true)) {
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded < 1000L) {
            final boolean hadYVelocity = e.getCheckable().getMeta().getSyncedValues().hadYVelocityStartMotion;
            if ((!e.isOnGround() || e.getYDirectedMotion() > 0.0) && hadYVelocity && e.getYDirectedMotion() < e.getCheckable().getMeta().getSyncedValues().lastYmovement) {
                final double solve_error = e.getYDirectedMotion() / 0.98 + 0.08 - e.getCheckable().getMeta().getSyncedValues().lastYmovement;
                final int length = e.getCheckable().getMeta().getSyncedValues().velocityCheckLength;
                boolean flag = false;
                if (Math.abs(solve_error) > 0.1) {
                    flag = true;
                }
                if (Math.abs(solve_error) > 1.0E-7) {
                    e.getCheckable().getMeta().getVioValues().suspiciousPostHitY = Math.min(5, e.getCheckable().getMeta().getVioValues().suspiciousPostHitY + 2);
                    if (e.getCheckable().getMeta().getVioValues().suspiciousPostHitY > 3) {
                        flag = true;
                    }
                }
                else {
                    final Checkable.CheckableMeta.ViolationValues vioValues = e.getCheckable().getMeta().getVioValues();
                    --vioValues.suspiciousPostHitY;
                }
                if (e.getCheckable().getMeta().getSyncedValues().lastYmovement > 0.0 && e.getYDirectedMotion() < 0.0 && flag && e.getCheckable().getMeta().getSyncedValues().velocityCheckLength > 2) {
                    flag = (Math.abs(solve_error) > 0.2);
                }
                if (flag && LocationHelper.collidesAny(p.getWorld(), (Entity)p, e.getTo(), e.getYDirectedMotion() + 0.1, 0.05)) {
                    flag = false;
                }
                if (flag) {
                    if (this.plugin.getRetributionManager().markPlayer(p, 20, "Flight", CheatCategory.MOVING, this.getFlagMessage("*33/2", e.getYDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastYmovement, (e.getCheckable().getMeta().getSyncedValues().lastYmovement - 0.08) * 0.98))) {
                        e.getMoveFlagData().setPullSilent(true);
                        e.getMoveFlagData().setPullYMotion((e.getCheckable().getMeta().getSyncedValues().velocityCheckLength > 0) ? ((e.getCheckable().getMeta().getSyncedValues().lastYmovement - 0.08) * 0.98) : 0.0);
                        return;
                    }
                    e.getMoveFlagData().setShouldCheck(false);
                }
            }
        }
        else {
            e.getCheckable().getMeta().getVioValues().suspiciousPostHitY = 0;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastDamageTakenTimestamp < 1000L && e.getYDirectedMotion() < e.getCheckable().getMeta().getSyncedValues().lastYmovement) {
            final double lastYMovement = e.getCheckable().getMeta().getSyncedValues().lastYmovement;
            final double movementIRR = lastYMovement - 0.09 - e.getYDirectedMotion();
            if (e.getCheckable().getMeta().getSyncedValues().lastVelocity == null) {
                return;
            }
            if (!LocationHelper.collidesAnyFar(p.getWorld(), (Entity)p, e.getTo()) && !LocationHelper.collidesAnyFar(p.getWorld(), (Entity)p, e.getFrom())) {
                if (e.getCheckable().getMeta().getSyncedValues().lastVelocity.getY() < -0.08 && e.getYDirectedMotion() > 0.0 && lastYMovement < 0.0 && this.plugin.getRetributionManager().markPlayer(p, 5, "Flight", CheatCategory.MOVING, this.getFlagMessage("*33", e.getYDirectedMotion(), lastYMovement, lastYMovement - 0.08))) {
                    e.getMoveFlagData().setPullYMotion((lastYMovement - 0.08) * 0.98);
                    e.getMoveFlagData().setShouldCheck(false);
                    e.getMoveFlagData().setPullSilent(true);
                    return;
                }
                if (e.getYDirectedMotion() == 0.0) {
                    return;
                }
                if (MathHelper.diff(e.getYDirectedMotion(), -0.653) < 0.002) {
                    return;
                }
                if (MathHelper.diff(lastYMovement, -0.178) < 0.003 && MathHelper.diff(e.getYDirectedMotion(), -0.203) < 0.003) {
                    return;
                }
            }
        }
        else {
            e.getCheckable().getMeta().getVioValues().suspiciousPostHitY = 0;
        }
    }
    
    private void checkStepMovement(final CheckableMoveEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        if (PlayerUtils.getAllowFlight(p, false, false, false, true, false) && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeClimbedALadder > 200L) {
            return;
        }
        if (IIUA.getAllowFlight(p)) {
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnASlime < 6000L) {
            return;
        }
        final double yDiff = e.getYDirectedMotion();
        final double lastYDiff = e.getCheckable().getMeta().getSyncedValues().lastYmovement;
        double maxYDiff = 0.421;
        double jumpBoost = 0.0;
        for (final PotionEffect effect : p.getActivePotionEffects()) {
            if (effect.getType().equals((Object)PotionEffectType.JUMP)) {
                final double v = 0.105 * (effect.getAmplifier() + 1);
                jumpBoost += v;
            }
        }
        if (MathHelper.diff(e.getYDirectedMotion(), 0.44537446) < 1.0E-6) {
            return;
        }
        if (PlayerUtils.hasLevitation(p)) {
            maxYDiff = 0.1113;
        }
        if (BlockData.isLiquid(e.getFrom().getBlock()) && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded > 2000L) {
            maxYDiff = ((IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded < e.getCheckable().getPing() + 100) ? 0.5 : (BlockData.isLiquid(LocationHelper.getBlockAt(e.getTo())) ? 0.36 : 0.4));
        }
        if (e.getFrom().getBlock().getType().equals((Object)Material.DAYLIGHT_DETECTOR) || e.getFrom().getBlock().getType().equals((Object)Material.DAYLIGHT_DETECTOR_INVERTED)) {
            maxYDiff = 0.522;
        }
        if (e.getCheckable().getMeta().getSyncedValues().lastYmovement < -0.4) {
            maxYDiff = 0.6;
        }
        if (LocationHelper.hasStepableNearbyITB(p, e.getFrom()) || LocationHelper.hasStepableNearbyITB(p, e.getTo())) {
            maxYDiff = 0.5;
        }
        if (MathHelper.diff(e.getYDirectedMotion(), 0.498) > 0.001 && (LocationHelper.hasStepableNearbyITB(p, e.getFrom()) || LocationHelper.hasStepableNearbyITB(p, e.getTo())) && lastYDiff < 0.6) {
            maxYDiff = 0.6;
        }
        if (LocationHelper.collidesFar(p.getWorld(), (Entity)p, e.getFrom(), Material.PISTON_EXTENSION, Material.PISTON_MOVING_PIECE)) {
            maxYDiff = 1.2;
        }
        if (p.isInsideVehicle()) {
            maxYDiff = (p.getVehicle().getType().equals((Object)EntityType.HORSE) ? 1.8 : 0.76);
        }
        maxYDiff += jumpBoost;
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded < 2000L && e.getCheckable().getMeta().getSyncedValues().lastVelocityFI != null) {
            maxYDiff = Math.max(maxYDiff, e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.getY() + 0.01);
        }
        if (yDiff > maxYDiff) {
            final double expectation = (e.getCheckable().getMeta().getSyncedValues().lastYmovement - 0.08) * 0.98;
            if (this.plugin.getRetributionManager().markPlayer(p, Math.min((int)(40.0 * (yDiff / maxYDiff)), 100), "Flight", CheatCategory.MOVING, this.getFlagMessage("*99", e.getYDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastYmovement, expectation))) {
                if (e.getCheckable().getMeta().getSyncedValues().lastYmovement < -0.4 && !LocationHelper.collidesAnyFar(p.getWorld(), (Entity)p, e.getFrom())) {
                    e.getMoveFlagData().setPullSilent(true);
                    e.getMoveFlagData().setPullYMotion(expectation);
                }
                else {
                    e.setCancelled(true);
                }
            }
            e.getMoveFlagData().setShouldCheck(false);
        }
        if (e.getYDirectedMotion() <= 0.02 && !p.isInsideVehicle() && !BlockData.isStepable(e.getTo().getBlock(), e.getTo()) && !BlockData.isStepable(e.getFrom().getBlock(), e.getTo()) && !BlockData.isStepable(e.getCheckable().getMeta().getLocationValues().penaltyLocation.getBlock(), e.getTo()) && e.getYDirectedMotion() >= 0.0 && e.getCheckable().getMeta().getSyncedValues().lastYmovement > 0.0 && e.getCheckable().getMeta().getSyncedValues().lastYmovement < 0.26 && e.getCheckable().getMeta().getSyncedValues().lastYmovement > 0.216 && e.getCheckable().getMeta().getSyncedValues().lastYmovement < 0.249 && !p.hasPotionEffect(PotionEffectType.JUMP)) {
            final double expectation = (e.getCheckable().getMeta().getSyncedValues().lastYmovement - 0.08) * 0.98;
            if (this.plugin.getRetributionManager().markPlayer(p, 10, "Flight", CheatCategory.MOVING, this.getFlagMessage("*99/2", e.getYDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastYmovement, expectation))) {
                if (!LocationHelper.isInsideUnpassable(p.getWorld(), (Entity)p, e.getCheckable().getMeta().getLocationValues().penaltyLocation)) {
                    final Checkable.CheckableMeta.ViolationValues vioValues = e.getCheckable().getMeta().getVioValues();
                    vioValues.suspiciousLowPackets -= 2;
                }
                else {
                    e.getMoveFlagData().setPullSilent(true);
                    e.getMoveFlagData().setPullYMotion(expectation);
                }
            }
            e.getMoveFlagData().setShouldCheck(false);
        }
    }
    
    private String getFlagMessage(final String checkname, final double yMotion, final double lastYMotion, final double expectedMotion) {
        final String yMotionS = MathHelper.roundFromDouble(yMotion, 5);
        final String lastYMotionS = MathHelper.roundFromDouble(lastYMotion, 5);
        final String expectedMotionS = MathHelper.roundFromDouble(expectedMotion, 5);
        return "sent unexpected position (" + lastYMotionS + " -> " + yMotionS + ", but expected " + expectedMotionS + ") " + "";
    }
    
    @Override
    public void onCheckableDamageEntity(final CheckableDamageEntityEvent e) {
        if (this.plugin.isLinkedToIntave(e.getEntity().getUniqueId())) {
            e.getCheckable().getMeta().getLocationValues().lastHitted = e.getEntity().getLocation();
        }
    }
}
