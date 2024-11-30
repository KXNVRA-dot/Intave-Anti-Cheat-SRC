// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.api.internal.item;

import java.util.Collection;
import java.util.ArrayList;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import java.util.Arrays;
import java.util.List;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.SkullMeta;
import java.lang.reflect.InvocationTargetException;
import de.jpx3.intave.api.internal.reflections.Reflections;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Color;
import org.bukkit.Material;

public final class ItemAPI
{
    public static ItemStack brust(final Material leatherPiece, final String displayName, final Color color) {
        final ItemStack item = new ItemStack(leatherPiece);
        final LeatherArmorMeta meta = (LeatherArmorMeta)item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setColor(color);
        item.setItemMeta((ItemMeta)meta);
        return item;
    }
    
    public static Object toNMSItem(final ItemStack bukkitStack) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (bukkitStack == null) {
            return null;
        }
        return Reflections.getCraftBukkitClass("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class).invoke(null, bukkitStack);
    }
    
    public static ItemStack head(final String head) {
        final SkullMeta meta = (SkullMeta)Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
        meta.setOwner(head);
        final ItemStack stack = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        stack.setItemMeta((ItemMeta)meta);
        return stack;
    }
    
    public static ItemStack getItem(final Material m) {
        return new ItemStack(m);
    }
    
    public static ItemStack getItem(final Material m, final String name) {
        final ItemStack is = new ItemStack(m);
        final ItemMeta im = is.getItemMeta();
        im.setDisplayName(name);
        im.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES });
        is.setItemMeta(im);
        return is;
    }
    
    public static ItemStack getItem(final Material m, final String name, final int amount) {
        final ItemStack is = new ItemStack(m);
        final ItemMeta im = is.getItemMeta();
        im.setDisplayName(name);
        im.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES });
        is.setItemMeta(im);
        is.setAmount(amount);
        return is;
    }
    
    public static ItemStack getItem(final Material m, final String name, final int amount, final String[] lore) {
        final ItemStack is = new ItemStack(m, amount);
        final ItemMeta im = is.getItemMeta();
        im.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES });
        im.setDisplayName(name);
        final List<String> lores = Arrays.stream(lore).map(s -> ChatColor.GRAY + s).collect((Collector<? super Object, ?, List<String>>)Collectors.toList());
        im.setLore((List)lores);
        is.setItemMeta(im);
        return is;
    }
    
    public static ItemStack getItem(final Material m, final String name, final int amount, final String[] lore, final short meta) {
        final ItemStack is = new ItemStack(m, amount, meta);
        final ItemMeta im = is.getItemMeta();
        im.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES });
        im.setDisplayName(name);
        final List<String> lores = new ArrayList<String>(Arrays.asList(lore));
        im.setLore((List)lores);
        is.setItemMeta(im);
        return is;
    }
}
