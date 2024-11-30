// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.other;

import org.bukkit.entity.Player;
import de.jpx3.intave.util.calc.analysis.StringUtils;
import org.bukkit.ChatColor;
import java.net.InetAddress;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.Plugin;
import de.jpx3.intave.util.enums.CheatCategory;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.event.Listener;
import de.jpx3.intave.util.objectable.IntaveCheck;

public class MultipleAccountsCheck extends IntaveCheck implements Listener
{
    private final IntavePlugin plugin;
    
    public MultipleAccountsCheck(final IntavePlugin plugin) {
        super("MultipleAccounts", CheatCategory.OTHER);
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this.plugin);
    }
    
    @EventHandler
    public void on(final AsyncPlayerPreLoginEvent e) {
        if (!this.isActivated()) {
            return;
        }
        final long accountsRegisteredWithIp = this.plugin.getServer().getOnlinePlayers().parallelStream().filter(o -> o.getAddress().getAddress().equals(e.getAddress())).count();
        final long maxAccountsAllowed = Math.max(1, this.plugin.getConfig().getInt(this.getConfigPath() + ".maximum-accounts-for-ip"));
        if (maxAccountsAllowed < accountsRegisteredWithIp) {
            e.disallow(PlayerPreLoginEvent.Result.KICK_BANNED, this.replaceTags(this.plugin.getConfig().getString(this.getConfigPath() + ".kick-message"), e.getUniqueId(), e.getAddress()));
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
