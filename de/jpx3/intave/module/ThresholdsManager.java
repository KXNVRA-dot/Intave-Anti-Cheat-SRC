// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.module;

import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import de.jpx3.intave.util.lists.Lists;
import java.util.LinkedHashMap;
import org.bukkit.plugin.Plugin;
import de.jpx3.intave.antipiracy.IIUA;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import de.jpx3.intave.IntavePlugin;

public final class ThresholdsManager
{
    private final IntavePlugin plugin;
    private final Map<String, Map<Integer, List<String>>> cache;
    private final Map<String, Integer> directFCache;
    
    public ThresholdsManager(final IntavePlugin plugin) {
        this.cache = new ConcurrentHashMap<String, Map<Integer, List<String>>>();
        this.directFCache = new ConcurrentHashMap<String, Integer>();
        this.plugin = plugin;
        plugin.getServer().getScheduler().scheduleAsyncRepeatingTask((Plugin)plugin, () -> plugin.getServer().getOnlinePlayers().forEach(p -> {
            synchronized (plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeFlaggedFor) {
                plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeFlaggedFor.keySet().stream().filter(s -> !plugin.getCheckManager().getCheck(s).isVLReduceCustom()).filter(s -> IIUA.getCurrentTimeMillis() - plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeFlaggedFor.get(s) > plugin.getConfig().getInt(this.getConfigPathFromName(s) + ".vl_reduce_time", 10) * 50).forEachOrdered(s -> plugin.getViolationManager().takeViolationLevel(p, 5, s));
            }
        }), 100L, 10L);
    }
    
    private Map<Integer, List<String>> getThresholdFor(String modulename) throws NullPointerException {
        modulename = modulename.toLowerCase();
        if (this.cache.containsKey(modulename)) {
            return this.cache.get(modulename);
        }
        final Map<Integer, List<String>> hashMap = new LinkedHashMap<Integer, List<String>>();
        final String path = this.getConfigPathFromName(modulename) + ".thresholds";
        final ConfigurationSection d = this.plugin.getConfig().getConfigurationSection(path);
        if (d == null) {
            throw new NullPointerException("Couldn't find check " + modulename + " while fetching its thresholds.");
        }
        final String str;
        List<String> list;
        final Map<Integer, List<String>> map;
        final Integer n;
        final List<String> list2;
        d.getKeys(false).forEach(s -> {
            Integer.valueOf((int)Double.parseDouble(s));
            if (this.plugin.getConfig().getStringList(str + "." + s).isEmpty()) {
                list = Lists.newArrayList(this.plugin.getConfig().getString(str + "." + s));
            }
            else {
                list = (List<String>)this.plugin.getConfig().getStringList(str + "." + s);
            }
            list2 = map.put(n, list);
            return;
        });
        this.cache.put(modulename, hashMap);
        return hashMap;
    }
    
    public final List<String> getActiveThresholdCommands(final String modulename, final int step) {
        return this.getThresholdFor(modulename).get(step);
    }
    
    public final String getConfigPathFromName(final String modulename) {
        return "checks." + modulename.toLowerCase();
    }
    
    final boolean thresholdHasStep(final String modulename, final int step) {
        return this.getThresholdFor(modulename).containsKey(step);
    }
    
    public final boolean shouldFlag(final String modulename, final int currentVL) {
        if (!this.directFCache.containsKey(modulename)) {
            final String path = this.getConfigPathFromName(modulename) + ".cancel_vl";
            this.directFCache.put(modulename, (int)this.plugin.getConfig().getDouble(path, 2.0));
        }
        return this.directFCache.get(modulename) <= currentVL;
    }
    
    public final boolean shouldLog(final String modulename, final int currentVL) {
        final String path = this.getConfigPathFromName(modulename) + ".log_vl";
        return (int)this.plugin.getConfig().getDouble(path, 0.0) <= currentVL;
    }
    
    public void reload() {
        this.cache.clear();
    }
}
