// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.gui;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import de.jpx3.intave.util.objectable.Checkable;

public interface iGui
{
    String getName();
    
    Inventory getInventory(final Checkable p0);
    
    void onItemStackInteract(final Checkable p0, final ItemStack p1, final int p2);
    
    void onEnable(final Checkable p0);
    
    void onDisable();
}
