// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.calc;

import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.potion.PotionEffect;
import java.util.ArrayList;
import org.bukkit.entity.LivingEntity;
import java.util.Iterator;
import de.jpx3.intave.util.objectable.BukkitEntityHitbox;
import de.jpx3.intave.util.data.BlockData;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Material;
import de.jpx3.intave.util.objectable.Checkable;
import java.lang.reflect.InvocationTargetException;
import de.jpx3.intave.api.external.linked.exceptions.IntaveInternalException;
import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.antipiracy.IIUA;
import com.comphenix.protocol.utility.MinecraftVersion;
import de.jpx3.intave.api.internal.ProtocolLibAdapter;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.ConcurrentModificationException;
import org.bukkit.entity.EntityType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import java.util.List;

public final class PlayerUtils
{
    private static final List<Entity> cachedList;
    
    public static boolean hasEntityTypesNearby(final Player player, final Location location, final double dis, final EntityType... entityTypes) {
        try {
            final double maxX = location.getX() + dis;
            final double minX = location.getX() - dis;
            final double maxZ = location.getZ() + dis;
            final double minZ = location.getZ() - dis;
            for (double blockX = minX; blockX < maxX; ++blockX) {
                for (double blockZ = minZ; blockZ < maxZ; ++blockZ) {
                    if (LocationHelper.isInLoadedChunk(player.getWorld(), (int)blockX, (int)blockZ)) {
                        for (final Entity entity : player.getWorld().getChunkAt((int)blockX << 4, (int)blockZ << 4).getEntities()) {
                            for (final EntityType entityType : entityTypes) {
                                if (entity.getType().equals((Object)entityType)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (ConcurrentModificationException ex) {}
        return false;
    }
    
    public static List<Entity> getNearbyEntities(final Player player, final Location location, final double dis) {
        synchronized (PlayerUtils.cachedList) {
            PlayerUtils.cachedList.clear();
            final List<Entity> list = PlayerUtils.cachedList;
            try {
                location.getWorld().getEntities().stream().filter(entity -> LocationHelper.getDistanceSafe(entity.getLocation(), location) <= dis).filter(entity -> entity.getEntityId() != player.getEntityId()).forEach(list::add);
            }
            catch (NoSuchElementException ex) {}
            catch (ConcurrentModificationException ex2) {}
            return list;
        }
    }
    
    public static boolean hasLevitation(final Player p) {
        if (!ProtocolLibAdapter.getProtocolManager().getMinecraftVersion().isAtLeast(MinecraftVersion.BOUNTIFUL_UPDATE)) {
            return false;
        }
        if (IIUA.getCurrentTimeMillis() - IntavePlugin.getStaticReference().catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeHadLeviation < 500L) {
            return true;
        }
        try {
            boolean hasIt = p.getActivePotionEffects().stream().anyMatch(potionEffect -> potionEffect.getType().getName().equalsIgnoreCase("levitation"));
            if (hasIt) {
                IntavePlugin.getStaticReference().catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeHadLeviation = IIUA.getCurrentTimeMillis();
            }
            else {
                hasIt = (IIUA.getCurrentTimeMillis() - IntavePlugin.getStaticReference().catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeHadLeviation < 500L);
            }
            return hasIt;
        }
        catch (ConcurrentModificationException e) {
            return true;
        }
    }
    
    public static boolean isUsingElytra(final Player p) {
        if (!ProtocolLibAdapter.getProtocolManager().getMinecraftVersion().isAtLeast(MinecraftVersion.COMBAT_UPDATE)) {
            return false;
        }
        try {
            final Checkable checkable = IntavePlugin.getStaticReference().catchCheckable(p.getUniqueId());
            if (IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastTimeUsedelytra < 1000L) {
                return true;
            }
            try {
                final boolean usingIt = (boolean)p.getClass().getMethod("isGliding", (Class<?>[])new Class[0]).invoke(p, new Object[0]);
                if (usingIt) {
                    checkable.getMeta().getTimedValues().lastTimeUsedelytra = IIUA.getCurrentTimeMillis();
                }
                return usingIt;
            }
            catch (InvocationTargetException e) {
                throw new IntaveInternalException("Intave has problems fetching the method $isGliding on player " + p.getName() + ".", e);
            }
        }
        catch (IllegalAccessException ex) {}
        catch (InvocationTargetException ex2) {}
        catch (NoSuchMethodException ex3) {}
    }
    
    public static boolean isOnGroundValueCorruptable(final Player p) {
        final Checkable player = IntavePlugin.getStaticReference().catchCheckable(IIUA.getUUIDFrom(p));
        return IIUA.getCurrentTimeMillis() - player.getMeta().getTimedValues().lastTimeInCobweb < 800L || p.isInsideVehicle() || p.getEyeLocation().getBlock().isLiquid() || IIUA.getCurrentTimeMillis() - player.getMeta().getTimedValues().lastTimeMovedOnWaterConsistant < 850L || getNearbyEntities(p, p.getLocation(), 1.5).stream().anyMatch(entity -> entity.getType().equals((Object)EntityType.MINECART) || entity.getType().equals((Object)EntityType.BOAT));
    }
    
    public static boolean getAllowFlight(final Player p) {
        return getAllowFlight(p, true, true, true, true, true);
    }
    
    public static boolean getAllowFlight(final Player p, final boolean calcJumpPotions, final boolean calcDamage, final boolean calcWater) {
        return getAllowFlight(p, calcJumpPotions, calcDamage, calcWater, true, true);
    }
    
    public static boolean getAllowFlight(final Player p, final boolean calcJumpPotions, final boolean calcDamage, final boolean calcWater, final boolean calcSlimes, final boolean calcWebs) {
        final Checkable player = IntavePlugin.getStaticReference().catchCheckable(IIUA.getUUIDFrom(p));
        final long start = System.nanoTime();
        if (IIUA.getAllowFlight(p) || isUsingElytra(p)) {
            return true;
        }
        if (IIUA.getCurrentTimeMillis() - player.getMeta().getTimedValues().lastTimeFlying < 500 + player.getPing()) {
            return true;
        }
        if ((IIUA.getCurrentTimeMillis() - player.getMeta().getTimedValues().lastHitByEntityTimestamp < 900L || LocationHelper.collides(p.getLocation().getWorld(), (Entity)p, Material.CACTUS)) && calcDamage && (player.getMeta().getTimedValues().lastTimeStandingOnABlock < player.getMeta().getTimedValues().lastHitByEntityTimestamp || player.getMeta().getTimedValues().lastTimeStandingOnABlock - player.getMeta().getTimedValues().lastHitByEntityTimestamp < 100L) && player.getMeta().getTimedValues().lastTimeStandingOnABlock != 0L) {
            return !player.getMeta().getSyncedValues().hadYVelocityStartMotion;
        }
        if (LocationHelper.collides(p.getWorld(), (Entity)p, Material.WEB) && calcWebs) {
            return true;
        }
        if (calcDamage && movementAffectedByExplosion(player)) {
            return true;
        }
        if (calcWater && (LocationHelper.collidesLiquid((Entity)p) || LocationHelper.collidesACLiquit(p.getWorld(), (Entity)p, p.getLocation()))) {
            return true;
        }
        if (IIUA.getCurrentTimeMillis() - player.getMeta().getTimedValues().lastTimeInteractedWithVehicle < 750L) {
            return true;
        }
        if (calcJumpPotions && LocationHelper.collidesEFar(p.getWorld(), (Entity)p, p.getLocation(), Material.PISTON_EXTENSION, Material.PISTON_MOVING_PIECE, Material.PISTON_BASE, Material.PISTON_STICKY_BASE)) {
            return true;
        }
        if (calcJumpPotions && p.hasPotionEffect(PotionEffectType.JUMP)) {
            return true;
        }
        if (p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().name().contains("PISTON")) {
            return true;
        }
        if (IIUA.getCurrentTimeMillis() - player.getMeta().getTimedValues().lastTimeStandingOnAPiston < 1000L) {
            return true;
        }
        final long slime = IIUA.getCurrentTimeMillis() - player.getMeta().getTimedValues().lastTimeStandingOnASlime;
        return (slime < 2000L && player.getMeta().getSyncedValues().lastYmovement > 0.0 && calcSlimes) || (calcSlimes && p.getVelocity().getY() >= -0.8 && p.getVelocity().getY() <= 0.42 && IIUA.getCurrentTimeMillis() - player.getMeta().getTimedValues().lastTimeSlimeWasNear < 1200L) || (IIUA.getCurrentTimeMillis() - player.getMeta().getTimedValues().lastTimeClimbedALadder < 100L && player.getMeta().getSyncedValues().ticksInAir < 3L) || getNearbyEntities(p, player.getVerifiedLocation(), 2.0).stream().anyMatch(entity -> entity.getType().equals((Object)EntityType.BOAT) || entity.getType().equals((Object)EntityType.MINECART));
    }
    
    public static void sendMessage(final String playername, final String message) {
        if (isOnline(playername)) {
            Bukkit.getPlayer(playername).sendMessage(message);
        }
    }
    
    public static void sendMessage(final UUID uuid, final String message) {
        if (isOnline(uuid)) {
            Bukkit.getPlayer(uuid).sendMessage(message);
        }
    }
    
    public static <T> void performSyc(final T applier, final Consumer<? super T> action) {
        Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)IntavePlugin.getStaticReference(), () -> action.accept((Object)applier));
    }
    
    public static boolean isOnline(final String name) {
        return Bukkit.getPlayer(name) != null && Bukkit.getPlayer(name).isOnline();
    }
    
    public static boolean isOnline(final UUID uuid) {
        return Bukkit.getPlayer(uuid) != null && Bukkit.getPlayer(uuid).isOnline();
    }
    
    public static void suspendSlot(final Player p) {
        final int slot = p.getInventory().getHeldItemSlot();
        final int newF = (slot > 7) ? 7 : (slot + 1);
        p.getInventory().setHeldItemSlot(newF);
    }
    
    public static boolean movementAffectedByExplosion(final Checkable player) {
        final long lastTimeOnGround = player.getMeta().getTimedValues().lastTimeOnGroundACC + 100L;
        final long lastTNTExplosion = player.getMeta().getTimedValues().lastTimeBlockExplodeAffect;
        return IIUA.getCurrentTimeMillis() - lastTNTExplosion < 6000L && (lastTimeOnGround < lastTNTExplosion || (IIUA.getCurrentTimeMillis() - lastTimeOnGround < 700L && IIUA.getCurrentTimeMillis() - lastTNTExplosion < 700L));
    }
    
    @Deprecated
    public static boolean hasInLineOfSight(final Player observer, final Entity target) {
        return getLineOfSightSolveError(observer, target) <= 1.5;
    }
    
    public static double getLineOfSightSolveError(final Player observer, final Entity target) {
        final BukkitEntityHitbox entityHitbox = HitBoxService.getBoundingBox(target);
        double nearest = 5.0;
        boolean iteracted = false;
        for (final Block b : getBlocksInSight(observer, 4)) {
            iteracted = true;
            if (!BlockData.isPassable(b)) {
                break;
            }
            final Location targetLocation = target.getLocation();
            targetLocation.setY(targetLocation.getY() + entityHitbox.getBoxHeight() / 2.0);
            final double distance = b.getLocation().toVector().distance(targetLocation.toVector());
            if (nearest <= distance) {
                continue;
            }
            nearest = distance;
        }
        if (!iteracted) {
            return 0.0;
        }
        return nearest - entityHitbox.getBoxWidth() / 2.0;
    }
    
    public static double getLineOfSightSolveError(final Player observer, final Entity target, final Location location) {
        final BukkitEntityHitbox entityHitbox = HitBoxService.getBoundingBox(target);
        double nearest = 5.0;
        boolean iterated = false;
        for (final Block b : getBlocksInSight(observer, 4)) {
            iterated = true;
            if (!BlockData.isPassable(b)) {
                break;
            }
            final Location targetLocation = location.clone();
            targetLocation.setY(targetLocation.getY() + entityHitbox.getBoxHeight() / 2.0);
            final double distance = b.getLocation().toVector().distance(targetLocation.toVector());
            if (nearest <= distance) {
                continue;
            }
            nearest = distance;
        }
        if (!iterated) {
            return 0.0;
        }
        return nearest - entityHitbox.getBoxWidth() / 2.0;
    }
    
    public static List<Block> getBlocksInSight(final Player player, final int range) {
        return IntavePlugin.getStaticReference().getIntaveBlockIterator().calc((LivingEntity)player, range);
    }
    
    public static List<Block> getBlocksInSight(final Location location, final int range) {
        if (IntavePlugin.getStaticReference().getIntaveBlockIterator() == null) {
            return new ArrayList<Block>();
        }
        return IntavePlugin.getStaticReference().getIntaveBlockIterator().calc(location, 1.6, range);
    }
    
    public static Entity getNearestEntity(final Player p) {
        Entity nearest = null;
        for (final Entity entity : getNearbyEntities(p, p.getLocation(), 4.5)) {
            if ((nearest == null && entity != p) || (entity != p && entity.getLocation().toVector().distance(p.getLocation().toVector()) < nearest.getLocation().toVector().distance(p.getLocation().toVector()))) {
                nearest = entity;
            }
        }
        return nearest;
    }
    
    static {
        cachedList = new CopyOnWriteArrayList<Entity>();
    }
}
