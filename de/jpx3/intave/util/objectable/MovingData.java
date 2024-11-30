// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.objectable;

public final class MovingData
{
    private boolean cancelled;
    private boolean bukkitCancelled;
    private boolean pullSilent;
    private boolean shouldCheck;
    private boolean overrideFMB;
    private double pullYMotion;
    
    public MovingData() {
        this.cancelled = false;
        this.bukkitCancelled = false;
        this.pullSilent = false;
        this.shouldCheck = true;
        this.overrideFMB = false;
        this.pullYMotion = -0.25;
    }
    
    public boolean shouldOverrideFMB() {
        return this.overrideFMB;
    }
    
    public final boolean isCancelled() {
        return this.cancelled;
    }
    
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    public final boolean isPullSilent() {
        return this.pullSilent;
    }
    
    public void setPullSilent(final boolean pullSilent) {
        this.pullSilent = pullSilent;
    }
    
    public final double getPullYMotion() {
        return this.pullYMotion;
    }
    
    public void setPullYMotion(final double pullYMotion) {
        this.pullYMotion = pullYMotion;
    }
    
    public void setPullYMotionPositive(final double pullYMotion) {
        this.pullYMotion = pullYMotion;
        this.overrideFMB = true;
    }
    
    public boolean shouldCheck() {
        return this.shouldCheck;
    }
    
    public void setShouldCheck(final boolean shouldCheck) {
        this.shouldCheck = shouldCheck;
    }
    
    public final boolean isBukkitCancelled() {
        return this.bukkitCancelled;
    }
    
    public void setBukkitCancelled(final boolean bukkitCancelled) {
        this.bukkitCancelled = bukkitCancelled;
    }
    
    public void renew() {
        this.overrideFMB = false;
        this.cancelled = false;
        this.pullSilent = false;
        this.bukkitCancelled = false;
        this.shouldCheck = true;
        this.pullYMotion = -0.25;
    }
}
