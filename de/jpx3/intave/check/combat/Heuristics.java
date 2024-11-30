// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.combat;

import java.util.stream.IntStream;
import net.md_5.bungee.api.ChatColor;
import de.jpx3.intave.api.external.linked.exceptions.IntaveInternalException;
import java.util.concurrent.CopyOnWriteArrayList;
import de.jpx3.intave.util.IntaveExceptionHandler;
import java.util.ArrayList;
import de.jpx3.intave.api.internal.reflections.Reflections;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import java.util.function.Predicate;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.util.Vector;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import de.jpx3.intave.module.PersistentDebugTelemetry;
import org.bukkit.permissions.Permissible;
import de.jpx3.intave.api.external.IntavePermission;
import de.jpx3.intave.util.event.CheckableDamageEntityEvent;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import de.jpx3.intave.util.calc.BalanceUtils;
import com.comphenix.protocol.utility.MinecraftVersion;
import de.jpx3.intave.api.internal.ViaVersionAdapter;
import org.bukkit.entity.Entity;
import de.jpx3.intave.util.calc.LocationHelper;
import de.jpx3.intave.util.calc.PlayerUtils;
import de.jpx3.intave.util.event.CheckableMoveEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.Iterator;
import java.util.List;
import de.jpx3.intave.util.objectable.Checkable;
import de.jpx3.intave.util.calc.YawUtil;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.PacketType;
import de.jpx3.intave.api.internal.ProtocolLibAdapter;
import de.jpx3.intave.util.calc.MathHelper;
import org.bukkit.entity.Player;
import java.util.concurrent.ThreadLocalRandom;
import de.jpx3.intave.antipiracy.IIUA;
import org.bukkit.plugin.Plugin;
import java.util.concurrent.ConcurrentHashMap;
import de.jpx3.intave.util.enums.CheatCategory;
import java.util.UUID;
import java.util.Map;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.event.Listener;
import de.jpx3.intave.util.objectable.IntaveCheck;

public final class Heuristics extends IntaveCheck implements Listener
{
    private IntavePlugin plugin;
    private static final float[] SIN_TABLE_FAST;
    private static final float[] SIN_TABLE;
    private final boolean rotationDebug = false;
    private final boolean debug = false;
    private Boolean aggressive;
    private final Map<UUID, Object[]> entityBlocks;
    private final Map<UUID, UUID> observing;
    private final Map<UUID, Boolean> canSeeEntitys;
    private Map<UUID, Long> lastEntitySpawn;
    
    public Heuristics(final IntavePlugin plugin) {
        super("Heuristics", CheatCategory.COMBAT);
        this.aggressive = null;
        this.entityBlocks = new ConcurrentHashMap<UUID, Object[]>();
        this.observing = new ConcurrentHashMap<UUID, UUID>();
        this.canSeeEntitys = new ConcurrentHashMap<UUID, Boolean>();
        this.lastEntitySpawn = new ConcurrentHashMap<UUID, Long>();
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)plugin);
        final Checkable.CheckableMeta.HeuristicValues heuristicValues;
        final boolean isSave;
        final double minConfi;
        double nwConfi;
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)plugin, () -> plugin.getServer().getOnlinePlayers().stream().map(all -> plugin.catchCheckable(IIUA.getUUIDFrom(all))).filter(checkable -> checkable.getMeta().getHeuristicValues().flagKalysaMachineLearning).forEachOrdered(checkable -> {
            heuristicValues = checkable.getMeta().getHeuristicValues();
            isSave = (heuristicValues.rotMoveComparisonVL_combined_total > 20);
            minConfi = (isSave ? 90.0 : this.getMinConfidenceToFlag());
            if (IIUA.getCurrentTimeMillis() - heuristicValues.lastTime51CBTReset > 216000000L) {
                heuristicValues.lastTime51CBTReset = IIUA.getCurrentTimeMillis();
                heuristicValues.rotMoveComparisonVL_combined_total = 0;
            }
            if (checkable.getPatternManager().getPatternConfidenceFor("51") <= minConfi) {
                nwConfi = checkable.getPatternManager().getPatternConfidenceFor("51") + ThreadLocalRandom.current().nextDouble(1.0, 10.0) + ThreadLocalRandom.current().nextFloat();
                if (nwConfi > minConfi) {
                    nwConfi = minConfi + 0.5;
                }
                checkable.getPatternManager().setPatternConfidenceFor("51", nwConfi);
            }
            else {
                checkable.getMeta().getHeuristicValues().flagKalysaMachineLearning = false;
            }
        }), 0L, (long)(20 * ThreadLocalRandom.current().nextInt(5, 15)));
        final Iterator<Player> iterator;
        Player p;
        Map<String, Double> g;
        String mostAccuratePattern;
        double mostAccuratePatternConfidence;
        final Iterator<Map.Entry<String, Double>> iterator2;
        Map.Entry<String, Double> entry;
        String s;
        Double aDouble;
        Checkable checkable2;
        boolean flagged;
        StringBuilder f;
        int vl;
        plugin.getServer().getScheduler().scheduleAsyncRepeatingTask((Plugin)plugin, () -> {
            if (!this.isActivated()) {
                return;
            }
            else {
                plugin.getServer().getOnlinePlayers().iterator();
                while (iterator.hasNext()) {
                    p = iterator.next();
                    try {
                        g = plugin.catchCheckable(IIUA.getUUIDFrom(p)).getPatternManager().getAllPatternConfidences();
                        mostAccuratePattern = "";
                        mostAccuratePatternConfidence = 0.0;
                        g.entrySet().iterator();
                        while (iterator2.hasNext()) {
                            entry = iterator2.next();
                            s = entry.getKey();
                            aDouble = entry.getValue();
                            if (aDouble > this.getMinConfidenceToFlag() && mostAccuratePatternConfidence < aDouble) {
                                mostAccuratePattern = s;
                                mostAccuratePatternConfidence = aDouble;
                            }
                        }
                        checkable2 = plugin.catchCheckable(IIUA.getUUIDFrom(p));
                        flagged = false;
                        if (!mostAccuratePattern.equalsIgnoreCase("")) {
                            f = new StringBuilder(MathHelper.roundFromDouble(mostAccuratePatternConfidence, 2));
                            if (f.toString().startsWith("1")) {
                                f.append("0");
                            }
                            vl = 1;
                            if (mostAccuratePatternConfidence > 90.0) {
                                ++vl;
                            }
                            if (mostAccuratePatternConfidence > 95.0) {
                                ++vl;
                            }
                            flagged = true;
                            plugin.getRetributionManager().markPlayer(p, vl, "Heuristics", CheatCategory.COMBAT, "is/was fighting computer-like (p[" + mostAccuratePattern + "] at " + (Object)f + "%)");
                        }
                        checkable2.getMeta().getHeuristicValues().flaggedAnyPatternInCurrentCyc = flagged;
                        plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().canHitOthers = !flagged;
                    }
                    catch (Exception e) {
                        this.setActivated(false);
                        plugin.getILogger().error("[FATAL] The Heuristics check crashed. Please restart the server.");
                        e.printStackTrace();
                    }
                }
                return;
            }
        }, 200L, 140L);
        final IntavePlugin plugin2 = plugin;
        ProtocolLibAdapter.getProtocolManager().addPacketListener((PacketListener)new PacketAdapter(plugin, new PacketType[] { PacketType.Play.Client.LOOK }) {
            public void onPacketReceiving(final PacketEvent event) {
                if (!Heuristics.this.isActivated()) {
                    return;
                }
                final Player p = event.getPlayer();
                final PacketContainer packet = event.getPacket();
                final float newYawPosition = packet.getFloat().getValues().get(0);
                final float newPitchPosition = packet.getFloat().getValues().get(1);
                if (p == null || p.getLocation() == null) {
                    return;
                }
                if (p.getLocation().getYaw() == newYawPosition && p.getLocation().getPitch() == newPitchPosition && plugin2.isLinkedToIntave(IIUA.getUUIDFrom(p)) && IIUA.getCurrentTimeMillis() - plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeFlagged > 1000L && p.getVehicle() == null && IIUA.getCurrentTimeMillis() - plugin2.catchCheckable(IIUA.getUUIDFrom(p)).setupTime > 15000L && IIUA.getCurrentTimeMillis() - plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeTeleported > 5000L) {
                    Heuristics.this.addVLToPattern(plugin2.catchCheckable(IIUA.getUUIDFrom(p)), "11", 5.0);
                }
            }
        });
        ProtocolLibAdapter.getProtocolManager().addPacketListener((PacketListener)new PacketAdapter(plugin, new PacketType[] { PacketType.Play.Client.POSITION_LOOK, PacketType.Play.Client.LOOK }) {
            public void onPacketReceiving(final PacketEvent event) {
                final Player p = event.getPlayer();
                final PacketContainer packetContainer = event.getPacket();
                if (event.getPacketType().equals((Object)PacketType.Play.Client.POSITION_LOOK) || event.getPacketType().equals((Object)PacketType.Play.Client.LOOK)) {
                    final Checkable.CheckableMeta.HeuristicValues heuristicValues = plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getHeuristicValues();
                    final List<Float> packetFloats = (List<Float>)packetContainer.getFloat().getValues();
                    final float newYaw = packetFloats.get(0);
                    final float newPitch = packetFloats.get(1);
                    final float oldYaw = plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getHeuristicValues().lastPLYaw;
                    final float oldPitch = plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getHeuristicValues().lastPLPitch;
                    final double yawDiff = YawUtil.yawDiff(oldYaw, newYaw);
                    final double pitchDiff = YawUtil.pitchDiff(oldPitch, newPitch);
                    final double movementTotal = Math.abs(yawDiff) + Math.abs(pitchDiff);
                    final long lastAttack = IIUA.getCurrentTimeMillis() - plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastHitOtherTimestamp;
                    if (movementTotal > 8.0 && lastAttack < 4000L) {
                        final Checkable.CheckableMeta.HeuristicValues heuristicValues2 = heuristicValues;
                        ++heuristicValues2.mouseUnsmooth;
                        if (movementTotal > 30.0 && lastAttack < 250L) {
                            final Checkable.CheckableMeta.HeuristicValues heuristicValues3 = heuristicValues;
                            ++heuristicValues3.mouseUnsmooth;
                            if (movementTotal > 60.0) {
                                final Checkable.CheckableMeta.HeuristicValues heuristicValues4 = heuristicValues;
                                ++heuristicValues4.mouseUnsmooth;
                            }
                        }
                    }
                    else if (heuristicValues.mouseUnsmooth > 0) {
                        if (movementTotal < 2.0 || lastAttack > 5000L) {
                            heuristicValues.mouseUnsmooth = 0;
                        }
                        else {
                            final Checkable.CheckableMeta.HeuristicValues heuristicValues5 = heuristicValues;
                            --heuristicValues5.mouseUnsmooth;
                        }
                    }
                    heuristicValues.lastPLYaw = newYaw;
                    heuristicValues.lastPLPitch = newPitch;
                }
            }
        });
        ProtocolLibAdapter.getProtocolManager().addPacketListener((PacketListener)new PacketAdapter(plugin, new PacketType[] { PacketType.Play.Client.BLOCK_DIG }) {
            public void onPacketReceiving(final PacketEvent event) {
                if (!Heuristics.this.isActivated()) {
                    return;
                }
                final Player p = event.getPlayer();
                final long lastHit = IIUA.getCurrentTimeMillis() - plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastHitOtherTimestamp;
                if (lastHit < 15L) {
                    final Checkable.CheckableMeta.HeuristicValues heuristicValues = plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getHeuristicValues();
                    heuristicValues.autoBlockVL += 4;
                    final int newVL = plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getHeuristicValues().autoBlockVL;
                    if (newVL > 20) {
                        plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getPatternManager().multiplyPatternConfidence("12", 1.2);
                    }
                }
                else if (plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getHeuristicValues().autoBlockVL > 0) {
                    final Checkable.CheckableMeta.HeuristicValues heuristicValues2 = plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getHeuristicValues();
                    --heuristicValues2.autoBlockVL;
                    plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getPatternManager().multiplyPatternConfidence("12", 0.999999);
                }
            }
        });
        ProtocolLibAdapter.getProtocolManager().addPacketListener((PacketListener)new PacketAdapter(plugin, new PacketType[] { PacketType.Play.Client.LOOK, PacketType.Play.Client.POSITION_LOOK, PacketType.Play.Client.FLYING, PacketType.Play.Client.POSITION }) {
            public void onPacketReceiving(final PacketEvent event) {
                final PacketContainer pkt = event.getPacket();
                final Player p = event.getPlayer();
                final Checkable checkable = plugin2.catchCheckable(IIUA.getUUIDFrom(p));
                final PacketType pktType = pkt.getType();
                final long currentTSMP = IIUA.getCurrentTimeMillis();
                if (pktType.equals((Object)PacketType.Play.Client.POSITION_LOOK)) {
                    checkable.getMeta().getHeuristicValues().lastPositionLookPacket = currentTSMP;
                }
            }
        });
        ProtocolLibAdapter.getProtocolManager().addPacketListener((PacketListener)new PacketAdapter(plugin, new PacketType[] { PacketType.Play.Client.USE_ENTITY }) {
            public void onPacketReceiving(final PacketEvent event) {
                final Player p = event.getPlayer();
                final Checkable checkable = plugin2.catchCheckable(IIUA.getUUIDFrom(p));
                final long lastPosLookPkt = IIUA.getCurrentTimeMillis() - checkable.getMeta().getHeuristicValues().lastPositionLookPacket;
                if (lastPosLookPkt < 23L && lastPosLookPkt > 0L) {
                    final int vl = ++checkable.getMeta().getHeuristicValues().deltaVL;
                    if (checkable.getMeta().getHeuristicValues().deltaVL > 10) {
                        checkable.getMeta().getHeuristicValues().deltaVL = 10;
                    }
                    if (vl > 5) {
                        Heuristics.this.debug("19", p, "l: " + lastPosLookPkt + " vl: " + vl);
                    }
                }
                else if (checkable.getMeta().getHeuristicValues().deltaVL > 0) {
                    final Checkable.CheckableMeta.HeuristicValues heuristicValues = checkable.getMeta().getHeuristicValues();
                    --heuristicValues.deltaVL;
                }
                checkable.getMeta().getSyncedValues().attacked = true;
                checkable.getMeta().getSyncedValues().moveArrivedAfterAttack = false;
                checkable.getMeta().getHeuristicValues().lastAttackPacket = IIUA.getCurrentTimeMillis();
            }
        });
    }
    
    @EventHandler
    public void on1(final PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        final Checkable checkable = this.plugin.catchCheckable(IIUA.getUUIDFrom(p));
        final boolean attacked = checkable.getMeta().getSyncedValues().attacked;
        if (e.getAction().equals((Object)Action.LEFT_CLICK_AIR) || e.getAction().equals((Object)Action.LEFT_CLICK_BLOCK)) {}
        checkable.getMeta().getSyncedValues().attacked = false;
    }
    
    @EventHandler
    public void on(final PlayerInteractAtEntityEvent e) {
        if (!this.isActivated()) {
            return;
        }
        final Player p = e.getPlayer();
        this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastClickInteraction = IIUA.getCurrentTimeMillis();
    }
    
    @Override
    public void onCheckableMove(final CheckableMoveEvent e) {
        this.rotationAnalytics(e);
        this.verifTeleportRotation(e);
    }
    
    private void atanAnalysis(final CheckableMoveEvent e) {
        final Player p = e.getBukkitPlayer();
        final UUID uuid = IIUA.getUUIDFrom(p);
        double motionX = e.getXDirectedMotion();
        double motionZ = e.getZDirectedMotion();
        final double motionXZ = motionX * motionX + motionZ * motionZ;
        final double lmotionX = 0.0;
        final double lmotionZ = 0.0;
        if (Math.abs(motionX) < 0.005) {
            motionX = 0.0;
        }
        if (Math.abs(motionZ) < 0.005) {
            motionZ = 0.0;
        }
        if (!p.isOnGround() && Math.abs(p.getLocation().getY() % 0.125) != 0.0) {
            final double mx = motionX - lmotionX;
            final double mz = motionZ - lmotionZ;
            final float motionYaw = (float)(Math.atan2(mz, mx) * 180.0 / 3.141592653589793 - 90.0) - e.getTo().getYaw();
            final double motionAngle = Math.abs(this.angleTo180(motionYaw));
            final double solve_error = IntavePlugin.getStaticReference().catchCheckable(uuid).checkableMoveEvent.getYDirectedMotion() / 0.98 + 0.08 - e.getCheckable().getMeta().getSyncedValues().lastYmovement;
            final boolean doCheck2 = motionAngle % 45.0 > 0.14 && motionAngle % 45.0 < 43.0;
            p.sendMessage(e.isMoving() + " " + (e.getXZDirectedMotion() > e.getCheckable().getMeta().getSyncedValues().lastXZMovement) + " " + (motionXZ > 0.0041) + " " + doCheck2 + " " + (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeOnGroundACC > 103L));
            if (e.isMoving() && e.isRotating() && e.getXZDirectedMotion() > e.getCheckable().getMeta().getSyncedValues().lastXZMovement && motionXZ > 0.0041 && doCheck2 && solve_error < 0.0125 && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeOnGroundACC > 103L) {
                final Checkable.CheckableMeta.HeuristicValues heuristicValues = e.getCheckable().getMeta().getHeuristicValues();
                ++heuristicValues.atanVL;
                if (e.getCheckable().getMeta().getHeuristicValues().atanVL > 1) {
                    this.addVLToPattern(IntavePlugin.getStaticReference().catchCheckable(uuid), "81", 3.0 + ThreadLocalRandom.current().nextDouble(0.0, 2.0));
                    this.debug("81", p, "[atan] rotated with unusual angle ~" + motionAngle % 45.0 + " / vl: " + e.getCheckable().getMeta().getHeuristicValues().atanVL);
                }
            }
            else if (e.getCheckable().getMeta().getHeuristicValues().atanVL >= 1) {
                final Checkable.CheckableMeta.HeuristicValues heuristicValues2 = e.getCheckable().getMeta().getHeuristicValues();
                --heuristicValues2.atanVL;
                this.removeVLFromPattern(IntavePlugin.getStaticReference().catchCheckable(uuid), "81", ThreadLocalRandom.current().nextDouble() * 2.0);
            }
        }
    }
    
    private float angleTo180(float value) {
        value %= 360.0f;
        if (value >= 180.0f) {
            value -= 360.0f;
        }
        if (value < -180.0f) {
            value += 360.0f;
        }
        return value;
    }
    
    private void rotationAnalytics(final CheckableMoveEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        final Checkable.CheckableMeta.HeuristicValues heuristicValues = e.getCheckable().getMeta().getHeuristicValues();
        final Entity nearest = PlayerUtils.getNearestEntity(p);
        if (IIUA.getAllowFlight(p)) {
            return;
        }
        final long lastAttack = IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastHitOtherTimestamp;
        final float yawDiffX = YawUtil.yawDiff(e.getFrom().getYaw(), e.getTo().getYaw());
        final double moveDiff = MathHelper.diff(e.getCheckable().getMeta().getHeuristicValues().lastDirection, e.getDirection());
        final boolean suspicious = MathHelper.amount(yawDiffX) > 20.0 && moveDiff < 0.5 && e.getCheckable().getMeta().getHeuristicValues().lastDirectionDiff < 0.5 && (e.getXZDirectedMotion() >= 0.3 || (MathHelper.amount(yawDiffX) > 20.0 && moveDiff > 0.002));
        if (!IIUA.isPretendingToBeOnGround(p) && !LocationHelper.isOnGroundAccurate(e.getTo(), (Entity)p) && !LocationHelper.hasStepableNearby(e.getTo()) && e.getYDirectedMotion() < e.getCheckable().getMeta().getSyncedValues().lastYmovement && suspicious) {
            final Checkable.CheckableMeta.HeuristicValues heuristicValues2 = heuristicValues;
            heuristicValues2.rotMoveComparisonVL += (p.isSprinting() ? 4 : ((moveDiff > 0.001) ? 2 : 1));
            heuristicValues.lastMRVLClean = IIUA.getCurrentTimeMillis();
        }
        else if (IIUA.getCurrentTimeMillis() - heuristicValues.lastMRVLClean > 20000L && !e.getCheckable().getMeta().getHeuristicValues().flagPattern57 && heuristicValues.rotMoveComparisonVL > 0) {
            heuristicValues.rotMoveComparisonVL = 0;
            heuristicValues.lastMRVLClean = IIUA.getCurrentTimeMillis();
        }
        if (heuristicValues.rotMoveComparisonVL > 7 || e.getCheckable().getMeta().getHeuristicValues().flagPattern57) {
            e.getCheckable().getMeta().getHeuristicValues().flagPattern57 = true;
        }
        if (heuristicValues.rotMoveComparisonVL_combined_total > 6) {
            e.getCheckable().getMeta().getHeuristicValues().flagKalysaMachineLearning = true;
        }
        heuristicValues.lastDirection = e.getDirection();
        heuristicValues.lastDirectionDiff = moveDiff;
        if (!LocationHelper.isOnGroundAccurate(e.getTo(), (Entity)p) && !LocationHelper.isOnGroundAccurate(e.getFrom(), (Entity)p)) {
            final double var7 = e.getCheckable().getMeta().getSyncedValues().lastXMovement * 0.9100000262260437 - e.getXDirectedMotion();
            final double var8 = e.getCheckable().getMeta().getSyncedValues().lastZMovement * 0.9100000262260437 - e.getZDirectedMotion();
            final double strafe2 = var7 * var7 + var8 * var8;
            boolean wasChecked = false;
            if (!LocationHelper.collidesAnyXZAxis(p.getWorld(), (Entity)p, e.getFrom()) && !LocationHelper.collidesAnyXZAxis(p.getWorld(), (Entity)p, e.getTo()) && strafe2 > 4.0E-4) {
                final double strafeDiff = MathHelper.diff(strafe2, heuristicValues.lastStrafeValue);
                int yawDiffMin = 3;
                if (ViaVersionAdapter.hasViaVersion()) {
                    final int version = ViaVersionAdapter.getPlayerVersion(p);
                    if (version > 210) {
                        yawDiffMin = 16;
                    }
                }
                else if (ProtocolLibAdapter.getProtocolManager().getMinecraftVersion().isAtLeast(MinecraftVersion.FROSTBURN_UPDATE)) {
                    yawDiffMin = 16;
                }
                if (strafeDiff < 1.0E-14 && yawDiffX > yawDiffMin) {
                    final int addedVl = (lastAttack < 1000L) ? ((yawDiffX < 20.0f) ? 2 : 4) : 1;
                    final Checkable.CheckableMeta.HeuristicValues heuristicValues3 = heuristicValues;
                    final int rotMoveComparisonVL2 = heuristicValues3.rotMoveComparisonVL2 + addedVl;
                    heuristicValues3.rotMoveComparisonVL2 = rotMoveComparisonVL2;
                    final int vl = rotMoveComparisonVL2;
                    final int combinedTotal = ++heuristicValues.rotMoveComparisonVL_combined_total;
                    heuristicValues.lastSTRFROTClean = IIUA.getCurrentTimeMillis();
                    this.debug("51", p, "[kalysa] rotated dynamic. (AIR " + strafeDiff + "/" + yawDiffX + ") cbT: " + combinedTotal + " / vl: " + vl);
                    if (heuristicValues.rotMoveComparisonVL2 > 7 && lastAttack < 1000L) {
                        e.getCheckable().getMeta().getHeuristicValues().flagKalysaMachineLearning = true;
                    }
                    wasChecked = true;
                }
            }
            if (!wasChecked && IIUA.getCurrentTimeMillis() - heuristicValues.lastSTRFROTClean > 20000L) {
                heuristicValues.rotMoveComparisonVL2 = 0;
                heuristicValues.lastSTRFROTClean = IIUA.getCurrentTimeMillis();
            }
            heuristicValues.lastStrafeValue = strafe2;
        }
        else if (e.isOnGround()) {
            final double var7 = e.getCheckable().getMeta().getSyncedValues().lastXMovement * 0.9100000262260437 * 0.60000005239967 - e.getXDirectedMotion();
            final double var8 = e.getCheckable().getMeta().getSyncedValues().lastZMovement * 0.9100000262260437 * 0.60000005239967 - e.getZDirectedMotion();
            final double strafe2 = var7 * var7 + var8 * var8;
            int yawDiffMin2 = 3;
            if (ViaVersionAdapter.hasViaVersion()) {
                final int version2 = ViaVersionAdapter.getPlayerVersion(p);
                if (version2 > 210) {
                    yawDiffMin2 = 16;
                }
            }
            else if (ProtocolLibAdapter.getProtocolManager().getMinecraftVersion().isAtLeast(MinecraftVersion.FROSTBURN_UPDATE)) {
                yawDiffMin2 = 16;
            }
            if (e.getXZDirectedMotion() > 0.1 && MathHelper.diff(e.getYDirectedMotion(), 0.0) < 1.0E-4) {
                final double strafeDiff = MathHelper.diff(strafe2, heuristicValues.lastStrafeOGValue);
                if (strafeDiff < 1.0E-13 && MathHelper.amount(yawDiffX) > yawDiffMin2 && lastAttack < 1000L) {
                    final Checkable.CheckableMeta.HeuristicValues heuristicValues4 = heuristicValues;
                    final int rotMoveComparisonVL3 = heuristicValues4.rotMoveComparisonVL3 + 3;
                    heuristicValues4.rotMoveComparisonVL3 = rotMoveComparisonVL3;
                    final int vl2 = rotMoveComparisonVL3;
                    final int combinedTotal2 = ++heuristicValues.rotMoveComparisonVL_combined_total;
                    this.debug("51", p, "[kalysa] rotated dynamic. (GRD " + strafeDiff + "/" + yawDiffX + ") cbT: " + combinedTotal2 + " / vl: " + vl2);
                    if (heuristicValues.rotMoveComparisonVL3 > 9) {
                        e.getCheckable().getMeta().getHeuristicValues().flagKalysaMachineLearning = true;
                    }
                }
            }
            if (heuristicValues.rotMoveComparisonVL3 > 0 && IIUA.getCurrentTimeMillis() - heuristicValues.lastSTRFROT2Clean > 20000L) {
                heuristicValues.rotMoveComparisonVL3 = 0;
                heuristicValues.lastSTRFROT2Clean = IIUA.getCurrentTimeMillis();
            }
            heuristicValues.lastStrafeOGValue = strafe2;
        }
        if (nearest == null) {
            if (heuristicValues.tracingVL > 0) {
                final Checkable.CheckableMeta.HeuristicValues heuristicValues5 = heuristicValues;
                --heuristicValues5.tracingVL;
            }
            heuristicValues.lastCDiff = YawUtil.yawDiff(e.getFrom().getYaw(), e.getTo().getYaw());
            heuristicValues.lastrDir = 0.0f;
            return;
        }
        final float dir = YawUtil.getYawFrom(p.getLocation(), nearest.getLocation());
        final float nDir = YawUtil.getYawFrom(e.getTo(), nearest.getLocation());
        final float cdiff = YawUtil.yawDiff(e.getFrom().getYaw(), e.getTo().getYaw());
        final float cdiff2 = YawUtil.pitchDiff(e.getFrom().getPitch(), e.getTo().getPitch());
        final float diff1 = YawUtil.yawDiff(e.getFrom().getYaw(), dir);
        final float diff2 = YawUtil.yawDiff(e.getTo().getYaw(), dir);
        final float rDir = YawUtil.yawDiff(dir, e.getTo().getYaw());
        final float rnDir = YawUtil.yawDiff(nDir, e.getTo().getYaw());
        final double rDirDiff = MathHelper.diff(rDir, heuristicValues.lastrDir);
        if (rDir != rnDir && e.isRotating() && Math.abs(cdiff) > 0.0f) {
            final double rnDirDiff = MathHelper.diff(rnDir, heuristicValues.lastrnDir);
            boolean flag1 = false;
            if (rnDirDiff == 0.0 && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeTeleported < 1000L) {
                flag1 = true;
            }
            final boolean isAimingTowartsEntity = diff2 < diff1 || diff2 < 5.0f;
            if (e.isRotating() && isAimingTowartsEntity && e.getCheckable().getPing() < 80) {
                final boolean flagAimBot = heuristicValues.lastrnDirDiff >= rnDirDiff && rnDirDiff < 0.05;
                if (flagAimBot) {
                    this.addVLToPattern(e.getCheckable(), "52", ThreadLocalRandom.current().nextInt(1, 3));
                }
            }
            if (rnDirDiff < 0.001 && !flag1 && isAimingTowartsEntity) {
                this.addVLToPattern(e.getCheckable(), "52", ThreadLocalRandom.current().nextInt(1, 2));
                if (e.getCheckable().getPatternManager().getPatternConfidenceFor("52") > 70.0) {
                    this.debug("52", p, "aiming equaly! Aim: " + rDirDiff);
                }
            }
            e.getCheckable().getPatternManager().multiplyPatternConfidence("52", 0.9996);
            heuristicValues.lastrnDirDiff = rnDirDiff;
        }
        e.getCheckable().getPatternManager().multiplyPatternConfidence("52", 0.9996);
        heuristicValues.lastrDir = rDir;
        heuristicValues.lastrnDir = rnDir;
        heuristicValues.lastrDirDiff = rDirDiff;
        final long lastlvlclear2 = IIUA.getCurrentTimeMillis() - heuristicValues.lastFragVLClean;
        final double yawDiff = MathHelper.amount(YawUtil.yawDiff(e.getFrom().getYaw(), e.getTo().getYaw()));
        final double yawDiffDiff = MathHelper.diff(yawDiff, heuristicValues.lastYawADiff);
        if (yawDiffDiff < 0.1 && yawDiff > 2.0) {
            if (yawDiffDiff > 0.005 && lastAttack < 1000L && this.aggressiveModeActivated()) {
                if (yawDiffDiff < 0.01) {
                    this.addVLToPattern(e.getCheckable(), "53", 15.0 + ThreadLocalRandom.current().nextDouble(0.0, 5.0));
                }
                if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getHeuristicValues().lastIYKClean > 60000L) {
                    e.getCheckable().getMeta().getHeuristicValues().iykVL = 0;
                    e.getCheckable().getMeta().getHeuristicValues().lastIYKClean = IIUA.getCurrentTimeMillis();
                }
                final int iykVl = ++e.getCheckable().getMeta().getHeuristicValues().iykVL;
                if (iykVl > 10 || yawDiffDiff < 0.01) {
                    this.debug((yawDiffDiff < 0.01) ? "53" : "54", p, "[iyk] smooth aiming ~" + yawDiffDiff + "/ vl: " + iykVl);
                }
                if (iykVl > 20) {
                    e.getCheckable().getPatternManager().setPatternConfidenceFor("54", 85.1 + ThreadLocalRandom.current().nextDouble(1.0, 5.0));
                }
            }
            if (lastAttack < 500L) {
                final Checkable.CheckableMeta.HeuristicValues heuristicValues6 = heuristicValues;
                heuristicValues6.silimarRotationVL += ((yawDiffDiff > 0.005) ? 60 : 3);
            }
            if (heuristicValues.silimarRotationVL > 30) {
                heuristicValues.silimarRotationVL = 0;
            }
        }
        else if (heuristicValues.silimarRotationVL > 0) {
            final Checkable.CheckableMeta.HeuristicValues heuristicValues7 = heuristicValues;
            --heuristicValues7.silimarRotationVL;
        }
        heuristicValues.lastYawADiff = (float)yawDiff;
        if (heuristicValues.fragVL > 0 && lastlvlclear2 > 2000L) {
            final Checkable.CheckableMeta.HeuristicValues heuristicValues8 = heuristicValues;
            --heuristicValues8.fragVL;
            heuristicValues.lastFragVLClean = IIUA.getCurrentTimeMillis();
        }
        if (MathHelper.amount(cdiff) < 5.0 && MathHelper.amount(cdiff) > 1.0 && MathHelper.amount(diff2) < 5.0 && MathHelper.amount(cdiff2) > 1.0) {
            if (MathHelper.diff(cdiff, diff1) < 0.5) {
                if (heuristicValues.suspiciousAimBotAims > 2) {
                    this.addVLToPattern(e.getCheckable(), "55", 5.0);
                }
                final Checkable.CheckableMeta.HeuristicValues heuristicValues9 = heuristicValues;
                ++heuristicValues9.suspiciousAimBotAims;
            }
        }
        else if (e.getCheckable().getMeta().getHeuristicValues().suspiciousAimBotAims > 0) {
            final Checkable.CheckableMeta.HeuristicValues heuristicValues10 = heuristicValues;
            --heuristicValues10.suspiciousAimBotAims;
            e.getCheckable().getPatternManager().multiplyPatternConfidence("55", 0.9999);
        }
        if (cdiff > 60.0f && MathHelper.amount(diff1) < 90.0 && MathHelper.amount(diff1) > 70.0 && MathHelper.amount(diff2) < 2.0) {
            if (e.getCheckable().getPatternManager().getPatternConfidenceFor("57") < 70.0) {
                e.getCheckable().getPatternManager().setPatternConfidenceFor("57", 80.0);
            }
            else {
                e.getCheckable().getPatternManager().multiplyPatternConfidence("57", 1.6);
            }
        }
        else {
            e.getCheckable().getPatternManager().multiplyPatternConfidence("57", 0.999);
        }
        final double TFYawDiff = Math.abs(YawUtil.yawDiff(e.getFrom().getYaw(), e.getTo().getYaw()));
        final double TFPitchDiff = Math.abs(YawUtil.pitchDiff(e.getFrom().getPitch(), e.getTo().getPitch()));
        final boolean doCheck = TFYawDiff > 0.001 && TFPitchDiff > 0.001;
        final boolean sameYaw = e.getFrom().getYaw() == e.getTo().getYaw();
        final boolean samePitch = e.getFrom().getPitch() == e.getTo().getPitch();
        final boolean aggressiveMode = this.aggressiveModeActivated();
        if (doCheck && aggressiveMode && Math.abs(TFPitchDiff / TFYawDiff) > 0.001 && (!sameYaw || !samePitch) && (Math.abs(TFPitchDiff / TFYawDiff) < 0.001 || Math.abs(TFYawDiff / TFPitchDiff) < 0.001)) {
            this.debug("58", p, "[eion/iyk-ext] too small rotation (" + Math.abs(TFPitchDiff / TFYawDiff) + " / " + Math.abs(TFYawDiff / TFPitchDiff));
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastHitOtherTimestamp < 200L) {
            if (MathHelper.amount(diff1) > MathHelper.amount(diff2)) {
                if (MathHelper.diff(cdiff, 0.0f) < 5.0f && MathHelper.diff(cdiff, 0.0f) > 1.0f && MathHelper.amount(diff2) < 1.8) {
                    e.getCheckable().getPatternManager().multiplyPatternConfidence("59", 1.04);
                }
            }
            else if (e.getCheckable().getPatternManager().getPatternConfidenceFor("59") < 90.0) {
                e.getCheckable().getPatternManager().multiplyPatternConfidence("59", 0.95);
            }
            else {
                e.getCheckable().getPatternManager().multiplyPatternConfidence("59", 0.99);
            }
        }
        heuristicValues.lastYawMove = cdiff;
        heuristicValues.lastCDiff = cdiff;
    }
    
    public double getBalance(final List<Double> doubles) {
        return BalanceUtils.getSquaredBalanceFromDouble(doubles);
    }
    
    public double addNumberAndGetBalance(final List<Double> doubles, final double toAdd, final int maxSize) {
        return BalanceUtils.getSquaredBalanceFromDouble(this.addDynamic(doubles, toAdd, maxSize));
    }
    
    private <T> List<T> addDynamic(final List<T> list, final T toAdd, final double maxSize) {
        list.add(toAdd);
        if (list.size() > maxSize) {
            list.remove(0);
        }
        return list;
    }
    
    @EventHandler
    public void on(final PlayerAnimationEvent e) {
        if (!this.isActivated()) {
            return;
        }
        final Player p = e.getPlayer();
    }
    
    @EventHandler
    public final void on(final PlayerTeleportEvent e) {
        final Checkable checkable = this.plugin.catchCheckable(IIUA.getUUIDFrom(e.getPlayer()));
        checkable.getMeta().getLocationValues().lastTeleportToLocation = e.getTo();
        if (LocationHelper.getDistanceSafe(e.getFrom(), e.getTo()) > 0.2 && e.getTo().getY() >= e.getFrom().getY() && MathHelper.diff(e.getFrom().getY(), e.getTo().getY()) < 0.4 && e.getTo().getWorld().equals(e.getFrom().getWorld())) {
            checkable.getMeta().getSyncedValues().positionUpdateExpected = true;
        }
    }
    
    public final void verifTeleportRotation(final CheckableMoveEvent e) {
        final Player p = e.getBukkitPlayer();
        final Checkable checkable = this.plugin.catchCheckable(IIUA.getUUIDFrom(p));
        if (checkable.getMeta().getSyncedValues().positionUpdateExpected) {
            final Location last = checkable.getMeta().getLocationValues().lastTeleportToLocation;
            if (LocationHelper.getDistanceSafe(e.getTo(), last) < 0.001) {
                if (Math.abs(YawUtil.yawDiff(e.getTo().getYaw(), last.getYaw())) + Math.abs(YawUtil.pitchDiff(e.getTo().getPitch(), last.getPitch())) > 20.0f) {
                    this.debug("41", p, "sent teleport-responce with unexpected rotation values (" + YawUtil.yawDiff(e.getTo().getYaw(), last.getYaw()) + "/" + YawUtil.pitchDiff(e.getTo().getPitch(), last.getPitch()) + ")");
                }
                checkable.getMeta().getSyncedValues().positionUpdateExpected = false;
            }
        }
    }
    
    @EventHandler
    public void on(final PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        if (e.getAction().equals((Object)Action.LEFT_CLICK_BLOCK)) {
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().swungArmAndExpectingAttackPacket = false;
        }
    }
    
    @Override
    public void onCheckableDamageEntity(final CheckableDamageEntityEvent e) {
        if (!this.isActivated()) {
            return;
        }
        if (e.getCheckable().getMeta().getHeuristicValues().flaggedAnyPatternInCurrentCyc) {
            e.getCheckable().getMeta().getTimedValues().lastTimeHitBlockRequest = IIUA.getCurrentTimeMillis();
        }
        this.checkCritsAndCS(e);
        this.checkHurtTime(e);
        this.impossibleInteract(e);
        this.missingAttack(e);
        this.blockSuspiciousHits(e);
        this.accuracy(e);
    }
    
    private void blockSuspiciousHits(final CheckableDamageEntityEvent e) {
        final Player p = e.getBukkitPlayer();
        final Checkable checkable = e.getCheckable();
        final int vl = checkable.getMeta().getHeuristicValues().deltaVL;
        if (this.plugin.getPermissionManager().hasPermission(IntavePermission.BYPASS, (Permissible)p)) {
            return;
        }
        if (!this.plugin.getConfig().getBoolean(this.getConfigPath() + ".block_killaura_attacks")) {
            return;
        }
        final boolean hasOpenInventory = e.getCheckable().getMeta().getSyncedValues().hasAnOpenInventory;
        if (hasOpenInventory) {
            PersistentDebugTelemetry.hitCancel(e.getBukkitPlayer(), (Cancellable)e, "heuristics-heuBlDes-?5");
        }
        final int unsmoothVL = checkable.getMeta().getHeuristicValues().mouseUnsmooth;
        if (unsmoothVL > 12) {
            if (unsmoothVL > 50 && (unsmoothVL < 100 || (ThreadLocalRandom.current().nextBoolean() && ThreadLocalRandom.current().nextBoolean()))) {
                this.debug("56", p, "is rotating suspiciously unsmooth | vl: " + unsmoothVL);
            }
            PersistentDebugTelemetry.hitCancel(p, (Cancellable)e, "heuristics-heuBlDes-?g1");
        }
        if (vl > 8) {
            PersistentDebugTelemetry.hitCancel(p, (Cancellable)e, "heuristics-heuBlDes-?g2");
        }
        if (checkable.getMeta().getHeuristicValues().moreKBKVL > 55) {
            PersistentDebugTelemetry.hitCancel(p, (Cancellable)e, "heuristics-heuBlDes-?g3");
        }
    }
    
    private void missingAttack(final CheckableDamageEntityEvent e) {
        final Player p = e.getBukkitPlayer();
        this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().swungArmAndExpectingAttackPacket = false;
    }
    
    private void accuracy(final CheckableDamageEntityEvent e) {
    }
    
    public void on2(final PlayerAnimationEvent e) {
        final Player player = e.getPlayer();
        final Checkable checkable = this.plugin.catchCheckable(IIUA.getUUIDFrom(player));
        if (checkable.getMeta().getHeuristicValues().awaitingAttack) {
            checkable.getMeta().getHeuristicValues().awaitingAttack = false;
            this.addDynamic(checkable.getMeta().getHeuristicValues().accuracy, -1.0, 40.0);
        }
        checkable.getMeta().getHeuristicValues().awaitingAttack = true;
    }
    
    private void impossibleInteract(final CheckableDamageEntityEvent e) {
    }
    
    private void tooGoodRotation(final CheckableDamageEntityEvent e) {
        double offset = 0.0;
        final Location eLoc = e.getEntity().getLocation().add(0.0, ((LivingEntity)e.getEntity()).getEyeHeight(), 0.0);
        final Location pLoc = e.getBukkitPlayer().getLocation().add(0.0, e.getBukkitPlayer().getEyeHeight(), 0.0);
        final Vector playerRotation = new Vector(pLoc.getYaw(), pLoc.getPitch(), 0.0f);
        final Vector expectedRotation = this.getRotation(pLoc, eLoc);
        final double yaw = this.handle(playerRotation.getX() - expectedRotation.getX());
        final double pitch = this.handle(playerRotation.getY() - expectedRotation.getY());
        final double horizontalDistance = this.getHorizontalDistance(pLoc, eLoc);
        final double dis = this.getDis(pLoc, eLoc);
        final double offsetX = yaw * horizontalDistance * dis;
        final double offsetY = pitch * Math.abs(eLoc.getY() - pLoc.getY()) * dis;
        offset += Math.abs(offsetX);
        offset += Math.abs(offsetY);
        if (offset < 0.6 && horizontalDistance > 1.0 && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeChangedRotation < 200L) {
            e.getCheckable().getPatternManager().setPatternConfidenceFor("18", e.getCheckable().getPatternManager().getPatternConfidenceFor("18") + 4.0);
        }
        else if (e.getCheckable().getPatternManager().getPatternConfidenceFor("18") - 0.4 > 50.0) {
            e.getCheckable().getPatternManager().setPatternConfidenceFor("18", e.getCheckable().getPatternManager().getPatternConfidenceFor("18") - 0.4);
        }
    }
    
    private void checkHurtTime(final CheckableDamageEntityEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        if (e.getCheckable().getMeta().getSyncedValues().lastEntityAttacked == null) {
            return;
        }
        if (e.getCheckable().getMeta().getSyncedValues().lastEntityAttacked.equals(e.getEntity().getUniqueId()) && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeHitCanceled > 1000L) {
            final int maxHurtTime = IIUA.isPretendingToBeOnGround(p) ? 280 : 30;
            final long hurtTime = IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getHeuristicValues().lastTimeHittedOther;
            if (maxHurtTime > hurtTime) {
                PersistentDebugTelemetry.hitCancel(p, (Cancellable)e, "heuristics-heuBlDes-?3");
            }
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getHeuristicValues().lastTimeHittedOther < 100L) {
            if (e.getCheckable().getMeta().getSyncedValues().lastHitted == null) {
                return;
            }
            if (e.getCheckable().getMeta().getSyncedValues().lastHitted.equals(e.getEntity().getUniqueId()) && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeHitCanceled > 1000L) {
                PersistentDebugTelemetry.hitCancel(p, (Cancellable)e, "heuristics-heuBlDes-?4");
            }
        }
        e.getCheckable().getMeta().getHeuristicValues().lastTimeHittedOther = IIUA.getCurrentTimeMillis();
    }
    
    private void checkCritsAndCS(final CheckableDamageEntityEvent e) {
        final Player damager = e.getBukkitPlayer();
        if (!IIUA.isPretendingToBeOnGround(damager) && damager.getLocation().getY() % 1.0 == 0.0 && IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(damager.getUniqueId()).getMeta().getTimedValues().lastTimeTeleported > 1000L && IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(damager.getUniqueId()).getMeta().getTimedValues().lastTimeOnGroundACC < 49L) {
            PersistentDebugTelemetry.hitCancel(damager, (Cancellable)e, "heuristics-heuBlDes-?1");
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeHitCanceled < 3000L) {
            e.getCheckable().getMeta().getHeuristicValues().fastAttackVL = 0;
            return;
        }
        if (e.getCheckable().getMeta().getHeuristicValues().lastFAJump + 350L > IIUA.getCurrentTimeMillis()) {
            final long time = IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getHeuristicValues().lastFADmgA;
            int vl = 0;
            if (time < 50L) {
                PersistentDebugTelemetry.hitCancel(damager, (Cancellable)e, "heuristics-heuBlDes-?2");
                vl = 3;
            }
            else if (time < 150L) {
                vl = 2;
            }
            if (vl > 0) {
                final Checkable.CheckableMeta.HeuristicValues heuristicValues = e.getCheckable().getMeta().getHeuristicValues();
                heuristicValues.fastAttackVL += vl;
            }
        }
        else {
            final long time = IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getHeuristicValues().lastFADmgA;
            if (time < 498L) {
                final Checkable.CheckableMeta.HeuristicValues heuristicValues2 = e.getCheckable().getMeta().getHeuristicValues();
                heuristicValues2.fastAttackVL += 3;
            }
        }
        e.getCheckable().getMeta().getHeuristicValues().lastFADmgA = IIUA.getCurrentTimeMillis();
        this.summoidFAvl(this.plugin.catchCheckable(damager.getUniqueId()));
    }
    
    private void prepareFACheckValues(final CheckableMoveEvent e) {
        if (e.getBukkitPlayer().getFallDistance() > 0.67) {
            e.getCheckable().getMeta().getHeuristicValues().lastFAJump = IIUA.getCurrentTimeMillis();
        }
    }
    
    private void summoidFAvl(final Checkable checkable) {
        if (checkable.getMeta().getHeuristicValues().lastFASummoidClean + 1000L < IIUA.getCurrentTimeMillis() && checkable.getMeta().getHeuristicValues().fastAttackVL > 2) {
            checkable.getMeta().getHeuristicValues().fastAttackVL = 0;
            checkable.getMeta().getHeuristicValues().lastFASummoidClean = IIUA.getCurrentTimeMillis();
        }
    }
    
    @EventHandler
    public void on(final PlayerToggleSprintEvent e) {
        if (!this.isActivated() || e.isCancelled()) {
            return;
        }
        final Checkable checkable = this.plugin.catchCheckable(e.getPlayer().getUniqueId());
        if (IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastSprintToggleListenerRefresh > 1000L || checkable.getMeta().getSyncedValues().sprintToggleCounter > 20) {
            checkable.getMeta().getSyncedValues().sprintToggleCounter = 0;
            checkable.getMeta().getTimedValues().lastSprintToggleListenerRefresh = IIUA.getCurrentTimeMillis();
        }
        final long lastAttack = IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastTimeLeftClickedEntity;
        if (lastAttack < 40L && checkable.getMeta().getHeuristicValues().moreKBKVL < 60) {
            if (checkable.getMeta().getHeuristicValues().moreKBKVL > 40) {
                this.debug("16", e.getPlayer(), "sent suspicious sprint-toggles vl: " + checkable.getMeta().getHeuristicValues().moreKBKVL);
            }
            final Checkable.CheckableMeta.HeuristicValues heuristicValues = checkable.getMeta().getHeuristicValues();
            ++heuristicValues.moreKBKVL;
        }
        else if (checkable.getMeta().getHeuristicValues().moreKBKVL > 1) {
            final Checkable.CheckableMeta.HeuristicValues heuristicValues2 = checkable.getMeta().getHeuristicValues();
            heuristicValues2.moreKBKVL -= 2;
        }
        if (IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastSprintToggle < 5L && lastAttack < 100L && checkable.getMeta().getHeuristicValues().moreKBKVL < 60) {
            final Checkable.CheckableMeta.HeuristicValues heuristicValues3 = checkable.getMeta().getHeuristicValues();
            heuristicValues3.moreKBKVL += 3;
        }
        checkable.getMeta().getTimedValues().lastSprintToggle = System.currentTimeMillis();
        final Checkable.CheckableMeta.SyncedValues syncedValues = checkable.getMeta().getSyncedValues();
        ++syncedValues.sprintToggleCounter;
    }
    
    private void setupChickenProtocol() {
        ProtocolLibAdapter.getProtocolManager().addPacketListener((PacketListener)new PacketAdapter(this.plugin, new PacketType[] { PacketType.Play.Client.USE_ENTITY }) {
            public void onPacketReceiving(final PacketEvent e) {
                final Player p = e.getPlayer();
                final int entityId = e.getPacket().getIntegers().getValues().get(0);
                if (Heuristics.this.entityBlocks.containsKey(p.getUniqueId()) && Heuristics.this.canSeeEntitys.containsKey(p.getUniqueId())) {
                    for (int i = 0; i < ((Object[])Heuristics.this.entityBlocks.get(p.getUniqueId())).length; ++i) {
                        if ((int)Heuristics.this.getMethodValue(((Object[])Heuristics.this.entityBlocks.get(p.getUniqueId()))[i], "getId") == entityId) {}
                    }
                }
            }
        });
    }
    
    private void prepareChicken(final CheckableDamageEntityEvent e) {
        if (e.getEntity() instanceof Player) {
            final Player p = e.getBukkitPlayer();
            this.observing.put(p.getUniqueId(), e.getEntity().getUniqueId());
            this.lastEntitySpawn.put(p.getUniqueId(), IIUA.getCurrentTimeMillis());
            if (!this.isAlive(p.getUniqueId())) {
                this.setAlive(p.getUniqueId(), true, e.getEntity().getLocation());
            }
        }
    }
    
    private void chickenUpdate(final CheckableMoveEvent e) {
        final Player p = e.getBukkitPlayer();
        final Player targetPlayer;
        final Player p2;
        final Object[] entities;
        final double xzmargin;
        final double yMargin;
        this.observing.keySet().stream().filter(uuid -> this.observing.get(uuid).equals(p.getUniqueId())).filter((Predicate<? super Object>)this::isAlive).forEach(uuid -> {
            targetPlayer = Bukkit.getPlayer(uuid);
            entities = this.getOrCreateAndSpawnEntities(p2, e.getTo());
            xzmargin = 0.25;
            yMargin = 0.4;
            this.entityTeleport(targetPlayer, entities[0], e.getTo().clone().add(0.0, 0.0, 0.0));
            this.entityTeleport(targetPlayer, entities[1], e.getTo().clone().add(0.0, 0.4, 0.0));
            return;
        });
        final double lastSpawn = (double)(IIUA.getCurrentTimeMillis() - this.lastEntitySpawn.getOrDefault(p.getUniqueId(), IIUA.getCurrentTimeMillis()));
        if (lastSpawn > 500.0 && this.isAlive(p.getUniqueId())) {
            this.setAlive(p.getUniqueId(), false, e.getTo());
        }
    }
    
    private void setAlive(final UUID uuid, final boolean alive, final Location loc) {
        final Player p = Bukkit.getPlayer(uuid);
        if (alive && !this.isAlive(uuid)) {
            this.performForAllEntitiesOf(p, loc, o -> this.sendEntitySpawn(p, o, loc));
        }
        else if (!alive && this.isAlive(uuid)) {
            this.performForAllEntitiesOf(p, loc, o -> this.sendEntityKill(p, o));
        }
        this.canSeeEntitys.put(uuid, alive);
    }
    
    private void performForAllEntitiesOf(final Player p, final Location loc, final Consumer<? super Object> objectConsumer) {
        Arrays.stream(this.getOrCreateAndSpawnEntities(p, loc)).forEach(objectConsumer);
    }
    
    private Object[] getOrCreateAndSpawnEntities(final Player p, final Location loc) {
        if (!this.hasEntities(p.getUniqueId())) {
            try {
                final int entityAmount = 2;
                final Object[] entities = new Object[2];
                for (int i = 0; i < 2; ++i) {
                    entities[i] = this.createNMSEntity("~", this.getWorldServerUsing(loc), "EntitySlime");
                    entities[i].getClass().getMethod("setSize", Integer.TYPE).invoke(entities[i], 2);
                }
                this.entityBlocks.put(p.getUniqueId(), entities);
            }
            catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException ex2) {
                final ReflectiveOperationException ex;
                final ReflectiveOperationException e = ex;
                e.printStackTrace();
            }
        }
        return this.entityBlocks.get(p.getUniqueId());
    }
    
    private boolean isAlive(final UUID uuid) {
        return this.canSeeEntitys.getOrDefault(uuid, false);
    }
    
    private boolean hasEntities(final UUID uuid) {
        return this.entityBlocks.containsKey(uuid);
    }
    
    private void sendEntitySpawn(final Player receiver, final Object nmsEntity, final Location destination) {
        try {
            nmsEntity.getClass().getMethod("setLocation", Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE).invoke(nmsEntity, destination.getX(), destination.getY(), destination.getZ(), destination.getYaw(), destination.getPitch());
            final Object packetPlayOutSpawnEntityLiving = Reflections.getNmsClass("PacketPlayOutSpawnEntityLiving").getConstructor(Reflections.getNmsClass("EntityLiving")).newInstance(nmsEntity);
            Reflections.sendPacket(receiver, packetPlayOutSpawnEntityLiving);
        }
        catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException ex2) {
            final ReflectiveOperationException ex;
            final ReflectiveOperationException e = ex;
            final List<String> header = new ArrayList<String>();
            header.add("[Intave] [BAD EXCEPTION] It looks like intave had problems creating \"entity spawn\" packets.");
            IntaveExceptionHandler.printAndSaveToFile(header, e);
        }
    }
    
    private void entityTeleport(final Player receiver, final Object nmsEntity, final Location loc) {
        try {
            nmsEntity.getClass().getMethod("setLocation", Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE).invoke(nmsEntity, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
            final Object emptyTeleportPacket = Reflections.getNmsClass("PacketPlayOutEntityTeleport").getConstructor(Reflections.getNmsClass("Entity")).newInstance(nmsEntity);
            Reflections.sendPacket(receiver, emptyTeleportPacket);
        }
        catch (IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException ex2) {
            final ReflectiveOperationException ex;
            final ReflectiveOperationException e = ex;
            final List<String> header = new CopyOnWriteArrayList<String>();
            header.add("[Intave] [BAD EXCEPTION] It looks like intave had problems creating teleport packets.");
            IntaveExceptionHandler.printAndSaveToFile(header, e);
        }
        catch (Exception e2) {
            e2.printStackTrace();
            System.out.println(nmsEntity.getClass().getSimpleName());
        }
    }
    
    private void sendEntityKill(final Player receiver, final Object nmsEntity) {
        final int id = (int)this.getMethodValue(nmsEntity, "getId");
        final PacketContainer packetContainer = ProtocolLibAdapter.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        packetContainer.getIntegerArrays().write(0, (Object)new int[] { id });
        try {
            ProtocolLibAdapter.getProtocolManager().sendServerPacket(receiver, packetContainer);
        }
        catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    
    private Object getMethodValue(final Object object, final String method_name) {
        try {
            return object.getClass().getMethod(method_name, (Class<?>[])new Class[0]).invoke(object, new Object[0]);
        }
        catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex2) {
            final ReflectiveOperationException ex;
            final ReflectiveOperationException e = ex;
            throw new IntaveInternalException("Intave has problems fetching the method $" + method_name + " on " + object.getClass().getSimpleName() + "", e);
        }
    }
    
    private Object createNMSEntity(final String name, final Object nmsWorldServer, final String entityNMSName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        final Object nmsEntity = Reflections.getNmsClass(entityNMSName).getConstructor(Reflections.getNmsClass("World")).newInstance(nmsWorldServer);
        nmsEntity.getClass().getMethod("setInvisible", Boolean.TYPE).invoke(nmsEntity, true);
        nmsEntity.getClass().getMethod("setCustomName", String.class).invoke(nmsEntity, name);
        return nmsEntity;
    }
    
    private Object getWorldServerUsing(final Location l) {
        return this.getMethodValue(l.getWorld(), "getHandle");
    }
    
    public void debug(final String pattern, final Player player, final String info) {
        final Checkable checkable = this.plugin.catchCheckable(player.getUniqueId());
        final String playerName = player.getName();
        final String patternInfo = "(p[" + pattern + "] / " + checkable.getPatternManager().getPatternConfidenceFor(pattern) + ")";
        final String messsage = ChatColor.RED + "[Heuristics] [Debug] " + playerName + " on pattern " + patternInfo + " | " + info;
        PlayerUtils.sendMessage(UUID.fromString("5ee6db6d-6751-4081-9cbf-28eb0f6cc055"), messsage);
        PlayerUtils.sendMessage(UUID.fromString("a0f3f956-1040-410d-9f04-bb566054f148"), messsage);
    }
    
    private void addVLToPattern(final Checkable checkable, final String pattern, final double toAdd) {
        checkable.getPatternManager().setPatternConfidenceFor(pattern, checkable.getPatternManager().getPatternConfidenceFor(pattern) + toAdd);
    }
    
    private void removeVLFromPattern(final Checkable checkable, final String pattern, final double toRemove) {
        checkable.getPatternManager().setPatternConfidenceFor(pattern, checkable.getPatternManager().getPatternConfidenceFor(pattern) - toRemove);
    }
    
    private boolean isOppositeSide(final float dir, final float dir2) {
        return (dir < 0.0f && dir2 > 0.0f) || (dir > 0.0f && dir2 < 0.0f);
    }
    
    private Vector getRotation(final Location loc1, final Location loc2) {
        final double dX = loc2.getX() - loc1.getX();
        final double dY = loc2.getY() - loc1.getY();
        final double dZ = loc2.getZ() - loc1.getZ();
        final double disXZ = Math.sqrt(dX * dX + dZ * dZ);
        final float yaw = (float)(Math.atan2(dZ, dX) * 180.0 / 3.141592653589793) - 90.0f;
        final float pitch = (float)(-(Math.atan2(dY, disXZ) * 180.0 / 3.141592653589793));
        return new Vector(yaw, pitch, 0.0f);
    }
    
    private double handle(double degrees) {
        degrees %= 360.0;
        if (degrees >= 180.0) {
            degrees -= 360.0;
        }
        if (degrees < -180.0) {
            degrees += 360.0;
        }
        return degrees;
    }
    
    private double getHorizontalDistance(final Location loc1, final Location loc2) {
        return Math.abs(Math.sqrt((loc2.getX() - loc1.getX()) * (loc2.getX() - loc1.getX()) + (loc2.getZ() - loc1.getZ()) * (loc2.getZ() - loc1.getZ())));
    }
    
    private double getDis(final Location loc1, final Location loc2) {
        return Math.abs(Math.sqrt((loc2.getX() - loc1.getX()) * (loc2.getX() - loc1.getX()) + (loc2.getY() - loc1.getY()) * (loc2.getY() - loc1.getY()) + (loc2.getZ() - loc1.getZ()) * (loc2.getZ() - loc1.getZ())));
    }
    
    private double getMinConfidenceToFlag() {
        return 81.0;
    }
    
    private boolean aggressiveModeActivated() {
        if (this.aggressive == null) {
            this.aggressive = this.plugin.getConfig().getBoolean(this.getConfigPath() + ".aggressive");
        }
        return this.aggressive;
    }
    
    private double getFrac(final double dis) {
        return dis % 1.0;
    }
    
    private float handleX(float degrees) {
        if (degrees >= 180.0) {
            degrees -= 360.0;
        }
        if (degrees < -180.0) {
            degrees += 360.0;
        }
        return degrees;
    }
    
    public static float sin(final float p_76126_0_, final boolean fastMath) {
        return fastMath ? Heuristics.SIN_TABLE_FAST[(int)(p_76126_0_ * 651.8986f) & 0xFFF] : Heuristics.SIN_TABLE[(int)(p_76126_0_ * 10430.378f) & 0xFFFF];
    }
    
    public static float cos(final float p_76134_0_, final boolean fastMath) {
        return fastMath ? Heuristics.SIN_TABLE_FAST[(int)((p_76134_0_ + 1.5707964f) * 651.8986f) & 0xFFF] : Heuristics.SIN_TABLE[(int)(p_76134_0_ * 10430.378f + 16384.0f) & 0xFFFF];
    }
    
    public static float sqrt_float(final float p_76129_0_) {
        return (float)Math.sqrt(p_76129_0_);
    }
    
    public static float sqrt_double(final double p_76133_0_) {
        return (float)Math.sqrt(p_76133_0_);
    }
    
    @Override
    public void setActivated(final boolean active) {
        super.setActivated(active);
        this.plugin.getServer().getOnlinePlayers().forEach(o -> this.plugin.catchCheckable(IIUA.getUUIDFrom(o)).getMeta().resetAllHeuristicalData());
    }
    
    @Override
    public void onSystemShutdown() {
    }
    
    static {
        SIN_TABLE_FAST = new float[4096];
        SIN_TABLE = new float[65536];
        IntStream.range(0, 65536).forEachOrdered(i -> Heuristics.SIN_TABLE[i] = (float)Math.sin(i * 3.141592653589793 * 2.0 / 65536.0));
        IntStream.range(0, 4096).forEachOrdered(i -> Heuristics.SIN_TABLE_FAST[i] = (float)Math.sin((i + 0.5f) / 4096.0f * 6.2831855f));
        for (int j = 0; j < 360; j += 90) {
            Heuristics.SIN_TABLE_FAST[(int)(j * 11.377778f) & 0xFFF] = (float)Math.sin(j * 0.017453292f);
        }
    }
}
