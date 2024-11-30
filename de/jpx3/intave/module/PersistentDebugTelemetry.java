// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.module;

import org.bukkit.permissions.Permissible;
import org.bukkit.ChatColor;
import de.jpx3.intave.api.external.IntavePermission;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import de.jpx3.intave.util.calc.MathHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public final class PersistentDebugTelemetry
{
    public static DebugType currentDebugType;
    
    public static void teleport(final Entity entity, final Location location, final String reason) {
        debugMessage(entity.getName() + " teleported to (w: " + location.getWorld().getName() + ",x: " + location.getX() + ",y: " + location.getY() + ",z: " + location.getZ() + ")  | reason: " + reason, DebugType.TELEPORTATION);
        entity.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND);
    }
    
    public static void movementDuration(final Player player, final double durationInMS) {
        debugMessage("Move of " + player.getName() + " took " + MathHelper.roundFromDouble(durationInMS, 5) + "ms to verify", DebugType.MOVEMENTDURATION);
    }
    
    public static void hitCancel(final Player player, final Cancellable cancellable, final String reason) {
        if (cancellable.isCancelled()) {
            return;
        }
        cancellable.setCancelled(true);
        debugMessage("Rejected attack of " + player.getName() + " | reason: " + reason, DebugType.ATTACK_CANCEL);
    }
    
    private static void debugMessage(final String message, final DebugType type) {
        if (!PersistentDebugTelemetry.currentDebugType.equals(DebugType.NONE) && (PersistentDebugTelemetry.currentDebugType.equals(type) || PersistentDebugTelemetry.currentDebugType.equals(DebugType.ALL))) {
            Bukkit.getOnlinePlayers().stream().filter(o -> IntavePlugin.getStaticReference().getPermissionManager().hasPermission(IntavePermission.ADMIN_DEBUG, o)).forEach(o -> o.sendMessage(IntavePlugin.getStaticReference().getPrefix() + "[DEBUG] [" + type.name() + "] " + ChatColor.RED + message));
            IntavePlugin.getStaticReference().getILogger().debug(message);
        }
    }
    
    static {
        PersistentDebugTelemetry.currentDebugType = DebugType.NONE;
    }
    
    public enum DebugType
    {
        NONE, 
        ALL, 
        ATTACK_CANCEL, 
        TELEPORTATION, 
        MOVEMENTDURATION, 
        EXCEPTIONHANDLING;
    }
}
