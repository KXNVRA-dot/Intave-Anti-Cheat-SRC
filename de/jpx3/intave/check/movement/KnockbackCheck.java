// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.movement;

import de.jpx3.intave.check.connection.MoveCheck;
import de.jpx3.intave.util.data.BlockData;
import de.jpx3.intave.util.calc.MathHelper;
import de.jpx3.intave.util.calc.YawUtil;
import de.jpx3.intave.util.calc.PlayerUtils;
import org.bukkit.entity.Player;
import de.jpx3.intave.util.event.CheckableVelocityEvent;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.bukkit.entity.Entity;
import de.jpx3.intave.util.calc.LocationHelper;
import de.jpx3.intave.util.event.CheckableMoveEvent;
import de.jpx3.intave.util.objectable.Checkable;
import org.bukkit.plugin.Plugin;
import de.jpx3.intave.antipiracy.IIUA;
import de.jpx3.intave.util.enums.CheatCategory;
import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.util.objectable.IntaveCheck;

public final class KnockbackCheck extends IntaveCheck
{
    private final IntavePlugin plugin;
    private final boolean debug = false;
    private static final int vlHandle = 6;
    private static final int velocityLatency = 10;
    private static final double sb_maxmulipl = 3.0;
    private static final double sb_mulipl_add = 0.0025;
    public static boolean verify_packet;
    
    public KnockbackCheck(final IntavePlugin plugin) {
        super("Knockback", CheatCategory.MOVING);
        this.plugin = plugin;
        KnockbackCheck.verify_packet = plugin.getConfig().getBoolean(this.getConfigPath() + ".verify_packet");
        final Checkable checkable;
        this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)this.plugin, () -> plugin.getServer().getOnlinePlayers().stream().filter(p -> plugin.isLinkedToIntave(IIUA.getUUIDFrom(p))).forEachOrdered(p -> {
            checkable = plugin.catchCheckable(IIUA.getUUIDFrom(p));
            if (checkable.getMeta().getSyncedValues().shouldPerformVelocityCheck) {
                this.giveKnockback(p, checkable.getMeta().getSyncedValues().requestedCheckVelocity.clone().setY(0.1));
                checkable.getMeta().getSyncedValues().shouldPerformVelocityCheck = false;
            }
        }), 100L, 100L);
    }
    
    @Override
    public void onCheckableMove(final CheckableMoveEvent e) {
        if (e.getCheckable().getMeta().getSyncedValues().ticksMotionKellCorr > 0) {
            final Vector lastVelocity = e.getCheckable().getMeta().getSyncedValues().lastVelocity;
            lastVelocity.setY(Math.min(0.45, Math.max(-0.5, (lastVelocity.getY() - 0.08) * 0.98)));
            final double length = lastVelocity.clone().setY(0).length();
            if (length < 0.5) {
                lastVelocity.setX(lastVelocity.getX() * 1.32);
                lastVelocity.setZ(lastVelocity.getZ() * 1.32);
            }
            else if (length > 1.2) {
                lastVelocity.setX(this.limit(lastVelocity.getX(), 0.8));
                lastVelocity.setZ(this.limit(lastVelocity.getZ(), 0.8));
            }
            final Location location = e.getCache().getCloned(e.getCheckable().getVerifiedLocation()).add(lastVelocity);
            if (!LocationHelper.isInsideUnpassable(e.getBukkitPlayer().getWorld(), (Entity)e.getBukkitPlayer(), location) && !LocationHelper.isInsideUnpassable(e.getBukkitPlayer().getWorld(), (Entity)e.getBukkitPlayer(), location.clone().subtract(0.0, 0.2, 0.0))) {
                this.setBack(e.getBukkitPlayer(), lastVelocity, e.getCheckable().getVerifiedLocation(), 0.0, 1.5);
            }
            final Checkable.CheckableMeta.SyncedValues syncedValues = e.getCheckable().getMeta().getSyncedValues();
            --syncedValues.ticksMotionKellCorr;
            return;
        }
        this.checkStrafe(e);
        this.checkVelocity2(e);
    }
    
    @Override
    public void onCheckableVelocity(final CheckableVelocityEvent e) {
        this.advancedVelocity(e);
    }
    
    final void onInstantDetect(final Player p) {
        this.plugin.getRetributionManager().markPlayer(p, 32, "Knockback", CheatCategory.MOVING, "tried to ignore velocity packet (missing override)");
    }
    
    private void checkStrafe(final CheckableMoveEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        if (PlayerUtils.getAllowFlight(p, true, false, true) || LocationHelper.collidesStepable(p.getWorld(), (Entity)p, e.getTo())) {
            e.getCheckable().getMeta().getSyncedValues().lastDirectionMidAir = 0.0f;
            return;
        }
        if (e.getDirection() == -180.0f) {
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().setupTime < 2000L) {
            return;
        }
        if (!e.getCheckable().getMeta().getSyncedValues().hadYVelocityStartMotion && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded < 2000L) {
            e.getCheckable().getMeta().getSyncedValues().lastDirectionMidAir = YawUtil.getYawFrom(e.getFrom(), e.getTo());
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeTeleported < 1000L && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeFlagged > 1000L) {
            e.getCheckable().getMeta().getSyncedValues().lastDirectionMidAir = YawUtil.getYawFrom(e.getFrom(), e.getTo());
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnASlime < 5000L) {
            return;
        }
        if (e.getTo().getX() == e.getFrom().getX() && e.getTo().getZ() == e.getFrom().getZ()) {
            return;
        }
        if (!IIUA.isPretendingToBeOnGround(p) && !p.isInsideVehicle()) {
            final float dir = YawUtil.getYawFrom(e.getFrom(), e.getTo());
            if (!LocationHelper.couldCollide(e.getTo()) && e.getCheckable().getMeta().getSyncedValues().lastDirectionMidAir != 0.0f && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeBlockPlaced > 15000L) {
                final Vector motionVector = e.getCache().getCloned(e.getMotionVector()).setY(0);
                final Vector lastMotionVector = e.getCheckable().getMeta().getSyncedValues().lastVector.clone().multiply(0.98).setY(0);
                final double diffToCurrentVector = motionVector.angle(lastMotionVector);
                final float diff = MathHelper.diff(YawUtil.yawDiff(dir, e.getCheckable().getMeta().getSyncedValues().lastDirectionMidAir), 0.0f);
                final boolean bad = diffToCurrentVector > 0.4 && e.getXZDirectedMotion() > 0.02 && diff > 39.0f && e.getXZDirectedMotion() > 0.03 && diff < ((e.getXZDirectedMotion() > 0.08) ? 180 : 150);
                final double lastClickOnPlayer = (double)(IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeLeftClickedEntity);
                final boolean xzDoesntMatter = e.getXZDirectedMotion() < 0.2 && lastClickOnPlayer < 50.0 && e.getXZDirectedMotion() - 0.1 < e.getCache().getCloned(e.getCheckable().getMeta().getSyncedValues().lastVelocityFI).setY(0).length() && e.getCache().getCloned(e.getCheckable().getMeta().getSyncedValues().lastVelocityFI).setY(0).length() < 1.0;
                if (xzDoesntMatter) {
                    return;
                }
                if (e.getXZDirectedMotion() < 0.1 && e.getCheckable().getMeta().getSyncedValues().lastXZMovement < 0.1) {
                    return;
                }
                if (e.getCheckable().getMeta().getSyncedValues().lastXZMovement < 0.5 && (Math.abs(e.getCheckable().getMeta().getSyncedValues().lastXMovement) < 0.06 || Math.abs(e.getCheckable().getMeta().getSyncedValues().lastZMovement) < 0.06)) {
                    return;
                }
                if (Math.abs(e.getXDirectedMotion()) < 0.06 || MathHelper.amount(e.getZDirectedMotion()) < 0.06) {
                    return;
                }
                if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded < 2000L && e.getCheckable().getMeta().getSyncedValues().velocityCheckLength > 4) {
                    return;
                }
                if (bad && this.plugin.getRetributionManager().markPlayer(p, 2, "Knockback", CheatCategory.MOVING, "applied suspicious change of direction")) {
                    this.setBack(p, (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded < 2000L) ? e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.clone().setY(e.getCheckable().getMeta().getSyncedValues().lastYmovement) : new Vector(e.getCheckable().getMeta().getSyncedValues().lastXMovement, e.getCheckable().getMeta().getSyncedValues().lastYmovement, e.getCheckable().getMeta().getSyncedValues().lastZMovement).multiply(0.91), (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded < 2000L) ? e.getCheckable().getMeta().getLocationValues().lastVelocity : e.getFrom());
                }
            }
            e.getCheckable().getMeta().getSyncedValues().lastDirectionMidAir = dir;
        }
        else {
            e.getCheckable().getMeta().getSyncedValues().lastDirectionMidAir = 0.0f;
        }
    }
    
    private void advancedVelocity(final CheckableVelocityEvent e) {
        final Player p = e.getBukkitPlayer();
        final Checkable.CheckableMeta.SyncedValues syncedValues = e.getCheckable().getMeta().getSyncedValues();
        final boolean willGetArtificialVelocity = e.getCheckable().getMeta().getSyncedValues().willGetArtificialVelocity;
        syncedValues.hadArtificialVelocity = willGetArtificialVelocity;
        final boolean isArtificialVelocity = willGetArtificialVelocity;
        e.getCheckable().getMeta().getSyncedValues().willGetArtificialVelocity = false;
        if (!e.getCheckable().getMeta().getSyncedValues().hadGoodVelocity && e.getVelocity().getY() > 0.0 && !IIUA.getAllowFlight(p) && hasHeapSpace(p, e.getCheckable().getVerifiedLocation())) {
            final Checkable.CheckableMeta.ViolationValues vioValues = e.getCheckable().getMeta().getVioValues();
            ++vioValues.velocityVL;
            if (e.getCheckable().getMeta().getVioValues().velocityVL > 3) {
                final String current = MathHelper.roundFromDouble(0.0, 5) + " " + MathHelper.roundFromDouble(0.0, 5) + " " + MathHelper.roundFromDouble(0.0, 5);
                final String last = MathHelper.shortVector(e.getVelocity(), " ");
                int vl = isArtificialVelocity ? 6 : 4;
                final long lastPositionPacket = IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimePositionPacketSent;
                if (lastPositionPacket > 2000L) {
                    vl = 0;
                }
                final double ticksBehind = lastPositionPacket / 50.0;
                if (this.plugin.getRetributionManager().markPlayer(p, vl, "Knockback", CheatCategory.MOVING, "did not respond on given knockback (" + MathHelper.roundFromDouble(ticksBehind, 3) + " ticks behind) (expected " + last + ")")) {
                    this.setBack(p, e.getVelocity().clone().multiply((e.getVelocity().clone().setY(0).length() > 0.3) ? 0.96 : 1.0).setY(e.getVelocity().getY()), e.getCheckable().getVerifiedLocation());
                    this.delayedVelocity(p, e.getVelocity().clone().setY(e.getVelocity().getY()), 16);
                    if (!this.plugin.getThresholdsManager().shouldFlag("Knockback", 8) || this.plugin.getViolationManager().getViolationLevel(p, "Knockback") >= 8) {}
                }
            }
        }
        else if (e.getCheckable().getMeta().getVioValues().velocityVL > 0) {
            final Checkable.CheckableMeta.ViolationValues vioValues2 = e.getCheckable().getMeta().getVioValues();
            --vioValues2.velocityVL;
        }
        if (e.getCheckable().getMeta().getVioValues().velocityVL2 > 0) {
            final Checkable.CheckableMeta.ViolationValues vioValues3 = e.getCheckable().getMeta().getVioValues();
            --vioValues3.velocityVL2;
        }
        e.getCheckable().getMeta().getSyncedValues().hadVelocityOverflow = false;
        e.getCheckable().getMeta().getSyncedValues().hadYVelocityStartMotion = false;
        e.getCheckable().getMeta().getSyncedValues().hadGoodVelocity = false;
        this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().velocityCheckLength = 0;
    }
    
    void reverse(final CheckableMoveEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        if (!e.getCheckable().getMeta().getSyncedValues().hadYVelocityStartMotion || IIUA.getAllowFlight(p)) {
            return;
        }
        if (e.getCheckable().getMeta().getSyncedValues().lastVelocityFI != null && (e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.getY() > 0.6 || e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.getY() < 0.0)) {
            return;
        }
        final String current = MathHelper.roundFromDouble(p.getVelocity().getX(), 5) + " " + MathHelper.roundFromDouble(p.getVelocity().getY(), 5) + " " + MathHelper.roundFromDouble(p.getVelocity().getZ(), 5);
        final String last = MathHelper.shortVector(e.getCheckable().getMeta().getSyncedValues().lastVelocityFI, " ");
        final boolean isArtificialKnockback = e.getCheckable().getMeta().getSyncedValues().hadArtificialVelocity;
        if (this.plugin.getThresholdsManager().shouldFlag("Knockback", 8)) {
            e.getCheckable().setSilentVelocity(new Vector(0.0, (e.getYDirectedMotion() - 0.08) * 0.98, 0.0));
        }
        if (this.plugin.getRetributionManager().markPlayer(p, isArtificialKnockback ? 8 : 2, "Knockback", CheatCategory.MOVING, "manipulated his knockback. (" + current + " expected " + last + ")")) {
            this.giveKnockback(p, e.getCheckable().getMeta().getSyncedValues().lastVelocity.clone().setY((e.getYDirectedMotion() - 0.08) * 0.98));
        }
    }
    
    private void checkVelocity2(final CheckableMoveEvent e) {
        if (e.isCancelled() || !this.isActivated() || e.getBukkitPlayer().getAllowFlight()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        final long lVelocityAdded = IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded;
        final boolean isArtificialKnockback = false;
        if (lVelocityAdded > 3000L) {
            return;
        }
        if (e.getCheckable().getMeta().getSyncedValues().lastVelocity != null && e.getCheckable().getMeta().getSyncedValues().ticksVelocityMidAir > 1L && e.getCheckable().getMeta().getSyncedValues().hadYVelocityStartMotion && e.getCheckable().getMeta().getTimedValues().lastTimeOnGroundACC - 100L < e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded) {
            final double xz_increasement = e.getXZDirectedMotion() / e.getCheckable().getMeta().getSyncedValues().lastXZMovement;
            final double xz_diff_to_vel = e.getXZDirectedMotion() / e.getCache().getCloned(e.getCheckable().getMeta().getSyncedValues().lastVelocityFI).setY(0).length();
            if (e.getCheckable().getMeta().getSyncedValues().lastVelocityFI != null && (e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.getY() > 0.6 || e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.getY() < 0.0)) {
                return;
            }
            if (e.getCheckable().getMeta().getSyncedValues().hadYVelocityStartMotion) {
                if (e.getXZDirectedMotion() < 0.2 && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded < 3000 + e.getCheckable().getPing() && hasHeapSpace(p, e.getTo()) && hasHeapSpace(p, e.getFrom())) {
                    e.getCheckable().getMeta().getSyncedValues().hadVelocityOverflow = true;
                }
                if (e.getCheckable().getMeta().getSyncedValues().hadVelocityOverflow && IIUA.getCurrentTimeMillis() - e.getCheckable().setupTime > 10000L) {
                    boolean bad3 = false;
                    if (xz_increasement > 1.5 && e.getXZDirectedMotion() > 0.18 && e.getYDirectedMotion() > -0.5) {
                        bad3 = true;
                    }
                    else if (e.getXZDirectedMotion() > 0.4 && e.getYDirectedMotion() > -0.5) {
                        bad3 = true;
                    }
                    if (e.getXZDirectedMotion() > 0.09 && xz_increasement > 1.3) {
                        bad3 = true;
                    }
                    if (e.getXZDirectedMotion() > 0.15 && xz_increasement > 1.1) {
                        bad3 = true;
                    }
                    if (bad3) {
                        final String current = MathHelper.roundFromDouble(e.getXDirectedMotion(), 5) + " " + MathHelper.roundFromDouble(e.getYDirectedMotion(), 5) + " " + MathHelper.roundFromDouble(e.getZDirectedMotion(), 5);
                        final Vector expectation = e.getCheckable().getMeta().getSyncedValues().lastNonArtifVelocityFI.clone();
                        expectation.setY(e.getYDirectedMotion());
                        final String last = MathHelper.shortVector(expectation, " ");
                        if (this.plugin.getRetributionManager().markPlayer(p, 2, "Knockback", CheatCategory.MOVING, "manipulated his knockback. (" + current + " expected " + last + ")")) {
                            this.setBack(p, expectation, e.getCheckable().getVerifiedLocation());
                            this.giveKnockback(p, e.getCheckable().getMeta().getSyncedValues().lastNonArtifVelocityFI.clone().setY((e.getYDirectedMotion() - 0.08) * 0.98));
                        }
                    }
                }
                final boolean bad4 = ((e.getXZDirectedMotion() > 0.19 && xz_increasement > 1.3) || (e.getXZDirectedMotion() > 0.29 && xz_increasement > 0.92)) && xz_diff_to_vel >= e.getCheckable().getMeta().getSyncedValues().lastVFIXZDiff && e.getCache().getCloned(e.getCheckable().getMeta().getSyncedValues().lastVelocityFI).setY(0).length() > 0.21;
                if (bad4) {
                    if (e.getCheckable().getMeta().getVioValues().velocityVL3 < 3) {
                        final Checkable.CheckableMeta.ViolationValues vioValues = e.getCheckable().getMeta().getVioValues();
                        ++vioValues.velocityVL3;
                    }
                    if (e.getCheckable().getMeta().getVioValues().velocityVL3 > 1) {
                        final String current = MathHelper.roundFromDouble(e.getXDirectedMotion(), 5) + " " + MathHelper.roundFromDouble(e.getYDirectedMotion(), 5) + " " + MathHelper.roundFromDouble(e.getZDirectedMotion(), 5);
                        final Vector expectation = e.getCache().getCloned(e.getCheckable().getMeta().getSyncedValues().lastVelocityFI);
                        expectation.setY(e.getYDirectedMotion());
                        final String last = MathHelper.shortVector(expectation, " ");
                        if (this.plugin.getRetributionManager().markPlayer(p, 1, "Knockback", CheatCategory.MOVING, "manipulated his knockback. (" + current + " expected " + last + ")")) {
                            e.setCancelled(true);
                            this.giveKnockback(p, e.getCheckable().getMeta().getSyncedValues().lastVelocity.clone().setY((e.getYDirectedMotion() - 0.08) * 0.98));
                        }
                    }
                }
                else if (e.getCheckable().getMeta().getVioValues().velocityVL3 > 0) {
                    final Checkable.CheckableMeta.ViolationValues vioValues2 = e.getCheckable().getMeta().getVioValues();
                    --vioValues2.velocityVL3;
                }
            }
            e.getCheckable().getMeta().getSyncedValues().lastVFIXZDiff = xz_diff_to_vel;
        }
        if (lVelocityAdded < 769L) {
            final double xz_increasement = e.getXZDirectedMotion() / e.getCheckable().getMeta().getSyncedValues().lastXZMovement;
            if (e.getYDirectedMotion() < 0.0 && e.getCheckable().getMeta().getSyncedValues().lastVelocity.getY() > 0.0 && xz_increasement > 1.29 && e.getXZDirectedMotion() > 0.25) {
                final String current2 = MathHelper.roundFromDouble(e.getXDirectedMotion(), 5) + " " + MathHelper.roundFromDouble(e.getYDirectedMotion(), 5) + " " + MathHelper.roundFromDouble(e.getZDirectedMotion(), 5);
                final Vector expectation2 = e.getCache().getCloned(e.getCheckable().getMeta().getSyncedValues().lastVelocityFI);
                expectation2.setY(e.getYDirectedMotion());
                final String last2 = MathHelper.shortVector(expectation2, " ");
                if (this.plugin.getRetributionManager().markPlayer(p, 3, "Knockback", CheatCategory.MOVING, "manipulated his knockback. (" + current2 + " expected " + last2 + ")")) {
                    this.giveKnockback(p, e.getCheckable().getMeta().getSyncedValues().lastVelocity.clone().setY((e.getYDirectedMotion() - 0.08) * 0.98));
                }
            }
            if (e.getCheckable().getMeta().getSyncedValues().lastVelocity.getY() > 0.0 && !IIUA.getAllowFlight(p) && xz_increasement < 0.9) {
                final boolean bad5 = (e.getCheckable().getMeta().getSyncedValues().lastXZMovement > 0.4 && xz_increasement < 0.21) || (e.getCheckable().getMeta().getSyncedValues().lastXZMovement > 0.3 && xz_increasement < 0.1) || (e.getCheckable().getMeta().getSyncedValues().lastXZMovement > 0.2 && xz_increasement < 0.05);
                if (bad5 && !LocationHelper.couldCollide(p.getLocation()) && hasHeapSpace(p, p.getLocation())) {
                    final int percentage = (int)(xz_increasement * 100.0);
                    final boolean flag = IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getVioValues().lastTimeSuspiciousForVelocityMotionReduce < 12000L;
                    if (flag) {
                        final String current = MathHelper.roundFromDouble(e.getXDirectedMotion(), 5) + " " + MathHelper.roundFromDouble(e.getYDirectedMotion(), 5) + " " + MathHelper.roundFromDouble(e.getZDirectedMotion(), 5);
                        final Vector expectation = e.getCache().getCloned(e.getCheckable().getMeta().getSyncedValues().lastVelocityFI);
                        expectation.setY(e.getYDirectedMotion());
                        final String last = MathHelper.shortVector(expectation, " ");
                        if (this.plugin.getRetributionManager().markPlayer(p, (xz_increasement > 0.2) ? 1 : 2, "Knockback", CheatCategory.MOVING, "manipulated his knockback. (" + current + " expected " + last + ")")) {
                            if (e.getCheckable().getMeta().getSyncedValues().ticksMotionKellCorr < 12) {
                                e.getCheckable().getMeta().getSyncedValues().ticksMotionKellCorr = 6;
                            }
                            this.giveKnockback(p, e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.clone().multiply(3.2125 - xz_increasement).setY((e.getYDirectedMotion() - 0.08) * 0.98));
                        }
                    }
                    e.getCheckable().getMeta().getVioValues().lastTimeSuspiciousForVelocityMotionReduce = IIUA.getCurrentTimeMillis();
                }
            }
            if (e.getYDirectedMotion() > 0.0 && e.getCheckable().getMeta().getSyncedValues().lastVelocity.getY() > 0.0 && !IIUA.getAllowFlight(p) && e.getCheckable().getMeta().getSyncedValues().hadYVelocityStartMotion && hasHeapSpace(p, e.getTo())) {
                final int vl = e.getCheckable().getMeta().getVioValues().velocity_increasementVL;
                final double lastClickOnPlayer = (double)(IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeLeftClickedEntity);
                if ((xz_increasement > 1.05 && e.getXZDirectedMotion() > 0.3) || (xz_increasement < 0.45 && e.getCheckable().getMeta().getSyncedValues().lastXZMovement > 0.2 && lastClickOnPlayer > 40.0)) {
                    final boolean valid_velocity = e.getCache().getCloned(e.getCheckable().getMeta().getSyncedValues().lastVelocityFI).setY(0).length() > 0.1;
                    if (vl > 13 && valid_velocity) {
                        final String current3 = MathHelper.roundFromDouble(e.getXDirectedMotion(), 5) + " " + MathHelper.roundFromDouble(e.getYDirectedMotion(), 5) + " " + MathHelper.roundFromDouble(e.getZDirectedMotion(), 5);
                        final Vector expectation3 = e.getCache().getCloned(e.getCheckable().getMeta().getSyncedValues().lastVelocityFI);
                        expectation3.setY(e.getYDirectedMotion());
                        final String last3 = MathHelper.shortVector(expectation3, " ");
                        if (this.plugin.getRetributionManager().markPlayer(p, 3, "Knockback", CheatCategory.MOVING, "manipulated his knockback. (" + current3 + " expected " + last3 + ")")) {
                            if (e.getCheckable().getMeta().getSyncedValues().ticksMotionKellCorr < 12) {
                                e.getCheckable().getMeta().getSyncedValues().ticksMotionKellCorr = 6;
                            }
                            this.giveKnockback(p, e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.clone().multiply(2.5 - xz_increasement).multiply(0.6).setY((e.getYDirectedMotion() - 0.08) * 0.98));
                        }
                    }
                    final Checkable.CheckableMeta.ViolationValues vioValues3 = e.getCheckable().getMeta().getVioValues();
                    vioValues3.velocity_increasementVL += 6;
                }
                else if (e.getCheckable().getMeta().getVioValues().velocity_increasementVL > 0) {
                    final Checkable.CheckableMeta.ViolationValues vioValues4 = e.getCheckable().getMeta().getVioValues();
                    --vioValues4.velocity_increasementVL;
                }
            }
            if (e.getCheckable().getMeta().getSyncedValues().hadYVelocityStartMotion && !IIUA.getAllowFlight(p)) {
                final Vector lastVelocity = e.getCheckable().getMeta().getSyncedValues().lastVelocity;
                lastVelocity.setY((lastVelocity.getY() - 0.08) * 0.98).setX(lastVelocity.getX() * 0.93125).setZ(lastVelocity.getZ() * 0.93125);
                final Checkable.CheckableMeta.SyncedValues syncedValues = this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues();
                ++syncedValues.velocityCheckLength;
            }
            final Vector velocity = e.getCheckable().getMeta().getSyncedValues().lastVelocity;
            final double velocity_sq_f = e.getMotionVector().distance(e.getCheckable().getMeta().getSyncedValues().lastVelocityFI);
            final double velocity_sq_xz = e.getMotionVector().clone().setY(0).distance(e.getCache().getCloned(e.getCheckable().getMeta().getSyncedValues().lastVelocityFI).setY(0));
            final double velocity_sq_y = MathHelper.diff(e.getYDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.getY());
            final double lastClickOnPlayer2 = (double)(IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeLeftClickedEntity);
            if (velocity_sq_y < 0.09 || IIUA.getAllowFlight(p)) {
                e.getCheckable().getMeta().getSyncedValues().hadYVelocityStartMotion = true;
            }
            final boolean xzDoesntMatter = e.getXZDirectedMotion() < 0.26 && lastClickOnPlayer2 < 50.0 && e.getXZDirectedMotion() - 0.1 < e.getCache().getCloned(e.getCheckable().getMeta().getSyncedValues().lastVelocityFI).setY(0).length() && e.getCache().getCloned(e.getCheckable().getMeta().getSyncedValues().lastVelocityFI).setY(0).length() < 1.0;
            final boolean xzCanBeIgnored = e.getCache().getCloned(e.getCheckable().getMeta().getSyncedValues().lastVelocityFI).setY(0).length() < 0.01;
            boolean hadGoodVelocity = e.getCheckable().getMeta().getSyncedValues().hadGoodVelocity;
            if (((velocity_sq_f <= 0.25 || xzDoesntMatter) && !hadGoodVelocity && velocity_sq_y < 0.1 && (velocity_sq_xz < 0.22 || xzDoesntMatter)) || !hasHeapSpace(p, e.getTo()) || !hasHeapSpace(p, e.getFrom())) {
                e.getCheckable().getMeta().getSyncedValues().hadGoodVelocity = true;
                hadGoodVelocity = true;
            }
            if (xzCanBeIgnored) {
                e.getCheckable().getMeta().getSyncedValues().hadGoodVelocity = true;
                hadGoodVelocity = true;
            }
            if (!hadGoodVelocity && e.getCheckable().getMeta().getSyncedValues().ticksVelocityMidAir > 10L && !IIUA.getAllowFlight(p)) {
                if (e.getCheckable().getMeta().getVioValues().velocityVL2 < 9) {
                    final Checkable.CheckableMeta.ViolationValues vioValues5 = e.getCheckable().getMeta().getVioValues();
                    ++vioValues5.velocityVL2;
                }
                if (e.getCheckable().getMeta().getVioValues().velocityVL2 > 6) {
                    int vl2 = (e.getCheckable().getMeta().getSyncedValues().ticksVelocityMidAir > 12L) ? 0 : 2;
                    if (BlockData.doesAffectMovement(e.getFrom().clone().add(velocity.clone().setY(Math.max(0.0, velocity.getY()))).getBlock())) {
                        vl2 = Math.min(vl2, 2);
                    }
                    final String current4 = MathHelper.roundFromDouble(e.getXDirectedMotion(), 5) + " " + MathHelper.roundFromDouble(e.getYDirectedMotion(), 5) + " " + MathHelper.roundFromDouble(e.getZDirectedMotion(), 5);
                    final Vector expectation4 = e.getCheckable().getMeta().getSyncedValues().lastVelocity.clone().setY(e.getYDirectedMotion());
                    final String last4 = MathHelper.shortVector(this.getNormalized(expectation4), " ");
                    if (this.plugin.getRetributionManager().markPlayer(p, vl2, "Knockback", CheatCategory.MOVING, "manipulated his knockback. (" + current4 + " expected " + last4 + ")")) {
                        this.setBack(p, e.getCheckable().getMeta().getSyncedValues().lastVelocityFI, e.getCheckable().getMeta().getLocationValues().lastVelocity.clone().add(e.getCheckable().getMeta().getSyncedValues().lastVelocityFI));
                        this.giveKnockback(p, e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.clone().multiply(1.15));
                        if (this.plugin.getThresholdsManager().shouldFlag("Knockback", 8) && this.plugin.getViolationManager().getViolationLevel(p, "Knockback") > 7) {
                            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).performVelocityCheck(expectation4);
                        }
                    }
                }
                return;
            }
            if (hadGoodVelocity && e.getCheckable().getMeta().getVioValues().velocityVL2 > 0) {
                final Checkable.CheckableMeta.ViolationValues vioValues6 = e.getCheckable().getMeta().getVioValues();
                --vioValues6.velocityVL2;
            }
        }
    }
    
    private Vector getNormalized(final Vector intputVector) {
        final Vector vector = intputVector.clone();
        if (MathHelper.amount(vector.getX()) < 0.02) {
            vector.setX(0);
        }
        if (MathHelper.amount(vector.getY()) < 0.02) {
            vector.setY(0);
        }
        if (MathHelper.amount(vector.getZ()) < 0.02) {
            vector.setZ(0);
        }
        return vector;
    }
    
    private void giveKnockback(final Player p, final Vector knockback) {
        if (knockback.clone().setY(0).length() > 1.9) {
            knockback.setX(this.limit(knockback.getX(), 1.0));
            knockback.setZ(this.limit(knockback.getZ(), 1.0));
        }
        this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().willGetArtificialVelocity = true;
        p.setVelocity(knockback);
    }
    
    private double limit(final double value, final double positiveLimit) {
        return (value > positiveLimit) ? positiveLimit : ((value < -positiveLimit) ? (-positiveLimit) : value);
    }
    
    private void delayedVelocity(final Player p, final Vector expectation, final int ticks) {
        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this.plugin, () -> this.giveKnockback(p, expectation), (long)ticks);
    }
    
    private void setBack(final Player p, final Vector expected_vector, final Location standPoint) {
        this.setBack(p, expected_vector, standPoint, 0.0, 3.0);
    }
    
    private void setBack(final Player p, final Vector expected_vector, final Location standPoint, final double minimum, final double sb_maxmulipl) {
        double d = minimum;
        Location ex = standPoint.clone();
        ex.setY(ex.getY() + 0.0025 + minimum);
        do {
            ex = standPoint.clone().add(expected_vector.clone().multiply(d));
            ex.setY(standPoint.getY() + Math.max(0.12, expected_vector.getY()));
            d += 0.0025;
        } while (hasHeapSpace(p, ex.clone().add(expected_vector.clone().multiply(d))) && d < sb_maxmulipl && !LocationHelper.isInsideUnpassable(p.getWorld(), (Entity)p, ex.clone().add(expected_vector.clone().multiply(d))) && !LocationHelper.collidesAny(p.getWorld(), (Entity)p, ex.clone().add(expected_vector.clone().multiply(d))) && hasHeapSpace(p, ex) && !LocationHelper.isInsideUnpassable(p.getWorld(), (Entity)p, ex) && !LocationHelper.collidesAny(p.getWorld(), (Entity)p, ex));
        if (d < 0.5) {
            return;
        }
        if (!LocationHelper.isInsideUnpassable(p.getWorld(), (Entity)p, ex) && BlockData.isPassable(ex.getBlock())) {
            ((MoveCheck)this.plugin.getCheckManager().getCheck("Move")).teleportAsync(p, ex, "v-kell|/corr");
        }
    }
    
    private static boolean hasHeapSpace(final Player p, final Location location) {
        return (!LocationHelper.collidesLiquid((Entity)p, location) && !LocationHelper.collidesAnyXZAxis(p.getWorld(), (Entity)p, location) && !BlockData.doesAffectMovementSimple(LocationHelper.getBlockAt(location))) || LocationHelper.isInsideUnpassable(p.getWorld(), (Entity)p, location);
    }
}
