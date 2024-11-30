// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.module;

import de.jpx3.intave.api.external.linked.event.AsyncIntaveCommandTriggerEvent;
import org.bukkit.command.CommandSender;
import de.jpx3.intave.api.external.linked.event.iIntaveExternalEvent;
import org.bukkit.entity.Player;
import java.util.Iterator;
import org.bukkit.plugin.Plugin;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import java.util.Map;
import de.jpx3.intave.IntavePlugin;

public final class CommandWaveManager
{
    private final IntavePlugin plugin;
    private final Map<UUID, String> currentBanWave;
    
    public CommandWaveManager(final IntavePlugin plugin) {
        this.plugin = plugin;
        this.currentBanWave = new ConcurrentHashMap<UUID, String>();
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)plugin, (Runnable)new Runnable() {
            @Override
            public void run() {
                CommandWaveManager.this.executeBanWave();
                CommandWaveManager.this.clearBanWave();
            }
        }, 200L, plugin.getConfig().getLong("violation.banwave.exe_delay") / 50L);
    }
    
    public void addPlayerToBanWave(final UUID uuid, final String command) {
        this.currentBanWave.put(uuid, command);
    }
    
    public boolean containsPlayer(final UUID uuid) {
        return this.currentBanWave.keySet().contains(uuid);
    }
    
    public void removePlayerFromBanWave(final UUID uuid) {
        this.currentBanWave.remove(uuid);
    }
    
    public void executeBanWave() {
        for (final Map.Entry<UUID, String> f : this.currentBanWave.entrySet()) {
            if (this.plugin.getServer().getPlayer((UUID)f.getKey()) == null) {
                continue;
            }
            if (!this.plugin.getServer().getPlayer((UUID)f.getKey()).isOnline()) {
                this.executeCommandSilent(f.getValue());
            }
            else {
                this.executeCommand(this.plugin.getServer().getPlayer((UUID)f.getKey()), f.getValue());
            }
        }
    }
    
    public final Map<UUID, String> getCurrentBanWave() {
        return this.currentBanWave;
    }
    
    private void executeCommand(final Player p, String command) {
        command = command.trim();
        final AsyncIntaveCommandTriggerEvent newAsyncIntaveCommandTriggerEvent = this.plugin.getRetributionManager().getIntaveCommandTriggerEvent(p, command, true);
        this.plugin.getIntaveAPIService().fireEvent(newAsyncIntaveCommandTriggerEvent);
        if (newAsyncIntaveCommandTriggerEvent.isCancelled()) {
            return;
        }
        this.plugin.getServer().dispatchCommand((CommandSender)this.plugin.getServer().getConsoleSender(), command);
        this.plugin.getILogger().logToFile("(CommandWaveManager) Executed command '" + command + "' for player " + p.getName());
    }
    
    private void executeCommandSilent(String command) {
        command = command.trim();
        this.plugin.getServer().dispatchCommand((CommandSender)this.plugin.getServer().getConsoleSender(), command);
        this.plugin.getILogger().logToFile("(CommandWaveManager) Executed command '" + command + "' for player null");
    }
    
    public void clearBanWave() {
        this.currentBanWave.clear();
    }
}
