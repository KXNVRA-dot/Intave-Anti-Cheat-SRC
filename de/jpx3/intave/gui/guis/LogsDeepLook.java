// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.gui.guis;

import java.io.IOException;
import de.jpx3.intave.util.calc.analysis.StringUtils;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.stream.IntStream;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.ItemStack;
import java.util.Iterator;
import java.util.List;
import de.jpx3.intave.api.internal.item.ItemAPI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.Bukkit;
import de.jpx3.intave.util.objectable.Checkable;
import de.jpx3.intave.IntavePlugin;
import java.util.Arrays;
import java.io.File;
import org.bukkit.inventory.Inventory;
import de.jpx3.intave.gui.iGui;

public class LogsDeepLook implements iGui
{
    private static Inventory inventory;
    private final File log_file;
    private int lastPage;
    
    public LogsDeepLook(final File log_file, final int lastPage) {
        this.log_file = log_file;
        this.lastPage = lastPage;
        System.out.println(Arrays.toString(this.getActionsFromLogs(log_file).toArray()));
    }
    
    @Override
    public String getName() {
        return IntavePlugin.getStaticReference().getPrefix() + "Log Insp.";
    }
    
    @Override
    public Inventory getInventory(final Checkable checkable) {
        final Inventory inventoryx = Bukkit.createInventory((InventoryHolder)null, 54, this.getName());
        inventoryx.clear();
        this.fill(inventoryx, ItemAPI.getItem(Material.STAINED_GLASS_PANE, ChatColor.BLACK + "", 1, new String[0], (short)15), 45);
        final List<String> actions = this.getActionsFromLogs(this.log_file);
        int i = 0;
        for (final String action : actions) {
            final String[] actionSplit = action.split("#");
            final String violator = actionSplit[0];
            final String command = actionSplit[1];
            final ItemStack itemStack = ItemAPI.head(violator);
            final ItemMeta itemMeta = itemStack.getItemMeta();
            final String[] lore = { "Command: " + command };
            itemMeta.setDisplayName(violator);
            itemMeta.setLore((List)Arrays.asList(lore));
            itemStack.setItemMeta(itemMeta);
            inventoryx.setItem(i++, itemStack);
        }
        LogsDeepLook.inventory.setItem(45, ItemAPI.getItem(Material.ARROW, ChatColor.RED + "Back"));
        return inventoryx;
    }
    
    private void fill(final Inventory inventory, final ItemStack toFill, final int start) {
        IntStream.range(start, inventory.getSize()).forEachOrdered(i -> inventory.setItem(i, toFill));
    }
    
    private void fill(final Inventory inventory, final ItemStack toFill) {
        this.fill(inventory, toFill, 0);
    }
    
    private List<String> getActionsFromLogs(final File file) {
        final List<String> list = new ArrayList<String>();
        if (file.length() < 5L) {
            return list;
        }
        try {
            final AtomicInteger i = new AtomicInteger();
            int nameIndex;
            final int cmdIndex;
            final int cmdEnd;
            final StringBuilder name;
            final String command;
            final AtomicInteger atomicInteger;
            final List<String> list2;
            Files.lines(file.toPath()).filter(s -> s.contains("(CMD)")).forEach(s -> {
                nameIndex = s.indexOf("(CMD) ") + 6;
                cmdIndex = s.indexOf(39) + 1;
                cmdEnd = s.lastIndexOf(39);
                name = new StringBuilder();
                while (!s.substring(nameIndex, ++nameIndex).equals(" ")) {
                    name.append(s, nameIndex - 1, nameIndex);
                }
                command = s.substring(cmdIndex, cmdEnd);
                if (atomicInteger.getAndIncrement() < 45) {
                    list2.add(name.toString() + "#" + StringUtils.replaceString(command, "#", "?"));
                }
                return;
            });
        }
        catch (IOException ex) {}
        return list;
    }
    
    @Override
    public void onItemStackInteract(final Checkable checkable, final ItemStack itemStack, final int slot) {
        if (itemStack.getType().equals((Object)Material.ARROW)) {
            IntavePlugin.getStaticReference().getGuiHandler().openGUI(checkable, new LogsOverview(this.lastPage));
        }
    }
    
    @Override
    public void onEnable(final Checkable checkable) {
    }
    
    @Override
    public void onDisable() {
    }
}
