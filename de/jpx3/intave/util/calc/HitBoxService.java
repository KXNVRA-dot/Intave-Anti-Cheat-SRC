// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.calc;

import java.util.concurrent.ConcurrentHashMap;
import de.jpx3.intave.api.external.linked.exceptions.IntaveInternalException;
import de.jpx3.intave.api.internal.reflections.Reflections;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import de.jpx3.intave.util.objectable.BukkitEntityHitbox;
import org.bukkit.entity.EntityType;
import java.util.Map;

public final class HitBoxService
{
    private static final Map<EntityType, BukkitEntityHitbox> cache;
    private static final BukkitEntityHitbox cachedHitbox;
    private static Class<?> entityAgeableClazz;
    
    public static BukkitEntityHitbox getBoundingBox(final Entity e) {
        if (!(e instanceof Player) && couldBeAChild(e)) {
            return loadBoundingBox(e);
        }
        if (!HitBoxService.cache.containsKey(e.getType())) {
            HitBoxService.cache.put(e.getType(), loadBoundingBox(e));
        }
        return HitBoxService.cache.get(e.getType());
    }
    
    public static BukkitEntityHitbox loadBoundingBox(final Entity e) {
        try {
            final Object craftEntityHandle = Reflections.getCraftBukkitClass("entity.CraftEntity").getMethod("getHandle", (Class<?>[])new Class[0]).invoke(e, new Object[0]);
            final Object nmsHitbox = Reflections.getNmsClass("Entity").getMethod("getBoundingBox", (Class<?>[])new Class[0]).invoke(craftEntityHandle, new Object[0]);
            if (Reflections.useAlternateMethodNames()) {
                return HitBoxService.cachedHitbox.clone().construct((double)nmsHitbox.getClass().getDeclaredField("minX").get(nmsHitbox), (double)nmsHitbox.getClass().getDeclaredField("minY").get(nmsHitbox), (double)nmsHitbox.getClass().getDeclaredField("minZ").get(nmsHitbox), (double)nmsHitbox.getClass().getDeclaredField("maxX").get(nmsHitbox), (double)nmsHitbox.getClass().getDeclaredField("maxY").get(nmsHitbox), (double)nmsHitbox.getClass().getDeclaredField("maxZ").get(nmsHitbox));
            }
            return HitBoxService.cachedHitbox.clone().construct((double)nmsHitbox.getClass().getDeclaredField("a").get(nmsHitbox), (double)nmsHitbox.getClass().getDeclaredField("b").get(nmsHitbox), (double)nmsHitbox.getClass().getDeclaredField("c").get(nmsHitbox), (double)nmsHitbox.getClass().getDeclaredField("d").get(nmsHitbox), (double)nmsHitbox.getClass().getDeclaredField("e").get(nmsHitbox), (double)nmsHitbox.getClass().getDeclaredField("f").get(nmsHitbox));
        }
        catch (Exception ex) {
            throw new IntaveInternalException("Could not load hitbox for entity(type:" + e.getType().name() + ",uuid:" + e.getUniqueId().toString() + ")", ex);
        }
    }
    
    private static boolean couldBeAChild(final Entity e) {
        if (HitBoxService.entityAgeableClazz == null) {
            HitBoxService.entityAgeableClazz = Reflections.getNmsClass("EntityAgeable");
        }
        return HitBoxService.entityAgeableClazz.isInstance(e);
    }
    
    static {
        cache = new ConcurrentHashMap<EntityType, BukkitEntityHitbox>();
        cachedHitbox = new BukkitEntityHitbox(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        HitBoxService.entityAgeableClazz = null;
    }
}
