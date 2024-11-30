// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.calc;

import org.bukkit.Location;

public final class YawUtil
{
    public static double angleTo180Deg(double degrees) {
        degrees %= 360.0;
        if (degrees >= 180.0) {
            degrees -= 360.0;
        }
        if (degrees < -180.0) {
            degrees += 360.0;
        }
        return degrees;
    }
    
    public static float getYawFrom(final Location from, final Location to) {
        final double dx = to.getX() - from.getX();
        final double dz = to.getZ() - from.getZ();
        return -((dx != 0.0) ? (((dx < 0.0) ? 4.712389f : 1.5707964f) - (float)Math.atan(dz / dx)) : 3.1415927f) * 180.0f / 3.1415927f;
    }
    
    public static float getPitchFrom(Location loc, final Location lookat) {
        loc = loc.clone();
        final double dx = lookat.getX() - loc.getX();
        final double dy = lookat.getY() - loc.getY();
        final double dz = lookat.getZ() - loc.getZ();
        final double dxz = Math.sqrt(Math.pow(dx, 2.0) + Math.pow(dz, 2.0));
        loc.setPitch((float)(-Math.atan(dy / dxz)));
        loc.setPitch(loc.getPitch() * 180.0f / 3.1415927f);
        return loc.getPitch();
    }
    
    public static float yawDiff(float fromYaw, float toYaw) {
        if (fromYaw <= -360.0f) {
            fromYaw = -(-fromYaw % 360.0f);
        }
        else if (fromYaw >= 360.0f) {
            fromYaw %= 360.0f;
        }
        if (toYaw <= -360.0f) {
            toYaw = -(-toYaw % 360.0f);
        }
        else if (toYaw >= 360.0f) {
            toYaw %= 360.0f;
        }
        float yawDiff = toYaw - fromYaw;
        if (yawDiff < -180.0f) {
            yawDiff += 360.0f;
        }
        else if (yawDiff > 180.0f) {
            yawDiff -= 360.0f;
        }
        return yawDiff;
    }
    
    public static float pitchDiff(float fromPitch, float toPitch) {
        if (fromPitch <= -180.0f) {
            fromPitch = -(-fromPitch % 180.0f);
        }
        else if (fromPitch >= 180.0f) {
            fromPitch %= 180.0f;
        }
        if (toPitch <= -180.0f) {
            toPitch = -(-toPitch % 180.0f);
        }
        else if (toPitch >= 180.0f) {
            toPitch %= 180.0f;
        }
        float pitchDiff = toPitch - fromPitch;
        if (pitchDiff < -90.0f) {
            pitchDiff += 180.0f;
        }
        else if (pitchDiff > 90.0f) {
            pitchDiff -= 180.0f;
        }
        return pitchDiff;
    }
}
