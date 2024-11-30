// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.connection;

import de.jpx3.intave.util.calc.analysis.StringUtils;
import org.bukkit.ChatColor;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import java.net.InetAddress;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.Plugin;
import de.jpx3.intave.util.enums.CheatCategory;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.event.Listener;
import de.jpx3.intave.util.objectable.IntaveCheck;

public final class ProxyCheck extends IntaveCheck implements Listener
{
    private final IntavePlugin plugin;
    
    public ProxyCheck(final IntavePlugin plugin) {
        super("ProxyCheck", CheatCategory.NETWORK);
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this.plugin);
    }
    
    @EventHandler
    public void on(final AsyncPlayerPreLoginEvent e) {
        if (!this.isActivated()) {
            return;
        }
        final InetAddress ipAddress = e.getAddress();
        final String hostAddress = ipAddress.getHostAddress();
        final String hostName = ipAddress.getHostName();
        final boolean isStatic = hostName.isEmpty() || hostAddress.equalsIgnoreCase(hostName);
        if (isStatic && this.plugin.getServerStatisticsGateawayAdapter().isUsingVPN(ipAddress)) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, this.replaceTags(this.plugin.getConfig().getString(this.getConfigPath() + ".kick-message"), e.getUniqueId(), e.getAddress()));
        }
    }
    
    private String replaceTags(String message, final UUID uuid, final InetAddress address) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        message = StringUtils.saveReplace("%prefix%", message, this.plugin.getPrefix() + "");
        message = StringUtils.saveReplace("%uuid%", message, uuid.toString());
        message = StringUtils.saveReplace("%ip%", message, address.getHostAddress());
        return message.replaceAll("\\s{2,}", " ").trim();
    }
}
