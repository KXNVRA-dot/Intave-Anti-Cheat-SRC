// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.antipiracy;

import de.jpx3.intave.util.objectable.Checkable;
import java.util.UUID;
import java.net.InetAddress;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;

public final class IIUA
{
    public static int getDataFromBlock(final Block block) {
        return (block == null) ? 0 : block.getData();
    }
    
    public static boolean isPretendingToBeOnGround(final Player player) {
        if (player == null) {
            return false;
        }
        if (!IntavePlugin.getStaticReference().isLinkedToIntave(getUUIDFrom(player))) {
            return player.isOnGround();
        }
        synchronized (IntavePlugin.getStaticReference().catchCheckable(getUUIDFrom(player))) {
            return IntavePlugin.getStaticReference().catchCheckable(getUUIDFrom(player)).getMeta().getSyncedValues().claimingInActiveTaskToBeOnGround;
        }
    }
    
    public static InetAddress getSharedAddress(final Player player) throws NullPointerException {
        return (player == null) ? null : player.getAddress().getAddress();
    }
    
    public static UUID getUUIDFrom(final Player player) {
        return (player == null) ? UUID.randomUUID() : player.getUniqueId();
    }
    
    public static boolean getAllowFlight(final Player player) {
        if (player.getAllowFlight()) {
            final Checkable checkable;
            final Checkable checkable2 = checkable = IntavePlugin.getStaticReference().catchCheckable(getUUIDFrom(player));
            synchronized (checkable2) {
                return getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastTimeToggledFlight < 800 + checkable.getPing() || player.isFlying();
            }
        }
        return false;
    }
    
    public static long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }
}
