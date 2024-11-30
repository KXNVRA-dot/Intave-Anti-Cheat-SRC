// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.world;

import org.bukkit.event.inventory.InventoryOpenEvent;
import de.jpx3.intave.util.calc.BalanceUtils;
import de.jpx3.intave.util.event.CheckableMoveEvent;
import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;
import java.util.Iterator;
import java.util.List;
import de.jpx3.intave.util.calc.LocationHelper;
import de.jpx3.intave.util.data.BlockData;
import de.jpx3.intave.util.calc.PlayerUtils;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;
import de.jpx3.intave.util.objectable.Checkable;
import org.bukkit.GameMode;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;
import de.jpx3.intave.antipiracy.IIUA;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.PacketType;
import de.jpx3.intave.api.internal.ProtocolLibAdapter;
import org.bukkit.plugin.Plugin;
import de.jpx3.intave.util.enums.CheatCategory;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.event.Listener;
import de.jpx3.intave.util.objectable.IntaveCheck;

public final class InteractCheck extends IntaveCheck implements Listener
{
    private final IntavePlugin plugin;
    
    public InteractCheck(final IntavePlugin plugin) {
        super("Interact", CheatCategory.WORLD);
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)plugin);
        final IntavePlugin plugin2 = plugin;
        ProtocolLibAdapter.getProtocolManager().addPacketListener((PacketListener)new PacketAdapter(plugin, new PacketType[] { PacketType.Play.Client.CLIENT_COMMAND, PacketType.Play.Client.CLOSE_WINDOW, PacketType.Play.Client.WINDOW_CLICK }) {
            public void onPacketReceiving(final PacketEvent event) {
                if (!InteractCheck.this.isActivated()) {
                    return;
                }
                final Player p = event.getPlayer();
                final PacketContainer packetContainer = event.getPacket();
                if (packetContainer.getType().equals((Object)PacketType.Play.Client.CLIENT_COMMAND)) {
                    if (packetContainer.getClientCommands().getValues().get(0).equals((Object)EnumWrappers.ClientCommand.OPEN_INVENTORY_ACHIEVEMENT)) {
                        plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().hasAnOpenInventory = true;
                        plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeOpenedIntentory = IIUA.getCurrentTimeMillis();
                    }
                }
                else if (packetContainer.getType().equals((Object)PacketType.Play.Client.CLOSE_WINDOW)) {
                    plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().hasAnOpenInventory = false;
                    plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeClosedInventory = IIUA.getCurrentTimeMillis();
                }
            }
        });
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final InventoryCloseEvent e) {
        final Player p = (Player)e.getPlayer();
        this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().hasAnOpenInventory = false;
        this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeClosedInventory = IIUA.getCurrentTimeMillis();
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void on2(final InventoryClickEvent e) {
        if (!this.isActivated()) {
            return;
        }
        final Player p = (Player)e.getWhoClicked();
        if (p.getGameMode().equals((Object)GameMode.CREATIVE)) {
            return;
        }
        if ((p.isSprinting() || p.isSneaking()) && this.shouldDetectInvWalk() && this.plugin.getRetributionManager().markPlayer(p, 5, "Interact", CheatCategory.WORLD, "tried to click on items whilest walking.")) {
            p.closeInventory();
        }
        if (e.getCurrentItem() == null) {
            return;
        }
        if (this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().lastClickedItemStack != null && this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().lastClickedItemStack.getType().equals((Object)e.getCurrentItem().getType())) {
            return;
        }
        final Checkable checkable = this.plugin.catchCheckable(IIUA.getUUIDFrom(p));
        final int slot = e.getSlot();
        final int lastSlot = checkable.getMeta().getSyncedValues().lastClickedSlot;
        final double distance = this.distanceBetween(slot, lastSlot);
        final double min_time = distance * 40.0;
        final long time = IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastClickInv;
        final int vl = (time < min_time) ? ((time < min_time / 2.0) ? 8 : 4) : 0;
        final Checkable.CheckableMeta.ViolationValues vioValues = checkable.getMeta().getVioValues();
        final int itemStealerVL = vioValues.itemStealerVL + vl;
        vioValues.itemStealerVL = itemStealerVL;
        final int totalvl = itemStealerVL;
        if (vl < 2 && totalvl > 2) {
            final Checkable.CheckableMeta.ViolationValues vioValues2 = checkable.getMeta().getVioValues();
            vioValues2.itemStealerVL -= ((time * 2L > min_time) ? 2 : 1);
        }
        final double speedAttr = time / distance;
        if (totalvl > 4 && vl > 2 && time > 0L) {
            if (this.plugin.getRetributionManager().markPlayer(p, 5 + totalvl, "Interact", CheatCategory.WORLD, "tried to click too fast on items (" + (int)speedAttr + " ms/slot)")) {
                e.setCancelled(true);
            }
        }
        else if (time < 1L) {
            e.setCancelled(true);
        }
        checkable.getMeta().getSyncedValues().lastClickedSlot = slot;
        checkable.getMeta().getTimedValues().lastClickInv = IIUA.getCurrentTimeMillis();
        checkable.getMeta().getSyncedValues().lastClickedItemStack = e.getCurrentItem();
    }
    
    public int[] translatePosition(final int slot) {
        final int row = slot / 9 + 1;
        final int rowPosition = slot - (row - 1) * 9;
        return new int[] { row, rowPosition };
    }
    
    public double distanceBetween(final int slot1, final int slot2) {
        final int[] slot1XZ = this.translatePosition(slot1);
        final int[] slot2XZ = this.translatePosition(slot2);
        return Math.sqrt((slot1XZ[0] - slot2XZ[0]) * (slot1XZ[0] - slot2XZ[0]) + (slot1XZ[1] - slot2XZ[1]) * (slot1XZ[1] - slot2XZ[1]));
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void on(final PlayerInteractEvent e) {
        if (!this.isActivated()) {
            return;
        }
        final Player p = e.getPlayer();
        final Checkable checkable = this.plugin.catchCheckable(IIUA.getUUIDFrom(p));
        if (e.hasBlock()) {
            final boolean leftClick = false;
            final Block interacted = e.getClickedBlock();
            final boolean currentValidation = this.validateRayTrace(interacted, e.getBlockFace(), p.getLocation(), false);
            final boolean validationLastTick = this.validateRayTrace(interacted, e.getBlockFace(), checkable.getMeta().getLocationValues().lastLocationServerTickSync, false);
            final boolean validationPenTick = this.validateRayTrace(interacted, e.getBlockFace(), checkable.getMeta().getLocationValues().penaltyLocationServerTickSync, false);
            boolean flag = false;
            if (!currentValidation && !validationLastTick && !validationPenTick) {
                flag = (checkable.getMeta().getVioValues().interactVL > 0);
            }
            if (flag) {
                e.setCancelled(true);
                if (checkable.getMeta().getVioValues().interactVL > 1) {
                    this.plugin.getRetributionManager().markPlayer(p, 10, "Interact", CheatCategory.WORLD, "tried to interact with a block out of his line of sight");
                }
            }
            final Checkable.CheckableMeta.ViolationValues vioValues = checkable.getMeta().getVioValues();
            vioValues.interactVL += ((!currentValidation && !validationLastTick && checkable.getMeta().getVioValues().interactVL < 5) ? 1 : ((checkable.getMeta().getVioValues().interactVL > 0) ? -1 : 0));
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void on(final BlockBreakEvent e) {
        final boolean currentValidation = this.validateRayTrace(e.getBlock(), BlockFace.SELF, e.getPlayer().getLocation(), true);
        e.setCancelled(!currentValidation || e.isCancelled());
    }
    
    private boolean validateRayTrace(final Block interacted, final BlockFace blockFace, final Location location, final boolean strict) {
        final List<Block> s = PlayerUtils.getBlocksInSight(location, 7);
        for (final Block block : s) {
            if (!block.getWorld().equals(interacted.getWorld())) {
                continue;
            }
            final boolean isInteracted = block.equals(interacted);
            final boolean isPassable = BlockData.isPassable(block);
            if (isInteracted || (!strict && LocationHelper.getDistanceSafe(block.getLocation(), interacted.getLocation()) < 1.7)) {
                return true;
            }
            if (!isPassable) {
                return false;
            }
        }
        return false;
    }
    
    private double getSolveError(final Block interacted, final Location location) {
        final List<Block> s = PlayerUtils.getBlocksInSight(location, 7);
        double bestSolve = 10.0;
        for (final Block block : s) {
            if (!block.getWorld().equals(interacted.getWorld())) {
                continue;
            }
            final boolean isPassable = BlockData.isPassable(block);
            if (!isPassable) {
                break;
            }
            bestSolve = Math.min(bestSolve, LocationHelper.getDistanceSafe(block.getLocation(), interacted.getLocation()));
        }
        return bestSolve;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final BlockPlaceEvent e) {
        if (!this.isActivated() || !this.shouldDetectScaffold()) {
            return;
        }
        if (e.isCancelled()) {
            return;
        }
        final Player p = e.getPlayer();
        if (!p.getWorld().equals(e.getBlockPlaced().getWorld()) || !p.getWorld().equals(e.getBlockAgainst().getWorld())) {
            return;
        }
        final double distPlaced = p.getLocation().distance(e.getBlockPlaced().getLocation());
        final double distAgainst = p.getLocation().distance(e.getBlockAgainst().getLocation());
        final boolean cheating = distPlaced > distAgainst && distAgainst > 1.6;
        if (cheating) {
            if (this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues().suspiciousExpandedBlockPlacements > 10 || this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues().blocksToRemove.size() > 10) {
                if (this.plugin.getRetributionManager().markPlayer(p, 1, "Interact", CheatCategory.WORLD, "tried to place a block out of his line of sight")) {
                    this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues().suspiciousExpandedBlockPlacements = 0;
                    for (final Block b : this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues().blocksToRemove) {
                        b.setType(Material.AIR);
                    }
                    this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues().blocksToRemove.clear();
                    e.setCancelled(true);
                }
            }
            else if (this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues().suspiciousExpandedBlockPlacements > 1) {
                this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues().blocksToRemove.add(e.getBlockPlaced());
            }
            final Checkable.CheckableMeta.ViolationValues vioValues = this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues();
            vioValues.suspiciousExpandedBlockPlacements += 2;
        }
        else if (this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues().suspiciousExpandedBlockPlacements > 0) {
            final Checkable.CheckableMeta.ViolationValues vioValues2 = this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues();
            --vioValues2.suspiciousExpandedBlockPlacements;
        }
    }
    
    @Override
    public void onCheckableMove(final CheckableMoveEvent e) {
        this.checkInventoryWalk(e);
        this.checkInventoryWalk2(e);
    }
    
    private void checkInventoryWalk2(final CheckableMoveEvent e) {
        final Player p = e.getBukkitPlayer();
        if (e.getCheckable().getMeta().getSyncedValues().hasAnOpenInventory && !e.isOnGround() && e.getYDirectedMotion() != 0.0 && e.isMoving()) {
            final double var7 = e.getCheckable().getMeta().getSyncedValues().lastXMovement * 0.9100000262260437 - e.getXDirectedMotion();
            final double var8 = e.getCheckable().getMeta().getSyncedValues().lastZMovement * 0.9100000262260437 - e.getZDirectedMotion();
            final double strafe = StrictMath.sqrt(var7 * var7 + var8 * var8);
            if (strafe > 0.02 && this.shouldDetectInvWalk()) {
                if (++e.getCheckable().getMeta().getVioValues().inventoryMoveVL > 2) {
                    this.plugin.getRetributionManager().markPlayer(p, e.getCheckable().getMeta().getVioValues().inventoryMoveVL + 3, "Interact", CheatCategory.WORLD, "moved with open inventory");
                }
            }
            else {
                e.getCheckable().getMeta().getVioValues().inventoryMoveVL = Math.max(e.getCheckable().getMeta().getVioValues().inventoryMoveVL - 2, 0);
            }
        }
    }
    
    private void checkInventoryWalk(final CheckableMoveEvent e) {
        if (!this.isActivated() || e.isCancelled()) {
            return;
        }
        final Player p = e.getBukkitPlayer();
        if (PlayerUtils.getAllowFlight(p)) {
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeStandingOnASlime < 5000L) {
            return;
        }
        if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastDeath < 5000L) {
            return;
        }
        if (e.getCheckable().getMeta().getSyncedValues().hasAnOpenInventory) {
            double maxOpenTime = 2000.0;
            if (p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().equals((Object)Material.ICE) || p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().equals((Object)Material.PACKED_ICE)) {
                maxOpenTime = 1600.0;
            }
            if (IIUA.getCurrentTimeMillis() - e.getCheckable().getMeta().getTimedValues().lastTimeOpenedIntentory < maxOpenTime) {
                return;
            }
            if (e.getCheckable().getMeta().getTimedValues().lastHitByEntityTimestamp > e.getCheckable().getMeta().getTimedValues().lastTimeOpenedIntentory) {
                return;
            }
            final Location to2 = e.getTo().clone();
            to2.setY(e.getFrom().getY());
            if (!e.getFrom().getWorld().equals(to2.getWorld())) {
                e.getCheckable().getMeta().getSyncedValues().hasAnOpenInventory = false;
                return;
            }
            final double f = e.getFrom().distance(to2);
            final double last = this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().lastXZMovement;
            if (last < f || (last > 0.05 && last == f)) {
                e.getCheckable().getMeta().getVioValues().lastTimeClaimedWrongInventory = IIUA.getCurrentTimeMillis();
            }
        }
    }
    
    @EventHandler
    public void on(final InventoryClickEvent e) {
        if (!this.isActivated()) {
            return;
        }
        final Player p = (Player)e.getWhoClicked();
        final long lastTimeOpenedInv = this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeOpenedIntentory;
        final long lastTimeSusFInvMove = this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues().lastTimeClaimedWrongInventory;
        if (e.getInventory() == null || e.getCurrentItem() == null || p.getGameMode().equals((Object)GameMode.CREATIVE)) {
            return;
        }
        if (lastTimeOpenedInv < lastTimeSusFInvMove && this.shouldDetectInvWalk() && this.plugin.getRetributionManager().markPlayer(p, 6, "Interact", CheatCategory.WORLD, "clicked on item whitest moving")) {
            p.closeInventory();
            e.setCancelled(true);
        }
        if (this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().lastClickedItemStack != null && this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().lastClickedItemStack.getType().equals((Object)e.getCurrentItem().getType())) {
            return;
        }
        final long delayToLast = IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeClickedItem;
        final int size = 10;
        if (IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues().lastTimeSuspiciousForChestStealer < 200L) {
            e.setCancelled(true);
        }
        if (this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getBalanceValues().lastItemClickDiffs.size() < 10) {
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getBalanceValues().lastItemClickDiffs.add(delayToLast);
        }
        else {
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getBalanceValues().lastItemClickDiffs.remove(0);
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getBalanceValues().lastItemClickDiffs.add(delayToLast);
        }
        if (this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getBalanceValues().lastItemClickDiffs.size() >= 10) {
            final double balance = BalanceUtils.getSquaredBalanceFromLong(this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getBalanceValues().lastItemClickDiffs);
            if (balance < 100.0 && this.plugin.getRetributionManager().markPlayer(p, 10, "Interact", CheatCategory.WORLD, "is clicking on items too fast! (balance: " + balance + "ms)")) {
                this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues().lastTimeSuspiciousForChestStealer = IIUA.getCurrentTimeMillis();
                e.setCancelled(true);
            }
        }
        this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeClickedItem = IIUA.getCurrentTimeMillis();
        this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().lastClickedItemStack = e.getCurrentItem();
    }
    
    @EventHandler
    public void on(final InventoryOpenEvent e) {
        if (!this.isActivated()) {
            return;
        }
        if (e.getInventory() != null) {
            this.plugin.catchCheckable(e.getPlayer().getUniqueId()).getMeta().getTimedValues().lastTimeOpenedIntentory = IIUA.getCurrentTimeMillis();
        }
    }
    
    private boolean shouldDetectScaffold() {
        return this.plugin.getConfig().getBoolean(this.getConfigPath() + ".check_blockplacement");
    }
    
    private boolean shouldDetectInvWalk() {
        return this.plugin.getConfig().getBoolean(this.getConfigPath() + ".block_inventorywalk");
    }
}
