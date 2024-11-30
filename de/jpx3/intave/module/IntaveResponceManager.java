// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.module;

import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import de.jpx3.intave.antipiracy.IIUA;
import org.bukkit.permissions.Permissible;
import de.jpx3.intave.api.external.IntavePermission;
import org.bukkit.ChatColor;
import de.jpx3.intave.util.enums.iNotifyType;
import de.jpx3.intave.IntavePlugin;

public final class IntaveResponceManager
{
    private final IntavePlugin plugin;
    
    public IntaveResponceManager(final IntavePlugin plugin) {
        this.plugin = plugin;
    }
    
    void sendFlagData(final String message, final iNotifyType type) {
        if (this.plugin.getConfig().getBoolean("logging.console_log")) {
            this.plugin.getServer().getConsoleSender().sendMessage(message);
        }
        if (type.equals(iNotifyType.NOTIFY)) {
            this.plugin.getILogger().logToFile("(Notify) " + ChatColor.stripColor(message));
            this.plugin.getServer().getOnlinePlayers().stream().filter(p -> this.plugin.getPermissionManager().hasPermission(IntavePermission.ADMIN_NOTIFY, (Permissible)p) && this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).isReciving(iNotifyType.NOTIFY)).forEachOrdered(p -> p.sendMessage(message));
        }
        else if (type.equals(iNotifyType.VERBOSE)) {
            IntavePlugin.getStaticReference().getServer().getOnlinePlayers().stream().filter(p -> this.plugin.getPermissionManager().hasPermission(IntavePermission.ADMIN_VERBOSE, (Permissible)p) && IntavePlugin.getStaticReference().catchCheckable(IIUA.getUUIDFrom(p)).isReciving(iNotifyType.VERBOSE)).forEachOrdered(p -> p.sendMessage(message));
        }
    }
    
    public void rejectNoPermMessage(final CommandSender p) {
        p.sendMessage(this.plugin.getPrefix() + ChatColor.RED + "Access denied: Insufficient permissions");
    }
}
