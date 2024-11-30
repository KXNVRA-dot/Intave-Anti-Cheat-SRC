// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.data;

import org.bukkit.Server;
import org.bukkit.Bukkit;
import java.lang.reflect.Field;

public class TPSUtils
{
    private static Object minecraftServer;
    private static Field recentTps;
    
    public static double[] getRecentTps() {
        try {
            if (TPSUtils.minecraftServer == null) {
                final Server server = Bukkit.getServer();
                final Field consoleField = server.getClass().getDeclaredField("console");
                consoleField.setAccessible(true);
                TPSUtils.minecraftServer = consoleField.get(server);
            }
            if (TPSUtils.recentTps == null) {
                (TPSUtils.recentTps = TPSUtils.minecraftServer.getClass().getSuperclass().getDeclaredField("recentTps")).setAccessible(true);
            }
            return (double[])TPSUtils.recentTps.get(TPSUtils.minecraftServer);
        }
        catch (IllegalAccessException | NoSuchFieldException ex) {
            return new double[] { 17.0, 17.0, 17.0 };
        }
    }
}
