// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.world;

import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import de.jpx3.intave.util.event.CheckableMoveEvent;
import java.util.List;
import org.bukkit.event.EventHandler;
import de.jpx3.intave.util.objectable.Checkable;
import de.jpx3.intave.util.calc.BalanceUtils;
import java.util.Set;
import de.jpx3.intave.util.data.BlockData;
import org.bukkit.block.BlockFace;
import de.jpx3.intave.util.calc.MathHelper;
import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;
import com.comphenix.protocol.events.PacketListener;
import org.bukkit.entity.Player;
import de.jpx3.intave.antipiracy.IIUA;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.PacketType;
import de.jpx3.intave.api.internal.ProtocolLibAdapter;
import org.bukkit.plugin.Plugin;
import java.util.concurrent.ConcurrentHashMap;
import de.jpx3.intave.util.enums.CheatCategory;
import java.util.ArrayList;
import org.bukkit.block.Block;
import java.util.UUID;
import java.util.Map;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.event.Listener;
import de.jpx3.intave.util.objectable.IntaveCheck;

public final class MachineBlockCheck extends IntaveCheck implements Listener
{
    private final IntavePlugin plugin;
    private final boolean debug = false;
    private final Map<UUID, Block> lastBlock;
    private final Map<UUID, Block> penaltyBlock;
    private final Map<UUID, Long> lastWentDown;
    private final Map<UUID, Long> lastBlockPlaced;
    private final Map<UUID, Long> lastsneak;
    private final Map<UUID, Long> lastblockplace;
    private final Map<UUID, Long> lastmove;
    private final Map<UUID, Integer> mode;
    private final Map<UUID, Long> lastinteract;
    private final Map<UUID, Long> lastclear;
    private final Map<UUID, Integer> clicks;
    private final Map<UUID, Integer> lvl;
    private final Map<UUID, ArrayList<Block>> lastblocks;
    
    public MachineBlockCheck(final IntavePlugin plugin) {
        super("MachineBlock", CheatCategory.WORLD);
        this.lastBlock = new ConcurrentHashMap<UUID, Block>();
        this.penaltyBlock = new ConcurrentHashMap<UUID, Block>();
        this.lastWentDown = new ConcurrentHashMap<UUID, Long>();
        this.lastBlockPlaced = new ConcurrentHashMap<UUID, Long>();
        this.lastsneak = new ConcurrentHashMap<UUID, Long>();
        this.lastblockplace = new ConcurrentHashMap<UUID, Long>();
        this.lastmove = new ConcurrentHashMap<UUID, Long>();
        this.mode = new ConcurrentHashMap<UUID, Integer>();
        this.lastinteract = new ConcurrentHashMap<UUID, Long>();
        this.lastclear = new ConcurrentHashMap<UUID, Long>();
        this.clicks = new ConcurrentHashMap<UUID, Integer>();
        this.lvl = new ConcurrentHashMap<UUID, Integer>();
        this.lastblocks = new ConcurrentHashMap<UUID, ArrayList<Block>>();
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this.plugin);
        final IntavePlugin plugin2 = plugin;
        ProtocolLibAdapter.getProtocolManager().addPacketListener((PacketListener)new PacketAdapter(plugin, new PacketType[] { PacketType.Play.Client.BLOCK_PLACE }) {
            public void onPacketReceiving(final PacketEvent event) {
                if (!MachineBlockCheck.this.isActivated()) {
                    return;
                }
                final Player p = event.getPlayer();
                if (event.getPacket().getFloat().size() < 3) {
                    plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeValidBlockPlaced = IIUA.getCurrentTimeMillis();
                    return;
                }
                final float f1 = event.getPacket().getFloat().getValues().get(0);
                final float f2 = event.getPacket().getFloat().getValues().get(1);
                final float f3 = event.getPacket().getFloat().getValues().get(2);
                plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeBlockPacketArrived = IIUA.getCurrentTimeMillis();
                if (f1 == 0.0 && f2 == 0.0 && f3 == 0.0) {
                    return;
                }
                plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().lastBlockFaceLook.setX(f1);
                plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().lastBlockFaceLook.setY(f2);
                plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().lastBlockFaceLook.setZ(f3);
                plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeValidBlockPlaced = IIUA.getCurrentTimeMillis();
            }
        });
    }
    
    @EventHandler
    public void on3(final BlockPlaceEvent e) {
        if (!this.isActivated()) {
            return;
        }
        final Player p = e.getPlayer();
        final Checkable checkable = this.plugin.catchCheckable(IIUA.getUUIDFrom(p));
        final long diffToLastValidBlockPacket = IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastTimeValidBlockPlaced;
        final boolean valid = IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastTimeBlockPacketArrived < 2000L;
        final boolean flag = true;
        if (IIUA.getCurrentTimeMillis() - checkable.getMeta().getHeuristicValues().lastMotionBlockFlag < 1000L) {
            if (IIUA.getCurrentTimeMillis() - checkable.getMeta().getVioValues().lastMLAJFlag < 5000L && this.plugin.getRetributionManager().markPlayer(p, 1, "MachineBlock", CheatCategory.WORLD, "tried to place a block suspiciously.")) {
                e.setCancelled(true);
            }
            checkable.getMeta().getVioValues().lastMLAJFlag = IIUA.getCurrentTimeMillis();
            checkable.getMeta().getHeuristicValues().lastMotionBlockFlag = 0L;
            return;
        }
        if (p.getItemInHand().getType().equals((Object)Material.WATER_LILY)) {
            return;
        }
        if (diffToLastValidBlockPacket > 2000L && valid) {
            if (this.plugin.getRetributionManager().markPlayer(p, 1, "MachineBlock", CheatCategory.WORLD, "tried to place a block suspiciously.")) {
                e.setCancelled(true);
            }
            return;
        }
        final double f1 = checkable.getMeta().getSyncedValues().lastBlockFaceLook.getX();
        final double f2 = checkable.getMeta().getSyncedValues().lastBlockFaceLook.getY();
        final double f3 = checkable.getMeta().getSyncedValues().lastBlockFaceLook.getZ();
        int vl = 0;
        final float expectedX = (int)p.getLocation().getX() * 16 / 16.0f - e.getBlockPlaced().getX();
        final float expectedY = (int)p.getLocation().getY() * 16 / 16.0f - e.getBlockPlaced().getY();
        final float expectedZ = (int)p.getLocation().getZ() * 16 / 16.0f - e.getBlockPlaced().getZ();
        if (e.getBlock().getY() < e.getPlayer().getLocation().getY() && !p.getAllowFlight() && e.getBlock().getY() + 1.6 > e.getPlayer().getLocation().getY()) {
            final long diff = IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastTimeBlockPlacedBelow;
            if (diff < 2000L && MathHelper.amount(MathHelper.diff((float)(e.getBlockPlaced().getY() - 2), (float)this.penaltyBlock.getOrDefault(p.getUniqueId(), e.getBlockPlaced()).getY())) < 0.1) {
                final double balance = this.addNumberAndGetBalance(checkable.getMeta().getBalanceValues().blockPlaceUnderDiff, diff, 10);
                if (balance < 380.0 && this.plugin.getRetributionManager().markPlayer(p, 1, "MachineBlock", CheatCategory.WORLD, "tried to place a block suspiciously.")) {
                    e.setCancelled(true);
                }
            }
            checkable.getMeta().getTimedValues().lastTimeBlockPlacedBelow = IIUA.getCurrentTimeMillis();
        }
        if (this.isSuspicious(f1)) {
            ++vl;
        }
        if (this.isSuspicious(f2)) {
            ++vl;
        }
        if (this.isSuspicious(f3)) {
            ++vl;
        }
        if ((f1 < 0.0 || f2 < 0.0 || f3 < 0.0 || f1 > 1.0 || f2 > 1.0 || f3 > 1.0) && valid && flag) {
            if (this.plugin.getRetributionManager().markPlayer(p, 1, "MachineBlock", CheatCategory.WORLD, "tried to place a block suspiciously.")) {
                checkable.getMeta().getVioValues().cancelBlockPlacement = true;
            }
            return;
        }
        if (!e.getBlock().getWorld().equals(e.getPlayer().getWorld())) {
            e.setCancelled(true);
            return;
        }
        final double hsDist = e.getBlock().getRelative(BlockFace.UP).getLocation().distance(e.getPlayer().getLocation());
        if (hsDist < 1.3 && !BlockData.doesAffectMovement(e.getBlock().getRelative(BlockFace.DOWN)) && this.collides(e.getBlock()) < 2) {
            if (this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getLocationValues().lastBlockPlacedUnder != null && p.getWorld().equals(this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getLocationValues().lastBlockPlacedUnder.getWorld())) {
                this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getHeuristicValues().lastBPULocationDist = p.getLocation().distance(this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getLocationValues().lastBlockPlacedUnder);
            }
            this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getLocationValues().lastBlockPlacedUnder = p.getLocation();
            if (IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getTimedValues().lastBlockPlaceUnderBlockRequest < 2000L) {
                e.setCancelled(true);
                return;
            }
            if (IIUA.getCurrentTimeMillis() - checkable.getMeta().getVioValues().lastTimeSuspiciousForSaveWalk <= 200L) {
                final Checkable.CheckableMeta.ViolationValues vioValues = checkable.getMeta().getVioValues();
                ++vioValues.suspiciousSafeWalkBlockPlaces;
                if (checkable.getMeta().getVioValues().suspiciousSafeWalkBlockPlaces > 1) {
                    final double prob = MathHelper.map(checkable.getMeta().getVioValues().suspiciousSafeWalkBlockPlaces, 2.0, 5.0, 90.0, 100.0);
                    if (this.plugin.getRetributionManager().markPlayer(p, 1, "MachineBlock", CheatCategory.WORLD, "might be using safewalk." + "")) {
                        e.setCancelled(true);
                    }
                    return;
                }
            }
            else if (checkable.getMeta().getVioValues().suspiciousSafeWalkBlockPlaces > 0) {
                final Checkable.CheckableMeta.ViolationValues vioValues2 = checkable.getMeta().getVioValues();
                --vioValues2.suspiciousSafeWalkBlockPlaces;
            }
            if (vl > 2 && valid) {
                if (checkable.getMeta().getHeuristicValues().scaffoldwalkVL * 1.1 < 100.0) {
                    final Checkable.CheckableMeta.HeuristicValues heuristicValues = checkable.getMeta().getHeuristicValues();
                    heuristicValues.scaffoldwalkVL *= 1.1;
                }
            }
            else if (valid) {
                if (checkable.getMeta().getHeuristicValues().scaffoldwalkVL * 0.6 > 50.0) {
                    final Checkable.CheckableMeta.HeuristicValues heuristicValues2 = checkable.getMeta().getHeuristicValues();
                    heuristicValues2.scaffoldwalkVL *= 0.6;
                }
                else {
                    checkable.getMeta().getHeuristicValues().scaffoldwalkVL = 50.0;
                }
            }
            if (checkable.getMeta().getHeuristicValues().scaffoldwalkVL > 80.0 && flag) {
                if (this.plugin.getRetributionManager().markPlayer(p, 1, "MachineBlock", CheatCategory.WORLD, "tried to place a block suspiciously. " + "")) {
                    e.setCancelled(true);
                }
                return;
            }
            final double f4 = p.getTargetBlock((Set)null, 3).getLocation().distance(e.getBlockPlaced().getLocation());
            if (f4 > 0.0 && f4 < 1.0 && hsDist < 1.0) {
                if (IIUA.getCurrentTimeMillis() - checkable.getMeta().getVioValues().lastTimeSuspiciousForScaffoldWalk < 2000L && flag) {
                    if (this.plugin.getRetributionManager().markPlayer(p, 1, "MachineBlock", CheatCategory.WORLD, "tried to place a block suspiciously." + "")) {
                        e.setCancelled(true);
                        checkable.getMeta().getVioValues().cancelBlockPlacement = true;
                    }
                    return;
                }
                checkable.getMeta().getVioValues().lastTimeSuspiciousForScaffoldWalk = IIUA.getCurrentTimeMillis();
            }
            if (!e.isCancelled()) {
                final int size = 8;
                if (checkable.getMeta().getBalanceValues().blockPlaceInCornerDiff.size() >= size) {
                    checkable.getMeta().getBalanceValues().blockPlaceInCornerDiff.remove(0);
                }
                if (e.getBlockAgainst().getLocation().getY() == e.getBlockPlaced().getLocation().getY()) {
                    checkable.getMeta().getBalanceValues().blockPlaceInCornerDiff.add(IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastTimeBlockPlacedCorner);
                    checkable.getMeta().getTimedValues().lastTimeBlockPlacedCorner = IIUA.getCurrentTimeMillis();
                }
                else {
                    checkable.getMeta().getBalanceValues().blockPlaceInCornerDiff.add(IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastTimeBlockPlacedCorner + 1000L);
                }
                if (checkable.getMeta().getBalanceValues().blockPlaceInCornerDiff.size() >= size) {
                    final double balance2 = BalanceUtils.getSquaredBalanceFromLong(checkable.getMeta().getBalanceValues().blockPlaceInCornerDiff);
                    if (balance2 < 200.0) {}
                }
            }
        }
        final Block target = p.getTargetBlock((Set)null, 10);
        if (!target.getRelative(BlockFace.NORTH).equals(e.getBlock()) && !target.getRelative(BlockFace.EAST).equals(e.getBlock()) && !target.getRelative(BlockFace.SOUTH).equals(e.getBlock()) && !target.getRelative(BlockFace.WEST).equals(e.getBlock()) && IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastTimeBlockPlacedUnder < 380L && p.getVelocity().getY() < 0.0 && p.isSprinting()) {
            if (checkable.getMeta().getHeuristicValues().scaffoldwalkVL * 1.1 < 100.0) {
                final Checkable.CheckableMeta.HeuristicValues heuristicValues3 = checkable.getMeta().getHeuristicValues();
                heuristicValues3.scaffoldwalkVL *= 1.1;
            }
            if (checkable.getMeta().getHeuristicValues().scaffoldwalkVL > 90.0 && this.plugin.getRetributionManager().markPlayer(p, 1, "MachineBlock", CheatCategory.WORLD, "tried to place a block suspiciously.")) {
                e.setCancelled(true);
            }
        }
    }
    
    public double addNumberAndGetBalance(final List<Long> doubles, final long toAdd, final int maxSize) {
        return BalanceUtils.getSquaredBalanceFromLong(this.addDynamic(doubles, toAdd, maxSize));
    }
    
    private <T> List<T> addDynamic(final List<T> list, final T toAdd, final double maxSize) {
        list.add(toAdd);
        if (list.size() > maxSize) {
            list.remove(0);
        }
        return list;
    }
    
    private boolean isSuspicious(final double f) {
        return f == 0.0 || f == 0.5 || f >= 1.0;
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
    
    @Override
    public void onCheckableMove(final CheckableMoveEvent e) {
        final Player p = e.getBukkitPlayer();
        final UUID uuid = IIUA.getUUIDFrom(p);
        if (!this.lastBlockPlaced.containsKey(uuid)) {
            this.lastBlockPlaced.put(uuid, System.currentTimeMillis());
        }
        if (!this.lastWentDown.containsKey(uuid)) {
            this.lastWentDown.put(uuid, System.currentTimeMillis());
        }
        else if (p.getFallDistance() > 0.0f && e.getTo().getY() - e.getFrom().getY() < 0.0) {
            this.lastWentDown.replace(uuid, System.currentTimeMillis());
        }
        final double xzMotionDiff = MathHelper.diff(e.getXZDirectedMotion(), e.getCheckable().getMeta().getSyncedValues().lastXZMovement);
        final double xzConjToLastxz = e.getXZDirectedMotion() / e.getCheckable().getMeta().getSyncedValues().lastXZMovement;
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeBlockPlacedUnder < 500L && e.getXZDirectedMotion() > 0.0 && !IIUA.getAllowFlight(p)) {
            final boolean unsneak = !p.isSneaking() && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastSneakToggled < 100L;
            final boolean bad = (xzConjToLastxz < 0.4 || (MathHelper.diff(xzConjToLastxz, 1.0) < 1.0E-4 && xzConjToLastxz != 1.0 && !p.isSneaking())) && e.getYDirectedMotion() == 0.0;
            if (bad) {
                this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getHeuristicValues().lastMotionBlockFlag = IIUA.getCurrentTimeMillis();
            }
        }
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType().equals((Object)Material.AIR) && !p.isSneaking() && e.getYDirectedMotion() == 0.0 && IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeBlockPlacedUnder < 500L) {
            final double blockDist = e.getFrom().getBlock().getRelative(BlockFace.DOWN).getLocation().clone().add(0.5, 0.5, 0.5).distance(e.getTo());
            if (blockDist > 0.76) {}
            if (((xzMotionDiff < 0.07 || blockDist <= 0.8) && (xzMotionDiff >= 1.0E-5 || blockDist <= 0.76)) || IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeBlockPlacedUnder < 400L) {}
            if (xzMotionDiff > 0.09) {
                e.getCheckable().getMeta().getVioValues().lastTimeSuspiciousForSaveWalk = IIUA.getCurrentTimeMillis();
            }
        }
        if (!this.lastsneak.containsKey(uuid)) {
            this.lastsneak.put(uuid, IIUA.getCurrentTimeMillis());
        }
        if (!this.clicks.containsKey(uuid)) {
            this.clicks.put(uuid, 0);
        }
        if (!this.lvl.containsKey(uuid)) {
            this.lvl.put(uuid, 0);
        }
        if (!this.lastinteract.containsKey(uuid)) {
            this.lastinteract.put(uuid, IIUA.getCurrentTimeMillis());
        }
        if (!this.lastclear.containsKey(uuid)) {
            this.lastclear.put(uuid, IIUA.getCurrentTimeMillis());
        }
        if (!this.lastblockplace.containsKey(uuid)) {
            this.lastblockplace.put(uuid, IIUA.getCurrentTimeMillis());
        }
        if (!this.lastmove.containsKey(uuid)) {
            this.lastmove.put(uuid, IIUA.getCurrentTimeMillis());
        }
        if (!this.mode.containsKey(uuid)) {
            this.mode.put(uuid, 0);
        }
        if (e.isMoving()) {
            this.lastmove.replace(uuid, IIUA.getCurrentTimeMillis());
        }
        if (p.isSneaking()) {
            this.lastsneak.replace(uuid, IIUA.getCurrentTimeMillis());
        }
    }
    
    @EventHandler
    public void on(final PlayerToggleSneakEvent e) {
        final Player p = e.getPlayer();
        final Checkable checkable = this.plugin.catchCheckable(IIUA.getUUIDFrom(p));
        if (e.isSneaking() && Math.abs(checkable.getMeta().getSyncedValues().lastYmovement) < 0.005 && IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastTimeBlockPlaced < 500L) {
            checkable.getMeta().getHeuristicValues().lastSneakWasSuspicious = p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().equals((Object)Material.AIR);
        }
        if (!e.isSneaking()) {
            final int tSneaking = Math.toIntExact(checkable.getMeta().getSyncedValues().ticksSneaking);
            if (checkable.getMeta().getHeuristicValues().lastSneakWasSuspicious && tSneaking == checkable.getMeta().getHeuristicValues().lastSneakDurationTicks) {
                final int vl = checkable.getMeta().getHeuristicValues().machineSneakVL + 1;
                if (vl > 8) {
                    this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getHeuristicValues().lastMotionBlockFlag = IIUA.getCurrentTimeMillis();
                }
                else {
                    final Checkable.CheckableMeta.HeuristicValues heuristicValues = checkable.getMeta().getHeuristicValues();
                    ++heuristicValues.machineSneakVL;
                }
            }
            else if (checkable.getMeta().getHeuristicValues().machineSneakVL > 1) {
                final Checkable.CheckableMeta.HeuristicValues heuristicValues2 = checkable.getMeta().getHeuristicValues();
                heuristicValues2.machineSneakVL -= 2;
            }
            checkable.getMeta().getHeuristicValues().lastSneakDurationTicks = tSneaking;
            checkable.getMeta().getHeuristicValues().lastSneakWasSuspicious = false;
        }
    }
    
    @EventHandler
    public void onPlace(final BlockPlaceEvent e) {
        if (!this.isActivated()) {
            return;
        }
        final Player p = e.getPlayer();
        final UUID uuid = IIUA.getUUIDFrom(p);
        if (p.isSneaking()) {
            this.lastsneak.replace(uuid, IIUA.getCurrentTimeMillis());
        }
        if (!this.lastsneak.containsKey(uuid)) {
            this.lastsneak.put(uuid, IIUA.getCurrentTimeMillis());
        }
        if (!this.clicks.containsKey(uuid)) {
            this.clicks.put(uuid, 0);
        }
        if (!this.lvl.containsKey(uuid)) {
            this.lvl.put(uuid, 0);
        }
        if (!this.lastinteract.containsKey(uuid)) {
            this.lastinteract.put(uuid, IIUA.getCurrentTimeMillis());
        }
        if (!this.lastclear.containsKey(uuid)) {
            this.lastclear.put(uuid, IIUA.getCurrentTimeMillis());
        }
        if (!this.lastblockplace.containsKey(uuid)) {
            this.lastblockplace.put(uuid, IIUA.getCurrentTimeMillis());
        }
        if (!this.mode.containsKey(uuid)) {
            this.mode.put(uuid, 0);
        }
        if (!this.lastmove.containsKey(uuid)) {
            this.lastmove.put(uuid, IIUA.getCurrentTimeMillis());
        }
        final double lastcleared = (double)(IIUA.getCurrentTimeMillis() - this.lastclear.get(uuid));
        final double blockplacedelay = (double)(IIUA.getCurrentTimeMillis() - this.lastblockplace.get(uuid));
        final double lastsneakdelay = (double)(IIUA.getCurrentTimeMillis() - this.lastsneak.get(uuid));
        if (this.lvl.get(uuid) >= 1 && lastcleared > 1200.0) {
            this.lvl.replace(uuid, this.lvl.get(uuid) - 1);
            this.lastclear.replace(uuid, IIUA.getCurrentTimeMillis());
        }
        if (!this.lastBlock.containsKey(uuid)) {
            this.lastBlock.put(uuid, e.getBlock());
        }
        if (!this.lastBlockPlaced.containsKey(uuid)) {
            this.lastBlockPlaced.put(uuid, System.currentTimeMillis());
        }
        if (!this.lastWentDown.containsKey(uuid)) {
            this.lastWentDown.put(uuid, System.currentTimeMillis());
        }
        final long lastBlockPlaced2 = System.currentTimeMillis() - this.lastBlockPlaced.get(uuid);
        final long lastWentDown2 = System.currentTimeMillis() - this.lastWentDown.get(uuid);
        final Checkable checkable = this.plugin.catchCheckable(IIUA.getUUIDFrom(p));
        if (lastBlockPlaced2 < lastWentDown2 - lastBlockPlaced2 && e.getPlayer().getLocation().getY() > e.getBlockPlaced().getY() && !p.getAllowFlight() && !p.hasPotionEffect(PotionEffectType.JUMP) && MathHelper.diff(e.getBlock().getLocation().getY() - 2.0, this.penaltyBlock.getOrDefault(uuid, e.getBlock()).getLocation().getY()) < 1.0E-4 && e.getBlock().getLocation().getY() - 1.0 == this.lastBlock.get(uuid).getLocation().getY()) {
            final boolean legit = String.valueOf(e.getPlayer().getLocation().getY()).length() > 6;
            final double lastTowerhopFlag = (double)(IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues().lastTimeSuspiciousForTowerhop);
            if (lastTowerhopFlag < 4000.0 && !legit && this.plugin.getRetributionManager().markPlayer(p, 1, "MachineBlock", CheatCategory.WORLD, "tried to place a block suspiciously." + "")) {
                e.setCancelled(true);
            }
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues().lastTimeSuspiciousForTowerhop = IIUA.getCurrentTimeMillis();
        }
        this.penaltyBlock.put(uuid, this.lastBlock.getOrDefault(uuid, e.getBlock()));
        this.lastBlock.put(uuid, e.getBlock());
        if (e.getBlock().getLocation().add(0.0, -1.0, 0.0).getBlock().getType() != Material.AIR || e.getBlock().getLocation().add(-1.0, 0.0, -1.0).getBlock().getType() != Material.AIR || e.getBlock().getLocation().add(1.0, 0.0, 1.0).getBlock().getType() != Material.AIR || e.getBlock().getLocation().add(-1.0, 0.0, 1.0).getBlock().getType() != Material.AIR || e.getBlock().getLocation().add(1.0, 0.0, -1.0).getBlock().getType() != Material.AIR || e.getBlock().getLocation().getY() != p.getLocation().getY() - 1.0) {
            return;
        }
        if (!this.lastblocks.containsKey(uuid)) {
            this.lastblocks.put(uuid, new ArrayList<Block>());
            this.lastblocks.get(uuid).add(e.getBlock());
        }
        else if (this.lastblocks.get(uuid).size() == 1) {
            this.lastblocks.get(uuid).add(e.getBlock());
        }
        else if (this.lastblocks.get(uuid).size() == 2) {
            this.lastblocks.get(uuid).add(e.getBlock());
        }
        else if (this.lastblocks.get(uuid).size() == 3) {
            this.lastblocks.get(uuid).remove(0);
            this.lastblocks.get(uuid).add(e.getBlock());
        }
        this.lastblocks.get(uuid).add(e.getBlock());
        final Block block0m = this.lastblocks.get(uuid).get(0);
        final Block block1m = this.lastblocks.get(uuid).get(1);
        if (block0m.getFace(block1m).equals((Object)BlockFace.NORTH)) {
            this.lastblockplace.replace(uuid, IIUA.getCurrentTimeMillis());
            if (block0m.getRelative(BlockFace.NORTH_EAST).equals(e.getBlock())) {
                this.mode.replace(uuid, 1);
            }
            else if (block0m.getRelative(BlockFace.NORTH_WEST).equals(e.getBlock())) {
                this.mode.replace(uuid, 1);
            }
            else {
                this.mode.replace(uuid, 0);
            }
        }
        else if (block0m.getFace(block1m).equals((Object)BlockFace.EAST)) {
            this.lastblockplace.replace(uuid, IIUA.getCurrentTimeMillis());
            if (block0m.getRelative(BlockFace.NORTH_EAST).equals(e.getBlock())) {
                this.mode.replace(uuid, 1);
            }
            else if (block0m.getRelative(BlockFace.SOUTH_EAST).equals(e.getBlock())) {
                this.mode.replace(uuid, 1);
            }
            else {
                this.mode.replace(uuid, 0);
            }
        }
        else if (block0m.getFace(block1m).equals((Object)BlockFace.SOUTH)) {
            this.lastblockplace.replace(uuid, IIUA.getCurrentTimeMillis());
            if (block0m.getRelative(BlockFace.SOUTH_EAST).equals(e.getBlock())) {
                this.mode.replace(uuid, 1);
            }
            else if (block0m.getRelative(BlockFace.SOUTH_WEST).equals(e.getBlock())) {
                this.mode.replace(uuid, 1);
            }
            else {
                this.mode.replace(uuid, 0);
            }
        }
        else if (block0m.getFace(block1m).equals((Object)BlockFace.WEST)) {
            this.lastblockplace.replace(uuid, IIUA.getCurrentTimeMillis());
            if (block0m.getRelative(BlockFace.SOUTH_WEST).equals(e.getBlock())) {
                this.mode.replace(uuid, 1);
            }
            else if (block0m.getRelative(BlockFace.NORTH_WEST).equals(e.getBlock())) {
                this.mode.replace(uuid, 1);
            }
            else {
                this.mode.replace(uuid, 0);
            }
        }
        this.lastblockplace.replace(uuid, IIUA.getCurrentTimeMillis());
        if (lastsneakdelay > blockplacedelay && this.mode.get(uuid) == 0 && blockplacedelay < 600.0 && this.clicks.get(uuid) == 1) {
            this.lvl.replace(uuid, this.lvl.get(uuid) + 1);
            if (this.lvl.get(uuid) >= 3 && this.plugin.getRetributionManager().markPlayer(p, 1, "MachineBlock", CheatCategory.WORLD, "tried to place a block suspiciously." + "")) {
                e.setCancelled(true);
            }
        }
        this.clicks.replace(uuid, 0);
    }
    
    @EventHandler
    public void onInteract(final PlayerInteractEvent e) {
        if (!this.isActivated()) {
            return;
        }
        final Player p = e.getPlayer();
        final UUID uuid = IIUA.getUUIDFrom(p);
        if (!this.clicks.containsKey(uuid)) {
            this.clicks.put(uuid, 0);
        }
        if (p.isSneaking()) {
            this.lastsneak.replace(uuid, IIUA.getCurrentTimeMillis());
        }
        if (e.getAction().equals((Object)Action.RIGHT_CLICK_BLOCK)) {
            this.clicks.replace(uuid, this.clicks.get(uuid) + 1);
        }
    }
}
