// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.module;

import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import de.jpx3.intave.util.objectable.IntaveCheck;
import de.jpx3.intave.api.external.IntavePermissionOverrideHook;
import org.bukkit.event.Event;
import org.bukkit.Bukkit;
import de.jpx3.intave.api.external.linked.event.iIntaveExternalEvent;
import de.jpx3.intave.api.external.IntaveHandle;
import java.util.concurrent.ConcurrentHashMap;
import de.jpx3.intave.api.external.TelicCheckMirror;
import java.util.Map;
import de.jpx3.intave.IntavePlugin;

public final class IntaveAPIService
{
    private final IntavePlugin plugin;
    private final Map<String, TelicCheckMirror> mirrorMap;
    
    public IntaveAPIService(final IntavePlugin plugin) {
        this.mirrorMap = new ConcurrentHashMap<String, TelicCheckMirror>();
        this.plugin = plugin;
    }
    
    public void startup() {
        this.insertIntaveAPI(this.createIntaveAPI());
    }
    
    private void insertIntaveAPI(final IntaveHandle api) {
        this.plugin.setIntaveHandleAPI(api);
        this.plugin.getILogger().info("API was started");
    }
    
    public void fireEvent(final iIntaveExternalEvent externalEvent) {
        Bukkit.getPluginManager().callEvent((Event)externalEvent);
    }
    
    private IntaveHandle createIntaveAPI() {
        return new IntaveHandle() {
            @Override
            public final void setPermissionOverrideHook(final IntavePermissionOverrideHook hook) {
                IntaveAPIService.this.plugin.getPermissionManager().setPermControlHook(hook);
            }
            
            @Override
            public final TelicCheckMirror getCheckMirrorOf(final String simpleCheckName) {
                if (!IntaveAPIService.this.mirrorMap.containsKey(simpleCheckName)) {
                    final IntaveCheck check = IntaveAPIService.this.plugin.getCheckManager().getCheck(simpleCheckName.toLowerCase());
                    IntaveAPIService.this.mirrorMap.put(simpleCheckName.toLowerCase(), new TelicCheckMirror() {
                        @Override
                        public final String getName() {
                            return check.getName();
                        }
                        
                        @Override
                        public final String getCategory() {
                            return ChatColor.stripColor(check.getCategory().getName());
                        }
                        
                        @Override
                        public final int getViolationLevelOf(final Player player) {
                            if (!IntaveAPIService.this.plugin.isLinkedToIntave(player.getUniqueId())) {
                                throw new IllegalStateException(player.getName() + " is not synced");
                            }
                            return IntaveAPIService.this.plugin.getViolationManager().getViolationLevel(player, this.getName().toLowerCase());
                        }
                        
                        @Override
                        public final void enable() {
                            check.setActivated(true);
                        }
                        
                        @Override
                        public final void disable() {
                            check.setActivated(false);
                        }
                        
                        @Override
                        public final boolean isEnabled() {
                            return check.isActivated();
                        }
                    });
                }
                final TelicCheckMirror mirror = IntaveAPIService.this.mirrorMap.get(simpleCheckName);
                return mirror;
            }
        };
    }
}
