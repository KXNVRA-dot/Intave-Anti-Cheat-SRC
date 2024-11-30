// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.movement;

import net.minecraft.server.v1_8_R3.MathHelper;
import de.jpx3.intave.check.combat.Heuristics;
import java.math.RoundingMode;
import java.math.BigDecimal;
import de.jpx3.intave.antipiracy.IIUA;
import org.bukkit.entity.Player;
import de.jpx3.intave.util.event.CheckableMoveEvent;
import java.util.concurrent.ConcurrentHashMap;
import de.jpx3.intave.util.enums.CheatCategory;
import de.jpx3.intave.IntavePlugin;
import java.util.UUID;
import java.util.Map;
import de.jpx3.intave.util.objectable.IntaveCheck;

public class ReplicatedMoveCheck extends IntaveCheck
{
    private Map<UUID, Double> vl;
    private Map<UUID, Integer> lastD;
    private Map<UUID, Boolean> fastMath;
    private Map<UUID, Boolean> fMath;
    private Map<UUID, Boolean> blockList;
    private final IntavePlugin plugin;
    
    public ReplicatedMoveCheck(final IntavePlugin plugin) {
        super("ReplicatedMove", CheatCategory.MOVING);
        this.vl = new ConcurrentHashMap<UUID, Double>();
        this.lastD = new ConcurrentHashMap<UUID, Integer>();
        this.fastMath = new ConcurrentHashMap<UUID, Boolean>();
        this.fMath = new ConcurrentHashMap<UUID, Boolean>();
        this.blockList = new ConcurrentHashMap<UUID, Boolean>();
        this.plugin = plugin;
    }
    
    @Override
    public void onCheckableMove(final CheckableMoveEvent e) {
        this.calc(e.getCheckable().getMeta().getSyncedValues().lastXMovement, e.getCheckable().getMeta().getSyncedValues().lastZMovement, e.getXDirectedMotion(), e.getZDirectedMotion(), e.getBukkitPlayer(), e.getTo().getYaw(), e.getCheckable().getMeta().getTimedValues().lastSprintToggle < 100L && !e.getBukkitPlayer().isSprinting());
    }
    
    private void calc(final double lmotionX, final double lmotionZ, final double rmotionX, final double rmotionZ, final Player p, final float yaw, final boolean lastTickSprint) {
        final UUID u = p.getUniqueId();
        final boolean sneak = p.isSneaking();
        final boolean blocking = this.blockList.containsKey(p.getUniqueId()) ? this.blockList.get(p.getUniqueId()) : p.isBlocking();
        boolean flag = true;
        final boolean openInv = IntavePlugin.getStaticReference().catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().hasAnOpenInventory;
        final double mx = rmotionX - lmotionX;
        final double mz = rmotionZ - lmotionZ;
        float motionYaw = (float)(Math.atan2(mz, mx) * 180.0 / 3.141592653589793) - 90.0f;
        int direction = 6;
        for (motionYaw -= yaw; motionYaw > 360.0f; motionYaw -= 360.0f) {}
        while (motionYaw < 0.0f) {
            motionYaw += 360.0f;
        }
        motionYaw /= 45.0f;
        float moveS = 0.0f;
        float moveF = 0.0f;
        String key = "Nix";
        if (Math.abs(Math.abs(mx) + Math.abs(mz)) > 1.0E-12) {
            direction = (int)new BigDecimal(motionYaw).setScale(1, RoundingMode.HALF_UP).doubleValue();
            if (direction == 1) {
                moveF = 1.0f;
                moveS = -1.0f;
                key = "W + D";
            }
            else if (direction == 2) {
                moveS = -1.0f;
                key = "D";
            }
            else if (direction == 3) {
                moveF = -1.0f;
                moveS = -1.0f;
                key = "S + D";
            }
            else if (direction == 4) {
                moveF = -1.0f;
                key = "S";
            }
            else if (direction == 5) {
                moveF = -1.0f;
                moveS = 1.0f;
                key = "S + A";
            }
            else if (direction == 6) {
                moveS = 1.0f;
                key = "A";
            }
            else if (direction == 7) {
                moveF = 1.0f;
                moveS = 1.0f;
                key = "W + A";
            }
            else if (direction == 8) {
                moveF = 1.0f;
                key = "W";
            }
            else if (direction == 0) {
                moveF = 1.0f;
                key = "W";
            }
        }
        moveF *= 0.98f;
        moveS *= 0.98f;
        if (openInv) {
            moveF = 0.0f;
            moveS = 0.0f;
            key = "NIX";
        }
        double diff = -1337.0;
        double closestdiff = 1337.0;
        int loops = 0;
        for (int fastLoop = 2; fastLoop > 0; --fastLoop) {
            this.fastMath.put(p.getUniqueId(), (fastLoop == 2 && this.fMath.getOrDefault(p.getUniqueId(), false)) || (fastLoop != 2 && !this.fMath.getOrDefault(p.getUniqueId(), false)));
            for (int blockLoop = 2; blockLoop > 0; --blockLoop) {
                if (!blocking) {
                    blockLoop = 0;
                }
                final boolean blocking2 = blockLoop == 1 == !blocking;
                ++loops;
                float moveStrafing = moveS;
                float moveForward = moveF;
                if (sneak) {
                    if (lastTickSprint) {
                        return;
                    }
                    moveForward *= 0.3f;
                    moveStrafing *= 0.3f;
                }
                if (openInv) {
                    if (lastTickSprint) {
                        return;
                    }
                    if (sneak) {
                        return;
                    }
                }
                if (blocking2) {
                    moveForward *= 0.2f;
                    moveStrafing *= 0.2f;
                }
                float jumpMovementFactor = 0.02f;
                if (lastTickSprint) {
                    jumpMovementFactor += (float)0.006;
                }
                double motionX = lmotionX;
                double motionZ = lmotionZ;
                float var14 = moveStrafing * moveStrafing + moveForward * moveForward;
                if (var14 >= 1.0E-4f) {
                    var14 = Heuristics.sqrt_float(var14);
                    if (var14 < 1.0f) {
                        var14 = 1.0f;
                    }
                    var14 = jumpMovementFactor / var14;
                    moveStrafing *= var14;
                    moveForward *= var14;
                    final float var15 = MathHelper.sin(yaw * 3.1415927f / 180.0f);
                    final float var16 = MathHelper.cos(yaw * 3.1415927f / 180.0f);
                    motionX += moveStrafing * var16 - moveForward * var15;
                    motionZ += moveForward * var16 + moveStrafing * var15;
                }
                final double diffX = rmotionX - motionX;
                final double diffZ = rmotionZ - motionZ;
                diff = Math.hypot(diffX, diffZ);
                diff = new BigDecimal(diff).setScale(14, RoundingMode.HALF_UP).doubleValue();
                if (diff < 1.0E-12) {
                    this.lastD.put(p.getUniqueId(), direction);
                    flag = false;
                    this.fMath.put(p.getUniqueId(), this.fastMath.getOrDefault(p.getUniqueId(), false));
                    if (blockLoop == 1) {
                        this.blockList.put(u, false);
                    }
                    if (this.vl.get(p.getUniqueId()) - 0.25 > 0.0) {
                        this.vl.put(p.getUniqueId(), this.vl.getOrDefault(p.getUniqueId(), 0.0) - 0.25);
                    }
                }
                if (diff < closestdiff) {
                    closestdiff = diff;
                }
            }
        }
        if (flag) {
            if (p.isOp()) {
                p.sendMessage("§4 diff " + new BigDecimal(closestdiff).setScale(14, RoundingMode.HALF_UP).toPlainString() + " key: " + key + " loops: " + loops);
            }
            this.vl.put(p.getUniqueId(), this.vl.getOrDefault(p.getUniqueId(), 0.0) + 1.0);
        }
    }
}
