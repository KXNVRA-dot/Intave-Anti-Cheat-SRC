// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.gui.guis;

import java.util.stream.Collector;
import java.util.function.Supplier;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.Collection;
import java.util.LinkedList;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Iterator;
import org.bukkit.Material;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import de.jpx3.intave.api.internal.item.ItemAPI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.Bukkit;
import de.jpx3.intave.util.objectable.Checkable;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.inventory.Inventory;
import de.jpx3.intave.gui.iGui;

public final class PlayerVioListGUI implements iGui
{
    private static Inventory inventory;
    
    @Override
    public String getName() {
        return IntavePlugin.getStaticReference().getPrefix() + "Violations";
    }
    
    @Override
    public Inventory getInventory(final Checkable checkable) {
        if (PlayerVioListGUI.inventory == null) {
            PlayerVioListGUI.inventory = Bukkit.createInventory((InventoryHolder)null, 54, this.getName());
        }
        PlayerVioListGUI.inventory.clear();
        final List<String> allCheckNames = IntavePlugin.getStaticReference().getCheckManager().getAllCheckNames();
        final Map<ItemStack, Integer> map = new ConcurrentHashMap<ItemStack, Integer>();
        for (final Player p : IntavePlugin.getStaticReference().getServer().getOnlinePlayers()) {
            final Map<String, Integer> localVlMap = new ConcurrentHashMap<String, Integer>();
            int i = 0;
            int g = 0;
            for (final String checkname : allCheckNames) {
                final int checkVL = IntavePlugin.getStaticReference().getViolationManager().getViolationLevel(p, checkname);
                g += checkVL;
                if (checkVL > 0) {
                    if (i < 6) {}
                    localVlMap.put(checkname, checkVL);
                    ++i;
                }
            }
            final ItemStack is = ItemAPI.head(p.getDisplayName());
            final ItemMeta im = is.getItemMeta();
            im.setDisplayName(ChatColor.GRAY + p.getName());
            final List<String> lore = new ArrayList<String>();
            lore.add(" ");
            if (i < 1) {
                lore.add(ChatColor.GRAY + "No Violations");
            }
            else {
                sortByValue(localVlMap).forEach((s, integer) -> lore.add(ChatColor.RED + s + " vl " + integer));
            }
            lore.add(" ");
            im.setLore((List)lore);
            is.setItemMeta(im);
            map.put(is, g);
        }
        int j = 0;
        for (final Map.Entry<ItemStack, Integer> e : sortByValueIS(map).entrySet()) {
            if (j > PlayerVioListGUI.inventory.getSize() - 10) {
                break;
            }
            PlayerVioListGUI.inventory.setItem(j, (ItemStack)e.getKey());
            ++j;
        }
        PlayerVioListGUI.inventory.setItem(45, ItemAPI.getItem(Material.ARROW, ChatColor.RED + "Back"));
        PlayerVioListGUI.inventory.setItem(46, ItemAPI.getItem(Material.FEATHER, ChatColor.GREEN + "Refresh"));
        return PlayerVioListGUI.inventory;
    }
    
    @Override
    public void onItemStackInteract(final Checkable checkable, final ItemStack itemStack, final int slot) {
        if (itemStack.getType().equals((Object)Material.SKULL_ITEM)) {}
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
    
    private static Map<String, Integer> sortByValue(final Map<String, Integer> unsortMap) {
        final List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());
        list.sort(Comparator.comparing(o -> o.getValue()));
        return list.stream().collect((Collector<? super Object, ?, Map<String, Integer>>)Collectors.toMap((Function<? super Object, ?>)Map.Entry::getKey, (Function<? super Object, ?>)Map.Entry::getValue, (a, b) -> b, (Supplier<R>)LinkedHashMap::new));
    }
    
    private static Map<ItemStack, Integer> sortByValueIS(final Map<ItemStack, Integer> unsortMap) {
        final List<Map.Entry<ItemStack, Integer>> list = new LinkedList<Map.Entry<ItemStack, Integer>>(unsortMap.entrySet());
        list.sort(Comparator.comparing(o -> o.getValue()));
        return list.stream().collect((Collector<? super Object, ?, Map<ItemStack, Integer>>)Collectors.toMap((Function<? super Object, ?>)Map.Entry::getKey, (Function<? super Object, ?>)Map.Entry::getValue, (a, b) -> b, (Supplier<R>)LinkedHashMap::new));
    }
}
