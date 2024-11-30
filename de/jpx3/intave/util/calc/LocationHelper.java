// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.calc;

import org.bukkit.util.NumberConversions;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import org.bukkit.entity.Player;
import de.jpx3.intave.util.objectable.BukkitEntityHitbox;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import de.jpx3.intave.util.data.MaterialData;
import de.jpx3.intave.util.data.BlockData;
import java.util.Arrays;
import org.bukkit.block.BlockFace;
import org.bukkit.Location;
import org.bukkit.block.Block;

public final class LocationHelper
{
    private static Block deadRequestBlock;
    
    public static int unpassableCollideCode(final Location loc) {
        final Block[] blocks = { loc.getBlock().getRelative(BlockFace.NORTH), loc.getBlock().getRelative(BlockFace.SOUTH), loc.getBlock().getRelative(BlockFace.WEST), loc.getBlock().getRelative(BlockFace.EAST) };
        return (int)Arrays.stream(blocks).filter(block -> !BlockData.isPassable(block)).count();
    }
    
    public static boolean hasStepableNearby(final Location loc) {
        return BlockData.isStepable(loc.getBlock().getRelative(BlockFace.NORTH), loc) || BlockData.isStepable(loc.getBlock().getRelative(BlockFace.SOUTH), loc) || BlockData.isStepable(loc.getBlock().getRelative(BlockFace.WEST), loc) || BlockData.isStepable(loc.getBlock().getRelative(BlockFace.EAST), loc) || BlockData.isStepable(loc.getBlock().getRelative(BlockFace.NORTH_WEST), loc) || BlockData.isStepable(loc.getBlock().getRelative(BlockFace.NORTH_EAST), loc) || BlockData.isStepable(loc.getBlock().getRelative(BlockFace.SOUTH_WEST), loc) || BlockData.isStepable(loc.getBlock().getRelative(BlockFace.SOUTH_EAST), loc) || BlockData.isStepable(loc.getBlock().getRelative(BlockFace.DOWN), loc) || BlockData.isStepable(loc.getBlock(), loc);
    }
    
    public static boolean hasStepableNoTopNearby(final Location loc) {
        final Block[] blocks = { loc.getBlock().getRelative(BlockFace.NORTH), loc.getBlock().getRelative(BlockFace.SOUTH), loc.getBlock().getRelative(BlockFace.WEST), loc.getBlock().getRelative(BlockFace.EAST), loc.getBlock().getRelative(BlockFace.NORTH_WEST), loc.getBlock().getRelative(BlockFace.NORTH_EAST), loc.getBlock().getRelative(BlockFace.SOUTH_WEST), loc.getBlock().getRelative(BlockFace.SOUTH_EAST), loc.getBlock().getRelative(BlockFace.DOWN), loc.getBlock() };
        return Arrays.stream(blocks).anyMatch(block -> BlockData.isStepable(block, loc) || MaterialData.isDoor(block.getRelative(BlockFace.UP).getType()));
    }
    
    public static boolean hasStepableNoGroundNearby(final Location loc) {
        final Block[] blocks = { loc.getBlock().getRelative(BlockFace.NORTH), loc.getBlock().getRelative(BlockFace.SOUTH), loc.getBlock().getRelative(BlockFace.WEST), loc.getBlock().getRelative(BlockFace.EAST), loc.getBlock().getRelative(BlockFace.NORTH_WEST), loc.getBlock().getRelative(BlockFace.NORTH_EAST), loc.getBlock().getRelative(BlockFace.SOUTH_WEST), loc.getBlock().getRelative(BlockFace.SOUTH_EAST), loc.getBlock().getRelative(BlockFace.DOWN), loc.getBlock() };
        return Arrays.stream(blocks).anyMatch(block -> BlockData.isStepable(block, loc) && BlockData.isStepable(block.getRelative(BlockFace.DOWN), loc) && block.getRelative(BlockFace.DOWN).getType().equals((Object)block.getType()));
    }
    
    public static boolean collidesLiquid(final Entity e) {
        return collidesLiquid(e, e.getLocation());
    }
    
    public static boolean collidesLiquid(final Entity e, final Location fakeLocation) {
        return collides(e.getLocation().getWorld(), e, fakeLocation, Material.WATER, Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA);
    }
    
    public static boolean hasLiquitNearby(final Location l) {
        final Block down = getBlockAt(l);
        return down.isLiquid() || getRelative(down, BlockFace.NORTH).isLiquid() || getRelative(down, BlockFace.SOUTH).isLiquid() || getRelative(down, BlockFace.WEST).isLiquid() || getRelative(down, BlockFace.NORTH_EAST).isLiquid() || getRelative(down, BlockFace.NORTH_WEST).isLiquid() || getRelative(down, BlockFace.SOUTH_EAST).isLiquid() || getRelative(down, BlockFace.SOUTH_WEST).isLiquid();
    }
    
    public static boolean collides(final World world, final Entity e, final Material material) {
        return collides(world, e, e.getLocation(), material);
    }
    
    public static boolean isInsideUnpassable(final World world, final Entity e, final Location l) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz + 0.0;
        final double minY = l.getY() + 0.08;
        final double minZ = l.getZ() - dxz + 0.0;
        final double maxX = l.getX() + dxz - 0.0;
        final double maxY = l.getY() + height - 0.3;
        final double maxZ = l.getZ() + dxz - 0.0;
        return collidesUnpassable(world, minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public static boolean isInsideMotionRelevant(final World world, final Entity e, final Location l) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz;
        final double minY = l.getY() + height - 0.1;
        final double minZ = l.getZ() - dxz + 0.0;
        final double maxX = l.getX() + dxz - 0.0;
        final double maxY = l.getY() + height + 0.1;
        final double maxZ = l.getZ() + dxz - 0.0;
        return collidesMotionRelevant(world, minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public static boolean isInsideStepable(final World world, final Entity e, final Location l) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz;
        final double minY = l.getY() - 0.25;
        final double minZ = l.getZ() - dxz + 0.0;
        final double maxX = l.getX() + dxz - 0.0;
        final double maxY = l.getY() + height - 0.6;
        final double maxZ = l.getZ() + dxz - 0.0;
        return collidesIntStepable(world, minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public static Block getUnpassableIfThere(final World world, final Entity e, final Location l) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz + 0.0;
        final double minY = l.getY() + 0.08;
        final double minZ = l.getZ() - dxz + 0.0;
        final double maxX = l.getX() + dxz - 0.0;
        final double maxY = l.getY() + height - 0.3;
        final double maxZ = l.getZ() + dxz - 0.0;
        return getUnpassable(world, minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public static boolean __unsafe__isInsideUnpassable(final World world, final Entity e, final Location l) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz + 0.1;
        final double minY = l.getY() + 0.1;
        final double minZ = l.getZ() - dxz + 0.1;
        final double maxX = l.getX() + dxz - 0.1;
        final double maxY = l.getY() + height - 0.2;
        final double maxZ = l.getZ() + dxz - 0.1;
        return collidesUnpassable(world, minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public static boolean collidesFar(final World world, final Entity e, final Location l, final Material... material) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz - 0.7;
        final double minY = l.getY() - 0.75;
        final double minZ = l.getZ() - dxz - 0.7;
        final double maxX = l.getX() + dxz + 0.7;
        final double maxY = l.getY() + height + 0.7;
        final double maxZ = l.getZ() + dxz + 0.7;
        return collides(world, minX, minY, minZ, maxX, maxY, maxZ, material);
    }
    
    public static boolean collidesEFar(final World world, final Entity e, final Location l, final Material... material) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz - 1.0;
        final double minY = l.getY() - 1.0;
        final double minZ = l.getZ() - dxz - 1.0;
        final double maxX = l.getX() + dxz + 1.0;
        final double maxY = l.getY() + height + 1.0;
        final double maxZ = l.getZ() + dxz + 1.0;
        return collides(world, minX, minY, minZ, maxX, maxY, maxZ, material);
    }
    
    public static boolean collidesAnyXZAxis(final World world, final Entity e, final Location l) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz - 0.2;
        final double minY = l.getY() + 0.2;
        final double minZ = l.getZ() - dxz - 0.2;
        final double maxX = l.getX() + dxz + 0.2;
        final double maxY = l.getY() + height + 0.3;
        final double maxZ = l.getZ() + dxz + 0.2;
        return collidesAny(world, minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public static boolean collidesAnySMRXZAxis(final World world, final Entity e, final Location l) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz - 0.2;
        final double minY = l.getY() + 0.1;
        final double minZ = l.getZ() - dxz - 0.2;
        final double maxX = l.getX() + dxz + 0.2;
        final double maxY = l.getY() + height + 0.4;
        final double maxZ = l.getZ() + dxz + 0.2;
        return collidesSolidMotionRelevant(world, minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public static boolean collidesAnyValidXZAxis(final World world, final Entity e, final Location l) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz - 0.4;
        final double minY = l.getY() + 0.1;
        final double minZ = l.getZ() - dxz - 0.4;
        final double maxX = l.getX() + dxz + 0.4;
        final double maxY = l.getY() + height + 0.5;
        final double maxZ = l.getZ() + dxz + 0.4;
        return collidesAnyValid(world, minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public static boolean isInside(final Location targetLocation, final Entity target, final Location fixlocation) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(target);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = targetLocation.getX() - dxz;
        final double minY = targetLocation.getY();
        final double minZ = targetLocation.getZ() - dxz - 0.01;
        final double maxX = targetLocation.getX() + dxz + 0.01;
        final double maxY = targetLocation.getY() + height;
        final double maxZ = targetLocation.getZ() + dxz + 0.01;
        return isInsideOf(targetLocation.getWorld(), minX, minY, minZ, maxX, maxY, maxZ, fixlocation);
    }
    
    public static boolean collidesAnyXZAxisNY(final World world, final Entity e, final Location l) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz - 0.01;
        final double minY = l.getY();
        final double minZ = l.getZ() - dxz - 0.01;
        final double maxX = l.getX() + dxz + 0.01;
        final double maxY = l.getY() + height;
        final double maxZ = l.getZ() + dxz + 0.01;
        return collidesAny(world, minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public static boolean collidesAnyXZAxisFar(final World world, final Entity e, final Location l) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz - 0.8;
        final double minY = l.getY() + 0.1;
        final double minZ = l.getZ() - dxz - 0.8;
        final double maxX = l.getX() + dxz + 0.8;
        final double maxY = l.getY() + height + 0.5;
        final double maxZ = l.getZ() + dxz + 0.8;
        return collidesAny(world, minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public static boolean collidesXZAxis(final World world, final Entity e, final Location l, final Material... material) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz - 0.2;
        final double minY = l.getY();
        final double minZ = l.getZ() - dxz - 0.2;
        final double maxX = l.getX() + dxz + 0.2;
        final double maxY = l.getY() + height + 0.5;
        final double maxZ = l.getZ() + dxz + 0.2;
        return collides(world, minX, minY, minZ, maxX, maxY, maxZ, material);
    }
    
    public static boolean collidesXZAxisFar(final World world, final Entity e, final Location l, final Material material) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz - 0.9;
        final double minY = l.getY();
        final double minZ = l.getZ() - dxz - 0.9;
        final double maxX = l.getX() + dxz + 0.9;
        final double maxY = l.getY() + height + 0.9;
        final double maxZ = l.getZ() + dxz + 0.9;
        return collides(world, minX, minY, minZ, maxX, maxY, maxZ, material);
    }
    
    public static boolean collidesACLiquit(final World world, final Entity e, final Location l) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz - 1.0;
        final double minY = l.getY() - 0.5;
        final double minZ = l.getZ() - dxz - 1.0;
        final double maxX = l.getX() + dxz + 1.0;
        final double maxY = l.getY() + height + 1.0;
        final double maxZ = l.getZ() + dxz + 1.0;
        return collidesACLiquit(world, minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public static boolean collidesNearACLiquit(final World world, final Entity e, final Location l) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz - 0.1;
        final double minY = l.getY();
        final double minZ = l.getZ() - dxz - 0.1;
        final double maxX = l.getX() + dxz + 0.1;
        final double maxY = l.getY() + height + 0.1;
        final double maxZ = l.getZ() + dxz + 0.1;
        return collidesACLiquit(world, minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public static boolean collidesAny(final World world, final Entity e, final Location l, final double ySize, final double xzSize) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz - xzSize;
        final double minY = l.getY() - ySize;
        final double minZ = l.getZ() - dxz - xzSize;
        final double maxX = l.getX() + dxz + xzSize;
        final double maxY = l.getY() + height + ySize;
        final double maxZ = l.getZ() + dxz + xzSize;
        return collidesAny(world, minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public static boolean collides(final World world, final Entity e, final Location l, final Material... material) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz - 0.08;
        final double minY = l.getY();
        final double minZ = l.getZ() - dxz - 0.08;
        final double maxX = l.getX() + dxz + 0.08;
        final double maxY = l.getY() + height + 0.08;
        final double maxZ = l.getZ() + dxz + 0.08;
        return collides(world, minX, minY, minZ, maxX, maxY, maxZ, material);
    }
    
    public static boolean collidesU(final World world, final Entity e, final Location l, final Material material, final double ex_size) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz + ex_size;
        final double minY = l.getY() - 0.05;
        final double minZ = l.getZ() - dxz + ex_size;
        final double maxX = l.getX() + dxz - ex_size;
        final double maxY = l.getY() + height + 0.05;
        final double maxZ = l.getZ() + dxz - ex_size;
        return collides(world, minX, minY, minZ, maxX, maxY, maxZ, material);
    }
    
    public static boolean collidesAny(final World world, final Entity e, final Location l) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz + 0.3;
        final double minY = l.getY() - 0.25;
        final double minZ = l.getZ() - dxz - 0.3;
        final double maxX = l.getX() + dxz + 0.3;
        final double maxY = l.getY() + height + 0.25;
        final double maxZ = l.getZ() + dxz + 0.3;
        return collidesAny(world, minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public static boolean collidesAnyFar(final World world, final Entity e, final Location l) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz - 0.5;
        final double minY = l.getY() - 0.3;
        final double minZ = l.getZ() - dxz - 0.5;
        final double maxX = l.getX() + dxz + 0.5;
        final double maxY = l.getY() + height + 0.5;
        final double maxZ = l.getZ() + dxz + 0.5;
        return collidesAnyValid(world, minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public static boolean collidesAnyValidFar(final World world, final Entity e, final Location l) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz - 0.5;
        final double minY = l.getY() - 0.3;
        final double minZ = l.getZ() - dxz - 0.5;
        final double maxX = l.getX() + dxz + 0.5;
        final double maxY = l.getY() + height + 0.5;
        final double maxZ = l.getZ() + dxz + 0.5;
        return collidesAnyValid(world, minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public static boolean onlyCollides(final World world, final Entity e, final Location l, final Material... material) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz - 0.001;
        final double minY = l.getY() - 0.2;
        final double minZ = l.getZ() - dxz - 0.001;
        final double maxX = l.getX() + dxz + 0.001;
        final double maxY = l.getY() + height + 0.2;
        final double maxZ = l.getZ() + dxz + 0.001;
        return hasHeapSpace(world, minX, minY, minZ, maxX, maxY, maxZ, material);
    }
    
    public static boolean onlyCollidesFar(final World world, final Entity e, final Location l, final Material... material) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz - 1.0;
        final double minY = l.getY() - 0.9;
        final double minZ = l.getZ() - dxz - 1.0;
        final double maxX = l.getX() + dxz + 1.0;
        final double maxY = l.getY() + height;
        final double maxZ = l.getZ() + dxz + 1.0;
        return hasHeapSpace(world, minX, minY, minZ, maxX, maxY, maxZ, material);
    }
    
    public static boolean onlyCollidesYFar(final World world, final Entity e, final Location l, final Material... material) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz - 0.1;
        final double minY = l.getY() - 1.2;
        final double minZ = l.getZ() - dxz - 0.1;
        final double maxX = l.getX() + dxz + 0.1;
        final double maxY = l.getY() + height + 0.9;
        final double maxZ = l.getZ() + dxz + 0.1;
        return hasHeapSpace(world, minX, minY, minZ, maxX, maxY, maxZ, material);
    }
    
    public static boolean collidesAnyStepable(final World world, final Entity e, final Location l, final double j) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz - j;
        final double minY = l.getY() - getPossibleOffset(l) - 0.01;
        final double minZ = l.getZ() - dxz - j;
        final double maxX = l.getX() + dxz + j;
        final double maxY = l.getY() + height;
        final double maxZ = l.getZ() + dxz + j;
        return validStepableBlockThere(l, world, minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public static boolean collidesStepable(final World world, final Entity e, final Location l) {
        return hasStepableNoTopNearby(l);
    }
    
    public static boolean collidesStepableIgnoringTopBlock(final World world, final Entity e, final Location l) {
        return hasStepableNearby(l);
    }
    
    public static boolean isOnGround(final Player p) {
        return isOnGround(p.getLocation());
    }
    
    public static boolean isOnGround(final Location l) {
        return BlockData.isStepable(l.clone().subtract(0.0, 0.5001, 0.0).getBlock(), l) || BlockData.doesAffectMovement(l.clone().subtract(0.0, 1.0, 0.0).getBlock());
    }
    
    public static boolean isOnGroundQ(final Location location) {
        return blockThere(location.getWorld(), location.getX() - 2.0005, location.getY() - getPossibleOffset(location), location.getZ() - 2.0005, location.getX() + 2.0005, location.getY(), location.getZ() + 2.0005);
    }
    
    public static boolean isOnGroundQ(final Player p) {
        return isOnGroundQ(p.getLocation());
    }
    
    public static boolean isOnGroundR(final Location location) {
        return blockThere(location.getWorld(), location.getX() - 1.002, location.getY() - getPossibleOffset(location), location.getZ() - 1.002, location.getX() + 1.002, location.getY() + 1.002, location.getZ() + 1.002);
    }
    
    public static boolean isOnGroundF(final Location location) {
        return blockThere(location.getWorld(), location.getX() - 0.9, location.getY() - getPossibleOffset(location), location.getZ() - 0.9, location.getX() + 0.9, location.getY() - 0.9, location.getZ() + 0.9);
    }
    
    public static boolean isOnGroundAccurate(final Location location, final Entity e) {
        return isOnGroundAccurate(location, e, 0.4);
    }
    
    public static boolean isOnGroundAccurate(final Location location, final Entity e, final double xzspeed) {
        final double f = xzspeed;
        return isOnGroundAccurate(location, e, f, false);
    }
    
    public static boolean isInsideStepableAccurate(final Location location) {
        final double f = 0.3;
        return validStepableBlockThere(location, location.getWorld(), location.getX() - f, location.getY() - getPossibleOffset(location) - 0.01, location.getZ() - f, location.getX() + f, location.getY() - 1.0, location.getZ() + f);
    }
    
    public static boolean hasWoopableBlockNearby(final Location l, final Entity e) {
        final BukkitEntityHitbox boundingBox = HitBoxService.getBoundingBox(e);
        final double width = boundingBox.getBoxWidth();
        final double height = boundingBox.getBoxHeight();
        final double dxz = Math.round(width * 500.0) / 1000.0;
        final double minX = l.getX() - dxz - 0.1;
        final double minY = l.getY();
        final double minZ = l.getZ() - dxz - 0.1;
        final double maxX = l.getX() + dxz + 0.1;
        final double maxY = l.getY() + height + 0.1;
        final double maxZ = l.getZ() + dxz + 0.1;
        return collidesWoopable(l.getWorld(), minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public static boolean isOnGroundAccurate(final Location location, final Entity e, final double f, final boolean h) {
        return validblockThere(location, location.getWorld(), location.getX() - f, location.getY() - getPossibleOffset(location), location.getZ() - f, location.getX() + f, location.getY() - 1.0, location.getZ() + f);
    }
    
    public static boolean isOnGroundLessAccurate(final Location location, final double size) {
        final double f = Math.max(0.3, size);
        return validblockThere(location, location.getWorld(), location.getX() - f, location.getY() - getPossibleOffset(location), location.getZ() - f, location.getX() + f, location.getY() - 1.0, location.getZ() + f);
    }
    
    public static boolean collidesStepableSimple(final Location from, final double size) {
        if (size < 0.0 || size > 1.0) {
            throw new IndexOutOfBoundsException("Size is greater than 1 or smaller than 0");
        }
        final Block[] blocks = { from.getBlock(), from.add(size, 0.0, 0.0).getBlock(), from.add(-size, 0.0, 0.0).getBlock(), from.add(0.0, 0.0, size).getBlock(), from.add(0.0, 0.0, -size).getBlock() };
        return Arrays.stream(blocks).anyMatch(BlockData::isSlabLikeBlock);
    }
    
    private static boolean blockThere(final World world, final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ) {
        final int iMinX = locToBlock(minX);
        final int iMaxX = locToBlock(maxX);
        final int iMinY = locToBlock(minY);
        final int iMaxY = locToBlock(maxY);
        final int iMinZ = locToBlock(minZ);
        final int iMaxZ = locToBlock(maxZ);
        for (int x = iMinX; iMaxX >= x; ++x) {
            for (int z = iMinZ; z <= iMaxZ; ++z) {
                for (int y = iMinY; y <= iMaxY; ++y) {
                    if (BlockData.doesAffectMovement(getBlock0(world, x, y, z))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private static boolean validblockThere(final Location playerLocation, final World world, final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ) {
        final int iMinX = locToBlock(minX);
        final int iMaxX = locToBlock(maxX);
        final int iMinY = locToBlock(minY);
        final int iMaxY = locToBlock(maxY);
        final int iMinZ = locToBlock(minZ);
        final int iMaxZ = locToBlock(maxZ);
        for (int x = iMinX; iMaxX >= x; ++x) {
            for (int z = iMinZ; z <= iMaxZ; ++z) {
                for (int y = iMinY; y <= iMaxY; ++y) {
                    if (BlockData.isInVoidHitbox(getBlock0(world, x, y, z), playerLocation) && (MaterialData.isDoor(getBlock0(world, x, y + 1, z).getType()) || BlockData.isPassable(getBlock0(world, x, y + 1, z)))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private static boolean validStepableBlockThere(final Location playerLocation, final World world, final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ) {
        final int iMinX = locToBlock(minX);
        final int iMaxX = locToBlock(maxX);
        final int iMinY = locToBlock(minY);
        final int iMaxY = locToBlock(maxY);
        final int iMinZ = locToBlock(minZ);
        final int iMaxZ = locToBlock(maxZ);
        for (int x = iMinX; iMaxX >= x; ++x) {
            for (int z = iMinZ; z <= iMaxZ; ++z) {
                for (int y = iMinY; y <= iMaxY; ++y) {
                    final Block b = getBlock0(world, x, y, z);
                    if (BlockData.isStepable(b, playerLocation) && BlockData.isPassable(getRelative(b, BlockFace.UP)) && !BlockData.isSlabLikeBlock(getRelative(b, BlockFace.UP))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private static boolean collidesAny(final World world, final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ) {
        final int iMinX = locToBlock(minX);
        final int iMaxX = locToBlock(maxX);
        final int iMinY = locToBlock(minY);
        final int iMaxY = locToBlock(maxY);
        final int iMinZ = locToBlock(minZ);
        final int iMaxZ = locToBlock(maxZ);
        for (int x = iMinX; x <= iMaxX; ++x) {
            for (int z = iMinZ; z <= iMaxZ; ++z) {
                for (int y = iMinY; y <= iMaxY; ++y) {
                    if (BlockData.doesAffectMovement(getBlock0(world, x, y, z))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private static boolean collidesAnyValid(final World world, final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ) {
        final int iMinX = locToBlock(minX);
        final int iMaxX = locToBlock(maxX);
        final int iMinY = locToBlock(minY);
        final int iMaxY = locToBlock(maxY);
        final int iMinZ = locToBlock(minZ);
        final int iMaxZ = locToBlock(maxZ);
        for (int x = iMinX; x <= iMaxX; ++x) {
            for (int z = iMinZ; z <= iMaxZ; ++z) {
                for (int y = iMinY; y <= iMaxY; ++y) {
                    if (BlockData.doesAffectMovement(getBlock0(world, x, y, z)) && BlockData.isPassable(getBlock0(world, x, y + 1, z))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private static boolean isInsideOf(final World world, final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ, final Location location) {
        final int iMinX = locToBlock(minX);
        final int iMaxX = locToBlock(maxX);
        final int iMinY = locToBlock(minY);
        final int iMaxY = locToBlock(maxY);
        final int iMinZ = locToBlock(minZ);
        final int iMaxZ = locToBlock(maxZ);
        for (int x = iMinX; x <= iMaxX; ++x) {
            for (int z = iMinZ; z <= iMaxZ; ++z) {
                for (int y = iMinY; y <= iMaxY; ++y) {
                    if (getBlock0(world, x, y, z).getLocation().distance(location) < 0.25) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private static boolean collidesACLiquit(final World world, final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ) {
        final int iMinX = locToBlock(minX);
        final int iMaxX = locToBlock(maxX);
        final int iMinY = locToBlock(minY);
        final int iMaxY = locToBlock(maxY);
        final int iMinZ = locToBlock(minZ);
        final int iMaxZ = locToBlock(maxZ);
        for (int x = iMinX; x <= iMaxX; ++x) {
            for (int z = iMinZ; z <= iMaxZ; ++z) {
                for (int y = iMinY; y <= iMaxY; ++y) {
                    final Block block = getBlock0(world, x, y, z);
                    if (!BlockData.isLiquid(block)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    private static boolean isCountableLiquit(final Block b) {
        return liquidOrSolid(getRelative(b, BlockFace.NORTH)) && liquidOrSolid(getRelative(b, BlockFace.SOUTH)) && liquidOrSolid(getRelative(b, BlockFace.EAST)) && liquidOrSolid(getRelative(b, BlockFace.WEST)) && liquidOrSolid(getRelative(b, BlockFace.SOUTH_WEST)) && liquidOrSolid(getRelative(b, BlockFace.SOUTH_EAST)) && liquidOrSolid(getRelative(b, BlockFace.NORTH_WEST)) && liquidOrSolid(getRelative(b, BlockFace.NORTH_EAST));
    }
    
    private static boolean liquidOrSolid(final Block b) {
        return b.isLiquid() || !BlockData.isPassable(b);
    }
    
    private static boolean collides(final World world, final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ, final Material material) {
        final int iMinX = locToBlock(minX);
        final int iMaxX = locToBlock(maxX);
        final int iMinY = locToBlock(minY);
        final int iMaxY = locToBlock(maxY);
        final int iMinZ = locToBlock(minZ);
        final int iMaxZ = locToBlock(maxZ);
        for (int x = iMinX; x <= iMaxX; ++x) {
            for (int z = iMinZ; z <= iMaxZ; ++z) {
                for (int y = iMinY; y <= iMaxY; ++y) {
                    if (material.equals((Object)getBlock0(world, x, y, z).getType())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private static boolean collidesSolidMotionRelevant(final World world, final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ) {
        final int iMinX = locToBlock(minX);
        final int iMaxX = locToBlock(maxX);
        final int iMinY = locToBlock(minY);
        final int iMaxY = locToBlock(maxY);
        final int iMinZ = locToBlock(minZ);
        final int iMaxZ = locToBlock(maxZ);
        for (int x = iMinX; x <= iMaxX; ++x) {
            for (int z = iMinZ; z <= iMaxZ; ++z) {
                for (int y = iMinY; y <= iMaxY; ++y) {
                    if (BlockData.doesAffectMovement(getBlock0(world, x, y, z)) && !BlockData.isLiquid(getBlock0(world, x, y, z))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private static boolean collides(final World world, final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ, final Material... materials) {
        final int iMinX = locToBlock(minX);
        final int iMaxX = locToBlock(maxX);
        final int iMinY = locToBlock(minY);
        final int iMaxY = locToBlock(maxY);
        final int iMinZ = locToBlock(minZ);
        final int iMaxZ = locToBlock(maxZ);
        for (int x = iMinX; x <= iMaxX; ++x) {
            for (int z = iMinZ; z <= iMaxZ; ++z) {
                for (int y = iMinY; y <= iMaxY; ++y) {
                    final Material mat = getBlock0(world, x, y, z).getType();
                    for (final Material material : materials) {
                        if (material.equals((Object)mat)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private static boolean collidesWoopable(final World world, final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ) {
        final int iMinX = locToBlock(minX);
        final int iMaxX = locToBlock(maxX);
        final int iMinY = locToBlock(minY);
        final int iMaxY = locToBlock(maxY);
        final int iMinZ = locToBlock(minZ);
        final int iMaxZ = locToBlock(maxZ);
        for (int x = iMinX; x <= iMaxX; ++x) {
            for (int z = iMinZ; z <= iMaxZ; ++z) {
                for (int y = iMinY; y <= iMaxY; ++y) {
                    if (BlockData.isWoopableBlock(getBlock0(world, x, y, z))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private static boolean collidesUnpassable(final World world, final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ) {
        final int iMinX = locToBlock(minX);
        final int iMaxX = locToBlock(maxX);
        final int iMinY = locToBlock(minY);
        final int iMaxY = locToBlock(maxY);
        final int iMinZ = locToBlock(minZ);
        final int iMaxZ = locToBlock(maxZ);
        for (int x = iMinX; x <= iMaxX; ++x) {
            for (int z = iMinZ; z <= iMaxZ; ++z) {
                for (int y = iMinY; y <= iMaxY; ++y) {
                    if (!BlockData.isPassable(getBlock0(world, x, y, z))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private static boolean collidesMotionRelevant(final World world, final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ) {
        final int iMinX = locToBlock(minX);
        final int iMaxX = locToBlock(maxX);
        final int iMinY = locToBlock(minY);
        final int iMaxY = locToBlock(maxY);
        final int iMinZ = locToBlock(minZ);
        final int iMaxZ = locToBlock(maxZ);
        for (int x = iMinX; x <= iMaxX; ++x) {
            for (int z = iMinZ; z <= iMaxZ; ++z) {
                for (int y = iMinY; y <= iMaxY; ++y) {
                    if (BlockData.doesAffectMovementSimple(getBlock0(world, x, y, z))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private static boolean collidesIntStepable(final World world, final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ) {
        final int iMinX = locToBlock(minX);
        final int iMaxX = locToBlock(maxX);
        final int iMinY = locToBlock(minY);
        final int iMaxY = locToBlock(maxY);
        final int iMinZ = locToBlock(minZ);
        final int iMaxZ = locToBlock(maxZ);
        for (int x = iMinX; x <= iMaxX; ++x) {
            for (int z = iMinZ; z <= iMaxZ; ++z) {
                for (int y = iMinY; y <= iMaxY; ++y) {
                    if (BlockData.doesAffectMovement(getBlock0(world, x, y, z))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private static Block getUnpassable(final World world, final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ) {
        final int iMinX = locToBlock(minX);
        final int iMaxX = locToBlock(maxX);
        final int iMinY = locToBlock(minY);
        final int iMaxY = locToBlock(maxY);
        final int iMinZ = locToBlock(minZ);
        final int iMaxZ = locToBlock(maxZ);
        for (int x = iMinX; x <= iMaxX; ++x) {
            for (int z = iMinZ; z <= iMaxZ; ++z) {
                for (int y = iMinY; y <= iMaxY; ++y) {
                    if (!BlockData.isPassable(getBlock0(world, x, y, z))) {
                        return getBlock0(world, x, y, z);
                    }
                }
            }
        }
        return null;
    }
    
    private static boolean hasHeapSpace(final World world, final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ, final Material... normal) {
        final int iMinX = locToBlock(minX);
        final int iMaxX = locToBlock(maxX);
        final int iMinY = locToBlock(minY);
        final int iMaxY = locToBlock(maxY);
        final int iMinZ = locToBlock(minZ);
        final int iMaxZ = locToBlock(maxZ);
        boolean c = false;
        for (int x = iMinX; x <= iMaxX; ++x) {
            for (int z = iMinZ; z <= iMaxZ; ++z) {
                for (int y = iMinY; y <= iMaxY; ++y) {
                    boolean d = false;
                    for (final Material material : normal) {
                        if (getBlock0(world, x, y, z).getType().equals((Object)material) && !d) {
                            d = true;
                            c = true;
                        }
                    }
                    if (!d) {
                        return false;
                    }
                }
            }
        }
        return c;
    }
    
    public static boolean couldCollide(final Location l) {
        return couldCollideAtBlock(l) || couldCollideAtBlock(getRelative(l.getBlock(), BlockFace.UP).getLocation()) || couldCollideAtBlock(getRelative(l.getBlock(), BlockFace.UP).getRelative(BlockFace.UP).getLocation());
    }
    
    private static boolean couldCollideAtBlock(final Location l) {
        return BlockData.doesAffectMovement(getRelative(l.getBlock(), BlockFace.NORTH)) || BlockData.doesAffectMovement(getRelative(l.getBlock(), BlockFace.SOUTH)) || BlockData.doesAffectMovement(getRelative(l.getBlock(), BlockFace.WEST)) || BlockData.doesAffectMovement(getRelative(l.getBlock(), BlockFace.EAST)) || BlockData.doesAffectMovement(getRelative(l.getBlock(), BlockFace.NORTH_WEST)) || BlockData.doesAffectMovement(getRelative(l.getBlock(), BlockFace.NORTH_EAST)) || BlockData.doesAffectMovement(getRelative(l.getBlock(), BlockFace.SOUTH_EAST)) || BlockData.doesAffectMovement(getRelative(l.getBlock(), BlockFace.SOUTH_WEST));
    }
    
    public static boolean hasStepableNearby(final Player p, final Location location) {
        return collidesStepable(p.getWorld(), (Entity)p, location) || collidesStepable(p.getWorld(), (Entity)p, location.clone().subtract(0.0, 0.5, 0.0));
    }
    
    public static boolean hasStepableNearbyITB(final Player p, final Location location) {
        return collidesStepableIgnoringTopBlock(p.getWorld(), (Entity)p, location) || collidesStepableIgnoringTopBlock(p.getWorld(), (Entity)p, getRelative(location.getBlock(), BlockFace.DOWN).getLocation());
    }
    
    public static boolean hasFullStepableNearbyITB(final Location location) {
        return hasStepableNoGroundNearby(location) && hasStepableNoGroundNearby(getRelative(location.getBlock(), BlockFace.DOWN).getLocation());
    }
    
    public static double getPossibleOffset(final Location location) {
        final Block under = getRelative(getBlockAt(location), BlockFace.DOWN);
        double max = 0.0;
        max = Math.max(max, BlockData.getYOffset(getBlockAt(location)));
        max = Math.max(max, BlockData.getYOffset(under));
        max = Math.max(max, BlockData.getYOffset(getRelative(under, BlockFace.NORTH)));
        max = Math.max(max, BlockData.getYOffset(getRelative(under, BlockFace.EAST)));
        max = Math.max(max, BlockData.getYOffset(getRelative(under, BlockFace.SOUTH)));
        max = Math.max(max, BlockData.getYOffset(getRelative(under, BlockFace.WEST)));
        max = Math.max(max, BlockData.getYOffset(getRelative(under, BlockFace.NORTH_WEST)));
        max = Math.max(max, BlockData.getYOffset(getRelative(under, BlockFace.NORTH_EAST)));
        max = Math.max(max, BlockData.getYOffset(getRelative(under, BlockFace.SOUTH_EAST)));
        max = Math.max(max, BlockData.getYOffset(getRelative(under, BlockFace.SOUTH_WEST)));
        return max;
    }
    
    public static Block getRelative(final Block block, final BlockFace blockFace) {
        final Block block2 = getRelative0(block.getWorld(), block, blockFace);
        return (block2 == null) ? block : block2;
    }
    
    public static Block getBlockAt(final Location location) {
        return getBlock0(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
    
    private static Block getRelative0(final World world, final Block origin, final BlockFace face) {
        return getRelative0(world, origin, face, 1);
    }
    
    private static Block getRelative0(final World world, final Block origin, final BlockFace face, final int distance) {
        return getRelative0(world, origin, face.getModX() * distance, face.getModY() * distance, face.getModZ() * distance);
    }
    
    private static Block getRelative0(final World world, final Block origin, final int modX, final int modY, final int modZ) {
        return isInLoadedChunk(world, origin.getX() + modX, origin.getZ() + modZ) ? world.getBlockAt(origin.getX() + modX, origin.getY() + modY, origin.getZ() + modZ) : null;
    }
    
    private static Block getBlock0(final World world, final int x, final int y, final int z) {
        if (isInLoadedChunk(world, x, z)) {
            if (LocationHelper.deadRequestBlock != null) {
                return world.getBlockAt(x, y, z);
            }
            LocationHelper.deadRequestBlock = world.getBlockAt(x, y, z);
        }
        return LocationHelper.deadRequestBlock;
    }
    
    private static int locToBlock(final double positionAxe) {
        final int floor = (int)positionAxe;
        return (floor == positionAxe) ? floor : (floor - (int)(Double.doubleToRawLongBits(positionAxe) >>> 63));
    }
    
    public static boolean isInLoadedChunk(final World world, final int x, final int z) {
        return world.isChunkLoaded(x >> 4, z >> 4);
    }
    
    public static void randomizeRotations(final Location l) {
        l.setYaw((float)ThreadLocalRandom.current().nextInt(-179, 179));
        l.setPitch((float)ThreadLocalRandom.current().nextInt(-89, 89));
    }
    
    public static double getDistanceSafe(final Location from, final Location to) {
        return (from == null || to == null) ? Double.MAX_VALUE : (from.getWorld().getUID().equals(to.getWorld().getUID()) ? from.distance(to) : Double.MAX_VALUE);
    }
    
    public static double distance(final Location from, final double toX, final double toY, final double toZ) {
        return Math.sqrt(distanceSquared(from, toX, toY, toZ));
    }
    
    public static double distanceSquared(final Location from, final double toX, final double toY, final double toZ) {
        if (from == null) {
            throw new IllegalArgumentException("Cannot measure distance to a null location");
        }
        return NumberConversions.square(from.getX() - toX) + NumberConversions.square(from.getY() - toY) + NumberConversions.square(from.getZ() - toZ);
    }
}
