// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.combat;

import java.util.Iterator;
import java.net.URLConnection;
import org.json.simple.JSONArray;
import java.util.Scanner;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import java.net.URL;
import org.bukkit.Material;
import de.jpx3.intave.util.event.CheckableMoveEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.plugin.Plugin;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.PacketType;
import de.jpx3.intave.api.internal.ProtocolLibAdapter;
import de.jpx3.intave.api.internal.reflections.Reflections;
import org.bukkit.inventory.ItemStack;
import de.jpx3.intave.util.enums.BotClassifier;
import java.lang.reflect.InvocationTargetException;
import de.jpx3.intave.api.internal.item.ItemAPI;
import org.bukkit.util.Vector;
import de.jpx3.intave.util.calc.YawUtil;
import org.bukkit.entity.Entity;
import de.jpx3.intave.util.calc.LocationHelper;
import java.util.concurrent.ThreadLocalRandom;
import de.jpx3.intave.antipiracy.IIUA;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.concurrent.ConcurrentHashMap;
import de.jpx3.intave.util.enums.CheatCategory;
import de.jpx3.intave.api.internal.killaurabot.KillAuraBot;
import java.util.UUID;
import java.util.Map;
import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.util.objectable.IntaveCheck;

public final class PseudoPlayerCheck extends IntaveCheck
{
    private final IntavePlugin plugin;
    private final Map<UUID, Map<String, KillAuraBot>> killAuraBots;
    
    public PseudoPlayerCheck(final IntavePlugin plugin) {
        super("PseudoPlayer", CheatCategory.COMBAT);
        this.killAuraBots = new ConcurrentHashMap<UUID, Map<String, KillAuraBot>>();
        this.plugin = plugin;
        this.setupProtocolAdapter();
    }
    
    private void handleUpdate(final Player player, final Location location) {
        if (this.hasBots(IIUA.getUUIDFrom(player))) {
            if (this.hasBot(IIUA.getUUIDFrom(player), "invisible-rbback")) {
                if (IIUA.getCurrentTimeMillis() - this.getBotUsingName(IIUA.getUUIDFrom(player), "invisible-rbback").getTimeCreated() < 6000L) {
                    this.getBotUsingName(IIUA.getUUIDFrom(player), "invisible-rbback").teleport(player, this.getPosWithRot(location, 3.0, 1.0, location.getYaw() - 45.0f));
                    this.getBotUsingName(IIUA.getUUIDFrom(player), "invisible-lbback").teleport(player, this.getPosWithRot(location, 3.0, 1.0, location.getYaw() + 45.0f));
                }
                else {
                    this.getBotUsingName(IIUA.getUUIDFrom(player), "invisible-rbback").destroy(player);
                    this.getBotsFor(IIUA.getUUIDFrom(player)).remove("invisible-rbback");
                    this.getBotUsingName(IIUA.getUUIDFrom(player), "invisible-lbback").destroy(player);
                    this.getBotsFor(IIUA.getUUIDFrom(player)).remove("invisible-lbback");
                }
            }
            if (this.hasBot(IIUA.getUUIDFrom(player), "backbot")) {
                if (IIUA.getCurrentTimeMillis() - this.getBotUsingName(IIUA.getUUIDFrom(player), "backbot").getTimeCreated() < 15000L) {
                    if (IIUA.getCurrentTimeMillis() - this.getBotUsingName(IIUA.getUUIDFrom(player), "backbot").getTimeCreated() < 7500L) {
                        this.getBotUsingName(IIUA.getUUIDFrom(player), "backbot").relMove(player, this.getBlockBehindOfPlayer(location, 1.0, 8.0));
                    }
                    else {
                        final long lastTimeAttackedBot = IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(player.getUniqueId()).getMeta().getVioValues().lastTimeHitAntiCheatBot;
                        final boolean upMode = lastTimeAttackedBot < 200L;
                        final Location from = this.getBotUsingName(IIUA.getUUIDFrom(player), "backbot").getLocation();
                        final Location to = this.getPosWithRot(location, upMode ? 0.0 : ThreadLocalRandom.current().nextDouble(2.0, 3.5), upMode ? ((double)ThreadLocalRandom.current().nextInt(2, 7)) : 0.0, location.getYaw());
                        if (LocationHelper.isInsideUnpassable(player.getWorld(), (Entity)player, to)) {
                            final boolean useOneBlockUp = LocationHelper.isInsideUnpassable(player.getWorld(), (Entity)player, from) && !LocationHelper.isInsideUnpassable(player.getWorld(), (Entity)player, from.clone().add(0.0, 1.0, 0.0));
                            to.setY(useOneBlockUp ? (from.getY() + 1.0) : from.getY());
                        }
                        if (ThreadLocalRandom.current().nextInt(0, 4) == 1) {
                            to.setYaw(YawUtil.getYawFrom(to, location));
                        }
                        this.getBotUsingName(IIUA.getUUIDFrom(player), "backbot").relMove(player, to);
                        if (ThreadLocalRandom.current().nextInt(0, 8) == 2) {
                            this.getBotUsingName(IIUA.getUUIDFrom(player), "backbot").animation(player, 0);
                        }
                        if (ThreadLocalRandom.current().nextInt(0, 24) == 2) {
                            this.getBotUsingName(IIUA.getUUIDFrom(player), "backbot").animation(player, 1);
                            this.getBotUsingName(IIUA.getUUIDFrom(player), "backbot").setVelocity(player, new Vector(ThreadLocalRandom.current().nextDouble(-2.0, 2.0), ThreadLocalRandom.current().nextDouble(-2.0, 2.0), ThreadLocalRandom.current().nextDouble(-2.0, 2.0)));
                        }
                        if (ThreadLocalRandom.current().nextInt(0, 60) == 31) {
                            this.getBotUsingName(IIUA.getUUIDFrom(player), "backbot").sendPingUpdate(player, this.getRandomPing());
                        }
                        if (ThreadLocalRandom.current().nextInt(0, 10) == 3) {
                            try {
                                this.getBotUsingName(IIUA.getUUIDFrom(player), "backbot").equip(player, 0, ThreadLocalRandom.current().nextBoolean() ? null : ItemAPI.toNMSItem(this.getRandomWeapon()));
                            }
                            catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex3) {
                                final ReflectiveOperationException ex;
                                final ReflectiveOperationException e = ex;
                                e.printStackTrace();
                            }
                        }
                        if (ThreadLocalRandom.current().nextInt(0, 10) == 4) {
                            final int slot = ThreadLocalRandom.current().nextInt(1, 4);
                            final ItemStack item = this.getRandomArmourStack(slot);
                            try {
                                this.getBotUsingName(IIUA.getUUIDFrom(player), "backbot").equip(player, slot, ThreadLocalRandom.current().nextBoolean() ? null : ItemAPI.toNMSItem(item));
                            }
                            catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex4) {
                                final ReflectiveOperationException ex2;
                                final ReflectiveOperationException e2 = ex2;
                                e2.printStackTrace();
                            }
                        }
                        final boolean toggleSneak = this.getBotUsingName(IIUA.getUUIDFrom(player), "backbot").isSneaking() || ThreadLocalRandom.current().nextInt(0, 14) == 2;
                        if (toggleSneak) {
                            this.getBotUsingName(IIUA.getUUIDFrom(player), "backbot").setSprinting(player, this.getBotUsingName(IIUA.getUUIDFrom(player), "backbot").isSneaking());
                            this.getBotUsingName(IIUA.getUUIDFrom(player), "backbot").setSneaking(player, !this.getBotUsingName(IIUA.getUUIDFrom(player), "backbot").isSneaking());
                        }
                    }
                }
                else {
                    this.getBotUsingName(IIUA.getUUIDFrom(player), "backbot").destroy(player);
                    this.getBotsFor(IIUA.getUUIDFrom(player)).remove("backbot");
                }
            }
        }
        if (!this.hasBot(IIUA.getUUIDFrom(player), "invisible-lbback") && this.plugin.catchCheckable(IIUA.getUUIDFrom(player)).isRequestingBots(BotClassifier.SIDE_BOT_IV_2)) {
            final KillAuraBot bot1 = this.createKillAuraBot(player, IIUA.getUUIDFrom(player), "invisible-rbback", this.getPosWithRot(location, 3.0, 1.0, location.getYaw() - 45.0f));
            bot1.spawn(player, true, true);
            bot1.animation(player, 0);
            bot1.sendPingUpdate(player, this.getRandomPing());
            final KillAuraBot bot2 = this.createKillAuraBot(player, IIUA.getUUIDFrom(player), "invisible-lbback", this.getPosWithRot(location, 3.0, 1.0, location.getYaw() + 45.0f));
            bot2.spawn(player, true, true);
            bot2.animation(player, 0);
            bot2.sendPingUpdate(player, this.getRandomPing());
        }
        if (!this.hasBot(IIUA.getUUIDFrom(player), "backbot") && this.plugin.catchCheckable(IIUA.getUUIDFrom(player)).isRequestingBots(BotClassifier.BACKBOT_V_1)) {
            final KillAuraBot bot1 = this.createKillAuraBot(player, IIUA.getUUIDFrom(player), "backbot", this.getPosWithRot(location, 2.0, 2.0, location.getYaw()));
            bot1.spawn(player, false, true);
            bot1.sendPingUpdate(player, this.getRandomPing());
        }
    }
    
    private boolean isInHashMap(final UUID uuid) {
        return this.killAuraBots.containsKey(uuid);
    }
    
    private Map<String, KillAuraBot> getBotsFor(final UUID uuid) {
        synchronized (this.killAuraBots) {
            return this.killAuraBots.get(uuid);
        }
    }
    
    private KillAuraBot getBotUsingName(final UUID uuid, final String botname) {
        return this.getBotsFor(uuid).get(botname.toLowerCase());
    }
    
    private KillAuraBot createKillAuraBot(final Player visible, final UUID uuid, final String name, final Location location) {
        if (!Reflections.getVersion().equalsIgnoreCase("v1_8_R3")) {
            throw new IllegalStateException("Could not load antibots for minecraft version " + Reflections.getVersion());
        }
        if (this.isInHashMap(uuid)) {
            final String[] playerData = this.getNameAndSkinFromUUID(UUID.fromString(this.getRandomUUID()));
            final KillAuraBot bot = new KillAuraBot(visible, playerData[0], location, false, new String[] { playerData[1], playerData[2] });
            this.getBotsFor(uuid).put(name, bot);
            return bot;
        }
        this.killAuraBots.put(uuid, new ConcurrentHashMap<String, KillAuraBot>());
        return this.createKillAuraBot(visible, uuid, name, location);
    }
    
    private boolean hasBots(final UUID uuid) {
        return this.isInHashMap(uuid) && this.getBotsFor(uuid).size() > 0;
    }
    
    private boolean hasBot(final UUID uuid, final String botname) {
        return this.hasBots(uuid) && this.getBotsFor(uuid).keySet().contains(botname.toLowerCase());
    }
    
    private Location getBlockBehindOfPlayer(Location loc, final double blocks, final double height) {
        loc = loc.clone();
        loc.setPitch(0.0f);
        final Vector direction = loc.getDirection().multiply(-blocks);
        direction.setY(0.0);
        loc = loc.add(direction);
        loc.setY(loc.getY() + height);
        return loc;
    }
    
    private Location getPosWithRot(Location loc, final double blocks, final double height, final float rot) {
        loc = loc.clone();
        loc.setYaw(rot);
        loc.setPitch(0.0f);
        final Vector direction = loc.getDirection().clone().multiply(-blocks);
        direction.setY(0.0);
        loc = loc.add(direction);
        if (ThreadLocalRandom.current().nextBoolean()) {
            loc.setYaw(ThreadLocalRandom.current().nextInt(-24, 63) + loc.getYaw());
        }
        final boolean hardRotation = ThreadLocalRandom.current().nextInt(1, 8) == 4;
        loc.setPitch(hardRotation ? ((float)ThreadLocalRandom.current().nextInt(-40, 50)) : ((float)ThreadLocalRandom.current().nextInt(-10, 10)));
        loc.setY(loc.getY() + height);
        return loc;
    }
    
    private void setupProtocolAdapter() {
        final IntavePlugin plugin1 = this.plugin;
        ProtocolLibAdapter.getProtocolManager().addPacketListener((PacketListener)new PacketAdapter(plugin1, new PacketType[] { PacketType.Play.Client.USE_ENTITY }) {
            public void onPacketReceiving(final PacketEvent e) {
                if (!PseudoPlayerCheck.this.isActivated()) {
                    return;
                }
                if (e.getPacket().getEntityUseActions().getValues().get(0).equals((Object)EnumWrappers.EntityUseAction.ATTACK)) {
                    final Player damager = e.getPlayer();
                    if (PseudoPlayerCheck.this.hasBots(damager.getUniqueId())) {
                        final IntavePlugin val$plugin1;
                        final Player p;
                        PseudoPlayerCheck.this.getBotsFor(damager.getUniqueId()).values().stream().filter(hm -> hm.getEntityID() == e.getPacket().getIntegers().getValues().get(0)).mapToLong(hm -> IIUA.getCurrentTimeMillis() - plugin1.catchCheckable(damager.getUniqueId()).getMeta().getVioValues().lastTimeHitAntiCheatBot).forEach(last -> {
                            val$plugin1 = plugin1;
                            val$plugin1.getRetributionManager().markPlayer(p, (last < 340L) ? 4 : 0, "PseudoPlayer", CheatCategory.COMBAT, "attacked a bot");
                            val$plugin1.catchCheckable(p.getUniqueId()).getMeta().getVioValues().lastTimeHitAntiCheatBot = IIUA.getCurrentTimeMillis();
                            e.setCancelled(true);
                        });
                    }
                }
            }
        });
    }
    
    @Override
    public void onCheckableMove(final CheckableMoveEvent e) {
        this.handleUpdate(e.getBukkitPlayer(), e.getTo());
    }
    
    private ItemStack getRandomWeapon() {
        final ItemStack[] itemStacks = { new ItemStack(Material.WOOD_SWORD), new ItemStack(Material.GOLD_SWORD), new ItemStack(Material.IRON_SWORD), new ItemStack(Material.STONE_SWORD), new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.WOOD_AXE), new ItemStack(Material.GOLD_AXE), new ItemStack(Material.IRON_AXE), new ItemStack(Material.STONE_AXE), new ItemStack(Material.DIAMOND_AXE), new ItemStack(Material.BOW), new ItemStack(Material.FISHING_ROD) };
        return itemStacks[ThreadLocalRandom.current().nextInt(0, itemStacks.length - 1)];
    }
    
    private ItemStack getRandomArmourStack(final int pos) {
        ItemStack[] itemStacks = new ItemStack[6];
        switch (pos) {
            case 1: {
                itemStacks = new ItemStack[] { null, new ItemStack(Material.LEATHER_HELMET), new ItemStack(Material.GOLD_HELMET), new ItemStack(Material.CHAINMAIL_HELMET), new ItemStack(Material.IRON_HELMET), new ItemStack(Material.DIAMOND_HELMET) };
                break;
            }
            case 2: {
                itemStacks = new ItemStack[] { null, new ItemStack(Material.LEATHER_CHESTPLATE), new ItemStack(Material.GOLD_CHESTPLATE), new ItemStack(Material.CHAINMAIL_CHESTPLATE), new ItemStack(Material.IRON_CHESTPLATE), new ItemStack(Material.DIAMOND_CHESTPLATE) };
                break;
            }
            case 3: {
                itemStacks = new ItemStack[] { null, new ItemStack(Material.LEATHER_LEGGINGS), new ItemStack(Material.GOLD_LEGGINGS), new ItemStack(Material.CHAINMAIL_LEGGINGS), new ItemStack(Material.IRON_LEGGINGS), new ItemStack(Material.DIAMOND_LEGGINGS) };
                break;
            }
            case 4: {
                itemStacks = new ItemStack[] { null, new ItemStack(Material.LEATHER_BOOTS), new ItemStack(Material.GOLD_BOOTS), new ItemStack(Material.CHAINMAIL_BOOTS), new ItemStack(Material.IRON_BOOTS), new ItemStack(Material.DIAMOND_BOOTS) };
                break;
            }
        }
        return itemStacks[ThreadLocalRandom.current().nextInt(0, itemStacks.length - 1)];
    }
    
    private int getRandomPing() {
        return ThreadLocalRandom.current().nextInt(ThreadLocalRandom.current().nextInt(11, 16), ThreadLocalRandom.current().nextInt(70, 80));
    }
    
    private String[] getNameAndSkinFromUUID(final UUID uuid) {
        try {
            final URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replaceAll("-", "") + "?unsigned=false");
            final URLConnection uc = url.openConnection();
            uc.setUseCaches(false);
            uc.setDefaultUseCaches(false);
            uc.addRequestProperty("User-Agent", "Intave/" + IntavePlugin.getStaticReference().getVersion());
            uc.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
            uc.addRequestProperty("Pragma", "no-cache");
            final JSONObject jsonOBJ = (JSONObject)new JSONParser().parse(new Scanner(uc.getInputStream(), "UTF-8").useDelimiter("\\A").next());
            for (final Object property1 : (JSONArray)jsonOBJ.get((Object)"properties")) {
                try {
                    final JSONObject property2 = (JSONObject)property1;
                    return new String[] { (String)jsonOBJ.get((Object)"name"), (String)property2.get((Object)"value"), (String)(property2.containsKey((Object)"signature") ? property2.get((Object)"signature") : "") };
                }
                catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                break;
            }
        }
        catch (Exception e2) {
            e2.printStackTrace();
        }
        return new String[0];
    }
    
    private String getRandomUUID() {
        try {
            final String url_path = "https://intave.de/api/randomplayeruuid.php";
            final URL url = new URL("https://intave.de/api/randomplayeruuid.php");
            final URLConnection uc = url.openConnection();
            uc.setUseCaches(false);
            uc.setDefaultUseCaches(false);
            uc.addRequestProperty("User-Agent", "Intave/" + IntavePlugin.getStaticReference().getVersion());
            uc.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
            uc.addRequestProperty("Pragma", "no-cache");
            final Scanner scanner = new Scanner(uc.getInputStream(), "UTF-8");
            final StringBuilder raw = new StringBuilder();
            while (scanner.hasNext()) {
                raw.append(scanner.next());
            }
            return raw.toString();
        }
        catch (Exception f) {
            return UUID.randomUUID().toString();
        }
    }
}
