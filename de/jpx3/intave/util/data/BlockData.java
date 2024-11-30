// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.data;

import de.jpx3.intave.antipiracy.IIUA;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import de.jpx3.intave.module.CrossVersionSupply;
import org.bukkit.Location;
import org.bukkit.block.Block;

public final class BlockData
{
    public static boolean isStepable(final Block b, final Location locationTo) {
        return CrossVersionSupply.getCurrentCFR().getBlockData().isStepable(b) && (isSlabLikeBlock(b) ? isPassable(b.getRelative(BlockFace.UP)) : (!CrossVersionSupply.getCurrentCFR().getBlockData().isStepable(b.getRelative(BlockFace.UP))));
    }
    
    public static boolean isClimbable(final Block b) {
        return CrossVersionSupply.getCurrentCFR().getBlockData().isClimbable(b);
    }
    
    private static boolean vHR(final Block b, final Location locationTo, final boolean downCart) {
        double height = -1.0;
        if (b.getType().equals((Object)Material.STONE_SLAB2)) {
            final int data = IIUA.getDataFromBlock(b);
            if (data == 0) {
                height = 0.75;
            }
        }
        else if (b.getType().equals((Object)Material.SNOW)) {
            height = getSnowLayerOffset(b) + 0.25;
        }
        else if (b.getType().equals((Object)Material.BED_BLOCK)) {
            height = 0.5625;
        }
        return height < 0.0 || (downCart && locationTo.getY() < b.getY() + height) || (locationTo.getY() <= b.getY() + height && locationTo.getY() >= b.getY());
    }
    
    public static boolean isInVoidHitbox(final Block b, final Location locationTo) {
        return doesAffectMovement(b);
    }
    
    public static boolean isWoopableBlock(final Block b) {
        final String name = b.getType().name();
        switch (name) {
            case "SOUL_SAND":
            case "GRASS_PATH": {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public static boolean doesAffectMovement(final Block b) {
        return (!CrossVersionSupply.getCurrentCFR().getBlockData().isStepable(b) || !CrossVersionSupply.getCurrentCFR().getBlockData().isStepable(b.getRelative(BlockFace.UP)) || isSlabLikeBlock(b)) && doesAffectMovementSimple(b);
    }
    
    public static boolean doesAffectMovementSimple(final Block b) {
        return CrossVersionSupply.getCurrentCFR().getBlockData().doesAffectMovement(b);
    }
    
    public static boolean isPassable(final Block b) {
        return CrossVersionSupply.getCurrentCFR().getBlockData().isPassable(b);
    }
    
    public static boolean isLiquid(final Block b) {
        return CrossVersionSupply.getCurrentCFR().getBlockData().isLiquid(b);
    }
    
    public static boolean isSlabLikeBlock(final Block b) {
        final String typeName = b.getType().name();
        return (typeName.contains("SLAB") && !typeName.contains("DOUBLE")) || typeName.contains("STAIRS") || typeName.contains("STEP");
    }
    
    public static boolean isFrozen(final Block b) {
        return CrossVersionSupply.getCurrentCFR().getBlockData().isFrozen(b);
    }
    
    public static boolean hasYOffset(final Block b) {
        return getYOffset(b) != 0.0625;
    }
    
    private static double getSnowLayerOffset(final Block b) {
        return b.getType().equals((Object)Material.SNOW) ? ((IIUA.getDataFromBlock(b) - 1) * 0.125) : 0.0;
    }
    
    public static double getYOffset(final Block b) {
        return CrossVersionSupply.getCurrentCFR().getBlockData().getYOffset(b);
    }
}
