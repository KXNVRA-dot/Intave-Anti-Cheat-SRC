// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.combat;

import org.bukkit.Location;
import java.util.function.Consumer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import de.jpx3.intave.antipiracy.IIUA;
import org.bukkit.entity.Player;
import de.jpx3.intave.util.enums.CheatCategory;
import java.util.List;
import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.util.objectable.IntaveCheck;

public final class EntitySpoofCheck extends IntaveCheck
{
    private final IntavePlugin plugin;
    public static final List<SpoofedEntityGroup> globalEntityGroups;
    
    public EntitySpoofCheck(final IntavePlugin plugin) {
        super("entityspoof", CheatCategory.COMBAT);
        this.plugin = plugin;
    }
    
    public void setupPlayer(final Player observer) {
    }
    
    private void setupDefaultGroups() {
    }
    
    private EntitySpoofServer getEntitySpoofServer(final Player player) {
        return this.plugin.catchCheckable(IIUA.getUUIDFrom(player)).getMeta().getSystemValues().entitySpoofServer;
    }
    
    static {
        globalEntityGroups = new CopyOnWriteArrayList<SpoofedEntityGroup>();
    }
    
    public static class EntitySpoofServer
    {
        private final Map<String, SpoofedEntityGroup> spoofedEntityGroupMap;
        
        public EntitySpoofServer() {
            this.spoofedEntityGroupMap = new ConcurrentHashMap<String, SpoofedEntityGroup>();
        }
        
        public final void loadDefaultGroupsToMap() {
            final SpoofedEntityGroup spoofedEntityGroup2;
            EntitySpoofCheck.globalEntityGroups.forEach(spoofedEntityGroup -> spoofedEntityGroup2 = this.spoofedEntityGroupMap.put(spoofedEntityGroup.getName(), spoofedEntityGroup));
        }
        
        public final SpoofedEntityGroup getGroupByName(final String name) {
            return this.spoofedEntityGroupMap.get(name);
        }
        
        public final boolean isGroupAttached(final String groupName) {
            return this.spoofedEntityGroupMap.containsKey(groupName);
        }
        
        public final void death() {
            this.spoofedEntityGroupMap.values().forEach(SpoofedEntityGroup::death);
        }
    }
    
    public class SpoofedEntityGroup
    {
        private final Player observer;
        private final String name;
        private boolean visibility;
        private final List<SpoofedEntityHandle> spoofedEntityHandleMap;
        
        public SpoofedEntityGroup(final Player observer, final String name) {
            this.spoofedEntityHandleMap = new CopyOnWriteArrayList<SpoofedEntityHandle>();
            this.observer = observer;
            this.name = name;
        }
        
        public final Player getObserver() {
            return this.observer;
        }
        
        public final String getName() {
            return this.name;
        }
        
        public synchronized boolean isVisible() {
            return this.visibility;
        }
        
        public synchronized void setVisibility(final boolean newVisibility) {
            if (newVisibility == this.visibility) {
                return;
            }
            this.visibility = newVisibility;
            if (newVisibility) {
                this.executeForAll(spoofedEntityHandle -> spoofedEntityHandle.spawn(this.observer));
            }
            else {
                this.executeForAll(spoofedEntityHandle -> spoofedEntityHandle.despawn(this.observer));
            }
        }
        
        private synchronized void executeForAll(final Consumer<? super SpoofedEntityHandle> action) {
            this.spoofedEntityHandleMap.forEach(action);
        }
        
        public void addAndInjectEntityHandle(final SpoofedEntityHandle spoofedEntityHandle) {
            this.spoofedEntityHandleMap.add(spoofedEntityHandle);
        }
        
        public void singleTeleport(final int rowId, final Location location) {
            this.spoofedEntityHandleMap.get(rowId).teleport(location);
        }
        
        public void death() {
            this.executeForAll(SpoofedEntityHandle::death);
            this.spoofedEntityHandleMap.clear();
        }
    }
    
    public interface SpoofedEntityHandle
    {
        void init();
        
        void death();
        
        void teleport(final Location p0);
        
        void spawn(final Player p0);
        
        void despawn(final Player p0);
        
        boolean isAlive();
        
        int getEntityId();
        
        Location getLocation();
    }
}
