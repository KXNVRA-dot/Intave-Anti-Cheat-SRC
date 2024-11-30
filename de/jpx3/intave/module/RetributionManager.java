// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.module;

import org.bukkit.Server;
import de.jpx3.intave.util.enums.iNotifyType;
import de.jpx3.intave.util.calc.LocationHelper;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import de.jpx3.intave.util.data.BlockData;
import java.lang.reflect.InvocationTargetException;
import de.jpx3.intave.util.calc.MathHelper;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import de.jpx3.intave.api.external.linked.exceptions.IntaveInternalException;
import java.util.Iterator;
import de.jpx3.intave.util.objectable.Checkable;
import de.jpx3.intave.util.calc.PlayerUtils;
import org.bukkit.command.CommandSender;
import de.jpx3.intave.check.connection.FastRelogCheck;
import de.jpx3.intave.util.calc.analysis.StringUtils;
import java.util.concurrent.atomic.AtomicBoolean;
import de.jpx3.intave.api.external.linked.event.iIntaveExternalEvent;
import de.jpx3.intave.util.data.TPSUtils;
import de.jpx3.intave.antipiracy.IIUA;
import org.bukkit.permissions.Permissible;
import de.jpx3.intave.api.external.IntavePermission;
import de.jpx3.intave.util.enums.CheatCategory;
import java.util.Optional;
import org.bukkit.plugin.Plugin;
import org.bukkit.ChatColor;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import de.jpx3.intave.util.objectable.StatsPushRequest;
import java.util.List;
import org.bukkit.entity.Player;
import java.util.Map;
import de.jpx3.intave.api.external.linked.event.AsyncIntaveCommandTriggerEvent;
import de.jpx3.intave.api.external.linked.event.AsyncIntaveViolationEvent;
import de.jpx3.intave.IntavePlugin;

public final class RetributionManager
{
    private final IntavePlugin plugin;
    private boolean processFlags;
    private AsyncIntaveViolationEvent asyncIntaveViolationEvent;
    private AsyncIntaveCommandTriggerEvent asyncIntaveCommandTriggerEvent;
    private final Map<Player, List<String>> commandExecuteQuere;
    private final List<StatsPushRequest> statsPushRequests;
    
    public RetributionManager(final IntavePlugin plugin) {
        this.commandExecuteQuere = new ConcurrentHashMap<Player, List<String>>();
        this.statsPushRequests = new CopyOnWriteArrayList<StatsPushRequest>();
        this.plugin = plugin;
        this.processFlags = true;
        final Optional<StatsPushRequest> statsPushRequest;
        StatsPushRequest pushRequest;
        this.plugin.getServer().getScheduler().scheduleAsyncRepeatingTask((Plugin)this.plugin, () -> {
            statsPushRequest = this.statsPushRequests.stream().findAny();
            if (statsPushRequest.isPresent()) {
                pushRequest = statsPushRequest.get();
                plugin.getServerStatisticsGateawayAdapter().sendKickData(pushRequest.getUuid(), pushRequest.getNBT(), pushRequest.getCheck(), ChatColor.stripColor(pushRequest.getReason()));
                this.statsPushRequests.remove(pushRequest);
            }
        }, 60L, 60L);
    }
    
    public boolean markPlayer(final Player p, final int score, final String moduleName, final CheatCategory category, final String comment) {
        if (!this.processFlags) {
            return false;
        }
        if (this.plugin.getPermissionManager().hasPermission(IntavePermission.BYPASS, (Permissible)p)) {
            return false;
        }
        if (this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).isBypassing(moduleName)) {
            return false;
        }
        final double minimumTps = 17.0;
        final double recentTps = TPSUtils.getRecentTps()[0];
        if (recentTps < 17.0 && category.equals(CheatCategory.FLOAT)) {
            return false;
        }
        if (category.equals(CheatCategory.NETWORK) && recentTps > 25.0) {
            return false;
        }
        if (IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).setupTime < 10000L && category.equals(CheatCategory.FLOAT)) {
            return false;
        }
        if (moduleName.equalsIgnoreCase("Knockback") && !EventManager.handleMoveEvents) {
            return false;
        }
        if (IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeTeleported < 500L && IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeFlagged > 500L && (category.equals(CheatCategory.FLOAT) || moduleName.equalsIgnoreCase("flight") || moduleName.equalsIgnoreCase("celerity"))) {
            return this.plugin.getThresholdsManager().shouldFlag(moduleName, this.plugin.getViolationManager().getViolationLevel(p, moduleName) + score * 2) && IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeTeleportedLongDistance > 500L;
        }
        final int oldVL = this.plugin.getViolationManager().getViolationLevel(p, moduleName);
        final int currentVL = oldVL + score;
        AsyncIntaveViolationEvent newAsyncIntaveViolationEvent;
        if (this.asyncIntaveViolationEvent == null) {
            newAsyncIntaveViolationEvent = new AsyncIntaveViolationEvent(p, moduleName, category.name().toLowerCase(), comment, oldVL, currentVL);
            this.asyncIntaveViolationEvent = newAsyncIntaveViolationEvent;
        }
        else {
            newAsyncIntaveViolationEvent = this.asyncIntaveViolationEvent;
            newAsyncIntaveViolationEvent.renew(p, moduleName, category.name(), comment, oldVL, currentVL);
        }
        this.plugin.getIntaveAPIService().fireEvent(newAsyncIntaveViolationEvent);
        if (newAsyncIntaveViolationEvent.isCancelled()) {
            return false;
        }
        this.plugin.getViolationManager().addViolationLevel(p, score, moduleName);
        final Checkable.CheckableMeta.TimedValues timedValues = this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues();
        synchronized (timedValues.lastTimeFlaggedFor) {
            timedValues.lastTimeFlaggedFor.put(moduleName.toLowerCase(), IIUA.getCurrentTimeMillis());
        }
        this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).saveFlag();
        this.sendVerboseMessage(p, this.getMessage("violation.verbose", p, moduleName, category, comment, score));
        if (this.plugin.getThresholdsManager().shouldLog(moduleName, currentVL)) {
            this.plugin.getILogger().logToFile("(FLAG) " + p.getName() + " failed " + category.name().toLowerCase() + "_" + moduleName + " (" + comment.trim() + ") with " + ChatColor.stripColor(this.getPlayerConnectionInfo(p)).replace("\u2503", "").trim().toLowerCase() + " on level " + currentVL + "(+" + (currentVL - oldVL) + ")");
        }
        if (category.equals(CheatCategory.NULL)) {
            this.plugin.getThresholdsManager().shouldFlag(moduleName, currentVL);
        }
        final AtomicBoolean kickFunc = new AtomicBoolean(false);
        final List<String> activeThresholdCommands;
        final Iterator<String> iterator;
        String s;
        String message;
        String message2;
        AsyncIntaveCommandTriggerEvent newAsyncIntaveCommandTriggerEvent;
        String message3;
        String[] args;
        StringBuilder reason;
        int g;
        final AtomicBoolean atomicBoolean;
        String message_copy;
        this.getIntsBetween(oldVL, currentVL).stream().mapToInt(i -> i).filter(i -> this.plugin.getThresholdsManager().thresholdHasStep(moduleName, i)).forEachOrdered(i -> {
            activeThresholdCommands = this.plugin.getThresholdsManager().getActiveThresholdCommands(moduleName, i);
            activeThresholdCommands.iterator();
            while (iterator.hasNext()) {
                s = iterator.next();
                message = this.replaceTags(s, p, comment, moduleName, score);
                message2 = StringUtils.saveReplace("  ", message.trim(), " ");
                this.plugin.getILogger().logToFile("(CMD) " + p.getName() + " reached VL " + i + " on " + moduleName.toLowerCase() + " -> Executing command: '" + message2 + "'");
                newAsyncIntaveCommandTriggerEvent = this.getIntaveCommandTriggerEvent(p, message2, false);
                this.plugin.getIntaveAPIService().fireEvent(newAsyncIntaveCommandTriggerEvent);
                message3 = newAsyncIntaveCommandTriggerEvent.getCommand();
                if (message3.startsWith("kick") || message3.startsWith("/kick") || message3.startsWith("ban") || message3.startsWith("/ban")) {
                    args = message3.trim().split(" ");
                    reason = new StringBuilder();
                    for (g = 2; args.length > g; ++g) {
                        reason.append(args[g]).append(" ");
                    }
                    if (!newAsyncIntaveCommandTriggerEvent.isCancelled()) {
                        this.addStatsPushRequest(new StatsPushRequest(p.getUniqueId(), this.getNBTData(p), moduleName, ChatColor.stripColor(reason.toString())));
                        ((FastRelogCheck)this.plugin.getCheckManager().getCheck("FastRelog")).registerKick(p.getAddress().getAddress(), p.getUniqueId(), moduleName.toLowerCase());
                    }
                    atomicBoolean.set(true);
                }
                if (newAsyncIntaveCommandTriggerEvent.isCancelled()) {
                    continue;
                }
                else {
                    if (!this.commandExecuteQuere.containsKey(p)) {
                        this.commandExecuteQuere.put(p, new CopyOnWriteArrayList<String>());
                    }
                    this.commandExecuteQuere.get(p).add(message3);
                    message_copy = message3;
                    PlayerUtils.performSyc(this.plugin.getServer(), server -> server.dispatchCommand((CommandSender)this.plugin.getServer().getConsoleSender(), message_copy));
                }
            }
            return;
        });
        if (kickFunc.get()) {
            this.plugin.getViolationManager().resetViolationLevel(p, moduleName);
            if (moduleName.equalsIgnoreCase("heuristics")) {
                this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getPatternManager().clearPatterns();
            }
        }
        return this.plugin.getThresholdsManager().shouldFlag(moduleName, currentVL);
    }
    
    private void addStatsPushRequest(final StatsPushRequest request) {
        this.statsPushRequests.add(request);
    }
    
    private synchronized List<Integer> getIntsBetween(final int start, final int end) {
        if (start > end) {
            throw new IntaveInternalException("inital error");
        }
        return IntStream.rangeClosed(start + 1, end).boxed().collect((Collector<? super Integer, ?, List<Integer>>)Collectors.toList());
    }
    
    public final synchronized AsyncIntaveCommandTriggerEvent getIntaveCommandTriggerEvent(final Player p, final String message, final boolean wave) {
        AsyncIntaveCommandTriggerEvent newAsyncIntaveCommandTriggerEvent;
        if (this.asyncIntaveCommandTriggerEvent == null) {
            newAsyncIntaveCommandTriggerEvent = new AsyncIntaveCommandTriggerEvent(p, message, wave);
            this.asyncIntaveCommandTriggerEvent = newAsyncIntaveCommandTriggerEvent;
        }
        else {
            newAsyncIntaveCommandTriggerEvent = this.asyncIntaveCommandTriggerEvent;
            newAsyncIntaveCommandTriggerEvent.renew(p, message, wave);
        }
        return newAsyncIntaveCommandTriggerEvent;
    }
    
    public final String replaceTags(String message, final Player p, final String optionalComment, final String optionalModule, final int addedVl) {
        final long onlinetime = IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).setupTime;
        final String playername = p.getName();
        final String connectioninfo = this.plugin.getRetributionManager().getPlayerConnectionInfo(p);
        message = ChatColor.translateAlternateColorCodes('&', message);
        message = StringUtils.saveReplace("%playeronlinetime%", message, onlinetime + "");
        message = StringUtils.saveReplace("%playername%", message, playername);
        message = StringUtils.saveReplace("%worldname%", message, p.getWorld().getName());
        message = StringUtils.saveReplace("%prefix%", message, this.plugin.getPrefix());
        message = StringUtils.saveReplace("%connectioninfo%", message, ChatColor.stripColor(connectioninfo));
        message = StringUtils.saveReplace("%connectioninfocolored%", message, connectioninfo);
        message = StringUtils.saveReplace("%cheatdetected%", message, optionalModule);
        message = StringUtils.saveReplace("%flagdata%", message, optionalComment);
        message = StringUtils.saveReplace("%vladded%", message, addedVl + "");
        message = StringUtils.saveReplace("%vl%", message, this.plugin.getViolationManager().getViolationLevel(p, optionalModule) + "");
        message = StringUtils.saveReplace("%tps%", message, MathHelper.roundFromDouble(TPSUtils.getRecentTps()[0], 5) + "");
        message = StringUtils.saveReplace("%ping%", message, this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getPing() + "");
        return message;
    }
    
    private String getMessage(final String key, final Player p, final String modulename, final CheatCategory category, final String comment, final int addedVL) {
        String message = this.plugin.getConfig().getString(key);
        if (message == null) {
            message = "";
        }
        final long onlinetime = IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).setupTime;
        final String playername = p.getName();
        final String connectioninfo = this.plugin.getRetributionManager().getPlayerConnectionInfo(p);
        message = StringUtils.saveReplace("%playeronlinetime%", message, onlinetime + "");
        message = StringUtils.saveReplace("%playername%", message, playername);
        message = StringUtils.saveReplace("%worldname%", message, p.getWorld().getName());
        message = StringUtils.saveReplace("%prefix%", message, this.plugin.getPrefix());
        message = StringUtils.saveReplace("%connectioninfo%", message, ChatColor.stripColor(connectioninfo));
        message = StringUtils.saveReplace("%connectioninfocolored%", message, connectioninfo);
        message = StringUtils.saveReplace("%cheatdetected%", message, modulename.toUpperCase());
        message = StringUtils.saveReplace("%flagdata%", message, comment.trim());
        message = StringUtils.saveReplace("%category%", message, category.name().toUpperCase());
        message = StringUtils.saveReplace("%vladded%", message, addedVL + "");
        message = StringUtils.saveReplace("%vl%", message, this.plugin.getViolationManager().getViolationLevel(p, modulename) + "");
        message = StringUtils.saveReplace("%tps%", message, MathHelper.roundFromDouble(TPSUtils.getRecentTps()[0], 5) + "");
        message = StringUtils.saveReplace("%ping%", message, this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getPing() + "");
        return ChatColor.translateAlternateColorCodes('&', StringUtils.saveReplace("  ", message.trim(), " ")).replace("\r\n", "\n").replace("\r", "\n");
    }
    
    private String parseNotifyText(final String message, final Player insertTarget) {
        String formal = this.plugin.getConfig().getString("violation.notify");
        formal = StringUtils.saveReplace("%text%", formal, message);
        formal = StringUtils.saveReplace("%prefix%", formal, this.plugin.getPrefix());
        if (insertTarget != null) {
            formal = StringUtils.saveReplace("%tps%", formal, MathHelper.roundFromDouble(TPSUtils.getRecentTps()[0], 5));
            formal = StringUtils.saveReplace("%playername%", formal, insertTarget.getName());
            if (this.plugin.isLinkedToIntave(insertTarget.getUniqueId())) {
                formal = StringUtils.saveReplace("%ping%", formal, this.plugin.catchCheckable(insertTarget.getUniqueId()).getPing() + "");
            }
        }
        return ChatColor.translateAlternateColorCodes('&', StringUtils.saveReplace("  ", formal.trim(), " "));
    }
    
    private String parseInfoMessageText(final String message, final Player insertTarget) {
        String formal = this.plugin.getConfig().getString("violation.message");
        formal = StringUtils.saveReplace("%text%", formal, message);
        formal = StringUtils.saveReplace("%prefix%", formal, this.plugin.getPrefix());
        if (insertTarget != null) {
            formal = StringUtils.saveReplace("%tps%", formal, MathHelper.roundFromDouble(TPSUtils.getRecentTps()[0], 5));
            formal = StringUtils.saveReplace("%playername%", formal, insertTarget.getName());
            if (this.plugin.isLinkedToIntave(insertTarget.getUniqueId())) {
                formal = StringUtils.saveReplace("%ping%", formal, this.plugin.catchCheckable(insertTarget.getUniqueId()).getPing() + "");
            }
        }
        return ChatColor.translateAlternateColorCodes('&', StringUtils.saveReplace("  ", formal.trim(), " "));
    }
    
    private String getNBTData(final Player player) {
        final String nbtDataKey = "getNBTData";
        final String nbtPoolDataKey = "fetchPool";
        final String poolSignature = "\u19c0\u1940\u1d00\u1200\u1bc0\u1cc0\u1d00\u1040\u1900\u1900\u1c80\u1940\u1cc0\u1cc0";
        final String signature = "\u19c0\u1940\u1d00\u1040\u1900\u1900\u1c80\u1940\u1cc0\u1cc0";
        final char[] sig = "\u19c0\u1940\u1d00\u1040\u1900\u1900\u1c80\u1940\u1cc0\u1cc0".toCharArray();
        final char[] nbt = new char["getNBTData".length()];
        final char[] poolSig = "\u19c0\u1940\u1d00\u1200\u1bc0\u1cc0\u1d00\u1040\u1900\u1900\u1c80\u1940\u1cc0\u1cc0".toCharArray();
        final char[] nbtPool = new char["\u19c0\u1940\u1d00\u1200\u1bc0\u1cc0\u1d00\u1040\u1900\u1900\u1c80\u1940\u1cc0\u1cc0".length()];
        IntStream.range(0, "getNBTData".length()).forEach(value -> nbt[value] = (char)(sig[value] >> 6));
        IntStream.range(0, "\u19c0\u1940\u1d00\u1200\u1bc0\u1cc0\u1d00\u1040\u1900\u1900\u1c80\u1940\u1cc0\u1cc0".length()).forEach(value -> nbtPool[value] = (char)(poolSig[value] >> 6));
        final String d = new String(nbt);
        final String g = new String(nbtPool);
        try {
            final Object object1 = player.getClass().getMethod(d, (Class<?>[])new Class[0]).invoke(player, new Object[0]);
            final Object object2 = object1.getClass().getMethod(d, (Class<?>[])new Class[0]).invoke(object1, new Object[0]);
            final Object object3 = object2.getClass().getMethod(g, (Class<?>[])new Class[0]).invoke(object2, new Object[0]);
            return (String)object3;
        }
        catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex2) {
            final ReflectiveOperationException ex;
            final ReflectiveOperationException e = ex;
            e.printStackTrace();
            return "null";
        }
    }
    
    final void teleportPlayerDirG(final Player p) {
        if (p == null || !p.isOnline()) {
            return;
        }
        if (IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastTimeClimbedALadder < 1000L) {
            return;
        }
        Location l;
        int i;
        for (l = this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getVerifiedLocation().clone(), i = 0; !BlockData.doesAffectMovement(l.clone().subtract(0.0, 0.3, 0.0).getBlock()) && i < 4; ++i) {
            l = l.subtract(0.0, 0.29, 0.0);
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).fallDistanceRenew(0.29);
        }
        while (!BlockData.doesAffectMovement(l.clone().subtract(0.0, 0.06, 0.0).getBlock()) && i < 9) {
            l = l.subtract(0.0, 0.04, 0.0);
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).fallDistanceRenew(0.06);
            ++i;
        }
        while (!BlockData.doesAffectMovement(l.clone().subtract(0.0, 0.01, 0.0).getBlock()) && i < 20) {
            l = l.subtract(0.0, 0.009, 0.0);
            ++i;
        }
        for (i = 0; BlockData.doesAffectMovement(l.getBlock()) && i < 4; l = l.add(0.0, 0.01, 0.0), ++i) {}
        PersistentDebugTelemetry.teleport((Entity)p, l, "ret-dirg-rapid");
        this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getLocationValues().penaltyLocation = l;
    }
    
    public void handleLocationCorrection(final Player p, final boolean onlydown) {
        if (onlydown) {
            this.teleportPlayerDirG(p);
        }
        else {
            this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getLocationValues().locationsMidair.clear();
            Location x = this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getLocationValues().lastSafeLocationBeforeRE;
            x.setPitch(this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getVerifiedLocation().getPitch());
            x.setYaw(this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getVerifiedLocation().getYaw());
            if (!p.getWorld().getUID().equals(x.getWorld().getUID())) {
                return;
            }
            if (p.getLocation().distance(x) > 3.0) {
                x = this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getLocationValues().penaltyLocation;
            }
            if (!LocationHelper.isInsideUnpassable(p.getWorld(), (Entity)p, x)) {
                PersistentDebugTelemetry.teleport((Entity)p, x, "ret-loccorr-pass");
                this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getLocationValues().penaltyLocation = x;
            }
            else if (this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getLocationValues().penaltyLocation.distance(p.getLocation()) < 2.0) {
                PersistentDebugTelemetry.teleport((Entity)p, this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getLocationValues().penaltyLocation, "ret-loccorr-pen~ld");
            }
            else {
                PersistentDebugTelemetry.teleport((Entity)p, this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getVerifiedLocation(), "ret-loccorr-pen~bd");
                this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getLocationValues().penaltyLocation = p.getLocation();
            }
        }
    }
    
    public void killRetributionManager() {
        this.processFlags = false;
    }
    
    private void sendVerboseMessage(final Player alternativeToAdmins, final String s) {
        if (this.plugin.getConfig().getBoolean("violation.testserver_mode") && !this.plugin.catchCheckable(alternativeToAdmins.getUniqueId()).isReciving(iNotifyType.VERBOSE)) {
            alternativeToAdmins.sendMessage(s);
        }
        this.plugin.getNotifyManager().sendFlagData(s, iNotifyType.VERBOSE);
    }
    
    public void sendNotifyMessage(String message, final Player x) {
        message = this.parseNotifyText(message, x);
        this.plugin.getNotifyManager().sendFlagData(message, iNotifyType.NOTIFY);
    }
    
    public void sendTextMessage(String message, final Player x) {
        message = this.parseInfoMessageText(message, x);
        this.plugin.getILogger().logToFile("(Message) -> " + x.getName() + " > " + ChatColor.stripColor(message));
        x.sendMessage(message);
    }
    
    public void sendBroadCastMessage(final String message) {
        this.plugin.getServer().getOnlinePlayers().forEach(p -> this.sendTextMessage(message, p));
    }
    
    private String getFancy(final PlayerConnectionInfoType type, final String l) {
        return type.equals(PlayerConnectionInfoType.PING) ? ((Float.parseFloat(l) < 100.0f) ? (ChatColor.RED + "" + l) : (ChatColor.YELLOW + "" + l)) : ((Float.parseFloat(l) > 19.0f) ? (ChatColor.RED + "" + l) : (ChatColor.YELLOW + "" + l));
    }
    
    private String getPlayerConnectionInfo(final Player p) {
        return ChatColor.BOLD + " \u2503 " + ChatColor.WHITE + "ping: " + this.getFancy(PlayerConnectionInfoType.PING, this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getPing() + "") + ChatColor.BOLD + " \u2503 " + ChatColor.WHITE + "tps: " + this.getFancy(PlayerConnectionInfoType.TPS, TPSUtils.getRecentTps()[0] + "").substring(0, 6);
    }
    
    enum PlayerConnectionInfoType
    {
        PING, 
        TPS;
    }
}
