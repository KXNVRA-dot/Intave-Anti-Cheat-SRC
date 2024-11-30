// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.movement;

import de.jpx3.intave.util.calc.MathHelper;
import org.bukkit.block.BlockFace;
import org.bukkit.Material;
import com.comphenix.protocol.utility.MinecraftVersion;
import de.jpx3.intave.util.event.CheckableMoveEvent;
import com.comphenix.protocol.events.PacketListener;
import de.jpx3.intave.util.objectable.Checkable;
import java.util.List;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import de.jpx3.intave.util.calc.PlayerUtils;
import de.jpx3.intave.util.calc.LocationHelper;
import de.jpx3.intave.antipiracy.IIUA;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.plugin.Plugin;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.PacketType;
import de.jpx3.intave.api.internal.ProtocolLibAdapter;
import de.jpx3.intave.util.enums.CheatCategory;
import org.bukkit.Location;
import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.util.objectable.IntaveCheck;

public final class NoFallCheck extends IntaveCheck
{
    private final IntavePlugin plugin;
    private Location location;
    
    public NoFallCheck(final IntavePlugin plugin) {
        super("NoFall", CheatCategory.MOVING);
        this.plugin = plugin;
        final IntavePlugin plugin2 = plugin;
        ProtocolLibAdapter.getProtocolManager().addPacketListener((PacketListener)new PacketAdapter(plugin, new PacketType[] { PacketType.Play.Client.POSITION, PacketType.Play.Client.POSITION_LOOK }) {
            public void onPacketReceiving(final PacketEvent event) {
                final Player p = event.getPlayer();
                final PacketContainer packetContainer = event.getPacket();
                final boolean claimsOnGround = packetContainer.getBooleans().getValues().get(0);
                if (event.getPacketType().equals((Object)PacketType.Play.Client.POSITION) || event.getPacketType().equals((Object)PacketType.Play.Client.POSITION_LOOK)) {
                    final List<Double> packetDoubles = (List<Double>)packetContainer.getDoubles().getValues();
                    final Checkable checkable = plugin2.catchCheckable(IIUA.getUUIDFrom(p));
                    final double yMotion = packetDoubles.get(1) - checkable.getMeta().getLocationValues().nofallVerifyLocation.getY();
                    final double lyMotion = checkable.getMeta().getSyncedValues().lastNFYmovement;
                    final Location newLocation = checkable.getMeta().getLocationValues().nofallVerifyLocation;
                    final Location oldLocation = newLocation.clone();
                    newLocation.setX((double)packetDoubles.get(0));
                    newLocation.setY((double)packetDoubles.get(1));
                    newLocation.setZ((double)packetDoubles.get(2));
                    if (p == null || newLocation.getWorld() == null) {
                        return;
                    }
                    if (!newLocation.getWorld().equals(p.getWorld())) {
                        checkable.getMeta().getLocationValues().nofallVerifyLocation.setWorld(p.getWorld());
                    }
                    if (LocationHelper.getDistanceSafe(newLocation, oldLocation) <= 30.0 || oldLocation.getY() - 10.0 > newLocation.getY()) {}
                    if (LocationHelper.getDistanceSafe(newLocation, oldLocation) > 30.0) {
                        final Checkable.CheckableMeta.ViolationValues vioValues = checkable.getMeta().getVioValues();
                        vioValues.syntexVL += 50;
                        if (checkable.getMeta().getVioValues().syntexVL > 500) {
                            PlayerUtils.performSyc(p, player -> player.kickPlayer("Invalid position"));
                        }
                        return;
                    }
                    if (checkable.getMeta().getVioValues().syntexVL > 0) {
                        final Checkable.CheckableMeta.ViolationValues vioValues2 = checkable.getMeta().getVioValues();
                        --vioValues2.syntexVL;
                    }
                    if (!LocationHelper.isInLoadedChunk(p.getWorld(), (int)newLocation.getX(), (int)newLocation.getZ())) {
                        return;
                    }
                    if (claimsOnGround) {
                        boolean og = LocationHelper.isOnGroundLessAccurate(newLocation, 0.5) || LocationHelper.isOnGroundLessAccurate(oldLocation, 0.5) || LocationHelper.isOnGroundF(newLocation) || LocationHelper.collidesAnyStepable(newLocation.getWorld(), (Entity)p, newLocation, 0.2) || LocationHelper.collidesAnyStepable(oldLocation.getWorld(), (Entity)p, oldLocation, 0.2) || LocationHelper.hasLiquitNearby(newLocation) || LocationHelper.hasWoopableBlockNearby(oldLocation, (Entity)p);
                        if (og && yMotion < lyMotion && yMotion != 0.0) {
                            og = false;
                        }
                        if (!og && yMotion > lyMotion && lyMotion <= 0.0 && yMotion > 0.0) {
                            og = true;
                        }
                        if (yMotion < lyMotion && yMotion < 0.0 && lyMotion <= 0.0 && lyMotion - yMotion > 0.07) {
                            og = false;
                        }
                        if (!og) {
                            if (yMotion > 0.0 && lyMotion > 0.0) {
                                plugin2.getRetributionManager().markPlayer(p, 1, "NoFall", CheatCategory.MOVING, "claimed to be on ground midair");
                            }
                            packetContainer.getBooleans().write(0, (Object)false);
                        }
                    }
                    plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().lastNFYmovement = yMotion;
                }
            }
        });
    }
    
    @Override
    public void onCheckableMove(final CheckableMoveEvent e) {
    }
    
    public void checkOnGround(final CheckableMoveEvent e) {
        final Player p = e.getBukkitPlayer();
        final double yDiff = e.getYDirectedMotion();
        final double lastYDiff = e.getCheckable().getMeta().getSyncedValues().lastYmovement;
        final long triedToPlaceABlockRecently = IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeValidBlockPlaced;
        if (ProtocolLibAdapter.getProtocolManager().getMinecraftVersion().isAtLeast(MinecraftVersion.COMBAT_UPDATE) && LocationHelper.isOnGroundQ(e.getTo()) && yDiff > lastYDiff) {
            return;
        }
        boolean flag = false;
        boolean fflag = false;
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeTeleported < 150L && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeFlagged > 150L) {
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded < 1000L && e.getCheckable().getMeta().getSyncedValues().lastVelocityFI.getY() < -0.08) {
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastNearbyBlockPhysicsEvent < 400 + e.getCheckable().getPing()) {
            return;
        }
        if (IIUA.isPretendingToBeOnGround(p) && !e.isOnGround() && (p.getFallDistance() > 1.0f || e.getYDirectedMotion() < -0.5) && this.plugin.getRetributionManager().markPlayer(p, 6, "NoFall", CheatCategory.MOVING, "claimed to be on ground midair")) {
            fflag = true;
            e.setCancelled(true);
        }
        if (IIUA.isPretendingToBeOnGround(p) && !PlayerUtils.isOnGroundValueCorruptable(p) && (p.getVelocity().getY() < -0.2 || e.getYDirectedMotion() > 0.0)) {
            if (!LocationHelper.isOnGroundAccurate(e.getFrom(), (Entity)p) && e.getYDirectedMotion() < -0.2) {
                flag = true;
            }
            if (LocationHelper.collidesEFar(p.getWorld(), (Entity)p, e.getFrom(), Material.PISTON_EXTENSION, Material.PISTON_MOVING_PIECE, Material.PISTON_BASE, Material.PISTON_STICKY_BASE)) {
                return;
            }
            if (LocationHelper.collidesEFar(p.getWorld(), (Entity)p, e.getTo(), Material.PISTON_EXTENSION, Material.PISTON_MOVING_PIECE, Material.PISTON_BASE, Material.PISTON_STICKY_BASE)) {
                return;
            }
            if (!LocationHelper.isOnGroundR(e.getTo()) && !LocationHelper.isOnGroundR(e.getFrom()) && !LocationHelper.collidesAnyStepable(e.getTo().getWorld(), (Entity)p, e.getFrom(), 0.2)) {
                flag = true;
            }
            final boolean og = LocationHelper.isOnGroundLessAccurate(e.getTo(), 0.5) || LocationHelper.isOnGroundLessAccurate(e.getFrom(), 0.5) || LocationHelper.isOnGroundF(e.getTo()) || LocationHelper.collidesAnyStepable(e.getTo().getWorld(), (Entity)p, e.getTo(), 0.2) || LocationHelper.collidesAnyStepable(e.getFrom().getWorld(), (Entity)p, e.getFrom(), 0.2) || LocationHelper.hasLiquitNearby(e.getTo());
            if (e.getYDirectedMotion() <= 0.0 && !og) {
                flag = true;
            }
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeXZMovementCancelled < 1000L && e.getXZDirectedMotion() > 0.01) {
            e.getMoveFlagData().setPullSilent(true);
            e.getMoveFlagData().setPullYMotion(lastYDiff - 0.0785);
        }
        if (flag && !IIUA.getAllowFlight(p)) {
            if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType().equals((Object)Material.AIR)) {
                p.sendBlockChange(e.getTo().getBlock().getRelative(BlockFace.DOWN).getLocation(), Material.AIR, (byte)0);
            }
            boolean flag2 = triedToPlaceABlockRecently < 700 + e.getCheckable().getPing() && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeXZMovementCancelled > 300L;
            if (LocationHelper.collidesAny(p.getWorld(), (Entity)p, e.getTo(), e.getYDirectedMotion() + 0.1, 0.05)) {
                flag2 = true;
            }
            if (p.getFallDistance() > 0.0f || e.getYDirectedMotion() == 0.0 || e.getXZDirectedMotion() > 0.4) {
                final Checkable.CheckableMeta.ViolationValues vioValues = e.getCheckable().getMeta().getVioValues();
                vioValues.suspiciousNoFallMovements += 2;
            }
            if (e.getCheckable().getMeta().getVioValues().suspiciousNoFallMovements > 5) {
                e.getCheckable().getMeta().getTimedValues().lastTimeXZMovementCancelled = IIUA.getCurrentTimeMillis();
            }
            if (flag2) {
                p.setFallDistance((float)(p.getFallDistance() + Math.abs(e.getYDirectedMotion())));
            }
            if (this.plugin.getRetributionManager().markPlayer(p, flag2 ? 0 : 1, "NoFall", CheatCategory.MOVING, "claimed to be on ground midair")) {
                fflag = true;
                final double locDiff = MathHelper.diff(p.getLocation().getY(), e.getCheckable().getMeta().getLocationValues().lastLocationOnGround.getY());
                final Location l = e.getFrom();
                if (l.subtract(0.0, lastYDiff - 0.0785, 0.0).getBlock().getType().equals((Object)Material.AIR)) {
                    e.getMoveFlagData().setPullSilent(true);
                    e.getMoveFlagData().setPullYMotion((lastYDiff - 0.08) * 0.98);
                }
                else {
                    e.getMoveFlagData().setPullSilent(true);
                    e.getMoveFlagData().setPullYMotion((lastYDiff - 0.08) * 0.98);
                }
            }
        }
        else if (e.getCheckable().getMeta().getVioValues().suspiciousNoFallMovements > 0) {
            final Checkable.CheckableMeta.ViolationValues vioValues2 = e.getCheckable().getMeta().getVioValues();
            --vioValues2.suspiciousNoFallMovements;
        }
        if (fflag) {}
    }
}
