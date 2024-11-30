// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.module;

import java.util.Iterator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import de.jpx3.intave.mcver.versions.IntaveCFR1_8_R1;
import de.jpx3.intave.mcver.versions.IntaveCFR1_12_R1;
import de.jpx3.intave.mcver.versions.IntaveCFR1_9_R2;
import de.jpx3.intave.mcver.versions.IntaveCFR1_8_R3;
import de.jpx3.intave.api.internal.reflections.Reflections;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import de.jpx3.intave.mcver.VersionCFR;
import de.jpx3.intave.IntavePlugin;

public final class CrossVersionSupply
{
    private final IntavePlugin plugin;
    private VersionCFR cfr;
    private final List<VersionCFR> cfrList;
    
    public CrossVersionSupply(final IntavePlugin plugin) {
        this.cfrList = new CopyOnWriteArrayList<VersionCFR>();
        this.plugin = plugin;
        this.loadCRFSToCache();
        this.setupVersion(Reflections.getVersion());
    }
    
    public static VersionCFR getCurrentCFR() {
        return IntavePlugin.getStaticReference().getCrossVersionSupply().cfr;
    }
    
    private void loadCRFSToCache() {
        this.cfrList.clear();
        this.cfrList.add(new IntaveCFR1_8_R3());
        this.cfrList.add(new IntaveCFR1_9_R2());
        this.cfrList.add(new IntaveCFR1_12_R1());
    }
    
    private void setupVersion(final String version) {
        final VersionCFR versionCFR2 = this.cfrList.stream().filter(versionCFR1 -> versionCFR1.getName().equalsIgnoreCase(version)).findFirst().orElse((this.getBestVersionFor(version) == null) ? new IntaveCFR1_8_R1() : this.getBestVersionFor(version));
        final boolean success = versionCFR2.getName().equalsIgnoreCase(version);
        if (!success) {
            this.plugin.getILogger().error("Couldn't find server version " + version + " in intaves database. Using " + versionCFR2.getName() + " instead!");
        }
        else {
            this.plugin.getILogger().info("Using stable adapter " + versionCFR2.getName());
        }
        this.cfr = versionCFR2;
    }
    
    private VersionCFR getBestVersionFor(final String version) {
        final List<VersionCFR> nearVersions = this.cfrList.stream().filter(versionCFR -> !this.isNewerThan(version, versionCFR.getName())).collect((Collector<? super Object, ?, List<VersionCFR>>)Collectors.toList());
        VersionCFR newest = null;
        for (final VersionCFR versionCFR2 : nearVersions) {
            if (newest == null || this.isNewerThan(newest.getName(), versionCFR2.getName())) {
                newest = versionCFR2;
            }
        }
        return newest;
    }
    
    public boolean isNewerThan(final String mightBeOlder, final String toCheck) {
        return this.versionToInt(mightBeOlder) < this.versionToInt(toCheck);
    }
    
    public int getVersionDifference(final String version1, final String version2) {
        return this.versionToInt(version1) - this.versionToInt(version2);
    }
    
    public int versionToInt(final String version) {
        final String onlyIntegers = version.replaceAll("[^\\d.]", "");
        return Integer.valueOf(onlyIntegers);
    }
    
    public VersionCFR getCurrentVersion() {
        return this.cfr;
    }
}
