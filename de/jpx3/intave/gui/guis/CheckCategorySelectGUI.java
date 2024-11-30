// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.gui.guis;

import java.util.stream.IntStream;
import org.bukkit.inventory.ItemStack;
import de.jpx3.intave.util.enums.CheatCategory;
import de.jpx3.intave.api.internal.item.ItemAPI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.Bukkit;
import de.jpx3.intave.util.objectable.Checkable;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.inventory.Inventory;
import de.jpx3.intave.gui.iGui;

public class CheckCategorySelectGUI implements iGui
{
    private static Inventory inventory;
    
    @Override
    public String getName() {
        return IntavePlugin.getStaticReference().getPrefix() + "Categories";
    }
    
    @Override
    public Inventory getInventory(final Checkable checkable) {
        this.fill(CheckCategorySelectGUI.inventory = Bukkit.createInventory((InventoryHolder)null, 27, this.getName()), ItemAPI.getItem(Material.STAINED_GLASS_PANE, ChatColor.BLACK + "", 1, new String[0], (short)15));
        int f = 0;
        for (final CheatCategory cheatCategory : CheatCategory.values()) {
            if (!cheatCategory.getName().equalsIgnoreCase("Null")) {
                CheckCategorySelectGUI.inventory.setItem(10 + ++f, ItemAPI.getItem(cheatCategory.getMaterial(), cheatCategory.getName()));
            }
        }
        CheckCategorySelectGUI.inventory.setItem(18, ItemAPI.getItem(Material.ARROW, ChatColor.RED + "Back"));
        return CheckCategorySelectGUI.inventory;
    }
    
    private void fill(final Inventory inventory, final ItemStack toFill, final int start) {
        IntStream.range(start, inventory.getSize()).forEachOrdered(i -> inventory.setItem(i, toFill));
    }
    
    private void fill(final Inventory inventory, final ItemStack toFill) {
        this.fill(inventory, toFill, 0);
    }
    
    @Override
    public void onItemStackInteract(final Checkable checkable, final ItemStack itemStack, final int slot) {
        if (itemStack.getType().equals((Object)Material.ARROW)) {
            IntavePlugin.getStaticReference().getGuiHandler().openGUI(checkable, new MainMenuGUI());
        }
        if (slot > 10 && slot < CheatCategory.values().length + 10 - 1) {
            final CheatCategory cheatCategoryClickedOn = CheatCategory.values()[slot - 11];
            IntavePlugin.getStaticReference().getGuiHandler().openGUI(checkable, new CategoryCheckListGUI(cheatCategoryClickedOn));
        }
    }
    
    @Override
    public void onEnable(final Checkable checkable) {
    }
    
    @Override
    public void onDisable() {
    }
}
