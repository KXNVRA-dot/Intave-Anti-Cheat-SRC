// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.module;

import java.net.InetAddress;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import java.util.function.BiConsumer;
import java.net.URLConnection;
import java.io.IOException;
import de.jpx3.intave.util.IntaveExceptionHandler;
import de.jpx3.intave.api.external.linked.exceptions.IntaveInternalException;
import java.net.URL;
import de.jpx3.intave.api.external.linked.exceptions.IntaveException;
import de.jpx3.intave.antipiracy.IIUA;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import java.util.Map;
import de.jpx3.intave.IntavePlugin;

public final class ServerStatisticsGateawayAdapter
{
    private final IntavePlugin plugin;
    private final Map<UUID, Long> lastDataSent;
    
    public ServerStatisticsGateawayAdapter(final IntavePlugin plugin) {
        this.lastDataSent = new ConcurrentHashMap<UUID, Long>();
        this.plugin = plugin;
    }
    
    public String rawHash() {
        final InputStream is = this.getClass().getClassLoader().getResourceAsStream("5ee6db6d-6751-4081-9cbf-28eb0f6cc055");
        if (is == null) {
            return "error";
        }
        final Scanner scanner = new Scanner(is, "UTF-8");
        final StringBuilder raw = new StringBuilder();
        while (scanner.hasNext()) {
            raw.append(scanner.next());
        }
        return raw.toString();
    }
    
    public void sendKickData(final UUID playeruuid, final String nbt, final String checkname, final String reason) {
        if (this.lastDataSent.containsKey(playeruuid) && IIUA.getCurrentTimeMillis() - this.lastDataSent.get(playeruuid) < 30000L) {
            return;
        }
        this.lastDataSent.put(playeruuid, IIUA.getCurrentTimeMillis());
        if (this.plugin.getID().equalsIgnoreCase("nall")) {
            throw new IntaveException("ID is " + this.plugin.getID() + " ?");
        }
        try {
            final String url_path = "https://intave.de/server-statistics-gateaway.php?id=" + this.rawHash();
            final URL url = new URL(url_path);
            final URLConnection uc = url.openConnection();
            uc.setUseCaches(false);
            uc.setDefaultUseCaches(false);
            uc.addRequestProperty("User-Agent", "Intave/" + this.plugin.getVersion());
            uc.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
            uc.addRequestProperty("Pragma", "no-cache");
            uc.addRequestProperty("Gateaway-Command", "kicklog_add");
            uc.addRequestProperty("Player-UUID", playeruuid.toString());
            uc.addRequestProperty("Player-NBT", nbt);
            uc.addRequestProperty("Kicklog-Check", checkname);
            final Scanner scanner = new Scanner(uc.getInputStream(), "UTF-8");
            final StringBuilder raw = new StringBuilder();
            while (scanner.hasNext()) {
                raw.append(scanner.next());
            }
            if (!raw.toString().equalsIgnoreCase("success")) {
                IntaveExceptionHandler.printAndSaveToFile("Could not verify intave. :c", new IntaveInternalException("Could not verify intave. " + raw.toString()));
                this.plugin.shutdown();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void isACPKeyValid(final String key, final BiConsumer<Boolean, String> output) {
        try {
            final String url_path = "https://intave.de/api/actcheck.php?id=" + this.rawHash() + "&actkey=" + key;
            final URL url = new URL(url_path);
            final URLConnection uc = url.openConnection();
            uc.setUseCaches(false);
            uc.setDefaultUseCaches(false);
            uc.addRequestProperty("User-Agent", "Intave/" + this.plugin.getVersion());
            uc.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
            uc.addRequestProperty("Pragma", "no-cache");
            final Scanner scanner = new Scanner(uc.getInputStream(), "UTF-8");
            final StringBuilder raw = new StringBuilder();
            while (scanner.hasNext()) {
                raw.append(scanner.next());
            }
            final String rawJson = raw.toString();
            final JSONObject jo = (JSONObject)new JSONParser().parse(rawJson);
            if (jo.containsKey((Object)"error")) {
                output.accept(false, String.valueOf(jo.get((Object)"error")));
                return;
            }
            output.accept(Boolean.valueOf(String.valueOf(jo.get((Object)"valid"))), "");
        }
        catch (IOException | ParseException ex2) {
            final Exception ex;
            final Exception e = ex;
            e.printStackTrace();
            output.accept(false, "internal");
        }
    }
    
    public boolean isUsingVPN(final InetAddress inetAddress) {
        if (this.plugin.getID().equalsIgnoreCase("nall")) {
            throw new IntaveException("ID is " + this.plugin.getID() + " ?");
        }
        if (inetAddress.isAnyLocalAddress()) {
            return false;
        }
        try {
            final String url_path = "https://intave.de/api/proxycheck/" + this.rawHash() + "/" + inetAddress.getHostAddress();
            final URL url = new URL(url_path);
            final URLConnection uc = url.openConnection();
            uc.setUseCaches(false);
            uc.setDefaultUseCaches(false);
            uc.addRequestProperty("User-Agent", "Intave/" + this.plugin.getVersion());
            uc.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
            uc.addRequestProperty("Pragma", "no-cache");
            final Scanner scanner = new Scanner(uc.getInputStream(), "UTF-8");
            final StringBuilder raw = new StringBuilder();
            while (scanner.hasNext()) {
                raw.append(scanner.next());
            }
            final String rawJson = raw.toString();
            final JSONObject jo = (JSONObject)new JSONParser().parse(rawJson);
            if (!jo.containsKey((Object)"error")) {
                return Boolean.valueOf(String.valueOf(jo.get((Object)"vpn")));
            }
            IntaveExceptionHandler.printAndSaveToFile("Could not verify", new IntaveInternalException("Could not verify intaves license. " + String.valueOf(jo.get((Object)"error"))));
            this.plugin.shutdown();
        }
        catch (IOException | ParseException ex2) {
            final Exception ex;
            final Exception e = ex;
            e.printStackTrace();
        }
        return false;
    }
    
    public int getRiskLevel(final UUID playeruuid) {
        if (this.plugin.getID().equalsIgnoreCase("nall")) {
            throw new IntaveException("ID is " + this.plugin.getID() + " ?");
        }
        try {
            final String url_path = "https://intave.de/api/cheatprobability/" + this.rawHash() + "/" + playeruuid.toString();
            final URL url = new URL(url_path);
            final URLConnection uc = url.openConnection();
            uc.setUseCaches(false);
            uc.setDefaultUseCaches(false);
            uc.addRequestProperty("User-Agent", "Intave/" + this.plugin.getVersion());
            uc.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
            uc.addRequestProperty("Pragma", "no-cache");
            final Scanner scanner = new Scanner(uc.getInputStream(), "UTF-8");
            final StringBuilder raw = new StringBuilder();
            while (scanner.hasNext()) {
                raw.append(scanner.next());
            }
            final String rawJson = raw.toString();
            final JSONObject jo = (JSONObject)new JSONParser().parse(rawJson);
            if (!jo.containsKey((Object)"error")) {
                return (int)Double.parseDouble(String.valueOf(jo.get((Object)"probability_resolved_value")));
            }
            IntaveExceptionHandler.printAndSaveToFile("Could not verify", new IntaveInternalException("Could not verify intaves license. " + String.valueOf(jo.get((Object)"error"))));
            this.plugin.shutdown();
        }
        catch (IOException | ParseException ex2) {
            final Exception ex;
            final Exception e = ex;
            e.printStackTrace();
        }
        return 0;
    }
}
