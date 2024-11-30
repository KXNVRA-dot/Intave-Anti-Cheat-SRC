// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.combat;

import org.bukkit.Effect;
import de.jpx3.intave.util.data.BlockData;
import org.bukkit.World;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import org.bukkit.util.Vector;
import de.jpx3.intave.util.event.CheckableDamageEntityEvent;
import com.comphenix.protocol.events.PacketListener;
import de.jpx3.intave.util.objectable.BukkitEntityHitbox;
import de.jpx3.intave.util.objectable.Checkable;
import org.bukkit.entity.Player;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.event.Cancellable;
import de.jpx3.intave.module.PersistentDebugTelemetry;
import org.bukkit.permissions.Permissible;
import de.jpx3.intave.api.external.IntavePermission;
import de.jpx3.intave.util.calc.HitBoxService;
import de.jpx3.intave.util.calc.LocationHelper;
import de.jpx3.intave.antipiracy.IIUA;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.PacketType;
import de.jpx3.intave.api.internal.ProtocolLibAdapter;
import org.bukkit.plugin.Plugin;
import de.jpx3.intave.util.enums.CheatCategory;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.event.Listener;
import de.jpx3.intave.util.objectable.IntaveCheck;

public final class HitBoxCheck extends IntaveCheck implements Listener
{
    private final IntavePlugin plugin;
    private final double hitbox_expand;
    private final boolean strict_block;
    
    public HitBoxCheck(final IntavePlugin plugin) {
        super("Hitbox", CheatCategory.COMBAT);
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)plugin);
        this.hitbox_expand = Math.max(0.15, plugin.getConfig().getDouble(this.getConfigPath() + ".hitbox_expand"));
        this.strict_block = plugin.getConfig().getBoolean(this.getConfigPath() + ".strict_block");
        final IntavePlugin reference = plugin;
        ProtocolLibAdapter.getProtocolManager().addPacketListener((PacketListener)new PacketAdapter(plugin, new PacketType[] { PacketType.Play.Client.USE_ENTITY }) {
            public void onPacketReceiving(final PacketEvent event) {
                final PacketContainer packet = event.getPacket();
                final Player p = event.getPlayer();
                final Entity e = (Entity)packet.getEntityModifier(event).read(0);
                if (e == null || !HitBoxCheck.this.isActivated()) {
                    return;
                }
                if (p.getAllowFlight() || p.getGameMode().equals((Object)GameMode.CREATIVE)) {
                    return;
                }
                final Checkable checkable = reference.catchCheckable(IIUA.getUUIDFrom(p));
                final Checkable damagedCheckable = reference.catchCheckable(e.getUniqueId());
                final double nomPosNomPos = LocationHelper.getDistanceSafe(checkable.asBukkitPlayer().getLocation(), damagedCheckable.asBukkitPlayer().getLocation());
                final double kPosNomPos = LocationHelper.getDistanceSafe(checkable.getMeta().getLocationValues().lastLocation, damagedCheckable.asBukkitPlayer().getLocation());
                final double nomPoskPos = LocationHelper.getDistanceSafe(checkable.asBukkitPlayer().getLocation(), damagedCheckable.getMeta().getLocationValues().lastLocation);
                final double kPoskPos = LocationHelper.getDistanceSafe(checkable.getMeta().getLocationValues().lastLocation, damagedCheckable.getMeta().getLocationValues().lastLocation);
                final double minPos = Math.min(Math.min(nomPoskPos, kPoskPos), Math.min(nomPosNomPos, kPosNomPos)) - 0.6;
                if (IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastHitOtherTimestamp >= 500L) {
                    final boolean b = minPos > 2.8;
                }
                final BukkitEntityHitbox hitbox = HitBoxService.loadBoundingBox(e);
                final double reach = p.getLocation().distance(e.getLocation()) - hitbox.getBoxWidth() / 2.0;
                final double calcreach = p.getLocation().clone().add(p.getVelocity().clone().multiply(1.6)).distance(e.getLocation()) - hitbox.getBoxWidth() / 2.0;
                if (Math.min(reach, calcreach) > 3.0 + HitBoxCheck.this.hitbox_expand && HitBoxCheck.this.strict_block && !reference.getPermissionManager().hasPermission(IntavePermission.BYPASS, (Permissible)p)) {
                    PersistentDebugTelemetry.hitCancel(p, (Cancellable)event, "hbox-aGm-str");
                }
                checkable.getMeta().getHeuristicValues().needsAttack = false;
                if (Math.min(reach, calcreach) > 3.4 + HitBoxCheck.this.hitbox_expand) {
                    if (IIUA.getCurrentTimeMillis() - checkable.getMeta().getTimedValues().lastHitOtherTimestamp >= 500L) {
                        if (checkable.getMeta().getVioValues().hitboxRVL > 1) {
                            if (HitBoxCheck.this.applyBuffer(checkable)) {
                                if (reference.getRetributionManager().markPlayer(p, 1, "HitBox", CheatCategory.COMBAT, "missed entity when attacking (reach)") && !event.isCancelled()) {
                                    PersistentDebugTelemetry.hitCancel(p, (Cancellable)event, "hbox-aGm-lt");
                                }
                            }
                            else if (!reference.getPermissionManager().hasPermission(IntavePermission.BYPASS, (Permissible)p)) {
                                PersistentDebugTelemetry.hitCancel(p, (Cancellable)event, "hbox-aGm-lt_?rar");
                            }
                        }
                        else {
                            final Checkable.CheckableMeta.ViolationValues vioValues = checkable.getMeta().getVioValues();
                            ++vioValues.hitboxRVL;
                        }
                    }
                    else {
                        PersistentDebugTelemetry.hitCancel(p, (Cancellable)event, "hbox-aGm-let-str");
                    }
                }
                else if (checkable.getMeta().getVioValues().hitboxRVL > 0) {
                    final Checkable.CheckableMeta.ViolationValues vioValues2 = checkable.getMeta().getVioValues();
                    --vioValues2.hitboxRVL;
                }
            }
        });
    }
    
    @Override
    public void onCheckableDamageEntity(final CheckableDamageEntityEvent event) {
        final Player p = event.getBukkitPlayer();
        final Entity e = event.getEntity();
        if (this.plugin.getPermissionManager().hasPermission(IntavePermission.BYPASS, (Permissible)p) || !this.isActivated()) {
            return;
        }
        if (p.getAllowFlight()) {
            return;
        }
        final BukkitEntityHitbox hitbox = HitBoxService.loadBoundingBox(e);
        if (e instanceof Player) {}
        if (!this.intersects(p.getWorld(), p.getEyeLocation().toVector(), p.getLocation().getDirection(), hitbox, 4.0, 0.125, this.hitbox_expand)) {
            final boolean failed = !this.intersects(p.getWorld(), p.getEyeLocation().toVector(), p.getLocation().getDirection(), hitbox, 4.25, 0.125, this.hitbox_expand * 2.0);
            if (failed && !p.hasLineOfSight(e)) {
                if (this.strict_block || this.plugin.getRetributionManager().markPlayer(p, 1, "HitBox", CheatCategory.COMBAT, "tried to attack through block")) {
                    PersistentDebugTelemetry.hitCancel(p, (Cancellable)event, "hbox-raytrx");
                }
                final Checkable.CheckableMeta.ViolationValues vioValues = event.getCheckable().getMeta().getVioValues();
                ++vioValues.hitboxFOVVL;
                return;
            }
            if (this.strict_block && failed && event.getCheckable().getMeta().getVioValues().hitboxFOVVL > 1) {
                PersistentDebugTelemetry.hitCancel(p, (Cancellable)event, "hbox-dirtr-str");
            }
            if (failed && event.getCheckable().getMeta().getVioValues().hitboxFOVVL > 1) {
                if (this.plugin.getRetributionManager().markPlayer(p, 1, "HitBox", CheatCategory.COMBAT, "missed entity when attacking (field of view)") && !event.isCancelled()) {
                    PersistentDebugTelemetry.hitCancel(p, (Cancellable)event, "hbox-dirtr-lt");
                }
            }
            else {
                final Checkable.CheckableMeta.ViolationValues vioValues2 = event.getCheckable().getMeta().getVioValues();
                ++vioValues2.hitboxFOVVL;
            }
        }
        else if (event.getCheckable().getMeta().getVioValues().hitboxFOVVL > 0) {
            final Checkable.CheckableMeta.ViolationValues vioValues3 = event.getCheckable().getMeta().getVioValues();
            --vioValues3.hitboxFOVVL;
        }
    }
    
    private synchronized boolean applyBuffer(final Checkable checkable) {
        if (IIUA.getCurrentTimeMillis() - checkable.getMeta().getVioValues().lastHitboxFlag > 150L) {
            checkable.getMeta().getVioValues().lastHitboxFlag = IIUA.getCurrentTimeMillis();
            return true;
        }
        return false;
    }
    
    private Vector getPostion(final Vector origin, final Vector direction, final double blocksAway) {
        return origin.clone().add(direction.clone().multiply(blocksAway));
    }
    
    private List<Vector> traverse(final Vector origin, final Vector direction, final double blocksAway, final double accuracy) {
        final List<Vector> positions = new CopyOnWriteArrayList<Vector>();
        for (double d = 0.0; d <= blocksAway; d += accuracy) {
            positions.add(this.getPostion(origin, direction, d));
        }
        return positions;
    }
    
    public boolean intersects(final Vector origin, final Vector direction, final Vector min, final Vector max, final double blocksAway, final double accuracy, final double hitbox_expand) {
        final List<Vector> positions = this.traverse(origin, direction, blocksAway, accuracy);
        return positions.stream().anyMatch(position -> this.intersects(position, min, max, hitbox_expand));
    }
    
    private boolean intersects(final World world, final Vector origin, final Vector direction, final BukkitEntityHitbox boundingBox, final double blocksAway, final double accuracy, final double hitbox_expand) {
        for (double d = 0.0; d <= blocksAway; d += accuracy) {
            final Vector position = this.getPostion(origin, direction, d);
            if (!BlockData.isPassable(position.toLocation(world).getBlock())) {
                return false;
            }
            if (this.intersects(position, boundingBox.getMin3DVector(), boundingBox.getMax3dVector(), hitbox_expand)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean intersects(final Vector position, final Vector min, final Vector max, final double hitbox_expand) {
        return position.getX() >= min.getX() - hitbox_expand && position.getX() <= max.getX() + hitbox_expand && position.getY() >= min.getY() - hitbox_expand && position.getY() <= max.getY() + hitbox_expand && position.getZ() >= min.getZ() - hitbox_expand && position.getZ() <= max.getZ() + hitbox_expand;
    }
    
    private void highlight(final Vector origin, final Vector direction, final World world, final double blocksAway, final double accuracy) {
        this.traverse(origin, direction, blocksAway, accuracy).forEach(position -> world.playEffect(position.toLocation(world), Effect.COLOURED_DUST, 0));
    }
}
