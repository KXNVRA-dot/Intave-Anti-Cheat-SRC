// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.module;

import org.bukkit.entity.Player;
import de.jpx3.intave.graph.iGraphTextComponent;
import de.jpx3.intave.graph.iGraphLine;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import de.jpx3.intave.graph.iGraphFactory;
import de.jpx3.intave.graph.iGraph;
import java.util.function.Consumer;
import java.util.function.Function;
import org.bukkit.entity.Entity;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Collections;
import de.jpx3.intave.util.stat.NumberFetcher;
import org.bukkit.plugin.Plugin;
import de.jpx3.intave.util.IntaveExceptionHandler;
import de.jpx3.intave.antipiracy.IIUA;
import de.jpx3.intave.util.stat.CheckableNumberFetcher;
import java.util.concurrent.ConcurrentHashMap;
import de.jpx3.intave.util.stat.StatisticValue;
import java.util.List;
import java.util.Map;
import de.jpx3.intave.IntavePlugin;

public final class StatisticsProvider
{
    private final IntavePlugin plugin;
    private final Map<String, Integer> statisticalTaskIds;
    private final Map<String, List<StatisticValue>> singletronStats;
    
    public StatisticsProvider(final IntavePlugin plugin) {
        this.statisticalTaskIds = new ConcurrentHashMap<String, Integer>();
        this.singletronStats = new ConcurrentHashMap<String, List<StatisticValue>>();
        this.plugin = plugin;
    }
    
    public void createPlayerStatistic(String name, final CheckableNumberFetcher numberFetcher, final int ticksBetweenChecks, final long expires) {
        if (expires < IIUA.getCurrentTimeMillis()) {
            IntaveExceptionHandler.printAndSaveToFile("[FATAL] Statistic \"" + name + "\" expired before it even started?", new IllegalStateException("Statistic expired before it started"));
            return;
        }
        if (this.statisticalTaskIds.containsKey(name)) {
            IntaveExceptionHandler.printAndSaveToFile("[FATAL] Statistic \"" + name + "\" is already running. Can't start a new one with the same name. Startig \"" + name + "-" + "\" instead", new IllegalStateException("Statistic already exists."));
            name += "-";
        }
        final String finalName = name;
        final String name2;
        final int i = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)this.plugin, () -> {
            if (expires < IIUA.getCurrentTimeMillis()) {
                this.plugin.getServer().getScheduler().cancelTask((int)this.statisticalTaskIds.get(name2));
                this.statisticalTaskIds.remove(name2);
            }
            else {
                this.plugin.getServer().getOnlinePlayers().forEach(all -> this.addPlayerValue(IIUA.getUUIDFrom(all), name2, numberFetcher.getValue(this.plugin.catchCheckable(all.getUniqueId()))));
            }
            return;
        }, 0L, (long)ticksBetweenChecks);
        this.statisticalTaskIds.put(name, i);
    }
    
    public void createSingletronStatistic(String name, final NumberFetcher numberFetcher, final int ticksBetweenChecks, final long expires) {
        if (expires < IIUA.getCurrentTimeMillis()) {
            IntaveExceptionHandler.printAndSaveToFile("[FATAL] Statistic \"" + name + "\" expired before it even started?", new IllegalStateException("Statistic expired before it started"));
            return;
        }
        if (this.statisticalTaskIds.containsKey(name)) {
            IntaveExceptionHandler.printAndSaveToFile("[FATAL] Statistic \"" + name + "\" is already running. Can't start a new one with the same name. Startig \"" + name + "-" + "\" instead", new IllegalStateException("Statistic already exists."));
            name += "-";
        }
        final String finalName = name;
        final String name2;
        final int i = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)this.plugin, () -> {
            if (expires < IIUA.getCurrentTimeMillis()) {
                this.plugin.getServer().getScheduler().cancelTask((int)this.statisticalTaskIds.get(name2));
                this.statisticalTaskIds.remove(name2);
            }
            else {
                this.addSingletronValue(name2, numberFetcher.getValue());
            }
            return;
        }, 0L, (long)ticksBetweenChecks);
        this.statisticalTaskIds.put(name, i);
    }
    
    private void addSingletronValue(final String name, final Number value) {
        if (!this.singletronStats.containsKey(name.toLowerCase())) {
            this.singletronStats.put(name.toLowerCase(), Collections.emptyList());
        }
        this.singletronStats.get(name.toLowerCase()).add(new StatisticValue(value));
    }
    
    private void addPlayerValue(final UUID player, final String name, final Number value) {
        final Map<String, List<StatisticValue>> statistics = this.plugin.catchCheckable(player).getMeta().getSystemValues().statistics;
        if (!statistics.containsKey(name.toLowerCase())) {
            statistics.put(name.toLowerCase(), Collections.emptyList());
        }
        statistics.get(name.toLowerCase()).add(new StatisticValue(value));
    }
    
    public final List<StatisticValue> getSingletronValues(final String name) {
        return this.singletronStats.getOrDefault(name.toLowerCase(), new ArrayList<StatisticValue>());
    }
    
    public final List<StatisticValue> getPlayerValues(final UUID player, final String name) {
        return this.plugin.catchCheckable(player).getMeta().getSystemValues().statistics.getOrDefault(name.toLowerCase(), new ArrayList<StatisticValue>());
    }
    
    public final List<List<StatisticValue>> getAllPlayerValues(final String name) {
        final List<List<StatisticValue>> listHashMap = Collections.emptyList();
        this.plugin.getServer().getOnlinePlayers().stream().map(Entity::getUniqueId).map(uuid -> this.getPlayerValues(uuid, name)).forEachOrdered(listHashMap::add);
        return listHashMap;
    }
    
    public void clearSingletronStatistic(final String name) {
        if (this.singletronStats.containsKey(name.toLowerCase())) {
            this.singletronStats.put(name.toLowerCase(), Collections.emptyList());
        }
    }
    
    public void clearPlayerStatistic(final String name) {
        this.plugin.getServer().getOnlinePlayers().forEach(all -> this.clearPlayerStatistic(name, IIUA.getUUIDFrom(all)));
    }
    
    public void stopAllStatistics() {
        this.statisticalTaskIds.forEach((s, integer) -> this.plugin.getServer().getScheduler().cancelTask((int)integer));
        this.statisticalTaskIds.clear();
        this.singletronStats.clear();
        this.plugin.getServer().getOnlinePlayers().forEach(o -> this.plugin.catchCheckable(o.getUniqueId()).getMeta().getSystemValues().statistics.clear());
    }
    
    public void clearPlayerStatistic(final String name, final UUID player) {
        if (this.plugin.catchCheckable(player).getMeta().getSystemValues().statistics.containsKey(name.toLowerCase())) {
            this.plugin.catchCheckable(player).getMeta().getSystemValues().statistics.put(name.toLowerCase(), new ArrayList<StatisticValue>());
        }
    }
    
    public final iGraph createSingletronStatisticGraph(final String name) {
        if (!this.singletronStats.containsKey(name)) {
            return iGraphFactory.createGraph();
        }
        final List<Double> values = this.singletronStats.get(name).stream().map(statisticValue -> statisticValue.getValue().doubleValue()).collect((Collector<? super Object, ?, List<Double>>)Collectors.toList());
        return iGraphFactory.createSimpleGraphWithText(new iGraphLine(values), new iGraphTextComponent(name, 20, 20));
    }
    
    public final iGraph createTimeFrameSingletronStatisticGraph(final String name, final long timeFrameStart, final long timeFrameStop) {
        if (!this.singletronStats.containsKey(name.toLowerCase())) {
            IntaveExceptionHandler.printAndSaveToFile("[FATAL] Intave could not create a graph", new NullPointerException("SingletronStatistic \"" + name + "\" couldn't be found"));
            return null;
        }
        if (timeFrameStart >= timeFrameStop) {
            IntaveExceptionHandler.printAndSaveToFile("[FATAL] Intave could not create a graph", new IllegalArgumentException("Bad timeframe"));
            return null;
        }
        final List<Double> values = this.singletronStats.get(name).stream().filter(statisticValue -> statisticValue.getTimeStamp() > timeFrameStart && statisticValue.getTimeStamp() < timeFrameStop).map(statisticValue -> statisticValue.getValue().doubleValue()).collect((Collector<? super Object, ?, List<Double>>)Collectors.toList());
        return iGraphFactory.createSimpleGraphWithText(new iGraphLine(values), new iGraphTextComponent(name, 20, 20));
    }
}
