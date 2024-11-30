// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.api.internal;

import org.bukkit.Bukkit;
import de.jpx3.intave.antipiracy.IIUA;
import java.lang.reflect.InvocationTargetException;
import org.bukkit.entity.Player;
import de.jpx3.intave.IntavePlugin;

public final class ViaVersionAdapter
{
    private final IntavePlugin plugin;
    private static boolean hasViaVersion;
    private static long lastCheckedViaVersion;
    
    public ViaVersionAdapter(final IntavePlugin plugin) {
        this.plugin = plugin;
    }
    
    public static int getPlayerVersion(final Player player) {
        if (!hasViaVersion()) {
            return -1;
        }
        try {
            final Class<?> viaVersion = Class.forName("us.myles.ViaVersion.api.ViaVersion");
            final Object viaVersionInstance = viaVersion.getMethod("getInstance", (Class<?>[])new Class[0]).invoke(viaVersion, new Object[0]);
            return (int)viaVersionInstance.getClass().getMethod("getPlayerVersion", Player.class).invoke(viaVersionInstance, player);
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException ex2) {
            final ReflectiveOperationException ex;
            final ReflectiveOperationException e = ex;
            e.printStackTrace();
            return 0;
        }
    }
    
    public static boolean hasViaVersion() {
        if (IIUA.getCurrentTimeMillis() - ViaVersionAdapter.lastCheckedViaVersion > 5000L) {
            ViaVersionAdapter.lastCheckedViaVersion = IIUA.getCurrentTimeMillis();
            ViaVersionAdapter.hasViaVersion = Bukkit.getServer().getPluginManager().isPluginEnabled("ViaVersion");
        }
        return ViaVersionAdapter.hasViaVersion;
    }
    
    static {
        ViaVersionAdapter.hasViaVersion = false;
        ViaVersionAdapter.lastCheckedViaVersion = 0L;
    }
}
