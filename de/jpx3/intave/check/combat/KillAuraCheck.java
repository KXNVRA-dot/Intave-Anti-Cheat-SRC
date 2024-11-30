// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.combat;

import java.lang.reflect.Field;
import org.bukkit.util.Vector;
import java.util.Arrays;
import de.jpx3.intave.util.objectable.BlockCache;
import de.jpx3.intave.api.external.linked.exceptions.IntaveInternalException;
import java.util.List;
import java.lang.reflect.InvocationTargetException;
import de.jpx3.intave.util.IntaveExceptionHandler;
import java.util.ArrayList;
import de.jpx3.intave.api.internal.reflections.Reflections;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import java.util.UUID;
import java.util.Iterator;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import de.jpx3.intave.util.calc.YawUtil;
import de.jpx3.intave.util.calc.MathHelper;
import de.jpx3.intave.util.event.CheckableMoveEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import com.comphenix.protocol.events.PacketListener;
import de.jpx3.intave.util.objectable.Checkable;
import org.bukkit.entity.Player;
import de.jpx3.intave.util.calc.LocationHelper;
import java.util.Objects;
import org.bukkit.Bukkit;
import com.comphenix.protocol.wrappers.EnumWrappers;
import de.jpx3.intave.antipiracy.IIUA;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import de.jpx3.intave.api.internal.ProtocolLibAdapter;
import org.bukkit.plugin.Plugin;
import de.jpx3.intave.util.enums.CheatCategory;
import com.comphenix.protocol.utility.MinecraftVersion;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.event.Listener;
import de.jpx3.intave.util.objectable.IntaveCheck;

public final class KillAuraCheck extends IntaveCheck implements Listener
{
    private final IntavePlugin plugin;
    private final MinecraftVersion minecraftVersionOnePointTwelve;
    private boolean deadMode;
    private static final int OVERHEAP_VL = 5;
    private static final int FLAG_VL = 8;
    private static final int MAXIMUM_VL = 14;
    
    public KillAuraCheck(final IntavePlugin plugin) {
        super("KillAura", CheatCategory.COMBAT);
        this.minecraftVersionOnePointTwelve = new MinecraftVersion("1.12");
        this.deadMode = false;
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this.plugin);
        if (!this.isActivated()) {
            return;
        }
        final IntavePlugin ref = plugin;
        ProtocolLibAdapter.getProtocolManager().addPacketListener((PacketListener)new PacketAdapter(plugin, ListenerPriority.LOWEST, new PacketType[] { PacketType.Play.Client.USE_ENTITY }) {
            public final void onPacketReceiving(final PacketEvent event) {
                if (!KillAuraCheck.this.isActivated()) {
                    return;
                }
                final Player player = event.getPlayer();
                final Checkable checkable = ref.catchCheckable(IIUA.getUUIDFrom(player));
                final int attackedEntityId = event.getPacket().getIntegers().getValues().get(0);
                final EnumWrappers.EntityUseAction attackType = event.getPacket().getEntityUseActions().getValues().get(0);
                if (checkable.getMeta().getVioValues().cryptaBuffer) {
                    checkable.getMeta().getVioValues().cryptaBuffer = false;
                    if (IIUA.getCurrentTimeMillis() - checkable.getMeta().getVioValues().lastCryptaBuffer < 1000L && !ProtocolLibAdapter.getProtocolManager().getMinecraftVersion().isAtLeast(KillAuraCheck.this.minecraftVersionOnePointTwelve) && IIUA.getCurrentTimeMillis() - checkable.getMeta().getVioValues().lastKillAuraFlag > 150L) {
                        if (ref.getRetributionManager().markPlayer(player, 4, "KillAura", CheatCategory.COMBAT, "attacked entitiy killaura-like (crypta engine)")) {
                            checkable.getMeta().getTimedValues().lastTimeHitCancelRequest = IIUA.getCurrentTimeMillis();
                            event.setCancelled(true);
                        }
                        checkable.getMeta().getVioValues().lastKillAuraFlag = IIUA.getCurrentTimeMillis();
                    }
                }
                if (!KillAuraCheck.this.useRayTrixEngine()) {
                    return;
                }
                final Player target = Bukkit.getPlayer(checkable.getMeta().getSyncedValues().lastHittedPlayer);
                if (Objects.isNull(target) || !target.isOnline()) {
                    checkable.getMeta().getSystemValues().botBuffer.setVisible(false, checkable.getVerifiedLocation(), "Target null or offline");
                    return;
                }
                if (checkable.getMeta().getSystemValues().botBuffer.isBot(attackedEntityId)) {
                    if (checkable.getMeta().getVioValues().killauraVl <= 5) {
                        event.getPacket().getIntegers().write(0, (Object)target.getEntityId());
                    }
                    checkable.getMeta().getVioValues().killauraVl = 0;
                    checkable.getMeta().getTimedValues().lastKillAuraEIDOverride = IIUA.getCurrentTimeMillis();
                    KillAuraCheck.this.updatePosition(target, target.getLocation());
                }
                else if (target.getEntityId() == attackedEntityId && LocationHelper.getDistanceSafe(player.getLocation(), target.getLocation()) > 1.2 && KillAuraCheck.this.isTracked(player.getUniqueId(), target.getUniqueId()) && checkable.getMeta().getSystemValues().botBuffer.areVisible()) {
                    final long sync = IIUA.getCurrentTimeMillis() - checkable.getMeta().getSystemValues().botBuffer.lastTeleport;
                    if (checkable.getMeta().getVioValues().killauraVl > 5 && ref.getThresholdsManager().shouldFlag("killaura", 8)) {
                        event.setCancelled(true);
                    }
                    if (sync < 100L) {
                        if (checkable.getMeta().getVioValues().killauraVl < 14 && IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastKillAuraEIDOverride > 2000L) {
                            final Checkable.CheckableMeta.ViolationValues vioValues = checkable.getMeta().getVioValues();
                            ++vioValues.killauraVl;
                        }
                        if (checkable.getMeta().getVioValues().killauraVl >= 8) {
                            if (IIUA.getCurrentTimeMillis() - checkable.getMeta().getVioValues().lastKillAuraFlag > 500L) {
                                if (ref.getRetributionManager().markPlayer(player, 4, "KillAura", CheatCategory.COMBAT, "attacked entitiy killaura-like (raytrx engine)")) {
                                    checkable.getMeta().getTimedValues().lastTimeHitCancelRequest = IIUA.getCurrentTimeMillis();
                                    event.setCancelled(true);
                                }
                                checkable.getMeta().getVioValues().lastKillAuraFlag = IIUA.getCurrentTimeMillis();
                            }
                            else if (ref.getThresholdsManager().shouldFlag("killaura", ref.getViolationManager().getViolationLevel(player, "killaura"))) {
                                event.setCancelled(true);
                            }
                        }
                    }
                    else if (checkable.getMeta().getVioValues().killauraVl > 0) {
                        final Checkable.CheckableMeta.ViolationValues vioValues2 = checkable.getMeta().getVioValues();
                        --vioValues2.killauraVl;
                    }
                }
            }
        });
        plugin.getServer().getOnlinePlayers().forEach(o -> this.getBotBufferOf(o.getUniqueId()).init(o, o.getLocation()));
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public final void on(final PlayerJoinEvent e) {
        if (this.isActivated()) {
            final Player p = e.getPlayer();
            this.getBotBufferOf(p.getUniqueId()).init(p, p.getLocation());
        }
    }
    
    @Override
    public final void onCheckableMove(final CheckableMoveEvent e) {
        if (!this.isActivated()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        this.updatePosition(p, e.getFrom());
        this.analyseMovement(e);
    }
    
    public final void analyseMovement(final CheckableMoveEvent e) {
        double disX = e.getXDirectedMotion();
        double disZ = e.getZDirectedMotion();
        final double calcMotionX = disX - e.getCheckable().getMeta().getSyncedValues().lastXMovementCypta;
        final double calcMotionZ = disZ - e.getCheckable().getMeta().getSyncedValues().lastZMovementCypta;
        final double diffY = MathHelper.diff(Math.abs(e.getFrom().getY()), Math.abs(e.getTo().getY()));
        final double motionXZ = e.getXZDirectedMotion();
        final float yawMotionAngle = (float)(Math.atan2(calcMotionZ, calcMotionX) * 180.0 / 3.141592653589793 - 90.0) - e.getTo().getYaw();
        final double angleTo180 = Math.abs(YawUtil.angleTo180Deg(yawMotionAngle)) % 45.0;
        final double xzMultiplier = (IIUA.isPretendingToBeOnGround(e.getBukkitPlayer()) ? 0.60000005239967 : 1.0) * 0.9100000262260437;
        disX *= xzMultiplier;
        disZ *= xzMultiplier;
        if (!e.getBukkitPlayer().getAllowFlight()) {
            if (MathHelper.isBetween(angleTo180, 2.5, 42.5)) {
                final long lastTimeOnIce = System.currentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnIce;
                final long lastTimeDamageTaken = System.currentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeVelocityAdded;
                final long lastTimeCollidedVertically = System.currentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeCollidedWithTopBlock;
                final long lastTimeOnSlime = System.currentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnASlime;
                final long lastTimeOnLadder = System.currentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeClimbedALadder;
                final long lastWaterCollision = System.currentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeInWater;
                if (Math.abs(disX) > 0.005 && Math.abs(disZ) > 0.005 && motionXZ > 0.14 && e.getCheckable().getMeta().getSyncedValues().lastXZMovement > 0.14) {
                    if (IIUA.isPretendingToBeOnGround(e.getBukkitPlayer())) {
                        if (diffY == 0.0 && MathHelper.diff(motionXZ, e.getCheckable().getMeta().getSyncedValues().lastXZMovement * 0.91) < 0.045 && motionXZ > 0.065 && lastTimeOnSlime > 200L && lastTimeOnLadder > 200L && lastTimeOnIce > 200L && lastTimeDamageTaken > 50L) {
                            if (e.getCheckable().getMeta().getVioValues().cryptaGroundVl < 24) {
                                final Checkable.CheckableMeta.ViolationValues vioValues = e.getCheckable().getMeta().getVioValues();
                                vioValues.cryptaGroundVl += 3;
                            }
                            if (e.getCheckable().getMeta().getVioValues().cryptaGroundVl > 20) {
                                e.getCheckable().getMeta().getVioValues().cryptaBuffer = true;
                                e.getCheckable().getMeta().getVioValues().lastCryptaBuffer = IIUA.getCurrentTimeMillis();
                            }
                        }
                    }
                    else if (MathHelper.diff(motionXZ, e.getCheckable().getMeta().getSyncedValues().lastXZMovement * 0.91) > 1.0E-5 && lastTimeOnSlime > 200L && lastTimeOnLadder > 200L && lastTimeOnIce > 200L && lastWaterCollision > 200L && lastTimeCollidedVertically > 200L && lastTimeDamageTaken > 600L) {
                        if (e.getCheckable().getMeta().getVioValues().cryptaAirVl < 24) {
                            final Checkable.CheckableMeta.ViolationValues vioValues2 = e.getCheckable().getMeta().getVioValues();
                            vioValues2.cryptaAirVl += 3;
                        }
                        if (e.getCheckable().getMeta().getVioValues().cryptaAirVl > 20) {
                            e.getCheckable().getMeta().getVioValues().cryptaBuffer = true;
                            e.getCheckable().getMeta().getVioValues().lastCryptaBuffer = IIUA.getCurrentTimeMillis();
                        }
                    }
                }
            }
            else {
                if (e.getCheckable().getMeta().getVioValues().cryptaAirVl > 0) {
                    final Checkable.CheckableMeta.ViolationValues vioValues3 = e.getCheckable().getMeta().getVioValues();
                    --vioValues3.cryptaAirVl;
                }
                if (e.getCheckable().getMeta().getVioValues().cryptaGroundVl > 0) {
                    final Checkable.CheckableMeta.ViolationValues vioValues4 = e.getCheckable().getMeta().getVioValues();
                    --vioValues4.cryptaGroundVl;
                }
            }
        }
        e.getCheckable().getMeta().getSyncedValues().lastXMovementCypta = disX;
        e.getCheckable().getMeta().getSyncedValues().lastZMovementCypta = disZ;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public final void on(final PlayerTeleportEvent e) {
        if (!this.isActivated() || !this.useRayTrixEngine()) {
            return;
        }
        final Player p = e.getPlayer();
        this.updatePosition(p, e.getTo());
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public final void on(final PlayerQuitEvent e) {
        if (this.isActivated() && this.useRayTrixEngine()) {
            final Player p = e.getPlayer();
            p.getWorld().getPlayers().stream().filter(all -> this.isTracked(all.getUniqueId(), p.getUniqueId())).forEachOrdered(all -> {
                this.plugin.catchCheckable(IIUA.getUUIDFrom(all)).getMeta().getSyncedValues().lastHittedPlayer = null;
                this.plugin.catchCheckable(IIUA.getUUIDFrom(all)).getMeta().getSystemValues().botBuffer.setVisible(false, all.getLocation(), "Player Quit Event");
            });
        }
    }
    
    @Override
    public final void onSystemShutdown() {
        this.deadMode = true;
        if (!this.isActivated() || !this.useRayTrixEngine()) {
            return;
        }
        final Checkable obsrvCheck;
        this.plugin.getServer().getOnlinePlayers().forEach(all -> {
            obsrvCheck = this.getCheckable(all.getUniqueId());
            obsrvCheck.getMeta().getSyncedValues().lastHittedPlayer = null;
            obsrvCheck.getMeta().getTimedValues().lastHitOtherTimestamp = 0L;
            this.isTracked(all.getUniqueId(), all.getUniqueId());
            obsrvCheck.getMeta().getSystemValues().botBuffer.setVisible(false, all.getLocation(), "Initial shutdown");
        });
    }
    
    private void updatePosition(final Player target, final Location location) {
        if (!this.useRayTrixEngine()) {
            return;
        }
        if (this.getCheckable(target.getUniqueId()).getMeta().getVioValues().killauraVl > 5) {}
        for (final Player all : target.getWorld().getPlayers()) {
            if (this.isTracked(all.getUniqueId(), target.getUniqueId())) {
                this.getCheckable(all.getUniqueId()).getMeta().getSystemValues().botBuffer.teleportAllTo((this.getCheckable(all.getUniqueId()).getMeta().getVioValues().killauraVl > 5) ? all.getLocation().clone().add(0.0, 0.8, 0.0) : location);
            }
        }
    }
    
    private boolean isTracked(final UUID observer, final UUID probTracked) {
        final Checkable obsrvCheck = this.getCheckable(observer);
        if (this.deadMode || !this.useRayTrixEngine() || obsrvCheck.getMeta().getSyncedValues().lastHittedPlayer == null) {
            obsrvCheck.getMeta().getSystemValues().botBuffer.setVisible(false, obsrvCheck.getVerifiedLocation(), "Deadmode or last hitted player is null");
            return false;
        }
        if (!obsrvCheck.getMeta().getSyncedValues().lastHittedPlayer.equals(probTracked)) {
            return false;
        }
        if (IIUA.getCurrentTimeMillis() - obsrvCheck.getMeta().getTimedValues().lastHitOtherTimestamp > 2000L) {
            obsrvCheck.getMeta().getSystemValues().botBuffer.setVisible(false, obsrvCheck.getVerifiedLocation(), "Timed out");
            return false;
        }
        final Player target = Bukkit.getPlayer(obsrvCheck.getMeta().getSyncedValues().lastHittedPlayer);
        if (Objects.isNull(target) || !target.isOnline()) {
            return false;
        }
        if (this.blocksCouldBeShifted(target, target.getLocation())) {
            obsrvCheck.getMeta().getSystemValues().botBuffer.setVisible(false, target.getLocation(), "Shifted");
        }
        else {
            obsrvCheck.getMeta().getSystemValues().botBuffer.setVisible(true, target.getLocation(), "Default start command initialized");
        }
        return true;
    }
    
    private boolean blocksCouldBeShifted(final Player target, final Location location) {
        boolean shift = false;
        if (LocationHelper.collidesAnyXZAxis(target.getWorld(), (Entity)target, target.getLocation()) || LocationHelper.collidesStepableIgnoringTopBlock(target.getWorld(), (Entity)target, location)) {
            shift = true;
        }
        if (IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(IIUA.getUUIDFrom(target)).getMeta().getTimedValues().lastTimeStandingOnASlime < 5000L) {
            shift = true;
        }
        return shift;
    }
    
    private static void sendEntitySpawnPacket(final Player observer, final Object abstrEntity, final Material material, final Location location) {
        try {
            abstrEntity.getClass().getMethod("setLocation", Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE).invoke(abstrEntity, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
            final Object spawnPacket = Reflections.getNmsClass("PacketPlayOutSpawnEntity").getConstructor(Reflections.getNmsClass("Entity"), Integer.TYPE, Integer.TYPE).newInstance(abstrEntity, 70, material.getId());
            Reflections.sendPacket(observer, spawnPacket);
        }
        catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException ex2) {
            final ReflectiveOperationException ex;
            final ReflectiveOperationException e = ex;
            final List<String> header = new ArrayList<String>();
            header.add("[Intave] [BAD EXCEPTION] It looks like intave had problems creating \"entity spawn\" packets.");
            IntaveExceptionHandler.printAndSaveToFile(header, e);
        }
    }
    
    private static void sendEntityMetaData(final Player observer, final Object entity) {
        try {
            final int entityId = (int)entity.getClass().getMethod("getId", (Class<?>[])new Class[0]).invoke(entity, new Object[0]);
            final Object dataWatcher = entity.getClass().getMethod("getDataWatcher", (Class<?>[])new Class[0]).invoke(entity, new Object[0]);
            final Object entityMetaDataPacket = Reflections.getNmsClass("PacketPlayOutEntityMetadata").getConstructor(Integer.TYPE, Reflections.getNmsClass("DataWatcher"), Boolean.TYPE).newInstance(entityId, dataWatcher, true);
            Reflections.sendPacket(observer, entityMetaDataPacket);
        }
        catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ex2) {
            final ReflectiveOperationException ex;
            final ReflectiveOperationException e = ex;
            throw new IntaveInternalException(e);
        }
    }
    
    private static Object createFallingBlock(final Location location) {
        final Object worldServer = getMethodValue(location.getWorld(), "getHandle");
        final double posX = location.getX();
        final double posY = location.getY();
        final double posZ = location.getZ();
        final Object iBlockData = getIBlockDataOf(Material.LAVA);
        try {
            final Object fallingBlock = Reflections.getNmsClass("EntityFallingBlock").getConstructor(Reflections.getNmsClass("World"), Double.TYPE, Double.TYPE, Double.TYPE, Reflections.getNmsClass("IBlockData")).newInstance(worldServer, posX, posY, posZ, iBlockData);
            fallingBlock.getClass().getMethod("setInvisible", Boolean.TYPE).invoke(fallingBlock, true);
            return fallingBlock;
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex2) {
            final ReflectiveOperationException ex;
            final ReflectiveOperationException e = ex;
            throw new IntaveInternalException(e);
        }
    }
    
    private static Object getIBlockDataOf(final Material material) {
        try {
            final Object simpleBlock = BlockCache.getBlockFromId(material.getId());
            return simpleBlock.getClass().getMethod("getBlockData", (Class<?>[])new Class[0]).invoke(simpleBlock, new Object[0]);
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex2) {
            final ReflectiveOperationException ex;
            final ReflectiveOperationException e = ex;
            throw new IntaveInternalException(e);
        }
    }
    
    private static Object createDataWatcherWatchableObjectOf(final int index, final Object value) {
        try {
            final Object classToIdMap = getValueOfStaticClass(Reflections.getNmsClass("DataWatcher"), "classToId");
            final int valueTypeInt = (int)classToIdMap.getClass().getMethod("get", Object.class).invoke(classToIdMap, value.getClass());
            return Reflections.getNmsClass("DataWatcher$WatchableObject").getConstructor(Integer.TYPE, Integer.TYPE, Object.class).newInstance(valueTypeInt, index, value);
        }
        catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex2) {
            final ReflectiveOperationException ex;
            final ReflectiveOperationException e = ex;
            throw new IntaveInternalException(e);
        }
    }
    
    private static void sendTeleportPacket(final Player observer, final Object nmsEntity, final Location loc) {
        try {
            nmsEntity.getClass().getMethod("setLocation", Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE).invoke(nmsEntity, loc.getX(), loc.getY(), loc.getZ(), 0.0f, 0.0f);
            nmsEntity.getClass().getField("onGround").set(nmsEntity, false);
            final Object emptyTeleportPacket = Reflections.getNmsClass("PacketPlayOutEntityTeleport").getConstructor(Reflections.getNmsClass("Entity")).newInstance(nmsEntity);
            Reflections.sendPacket(observer, emptyTeleportPacket);
        }
        catch (IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException ex2) {
            final ReflectiveOperationException ex;
            final ReflectiveOperationException e = ex;
            final List<String> header = new ArrayList<String>();
            header.add("[Intave] [BAD EXCEPTION] It looks like intave had problems creating teleport packets.");
            IntaveExceptionHandler.printAndSaveToFile(header, e);
        }
        catch (Exception e2) {
            e2.printStackTrace();
        }
    }
    
    private static void sendEntityKill(final Player observer, final Object... nmsEntity) {
        final int[] ids = Arrays.stream(nmsEntity).mapToInt(o -> (int)getMethodValue(o, "getId")).toArray();
        try {
            final Object entityDestroyPacket = Reflections.getNmsClass("PacketPlayOutEntityDestroy").newInstance();
            setValue(entityDestroyPacket, "a", ids);
            Reflections.sendPacket(observer, entityDestroyPacket);
        }
        catch (InstantiationException | IllegalAccessException ex2) {
            final ReflectiveOperationException ex;
            final ReflectiveOperationException e = ex;
            final List<String> header = new ArrayList<String>();
            header.add("[Intave] [BAD EXCEPTION] It looks like intave had problems sending entity destroy packets.");
            IntaveExceptionHandler.printAndSaveToFile(header, e);
        }
    }
    
    private static void sendVelocityPacket(final Player observer, final Object nmsEntity, final Vector velocty) {
        try {
            final Object velocityPacket = Reflections.getNmsClass("PacketPlayOutEntityVelocity").getConstructor(Integer.TYPE, Double.TYPE, Double.TYPE, Double.TYPE).newInstance((int)getMethodValue(nmsEntity, "getId"), velocty.getX(), velocty.getY(), velocty.getZ());
            Reflections.sendPacket(observer, velocityPacket);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private StaticBotBuffer getBotBufferOf(final UUID uuid) {
        return this.getCheckable(uuid).getMeta().getSystemValues().botBuffer;
    }
    
    private Checkable getCheckable(final UUID uuid) {
        return this.plugin.catchCheckable(uuid);
    }
    
    private static void setValue(final Object obj, final String name, final Object value) {
        try {
            final Field field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(obj, value);
        }
        catch (Exception ex) {}
    }
    
    private static Object getValueOfStaticClass(final Class clazz, final String fieldname) {
        try {
            final Field field = clazz.getDeclaredField(fieldname);
            field.setAccessible(true);
            return field.get(null);
        }
        catch (Exception e) {
            throw new IntaveInternalException("Intave has problems fetching the static field $" + fieldname + " on " + clazz.getSimpleName(), e);
        }
    }
    
    private static Object getValue(final Object obj, final String name) {
        try {
            final Field field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(obj);
        }
        catch (Exception e) {
            throw new IntaveInternalException("Intave has problems fetching the field $" + name + " on " + obj.getClass().getSimpleName(), e);
        }
    }
    
    private static Object getMethodValue(final Object object, final String method_name) {
        try {
            return object.getClass().getMethod(method_name, (Class<?>[])new Class[0]).invoke(object, new Object[0]);
        }
        catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex2) {
            final ReflectiveOperationException ex;
            final ReflectiveOperationException e = ex;
            throw new IntaveInternalException("Intave has problems fetching the method $" + method_name + " on " + object.getClass().getSimpleName(), e);
        }
    }
    
    public final boolean useRayTrixEngine() {
        return this.plugin.getConfig().getBoolean(this.getConfigPath() + ".use_raytrx_engine");
    }
    
    public static class StaticBotBuffer
    {
        private Player player;
        private Object entity1;
        private Object entity2;
        private int cachedEntityID1;
        private int cachedEntityID2;
        private boolean visible;
        private long lastTeleport;
        
        public StaticBotBuffer() {
            this.visible = false;
            this.lastTeleport = IIUA.getCurrentTimeMillis();
        }
        
        final void init(final Player p, final Location location) {
            this.player = p;
            this.entity1 = createFallingBlock(location);
            this.entity2 = createFallingBlock(location);
            this.cachedEntityID1 = (int)getMethodValue(this.entity1, "getId");
            this.cachedEntityID2 = (int)getMethodValue(this.entity2, "getId");
        }
        
        final synchronized boolean isBot(final int entityId) {
            return entityId == this.cachedEntityID1 || entityId == this.cachedEntityID2;
        }
        
        final synchronized void teleportAllTo(final Location location) {
            sendTeleportPacket(this.player, this.entity1, location);
            sendTeleportPacket(this.player, this.entity2, location.clone().add(0.0, 1.0, 0.0));
            this.lastTeleport = IIUA.getCurrentTimeMillis();
        }
        
        public final synchronized void setVelocityForAll(final double x, final double y, final double z) {
            final Vector vector = new Vector(x, y, z);
            sendVelocityPacket(this.player, this.entity1, vector);
            sendVelocityPacket(this.player, this.entity2, vector);
        }
        
        final synchronized boolean areVisible() {
            return this.visible;
        }
        
        synchronized void setVisible(final boolean visible, final Location location, final String reason) {
            if (this.visible == visible) {
                return;
            }
            if (!(this.visible = visible)) {
                sendEntityKill(this.player, new Object[] { this.entity1, this.entity2 });
            }
            else {
                final Material material = Reflections.useAlternateMethodNames() ? Material.LADDER : Material.LAVA;
                sendEntitySpawnPacket(this.player, this.entity1, material, location);
                sendEntityMetaData(this.player, this.entity1);
                sendEntitySpawnPacket(this.player, this.entity2, material, location);
                sendEntityMetaData(this.player, this.entity2);
            }
        }
    }
}
