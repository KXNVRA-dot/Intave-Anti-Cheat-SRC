// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.module;

import org.bukkit.event.EventHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.permissions.Permissible;
import de.jpx3.intave.api.external.IntavePermission;
import java.util.Arrays;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.event.Listener;

public final class UpdateNotifyManager implements Listener
{
    private final IntavePlugin plugin;
    
    public UpdateNotifyManager(final IntavePlugin plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this.plugin);
    }
    
    @EventHandler
    public void on(final PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        if (Arrays.stream(IntaveVersionState.values()).noneMatch(intaveVersionState -> intaveVersionState.name().equalsIgnoreCase("OUTDATED"))) {
            this.plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this.plugin, () -> p.kickPlayer("de.jpx3.intave.api.external.linked.exceptions.IntaveInternalException: Illegal version state"));
            return;
        }
        if (!this.plugin.getPermissionManager().hasPermission(IntavePermission.ADMIN_NOTIFY, (Permissible)p)) {
            return;
        }
        final String currentVersion = this.plugin.getVersion();
        final String newestVersion = this.plugin.getNewestVersion();
        final IntaveVersionState versionState = this.getStateFrom(currentVersion, newestVersion);
        final Enum enum1;
        final String s;
        final CommandSender p2;
        final String str;
        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this.plugin, () -> {
            if (enum1.equals(IntaveVersionState.OUTDATED)) {
                this.sendMessage(ChatColor.RED + "This server runs an outdated version of intave (" + s + ")", p2);
                this.sendMessage(ChatColor.RED + "Please upgrade intave now to version " + str + " to keep in charge against cheating", p2);
                this.sendMessage(ChatColor.RED + "If you continue to use this version of intave, it might no longer work", p2);
            }
            else if (enum1.equals(IntaveVersionState.EXPERIMENTAL)) {
                this.sendMessage(ChatColor.YELLOW + "This server runs an experimental version of intave (" + s + ")", p2);
                this.sendMessage(ChatColor.YELLOW + "It is possible that bugs occur", p2);
            }
        }, 10L);
    }
    
    public void sendMessage(final String message, final CommandSender p) {
        p.sendMessage(this.plugin.getPrefix() + message);
    }
    
    public IntaveVersionState getStateFrom(final String currentVersion, final String newestVersion) {
        final double currentVersionInt = this.parseIntaveVersionFromString(currentVersion);
        final double newestVersionInt = this.parseIntaveVersionFromString(newestVersion);
        final boolean needsToBeUpgraded = newestVersionInt > currentVersionInt;
        final boolean isExperimentalVersion = newestVersionInt < currentVersionInt;
        return needsToBeUpgraded ? IntaveVersionState.OUTDATED : (isExperimentalVersion ? IntaveVersionState.EXPERIMENTAL : IntaveVersionState.UP_TO_DATE);
    }
    
    double parseIntaveVersionFromString(String versionName) {
        if (versionName.isEmpty()) {
            return 0.0;
        }
        double buildId = 0.0;
        if (versionName.contains("-")) {
            final String[] g = versionName.split("-");
            versionName = versionName.split("-")[0];
            buildId = removeAlphaCharsAndGetInt(g[1]) / 100.0;
        }
        while (versionName.contains(".")) {
            versionName = versionName.replace('.', ' ');
        }
        versionName = versionName.trim().replaceAll(" ", "");
        return removeAlphaCharsAndGetInt(versionName) + buildId;
    }
    
    private static int removeAlphaCharsAndGetInt(final String string) {
        if (string.isEmpty()) {
            return 0;
        }
        final char[] output = new char[string.length()];
        int g = 0;
        for (final char c : string.toCharArray()) {
            if (Character.isDigit(c)) {
                output[g++] = c;
            }
        }
        return (int)Double.parseDouble(String.valueOf(output).trim());
    }
    
    public enum IntaveVersionState
    {
        OUTDATED("Outdated"), 
        UP_TO_DATE("Up to date"), 
        EXPERIMENTAL("Experimental");
        
        protected final String fancyName;
        
        private IntaveVersionState(final String fancyName) {
            this.fancyName = fancyName;
        }
        
        public final String getFancyName() {
            return this.fancyName;
        }
    }
}
