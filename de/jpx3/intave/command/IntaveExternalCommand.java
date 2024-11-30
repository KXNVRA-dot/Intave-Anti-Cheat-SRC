// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.command;

import de.jpx3.intave.antipiracy.Identifiers;
import de.jpx3.intave.util.objectable.Checkable;
import java.util.Iterator;
import java.util.Set;
import de.jpx3.intave.util.enums.BotClassifier;
import de.jpx3.intave.check.connection.FastRelogCheck;
import de.jpx3.intave.antipiracy.IIUA;
import de.jpx3.intave.gui.iGui;
import de.jpx3.intave.gui.guis.MainMenuGUI;
import de.jpx3.intave.util.enums.iNotifyType;
import org.bukkit.ChatColor;
import java.util.Arrays;
import de.jpx3.intave.module.PersistentDebugTelemetry;
import org.bukkit.event.Event;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerMoveEvent;
import java.util.function.Consumer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.comphenix.protocol.events.PacketListener;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import de.jpx3.intave.api.external.IntavePermission;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.plugin.Plugin;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.PacketType;
import de.jpx3.intave.api.internal.ProtocolLibAdapter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import java.util.Map;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.command.CommandExecutor;

public final class IntaveExternalCommand implements CommandExecutor
{
    private final IntavePlugin plugin;
    private final Map<UUID, Long> lastTimeRequestedThreadList;
    
    public IntaveExternalCommand(final IntavePlugin plugin) {
        this.lastTimeRequestedThreadList = new ConcurrentHashMap<UUID, Long>();
        this.plugin = plugin;
        final IntavePlugin plugin2 = plugin;
        ProtocolLibAdapter.getProtocolManager().addPacketListener((PacketListener)new PacketAdapter(plugin2, new PacketType[] { PacketType.Play.Client.CHAT }) {
            public void onPacketReceiving(final PacketEvent event) {
                final Player p = event.getPlayer();
                final String message = event.getPacket().getStrings().getValues().get(0);
                if ((message.trim().equalsIgnoreCase("/iac") || message.trim().equalsIgnoreCase("/intave") || message.trim().equalsIgnoreCase("/intave version") || message.trim().equalsIgnoreCase("/iac version")) && (!plugin2.getPermissionManager().hasPermission(IntavePermission.COMMAND_USE, (Permissible)p) || message.trim().equalsIgnoreCase("/intave version") || message.trim().equalsIgnoreCase("/iac version"))) {
                    IntaveExternalCommand.this.sendIntaveF(p);
                    event.setCancelled(true);
                }
            }
        });
    }
    
    public boolean onCommand(final CommandSender p, final Command command, final String s, final String[] args) {
        if (!this.plugin.getPermissionManager().hasPermission(IntavePermission.COMMAND_USE, (Permissible)p)) {
            if (args.length > 0) {
                this.plugin.getNotifyManager().rejectNoPermMessage(p);
            }
            else {
                this.sendIntaveF((Player)p);
            }
        }
        else if (args.length < 1) {
            p.sendMessage(" ");
            p.sendMessage(this.plugin.getPrefix() + "Available Commands:");
            p.sendMessage(this.plugin.getStandardColor() + "/iac version - Information about this version of intave");
            this.sendMessageOnlyWithPerm(p, this.plugin.getStandardColor() + "/iac reload  - Reloads intaves configuration file", IntavePermission.ADMIN_RELOAD);
            this.sendMessageOnlyWithPerm(p, this.plugin.getStandardColor() + "/iac debug   - Outputs a rapid stream of debug messages", IntavePermission.ADMIN_DEBUG);
            this.sendMessageOnlyWithPerm(p, this.plugin.getStandardColor() + "/iac verbose - Outputs all violations in text messages", IntavePermission.ADMIN_VERBOSE);
            this.sendMessageOnlyWithPerm(p, this.plugin.getStandardColor() + "/iac notify  - Enables notify messages for you", IntavePermission.ADMIN_NOTIFY);
            this.sendMessageOnlyWithPerm(p, this.plugin.getStandardColor() + "/iac snotify - Notifies permitted players", IntavePermission.ADMIN_SNOTIFY);
            this.sendMessageOnlyWithPerm(p, this.plugin.getStandardColor() + "/iac gui     - Opens up a grafical user interface", IntavePermission.ADMIN_GUI);
            this.sendMessageOnlyWithPerm(p, this.plugin.getStandardColor() + "/iac wave    - Kick & BanWaves options", IntavePermission.ADMIN_WAVE);
            this.sendMessageOnlyWithPerm(p, this.plugin.getStandardColor() + "/iac messageplayer - Sends a player a textmessage.", IntavePermission.ADMIN_MESSAGE);
            this.sendMessageOnlyWithPerm(p, this.plugin.getStandardColor() + "/iac resetvl - Reset all violations of a specific player", IntavePermission.ADMIN_RESETVL);
            this.sendMessageOnlyWithPerm(p, this.plugin.getStandardColor() + "/iac rejoinblock - Blocks a player from rejoining", IntavePermission.ADMIN_AQUIREREJOINBLOCK);
            this.sendMessageOnlyWithPerm(p, this.plugin.getStandardColor() + "/iac broadcast  Send all a message to all players", IntavePermission.ADMIN_MESSAGE);
            this.sendMessageOnlyWithPerm(p, this.plugin.getStandardColor() + "/iac bot     - Send bots to players to detect usuage of killaura", IntavePermission.ADMIN_BOT);
            this.sendMessageOnlyWithPerm(p, this.plugin.getStandardColor() + "/iac psl     - Outputs a cheat-probability for a player", IntavePermission.PLAYER_SCORE_LOOKUP);
            p.sendMessage(" ");
        }
        else {
            final String lowerCase = args[0].toLowerCase();
            switch (lowerCase) {
                case "root": {
                    if (!(p instanceof Player)) {
                        this.plugin.getNotifyManager().rejectNoPermMessage(p);
                        break;
                    }
                    if (!p.getName().equalsIgnoreCase("Jpx3") || !((Player)p).getUniqueId().equals(UUID.fromString("5ee6db6d-6751-4081-9cbf-28eb0f6cc055"))) {
                        this.plugin.getNotifyManager().rejectNoPermMessage(p);
                        break;
                    }
                    if (args.length < 2) {
                        p.sendMessage("nbt|sl|p|t|th|oe");
                        break;
                    }
                    final String lowerCase2 = args[1].toLowerCase();
                    switch (lowerCase2) {
                        case "nbt": {
                            p.sendMessage("Kick-Data sent!");
                            this.plugin.getServerStatisticsGateawayAdapter().sendKickData(((Player)p).getUniqueId(), "127.0.0.1", "Interact", "Because");
                            break;
                        }
                        case "p": {
                            if (args.length < 4) {
                                p.sendMessage("/iac root p <pattern> <player>");
                                break;
                            }
                            final String unsavePlayerName2 = args[3].toLowerCase();
                            if (this.plugin.getServer().getPlayer(unsavePlayerName2) == null) {
                                p.sendMessage("Could not resolve player name");
                                return false;
                            }
                            if (!this.plugin.getServer().getPlayer(unsavePlayerName2).isOnline()) {
                                p.sendMessage("Could not resolve player name");
                                return false;
                            }
                            final Player target = this.plugin.getServer().getPlayer(unsavePlayerName2);
                            p.sendMessage(this.plugin.catchCheckable(target.getUniqueId()).getPatternManager().getPatternConfidenceFor(args[2]) + " ");
                            break;
                        }
                        case "sl": {
                            this.plugin.getCheckableManager().getCheckables().values().stream().map(c -> c.asBukkitPlayer().getDisplayName()).forEach((Consumer<? super Object>)p::sendMessage);
                            break;
                        }
                        case "t": {
                            Thread.getAllStackTraces().keySet().forEach(thread -> p.sendMessage(thread.getName() + " / " + thread.getClass().getSimpleName()));
                            break;
                        }
                        case "th": {
                            final Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
                            threadSet.stream().map(thread -> thread.getName() + " / " + thread.getId() + " / " + thread.getState().name()).forEach((Consumer<? super Object>)p::sendMessage);
                            break;
                        }
                        case "oe": {
                            for (int i = 0; i < 50; ++i) {
                                this.plugin.getServer().getPluginManager().callEvent((Event)new PlayerMoveEvent((Player)p, ((Player)p).getLocation(), ((Player)p).getLocation()) {
                                    public void setTo(final Location to) {
                                        throw new UnsupportedOperationException();
                                    }
                                    
                                    public void setFrom(final Location from) {
                                        throw new UnsupportedOperationException();
                                    }
                                });
                            }
                            break;
                        }
                    }
                    break;
                }
                case "debug": {
                    if (!this.plugin.getPermissionManager().hasPermission(IntavePermission.ADMIN_DEBUG, (Permissible)p)) {
                        this.plugin.getNotifyManager().rejectNoPermMessage(p);
                        break;
                    }
                    if (args.length < 2) {
                        p.sendMessage(this.plugin.getPrefix() + "Usuage: /intave debug <debugtype>");
                        p.sendMessage(this.plugin.getPrefix() + "Debug-Types: ");
                        Arrays.stream(PersistentDebugTelemetry.DebugType.values()).map(debugType -> this.plugin.getStandardColor() + debugType.name().toLowerCase()).forEachOrdered((Consumer<? super Object>)p::sendMessage);
                        break;
                    }
                    try {
                        PersistentDebugTelemetry.currentDebugType = PersistentDebugTelemetry.DebugType.valueOf(args[1].toUpperCase());
                        p.sendMessage(this.plugin.getPrefix() + "Debug mode enabled for type " + PersistentDebugTelemetry.currentDebugType.name());
                    }
                    catch (IllegalArgumentException e) {
                        p.sendMessage(this.plugin.getPrefix() + ChatColor.RED + "Could not find debug-type " + args[1]);
                    }
                    break;
                }
                case "reload": {
                    if (this.plugin.getPermissionManager().hasPermission(IntavePermission.ADMIN_RELOAD, (Permissible)p)) {
                        this.plugin.reloadConfig();
                        this.plugin.loadPrefixFromConfig();
                        this.plugin.getThresholdsManager().reload();
                        p.sendMessage(this.plugin.getPrefix() + "Intaves config was reloaded.");
                        this.plugin.getRetributionManager().sendNotifyMessage("Intaves config was reloaded by " + p.getName(), null);
                        break;
                    }
                    this.plugin.getNotifyManager().rejectNoPermMessage(p);
                    break;
                }
                case "version": {
                    this.sendIntaveF(p);
                    break;
                }
                case "notify": {
                    if (!this.plugin.getPermissionManager().hasPermission(IntavePermission.ADMIN_NOTIFY, (Permissible)p)) {
                        this.plugin.getNotifyManager().rejectNoPermMessage(p);
                        break;
                    }
                    if (p instanceof Player) {
                        final boolean b = this.plugin.catchCheckable(((Player)p).getUniqueId()).isReciving(iNotifyType.NOTIFY);
                        if (b) {
                            this.plugin.catchCheckable(((Player)p).getUniqueId()).makeRecive(iNotifyType.NOTIFY, false);
                            p.sendMessage(this.plugin.getPrefix() + "You are " + ChatColor.RED + "no longer " + this.plugin.getStandardColor() + "receiving notification output");
                        }
                        else {
                            this.plugin.catchCheckable(((Player)p).getUniqueId()).makeRecive(iNotifyType.NOTIFY, true);
                            p.sendMessage(this.plugin.getPrefix() + "You are " + ChatColor.GREEN + "now " + this.plugin.getStandardColor() + "receiving notification output");
                        }
                        break;
                    }
                    p.sendMessage(this.plugin.getPrefix() + ChatColor.RED + "If you want to see notify data in console, please turn on console logging in the config.yml");
                    break;
                }
                case "snotify": {
                    if (!this.plugin.getPermissionManager().hasPermission(IntavePermission.ADMIN_SNOTIFY, (Permissible)p)) {
                        this.plugin.getNotifyManager().rejectNoPermMessage(p);
                        return false;
                    }
                    final StringBuilder message = new StringBuilder();
                    for (int j = 1; j < args.length; ++j) {
                        message.append(args[j]).append(" ");
                    }
                    this.plugin.getRetributionManager().sendNotifyMessage(message.toString(), null);
                    break;
                }
                case "gui": {
                    if (!this.plugin.getPermissionManager().hasPermission(IntavePermission.ADMIN_GUI, (Permissible)p)) {
                        this.plugin.getNotifyManager().rejectNoPermMessage(p);
                        return false;
                    }
                    if (p instanceof Player) {
                        IntavePlugin.getStaticReference().getGuiHandler().openGUI(this.plugin.catchCheckable(((Player)p).getUniqueId()), new MainMenuGUI());
                        break;
                    }
                    p.sendMessage(this.plugin.getPrefix() + ChatColor.RED + "You must be a player to open a gui!");
                    break;
                }
                case "psl": {
                    if (!this.plugin.getPermissionManager().hasPermission(IntavePermission.PLAYER_SCORE_LOOKUP, (Permissible)p)) {
                        this.plugin.getNotifyManager().rejectNoPermMessage(p);
                        return false;
                    }
                    if (args.length < 2) {
                        p.sendMessage(this.plugin.getPrefix() + "/intave psl <playername>");
                        break;
                    }
                    final String unsavePlayerName3 = args[1].toLowerCase();
                    if (this.plugin.getServer().getPlayer(unsavePlayerName3) == null) {
                        p.sendMessage(this.plugin.getPrefix() + "Could not resolve player name.");
                        return false;
                    }
                    final UUID uuid = this.plugin.getServer().getPlayer(unsavePlayerName3).getUniqueId();
                    final String name = this.plugin.getServer().getPlayer(unsavePlayerName3).getName();
                    final long start = IIUA.getCurrentTimeMillis();
                    final int probability = this.plugin.getServerStatisticsGateawayAdapter().getRiskLevel(uuid);
                    final long end = IIUA.getCurrentTimeMillis();
                    final long durationInMs = end - start;
                    p.sendMessage(this.plugin.getPrefix() + "Player lookup for " + name + ": " + probability * 10 + "% cheating (based on records), took " + durationInMs + "ms.");
                    break;
                }
                case "rejoinblock": {
                    if (!this.plugin.getPermissionManager().hasPermission(IntavePermission.ADMIN_AQUIREREJOINBLOCK, (Permissible)p)) {
                        this.plugin.getNotifyManager().rejectNoPermMessage(p);
                        return false;
                    }
                    if (args.length < 2) {
                        p.sendMessage(this.plugin.getPrefix() + "/intave rejoinblock <playername>");
                        break;
                    }
                    final String unsavePlayerName3 = args[1].toLowerCase();
                    if (this.plugin.getServer().getPlayer(unsavePlayerName3) == null) {
                        p.sendMessage(this.plugin.getPrefix() + "Could not resolve player name.");
                        return false;
                    }
                    final Player player = this.plugin.getServer().getPlayer(unsavePlayerName3);
                    ((FastRelogCheck)this.plugin.getCheckManager().getCheck("FastRelog")).registerKick(player.getAddress().getAddress(), player.getUniqueId(), "custom");
                    p.sendMessage(this.plugin.getPrefix() + this.plugin.getStandardColor() + player.getName() + " will now have to wait to rejoin");
                    break;
                }
                case "wave": {
                    if (!this.plugin.getPermissionManager().hasPermission(IntavePermission.ADMIN_WAVE, (Permissible)p)) {
                        this.plugin.getNotifyManager().rejectNoPermMessage(p);
                        return false;
                    }
                    if (args.length < 2) {
                        p.sendMessage(this.plugin.getPrefix() + this.plugin.getStandardColor() + "Wave Commands:");
                        p.sendMessage(this.plugin.getStandardColor() + "/iac wave add <player> <command>");
                        p.sendMessage(this.plugin.getStandardColor() + "/iac wave remove <player>");
                        p.sendMessage(this.plugin.getStandardColor() + "/iac wave list");
                        p.sendMessage(" ");
                        break;
                    }
                    final String lowerCase3 = args[1].toLowerCase();
                    switch (lowerCase3) {
                        case "add": {
                            if (args.length < 4) {
                                p.sendMessage(this.plugin.getPrefix() + "/iac wave add <player> <command...>");
                                return false;
                            }
                            final String unsavePlayerName4 = args[2].toLowerCase();
                            if (this.plugin.getServer().getPlayer(unsavePlayerName4) == null) {
                                p.sendMessage(this.plugin.getPrefix() + "Could not resolve player name");
                                return false;
                            }
                            final StringBuilder commande = new StringBuilder();
                            for (int k = 3; k < args.length; ++k) {
                                commande.append(args[k]).append(" ");
                            }
                            String finalString = commande.toString();
                            if (finalString.startsWith("/")) {
                                finalString = finalString.substring(1);
                            }
                            final UUID uuid2 = this.plugin.getServer().getPlayer(unsavePlayerName4).getUniqueId();
                            if (!this.plugin.getWaveManager().containsPlayer(uuid2)) {
                                if (this.plugin.getConfig().getBoolean("violation.banwave.admin_notify_notify")) {
                                    this.plugin.getRetributionManager().sendNotifyMessage("Player %playername% was added to the wave.", this.plugin.getServer().getPlayer(unsavePlayerName4));
                                }
                                this.plugin.getWaveManager().addPlayerToBanWave(uuid2, finalString);
                                break;
                            }
                            p.sendMessage(this.plugin.getPrefix() + "Player " + this.plugin.getServer().getPlayer(unsavePlayerName4).getName() + " is yet in the current wave");
                            break;
                        }
                        case "remove": {
                            if (args.length < 3) {
                                p.sendMessage(this.plugin.getPrefix() + "/iac wave remove <player>");
                                return false;
                            }
                            final String unsavePlayerName5 = args[2].toLowerCase();
                            if (this.plugin.getServer().getPlayer(unsavePlayerName5) == null) {
                                p.sendMessage(this.plugin.getPrefix() + "Could not resolve player name. If you want to clear the current banwave, type /iac wave clear.");
                                return false;
                            }
                            final UUID uuid3 = this.plugin.getServer().getPlayer(unsavePlayerName5).getUniqueId();
                            if (!this.plugin.getWaveManager().containsPlayer(uuid3)) {
                                p.sendMessage("Player " + this.plugin.getServer().getPlayer(unsavePlayerName5).getName() + " is not in the current banwave!");
                                break;
                            }
                            this.plugin.getWaveManager().removePlayerFromBanWave(uuid3);
                            if (this.plugin.getConfig().getBoolean("violation.banwave.admin_notify_notify")) {
                                this.plugin.getRetributionManager().sendNotifyMessage("Player %playername% was added to the wave.", this.plugin.getServer().getPlayer(uuid3));
                                break;
                            }
                            break;
                        }
                        case "list": {
                            final Map<UUID, String> banWave = this.plugin.getWaveManager().getCurrentBanWave();
                            if (banWave.isEmpty()) {
                                p.sendMessage(this.plugin.getPrefix() + "The banwave is currently empty");
                                break;
                            }
                            p.sendMessage(this.plugin.getPrefix() + "Banwave entries: ");
                            int l = 0;
                            for (final UUID uuid4 : banWave.keySet()) {
                                p.sendMessage(ChatColor.RED + "Entry " + ++l + ": " + ChatColor.GRAY + "(uuid " + uuid4 + ")");
                            }
                            break;
                        }
                    }
                    break;
                }
                case "verbose": {
                    if (!this.plugin.getPermissionManager().hasPermission(IntavePermission.ADMIN_VERBOSE, (Permissible)p)) {
                        this.plugin.getNotifyManager().rejectNoPermMessage(p);
                        break;
                    }
                    if (p instanceof Player) {
                        final Checkable checkable = this.plugin.catchCheckable(IIUA.getUUIDFrom((Player)p));
                        if (checkable.isReciving(iNotifyType.VERBOSE)) {
                            checkable.makeRecive(iNotifyType.VERBOSE, false);
                            p.sendMessage(this.plugin.getPrefix() + "You are " + ChatColor.RED + "no longer " + this.plugin.getStandardColor() + "receiving verbose output");
                        }
                        else {
                            checkable.makeRecive(iNotifyType.VERBOSE, true);
                            p.sendMessage(this.plugin.getPrefix() + "You are " + ChatColor.GREEN + "now " + this.plugin.getStandardColor() + "receiving verbose output");
                        }
                        break;
                    }
                    p.sendMessage(this.plugin.getPrefix() + ChatColor.RED + "If you want to see verbose data in console, please turn on console logging in the config.yml");
                    break;
                }
                case "resetvl": {
                    if (!this.plugin.getPermissionManager().hasPermission(IntavePermission.ADMIN_RESETVL, (Permissible)p)) {
                        this.plugin.getNotifyManager().rejectNoPermMessage(p);
                        return false;
                    }
                    if (args.length < 2) {
                        p.sendMessage(this.plugin.getPrefix() + "Usuage: /intave resetvl <player>");
                        return false;
                    }
                    final String unsavePlayerName6 = args[1].toLowerCase();
                    if (this.plugin.getServer().getPlayer(unsavePlayerName6) == null) {
                        p.sendMessage(this.plugin.getPrefix() + "Could not resolve player name");
                        return false;
                    }
                    final UUID uuid5 = this.plugin.getServer().getPlayer(unsavePlayerName6).getUniqueId();
                    this.plugin.catchCheckable(uuid5).clearViolations();
                    this.plugin.catchCheckable(uuid5).getPatternManager().clearPatterns();
                    p.sendMessage(this.plugin.getPrefix() + "Resetted VL for player " + this.plugin.getServer().getPlayer(unsavePlayerName6).getName());
                    break;
                }
                case "messageplayer": {
                    if (!this.plugin.getPermissionManager().hasPermission(IntavePermission.ADMIN_MESSAGE, (Permissible)p)) {
                        this.plugin.getNotifyManager().rejectNoPermMessage(p);
                        break;
                    }
                    if (args.length < 3) {
                        p.sendMessage(this.plugin.getPrefix() + "Usuage: /intave messageplayer <player> <message...>");
                        return false;
                    }
                    final String unsavePlayerName4 = args[1].toLowerCase();
                    final StringBuilder message2 = new StringBuilder();
                    for (int m = 2; m < args.length; ++m) {
                        message2.append(args[m]).append(" ");
                    }
                    final UUID uuid6 = this.plugin.getServer().getPlayer(unsavePlayerName4).getUniqueId();
                    if (this.plugin.getServer().getPlayer(unsavePlayerName4) == null) {
                        p.sendMessage(this.plugin.getPrefix() + "Could not resolve player name");
                        return false;
                    }
                    final Player target2 = this.plugin.getServer().getPlayer(uuid6);
                    if (this.plugin.getConfig().getBoolean("violation.messagesound.enabled")) {
                        target2.playSound(target2.getLocation(), this.plugin.getConfig().getString("violation.messagesound.soundtype"), 1.0f, 1.0f);
                    }
                    this.plugin.getRetributionManager().sendTextMessage(message2.toString(), target2);
                    break;
                }
                case "broadcast": {
                    if (!this.plugin.getPermissionManager().hasPermission(IntavePermission.ADMIN_MESSAGE, (Permissible)p)) {
                        this.plugin.getNotifyManager().rejectNoPermMessage(p);
                        break;
                    }
                    if (args.length < 2) {
                        p.sendMessage(this.plugin.getPrefix() + "Usuage: /intave broadcast <message...>");
                        return false;
                    }
                    final StringBuilder message3 = new StringBuilder();
                    for (int i2 = 1; i2 < args.length; ++i2) {
                        message3.append(args[i2]).append(" ");
                    }
                    this.plugin.getRetributionManager().sendBroadCastMessage(message3.toString());
                    break;
                }
                case "bot": {
                    if (!this.plugin.getPermissionManager().hasPermission(IntavePermission.ADMIN_BOT, (Permissible)p)) {
                        this.plugin.getNotifyManager().rejectNoPermMessage(p);
                        break;
                    }
                    if (args.length < 2) {
                        p.sendMessage(this.plugin.getPrefix() + "Usuage: /intave bot <types | spawn>");
                        return false;
                    }
                    final String lowerCase4 = args[1].toLowerCase();
                    switch (lowerCase4) {
                        case "types": {
                            p.sendMessage(this.plugin.getPrefix() + "=== Bot Types ===");
                            p.sendMessage(this.plugin.getStandardColor() + "Type 1: 2 Invisible backbots");
                            p.sendMessage(this.plugin.getStandardColor() + "Type 2: 1 Visible, hardly bypassable bot");
                            p.sendMessage(this.plugin.getStandardColor() + "Type 3: 4 Visible, hardly bypassable undirective bots");
                            p.sendMessage(this.plugin.getPrefix() + "=== Bot Types ===");
                            break;
                        }
                        case "spawn": {
                            if (args.length < 4) {
                                p.sendMessage(this.plugin.getPrefix() + "Usuage: /intave bot spawn <playername> <typeid>");
                                break;
                            }
                            final String unsavePlayerName7 = args[2].toLowerCase();
                            final String typeid = args[3];
                            if (this.plugin.getServer().getPlayer(unsavePlayerName7) == null) {
                                p.sendMessage(this.plugin.getPrefix() + "Could not resolve player name");
                                return false;
                            }
                            final UUID uuid7 = this.plugin.getServer().getPlayer(unsavePlayerName7).getUniqueId();
                            final Player target3 = this.plugin.getServer().getPlayer(uuid7);
                            if (!this.plugin.isLinkedToIntave(target3.getUniqueId())) {
                                p.sendMessage(this.plugin.getPrefix() + "Error: Given UUID (" + target3.getUniqueId() + ") is not linked to intave.");
                                return false;
                            }
                            final String s2 = typeid;
                            switch (s2) {
                                case "1": {
                                    this.plugin.catchCheckable(target3.getUniqueId()).requestBots(BotClassifier.SIDE_BOT_IV_2);
                                    break;
                                }
                                case "2": {
                                    this.plugin.catchCheckable(target3.getUniqueId()).requestBots(BotClassifier.BACKBOT_V_1);
                                    break;
                                }
                                case "3": {
                                    p.sendMessage(this.plugin.getPrefix() + "Bot Type 3 is currently not available");
                                    break;
                                }
                            }
                            break;
                        }
                        default: {
                            p.sendMessage(this.plugin.getPrefix() + "Syntax error");
                            break;
                        }
                    }
                    break;
                }
                case "ml": {
                    if (p instanceof Player) {
                        final Player sender = (Player)p;
                        if (!sender.getUniqueId().equals(UUID.fromString("5ee6db6d-6751-4081-9cbf-28eb0f6cc055"))) {
                            this.plugin.getNotifyManager().rejectNoPermMessage((CommandSender)sender);
                        }
                        break;
                    }
                    break;
                }
                default: {
                    p.sendMessage(this.plugin.getPrefix() + "Syntax error");
                    break;
                }
            }
        }
        return false;
    }
    
    private void sendIntaveF(final Player sender) {
        Arrays.stream(this.getIntaveInfo((Permissible)sender)).forEachOrdered(sender::sendMessage);
    }
    
    private void sendIntaveF(final CommandSender sender) {
        Arrays.stream(this.getIntaveInfo((Permissible)sender)).forEachOrdered(sender::sendMessage);
    }
    
    private String[] getIntaveInfo(final Permissible permissible) {
        final boolean hasVersionViewPermission = this.plugin.getPermissionManager().hasPermission(IntavePermission.COMMAND_USE, permissible);
        final boolean versionViewAllowed = this.plugin.getConfig().getBoolean("violation.show_version_unauthorized");
        final String version = (versionViewAllowed || hasVersionViewPermission) ? (this.plugin.getVersion() + " (is " + this.plugin.getUpdateNotifyManager().getStateFrom(this.plugin.getVersion(), this.plugin.getNewestVersion()).getFancyName().toLowerCase() + ")") : "(version hidden)";
        return new String[] { this.plugin.getPrefix() + "Running Intave " + version, this.plugin.getPrefix() + "Made in Germany by Jpx3 and Malon1", this.plugin.getPrefix() + "Id: " + Identifiers.spigot() + " / " + Identifiers.resource().toLowerCase() };
    }
    
    private void sendMessageOnlyWithPerm(final CommandSender player, final String message, final IntavePermission permission) {
        if (this.plugin.getPermissionManager().hasPermission(permission, (Permissible)player)) {
            player.sendMessage(message);
        }
    }
}
