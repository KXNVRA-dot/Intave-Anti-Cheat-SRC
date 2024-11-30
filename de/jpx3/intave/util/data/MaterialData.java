// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.data;

import org.bukkit.entity.Player;
import org.bukkit.Material;

public class MaterialData
{
    public static synchronized boolean isValidFood(final Material m, final Player p) {
        return m.equals((Object)Material.MILK_BUCKET) || m.equals((Object)Material.POTION) || m.equals((Object)Material.GOLDEN_APPLE) || (m.isEdible() && p.getFoodLevel() < 20);
    }
    
    public static synchronized boolean isLeaves(final Material material) {
        switch (material) {
            case LEAVES:
            case LEAVES_2: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public static synchronized boolean isGlas(final Material material) {
        switch (material) {
            case GLASS:
            case STAINED_GLASS:
            case STAINED_GLASS_PANE:
            case THIN_GLASS: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public static synchronized boolean isIce(final Material material) {
        switch (material) {
            case ICE:
            case PACKED_ICE: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public static synchronized boolean isChest(final Material material) {
        switch (material) {
            case CHEST:
            case ENDER_CHEST:
            case TRAPPED_CHEST: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public static synchronized boolean isDoor(final Material material) {
        switch (material) {
            case DARK_OAK_DOOR:
            case ACACIA_DOOR:
            case BIRCH_DOOR:
            case IRON_DOOR:
            case JUNGLE_DOOR:
            case SPRUCE_DOOR:
            case WOODEN_DOOR:
            case WOOD_DOOR:
            case IRON_DOOR_BLOCK: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public static synchronized boolean isGate(final Material material) {
        switch (material) {
            case ACACIA_FENCE_GATE:
            case BIRCH_FENCE_GATE:
            case DARK_OAK_FENCE_GATE:
            case FENCE_GATE:
            case JUNGLE_FENCE_GATE:
            case SPRUCE_FENCE_GATE: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
}
