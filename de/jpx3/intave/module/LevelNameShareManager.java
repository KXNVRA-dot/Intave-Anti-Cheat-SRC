// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.module;

import java.nio.ByteBuffer;
import java.lang.reflect.InvocationTargetException;
import de.jpx3.intave.api.internal.reflections.Reflections;
import org.bukkit.entity.Player;
import de.jpx3.intave.IntavePlugin;

public class LevelNameShareManager
{
    private final IntavePlugin plugin;
    
    public LevelNameShareManager(final IntavePlugin plugin) {
        this.plugin = plugin;
    }
    
    private void sendFakeExperience(final Player player, final float experience, final int experienceTotal, final int experienceLevel) {
        Object packetPlayOutExperience = null;
        try {
            packetPlayOutExperience = Reflections.getNmsClass("PacketPlayOutExperience").getConstructor(Float.TYPE, Integer.TYPE, Integer.TYPE).newInstance(experience, experienceTotal, experienceLevel);
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex2) {
            final ReflectiveOperationException ex;
            final ReflectiveOperationException e = ex;
            e.printStackTrace();
        }
        if (packetPlayOutExperience == null) {
            return;
        }
        Reflections.sendPacket(player, packetPlayOutExperience);
    }
    
    private int getLongFrom(final String string) {
        return ByteBuffer.wrap(string.getBytes()).getInt() >> 10;
    }
}
