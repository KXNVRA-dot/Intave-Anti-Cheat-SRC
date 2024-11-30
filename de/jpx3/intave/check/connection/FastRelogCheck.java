// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.connection;

import org.bukkit.event.EventHandler;
import de.jpx3.intave.antipiracy.IIUA;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.Plugin;
import java.util.concurrent.ConcurrentHashMap;
import de.jpx3.intave.util.enums.CheatCategory;
import de.jpx3.intave.IntavePlugin;
import java.util.UUID;
import java.net.InetAddress;
import java.util.Map;
import org.bukkit.event.Listener;
import de.jpx3.intave.util.objectable.IntaveCheck;

public final class FastRelogCheck extends IntaveCheck implements Listener
{
    private final Map<InetAddress, Long> lastKicked;
    private final Map<UUID, Long> lastKickedACC;
    private final IntavePlugin plugin;
    
    public FastRelogCheck(final IntavePlugin plugin) {
        super("FastRelog", CheatCategory.OTHER);
        this.lastKicked = new ConcurrentHashMap<InetAddress, Long>();
        this.lastKickedACC = new ConcurrentHashMap<UUID, Long>();
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)plugin);
    }
    
    @EventHandler
    public void on(final AsyncPlayerPreLoginEvent e) {
        if (!this.isActivated()) {
            return;
        }
        final long delay = this.plugin.getConfig().getLong(this.plugin.getThresholdsManager().getConfigPathFromName("fastrelog") + ".millies");
        final long ipDelayLeft = IIUA.getCurrentTimeMillis() - this.lastKicked.getOrDefault(e.getAddress(), 0L);
        final long accDelayLeft = IIUA.getCurrentTimeMillis() - this.lastKickedACC.getOrDefault(e.getUniqueId(), 0L);
        if (ipDelayLeft < delay || accDelayLeft < delay) {
            e.setKickMessage(this.plugin.getPrefix() + this.plugin.getConfig().getString(this.getConfigPath() + ".message"));
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST);
            this.registerKick(e.getAddress(), e.getUniqueId(), "rejoin");
        }
        else {
            this.lastKicked.remove(e.getAddress());
            this.lastKickedACC.remove(e.getUniqueId());
        }
    }
    
    public void registerKick(final InetAddress address, final UUID uuid, final String check) {
        final boolean isProtectedByTheConfiguration = this.plugin.getConfig().getStringList(this.getConfigPath() + ".ignore").stream().anyMatch(s -> s.equalsIgnoreCase(check));
        if (!isProtectedByTheConfiguration) {
            if (!address.getHostAddress().contains("127.0.0.1")) {
                this.lastKicked.put(address, IIUA.getCurrentTimeMillis());
            }
            this.lastKickedACC.put(uuid, IIUA.getCurrentTimeMillis());
        }
    }
}
