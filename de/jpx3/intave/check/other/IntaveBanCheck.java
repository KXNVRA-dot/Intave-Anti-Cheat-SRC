// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.other;

import de.jpx3.intave.util.calc.analysis.StringUtils;
import org.bukkit.ChatColor;
import java.net.InetAddress;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import de.jpx3.intave.api.external.linked.event.AsyncIntaveCommandTriggerEvent;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import de.jpx3.intave.api.external.linked.event.iIntaveExternalEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import de.jpx3.intave.util.enums.CheatCategory;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.event.Listener;
import de.jpx3.intave.util.objectable.IntaveCheck;

public final class IntaveBanCheck extends IntaveCheck implements Listener
{
    private final IntavePlugin plugin;
    
    public IntaveBanCheck(final IntavePlugin plugin) {
        super("IntaveWebPanelBan", CheatCategory.OTHER);
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this.plugin);
    }
    
    @EventHandler
    public void on(final PlayerJoinEvent e) {
        if (!this.isActivated()) {
            return;
        }
        final Player p = e.getPlayer();
        final int score;
        final int max_score;
        final Player p2;
        String message;
        AsyncIntaveCommandTriggerEvent newAsyncIntaveCommandTriggerEvent;
        this.plugin.getServer().getScheduler().scheduleAsyncDelayedTask((Plugin)this.plugin, () -> {
            score = this.plugin.getServerStatisticsGateawayAdapter().getRiskLevel(e.getPlayer().getUniqueId());
            max_score = this.plugin.getConfig().getInt(this.getConfigPath() + ".min-prob-for-command");
            if (score >= max_score) {
                message = this.replaceTags(this.plugin.getConfig().getString(this.getConfigPath() + ".login-deny-command"), p2.getName(), p2.getUniqueId(), p2.getAddress().getAddress());
                newAsyncIntaveCommandTriggerEvent = this.plugin.getRetributionManager().getIntaveCommandTriggerEvent(p2, message, false);
                this.plugin.getIntaveAPIService().fireEvent(newAsyncIntaveCommandTriggerEvent);
                if (!newAsyncIntaveCommandTriggerEvent.isCancelled()) {
                    this.plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this.plugin, () -> this.plugin.getServer().dispatchCommand((CommandSender)this.plugin.getServer().getConsoleSender(), message));
                }
            }
        });
    }
    
    private String replaceTags(String message, final String playername, final UUID uuid, final InetAddress address) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        message = StringUtils.saveReplace("%prefix%", message, this.plugin.getPrefix() + "");
        message = StringUtils.saveReplace("%uuid%", message, uuid.toString());
        message = StringUtils.saveReplace("%playername%", message, playername);
        message = StringUtils.saveReplace("%ip%", message, address.getHostAddress());
        return message;
    }
}
