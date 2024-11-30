// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.objectable;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class BukkitEntityHitbox implements Cloneable
{
    private Vector min3dVec;
    private Vector max3dVec;
    private double a;
    private double b;
    private double c;
    private double d;
    private double e;
    private double f;
    
    public BukkitEntityHitbox(final double a, final double b, final double c, final double d, final double e, final double f) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
        this.min3dVec = new Vector(a, b, c);
        this.max3dVec = new Vector(d, e, f);
    }
    
    public final Vector getMin3DVector() {
        return this.min3dVec;
    }
    
    public final Vector getMax3dVector() {
        return this.max3dVec;
    }
    
    public final void setVectorsAccordingTo(final Location location) {
        throw new UnsupportedOperationException();
    }
    
    public final double getBoxHeight() {
        return this.e - this.b;
    }
    
    public final double getBoxWidth() {
        return (this.d - this.a + (this.f - this.c)) / 2.0;
    }
    
    public final Vector midPoint() {
        return this.getMax3dVector().clone().add(this.getMin3DVector()).multiply(0.5);
    }
    
    public final double getSquaredBoxSize() {
        return this.getBoxWidth() * this.getBoxHeight();
    }
    
    public BukkitEntityHitbox construct(final double a, final double b, final double c, final double d, final double e, final double f) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
        this.min3dVec = new Vector(a, b, c);
        this.max3dVec = new Vector(d, e, f);
        return this;
    }
    
    @Override
    public final String toString() {
        return "BukkitEntityHitbox{min3dVec=" + this.min3dVec + ", max3dVec=" + this.max3dVec + ", a=" + this.a + ", b=" + this.b + ", c=" + this.c + ", d=" + this.d + ", e=" + this.e + ", f=" + this.f + '}';
    }
    
    public final BukkitEntityHitbox clone() {
        try {
            return (BukkitEntityHitbox)super.clone();
        }
        catch (CloneNotSupportedException e1) {
            e1.printStackTrace();
            return new BukkitEntityHitbox(this.a, this.b, this.c, this.d, this.e, this.f);
        }
    }
}
