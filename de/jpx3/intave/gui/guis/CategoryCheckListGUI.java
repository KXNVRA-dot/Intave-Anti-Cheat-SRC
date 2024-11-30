// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.gui.guis;

import java.util.stream.IntStream;
import org.bukkit.inventory.ItemStack;
import java.util.Iterator;
import java.util.List;
import de.jpx3.intave.util.objectable.IntaveCheck;
import de.jpx3.intave.api.internal.item.ItemAPI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.Bukkit;
import de.jpx3.intave.util.objectable.Checkable;
import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.util.enums.CheatCategory;
import org.bukkit.inventory.Inventory;
import de.jpx3.intave.gui.iGui;

public class CategoryCheckListGUI implements iGui
{
    private static Inventory inventory;
    private final CheatCategory cheatCategory;
    
    public CategoryCheckListGUI(final CheatCategory cheatCategory) {
        this.cheatCategory = cheatCategory;
    }
    
    @Override
    public String getName() {
        return IntavePlugin.getStaticReference().getPrefix() + this.cheatCategory.getName() + " Checks";
    }
    
    @Override
    public Inventory getInventory(final Checkable checkable) {
        this.fill(CategoryCheckListGUI.inventory = Bukkit.createInventory((InventoryHolder)null, 27, this.getName()), ItemAPI.getItem(Material.STAINED_GLASS_PANE, ChatColor.BLACK + "", 1, new String[0], (short)15));
        final List<IntaveCheck> checks = IntavePlugin.getStaticReference().getCheckManager().getAllChecksOf(this.cheatCategory);
        final String clearedNameOfO1;
        final String clearedNameOfO2;
        checks.sort((o1, o2) -> {
            clearedNameOfO1 = ChatColor.stripColor(o1.getName());
            clearedNameOfO2 = ChatColor.stripColor(o2.getName());
            return (clearedNameOfO1.charAt(0) > clearedNameOfO2.charAt(0)) ? 1 : 0;
        });
        int i = 0;
        for (final IntaveCheck check : checks) {
            CategoryCheckListGUI.inventory.setItem(9 + ++i, check.isActivated() ? ItemAPI.getItem(Material.WOOL, ChatColor.GREEN + check.getName(), 1, new String[0], (short)5) : ItemAPI.getItem(Material.WOOL, ChatColor.RED + check.getName(), 1, new String[0], (short)14));
        }
        CategoryCheckListGUI.inventory.setItem(18, ItemAPI.getItem(Material.ARROW, ChatColor.RED + "Back"));
        return CategoryCheckListGUI.inventory;
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
            IntavePlugin.getStaticReference().getGuiHandler().openGUI(checkable, new CheckCategorySelectGUI());
        }
    }
    
    @Override
    public void onEnable(final Checkable checkable) {
    }
    
    @Override
    public void onDisable() {
    }
}
