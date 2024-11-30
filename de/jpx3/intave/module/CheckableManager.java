// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.module;

import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.block.BlockFace;
import de.jpx3.intave.util.data.BlockData;
import de.jpx3.intave.util.calc.LocationHelper;
import de.jpx3.intave.util.event.CheckableMoveEvent;
import de.jpx3.intave.util.event.CheckableVelocityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import de.jpx3.intave.util.calc.PlayerUtils;
import java.util.function.Consumer;
import de.jpx3.intave.antipiracy.IIUA;
import org.bukkit.entity.Player;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.plugin.Plugin;
import de.jpx3.intave.util.objectable.Checkable;
import java.util.UUID;
import java.util.Map;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.event.Listener;

public final class CheckableManager implements Listener
{
    private final IntavePlugin plugin;
    private final Map<UUID, Checkable> checkableHashMap;
    private final Checkable emptyCheckable;
    private boolean cacheClearInProgress;
    
    public CheckableManager(final IntavePlugin plugin) {
        this.cacheClearInProgress = false;
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)plugin);
        this.checkableHashMap = new ConcurrentHashMap<UUID, Checkable>();
        this.registerAllOnlinePlayers();
        (this.emptyCheckable = new Checkable(null)).setup();
    }
    
    public final Checkable getCheckable(final UUID uuid) {
        if (!this.isUUIDLinkedToIntave(uuid)) {
            if (!this.cacheClearInProgress) {
                this.cacheClearInProgress = true;
                this.plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this.plugin, () -> {
                    if (!this.cacheClearInProgress) {
                        return;
                    }
                    else {
                        this.cacheClearInProgress = false;
                        this.cleanCache();
                        this.plugin.getServer().getOnlinePlayers().stream().filter(p -> !this.isUUIDLinkedToIntave(IIUA.getUUIDFrom(p))).forEachOrdered(this::registerCheckable);
                        return;
                    }
                });
            }
            return this.emptyCheckable;
        }
        return this.checkableHashMap.get(uuid);
    }
    
    public final Map<UUID, Checkable> getCheckables() {
        return this.checkableHashMap;
    }
    
    public final synchronized boolean isUUIDLinkedToIntave(final UUID uuid) {
        return this.checkableHashMap.containsKey(uuid);
    }
    
    private synchronized void registerCheckable(final Player bukkitPlayer) {
        final UUID uuid = bukkitPlayer.getUniqueId();
        if (this.isUUIDLinkedToIntave(uuid)) {
            this.unregisterCheckable(uuid);
        }
        final Checkable checkable = new Checkable(bukkitPlayer);
        checkable.setup();
        if (!this.checkableHashMap.containsKey(uuid)) {
            this.checkableHashMap.put(uuid, checkable);
        }
    }
    
    private synchronized void unregisterCheckable(final UUID uuid) {
        if (this.isUUIDLinkedToIntave(uuid)) {
            this.checkableHashMap.remove(uuid);
        }
    }
    
    private synchronized void cleanCache() {
        this.checkableHashMap.keySet().stream().filter(uuid -> !PlayerUtils.isOnline(uuid)).forEachOrdered((Consumer<? super Object>)this::unregisterCheckable);
    }
    
    private synchronized void registerAllOnlinePlayers() {
        this.plugin.getServer().getOnlinePlayers().stream().filter(p -> !this.isUUIDLinkedToIntave(IIUA.getUUIDFrom(p))).forEachOrdered(this::registerCheckable);
    }
    
    @EventHandler
    public void on(final PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        this.registerCheckable(p);
    }
    
    @EventHandler
    public void on(final PlayerQuitEvent e) {
        this.unregisterCheckable(e.getPlayer().getUniqueId());
    }
    
    void preRefreshVelocityData(final CheckableVelocityEvent e) {
        final Player p = e.getBukkitPlayer();
    }
    
    void preRefreshMovementData(final CheckableMoveEvent e) {
        final Player p = e.getBukkitPlayer();
        final Block blockStandingOn = LocationHelper.getBlockAt(e.getFrom().clone().subtract(0.0, 0.25, 0.0));
        final boolean isOnGroundR = LocationHelper.isOnGroundR(p.getLocation());
        final Block fromBlock = LocationHelper.getBlockAt(e.getFrom());
        final Block toBlock = LocationHelper.getBlockAt(e.getTo());
        final Block eyeLocation = LocationHelper.getBlockAt(e.getBukkitPlayer().getEyeLocation());
        if (p.getLocation().getBlockY() - p.getLocation().getY() == 0.0 || IIUA.isPretendingToBeOnGround(p)) {
            final Checkable.CheckableMeta.SyncedValues syncedValues = e.getCheckable().getMeta().getSyncedValues();
            ++syncedValues.ticksOnGround;
            if (LocationHelper.isOnGroundQ(p)) {
                e.getCheckable().getMeta().getLocationValues().locationsMidair.clear();
            }
        }
        else {
            e.getCheckable().getMeta().getSyncedValues().ticksOnGround = 0L;
        }
        if (p.isFlying() || IIUA.getAllowFlight(p)) {
            e.getCheckable().getMeta().getTimedValues().lastTimeFlying = IIUA.getCurrentTimeMillis();
        }
        if (BlockData.doesAffectMovement(eyeLocation)) {
            e.getCheckable().getMeta().getTimedValues().lastTimeHeadInHeap = IIUA.getCurrentTimeMillis();
        }
        if (fromBlock.isLiquid() || toBlock.isLiquid()) {
            e.getCheckable().getMeta().getTimedValues().lastTimeInWater = IIUA.getCurrentTimeMillis();
            final Checkable.CheckableMeta.SyncedValues syncedValues2 = e.getCheckable().getMeta().getSyncedValues();
            ++syncedValues2.ticksInLiquid;
            if (toBlock.getRelative(BlockFace.DOWN).isLiquid() && fromBlock.getRelative(BlockFace.DOWN).isLiquid()) {
                final Checkable.CheckableMeta.SyncedValues syncedValues3 = e.getCheckable().getMeta().getSyncedValues();
                ++syncedValues3.ticksInLiquitFloating;
            }
            else {
                e.getCheckable().getMeta().getSyncedValues().ticksInLiquitFloating = 0L;
            }
        }
        else {
            e.getCheckable().getMeta().getSyncedValues().ticksInLiquid = 0L;
        }
        if (LocationHelper.couldCollide(p.getLocation())) {
            e.getCheckable().getMeta().getTimedValues().lastTimeCollisitionWasPossible = IIUA.getCurrentTimeMillis();
        }
        if (fromBlock.getRelative(BlockFace.DOWN).getType().name().contains("PISTON")) {
            e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnAPiston = IIUA.getCurrentTimeMillis();
        }
        if (LocationHelper.isOnGroundAccurate(e.getTo(), (Entity)p) || LocationHelper.isOnGroundAccurate(e.getFrom(), (Entity)p)) {
            e.getCheckable().getMeta().getTimedValues().lastTimeOnGroundACC = IIUA.getCurrentTimeMillis();
        }
        if (LocationHelper.isOnGround(p) || BlockData.isClimbable(fromBlock)) {
            e.getCheckable().getMeta().getSyncedValues().ticksInAir = 0L;
            e.getCheckable().getMeta().getLocationValues().locationsMidair.clear();
        }
        else {
            e.getCheckable().getMeta().getLocationValues().locationsMidair.add(e.getFrom());
            final Checkable.CheckableMeta.SyncedValues syncedValues4 = e.getCheckable().getMeta().getSyncedValues();
            ++syncedValues4.ticksInAir;
        }
        if (fromBlock.getType().equals((Object)Material.WATER_LILY) || fromBlock.getType().equals((Object)Material.CARPET)) {
            e.getCheckable().getMeta().getTimedValues().lastTimeMovedOnWaterConsistant = IIUA.getCurrentTimeMillis();
        }
        final int size = 5;
        if (this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().lastYMotions.size() < size) {
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().lastYMotions.add(e.getYDirectedMotion());
        }
        else {
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().lastYMotions.remove(0);
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().lastYMotions.add(e.getYDirectedMotion());
        }
        if (IIUA.isPretendingToBeOnGround(p)) {
            if (!LocationHelper.couldCollide(p.getLocation())) {
                if (e.getCheckable().getMeta().getSyncedValues().ticksInAir_experimental < 4L && e.getCheckable().getMeta().getSyncedValues().ticksInAir_experimental > 0L && e.getCheckable().getMeta().getVioValues().suspiciousLowPackets < 4 && e.getCheckable().getMeta().getTimedValues().lastTimeCollidedWithTopBlock < 500L && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnASlime > 2000L) {
                    final Checkable.CheckableMeta.ViolationValues vioValues = e.getCheckable().getMeta().getVioValues();
                    vioValues.suspiciousLowPackets += 2;
                }
                else if (e.getCheckable().getMeta().getVioValues().suspiciousLowPackets > 0) {
                    final Checkable.CheckableMeta.ViolationValues vioValues2 = e.getCheckable().getMeta().getVioValues();
                    --vioValues2.suspiciousLowPackets;
                }
            }
            e.getCheckable().getMeta().getSyncedValues().ticksInAir_experimental = 0L;
        }
        else {
            final Checkable.CheckableMeta.SyncedValues syncedValues5 = e.getCheckable().getMeta().getSyncedValues();
            ++syncedValues5.ticksInAir_experimental;
        }
        if (p.isSneaking()) {
            final Checkable.CheckableMeta.SyncedValues syncedValues6 = e.getCheckable().getMeta().getSyncedValues();
            ++syncedValues6.ticksSneaking;
        }
        else {
            e.getCheckable().getMeta().getSyncedValues().ticksSneaking = 0L;
        }
        if (!BlockData.isPassable(blockStandingOn)) {
            switch (blockStandingOn.getType()) {
                case ICE:
                case PACKED_ICE: {
                    e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnIce = IIUA.getCurrentTimeMillis();
                    break;
                }
                case SLIME_BLOCK: {
                    e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnASlime = IIUA.getCurrentTimeMillis();
                    break;
                }
                case SOUL_SAND: {
                    if (BlockData.isPassable(fromBlock) && !BlockData.doesAffectMovement(fromBlock)) {
                        e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnSoulSand = IIUA.getCurrentTimeMillis();
                        break;
                    }
                    break;
                }
            }
        }
        else if (BlockData.doesAffectMovement(fromBlock)) {
            e.getCheckable().getMeta().getTimedValues().lastTimeStandingInsideBlock = IIUA.getCurrentTimeMillis();
        }
        if (IIUA.isPretendingToBeOnGround(p) && isOnGroundR) {
            e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnABlock = IIUA.getCurrentTimeMillis();
            e.getCheckable().getMeta().getLocationValues().lastLocationOnGround = e.getFrom();
        }
        if (LocationHelper.isOnGroundAccurate(e.getFrom(), (Entity)p)) {
            e.getCheckable().getMeta().getLocationValues().lastLocationOnGroundACC = e.getFrom();
        }
        if (LocationHelper.getBlockAt(p.getLocation().clone().subtract(0.0, 0.5, 0.0)).getType().equals((Object)Material.SLIME_BLOCK)) {
            e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnASlime = IIUA.getCurrentTimeMillis();
        }
        final boolean jump = e.getYDirectedMotion() > 0.0 && e.getCheckable().getMeta().getSyncedValues().lastYmovement <= 0.0;
        if (jump) {
            e.getCheckable().getMeta().getSyncedValues().isInSlimeJump = (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnASlime < 400L || LocationHelper.collidesFar(p.getWorld(), (Entity)p, p.getLocation(), Material.SLIME_BLOCK));
        }
        if (fromBlock.getType().equals((Object)Material.LADDER) || toBlock.getType().equals((Object)Material.VINE) || fromBlock.getType().equals((Object)Material.VINE) || toBlock.getType().equals((Object)Material.LADDER)) {
            e.getCheckable().getMeta().getTimedValues().lastTimeClimbedALadder = IIUA.getCurrentTimeMillis();
            final Checkable.CheckableMeta.SyncedValues syncedValues7 = e.getCheckable().getMeta().getSyncedValues();
            ++syncedValues7.ticksOnLadder;
        }
        else {
            e.getCheckable().getMeta().getSyncedValues().ticksOnLadder = 0L;
        }
        boolean playerMayhapsInWeb = false;
        final Location webCheckLocation = p.getLocation().clone();
        if (LocationHelper.collidesXZAxisFar(e.getTo().getWorld(), (Entity)e.getBukkitPlayer(), e.getTo(), Material.SLIME_BLOCK)) {
            e.getCheckable().getMeta().getTimedValues().lastTimeSlimeWasNear = IIUA.getCurrentTimeMillis();
        }
        if (LocationHelper.collidesStepable(p.getWorld(), (Entity)p, e.getTo()) || e.getYDirectedMotion() == 0.5) {
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeStepableNearby = IIUA.getCurrentTimeMillis();
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeInCobweb > 900L) {
            for (int i = 0; i <= 2; ++i) {
                if (this.checkSides(LocationHelper.getBlockAt(webCheckLocation), Material.WEB)) {
                    playerMayhapsInWeb = true;
                }
                webCheckLocation.add(0.0, 1.0, 0.0);
            }
            if (fromBlock.getType().equals((Object)Material.WEB) || fromBlock.getRelative(BlockFace.UP).getType().equals((Object)Material.WEB) || fromBlock.getRelative(BlockFace.UP).getRelative(BlockFace.UP).getType().equals((Object)Material.WEB) || playerMayhapsInWeb) {
                e.getCheckable().getMeta().getTimedValues().lastTimeInCobweb = IIUA.getCurrentTimeMillis();
            }
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeCollidedWithTopBlock > 100L) {
            final Block oneAboveToBlock = toBlock.getRelative(BlockFace.UP).getRelative(BlockFace.UP);
            if (!BlockData.isPassable(fromBlock) || BlockData.doesAffectMovementSimple(oneAboveToBlock) || (BlockData.doesAffectMovementSimple(oneAboveToBlock.getRelative(BlockFace.NORTH)) && !BlockData.doesAffectMovementSimple(oneAboveToBlock.getRelative(BlockFace.NORTH).getRelative(BlockFace.DOWN))) || (BlockData.doesAffectMovementSimple(oneAboveToBlock.getRelative(BlockFace.WEST)) && !BlockData.doesAffectMovementSimple(oneAboveToBlock.getRelative(BlockFace.WEST).getRelative(BlockFace.DOWN))) || (BlockData.doesAffectMovementSimple(oneAboveToBlock.getRelative(BlockFace.SOUTH)) && !BlockData.doesAffectMovementSimple(oneAboveToBlock.getRelative(BlockFace.SOUTH).getRelative(BlockFace.DOWN))) || (BlockData.doesAffectMovementSimple(oneAboveToBlock.getRelative(BlockFace.EAST)) && !BlockData.doesAffectMovementSimple(oneAboveToBlock.getRelative(BlockFace.EAST).getRelative(BlockFace.DOWN))) || (e.getCheckable().getMeta().getSyncedValues().ticksOnGround < 3L && e.getCheckable().getMeta().getSyncedValues().ticksOnGround > 0L && BlockData.isPassable(fromBlock) && BlockData.doesAffectMovementSimple(e.getFrom().getBlock()))) {
                e.getCheckable().getMeta().getTimedValues().lastTimeCollidedWithTopBlock = IIUA.getCurrentTimeMillis();
            }
            if (LocationHelper.isInsideMotionRelevant(p.getWorld(), (Entity)p, p.getLocation().clone().add(0.0, 0.04, 0.0))) {
                e.getCheckable().getMeta().getTimedValues().lastTimeCollidedWithTopBlock = IIUA.getCurrentTimeMillis();
            }
        }
        if (((IIUA.isPretendingToBeOnGround(p) && isOnGroundR && p.getVelocity().getY() > -0.8) || !BlockData.isPassable(p.getLocation().clone().subtract(0.0, 1.0E-5, 0.0).getBlock()) || LocationHelper.collidesLiquid((Entity)p) || fromBlock.getType().equals((Object)Material.WEB)) && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeFlagged > 1200L) {
            e.getCheckable().getMeta().getLocationValues().lastSafeLocationBeforeRE = p.getLocation();
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void on3(final EntityDamageByEntityEvent e) {
        if (e.isCancelled()) {
            this.plugin.catchCheckable(e.getDamager().getUniqueId()).getMeta().getTimedValues().lastTimeHitCanceled = IIUA.getCurrentTimeMillis();
        }
    }
    
    @EventHandler
    public void on(final PlayerVelocityEvent e) {
        final Player p = e.getPlayer();
        if (this.plugin.isLinkedToIntave(IIUA.getUUIDFrom(p))) {
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastHitByEntityTimestamp = IIUA.getCurrentTimeMillis();
        }
    }
    
    @EventHandler
    public void on(final PlayerToggleFlightEvent e) {
        if (this.plugin.isLinkedToIntave(e.getPlayer().getUniqueId())) {
            this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getTimedValues().lastFlightToggle = IIUA.getCurrentTimeMillis();
        }
    }
    
    @EventHandler
    public void on(final EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player) {
            if (IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(e.getEntity().getUniqueId()).getMeta().getTimedValues().lastTimeStandingOnABlock > 3000L && this.plugin.catchCheckable(e.getEntity().getUniqueId()).getMeta().getSyncedValues().lastYmovement > -3.0) {
                return;
            }
            this.plugin.catchCheckable(e.getEntity().getUniqueId()).getMeta().getTimedValues().lastHitByEntityTimestamp = IIUA.getCurrentTimeMillis();
            if (e.getDamager() instanceof LivingEntity) {
                final ItemStack i = ((LivingEntity)e.getDamager()).getEquipment().getItemInHand();
                if (i != null && i.hasItemMeta() && i.getItemMeta().hasEnchant(Enchantment.KNOCKBACK)) {
                    this.plugin.catchCheckable(e.getEntity().getUniqueId()).getMeta().getTimedValues().lastTimeHittedWithKnockbackItem = IIUA.getCurrentTimeMillis();
                }
                this.plugin.catchCheckable(e.getEntity().getUniqueId()).getMeta().getTimedValues().lastHitByPlayerTimestamp = IIUA.getCurrentTimeMillis();
            }
        }
    }
    
    @EventHandler
    public void on2(final EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Projectile) {
            this.plugin.catchCheckable(e.getEntity().getUniqueId()).getMeta().getTimedValues().lastHitByProjectile = IIUA.getCurrentTimeMillis();
        }
    }
    
    @EventHandler
    public void on(final PlayerDeathEvent e) {
        this.plugin.catchCheckable(e.getEntity().getUniqueId()).getMeta().getTimedValues().lastDeath = IIUA.getCurrentTimeMillis();
    }
    
    @EventHandler
    public void on(final EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            this.plugin.catchCheckable(e.getEntity().getUniqueId()).getMeta().getTimedValues().lastDamageTakenTimestamp = IIUA.getCurrentTimeMillis();
        }
    }
    
    @EventHandler
    public void on(final PlayerTeleportEvent e) {
        if (this.plugin.isLinkedToIntave(e.getPlayer().getUniqueId())) {
            this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getLocationValues().lastSafeLocationBeforeRE = e.getTo();
            this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getTimedValues().lastTimeTeleported = IIUA.getCurrentTimeMillis();
            this.plugin.catchCheckable(e.getPlayer().getUniqueId()).removePacketsFromBalanceCalc(2);
            if (!e.getFrom().getWorld().getUID().equals(e.getTo().getWorld().getUID())) {
                return;
            }
            if (e.getFrom().distance(e.getTo()) > 4.0) {
                this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getTimedValues().lastTimeTeleportedLongDistance = IIUA.getCurrentTimeMillis();
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final PlayerRespawnEvent e) {
        if (!this.plugin.isLinkedToIntave(e.getPlayer().getUniqueId())) {
            return;
        }
        this.plugin.catchCheckable(e.getPlayer().getUniqueId()).removePacketsFromBalanceCalc(7);
        this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getLocationValues().lastSafeLocationBeforeRE = e.getRespawnLocation();
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final PlayerInteractAtEntityEvent e) {
        final Player p = e.getPlayer();
        if (!this.plugin.isLinkedToIntave(e.getPlayer().getUniqueId())) {
            return;
        }
        switch (e.getRightClicked().getType()) {
            case BOAT:
            case MINECART:
            case HORSE:
            case PIG: {
                this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeInteractedWithVehicle = IIUA.getCurrentTimeMillis();
                break;
            }
        }
    }
    
    @EventHandler
    public void on(final VehicleExitEvent e) {
        if (e.getExited() instanceof Player && e.getVehicle() != null) {
            final Player p = (Player)e.getExited();
            if (!this.plugin.isLinkedToIntave(IIUA.getUUIDFrom(p))) {
                return;
            }
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeInteractedWithVehicle = IIUA.getCurrentTimeMillis();
        }
    }
    
    @EventHandler
    public void on(final BlockPlaceEvent e) {
        final Player p = e.getPlayer();
        if (this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues().cancelBlockPlacement) {
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues().cancelBlockPlacement = false;
            e.setCancelled(true);
            return;
        }
        if (IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getTimedValues().lastBlockPlaceBlockRequest < 2000L) {
            e.setCancelled(true);
            return;
        }
        if (!e.getBlock().getWorld().equals(e.getPlayer().getWorld())) {
            return;
        }
        final double hsDist = e.getBlock().getRelative(BlockFace.UP).getLocation().distance(e.getPlayer().getLocation());
        if (hsDist < 1.0 && e.getBlock().getRelative(BlockFace.DOWN).getType().equals((Object)Material.AIR) && this.collides(e.getBlock()) < 2) {
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeBlockPlacedUnder = IIUA.getCurrentTimeMillis();
        }
        this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeBlockPlaced = IIUA.getCurrentTimeMillis();
    }
    
    private int collides(final Block b) {
        int hs = 0;
        if (!b.getRelative(BlockFace.SOUTH).getType().equals((Object)Material.AIR)) {
            ++hs;
        }
        if (!b.getRelative(BlockFace.EAST).getType().equals((Object)Material.AIR)) {
            ++hs;
        }
        if (!b.getRelative(BlockFace.NORTH).getType().equals((Object)Material.AIR)) {
            ++hs;
        }
        if (!b.getRelative(BlockFace.WEST).getType().equals((Object)Material.AIR)) {
            ++hs;
        }
        return hs;
    }
    
    @EventHandler
    public void on(final PlayerGameModeChangeEvent e) {
        if (this.plugin.isLinkedToIntave(e.getPlayer().getUniqueId())) {
            this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getTimedValues().lastGameModeChange = IIUA.getCurrentTimeMillis();
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final BlockBreakEvent e) {
        if (IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getTimedValues().lastBlockBreakBlockRequest < 2000L) {
            e.setCancelled(true);
        }
        this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getTimedValues().lastTimeBlockBreak = IIUA.getCurrentTimeMillis();
    }
    
    @EventHandler
    public void on(final EntityExplodeEvent e) {
        this.plugin.getServer().getOnlinePlayers().stream().filter(p -> p.getLocation().getWorld().getUID().equals(e.getEntity().getLocation().getWorld().getUID())).filter(p -> p.getLocation().distance(e.getEntity().getLocation()) < 3.0).forEachOrdered(p -> this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeBlockExplodeAffect = IIUA.getCurrentTimeMillis());
    }
    
    @EventHandler
    public void on(final PlayerInteractEvent e) {
        if (e.getAction().equals((Object)Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock().getType().equals((Object)Material.BED_BLOCK)) {
            this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getTimedValues().lastTimeInteractedWithABed = IIUA.getCurrentTimeMillis();
            this.plugin.catchCheckable(e.getPlayer().getUniqueId()).removePacketsFromBalanceCalc(2);
        }
    }
    
    private boolean checkSides(final Block b, final Material tosearch) {
        return b.getRelative(BlockFace.NORTH).getType().equals((Object)tosearch) || b.getRelative(BlockFace.EAST).getType().equals((Object)tosearch) || b.getRelative(BlockFace.SOUTH).getType().equals((Object)tosearch) || b.getRelative(BlockFace.WEST).getType().equals((Object)tosearch) || b.getRelative(BlockFace.NORTH_EAST).getType().equals((Object)tosearch) || b.getRelative(BlockFace.NORTH_WEST).getType().equals((Object)tosearch) || b.getRelative(BlockFace.SOUTH_WEST).getType().equals((Object)tosearch) || b.getRelative(BlockFace.SOUTH_EAST).getType().equals((Object)tosearch);
    }
}
