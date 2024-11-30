// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.api.internal.killaurabot;

import de.jpx3.intave.api.internal.reflections.Reflections;
import net.minecraft.server.v1_8_R3.MathHelper;
import java.util.ArrayList;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftChatMessage;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutUpdateHealth;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityVelocity;
import org.bukkit.util.Vector;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity;
import de.jpx3.intave.util.calc.LocationHelper;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import org.bukkit.Material;
import org.bukkit.Bukkit;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.PacketPlayOutBed;
import java.lang.reflect.Field;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityStatus;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.WorldSettings;
import java.util.Objects;
import java.util.List;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutAnimation;
import java.util.Iterator;
import org.bukkit.entity.Entity;
import java.util.UUID;
import com.comphenix.protocol.events.PacketContainer;
import java.lang.reflect.InvocationTargetException;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.mojang.authlib.properties.Property;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.entity.Player;
import net.minecraft.server.v1_8_R3.DataWatcher;
import com.mojang.authlib.GameProfile;
import org.bukkit.Location;

public final class KillAuraBot
{
    private long created;
    private int entityID;
    private Location location;
    private Location cached_location;
    private GameProfile gameprofile;
    private DataWatcher dataWatcher;
    private boolean grooved;
    private boolean isVisible;
    private boolean alive;
    private boolean sneaking;
    private boolean sprinting;
    private final String skin = "eyJ0aW1lc3RhbXAiOjE1MjI2MjQzMTMxMzcsInByb2ZpbGVJZCI6IjVlZTZkYjZkNjc1MTQwODE5Y2JmMjhlYjBmNmNjMDU1IiwicHJvZmlsZU5hbWUiOiJKcHgzIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xZTg1NGI4YmE0N2ZlMjY5OWI1NDlhZTkxOTk5ZmY1ZjM2YmU5Y2VjMTdhZTVhYWMwYzAxZjFjZWFmNzgxNjIifX19";
    private final String skin_sig = "GZMbkOL8AOIMMRBxNUz2bGtr2s3JHcM07YAJ1aQyjLWilhE0O9X2q/qtmPqlxvFaMUSirj/8ts6evTM0wR/u4DfjxKywVaFi9ewqHL1Qzj7/rf6ssIL2hHYME5A4h9wtmE0pvS0N+xxGCHhxZFFkGQllHhsQPnzl4ewbuiv7g4gVTzMYa0LhgN7K73bsLpJo17h4WKeflreJN44cKehzTRh/AT9d3UHLvRSDg6QRj9VKnE+k6nfRInsoiXiX5Yr2T8TjpW1i7q4j1LVqFAoZSAFtk4w8sDwQtXjGamL8yk6nIdcYzKzGroetMXFoLAwnrLd1bA40qOLHQW8Ntd2Jkrb9T/py5V0/l87Vk5Ad/UHGBxOCHeugtM9Ux7bnQLXzOPAyXwzbfAJGdajZIx8LeHyYmIczVc0xhAhFFanmCU94tyoTS+MiIRV80LQH9hOGr6YnWIQ32UfuieXktPLVVENNQWZlRazIVIGZe9wrHRNW1m2CthGsgQLgX3w6jzdQy9fvl0bk8XUESIxa9iLp0bChOrRx3fm0+djyk40xGWNEEJn5Nvnv3aMV7K7o9EeDFkePaV8FXD95AXqSsQQWjCsc0ZYDYuNQSJ2URKJaw6YiGzhL6uAC2Y3kONMUMxE7U65NwjsC2HrifSRKadkPI2mJjUc8DRXaF7JSu4HiGao=";
    
    public KillAuraBot(final Player player, final String name, final Location location, final boolean grooved, final String[] skin) {
        this.isVisible = true;
        this.alive = false;
        this.sneaking = false;
        this.sprinting = false;
        int entityID;
        for (entityID = (int)(Math.ceil(Math.random() * 1000.0) + 2000.0); this.worldHasEntityID(entityID); entityID = ThreadLocalRandom.current().nextInt(0, 320000)) {}
        this.grooved = grooved;
        this.created = System.currentTimeMillis();
        this.location = location.clone();
        this.entityID = entityID;
        this.gameprofile = this.createGameProfile(player, name);
        this.gameprofile.getProperties().put((Object)"textures", (Object)new Property("textures", skin[0], skin[1]));
    }
    
    private void changeNameTo(final Player observer, final String name) {
        final PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        final WrappedDataWatcher watcher = new WrappedDataWatcher();
        watcher.setObject(2, (Object)name);
        packet.getWatchableCollectionModifier().write(0, (Object)watcher.getWatchableObjects());
        packet.getIntegers().write(0, (Object)this.entityID);
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(observer, packet);
        }
        catch (InvocationTargetException ex) {}
    }
    
    private GameProfile createGameProfile(final Player player, final String placeholderName) {
        return new GameProfile(UUID.randomUUID(), placeholderName);
    }
    
    private boolean worldHasEntityID(final int entityID) {
        if (this.location == null) {
            return false;
        }
        if (this.location.getWorld() == null) {
            return false;
        }
        if (this.location.getWorld().getEntities().isEmpty()) {
            return false;
        }
        for (final Entity e : this.location.getWorld().getEntities()) {
            if (e.getEntityId() == entityID) {
                return true;
            }
        }
        return false;
    }
    
    public void animation(final Player p, final int animation) {
        if (!this.alive) {
            return;
        }
        final PacketPlayOutAnimation packet = new PacketPlayOutAnimation();
        this.setValue(packet, "a", this.entityID);
        this.setValue(packet, "b", (byte)animation);
        this.sendPacket((Packet<?>)packet, p);
    }
    
    public void sendPingUpdate(final Player p, final int ping) {
        final PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
        this.setValue(packet, "a", PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_LATENCY);
        final List<PacketPlayOutPlayerInfo.PlayerInfoData> players = (List<PacketPlayOutPlayerInfo.PlayerInfoData>)this.getValue(packet, "b");
        final List<PacketPlayOutPlayerInfo.PlayerInfoData> list = Objects.requireNonNull(players);
        final PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = packet;
        packetPlayOutPlayerInfo.getClass();
        list.add(new PacketPlayOutPlayerInfo.PlayerInfoData(packetPlayOutPlayerInfo, this.gameprofile, ping, WorldSettings.EnumGamemode.SURVIVAL, (IChatBaseComponent)new ChatComponentText(this.gameprofile.getName())));
        this.setValue(packet, "b", players);
        this.sendPacket((Packet<?>)packet, p);
    }
    
    public void status(final Player p, final int status) {
        if (!this.alive) {
            return;
        }
        final PacketPlayOutEntityStatus packet = new PacketPlayOutEntityStatus();
        this.setValue(packet, "a", this.entityID);
        this.setValue(packet, "b", (byte)status);
        this.sendPacket((Packet<?>)packet, p);
    }
    
    public void equip(final Player p, final int slot, final Object itemstack) {
        if (!this.alive) {
            return;
        }
        final PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment();
        this.setValue(packet, "a", this.entityID);
        this.setValue(packet, "b", slot);
        this.setValue(packet, "c", itemstack);
        this.sendPacket((Packet<?>)packet, p);
    }
    
    public void setSprinting(final Player p, final boolean sprinting) {
        if (this.alive && this.isVisible) {
            final int dataWatcherByte = 3;
            final boolean dfSnk = (this.dataWatcher.getByte(0) & 0x8) != 0x0;
            if (dfSnk != sprinting && this.sprinting != sprinting) {
                final byte b0 = this.dataWatcher.getByte(0);
                this.watch(0, sprinting ? ((byte)(b0 | 0x8)) : ((byte)(b0 & 0xFFFFFFF7)));
            }
            final PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(this.entityID, this.dataWatcher, true);
            this.sendPacket((Packet<?>)packet, p);
            this.sprinting = sprinting;
        }
    }
    
    public void setSneaking(final Player p, final boolean sneaking) {
        if (this.alive && this.isVisible) {
            final int dataWatcherByte = 1;
            final boolean dfSnk = (this.dataWatcher.getByte(0) & 1 << dataWatcherByte) != 0x0;
            if (dfSnk != sneaking && this.sneaking != sneaking) {
                final byte b0 = this.dataWatcher.getByte(0);
                this.watch(0, sneaking ? ((byte)(b0 | 1 << dataWatcherByte)) : ((byte)(b0 & ~(1 << dataWatcherByte))));
            }
            final PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(this.entityID, this.dataWatcher, true);
            this.sendPacket((Packet<?>)packet, p);
            this.sneaking = sneaking;
        }
    }
    
    private <T> void watch(final int i, final T t0) {
        this.j(i).a((Object)t0);
    }
    
    private DataWatcher.WatchableObject j(final int i) {
        try {
            final Field f = this.dataWatcher.getClass().getDeclaredField("dataValues");
            f.setAccessible(true);
            final Object object = f.get(this.dataWatcher);
            return (DataWatcher.WatchableObject)object.getClass().getMethod("get", Integer.TYPE).invoke(object, i);
        }
        catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean isSneaking() {
        return this.sneaking;
    }
    
    public boolean isSprinting() {
        return this.sprinting;
    }
    
    public void sleep(final Player p, final boolean state) {
        if (!this.alive) {
            return;
        }
        if (state) {
            final Location bedLocation = new Location(this.location.getWorld(), 1.0, 1.0, 1.0);
            final PacketPlayOutBed packet = new PacketPlayOutBed();
            this.setValue(packet, "a", this.entityID);
            this.setValue(packet, "b", new BlockPosition(bedLocation.getX(), bedLocation.getY(), bedLocation.getZ()));
            for (final Player pl : Bukkit.getOnlinePlayers()) {
                pl.sendBlockChange(bedLocation, Material.BED_BLOCK, (byte)0);
            }
            this.sendPacket((Packet<?>)packet, p);
            this.teleport(p, this.location.clone().add(0.0, 0.3, 0.0));
        }
        else {
            this.animation(p, 2);
            this.teleport(p, this.location.clone().subtract(0.0, 0.3, 0.0));
        }
    }
    
    public void spawn(final Player p, final boolean invis, final boolean tab) {
        this.alive = true;
        final PacketPlayOutNamedEntitySpawn packet = new PacketPlayOutNamedEntitySpawn();
        this.setValue(packet, "a", this.entityID);
        this.setValue(packet, "b", this.gameprofile.getId());
        this.setValue(packet, "c", this.getFixLocation(this.location.getX()));
        this.setValue(packet, "d", this.getFixLocation(this.location.getY()));
        this.setValue(packet, "e", this.getFixLocation(this.location.getZ()));
        this.setValue(packet, "f", this.getFixRotation(this.location.getYaw()));
        this.setValue(packet, "g", this.getFixRotation(this.location.getPitch()));
        this.setValue(packet, "h", 0);
        if (this.dataWatcher == null) {
            (this.dataWatcher = new DataWatcher((net.minecraft.server.v1_8_R3.Entity)null)).a(6, (Object)20.0f);
            this.dataWatcher.a(0, (Object)(byte)(invis ? 32 : 0));
            this.dataWatcher.a(10, (Object)127);
        }
        this.addToTablist(p);
        this.setValue(packet, "i", this.dataWatcher);
        this.sendPacket((Packet<?>)packet, p);
        this.headRotation(p, this.location.getYaw(), this.location.getPitch());
        if (!tab) {
            this.rmvFromTablist(p);
        }
        this.isVisible = !invis;
        this.cached_location = this.location;
    }
    
    public void teleport(final Player p, final Location location) {
        if (!this.alive) {
            return;
        }
        final PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport();
        this.setValue(packet, "a", this.entityID);
        this.setValue(packet, "b", this.getFixLocation(location.getX()));
        this.setValue(packet, "c", this.getFixLocation(location.getY()));
        this.setValue(packet, "d", this.getFixLocation(location.getZ()));
        this.setValue(packet, "e", this.getFixRotation(location.getYaw()));
        this.setValue(packet, "f", this.getFixRotation(location.getPitch()));
        this.setValue(packet, "g", ThreadLocalRandom.current().nextInt(0, 6) > 5);
        this.sendPacket((Packet<?>)packet, p);
        this.headRotation(p, location.getYaw(), location.getPitch());
        this.location = location.clone();
        this.cached_location = location.clone();
    }
    
    public void relMove(final Player p, final Location location) {
        if (!this.alive) {
            return;
        }
        if (!this.cached_location.getWorld().getUID().equals(location.getWorld().getUID())) {
            return;
        }
        if (this.cached_location.distance(location) > 3.0) {
            this.teleport(p, location);
            return;
        }
        final double g = this.getFix(location.getX() - this.cached_location.getX());
        final double h = this.getFix(location.getY() - this.cached_location.getY());
        final double i = this.getFix(location.getZ() - this.cached_location.getZ());
        final double j = this.getFix(location.getYaw() - this.cached_location.getYaw());
        final double k = this.getFix(location.getPitch() - this.cached_location.getPitch());
        final PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook packet = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(this.entityID, (byte)g, (byte)h, (byte)i, (byte)j, (byte)k, LocationHelper.isOnGroundR(location));
        this.sendPacket((Packet<?>)packet, p);
        this.location = location.clone();
        this.cached_location = location.clone();
    }
    
    public void headRotation(final Player p, final float yaw, final float pitch) {
        if (!this.alive) {
            return;
        }
        final PacketPlayOutEntity.PacketPlayOutEntityLook packet = new PacketPlayOutEntity.PacketPlayOutEntityLook(this.entityID, this.getFixRotation(yaw), this.getFixRotation(pitch), true);
        final PacketPlayOutEntityHeadRotation packetHead = new PacketPlayOutEntityHeadRotation();
        this.setValue(packetHead, "a", this.entityID);
        this.setValue(packetHead, "b", this.getFixRotation(yaw));
        this.setValue(packetHead, "c", this.getFixRotation(pitch));
        this.sendPacket((Packet<?>)packet, p);
        this.sendPacket((Packet<?>)packetHead, p);
    }
    
    public void setVelocity(final Player p, final Vector v) {
        if (!this.alive) {
            return;
        }
        final Double x = v.getX();
        final Double y = v.getY();
        final Double z = v.getZ();
        final PacketPlayOutEntityVelocity packet = new PacketPlayOutEntityVelocity(this.entityID, (double)x, (double)y, (double)z);
        this.sendPacket((Packet<?>)packet, p);
    }
    
    public void normalizeHealth(final Player p) {
        if (!this.alive) {
            return;
        }
        final PacketPlayOutUpdateHealth packet = new PacketPlayOutUpdateHealth((float)ThreadLocalRandom.current().nextInt(3, 20), ThreadLocalRandom.current().nextInt(4, 7), 5.0f);
        this.sendPacket((Packet<?>)packet, p);
    }
    
    public void destroy(final Player p) {
        final PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(new int[] { this.entityID });
        this.rmvFromTablist(p);
        this.sendPacket((Packet<?>)packet, p);
        this.alive = false;
    }
    
    public void addToTablist(final Player p) {
        final PacketPlayOutPlayerInfo packetPlayOutPlayerInfo;
        final PacketPlayOutPlayerInfo packet = packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo();
        packetPlayOutPlayerInfo.getClass();
        final PacketPlayOutPlayerInfo.PlayerInfoData data = new PacketPlayOutPlayerInfo.PlayerInfoData(packetPlayOutPlayerInfo, this.gameprofile, 1, WorldSettings.EnumGamemode.NOT_SET, CraftChatMessage.fromString(this.gameprofile.getName())[0]);
        final List<PacketPlayOutPlayerInfo.PlayerInfoData> players = (List<PacketPlayOutPlayerInfo.PlayerInfoData>)this.getValue(packet, "b");
        assert players != null;
        players.add(data);
        this.setValue(packet, "a", PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER);
        this.setValue(packet, "b", players);
        this.sendPacket((Packet<?>)packet, p);
    }
    
    public void rmvFromTablist(final Player p) {
        final PacketPlayOutPlayerInfo packetPlayOutPlayerInfo;
        final PacketPlayOutPlayerInfo packet = packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo();
        packetPlayOutPlayerInfo.getClass();
        final PacketPlayOutPlayerInfo.PlayerInfoData data = new PacketPlayOutPlayerInfo.PlayerInfoData(packetPlayOutPlayerInfo, this.gameprofile, 1, WorldSettings.EnumGamemode.NOT_SET, CraftChatMessage.fromString(this.gameprofile.getName())[0]);
        final List<PacketPlayOutPlayerInfo.PlayerInfoData> players = (List<PacketPlayOutPlayerInfo.PlayerInfoData>)this.getValue(packet, "b");
        assert players != null;
        players.add(data);
        this.setValue(packet, "a", PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER);
        this.setValue(packet, "b", players);
        this.sendPacket((Packet<?>)packet, p);
    }
    
    public void setInvisible(final Player p, final boolean invisible) {
        this.isVisible = !invisible;
        if (!this.alive) {
            return;
        }
        final PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata();
        final List<DataWatcher.WatchableObject> f = new ArrayList<DataWatcher.WatchableObject>();
        f.add(new DataWatcher.WatchableObject(0, 0, (Object)(byte)(invisible ? 32 : 0)));
        this.setValue(metadata, "a", this.entityID);
        this.setValue(metadata, "b", f);
        this.sendPacket((Packet<?>)metadata, p);
    }
    
    private int getFixLocation(final double pos) {
        return MathHelper.floor(pos * 32.0);
    }
    
    private int getFix(final double pos) {
        return MathHelper.floor(pos * 32.0);
    }
    
    public Location getLocation() {
        return this.location;
    }
    
    public boolean isVisible() {
        return this.isVisible;
    }
    
    public boolean isAlive() {
        return this.alive;
    }
    
    public int getEntityID() {
        return this.entityID;
    }
    
    public long getTimeCreated() {
        return this.created;
    }
    
    private byte getFixRotation(final float yawpitch) {
        return (byte)(yawpitch * 256.0f / 360.0f);
    }
    
    private void setValue(final Object obj, final String name, final Object value) {
        try {
            final Field field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(obj, value);
        }
        catch (Exception ex) {}
    }
    
    private Object getValue(final Object obj, final String name) {
        try {
            final Field field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(obj);
        }
        catch (Exception ex) {
            return null;
        }
    }
    
    private void sendPacket(final Packet<?> packet, final Player player) {
        Reflections.sendPacket(player, packet);
    }
    
    public boolean isGrooved() {
        return this.grooved;
    }
    
    public void setGrooved(final boolean grooved) {
        this.grooved = grooved;
    }
}
