// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.module;

import de.jpx3.intave.util.event.CheckableDamageEntityEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import de.jpx3.intave.util.data.BlockData;
import de.jpx3.intave.util.calc.MathHelper;
import de.jpx3.intave.util.calc.YawUtil;
import de.jpx3.intave.util.event.CheckableMoveEvent;
import org.bukkit.block.BlockFace;
import org.bukkit.Location;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import de.jpx3.intave.api.external.linked.exceptions.IntaveInternalException;
import org.bukkit.entity.Entity;
import java.util.Objects;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import de.jpx3.intave.util.IntaveExceptionHandler;
import de.jpx3.intave.api.external.linked.exceptions.IntaveException;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import de.jpx3.intave.util.calc.LocationHelper;
import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerRespawnEvent;
import java.lang.reflect.InvocationTargetException;
import de.jpx3.intave.api.internal.reflections.Reflections;
import de.jpx3.intave.util.calc.PlayerUtils;
import de.jpx3.intave.util.data.MaterialData;
import de.jpx3.intave.util.enums.CheatCategory;
import org.bukkit.Material;
import com.comphenix.protocol.events.PacketListener;
import de.jpx3.intave.util.objectable.Checkable;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;
import de.jpx3.intave.check.movement.KnockbackCheck;
import de.jpx3.intave.util.event.iIntaveInternalEvent;
import de.jpx3.intave.util.event.CheckableVelocityEvent;
import org.bukkit.util.Vector;
import de.jpx3.intave.antipiracy.IIUA;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import de.jpx3.intave.api.internal.ProtocolLibAdapter;
import org.bukkit.plugin.Plugin;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import de.jpx3.intave.api.internal.system.ServerInformation;
import java.util.concurrent.CopyOnWriteArrayList;
import de.jpx3.intave.util.async.OrderingExecutor;
import java.util.List;
import de.jpx3.intave.check.connection.MoveCheck;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.event.Listener;

public final class EventManager implements Listener
{
    private final IntavePlugin plugin;
    public static boolean handleMoveEvents;
    public static boolean handleMoveEventsSync;
    public static boolean debug;
    private MoveCheck moveCheck;
    private volatile long totalDuration;
    private final List<?> unusedEmptyList;
    private final OrderingExecutor executor;
    
    public EventManager(final IntavePlugin plugin) {
        this.unusedEmptyList = new CopyOnWriteArrayList<Object>();
        this.executor = new OrderingExecutor(new ThreadPoolExecutor(0, (int)((int)ServerInformation.getAllowedThreadSize() * 1.5), 8L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(800), new OrderingExecutor.IntaveThreadFactory()));
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this.plugin);
        final IntavePlugin plugin2 = this.plugin;
        ProtocolLibAdapter.getProtocolManager().addPacketListener((PacketListener)new PacketAdapter(this.plugin, ListenerPriority.LOW, new PacketType[] { PacketType.Play.Server.ENTITY_VELOCITY }) {
            public void onPacketSending(final PacketEvent event) {
                final Player p = event.getPlayer();
                final PacketContainer packetContainer = event.getPacket();
                if (packetContainer.getIntegers().getValues().get(0) == p.getEntityId()) {
                    final double x = packetContainer.getIntegers().getValues().get(1) / 8000.0;
                    final double y = packetContainer.getIntegers().getValues().get(2) / 8000.0;
                    final double z = packetContainer.getIntegers().getValues().get(3) / 8000.0;
                    final Checkable checkable = plugin2.catchCheckable(IIUA.getUUIDFrom(p));
                    if (IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastTimeVelocityIGrequested < 100L && !checkable.getMeta().getSyncedValues().hadPullDownVelocity) {
                        checkable.getMeta().getSyncedValues().hadPullDownVelocity = true;
                        return;
                    }
                    double lFIY = -9999.0;
                    if (checkable.getMeta().getSyncedValues().lastVelocityFI != null && checkable.getMeta().getSyncedValues().ticksVelocityMidAir < 6L) {
                        lFIY = checkable.getMeta().getSyncedValues().lastVelocityFI.getY();
                    }
                    checkable.getMeta().getSyncedValues().ticksVelocityMidAir = 0L;
                    checkable.getMeta().getSyncedValues().lastVelocity = new Vector(x, y, z);
                    checkable.getMeta().getSyncedValues().lastVelocityFI = new Vector(x, (Math.abs(Math.max(y, lFIY)) < 0.005) ? 0.0 : Math.max(y, lFIY), z);
                    if (!checkable.getMeta().getSyncedValues().willGetArtificialVelocity) {
                        checkable.getMeta().getSyncedValues().lastNonArtifVelocityFI = new Vector(x, Math.max(y, lFIY), z);
                    }
                    checkable.getMeta().getTimedValues().lastTimeVelocityAdded = IIUA.getCurrentTimeMillis();
                    checkable.getMeta().getLocationValues().lastVelocity = checkable.getVerifiedLocation();
                    CheckableVelocityEvent newEvent;
                    if (checkable.checkableVelocityEvent == null) {
                        newEvent = new CheckableVelocityEvent(p, new Vector(x, y, z));
                        checkable.checkableVelocityEvent = newEvent;
                    }
                    else {
                        newEvent = checkable.checkableVelocityEvent;
                        newEvent.renewEvent(new Vector(x, y, z));
                    }
                    plugin2.getCheckableManager().preRefreshVelocityData(newEvent);
                    plugin2.getCheckManager().hook(newEvent);
                    if (newEvent.isCancelled()) {
                        event.setCancelled(true);
                        return;
                    }
                    if (KnockbackCheck.verify_packet) {
                        EventManager.this.sendAdditionalVelocityPacketTo(p, newEvent.getCheckable().getMeta().getSyncedValues().verifMotionVec3D, newEvent.getVelocity());
                        checkable.getMeta().getTimedValues().lastTimeFakeVelocityApplied = IIUA.getCurrentTimeMillis();
                    }
                }
            }
        });
        ProtocolLibAdapter.getProtocolManager().addPacketListener((PacketListener)new PacketAdapter(plugin, new PacketType[] { PacketType.Play.Client.BLOCK_PLACE, PacketType.Play.Client.BLOCK_DIG }) {
            public void onPacketReceiving(final PacketEvent event) {
                final Player p = event.getPlayer();
                final PacketContainer packet = event.getPacket();
                if (p.getItemInHand() != null) {
                    final boolean usable = this.isValidFood(p.getItemInHand().getType(), p) || (p.getItemInHand().getType().equals((Object)Material.BOW) && p.getInventory().contains(Material.ARROW)) || p.getItemInHand().getType().name().contains("SWORD");
                    if (IIUA.getCurrentTimeMillis() - plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastItemUsePacketsCounterResett > 1000L) {
                        plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().itemuseCounter_last = plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().itemuseCounter;
                        plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().itemuseCounter = 0;
                        plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastItemUsePacketsCounterResett = IIUA.getCurrentTimeMillis();
                    }
                    if (packet.getType().equals((Object)PacketType.Play.Client.BLOCK_DIG) && usable && plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().itemuseCounter < 18) {
                        plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeUnusedItem = IIUA.getCurrentTimeMillis();
                        plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().isUsingItem = false;
                    }
                    else if (usable) {
                        plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeUsedItem = IIUA.getCurrentTimeMillis();
                        plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().isUsingItem = true;
                        final Checkable.CheckableMeta.SyncedValues syncedValues = plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues();
                        syncedValues.itemuseCounter += 2;
                    }
                    if (plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().itemuseCounter > 35 && usable) {
                        if (plugin2.getCheckManager().getCheck("Celerity").isActivated()) {
                            plugin2.getRetributionManager().markPlayer(p, plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().itemuseCounter - 10, "Celerity", CheatCategory.NETWORK, "clicked too quickly (" + plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().itemuseCounter + " c/s)");
                        }
                        plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().isUsingItem = true;
                    }
                }
            }
            
            private boolean isValidFood(final Material m, final Player p) {
                return MaterialData.isValidFood(m, p);
            }
        });
        ProtocolLibAdapter.getProtocolManager().addPacketListener((PacketListener)new PacketAdapter(plugin, new PacketType[] { PacketType.Play.Client.USE_ENTITY }) {
            public void onPacketReceiving(final PacketEvent event) {
                final Player player = event.getPlayer();
                plugin2.catchCheckable(IIUA.getUUIDFrom(player)).getMeta().getTimedValues().lastTimeLeftClickedEntity = IIUA.getCurrentTimeMillis();
            }
        });
        plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)plugin, () -> EventManager.handleMoveEventsSync = !plugin.getConfig().getBoolean(plugin.getThresholdsManager().getConfigPathFromName("move") + ".async_movement_handling"));
        final int threads;
        final int maxThreads;
        final int remainingCapacity;
        plugin.getServer().getScheduler().scheduleAsyncRepeatingTask((Plugin)plugin, () -> {
            threads = ((ThreadPoolExecutor)this.executor.getDelegate()).getPoolSize();
            maxThreads = ((ThreadPoolExecutor)this.executor.getDelegate()).getMaximumPoolSize();
            remainingCapacity = ((ThreadPoolExecutor)this.executor.getDelegate()).getQueue().remainingCapacity();
            PlayerUtils.sendMessage("Jpx3", "Using " + threads + "/" + maxThreads + " threads with a remaining capacity of " + remainingCapacity + " task-slots");
        }, 80L, 80L);
    }
    
    private void sendAdditionalVelocityPacketTo(final Player player, final Vector playerMotion, final Vector vector) {
        try {
            final Object packet = Reflections.getNmsClass("PacketPlayOutExplosion").getConstructor(Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, List.class, Reflections.getNmsClass("Vec3D")).newInstance(0.0, 0.0, 0.0, -1.0f, this.unusedEmptyList, Reflections.getNmsClass("Vec3D").getConstructor(Double.TYPE, Double.TYPE, Double.TYPE).newInstance(vector.getX() * 1.25, 0, vector.getZ() * 1.25));
            Reflections.sendPacket(player, packet);
        }
        catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ex2) {
            final ReflectiveOperationException ex;
            final ReflectiveOperationException e = ex;
            e.printStackTrace();
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void on(final PlayerRespawnEvent e) {
        this.plugin.catchCheckable(IIUA.getUUIDFrom(e.getPlayer())).getMeta().getSyncedValues().syncedMovements = 0L;
        this.plugin.catchCheckable(IIUA.getUUIDFrom(e.getPlayer())).getMeta().getTimedValues().lastTeleportNano = System.nanoTime();
        this.plugin.catchCheckable(IIUA.getUUIDFrom(e.getPlayer())).getMeta().getLocationValues().verifiedLocation = e.getRespawnLocation().clone();
        if (EventManager.debug) {
            e.getPlayer().sendMessage(ChatColor.GREEN + "[TP] " + e.getRespawnLocation().toString() + " " + LocationHelper.getDistanceSafe(e.getPlayer().getLocation(), e.getRespawnLocation()));
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final PlayerTeleportEvent e) {
        this.executor.removeAllTasks(IIUA.getUUIDFrom(e.getPlayer()));
        e.getPlayer().setFallDistance(0.0f);
        this.plugin.catchCheckable(IIUA.getUUIDFrom(e.getPlayer())).getMeta().getSyncedValues().hasAnOpenInventory = false;
        this.plugin.catchCheckable(IIUA.getUUIDFrom(e.getPlayer())).getMeta().getLocationValues().verifiedLocation = e.getTo().clone();
        this.plugin.catchCheckable(IIUA.getUUIDFrom(e.getPlayer())).getMeta().getTimedValues().lastTeleportNano = System.nanoTime();
        if (!LocationHelper.isInLoadedChunk(e.getTo().getWorld(), e.getTo().getBlockX(), e.getTo().getBlockZ())) {
            if (!Bukkit.isPrimaryThread() && e.getCause().equals((Object)PlayerTeleportEvent.TeleportCause.PLUGIN)) {
                IntaveExceptionHandler.printAndSaveToFile(new IntaveException("Async chunk load prompt"), "[HIGHLY FATAL] A plugin of yours tried to teleport a player to a specific location asynchronously", "Since the chunk of that location was still not loaded by the server, Intave had to do it", "Unfortunately, chunks can not be loaded asynchronously, with is the reason why intave has blocked this teleportevent", "Use a timedtask to perform a teleportation synchronously");
                return;
            }
            if (Bukkit.isPrimaryThread()) {
                e.getTo().getChunk().load();
            }
        }
        if (!e.getFrom().getWorld().getUID().equals(e.getTo().getWorld().getUID())) {
            this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getSyncedValues().isUsingItem = false;
            this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getSyncedValues().syncedMovements = 0L;
            this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getLocationValues().nofallVerifyLocation = e.getTo().clone();
            return;
        }
        if (EventManager.debug) {
            e.getPlayer().sendMessage("[TP] " + e.getTo().toString() + " " + LocationHelper.getDistanceSafe(e.getFrom(), e.getTo()));
        }
        final boolean bigDist = e.getFrom().distance(e.getTo()) > 5.0;
        if (bigDist && this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getSyncedValues().isUsingItem) {
            this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getSyncedValues().isUsingItem = false;
        }
    }
    
    @EventHandler
    public void on(final PlayerQuitEvent e) {
        this.executor.removeAllTasks(e.getPlayer().getUniqueId());
    }
    
    @EventHandler
    public void on(final PlayerItemHeldEvent e) {
        if (this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getSyncedValues().isUsingItem) {
            this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getSyncedValues().isUsingItem = false;
        }
    }
    
    @EventHandler
    public void on(final InventoryOpenEvent e) {
        if (this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getSyncedValues().isUsingItem) {
            this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getSyncedValues().isUsingItem = false;
        }
    }
    
    @EventHandler
    public void on(final InventoryCloseEvent e) {
        if (this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getSyncedValues().isUsingItem) {
            this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getSyncedValues().isUsingItem = false;
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final PlayerInteractAtEntityEvent e) {
        if (this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getSyncedValues().isUsingItem) {
            this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getSyncedValues().isUsingItem = false;
        }
    }
    
    @EventHandler
    public void on(final PlayerItemConsumeEvent e) {
        if (this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getSyncedValues().isUsingItem) {
            this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getSyncedValues().isUsingItem = false;
        }
    }
    
    @EventHandler
    public void on(final ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter() instanceof Player && this.plugin.catchCheckable(((Player)e.getEntity().getShooter()).getUniqueId()).getMeta().getSyncedValues().isUsingItem) {
            this.plugin.catchCheckable(((Player)e.getEntity().getShooter()).getUniqueId()).getMeta().getSyncedValues().isUsingItem = false;
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on2(final PlayerMoveEvent e) {
        if (!e.isCancelled()) {
            return;
        }
        e.setFrom(e.getTo());
        e.setCancelled(false);
        e.getPlayer().teleport(e.getFrom());
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void on(final PlayerMoveEvent e) {
        final Player p = e.getPlayer();
        final Checkable checkable = this.plugin.catchCheckable(IIUA.getUUIDFrom(p));
        if (IIUA.getCurrentTimeMillis() - this.plugin.getStartupTime() > 1000L) {
            if (Objects.isNull(this.moveCheck)) {
                this.moveCheck = (MoveCheck)this.plugin.getCheckManager().getCheck("Move");
            }
            if (!this.moveCheck.checkEvent(e)) {
                PersistentDebugTelemetry.teleport((Entity)p, e.getFrom(), "move-setback");
                return;
            }
        }
        if (e.isCancelled()) {
            checkable.removePacketsFromBalanceCalc(2);
            return;
        }
        try {
            if (EventManager.handleMoveEvents) {
                final boolean claimingOnGround = p.isOnGround();
                final boolean asyncRequest = !EventManager.handleMoveEventsSync;
                final boolean asyncReady = IIUA.getCurrentTimeMillis() - checkable.getMeta().getSyncedValues().syncedMovements > 20L;
                if (!LocationHelper.isInLoadedChunk(p.getWorld(), e.getTo().getBlockX(), e.getTo().getBlockZ())) {
                    if (!e.getTo().getChunk().isLoaded()) {
                        e.getTo().getChunk().load();
                    }
                    return;
                }
                if ((asyncRequest && asyncReady) || this.getExecutor().hasTasksLeft(IIUA.getUUIDFrom(p))) {
                    final Checkable.CheckableMeta.SystemValues systemValues = checkable.getMeta().getSystemValues();
                    ++systemValues.queredMoves;
                    final Checkable checkable2;
                    final Checkable.CheckableMeta.SystemValues systemValues2;
                    long start;
                    final boolean claimingOnGround2;
                    long duration;
                    final Player player;
                    this.getExecutor().execute(() -> {
                        checkable2.getMeta().getSystemValues();
                        ++systemValues2.executedMoves;
                        try {
                            start = System.nanoTime();
                            this.runMoveEvent(e, claimingOnGround2, true);
                            duration = System.nanoTime() - start;
                            PersistentDebugTelemetry.movementDuration(player, duration / 1000000.0);
                        }
                        catch (IllegalStateException ex2) {}
                        return;
                    }, IIUA.getUUIDFrom(p));
                }
                else {
                    this.runMoveEvent(e, claimingOnGround, false);
                    final Checkable.CheckableMeta.SyncedValues syncedValues = checkable.getMeta().getSyncedValues();
                    ++syncedValues.syncedMovements;
                }
            }
        }
        catch (Exception ex) {
            if (ex instanceof IntaveInternalException || ex instanceof IntaveException || ex instanceof NullPointerException) {
                ex.printStackTrace();
                return;
            }
            if (ex instanceof ConcurrentModificationException) {
                if (!e.isAsynchronous()) {
                    p.kickPlayer("de.jpx3.intave.api.external.linked.exceptions.IntaveInternalException: java.util.ConcurrentModificationException: " + ex.getMessage());
                }
                IntaveExceptionHandler.printAndSaveToFile("[FATAL] Following error could have crashed intaves movement controller. Not expecting internal collapse", ex);
                return;
            }
            if (ex instanceof IllegalStateException) {
                return;
            }
            final List<String> header = new ArrayList<String>();
            header.add("[FATAL] Following error could have crashed intaves movement controller");
            IntaveExceptionHandler.printAndSaveToFile(header, ex);
        }
        checkable.getMeta().getLocationValues().lastLocation = e.getFrom().clone();
        checkable.getMeta().getLocationValues().penaltyLocationServerTickSync = checkable.getMeta().getLocationValues().lastLocationServerTickSync;
        checkable.getMeta().getLocationValues().lastLocationServerTickSync = e.getFrom();
    }
    
    private void loadNearbyBlocks(final Location location) {
        for (final BlockFace blockFace : BlockFace.values()) {
            for (final BlockFace blockFace2 : BlockFace.values()) {
                location.getBlock().getRelative(blockFace).getRelative(blockFace2).getType();
            }
        }
    }
    
    private void runMoveEvent(final PlayerMoveEvent e, final boolean claimingOnGround, final boolean async) {
        final Player p = e.getPlayer();
        if (!e.getFrom().getWorld().equals(e.getTo().getWorld())) {
            return;
        }
        final Checkable checkable = this.plugin.catchCheckable(IIUA.getUUIDFrom(p));
        checkable.getMeta().getSyncedValues().hasActiveMove = true;
        checkable.getMeta().getSyncedValues().claimingInActiveTaskToBeOnGround = claimingOnGround;
        e.getTo();
        e.getFrom();
        CheckableMoveEvent newEvent;
        if (checkable.checkableMoveEvent == null) {
            newEvent = new CheckableMoveEvent(e);
            checkable.checkableMoveEvent = newEvent;
        }
        else {
            newEvent = checkable.checkableMoveEvent;
            newEvent.renewEvent(e);
        }
        if (EventManager.debug) {
            p.sendMessage(String.valueOf(newEvent.getYDirectedMotion()));
        }
        this.plugin.getCheckableManager().preRefreshMovementData(newEvent);
        this.plugin.getCheckManager().hook(newEvent);
        checkable.getMeta().getSyncedValues().lastYmovement = newEvent.getYDirectedMotion();
        checkable.getMeta().getSyncedValues().lastXZMovement = newEvent.getXZDirectedMotion();
        checkable.getMeta().getSyncedValues().lastXMovement = newEvent.getXDirectedMotion();
        checkable.getMeta().getSyncedValues().lastZMovement = newEvent.getZDirectedMotion();
        checkable.getMeta().getSyncedValues().lastSquaredMovement = newEvent.getMotionSqared();
        checkable.getMeta().getSyncedValues().lastVector.setX(newEvent.getXDirectedMotion()).setY(newEvent.getYDirectedMotion()).setZ(newEvent.getZDirectedMotion());
        checkable.getMeta().getSyncedValues().lastYawDiff = YawUtil.yawDiff(e.getFrom().getYaw(), e.getTo().getYaw());
        checkable.getMeta().getTimedValues().lastMoveEvent = IIUA.getCurrentTimeMillis();
        if (newEvent.isMoving()) {
            checkable.getMeta().getTimedValues().lastTimeChangedPosition = IIUA.getCurrentTimeMillis();
        }
        if (newEvent.isRotating()) {
            checkable.getMeta().getTimedValues().lastTimeChangedRotation = IIUA.getCurrentTimeMillis();
        }
        if (checkable.getMeta().getSyncedValues().ticksSinceLastMovementCorrection < 100L) {
            final Checkable.CheckableMeta.SyncedValues syncedValues = checkable.getMeta().getSyncedValues();
            ++syncedValues.ticksSinceLastMovementCorrection;
        }
        if (checkable.getMeta().getSyncedValues().ticksVelocityMidAir < 40L && checkable.getMeta().getSyncedValues().ticksVelocityMidAir >= 0L) {
            final Checkable.CheckableMeta.SyncedValues syncedValues2 = checkable.getMeta().getSyncedValues();
            ++syncedValues2.ticksVelocityMidAir;
        }
        if (newEvent.timeOfQuere < checkable.getMeta().getTimedValues().lastTeleportNano) {
            return;
        }
        if (newEvent.isCancelled()) {
            checkable.getMeta().getSyncedValues().ticksSinceLastMovementCorrection = 0L;
            if (newEvent.getMoveFlagData().isBukkitCancelled()) {
                if (this.plugin.getConfig().getBoolean(this.plugin.getThresholdsManager().getConfigPathFromName("Flight") + ".teleport_falldistance_renew")) {
                    this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).fallDistanceRenew(newEvent.getYDirectedMotion());
                }
                checkable.removePacketsFromBalanceCalc(2);
                PersistentDebugTelemetry.teleport((Entity)p, e.getFrom(), "event-bktref");
                this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().hasActiveMove = false;
                return;
            }
            final boolean onlyDown = this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getLocationValues().lastSafeLocationBeforeRE.getY() > p.getLocation().getY() || checkable.getMeta().getSyncedValues().ticksInAir > 3L;
            if (newEvent.getMoveFlagData().isPullSilent()) {
                final Location c = this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getVerifiedLocation().clone();
                final double muliplyer = 1.0;
                double pullMotionCalculated = newEvent.getMoveFlagData().shouldOverrideFMB() ? (-(MathHelper.amount(newEvent.getMoveFlagData().getPullYMotion()) * 1.0)) : (MathHelper.amount(newEvent.getMoveFlagData().getPullYMotion()) * 1.0);
                c.setY(c.getY() - pullMotionCalculated);
                if ((!BlockData.isInVoidHitbox(c.getBlock(), c) || c.getBlock().isLiquid() || BlockData.isClimbable(c.getBlock())) && !LocationHelper.isInsideUnpassable(c.getWorld(), (Entity)p, c)) {
                    if (newEvent.getMoveFlagData().getPullYMotion() > 0.0 && !newEvent.getMoveFlagData().shouldOverrideFMB()) {
                        PersistentDebugTelemetry.teleport((Entity)p, e.getFrom(), "event-pullmotion-flowdrop");
                        this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().lastYmovement = 0.0;
                        this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().hasActiveMove = false;
                        return;
                    }
                    PersistentDebugTelemetry.teleport((Entity)p, c, "event-pullmotion-invokant");
                    if (pullMotionCalculated > 0.0) {
                        checkable.setSilentVelocity(new Vector(0.0, (-pullMotionCalculated - 0.08) * 0.98, 0.0));
                    }
                    checkable.fallDistanceRenew(Math.abs(e.getFrom().getY() - c.getY()));
                    if (-pullMotionCalculated > 0.0) {
                        pullMotionCalculated = -0.0784;
                    }
                    checkable.getMeta().getSyncedValues().lastYmovement = -pullMotionCalculated;
                    this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().hasActiveMove = false;
                    return;
                }
                else {
                    c.setY(c.getY() + pullMotionCalculated - 0.0789);
                    if ((!BlockData.doesAffectMovement(c.getBlock()) || c.getBlock().isLiquid() || BlockData.isClimbable(c.getBlock())) && !LocationHelper.isInsideUnpassable(c.getWorld(), (Entity)p, c)) {
                        if (this.plugin.getConfig().getBoolean(this.plugin.getThresholdsManager().getConfigPathFromName("Flight") + ".teleport_falldistance_renew")) {
                            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).fallDistanceRenew(-MathHelper.amount(newEvent.getMoveFlagData().getPullYMotion() * 1.0));
                        }
                        PersistentDebugTelemetry.teleport((Entity)p, c, "event-pullmotion-multpldaxis");
                        if (pullMotionCalculated >= 0.0) {
                            checkable.setSilentVelocity(new Vector(0.0, -pullMotionCalculated, 0.0));
                        }
                        this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().lastYmovement = -pullMotionCalculated;
                        this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().hasActiveMove = false;
                        return;
                    }
                    if (BlockData.isPassable(c.getBlock())) {
                        this.plugin.getRetributionManager().teleportPlayerDirG(p);
                    }
                    else {
                        PersistentDebugTelemetry.teleport((Entity)p, e.getFrom(), "event-pullmotion-dispocrct");
                    }
                }
            }
            else {
                PersistentDebugTelemetry.teleport((Entity)p, e.getFrom(), "event-bufferkill");
                this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).removePacketsFromBalanceCalc(1);
                this.plugin.getRetributionManager().handleLocationCorrection(p, onlyDown);
            }
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().lastYmovement = 0.0;
        }
        else {
            checkable.getMeta().getSyncedValues().lastFallDistance = p.getFallDistance();
            checkable.getMeta().getSyncedValues().verifMotionVec3D.setX(newEvent.getXDirectedMotion()).setY(newEvent.getYDirectedMotion()).setZ(newEvent.getZDirectedMotion());
            checkable.getMeta().getLocationValues().penaltyLocation = checkable.getMeta().getLocationValues().verifiedLocation.clone();
            checkable.getMeta().getLocationValues().verifiedLocation = newEvent.getTo().clone();
            if (EventManager.debug) {
                p.sendMessage(ChatColor.YELLOW + newEvent.getTo().toString());
            }
        }
        this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().hasActiveMove = false;
    }
    
    @EventHandler
    public void on(final PlayerPickupItemEvent e) {
        if (this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getLocationValues().penaltyLocation.toVector().distance(e.getItem().getLocation().toVector()) > 3.0 && IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(IIUA.getUUIDFrom(e.getPlayer())).getMeta().getTimedValues().lastMoveEvent < 400L) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void on(final PlayerToggleFlightEvent e) {
        this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getTimedValues().lastTimeToggledFlight = IIUA.getCurrentTimeMillis();
    }
    
    @EventHandler
    public void on(final EntityChangeBlockEvent e) {
        final boolean blockLanding = e.getBlock().getType().equals((Object)Material.AIR);
        if (!blockLanding) {
            this.plugin.getServer().getOnlinePlayers().stream().filter(o -> LocationHelper.getDistanceSafe(e.getBlock().getLocation(), o.getLocation()) < 3.0).filter(o -> this.plugin.isLinkedToIntave(o.getUniqueId())).forEach(o -> this.plugin.catchCheckable(o.getUniqueId()).getMeta().getTimedValues().lastNearbyBlockPhysicsEvent = IIUA.getCurrentTimeMillis());
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final EntityDamageByEntityEvent e) {
        final Entity damager = e.getDamager();
        final Entity damaged = e.getEntity();
        if (damager instanceof Player && damaged instanceof LivingEntity) {
            if (damager.getLocation().distance(damaged.getLocation()) > 6.0) {
                PersistentDebugTelemetry.hitCancel((Player)damager, (Cancellable)e, "buffr-contr-reach");
                return;
            }
            final double lavr_reach = LocationHelper.getDistanceSafe(this.plugin.catchCheckable(IIUA.getUUIDFrom((Player)damager)).getVerifiedLocation(), damaged.getLocation());
            if (lavr_reach > 6.0 && IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(IIUA.getUUIDFrom((Player)damager)).getMeta().getTimedValues().lastMoveEvent < 4000L) {
                PersistentDebugTelemetry.hitCancel((Player)damager, (Cancellable)e, "buffr-contr-laver~reach " + lavr_reach);
                return;
            }
            if (!this.plugin.isLinkedToIntave(IIUA.getUUIDFrom((Player)damager))) {
                PersistentDebugTelemetry.hitCancel((Player)damager, (Cancellable)e, "buffr-nolink");
                return;
            }
            if (IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(IIUA.getUUIDFrom((Player)damager)).getMeta().getTimedValues().lastTimeHitCancelRequest < 4000L) {
                PersistentDebugTelemetry.hitCancel((Player)damager, (Cancellable)e, "buffr-hcreq");
                return;
            }
            CheckableDamageEntityEvent damageEntityEvent;
            if (this.plugin.catchCheckable(damager.getUniqueId()).checkableDamageEntityEvent == null) {
                damageEntityEvent = new CheckableDamageEntityEvent((Player)damager, damaged);
                this.plugin.catchCheckable(damager.getUniqueId()).checkableDamageEntityEvent = damageEntityEvent;
            }
            else {
                damageEntityEvent = this.plugin.catchCheckable(damager.getUniqueId()).checkableDamageEntityEvent;
                damageEntityEvent.renew((Player)damager, damaged);
            }
            this.plugin.getCheckManager().hook(damageEntityEvent);
            damageEntityEvent.getCheckable().getMeta().getSyncedValues().lastEntityAttacked = damaged.getUniqueId();
            damageEntityEvent.getCheckable().getMeta().getTimedValues().lastHitOtherTimestamp = IIUA.getCurrentTimeMillis();
            if (damaged instanceof Player) {
                damageEntityEvent.getCheckable().getMeta().getSyncedValues().lastHittedPlayer = damaged.getUniqueId();
                damageEntityEvent.getCheckable().getMeta().getTimedValues().lastHitOtherPlayerTimestamp = IIUA.getCurrentTimeMillis();
            }
            if (damageEntityEvent.isCancelled()) {
                e.setCancelled(true);
                this.plugin.catchCheckable(e.getDamager().getUniqueId()).getMeta().getTimedValues().lastTimeHitCanceled = IIUA.getCurrentTimeMillis();
            }
        }
    }
    
    public final OrderingExecutor getExecutor() {
        return this.executor;
    }
    
    public final void shutdownAsyncTasks() {
        this.executor.removeAllTasks();
    }
    
    static {
        EventManager.handleMoveEvents = true;
        EventManager.handleMoveEventsSync = false;
        EventManager.debug = false;
    }
}
