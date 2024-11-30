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

public final class MainMenuGUI implements iGui
{
    private static Inventory inventory;
    
    @Override
    public String getName() {
        return IntavePlugin.getStaticReference().getPrefix() + "Options";
    }
    
    @Override
    public Inventory getInventory(final Checkable checkable) {
        if (MainMenuGUI.inventory == null) {
            final Inventory newInventory = Bukkit.createInventory((InventoryHolder)null, 27, this.getName());
            this.fill(newInventory, ItemAPI.getItem(Material.STAINED_GLASS_PANE, ChatColor.BLACK + "", 1, new String[0], (short)15));
            MainMenuGUI.inventory = newInventory;
        }
        final Inventory inventory = MainMenuGUI.inventory;
        inventory.setItem(11, ItemAPI.getItem(Material.COMMAND, ChatColor.GRAY + "Checks"));
        inventory.setItem(13, ItemAPI.getItem(Material.REDSTONE, ChatColor.RED + "Violations"));
        inventory.setItem(15, ItemAPI.getItem(Material.PAPER, ChatColor.WHITE + "Server info"));
        return inventory;
    }
    
    private void fill(final Inventory inventory, final ItemStack toFill) {
        IntStream.range(0, inventory.getSize()).forEachOrdered(i -> inventory.setItem(i, toFill));
    }
    
    @Override
    public void onItemStackInteract(final Checkable checkable, final ItemStack itemStack, final int slot) {
        if (itemStack.getType().equals((Object)Material.COMMAND)) {
            IntavePlugin.getStaticReference().getGuiHandler().openGUI(checkable, new CheckCategorySelectGUI());
        }
        if (itemStack.getType().equals((Object)Material.PAPER)) {
            IntavePlugin.getStaticReference().getGuiHandler().openGUI(checkable, new SystemValuesGUI());
        }
        if (itemStack.getType().equals((Object)Material.REDSTONE)) {
            IntavePlugin.getStaticReference().getGuiHandler().openGUI(checkable, new PlayerVioListGUI());
        }
        if (itemStack.getType().equals((Object)Material.BOOK)) {
            IntavePlugin.getStaticReference().getGuiHandler().openGUI(checkable, new LogTypeSelection());
        }
    }
    
    @Override
    public void onEnable(final Checkable checkable) {
    }
    
    @Override
    public void onDisable() {
    }
}
