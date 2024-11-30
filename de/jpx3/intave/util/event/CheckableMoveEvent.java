// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.event;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import de.jpx3.intave.util.calc.LocationHelper;
import de.jpx3.intave.util.calc.YawUtil;
import de.jpx3.intave.antipiracy.IIUA;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import de.jpx3.intave.util.objectable.MovingData;
import de.jpx3.intave.util.objectable.Checkable;

public final class CheckableMoveEvent extends iIntaveInternalEvent
{
    private final Checkable checkable;
    private final MovingData movingData;
    private volatile Player player;
    private volatile Location from;
    private volatile Location to;
    private Vector motionVector;
    private Boolean onGround;
    private double yMotion;
    private double xzMotion;
    private double xMotion;
    private double zMotion;
    private float direction;
    public long timeOfQuere;
    
    public CheckableMoveEvent(final PlayerMoveEvent e) {
        this.motionVector = new Vector(0, 0, 0);
        this.onGround = null;
        this.yMotion = -1.0;
        this.xzMotion = -1.0;
        this.xMotion = -1.0;
        this.zMotion = -1.0;
        this.direction = -1.0f;
        this.timeOfQuere = System.nanoTime();
        this.player = e.getPlayer();
        this.from = e.getFrom();
        this.to = e.getTo();
        this.movingData = new MovingData();
        this.checkable = IntavePlugin.getStaticReference().catchCheckable(IIUA.getUUIDFrom(e.getPlayer()));
    }
    
    public final Player getBukkitPlayer() {
        return this.player;
    }
    
    public final Location getTo() {
        return this.to;
    }
    
    public final Location getFrom() {
        return this.from;
    }
    
    public void setTo(final Location location) {
        this.to = location;
    }
    
    public void setFrom(final Location location) {
        this.from = location;
    }
    
    public final Checkable getCheckable() {
        synchronized (this.checkable) {
            return this.checkable;
        }
    }
    
    public final Checkable.CachingManager getCache() {
        return this.getCheckable().getCorrelatingCacher();
    }
    
    public final double getXZDirectedMotion() {
        if (this.xzMotion == -1.0) {
            final double var1 = this.getFrom().getX() - this.getTo().getX();
            final double var2 = this.getFrom().getZ() - this.getTo().getZ();
            this.xzMotion = StrictMath.sqrt(var1 * var1 + var2 * var2);
        }
        return this.xzMotion;
    }
    
    public final double getXDirectedMotion() {
        if (this.xMotion == -1.0) {
            this.xMotion = this.getTo().getX() - this.getFrom().getX();
        }
        return this.xMotion;
    }
    
    public final double getZDirectedMotion() {
        if (this.zMotion == -1.0) {
            this.zMotion = this.getTo().getZ() - this.getFrom().getZ();
        }
        return this.zMotion;
    }
    
    public final double getYDirectedMotion() {
        if (this.yMotion == -1.0) {
            this.yMotion = this.getTo().getY() - this.getFrom().getY();
        }
        return this.yMotion;
    }
    
    public final float getDirection() {
        if (this.direction == -1.0f) {
            this.direction = YawUtil.getYawFrom(this.getFrom(), this.getTo());
        }
        return this.direction;
    }
    
    public final boolean isMoving() {
        return this.getFrom().getX() != this.getTo().getX() || this.getFrom().getY() != this.getTo().getY() || this.getFrom().getZ() != this.getTo().getZ();
    }
    
    public final boolean isRotating() {
        return this.getFrom().getYaw() != this.getTo().getYaw() || this.getFrom().getPitch() != this.getTo().getPitch();
    }
    
    public final boolean isOnGround() {
        if (this.onGround == null) {
            this.onGround = (LocationHelper.isOnGroundAccurate(this.getCheckable().getMeta().getLocationValues().penaltyLocation, (Entity)this.getBukkitPlayer()) || this.getCheckable().getMeta().getSyncedValues().lastYmovement > 0.0 || LocationHelper.isOnGroundAccurate(this.getFrom(), (Entity)this.getBukkitPlayer()) || LocationHelper.isOnGroundAccurate(this.getCheckable().getMeta().getLocationValues().penaltyLocation, (Entity)this.getBukkitPlayer()) || LocationHelper.collidesAnyStepable(this.getFrom().getWorld(), (Entity)this.getBukkitPlayer(), this.getFrom(), 0.0) || LocationHelper.collidesAnyStepable(this.getFrom().getWorld(), (Entity)this.getBukkitPlayer(), this.getCheckable().getMeta().getLocationValues().penaltyLocation, 0.0) || LocationHelper.collides(this.getFrom().getWorld(), (Entity)this.getBukkitPlayer(), this.getFrom(), Material.SOUL_SAND));
        }
        synchronized (this.onGround) {
            return this.onGround;
        }
    }
    
    public final double getMotionSqared() {
        return this.getFrom().toVector().distance(this.getTo().toVector());
    }
    
    public final Vector getMotionVector() {
        if (this.motionVector.lengthSquared() <= 0.0) {
            this.motionVector = this.getTo().toVector().subtract(this.getFrom().toVector());
        }
        return this.motionVector;
    }
    
    public final MovingData getMoveFlagData() {
        synchronized (this.movingData) {
            return this.movingData;
        }
    }
    
    public final boolean isCancelled() {
        synchronized (this.movingData) {
            return this.movingData.isCancelled() || this.movingData.isPullSilent() || this.movingData.isBukkitCancelled();
        }
    }
    
    public final void setCancelled(final boolean cancel) {
        synchronized (this.movingData) {
            this.movingData.setCancelled(cancel);
        }
    }
    
    public final void renewEvent(final PlayerMoveEvent e) {
        this.to = e.getTo().clone();
        this.from = e.getFrom().clone();
        this.movingData.renew();
        this.motionVector.setX(0).setY(0).setZ(0);
        this.onGround = null;
        this.yMotion = -1.0;
        this.xzMotion = -1.0;
        this.xMotion = -1.0;
        this.zMotion = -1.0;
        this.direction = -1.0f;
        this.timeOfQuere = System.nanoTime();
    }
}
