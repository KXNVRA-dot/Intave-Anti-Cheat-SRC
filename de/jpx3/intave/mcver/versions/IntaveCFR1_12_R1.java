// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.mcver.versions;

import org.bukkit.Location;
import org.bukkit.Material;
import de.jpx3.intave.antipiracy.IIUA;
import de.jpx3.intave.util.data.MaterialData;
import org.bukkit.block.Block;
import de.jpx3.intave.mcver.iBlockData;
import de.jpx3.intave.mcver.VersionCFR;

public class IntaveCFR1_12_R1 implements VersionCFR
{
    private iBlockData blockData;
    
    public IntaveCFR1_12_R1() {
        this.loadBlockData();
    }
    
    private void loadBlockData() {
        this.blockData = new iBlockData() {
            @Override
            public boolean isStepable(final Block b) {
                if (MaterialData.isGate(b.getType()) && IIUA.getDataFromBlock(b) < 4) {
                    return true;
                }
                switch (b.getType()) {
                    case WATER_LILY:
                    case HOPPER:
                    case CAULDRON:
                    case DAYLIGHT_DETECTOR:
                    case DAYLIGHT_DETECTOR_INVERTED:
                    case IRON_TRAPDOOR:
                    case TRAP_DOOR:
                    case BREWING_STAND:
                    case COCOA:
                    case CAKE:
                    case CAKE_BLOCK:
                    case SKULL:
                    case ENCHANTMENT_TABLE:
                    case ENDER_PORTAL_FRAME:
                    case SNOW:
                    case CARPET:
                    case PISTON_MOVING_PIECE:
                    case PISTON_EXTENSION:
                    case BED_BLOCK:
                    case STEP:
                    case WOOD_STEP:
                    case STONE_SLAB2:
                    case DOUBLE_STONE_SLAB2:
                    case ACACIA_STAIRS:
                    case SANDSTONE_STAIRS:
                    case SMOOTH_STAIRS:
                    case SPRUCE_WOOD_STAIRS:
                    case BIRCH_WOOD_STAIRS:
                    case BRICK_STAIRS:
                    case COBBLESTONE_STAIRS:
                    case DARK_OAK_STAIRS:
                    case JUNGLE_WOOD_STAIRS:
                    case NETHER_BRICK_STAIRS:
                    case QUARTZ_STAIRS:
                    case RED_SANDSTONE_STAIRS:
                    case FENCE:
                    case ACACIA_FENCE:
                    case BIRCH_FENCE:
                    case DARK_OAK_FENCE:
                    case IRON_FENCE:
                    case JUNGLE_FENCE:
                    case NETHER_FENCE:
                    case SPRUCE_FENCE:
                    case CHEST:
                    case TRAPPED_CHEST:
                    case ENDER_CHEST:
                    case FLOWER_POT:
                    case PORTAL:
                    case WOOD_STAIRS:
                    case COBBLE_WALL:
                    case REDSTONE_COMPARATOR:
                    case REDSTONE_COMPARATOR_ON:
                    case REDSTONE_COMPARATOR_OFF:
                    case DIODE:
                    case DIODE_BLOCK_ON:
                    case DIODE_BLOCK_OFF: {
                        return true;
                    }
                    default: {
                        final String name = b.getType().name();
                        switch (name) {
                            case "PURPUR_STAIRS":
                            case "PURPUR_SLAB":
                            case "PURPUR_PILLAR":
                            case "CHORUS_PLANT": {
                                return true;
                            }
                            default: {
                                return false;
                            }
                        }
                        break;
                    }
                }
            }
            
            @Override
            public boolean isClimbable(final Block b) {
                return b.getType().equals((Object)Material.LADDER) || b.getType().equals((Object)Material.VINE);
            }
            
            private boolean vHR(final Block b, final Location locationTo, final boolean downCart) {
                double height = -1.0;
                if (b.getType().equals((Object)Material.STONE_SLAB2)) {
                    final int data = IIUA.getDataFromBlock(b);
                    if (data == 0) {
                        height = 0.75;
                    }
                }
                else if (b.getType().equals((Object)Material.SNOW)) {
                    height = this.getSnowLayerOffset(b) + 0.25;
                }
                else if (b.getType().equals((Object)Material.BED_BLOCK)) {
                    height = 0.5625;
                }
                return height < 0.0 || (downCart && locationTo.getY() < b.getY() + height) || (locationTo.getY() <= b.getY() + height && locationTo.getY() >= b.getY());
            }
            
            public boolean isInVoidHitbox(final Block b, final Location locationTo) {
                return this.doesAffectMovement(b);
            }
            
            @Override
            public boolean doesAffectMovement(final Block b) {
                if (b.isLiquid()) {
                    return true;
                }
                if (MaterialData.isGate(b.getType())) {
                    return false;
                }
                switch (b.getType()) {
                    case CARPET:
                    case PORTAL:
                    case BROWN_MUSHROOM:
                    case RAILS:
                    case ACTIVATOR_RAIL:
                    case DETECTOR_RAIL:
                    case POWERED_RAIL:
                    case AIR:
                    case RED_MUSHROOM:
                    case RED_ROSE:
                    case TORCH:
                    case STONE_BUTTON:
                    case WALL_SIGN:
                    case SIGN_POST:
                    case IRON_PLATE:
                    case GOLD_PLATE:
                    case FIRE:
                    case LONG_GRASS:
                    case SEEDS:
                    case MELON_SEEDS:
                    case YELLOW_FLOWER:
                    case DOUBLE_PLANT:
                    case TRIPWIRE:
                    case TRIPWIRE_HOOK:
                    case PUMPKIN_SEEDS:
                    case DEAD_BUSH:
                    case WOOD_PLATE:
                    case STONE_PLATE:
                    case CARROT:
                    case WOOD_BUTTON:
                    case BANNER:
                    case WHEAT: {
                        return false;
                    }
                    default: {
                        return true;
                    }
                }
            }
            
            @Override
            public boolean isPassable(final Block b) {
                if (b.isLiquid()) {
                    return true;
                }
                switch (b.getType()) {
                    case WATER_LILY:
                    case HOPPER:
                    case CAULDRON:
                    case DAYLIGHT_DETECTOR:
                    case DAYLIGHT_DETECTOR_INVERTED:
                    case IRON_TRAPDOOR:
                    case TRAP_DOOR:
                    case BREWING_STAND:
                    case COCOA:
                    case CAKE_BLOCK:
                    case SKULL:
                    case ENCHANTMENT_TABLE:
                    case ENDER_PORTAL_FRAME:
                    case SNOW:
                    case CARPET:
                    case PISTON_MOVING_PIECE:
                    case PISTON_EXTENSION:
                    case BED_BLOCK:
                    case STEP:
                    case WOOD_STEP:
                    case STONE_SLAB2:
                    case ACACIA_STAIRS:
                    case SANDSTONE_STAIRS:
                    case SMOOTH_STAIRS:
                    case SPRUCE_WOOD_STAIRS:
                    case BIRCH_WOOD_STAIRS:
                    case BRICK_STAIRS:
                    case COBBLESTONE_STAIRS:
                    case DARK_OAK_STAIRS:
                    case JUNGLE_WOOD_STAIRS:
                    case NETHER_BRICK_STAIRS:
                    case QUARTZ_STAIRS:
                    case RED_SANDSTONE_STAIRS:
                    case FENCE:
                    case ACACIA_FENCE:
                    case BIRCH_FENCE:
                    case DARK_OAK_FENCE:
                    case IRON_FENCE:
                    case JUNGLE_FENCE:
                    case NETHER_FENCE:
                    case SPRUCE_FENCE:
                    case CHEST:
                    case TRAPPED_CHEST:
                    case ENDER_CHEST:
                    case FLOWER_POT:
                    case PORTAL:
                    case WOOD_STAIRS:
                    case COBBLE_WALL:
                    case REDSTONE_COMPARATOR:
                    case REDSTONE_COMPARATOR_ON:
                    case REDSTONE_COMPARATOR_OFF:
                    case DIODE_BLOCK_ON:
                    case DIODE_BLOCK_OFF:
                    case BROWN_MUSHROOM:
                    case RAILS:
                    case ACTIVATOR_RAIL:
                    case DETECTOR_RAIL:
                    case POWERED_RAIL:
                    case AIR:
                    case RED_MUSHROOM:
                    case RED_ROSE:
                    case TORCH:
                    case STONE_BUTTON:
                    case WALL_SIGN:
                    case SIGN_POST:
                    case IRON_PLATE:
                    case GOLD_PLATE:
                    case FIRE:
                    case LONG_GRASS:
                    case SEEDS:
                    case MELON_SEEDS:
                    case YELLOW_FLOWER:
                    case DOUBLE_PLANT:
                    case TRIPWIRE:
                    case TRIPWIRE_HOOK:
                    case PUMPKIN_SEEDS:
                    case DEAD_BUSH:
                    case WOOD_PLATE:
                    case STONE_PLATE:
                    case CARROT:
                    case WOOD_BUTTON:
                    case BANNER:
                    case WHEAT:
                    case POTATO:
                    case SAND:
                    case GRAVEL:
                    case TNT:
                    case SOUL_SAND:
                    case SLIME_BLOCK:
                    case WEB:
                    case THIN_GLASS:
                    case CROPS:
                    case MELON_STEM:
                    case PUMPKIN_STEM:
                    case ACACIA_DOOR:
                    case SIGN:
                    case PISTON_STICKY_BASE:
                    case PISTON_BASE:
                    case LEVER:
                    case IRON_DOOR:
                    case JUNGLE_DOOR:
                    case SPRUCE_DOOR:
                    case WOODEN_DOOR:
                    case REDSTONE_ORE:
                    case NETHER_WARTS:
                    case STAINED_GLASS_PANE:
                    case STRING:
                    case BED:
                    case REDSTONE_WIRE:
                    case IRON_DOOR_BLOCK:
                    case WOOD_DOOR:
                    case ARMOR_STAND:
                    case BIRCH_DOOR:
                    case SUGAR_CANE_BLOCK:
                    case STANDING_BANNER:
                    case SAPLING:
                    case CAULDRON_ITEM:
                    case DARK_OAK_DOOR:
                    case DARK_OAK_FENCE_GATE:
                    case DRAGON_EGG:
                    case BLAZE_POWDER:
                    case CACTUS:
                    case FENCE_GATE:
                    case ENDER_PORTAL:
                    case GLOWSTONE_DUST:
                    case LADDER:
                    case ANVIL:
                    case HUGE_MUSHROOM_1:
                    case HUGE_MUSHROOM_2:
                    case WOOD_DOUBLE_STEP:
                    case VINE:
                    case WALL_BANNER:
                    case REDSTONE_TORCH_ON:
                    case REDSTONE_TORCH_OFF:
                    case REDSTONE:
                    case ACACIA_FENCE_GATE:
                    case BIRCH_FENCE_GATE:
                    case JUNGLE_FENCE_GATE:
                    case SPRUCE_FENCE_GATE: {
                        return true;
                    }
                    default: {
                        final String name = b.getType().name();
                        switch (name) {
                            case "PURPUR_STAIRS":
                            case "PURPUR_SLAB":
                            case "PURPUR_PILLAR":
                            case "CHORUS_PLANT":
                            case "GRASS_PATH": {
                                return true;
                            }
                            default: {
                                return false;
                            }
                        }
                        break;
                    }
                }
            }
            
            @Override
            public boolean isLiquid(final Block b) {
                return b.isLiquid();
            }
            
            @Override
            public boolean isFrozen(final Block b) {
                return MaterialData.isIce(b.getType());
            }
            
            public boolean hasYOffset(final Block b) {
                return this.getYOffset(b) != 0.0625;
            }
            
            public double getSnowLayerOffset(final Block b) {
                return b.getType().equals((Object)Material.SNOW) ? ((IIUA.getDataFromBlock(b) - 1) * 0.125) : 0.0;
            }
            
            @Override
            public double getYOffset(final Block b) {
                if (b.getType().equals((Object)Material.SNOW)) {
                    return this.getSnowLayerOffset(b);
                }
                switch (b.getType()) {
                    case CAKE:
                    case CAKE_BLOCK:
                    case FENCE:
                    case ACACIA_FENCE:
                    case BIRCH_FENCE:
                    case DARK_OAK_FENCE:
                    case IRON_FENCE:
                    case JUNGLE_FENCE:
                    case NETHER_FENCE:
                    case SPRUCE_FENCE:
                    case COBBLE_WALL:
                    case DARK_OAK_FENCE_GATE:
                    case FENCE_GATE:
                    case ACACIA_FENCE_GATE:
                    case BIRCH_FENCE_GATE:
                    case JUNGLE_FENCE_GATE:
                    case SPRUCE_FENCE_GATE: {
                        return 0.62;
                    }
                    case SOUL_SAND: {
                        return 0.25;
                    }
                    case BED_BLOCK: {
                        return 0.5625;
                    }
                    case SKULL: {
                        return 0.25;
                    }
                    case WATER_LILY: {
                        return 0.09375;
                    }
                    default: {
                        return 0.0625;
                    }
                }
            }
        };
    }
    
    @Override
    public iBlockData getBlockData() {
        return this.blockData;
    }
    
    @Override
    public String getName() {
        return "v1_12_R1";
    }
    
    @Override
    public boolean isAllowedToFly(final Object player) {
        return false;
    }
    
    @Override
    public boolean isAllowedToPassBlocks(final Object player) {
        return false;
    }
}
