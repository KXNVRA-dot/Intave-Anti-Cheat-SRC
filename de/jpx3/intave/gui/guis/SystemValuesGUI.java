// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.gui.guis;

import java.util.stream.IntStream;
import org.bukkit.inventory.ItemStack;
import de.jpx3.intave.api.internal.system.ServerInformation;
import de.jpx3.intave.api.internal.item.ItemAPI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.Bukkit;
import de.jpx3.intave.util.objectable.Checkable;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.inventory.Inventory;
import de.jpx3.intave.gui.iGui;

public final class SystemValuesGUI implements iGui
{
    private static Inventory inventory;
    
    @Override
    public String getName() {
        return IntavePlugin.getStaticReference().getPrefix() + "SystemValues";
    }
    
    @Override
    public Inventory getInventory(final Checkable checkable) {
        if (SystemValuesGUI.inventory == null) {
            final Inventory newInventory = Bukkit.createInventory((InventoryHolder)null, 27, this.getName());
            this.fill(newInventory, ItemAPI.getItem(Material.STAINED_GLASS_PANE, ChatColor.BLACK + "", 1, new String[0], (short)15));
            SystemValuesGUI.inventory = newInventory;
        }
        final int processors = ServerInformation.getAvailableProcessors();
        final long freeMemory = (int)(ServerInformation.getFreeMemory() / 1000000L);
        final long totalMemory = (int)(ServerInformation.getMaxAvailableMemory() / 1000000L);
        final Inventory inventory = SystemValuesGUI.inventory;
        inventory.setItem(11, ItemAPI.getItem(Material.BEACON, ChatColor.GRAY + "Processors", processors, new String[] { "There are " + processors + " processors available" }));
        inventory.setItem(13, ItemAPI.getItem(Material.BUCKET, ChatColor.GRAY + "Free RAM", 1, new String[] { freeMemory + "mb are free to use" }));
        inventory.setItem(15, ItemAPI.getItem(Material.LAVA_BUCKET, ChatColor.GRAY + "Total RAM", 1, new String[] { totalMemory + "mb can be used" }));
        inventory.setItem(18, ItemAPI.getItem(Material.ARROW, ChatColor.RED + "Back"));
        inventory.setItem(19, ItemAPI.getItem(Material.FEATHER, ChatColor.GREEN + "Refresh"));
        return inventory;
    }
    
    private void fill(final Inventory inventory, final ItemStack toFill) {
        IntStream.range(0, inventory.getSize()).forEachOrdered(i -> inventory.setItem(i, toFill));
    }
    
    @Override
    public void onItemStackInteract(final Checkable checkable, final ItemStack itemStack, final int slot) {
        if (itemStack.getType().equals((Object)Material.ARROW)) {
            IntavePlugin.getStaticReference().getGuiHandler().openGUI(checkable, new MainMenuGUI());
        }
        if (itemStack.getType().equals((Object)Material.FEATHER)) {
            IntavePlugin.getStaticReference().getGuiHandler().openGUI(checkable, this);
        }
    }
    
    @Override
    public void onEnable(final Checkable checkable) {
    }
    
    @Override
    public void onDisable() {
    }
}
