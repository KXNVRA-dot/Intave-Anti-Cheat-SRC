// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.module;

import org.bukkit.entity.Player;
import de.jpx3.intave.util.calc.MathHelper;
import de.jpx3.intave.antipiracy.IIUA;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import de.jpx3.intave.IntavePlugin;

public class ResourceOverwatch
{
    private final IntavePlugin plugin;
    private final Map<String, Map<String, List<Long>>> timingsData;
    private final Map<String, Long> lastHightickWarning;
    
    public ResourceOverwatch(final IntavePlugin plugin) {
        this.timingsData = new ConcurrentHashMap<String, Map<String, List<Long>>>();
        this.lastHightickWarning = new ConcurrentHashMap<String, Long>();
        this.plugin = plugin;
    }
    
    public final Map<String, Map<String, List<Long>>> getCompleteTimingsData() {
        return this.timingsData;
    }
    
    public final Map<String, List<Long>> getSubTimingListss(final String name) {
        if (!this.timingsData.containsKey(name)) {
            this.timingsData.put(name, new ConcurrentHashMap<String, List<Long>>());
        }
        return this.timingsData.get(name);
    }
    
    public final List<Long> getSubTimingList(final String name, final String subname) {
        if (!this.timingsData.containsKey(name)) {
            this.timingsData.put(name, new ConcurrentHashMap<String, List<Long>>());
        }
        if (!this.timingsData.get(name).containsKey(subname)) {
            this.timingsData.get(name).put(subname, new CopyOnWriteArrayList<Long>());
        }
        return this.timingsData.get(name).get(name);
    }
    
    public final void addTiming(final String name, final String subname, final long currentDur, final int length, final double maxBalance) {
    }
    
    private void infoOverclocking(final String name, final String subname, final double msBalance) {
        final String timingName = "Timing(" + name + "." + subname + ")";
        if (IIUA.getCurrentTimeMillis() - this.lastHightickWarning.getOrDefault(timingName, 0L) < 60000L) {
            return;
        }
        this.plugin.getILogger().info("(ResourceOverwatch) Overclocking detected: " + timingName + " is taking " + MathHelper.roundFromDouble(msBalance, 6) + "ms in average.");
        this.plugin.getRetributionManager().sendNotifyMessage("Overclocking detected. " + timingName + " is taking " + MathHelper.roundFromDouble(msBalance, 6) + "ms in average.", null);
        this.lastHightickWarning.put(timingName, IIUA.getCurrentTimeMillis());
    }
    
    private <T> List<T> addDynamic(final List<T> list, final T toAdd, final double maxSize) {
        list.add(toAdd);
        if (list.size() > maxSize) {
            list.remove(0);
        }
        return list;
    }
}
