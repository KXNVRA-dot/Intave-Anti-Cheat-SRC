// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.connection;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTeleportEvent;
import de.jpx3.intave.util.event.CheckableMoveEvent;
import org.bukkit.util.Vector;
import org.bukkit.block.Block;
import de.jpx3.intave.util.calc.YawUtil;
import org.bukkit.GameMode;
import org.bukkit.event.player.PlayerMoveEvent;
import de.jpx3.intave.util.calc.PlayerUtils;
import de.jpx3.intave.module.PersistentDebugTelemetry;
import org.bukkit.Location;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.entity.Entity;
import de.jpx3.intave.util.calc.LocationHelper;
import java.util.stream.IntStream;
import org.bukkit.permissions.Permissible;
import de.jpx3.intave.api.external.IntavePermission;
import com.comphenix.protocol.events.PacketListener;
import de.jpx3.intave.util.objectable.Checkable;
import org.bukkit.entity.Player;
import de.jpx3.intave.util.calc.BalanceUtils;
import de.jpx3.intave.util.calc.MathHelper;
import de.jpx3.intave.antipiracy.IIUA;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.PacketType;
import de.jpx3.intave.api.internal.ProtocolLibAdapter;
import org.bukkit.plugin.Plugin;
import de.jpx3.intave.util.enums.CheatCategory;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.event.Listener;
import de.jpx3.intave.util.objectable.IntaveCheck;

public final class MoveCheck extends IntaveCheck implements Listener
{
    private final IntavePlugin plugin;
    private final double MAXIMUM_PACKET_BALANCE;
    private final double MAXIMUM_LAG_LATENCY;
    public static boolean PHASE_VL_ADD;
    private final boolean STRICT_BUFFEROVERFLOW_BLOCK;
    public static boolean ONLY_SYNC_TP;
    
    public MoveCheck(final IntavePlugin plugin) {
        super("Move", CheatCategory.NETWORK);
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this.plugin);
        final IntavePlugin plugin2 = plugin;
        final IntavePlugin plugin3 = plugin;
        this.MAXIMUM_PACKET_BALANCE = 5.0;
        MoveCheck.PHASE_VL_ADD = plugin.getConfig().getBoolean(this.getConfigPath() + ".add_phase_vl");
        this.STRICT_BUFFEROVERFLOW_BLOCK = plugin.getConfig().getBoolean(this.getConfigPath() + ".strict_bufferoverflow_block");
        this.MAXIMUM_LAG_LATENCY = Math.max(2.0, plugin.getConfig().getDouble(this.getConfigPath() + ".lag_latency"));
        ProtocolLibAdapter.getProtocolManager().addPacketListener((PacketListener)new PacketAdapter(this.plugin, new PacketType[] { PacketType.Play.Client.KEEP_ALIVE, PacketType.Play.Server.KEEP_ALIVE }) {
            public void onPacketReceiving(final PacketEvent event) {
                final Player p = event.getPlayer();
                final Checkable checkable = plugin3.catchCheckable(IIUA.getUUIDFrom(p));
                final int ping = (int)(IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastTimeKeepAlivePacketSent);
                final int lastPing = checkable.getMeta().getSyncedValues().lastPing;
                final int size = 8;
                if (checkable.getMeta().getBalanceValues().pingDifferenceBalance.size() >= size) {
                    checkable.getMeta().getBalanceValues().pingDifferenceBalance.remove(0);
                }
                checkable.getMeta().getBalanceValues().pingDifferenceBalance.add((int)MathHelper.diff((float)lastPing, (float)ping));
                if (checkable.getMeta().getBalanceValues().pingDifferenceBalance.size() >= size) {
                    final double balance = BalanceUtils.getSquaredBalanceFrom(checkable.getMeta().getBalanceValues().pingDifferenceBalance);
                    if (ping >= 500 && ping < 1300 && balance < 100.0) {
                        if (checkable.getMeta().getVioValues().pingSpoofVL > 12) {
                            plugin2.getRetributionManager().markPlayer(p, 25, "Move", CheatCategory.NETWORK, "is trying to spoof his ping (callback " + ping + " seems unlegit)");
                        }
                        final Checkable.CheckableMeta.ViolationValues vioValues = checkable.getMeta().getVioValues();
                        ++vioValues.pingSpoofVL;
                    }
                    else if (checkable.getMeta().getVioValues().pingSpoofVL > 0) {
                        final Checkable.CheckableMeta.ViolationValues vioValues2 = checkable.getMeta().getVioValues();
                        --vioValues2.pingSpoofVL;
                    }
                }
                final long lastPositionPacket = IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastTimePositionPacketSent;
                if (lastPositionPacket > 3000L) {}
                checkable.getMeta().getSyncedValues().lastPing = ping;
            }
            
            public void onPacketSending(final PacketEvent event) {
                final Player player = event.getPlayer();
                plugin3.catchCheckable(player.getUniqueId()).getMeta().getTimedValues().lastTimeKeepAlivePacketSent = IIUA.getCurrentTimeMillis();
            }
        });
        ProtocolLibAdapter.getProtocolManager().addPacketListener((PacketListener)new PacketAdapter(IntavePlugin.getStaticReference(), new PacketType[] { PacketType.Play.Client.FLYING }) {
            public void onPacketReceiving(final PacketEvent event) {
                final Player p = event.getPlayer();
                plugin3.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeFlyPacketSent = IIUA.getCurrentTimeMillis();
            }
        });
        ProtocolLibAdapter.getProtocolManager().addPacketListener((PacketListener)new PacketAdapter(IntavePlugin.getStaticReference(), new PacketType[] { PacketType.Play.Client.KEEP_ALIVE }) {
            public void onPacketReceiving(final PacketEvent event) {
                final Player p = event.getPlayer();
                final Checkable checkable = IntavePlugin.getStaticReference().catchCheckable(IIUA.getUUIDFrom(p));
                final long lastPositionPacket2 = IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastTimePositionPacketSent;
                final long lastFlyingPacket2 = IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastTimeFlyPacketSent;
                if (lastPositionPacket2 > 1200L && lastFlyingPacket2 > 700L && !p.isDead()) {
                    checkable.getMeta().getVioValues().isInDeadQueueMode = true;
                }
            }
        });
        final PacketType[] types = { PacketType.Play.Client.POSITION, PacketType.Play.Client.POSITION_LOOK, PacketType.Play.Client.LOOK, PacketType.Play.Client.FLYING };
        ProtocolLibAdapter.getProtocolManager().addPacketListener((PacketListener)new PacketAdapter(plugin, types) {
            public void onPacketReceiving(final PacketEvent e) {
                if (!MoveCheck.this.isActivated()) {
                    return;
                }
                final Player p = e.getPlayer();
                if (!plugin2.isLinkedToIntave(IIUA.getUUIDFrom(p))) {
                    return;
                }
                final Checkable checkable = plugin2.catchCheckable(IIUA.getUUIDFrom(p));
                synchronized (checkable.getMeta().getVioValues()) {
                    if (checkable.getMeta().getVioValues().isInDeadQueueMode) {
                        checkable.getMeta().getVioValues().isInDeadQueueMode = false;
                        return;
                    }
                }
                final int lencyPush = 1;
                final int packets = ++checkable.getMeta().getSyncedValues().packetCounter * 1;
                final long lastReset = IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastMovePacketsCounterResett + 0L;
                final int sizze = 10;
                if (lastReset > 500L) {
                    boolean flagX = false;
                    final double maxPackets = MoveCheck.this.MAXIMUM_LAG_LATENCY / 2.0 * 20.0;
                    if (packets > maxPackets) {
                        if (plugin3.getRetributionManager().markPlayer(p, Math.min(packets / 2, 50), "Move", CheatCategory.NETWORK, "is sending too many packets (" + (packets - maxPackets) + " more than " + maxPackets + ")")) {
                            MoveCheck.this.teleportAsync(p, checkable.getMeta().getLocationValues().lastLocationSynced);
                        }
                        flagX = true;
                    }
                    final double balance = packets / 500.0;
                    checkable.getMeta().getBalanceValues().movementBalance.add(packets);
                    if (checkable.getMeta().getBalanceValues().movementBalance.size() > 10) {
                        checkable.getMeta().getBalanceValues().movementBalance.remove(0);
                        final double overallBalance = BalanceUtils.getSquaredBalanceFrom(checkable.getMeta().getBalanceValues().movementBalance);
                        if (overallBalance > 12.0 && balance >= 0.025) {
                            final Checkable.CheckableMeta.ViolationValues vioValues = checkable.getMeta().getVioValues();
                            final int min = Math.min(30, checkable.getMeta().getVioValues().moveBalanceVL + 1);
                            vioValues.moveBalanceVL = min;
                            final int vl = min;
                            final boolean overCharge = overallBalance > 14.0 || balance > 0.045;
                            if (IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastMoveEvent < 5000L && (vl > 6 || overCharge)) {
                                final boolean flag = plugin3.getRetributionManager().markPlayer(p, 5, "Move", CheatCategory.NETWORK, "is sending too many packets (balance " + MathHelper.roundFromDouble(overallBalance - 7.0, 4) + " / tflop " + MathHelper.roundFromDouble(balance * 100.0, 3) + ")");
                                if (flag || (overCharge && !plugin3.getPermissionManager().hasPermission(IntavePermission.BYPASS, (Permissible)p) && MoveCheck.this.STRICT_BUFFEROVERFLOW_BLOCK)) {
                                    final Location location = checkable.getMeta().getLocationValues().lastLocationSynced;
                                    MoveCheck.this.teleportAsync(p, location);
                                    flagX = true;
                                }
                            }
                        }
                        else if (checkable.getMeta().getVioValues().moveBalanceVL > 0) {
                            final Checkable.CheckableMeta.ViolationValues vioValues2 = checkable.getMeta().getVioValues();
                            --vioValues2.moveBalanceVL;
                        }
                    }
                    else {
                        IntStream.range(0, 10).forEach(value -> checkable.getMeta().getBalanceValues().movementBalance.add(value, 10));
                    }
                    if (!flagX && IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastTimeTeleported > 2000L) {
                        checkable.getMeta().getLocationValues().lastLocationSynced = checkable.getVerifiedLocation();
                    }
                    checkable.getMeta().getTimedValues().lastMovePacketsCounterResett = IIUA.getCurrentTimeMillis();
                    checkable.getMeta().getSyncedValues().packetCounter = 0;
                }
                final boolean lastTeleported = IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastTimeTeleported < 100L;
                final long lastPositionPacket = IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastTimePositionPacketSent;
                final int size = 80;
                checkable.getMeta().getBalanceValues().lastPPacketBalance.add(Math.max(lastTeleported ? 50L : lastPositionPacket, 40L));
                if (checkable.getMeta().getBalanceValues().lastPPacketBalance.size() > 80) {
                    checkable.getMeta().getBalanceValues().lastPPacketBalance.remove(0);
                }
                else {
                    IntStream.range(0, 79).forEach(value -> checkable.getMeta().getBalanceValues().lastPPacketBalance.add(50L));
                }
                if (lastPositionPacket >= 0L && lastPositionPacket < 100L) {
                    int vl2 = 30;
                    if (checkable.getMeta().getVioValues().closePacketVL < 30) {
                        vl2 = checkable.getMeta().getVioValues().closePacketVL++;
                    }
                    final double overallBalance = BalanceUtils.getSquaredBalanceFromLong(checkable.getMeta().getBalanceValues().lastPPacketBalance);
                    final double localBalance = BalanceUtils.getSquaredBalanceFromLong(checkable.getMeta().getBalanceValues().lastPPacketBalance.subList(69, 79));
                    final boolean wasRecentlyTeleported = IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastTimeTeleported < 400L;
                    boolean quickPacket = false;
                    if (lastPositionPacket < 1L && IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastTimeTeleported > 500L) {
                        final long priorPositionPacket = checkable.getMeta().getTimedValues().lastPositionPacketDiff;
                        boolean bad = false;
                        if (priorPositionPacket <= 80L && priorPositionPacket > 5L && overallBalance < 55.0 && localBalance < 55.0) {
                            bad = (quickPacket = true);
                        }
                    }
                    if (quickPacket || (MathHelper.diff((double)lastPositionPacket, localBalance) < 2.0 && 45.0 - (overallBalance - 15.0) > 10.0 && checkable.getMeta().getSyncedValues().lastMPDiffBalaceDiff > 20.0)) {
                        Location stbck = checkable.getMeta().getLocationValues().lastLocationSynced.clone();
                        final double dist = LocationHelper.getDistanceSafe(checkable.getVerifiedLocation(), stbck);
                        if (dist > 100.0) {
                            return;
                        }
                        if (dist > 2.0) {
                            stbck = checkable.getMeta().getLocationValues().lastLocationSynced.clone();
                        }
                        if (plugin3.getRetributionManager().markPlayer(p, 1, "Move", CheatCategory.NETWORK, "is sending too many packets (balance " + MathHelper.roundFromDouble(MathHelper.minmax(45.0 - (overallBalance - 15.0), 10.0, 15.0), 3) + ")")) {
                            stbck.setYaw(checkable.getVerifiedLocation().getYaw());
                            stbck.setPitch(checkable.getVerifiedLocation().getPitch());
                            if (p.getFallDistance() > 2.0f) {
                                checkable.getMeta().getVioValues().flagNextTimeMove = true;
                                checkable.getMeta().getLocationValues().lastLocationOnGroundACC = checkable.getVerifiedLocation();
                            }
                            else if (!LocationHelper.isInsideUnpassable(p.getWorld(), (Entity)p, stbck) && IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastTimeTeleportedLongDistance > 100L) {
                                MoveCheck.this.teleportAsync(p, stbck);
                            }
                        }
                    }
                    checkable.getMeta().getSyncedValues().lastMPDiffBalaceDiff = MathHelper.diff((double)lastPositionPacket, localBalance);
                    if (localBalance < 48.8 && checkable.getPing() > 0 && overallBalance < 58.0 && !wasRecentlyTeleported) {
                        Location stbck = checkable.getMeta().getLocationValues().lastLocationSynced.clone();
                        final double dist = LocationHelper.getDistanceSafe(checkable.getVerifiedLocation(), stbck);
                        if (dist > 100.0) {
                            return;
                        }
                        if (dist > 2.0) {
                            stbck = checkable.getMeta().getLocationValues().lastLocationSynced.clone();
                        }
                        boolean flag2 = overallBalance < 50.0 && checkable.getMeta().getVioValues().suspiciousOvermainBalanceMoves > 2;
                        final Checkable.CheckableMeta.ViolationValues vioValues3 = checkable.getMeta().getVioValues();
                        ++vioValues3.suspiciousOvermainBalanceMoves;
                        if (overallBalance >= 50.0) {
                            if (checkable.getMeta().getVioValues().suspiciousOvermainBalanceMoves > 80) {
                                flag2 = true;
                            }
                            if (checkable.getMeta().getVioValues().suspiciousOvermainBalanceMoves < 100) {
                                final Checkable.CheckableMeta.ViolationValues vioValues4 = checkable.getMeta().getVioValues();
                                vioValues4.suspiciousOvermainBalanceMoves += (int)(((localBalance < 47.0) ? 10 : 5) * ((overallBalance >= 55.0) ? 0.5 : 1.0));
                            }
                        }
                        else if (checkable.getMeta().getVioValues().suspiciousOvermainBalanceMoves > 5) {
                            final Checkable.CheckableMeta.ViolationValues vioValues5 = checkable.getMeta().getVioValues();
                            vioValues5.suspiciousOvermainBalanceMoves -= 5;
                        }
                        if (flag2 && plugin3.getRetributionManager().markPlayer(p, 1, "Move", CheatCategory.NETWORK, "is sending too many packets (balance " + MathHelper.roundFromDouble(MathHelper.minmax(45.0 - (overallBalance - 15.0), 10.0, 15.0), 3) + ")")) {
                            stbck.setYaw(checkable.getVerifiedLocation().getYaw());
                            stbck.setPitch(checkable.getVerifiedLocation().getPitch());
                            if (p.getFallDistance() > 2.0f) {
                                checkable.getMeta().getVioValues().flagNextTimeMove = true;
                                checkable.getMeta().getLocationValues().lastLocationOnGroundACC = checkable.getVerifiedLocation();
                            }
                            else if ((ThreadLocalRandom.current().nextBoolean() || ThreadLocalRandom.current().nextBoolean()) && !LocationHelper.isInsideUnpassable(p.getWorld(), (Entity)p, stbck)) {
                                MoveCheck.this.teleportAsync(p, stbck);
                            }
                        }
                    }
                    else if (checkable.getMeta().getVioValues().suspiciousOvermainBalanceMoves > 0) {
                        final Checkable.CheckableMeta.ViolationValues vioValues6 = checkable.getMeta().getVioValues();
                        --vioValues6.suspiciousOvermainBalanceMoves;
                    }
                }
                else if (checkable.getMeta().getVioValues().closePacketVL > 1) {
                    final Checkable.CheckableMeta.ViolationValues vioValues7 = checkable.getMeta().getVioValues();
                    vioValues7.closePacketVL -= 2;
                }
                checkable.getMeta().getTimedValues().lastPositionPacketDiff = lastPositionPacket;
                checkable.getMeta().getTimedValues().lastTimePositionPacketSent = IIUA.getCurrentTimeMillis();
                if (plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().packetCounter > 100) {
                    MoveCheck.this.teleportAsync(p, checkable.getMeta().getLocationValues().lastLocationSynced.clone());
                }
            }
        });
    }
    
    private void teleportAsync(final Player p, final Location locationTo) {
        this.teleportAsync(p, locationTo, "unknown");
    }
    
    public void teleportAsync(final Player p, final Location locationTo, final String reason) {
        if (MoveCheck.ONLY_SYNC_TP || IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).setupTime < 5000L) {
            PlayerUtils.performSyc(p, player -> {
                PersistentDebugTelemetry.teleport((Entity)player, locationTo, "move-sync-" + reason);
                this.plugin.catchCheckable(IIUA.getUUIDFrom(player)).fallDistanceRenew(0.3);
            });
        }
        else {
            PersistentDebugTelemetry.teleport((Entity)p, locationTo, "move-async-" + reason);
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).fallDistanceRenew(0.3);
        }
    }
    
    public boolean checkEvent(final PlayerMoveEvent e) {
        if (!this.isActivated()) {
            return true;
        }
        final Player p = e.getPlayer();
        final boolean onVehicle = p.isInsideVehicle();
        final Location locationFrom = e.getFrom();
        final double distance = locationFrom.toVector().distance(e.getTo().toVector());
        if (distance > 8.0) {
            final String x1 = MathHelper.roundFromDouble(locationFrom.getX(), 5);
            final String y1 = MathHelper.roundFromDouble(locationFrom.getY(), 5);
            final String z1 = MathHelper.roundFromDouble(locationFrom.getZ(), 5);
            final String x2 = MathHelper.roundFromDouble(e.getTo().getX(), 5);
            final String y2 = MathHelper.roundFromDouble(e.getTo().getY(), 5);
            final String z2 = MathHelper.roundFromDouble(e.getTo().getZ(), 5);
            this.plugin.getRetributionManager().markPlayer(p, (int)Math.min(Math.max(10.0, distance * 5.0), 100.0), "Move", CheatCategory.MOVING, "moved too expeditiously. (" + x1 + "," + y1 + "," + z1 + " -> " + x2 + "," + y2 + "" + z2 + ") over a distance of " + MathHelper.roundFromDouble(distance, 5) + " blocks.");
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).removePacketsFromBalanceCalc(1);
            return false;
        }
        final boolean insideBlocks = LocationHelper.isInsideUnpassable(e.getPlayer().getWorld(), (Entity)e.getPlayer(), e.getTo()) && !LocationHelper.isInsideUnpassable(e.getPlayer().getWorld(), (Entity)e.getPlayer(), e.getFrom());
        if (!onVehicle && !p.getGameMode().equals((Object)GameMode.SPECTATOR) && !e.getFrom().getBlock().equals(e.getTo().getBlock()) && insideBlocks && (this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeTeleported >= 1000L || this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeFlagged <= 1000L)) {
            final String xDist = MathHelper.roundFromDouble(e.getFrom().getX() - e.getTo().getX(), 5) + "";
            final String yDist = MathHelper.roundFromDouble(e.getFrom().getY() - e.getTo().getY(), 5) + "";
            final String zDist = MathHelper.roundFromDouble(e.getFrom().getZ() - e.getTo().getZ(), 5) + "";
            final int vl = MoveCheck.PHASE_VL_ADD ? 1 : 0;
            this.plugin.getRetributionManager().markPlayer(e.getPlayer(), vl, "Move", CheatCategory.MOVING, "tried to move into unpassable block (" + xDist + ", " + yDist + ", " + zDist + ")");
            this.plugin.catchCheckable(e.getPlayer().getUniqueId()).removePacketsFromBalanceCalc(1);
            final Block b = LocationHelper.getUnpassableIfThere(e.getPlayer().getLocation().getWorld(), (Entity)e.getPlayer(), e.getTo());
            final Vector push = e.getTo().clone().subtract(b.getLocation().clone().add(0.5, 0.5, 0.5)).toVector().multiply(0.2);
            if (push.length() < 1.0) {
                if (e.getPlayer().getLocation().getY() > b.getY()) {
                    push.setX(0.0);
                    push.setZ(0.0);
                }
                e.getPlayer().setVelocity(push);
            }
            return false;
        }
        if (YawUtil.yawDiff(e.getFrom().getYaw(), e.getTo().getYaw()) >= 180.0f || YawUtil.pitchDiff(e.getFrom().getPitch(), e.getTo().getPitch()) >= 89.9 || YawUtil.pitchDiff(e.getFrom().getPitch(), e.getTo().getPitch()) <= -89.9) {
            this.plugin.getRetributionManager().markPlayer(e.getPlayer(), 1, "Move", CheatCategory.MOVING, "tried to rotate suspiously");
            return false;
        }
        return true;
    }
    
    @Override
    public void onCheckableMove(final CheckableMoveEvent e) {
        if (e.getCheckable().getMeta().getVioValues().flagNextTimeMove) {
            final double expected = (e.getCheckable().getMeta().getSyncedValues().lastYmovement - 0.08) * 0.98;
            e.getMoveFlagData().setPullSilent(true);
            e.getMoveFlagData().setPullYMotion(expected);
            e.getCheckable().getMeta().getVioValues().flagNextTimeMove = false;
        }
    }
    
    @EventHandler
    public void on(final PlayerTeleportEvent e) {
        if (this.plugin.isLinkedToIntave(e.getPlayer().getUniqueId())) {
            final Player p = e.getPlayer();
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getLocationValues().locationTick4 = e.getTo();
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getLocationValues().lastVelocity = e.getTo();
            if (e.getCause().equals((Object)PlayerTeleportEvent.TeleportCause.PLUGIN)) {
                this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getLocationValues().lastLocationSynced = e.getTo();
            }
        }
    }
    
    static {
        MoveCheck.PHASE_VL_ADD = false;
        MoveCheck.ONLY_SYNC_TP = false;
    }
}
