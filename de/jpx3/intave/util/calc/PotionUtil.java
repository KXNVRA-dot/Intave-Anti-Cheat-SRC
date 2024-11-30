// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.calc;

import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.InvocationTargetException;
import de.jpx3.intave.util.IntaveExceptionHandler;
import java.util.stream.IntStream;
import de.jpx3.intave.api.internal.reflections.Reflections;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.function.Predicate;
import java.util.Objects;
import java.util.Arrays;
import org.bukkit.inventory.ItemStack;
import java.util.List;
import java.util.Iterator;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.Player;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public final class PotionUtil
{
    private static final AtomicReference<Double> cachedAtomicReference;
    private static final Map<String, Double> emptyMap;
    
    public static double getPotionEffectAmplifier(final Player player, final PotionEffectType type) {
        double max = 0.0;
        for (final PotionEffect potionEffect : player.getActivePotionEffects()) {
            if (type.equals((Object)potionEffect.getType())) {
                max = Math.max(max, potionEffect.getAmplifier());
            }
        }
        return Math.max(max, type.equals((Object)PotionEffectType.SPEED) ? getItemEffectAmplifier(player, "generic.movementSpeed") : 0.0);
    }
    
    public static double getItemEffectAmplifier(final Player player, final String itemEffect) {
        final List<ItemStack> f = Arrays.stream(player.getInventory().getArmorContents()).filter(Objects::isNull).collect((Collector<? super ItemStack, ?, List<ItemStack>>)Collectors.toList());
        if (player.getItemInHand() != null) {
            f.add(player.getItemInHand());
        }
        PotionUtil.cachedAtomicReference.set(0.0);
        f.forEach(itemStack -> getFromStack(itemStack).forEach((s1, aDouble) -> PotionUtil.cachedAtomicReference.set(Math.max(PotionUtil.cachedAtomicReference.get(), itemEffect.equalsIgnoreCase(s1) ? ((double)aDouble) : ((double)PotionUtil.cachedAtomicReference.get())))));
        return PotionUtil.cachedAtomicReference.get();
    }
    
    private static Map<String, Double> getFromStack(final ItemStack item) {
        if (!PotionUtil.emptyMap.isEmpty()) {
            PotionUtil.emptyMap.clear();
        }
        if (item == null) {
            return PotionUtil.emptyMap;
        }
        try {
            final Object itemNMS = Reflections.getCraftBukkitClass("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
            Objects.requireNonNull(itemNMS);
            final Object itemNMSTag = itemNMS.getClass().getMethod("getTag", (Class<?>[])new Class[0]).invoke(itemNMS, new Object[0]);
            Objects.requireNonNull(itemNMSTag);
            final Object modifiers = itemNMSTag.getClass().getMethod("getList", String.class, Integer.TYPE).invoke(itemNMSTag, "AttributeModifiers", 10);
            Objects.requireNonNull(modifiers);
            final Map<String, Double> cacheMap = PotionUtil.emptyMap;
            final int bound = (int)modifiers.getClass().getMethod("size", (Class<?>[])new Class[0]).invoke(modifiers, new Object[0]);
            final Object obj;
            Object q;
            final Map<String, Double> map;
            IntStream.range(0, bound).forEachOrdered(i -> {
                try {
                    q = obj.getClass().getMethod("get", Integer.TYPE).invoke(obj, i);
                    map.put(String.valueOf(q.getClass().getMethod("getString", String.class).invoke(q, "AttributeName")), (Double)q.getClass().getMethod("getDouble", String.class).invoke(q, "Amount"));
                }
                catch (Exception ex2) {}
                return;
            });
            return cacheMap;
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex3) {
            final Exception ex;
            final Exception e = ex;
            IntaveExceptionHandler.printAndSaveToFile("[FATAL] Intave had problems with item attributes", e);
            return PotionUtil.emptyMap;
        }
    }
    
    static {
        cachedAtomicReference = new AtomicReference<Double>(0.0);
        emptyMap = new ConcurrentHashMap<String, Double>();
    }
}
