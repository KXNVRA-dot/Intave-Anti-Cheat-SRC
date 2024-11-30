// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.other;

import java.util.function.Predicate;
import de.jpx3.intave.util.data.BlockData;
import java.util.Set;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTeleportEvent;
import de.jpx3.intave.util.event.CheckableDamageEntityEvent;
import org.bukkit.entity.Entity;
import de.jpx3.intave.util.calc.LocationHelper;
import org.bukkit.entity.Player;
import de.jpx3.intave.util.calc.PlayerUtils;
import de.jpx3.intave.util.event.CheckableMoveEvent;
import java.util.Iterator;
import org.bukkit.Bukkit;
import de.jpx3.intave.antipiracy.IIUA;
import org.bukkit.plugin.Plugin;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import de.jpx3.intave.util.enums.CheatCategory;
import org.bukkit.block.Block;
import java.util.List;
import org.bukkit.Location;
import java.util.UUID;
import java.util.Map;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.event.Listener;
import de.jpx3.intave.util.objectable.IntaveCheck;

public final class TeamingCheck extends IntaveCheck implements Listener
{
    private final IntavePlugin plugin;
    private final Map<UUID, Map<UUID, PlayerData>> otherPlayerData;
    private final int maxTeamSize;
    private final double scanRadius;
    private final double spawnRadius;
    private final Map<Location, Integer> possibleSpawnLocations;
    private final List<Block> cachedList;
    
    public TeamingCheck(final IntavePlugin plugin) {
        super("Teaming", CheatCategory.COMBAT);
        this.otherPlayerData = new ConcurrentHashMap<UUID, Map<UUID, PlayerData>>();
        this.possibleSpawnLocations = new ConcurrentHashMap<Location, Integer>();
        this.cachedList = new CopyOnWriteArrayList<Block>();
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this.plugin);
        this.maxTeamSize = this.plugin.getConfig().getInt(this.getConfigPath() + ".max_team_size");
        this.scanRadius = this.plugin.getConfig().getDouble(this.getConfigPath() + ".scan_radius");
        this.spawnRadius = this.plugin.getConfig().getDouble(this.getConfigPath() + ".spawn_radius");
        final Iterator<UUID> iterator;
        UUID uuid;
        Map<UUID, PlayerData> playerDataMap;
        int peopleTeamingWith;
        final Iterator<Map.Entry<UUID, PlayerData>> iterator2;
        Map.Entry<UUID, PlayerData> playerData;
        boolean isSuspiciousForTeaming;
        int teamSize;
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)plugin, () -> {
            if (!(!this.isActivated())) {
                this.otherPlayerData.keySet().iterator();
                while (iterator.hasNext()) {
                    uuid = iterator.next();
                    playerDataMap = this.otherPlayerData.get(uuid);
                    peopleTeamingWith = 0;
                    playerDataMap.entrySet().iterator();
                    while (iterator2.hasNext()) {
                        playerData = iterator2.next();
                        if (plugin.getServer().getOfflinePlayer((UUID)playerData.getKey()).isOnline()) {
                            isSuspiciousForTeaming = (IIUA.getCurrentTimeMillis() - playerData.getValue().lastAttackedByPlayer > 2000L && playerData.getValue().ticksNearby > 40);
                            if (isSuspiciousForTeaming) {
                                ++peopleTeamingWith;
                            }
                            else {
                                continue;
                            }
                        }
                    }
                    teamSize = peopleTeamingWith + 1;
                    if (teamSize > this.maxTeamSize) {
                        plugin.getRetributionManager().markPlayer(Bukkit.getPlayer(uuid), teamSize - this.maxTeamSize, "Teaming", CheatCategory.COMBAT, "Player seems to be teaming. (" + teamSize + " players in team)");
                    }
                }
            }
        }, 40L, 40L);
    }
    
    @Override
    public void onCheckableMove(final CheckableMoveEvent e) {
        if (e.isCancelled() || !this.isActivated()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        final List<Entity> nearbyEntities = PlayerUtils.getNearbyEntities(p, p.getLocation(), this.scanRadius);
        if (!this.otherPlayerData.containsKey(IIUA.getUUIDFrom(p))) {
            this.otherPlayerData.put(IIUA.getUUIDFrom(p), new ConcurrentHashMap<UUID, PlayerData>());
        }
        final Player player;
        final double distanceToSpawnPoint;
        final boolean canSeeOther;
        PlayerData playerData2;
        nearbyEntities.stream().filter(entity -> entity instanceof Player).map(entity -> entity).forEachOrdered(otherPlayer -> {
            if (!this.otherPlayerData.get(IIUA.getUUIDFrom(player)).containsKey(IIUA.getUUIDFrom(otherPlayer))) {
                this.otherPlayerData.get(IIUA.getUUIDFrom(player)).put(IIUA.getUUIDFrom(otherPlayer), new PlayerData(IIUA.getUUIDFrom(otherPlayer)));
            }
            distanceToSpawnPoint = LocationHelper.getDistanceSafe(player.getLocation(), this.getMostLiklySpawnLocation());
            canSeeOther = this.canAttack(player, otherPlayer);
            if (this.otherPlayerData.get(IIUA.getUUIDFrom(player)).get(IIUA.getUUIDFrom(otherPlayer)).ticksNearby < 50) {
                playerData2 = this.otherPlayerData.get(IIUA.getUUIDFrom(player)).get(IIUA.getUUIDFrom(otherPlayer));
                playerData2.ticksNearby += (canSeeOther ? ((distanceToSpawnPoint < this.spawnRadius) ? 0 : 2) : 0);
            }
            return;
        });
        this.otherPlayerData.get(IIUA.getUUIDFrom(p)).values().forEach(playerData -> playerData.setTicksNearby((playerData.getTicksNearby() > 0) ? (playerData.getTicksNearby() - 1) : 0));
    }
    
    @Override
    public void onCheckableDamageEntity(final CheckableDamageEntityEvent e) {
        final Player p = e.getBukkitPlayer();
        if (!this.otherPlayerData.containsKey(IIUA.getUUIDFrom(p))) {
            this.otherPlayerData.put(IIUA.getUUIDFrom(p), new ConcurrentHashMap<UUID, PlayerData>());
        }
        if (e.getEntity() instanceof Player) {
            final Player otherPlayer = (Player)e.getEntity();
            if (!this.otherPlayerData.get(IIUA.getUUIDFrom(p)).containsKey(IIUA.getUUIDFrom(otherPlayer))) {
                this.otherPlayerData.get(IIUA.getUUIDFrom(p)).put(IIUA.getUUIDFrom(otherPlayer), new PlayerData(IIUA.getUUIDFrom(otherPlayer)));
            }
            this.otherPlayerData.get(IIUA.getUUIDFrom(p)).get(IIUA.getUUIDFrom(otherPlayer)).lastAttackedByPlayer = IIUA.getCurrentTimeMillis();
        }
    }
    
    @EventHandler
    public void on(final PlayerTeleportEvent e) {
        if (!this.isActivated()) {
            return;
        }
        final Player player = e.getPlayer();
        final Location from = e.getFrom();
        final Location to = e.getTo();
        final double dist = LocationHelper.getDistanceSafe(from, to);
        if (dist > 10.0) {
            this.otherPlayerData.entrySet().stream().filter(uuidMapEntry -> this.otherPlayerData.get(uuidMapEntry.getKey()).containsKey(player.getUniqueId())).forEach(uuidMapEntry -> this.otherPlayerData.get(uuidMapEntry.getKey()).get(player.getUniqueId()).ticksNearby = 0);
            if (this.containsLocation(to, this.possibleSpawnLocations.keySet(), 0.5)) {
                final Location location = this.getLocationFrom(to, this.possibleSpawnLocations.keySet(), 0.5);
                this.possibleSpawnLocations.put(location, this.possibleSpawnLocations.get(location) + 1);
            }
            else {
                this.possibleSpawnLocations.put(to, 1);
            }
        }
    }
    
    @EventHandler
    public void on(final PlayerQuitEvent e) {
        this.clearData(e.getPlayer());
    }
    
    private void clearData(final Player p) {
        synchronized (this.otherPlayerData) {
            this.otherPlayerData.remove(p.getUniqueId());
            final PlayerData playerData;
            this.otherPlayerData.forEach((key, value) -> playerData = value.remove(p.getUniqueId()));
        }
    }
    
    private Location getMostLiklySpawnLocation() {
        Location locationWithMaxTeleports = null;
        int maxTeleports = 0;
        for (final Map.Entry<Location, Integer> locationTeleports : this.possibleSpawnLocations.entrySet()) {
            if (locationTeleports.getValue() > maxTeleports) {
                locationWithMaxTeleports = locationTeleports.getKey();
                maxTeleports = locationTeleports.getValue();
            }
        }
        return locationWithMaxTeleports;
    }
    
    private Location getLocationFrom(final Location location, final Set<Location> locationSet, final double max_dist) {
        return locationSet.stream().filter(location1 -> LocationHelper.getDistanceSafe(location, location1) < max_dist).findFirst().orElse(location);
    }
    
    private boolean containsLocation(final Location location, final Set<Location> locationSet, final double max_dist) {
        return locationSet.stream().anyMatch(location1 -> LocationHelper.getDistanceSafe(location, location1) < max_dist);
    }
    
    private boolean canAttack(final Player player, final Player target) {
        return this.blocksFromTwoPoints(player.getEyeLocation(), target.getEyeLocation()).stream().allMatch((Predicate<? super Object>)BlockData::isPassable);
    }
    
    private synchronized List<Block> blocksFromTwoPoints(final Location loc1, final Location loc2) {
        synchronized (this.cachedList) {
            this.cachedList.clear();
            final List<Block> blocks = this.cachedList;
            final int topBlockX = (loc1.getBlockX() < loc2.getBlockX()) ? loc2.getBlockX() : loc1.getBlockX();
            final int bottomBlockX = (loc1.getBlockX() > loc2.getBlockX()) ? loc2.getBlockX() : loc1.getBlockX();
            final int topBlockY = (loc1.getBlockY() < loc2.getBlockY()) ? loc2.getBlockY() : loc1.getBlockY();
            final int bottomBlockY = (loc1.getBlockY() > loc2.getBlockY()) ? loc2.getBlockY() : loc1.getBlockY();
            final int topBlockZ = (loc1.getBlockZ() < loc2.getBlockZ()) ? loc2.getBlockZ() : loc1.getBlockZ();
            final int bottomBlockZ = (loc1.getBlockZ() > loc2.getBlockZ()) ? loc2.getBlockZ() : loc1.getBlockZ();
            for (int x = bottomBlockX; x <= topBlockX; ++x) {
                for (int z = bottomBlockZ; z <= topBlockZ; ++z) {
                    for (int y = bottomBlockY; y <= topBlockY; ++y) {
                        blocks.add(loc1.getWorld().getBlockAt(x, y, z));
                    }
                }
            }
            return blocks;
        }
    }
    
    public class PlayerData
    {
        private final UUID playerUUID;
        private long lastAttackedByPlayer;
        private int ticksNearby;
        
        public PlayerData(final UUID playerUUID) {
            this.playerUUID = playerUUID;
        }
        
        public void wasAttacked() {
            this.lastAttackedByPlayer = IIUA.getCurrentTimeMillis();
        }
        
        public UUID getPlayerUUID() {
            return this.playerUUID;
        }
        
        public void setTicksNearby(final int ticksNearby) {
            this.ticksNearby = ticksNearby;
        }
        
        public int getTicksNearby() {
            return this.ticksNearby;
        }
    }
}
