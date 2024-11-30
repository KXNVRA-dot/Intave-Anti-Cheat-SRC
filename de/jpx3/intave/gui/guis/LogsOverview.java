// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.gui.guis;

import java.util.stream.IntStream;
import org.bukkit.inventory.ItemStack;
import java.util.stream.Collector;
import java.util.function.Supplier;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Date;
import java.util.Iterator;
import de.jpx3.intave.util.calc.analysis.StringUtils;
import java.util.function.Predicate;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import de.jpx3.intave.api.internal.item.ItemAPI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.Bukkit;
import de.jpx3.intave.util.objectable.Checkable;
import de.jpx3.intave.IntavePlugin;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ConcurrentHashMap;
import java.io.File;
import java.util.Map;
import org.bukkit.inventory.Inventory;
import de.jpx3.intave.gui.iGui;

public class LogsOverview implements iGui
{
    private static Inventory inventory;
    private int page;
    private Map<Long, File> cachedFiles;
    
    public LogsOverview() {
        this.cachedFiles = new ConcurrentHashMap<Long, File>();
        this.page = 0;
    }
    
    public LogsOverview(final int page) {
        this.cachedFiles = new ConcurrentHashMap<Long, File>();
        this.page = page;
    }
    
    private static boolean test(final File file) {
        try {
            return Files.lines(file.toPath()).anyMatch(s -> s.contains("(CMD)"));
        }
        catch (IOException ex) {
            return false;
        }
    }
    
    @Override
    public final String getName() {
        return IntavePlugin.getStaticReference().getPrefix() + "Log List";
    }
    
    @Override
    public final Inventory getInventory(final Checkable checkable) {
        LogsOverview.inventory = Bukkit.createInventory((InventoryHolder)null, 54, this.getName());
        final Inventory inventoryx = Bukkit.createInventory((InventoryHolder)null, 54, this.getName());
        inventoryx.clear();
        this.fill(inventoryx, ItemAPI.getItem(Material.STAINED_GLASS_PANE, ChatColor.BLACK + "", 1, new String[0], (short)15), 45);
        final AtomicReference<Map<Long, File>> logFiles = new AtomicReference<Map<Long, File>>();
        logFiles.set(new ConcurrentHashMap<Long, File>());
        try {
            final File file2;
            Arrays.stream(IntavePlugin.getStaticReference().getILogger().getLogFiles()).filter(LogsOverview::test).forEach(file -> file2 = logFiles.get().put(this.getTimeStampFromName(file), file));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        logFiles.set(sortByValue(logFiles.get()));
        this.cachedFiles = logFiles.get();
        boolean hasNextPage = false;
        final int max_equor = 45;
        int i = 0;
        int simpleI = 0;
        for (final File logFile : logFiles.get().values()) {
            if (this.page * max_equor > i) {
                ++i;
            }
            else {
                if (i > max_equor * (this.page + 1)) {
                    hasNextPage = true;
                    break;
                }
                String simpleName = logFile.getName();
                simpleName = StringUtils.replaceString(simpleName, "intave", "");
                simpleName = StringUtils.replaceString(simpleName, ".log", "");
                simpleName = StringUtils.replaceString(simpleName, "_", " ");
                simpleName = StringUtils.replaceString(simpleName, "-", " ");
                final String[] groovy = simpleName.split(" ");
                String days = (int)Double.parseDouble(groovy[2]) + "";
                String months = (int)Double.parseDouble(groovy[1]) + "";
                final String year = (int)Double.parseDouble(groovy[0]) + "";
                String hours = (int)Double.parseDouble(groovy[3]) + "";
                String minutes = (int)Double.parseDouble(groovy[4]) + "";
                String seconds = (int)Double.parseDouble(groovy[5]) + "";
                if (months.length() < 2) {
                    months = "0" + months;
                }
                if (days.length() < 2) {
                    days = "0" + days;
                }
                if (hours.length() < 2) {
                    hours = "0" + hours;
                }
                if (minutes.length() < 2) {
                    minutes = "0" + minutes;
                }
                if (seconds.length() < 2) {
                    seconds = "0" + seconds;
                }
                final String timeName = days + "." + months + "." + year + " at " + hours + ":" + minutes + ":" + seconds;
                inventoryx.setItem(simpleI++, ItemAPI.getItem(Material.BOOK, "Log for " + timeName, 1, new String[] { ChatColor.WHITE + "Size: " + (int)(logFile.length() / 1000L) + "kb" }));
                ++i;
            }
        }
        LogsOverview.inventory.setItem(45, ItemAPI.getItem(Material.ARROW, ChatColor.RED + "Back"));
        if (this.page > 0) {
            LogsOverview.inventory.setItem(52, ItemAPI.getItem(Material.PAPER, "To Page " + this.page));
        }
        if (hasNextPage) {
            LogsOverview.inventory.setItem(53, ItemAPI.getItem(Material.PAPER, "To Page " + (this.page + 1 + 1)));
        }
        return inventoryx;
    }
    
    private long getTimeStampFromName(final File file) {
        String simpleName = file.getName();
        simpleName = StringUtils.replaceString(simpleName, "intave", "");
        simpleName = StringUtils.replaceString(simpleName, ".log", "");
        simpleName = StringUtils.replaceString(simpleName, "_", " ");
        simpleName = StringUtils.replaceString(simpleName, "-", " ");
        final String[] groovy = simpleName.split(" ");
        final int years = (int)Double.parseDouble(groovy[0]);
        final int months = (int)Double.parseDouble(groovy[1]);
        final int days = (int)Double.parseDouble(groovy[2]);
        final int hours = (int)Double.parseDouble(groovy[3]);
        final int minutes = (int)Double.parseDouble(groovy[4]);
        final int seconds = (int)Double.parseDouble(groovy[5]);
        return new Date(years, months, days, hours, minutes, seconds).getTime();
    }
    
    private static Map<Long, File> sortByValue(final Map<Long, File> unsortMap) {
        final LinkedList<Map.Entry<Long, File>> list = new LinkedList<Map.Entry<Long, File>>(unsortMap.entrySet());
        list.sort(Comparator.comparing(o -> o.getValue()));
        return list.stream().collect((Collector<? super Object, ?, Map<Long, File>>)Collectors.toMap((Function<? super Object, ?>)Map.Entry::getKey, (Function<? super Object, ?>)Map.Entry::getValue, (a, b) -> b, (Supplier<R>)LinkedHashMap::new));
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
            return;
        }
        if (itemStack.getType().equals((Object)Material.PAPER)) {
            String name = itemStack.getItemMeta().getDisplayName();
            name = name.replace("To Page ", "");
            final int newPage = (int)(Double.parseDouble(name.trim()) - 1.0);
            IntavePlugin.getStaticReference().getGuiHandler().openGUI(checkable, new LogsOverview(newPage));
            return;
        }
        if (itemStack.getType().equals((Object)Material.BOOK)) {
            String itemName = itemStack.getItemMeta().getDisplayName();
            itemName = StringUtils.replaceString(StringUtils.replaceString(itemName, "at", ""), "Log for ", "").trim().replace(".", "!").replace(":", "!").replace(" ", "!").replace("!!", "!");
            final String[] groovy = itemName.split("!");
            final String filename = "intave" + groovy[2] + "_" + groovy[1] + "_" + groovy[0] + "_" + groovy[3] + "-" + groovy[4] + "-" + groovy[5] + ".log";
            final File logfile = new File("plugins/Intave/logs/" + filename);
            if (!logfile.isFile()) {
                checkable.asBukkitPlayer().sendMessage(IntavePlugin.getStaticReference().getPrefix() + ChatColor.RED + "File " + logfile.getAbsolutePath() + " was not found.");
                return;
            }
            IntavePlugin.getStaticReference().getGuiHandler().openGUI(checkable, new LogsDeepLook(logfile, this.page));
        }
    }
    
    @Override
    public void onEnable(final Checkable checkable) {
    }
    
    @Override
    public void onDisable() {
    }
}
