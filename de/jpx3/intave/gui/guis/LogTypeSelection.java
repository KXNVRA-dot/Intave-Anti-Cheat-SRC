// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.gui.guis;

import java.util.stream.IntStream;
import org.bukkit.inventory.ItemStack;
import de.jpx3.intave.api.internal.item.ItemAPI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.Bukkit;
import de.jpx3.intave.util.objectable.Checkable;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.inventory.Inventory;
import de.jpx3.intave.gui.iGui;

public class LogTypeSelection implements iGui
{
    private static Inventory inventory;
    
    @Override
    public String getName() {
        return IntavePlugin.getStaticReference().getPrefix() + "Log Options";
    }
    
    @Override
    public Inventory getInventory(final Checkable checkable) {
        this.fill(LogTypeSelection.inventory = Bukkit.createInventory((InventoryHolder)null, 27, this.getName()), ItemAPI.getItem(Material.STAINED_GLASS_PANE, ChatColor.BLACK + "", 1, new String[0], (short)15));
        LogTypeSelection.inventory.setItem(11, ItemAPI.getItem(Material.BOOK, ChatColor.WHITE + "Logged commands", 1, new String[] { "All commands executed and logged by intave " }));
        LogTypeSelection.inventory.setItem(13, ItemAPI.getItem(Material.REDSTONE_BLOCK, ChatColor.RED + "Logged bans", 1, new String[] { "Lists all logged commands containing 'ban' but not a 'snotify',", "by date" }));
        LogTypeSelection.inventory.setItem(15, ItemAPI.getItem(Material.IRON_BLOCK, ChatColor.GRAY + "Logged kicks", 1, new String[] { "Lists all logged commands containing 'kick' but not a 'snotify',", "by date" }));
        return LogTypeSelection.inventory;
    }
    
    private void fill(final Inventory inventory, final ItemStack toFill, final int start) {
        IntStream.range(start, inventory.getSize()).forEachOrdered(i -> inventory.setItem(i, toFill));
    }
    
    private void fill(final Inventory inventory, final ItemStack toFill) {
        this.fill(inventory, toFill, 0);
    }
    
    @Override
    public void onItemStackInteract(final Checkable checkable, final ItemStack itemStack, final int slot) {
        if (itemStack.getType().equals((Object)Material.BOOK)) {
            IntavePlugin.getStaticReference().getGuiHandler().openGUI(checkable, new LogsOverview(0));
        }
    }
    
    @Override
    public void onEnable(final Checkable checkable) {
    }
    
    @Override
    public void onDisable() {
    }
}
