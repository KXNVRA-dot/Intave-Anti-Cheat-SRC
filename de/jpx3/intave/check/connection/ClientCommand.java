// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.connection;

import io.netty.buffer.ByteBuf;
import de.jpx3.intave.api.internal.reflections.Reflections;
import io.netty.buffer.Unpooled;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;
import org.bukkit.event.EventHandler;
import java.lang.reflect.InvocationTargetException;
import java.io.IOException;
import de.jpx3.intave.util.IntaveExceptionHandler;
import java.util.stream.Collector;
import java.util.function.Function;
import java.util.stream.Collectors;
import de.jpx3.intave.util.enums.LabyModFeature;
import java.util.Map;
import org.bukkit.event.player.PlayerJoinEvent;
import com.comphenix.protocol.events.PacketListener;
import java.util.stream.Stream;
import org.bukkit.World;
import java.util.Iterator;
import de.jpx3.intave.util.calc.PlayerUtils;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import java.util.List;
import org.bukkit.entity.LivingEntity;
import com.comphenix.protocol.utility.MinecraftVersion;
import de.jpx3.intave.api.internal.ProtocolLibAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.plugin.Plugin;
import de.jpx3.intave.util.enums.CheatCategory;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.event.Listener;
import de.jpx3.intave.util.objectable.IntaveCheck;

public class ClientCommand extends IntaveCheck implements Listener
{
    private final IntavePlugin plugin;
    
    public ClientCommand(final IntavePlugin plugin) {
        super("ClientCommand", CheatCategory.NETWORK);
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this.plugin);
        final IntavePlugin plugin2 = this.plugin;
        ProtocolLibrary.getProtocolManager().addPacketListener((PacketListener)new PacketAdapter(this.plugin, new PacketType[] { PacketType.Play.Server.ENTITY_METADATA }) {
            public void onPacketSending(final PacketEvent event) {
                try {
                    if (event.getPacket().getIntegers().getValues().isEmpty()) {
                        return;
                    }
                    PacketContainer packet = event.getPacket();
                    final Entity e = this.getBukkitEntity(event.getPlayer(), event.getPacket().getIntegers().getValues().get(0));
                    if (e == null) {
                        return;
                    }
                    if (!plugin2.getConfig().getBoolean(ClientCommand.this.getConfigPath() + ".spoofdeath")) {
                        return;
                    }
                    if (ProtocolLibAdapter.getProtocolManager().getMinecraftVersion().isAtLeast(MinecraftVersion.COMBAT_UPDATE)) {
                        return;
                    }
                    if (e instanceof LivingEntity && packet.getWatchableCollectionModifier().read(0) != null && e.getUniqueId() != event.getPlayer().getUniqueId()) {
                        packet = packet.deepClone();
                        event.setPacket(packet);
                        if (event.getPacket().getType() == PacketType.Play.Server.ENTITY_METADATA) {
                            final WrappedDataWatcher watcher = new WrappedDataWatcher((List)packet.getWatchableCollectionModifier().read(0));
                            this.processDataWatcher(watcher);
                            packet.getWatchableCollectionModifier().write(0, (Object)watcher.getWatchableObjects());
                        }
                    }
                }
                catch (Exception ex) {}
            }
            
            private void processDataWatcher(final WrappedDataWatcher watcher) {
                if (watcher != null && watcher.getObject(6) != null && watcher.getFloat(6) != 0.0f) {
                    watcher.setObject(6, (Object)Float.NaN);
                }
            }
            
            private Entity getBukkitEntity(final Player f, final int entityid) {
                if (f == null) {
                    return null;
                }
                if (entityid == 0) {
                    return null;
                }
                for (final Entity e2 : PlayerUtils.getNearbyEntities(f, f.getLocation(), 8.0)) {
                    if (e2.getEntityId() == entityid) {
                        return e2;
                    }
                }
                for (final Entity e2 : f.getWorld().getLivingEntities()) {
                    if (e2.getEntityId() == entityid) {
                        return e2;
                    }
                }
                return (Entity)plugin2.getServer().getWorlds().stream().flatMap(w -> w.getLivingEntities().stream()).filter(e -> e.getEntityId() == entityid).findFirst().orElse(null);
            }
        });
    }
    
    @EventHandler
    public void on(final PlayerJoinEvent e) {
        if (!this.plugin.getConfig().getBoolean(this.getConfigPath() + ".client.labymod.process")) {
            return;
        }
        final Player p = e.getPlayer();
        final Map<LabyModFeature, Boolean> list = (Map<LabyModFeature, Boolean>)this.plugin.getConfig().getConfigurationSection(this.getConfigPath() + ".client.labymod.modules").getKeys(false).stream().collect(Collectors.toMap((Function<? super Object, ?>)LabyModFeature::valueOf, labyModFeature -> this.plugin.getConfig().getBoolean(this.getConfigPath() + ".client.labymod.modules." + labyModFeature), (a, b) -> b));
        if (!list.isEmpty()) {
            try {
                this.setLabyModFeature(p, list);
            }
            catch (IOException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException ex2) {
                final Exception ex;
                final Exception e2 = ex;
                IntaveExceptionHandler.printAndSaveToFile("[FATAL] Couldn't create custom payload packet.", e2);
            }
        }
    }
    
    private void setLabyModFeature(final Player p, final Map<LabyModFeature, Boolean> list) throws IOException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        final Map<String, Boolean> nList = list.keySet().stream().collect(Collectors.toMap((Function<? super Object, ? extends String>)Enum::name, (Function<? super Object, ? extends Boolean>)list::get, (a, b) -> b));
        final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(nList);
        final ByteBuf a = Unpooled.copiedBuffer(byteOut.toByteArray());
        final Object packetDataSerializer = Reflections.getNmsClass("PacketDataSerializer").getConstructor(ByteBuf.class).newInstance(a);
        final Object packetPlayOutCPL = Reflections.getNmsClass("PacketPlayOutCustomPayload").getConstructor(String.class, packetDataSerializer.getClass()).newInstance("LABYMOD", packetDataSerializer);
        if (list.containsKey(LabyModFeature.DAMAGEINDICATOR)) {
            Reflections.sendPacket(p, Reflections.getNmsClass("PacketPlayOutCustomPayload").getConstructor(String.class, packetDataSerializer.getClass()).newInstance("DAMAGEINDICATOR", Reflections.getNmsClass("PacketDataSerializer").getConstructor(ByteBuf.class).newInstance(Unpooled.copyBoolean((boolean)list.get(LabyModFeature.DAMAGEINDICATOR)))));
        }
        Reflections.sendPacket(p, packetPlayOutCPL);
    }
}
