// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.module;

import java.util.Optional;
import java.util.ArrayList;
import de.jpx3.intave.antipiracy.IIUA;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import de.jpx3.intave.api.external.IntavePermission;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import de.jpx3.intave.api.external.IntavePermissionOverrideHook;
import de.jpx3.intave.IntavePlugin;

public final class PermissionManager
{
    private final IntavePlugin plugin;
    private IntavePermissionOverrideHook permControlHook;
    private final Map<UUID, List<PermissionCache>> permissionCache;
    
    public PermissionManager(final IntavePlugin plugin) {
        this.permControlHook = null;
        this.permissionCache = new ConcurrentHashMap<UUID, List<PermissionCache>>();
        this.plugin = plugin;
    }
    
    public boolean hasPermission(final IntavePermission permission, final Permissible commandSender) {
        if (!(commandSender instanceof Player)) {
            return commandSender instanceof ConsoleCommandSender;
        }
        final Player player = (Player)commandSender;
        final UUID playerUUID = player.getUniqueId();
        if (this.permControlHook != null) {
            return this.permControlHook.hasPermission(permission, player);
        }
        if (this.permissionCache.containsKey(playerUUID)) {
            final Optional<PermissionCache> g = this.permissionCache.get(playerUUID).stream().filter(permissionCache1 -> permissionCache1.getPermissionName().equalsIgnoreCase(permission.getBukkitName())).findFirst();
            if (g.isPresent()) {
                final PermissionCache permissionCache2 = g.get();
                if (IIUA.getCurrentTimeMillis() - permissionCache2.getLastChecked() < 5000L) {
                    return permissionCache2.hasPermission();
                }
            }
        }
        else {
            this.permissionCache.put(playerUUID, new ArrayList<PermissionCache>());
        }
        final boolean bukkitFetched = player.hasPermission(permission.getBukkitName());
        synchronized (this.permissionCache) {
            final Optional<PermissionCache> g2 = this.permissionCache.get(playerUUID).stream().filter(permissionCache1 -> permissionCache1.getPermissionName().equalsIgnoreCase(permission.getBukkitName())).findFirst();
            if (g2.isPresent()) {
                g2.get().renew(bukkitFetched);
            }
            else {
                this.permissionCache.get(playerUUID).add(new PermissionCache(permission.getBukkitName(), bukkitFetched));
            }
        }
        return bukkitFetched;
    }
    
    public void setPermControlHook(final IntavePermissionOverrideHook hook) {
        this.permControlHook = hook;
    }
    
    private class PermissionCache
    {
        private String permissionName;
        private boolean hasPermission;
        private long lastChecked;
        
        private PermissionCache(final String permissionName, final boolean hasPermission) {
            this.permissionName = permissionName;
            this.hasPermission = hasPermission;
            this.lastChecked = IIUA.getCurrentTimeMillis();
        }
        
        private String getPermissionName() {
            return this.permissionName;
        }
        
        private boolean hasPermission() {
            return this.hasPermission;
        }
        
        private long getLastChecked() {
            return this.lastChecked;
        }
        
        private void renew(final boolean hasPermission) {
            this.hasPermission = hasPermission;
            this.lastChecked = IIUA.getCurrentTimeMillis();
        }
    }
}
