// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.module;

import org.bukkit.plugin.Plugin;
import de.jpx3.intave.util.calc.BalanceUtils;
import java.util.ArrayList;
import de.jpx3.intave.antipiracy.IIUA;
import java.util.List;
import de.jpx3.intave.IntavePlugin;

public final class TPSManager
{
    private final IntavePlugin plugin;
    private long lastSec;
    private List<Double> statisticalTPS;
    private static double avgtps;
    
    public TPSManager(final IntavePlugin plugin) {
        this.lastSec = IIUA.getCurrentTimeMillis();
        this.statisticalTPS = new ArrayList<Double>();
        final double secspertick;
        final double tps;
        final int size;
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)plugin, () -> {
            secspertick = (double)((IIUA.getCurrentTimeMillis() - this.lastSec) / 20L);
            tps = secspertick * 400.0 / 1000.0;
            size = 4;
            if (this.statisticalTPS.size() >= size) {
                this.statisticalTPS.remove(0);
            }
            this.statisticalTPS.add(tps);
            if (this.statisticalTPS.size() >= size) {
                TPSManager.avgtps = BalanceUtils.getSquaredBalanceFromDouble(this.statisticalTPS);
            }
            this.lastSec = IIUA.getCurrentTimeMillis();
            return;
        }, 20L, 20L);
        this.plugin = plugin;
    }
    
    public static double getAverageTicksPerSecond() {
        return TPSManager.avgtps;
    }
    
    static {
        TPSManager.avgtps = 20.0;
    }
}
