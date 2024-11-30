// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.gui;

import de.jpx3.intave.gui.guis.MainMenuGUI;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.EventHandler;
import de.jpx3.intave.util.objectable.Checkable;
import de.jpx3.intave.antipiracy.IIUA;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.event.Listener;

public final class GuiHandler implements Listener
{
    private final IntavePlugin plugin;
    
    public GuiHandler(final IntavePlugin plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)plugin);
    }
    
    @EventHandler
    public void on(final InventoryClickEvent e) {
        final Player p = (Player)e.getWhoClicked();
        final Checkable checkable = this.plugin.catchCheckable(IIUA.getUUIDFrom(p));
        if (e.getInventory() != null && e.getCurrentItem() != null && checkable.getMeta().getSystemValues().currentGUI != null && e.getInventory().getName().equalsIgnoreCase(checkable.getMeta().getSystemValues().currentGUI.getName())) {
            checkable.getMeta().getSystemValues().currentGUI.onItemStackInteract(checkable, e.getCurrentItem(), e.getSlot());
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void on(final PlayerDropItemEvent e) {
        final Player p = e.getPlayer();
        final Checkable checkable = this.plugin.catchCheckable(IIUA.getUUIDFrom(p));
        if (p.getOpenInventory().getTopInventory() != null && e.getItemDrop() != null && checkable.getMeta().getSystemValues().currentGUI != null && p.getOpenInventory().getTopInventory().getName().equalsIgnoreCase(checkable.getMeta().getSystemValues().currentGUI.getName())) {
            checkable.getMeta().getSystemValues().currentGUI.onItemStackInteract(checkable, e.getItemDrop().getItemStack(), -1);
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void on(final InventoryCloseEvent e) {
        final Player p = (Player)e.getPlayer();
        if (e.getPlayer().getOpenInventory().getTopInventory() == null) {
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSystemValues().currentGUI = null;
        }
    }
    
    public void openGUI(final Checkable checkable, final iGui gui) {
        if (checkable.getMeta().getSystemValues().currentGUI == gui || gui instanceof MainMenuGUI) {}
        if (gui == null) {
            return;
        }
        checkable.getMeta().getSystemValues().currentGUI = gui;
        checkable.asBukkitPlayer().openInventory(gui.getInventory(checkable));
    }
}
