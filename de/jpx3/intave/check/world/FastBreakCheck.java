// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.world;

import de.jpx3.intave.util.calc.MathHelper;
import org.bukkit.enchantments.Enchantment;
import de.jpx3.intave.util.data.ToolHardness;
import org.bukkit.inventory.ItemStack;
import java.util.Iterator;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.GameMode;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;
import de.jpx3.intave.antipiracy.IIUA;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import java.util.concurrent.ConcurrentHashMap;
import de.jpx3.intave.util.enums.CheatCategory;
import org.bukkit.Material;
import java.util.Map;
import de.jpx3.intave.util.objectable.BlockCache;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.event.Listener;
import de.jpx3.intave.util.objectable.IntaveCheck;

public final class FastBreakCheck extends IntaveCheck implements Listener
{
    private final IntavePlugin plugin;
    private final BlockCache blockCache;
    private final Map<Material, Material> combinedFast;
    
    public FastBreakCheck(final IntavePlugin plugin) {
        super("FastBreak", CheatCategory.WORLD);
        this.blockCache = new BlockCache();
        this.combinedFast = new ConcurrentHashMap<Material, Material>();
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this.plugin);
        this.setupCombinedFast();
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockClick(final PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        if (e.getAction().equals((Object)Action.LEFT_CLICK_BLOCK)) {
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeLeftClickedBlock = System.currentTimeMillis();
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(final BlockBreakEvent e) {
        final Player p = e.getPlayer();
        if (!this.isActivated() || p.getGameMode().equals((Object)GameMode.CREATIVE)) {
            return;
        }
        double hastePotionLevel = 0.0;
        for (final PotionEffect f : p.getActivePotionEffects()) {
            if (f.getType().equals((Object)PotionEffectType.FAST_DIGGING)) {
                hastePotionLevel = f.getAmplifier() + 1;
            }
        }
        long breakDuration = IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeLeftClickedBlock;
        final long minimumBreakDuration = this.calculateBreakDuration(e.getPlayer().getItemInHand(), e.getBlock().getType(), hastePotionLevel);
        final int tickLeniency = (int)(minimumBreakDuration / 50.0 * 0.25);
        if (minimumBreakDuration > 8000L) {
            breakDuration += 2000L;
        }
        if (breakDuration + tickLeniency * 50 < minimumBreakDuration) {
            int vl = 1;
            if (breakDuration < 10L && minimumBreakDuration > 250L) {
                vl += 4;
            }
            if (minimumBreakDuration > 750L) {
                ++vl;
            }
            if (minimumBreakDuration > 1000L) {
                vl += 2;
            }
            if (minimumBreakDuration > 1500L) {
                vl += 4;
            }
            if (minimumBreakDuration > 2000L) {
                vl += 4;
            }
            if (this.plugin.getRetributionManager().markPlayer(p, vl, "FastBreak", CheatCategory.WORLD, "broke a " + e.getBlock().getType().name().toLowerCase() + " block too quickly (" + (int)(breakDuration / 50L) + " ticks out of " + (int)(minimumBreakDuration / 50L) + ")")) {
                e.setCancelled(true);
            }
        }
    }
    
    private void setupCombinedFast() {
        this.combinedFast.put(Material.SHEARS, Material.WOOL);
        this.combinedFast.put(Material.IRON_SWORD, Material.WEB);
        this.combinedFast.put(Material.DIAMOND_SWORD, Material.WEB);
        this.combinedFast.put(Material.STONE_SWORD, Material.WEB);
        this.combinedFast.put(Material.WOOD_SWORD, Material.WEB);
    }
    
    private long calculateBreakDuration(ItemStack tool, final Material block, final double hasteLevel) {
        if (tool == null) {
            tool = new ItemStack(Material.AIR);
        }
        if (tool.getType().equals((Object)Material.SHEARS) && block.equals((Object)Material.LEAVES)) {
            return 0L;
        }
        final double bhardness = this.blockCache.getBlockCacheEntry(block).getBlockHardness() * ((hasteLevel < 0.0) ? 1.0 : Math.max(1.0 - 0.2 * hasteLevel, 0.0));
        final double thardness = ToolHardness.getToolHardness(tool.getType());
        final long enchantmentLevel = tool.getEnchantmentLevel(Enchantment.DIG_SPEED);
        if (bhardness < 0.01) {
            return 0L;
        }
        final long result = MathHelper.minmax(Math.round(bhardness * thardness * 1000.0), 0L, 25000L) / ((enchantmentLevel > 0L) ? (enchantmentLevel * enchantmentLevel + 2L) : 1L);
        return this.isQuickCombo(tool, block) ? (result / 4L) : result;
    }
    
    private boolean isQuickCombo(final ItemStack tool, final Material block) {
        return this.combinedFast.keySet().stream().anyMatch(t -> tool.getType().equals((Object)t) && this.combinedFast.get(t).equals((Object)block));
    }
}
