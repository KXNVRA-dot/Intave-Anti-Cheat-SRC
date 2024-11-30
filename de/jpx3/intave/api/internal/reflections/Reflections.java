// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.api.internal.reflections;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

public final class Reflections
{
    private static final String version;
    
    public static String getVersion() {
        return Reflections.version;
    }
    
    public static SaveField getField(final String name, final Class<?> clazz) {
        try {
            final Field f = clazz.getField(name);
            return new SaveField(f);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static SaveField getDeclaredField(final Class<?> clazz) {
        try {
            final Field f = clazz.getDeclaredField("aK");
            return new SaveField(f);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static SaveMethod getMethode(final String name, final Class<?> clazz, final Class<?>... parameterClasses) {
        try {
            final Method m = clazz.getMethod(name, parameterClasses);
            return new SaveMethod(m);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static SaveMethod getDeclaredMethode(final String name, final Class<?> clazz, final Class<?>... parameterClasses) {
        try {
            final Method m = clazz.getDeclaredMethod(name, parameterClasses);
            m.setAccessible(true);
            return new SaveMethod(m);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static Class<?> getClass(final String name, final boolean asArray) {
        try {
            return asArray ? Array.newInstance(Class.forName(name), 0).getClass() : Class.forName(name);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static String getNmsPrefix() {
        return "net.minecraft.server." + Reflections.version + ".";
    }
    
    public static String getCraftBukkitPrefix() {
        return "org.bukkit.craftbukkit." + Reflections.version + ".";
    }
    
    public static Class<?> getNmsClass(final String name) {
        return getClass("net.minecraft.server." + Reflections.version + "." + name, false);
    }
    
    public static Class<?> getNmsClassAsArray(final String name) {
        return getClass("net.minecraft.server." + Reflections.version + "." + name, true);
    }
    
    public static Class<?> getCraftBukkitClass(final String name) {
        return getClass("org.bukkit.craftbukkit." + Reflections.version + "." + name, false);
    }
    
    public static Class<?> getCraftBukkitClassAsArray(final String name) {
        return getClass("org.bukkit.craftbukkit." + Reflections.version + "." + name, true);
    }
    
    public static boolean useAlternateMethodNames() {
        return Reflections.version.equalsIgnoreCase("v1_13_R2");
    }
    
    private static Object getEntityPlayer(final Player p) {
        try {
            return getCraftBukkitClass("entity.CraftPlayer").getMethod("getHandle", (Class<?>[])new Class[0]).invoke(p, new Object[0]);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Object serializeString(final String s) {
        try {
            final Class<?> chatSerelizer = getCraftBukkitClass("util.CraftChatMessage");
            final Method mSerelize = chatSerelizer.getMethod("fromString", String.class);
            return ((Object[])mSerelize.invoke(null, s))[0];
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static String fromIChatBaseComponent(final Object component) {
        try {
            final Class<?> chatSerelizer = getCraftBukkitClass("util.CraftChatMessage");
            final Method mSerelize = chatSerelizer.getMethod("fromComponent", getNmsClass("IChatBaseComponent"));
            return (String)mSerelize.invoke(null, component);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Object getEnumGamemode(final Player p) {
        try {
            final Field fInteractManager = getNmsClass("EntityPlayer").getField("playerInteractManager");
            fInteractManager.setAccessible(true);
            final Object oInteractManager = fInteractManager.get(getEntityPlayer(p));
            final Field enumGamemode = getNmsClass("PlayerInteractManager").getDeclaredField("gamemode");
            enumGamemode.setAccessible(true);
            return enumGamemode.get(oInteractManager);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static int getPing(final Player p) {
        try {
            final Field ping = getNmsClass("EntityPlayer").getDeclaredField("ping");
            ping.setAccessible(true);
            return (int)ping.get(getEntityPlayer(p));
        }
        catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    public static void sendPacket(final Player p, final Object packet) {
        try {
            final Object nmsPlayer = getEntityPlayer(p);
            final Field fieldCon = (nmsPlayer != null) ? nmsPlayer.getClass().getDeclaredField("playerConnection") : null;
            final Object nmsCon = (fieldCon != null) ? fieldCon.get(nmsPlayer) : null;
            assert nmsCon != null;
            final Method sendPacket = nmsCon.getClass().getMethod("sendPacket", getNmsClass("Packet"));
            sendPacket.invoke(nmsCon, packet);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static int getID(final Object entity) {
        try {
            final Object entityEntity = getCraftBukkitClass("entity.CraftEntity").getMethod("getHandle", (Class<?>[])new Class[0]).invoke(entity, new Object[0]);
            return (int)getNmsClass("Entity").getMethod("getId", (Class<?>[])new Class[0]).invoke(entityEntity, new Object[0]);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }
    
    public static Object getEntity(final Entity entity) {
        try {
            return getCraftBukkitClass("entity.CraftEntity").getMethod("getHandle", (Class<?>[])new Class[0]).invoke(entity, new Object[0]);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Object getPlayerHandle(final Player player) {
        try {
            return player.getClass().getMethod("getHandle", (Class<?>[])new Class[0]).invoke(player, new Object[0]);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Object getWorldServer(final World w) {
        try {
            return getCraftBukkitClass("CraftWorld").getMethod("getHandle", (Class<?>[])new Class[0]).invoke(w, new Object[0]);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Object getObjectNMSItemStack(final ItemStack item) {
        try {
            return getCraftBukkitClass("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static String[] fromIChatBaseComponent(final Object[] baseComponentArray) {
        final String[] array = new String[baseComponentArray.length];
        for (int i = 0; i < array.length; ++i) {
            array[i] = fromIChatBaseComponent(baseComponentArray[i]);
        }
        return array;
    }
    
    public static Object[] serializeString(final String[] strings) {
        final Object[] array = (Object[])Array.newInstance(getNmsClass("IChatBaseComponent"), strings.length);
        for (int i = 0; i < array.length; ++i) {
            array[i] = serializeString(strings[i]);
        }
        return array;
    }
    
    public static int floor(final double d) {
        try {
            return (int)getNmsClass("MathHelper").getMethod("floor", Double.TYPE).invoke(null, d);
        }
        catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    static {
        final String packageName = Bukkit.getServer().getClass().getPackage().getName();
        version = packageName.substring(packageName.lastIndexOf(".") + 1);
    }
}
