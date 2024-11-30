// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.connection;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import de.jpx3.intave.util.calc.MathHelper;
import de.jpx3.intave.util.event.CheckableMoveEvent;
import de.jpx3.intave.util.enums.CheatCategory;
import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.util.objectable.IntaveCheck;

public final class IllegalRotationCheck extends IntaveCheck
{
    private final IntavePlugin plugin;
    private static final float[] SIN_TABLE_FAST;
    private static boolean fastMath;
    private static final float[] SIN_TABLE;
    
    public IllegalRotationCheck(final IntavePlugin plugin) {
        super("IllegalRotation", CheatCategory.MOVING);
        this.plugin = plugin;
    }
    
    @Override
    public void onCheckableMove(final CheckableMoveEvent e) {
        final Player p = e.getBukkitPlayer();
        if (MathHelper.amount(e.getTo().getPitch()) > 90.0) {
            this.plugin.getRetributionManager().markPlayer(p, 1, "IllegalRotation", CheatCategory.MOVING, "moved pitch to " + MathHelper.roundFromDouble(e.getTo().getPitch(), 5));
            final Location fixxedLocation = e.getTo();
            fixxedLocation.setYaw(0.0f);
            fixxedLocation.setPitch(0.0f);
            e.setTo(fixxedLocation);
        }
    }
    
    private static float sin(final float p_76126_0_) {
        return IllegalRotationCheck.fastMath ? IllegalRotationCheck.SIN_TABLE_FAST[(int)(p_76126_0_ * 651.8986f) & 0xFFF] : IllegalRotationCheck.SIN_TABLE[(int)(p_76126_0_ * 10430.378f) & 0xFFFF];
    }
    
    private static float cos(final float p_76134_0_) {
        return IllegalRotationCheck.fastMath ? IllegalRotationCheck.SIN_TABLE_FAST[(int)((p_76134_0_ + 1.5707964f) * 651.8986f) & 0xFFF] : IllegalRotationCheck.SIN_TABLE[(int)(p_76134_0_ * 10430.378f + 16384.0f) & 0xFFFF];
    }
    
    private static float sqrt_float(final float p_76129_0_) {
        return (float)Math.sqrt(p_76129_0_);
    }
    
    private static float sqrt_double(final double p_76133_0_) {
        return (float)Math.sqrt(p_76133_0_);
    }
    
    static {
        SIN_TABLE_FAST = new float[4096];
        IllegalRotationCheck.fastMath = false;
        SIN_TABLE = new float[65536];
        for (int i = 0; i < 65536; ++i) {
            IllegalRotationCheck.SIN_TABLE[i] = (float)Math.sin(i * 3.141592653589793 * 2.0 / 65536.0);
        }
        for (int i = 0; i < 4096; ++i) {
            IllegalRotationCheck.SIN_TABLE_FAST[i] = (float)Math.sin((i + 0.5f) / 4096.0f * 6.2831855f);
        }
        for (int i = 0; i < 360; i += 90) {
            IllegalRotationCheck.SIN_TABLE_FAST[(int)(i * 11.377778f) & 0xFFF] = (float)Math.sin(i * 0.017453292f);
        }
    }
}
