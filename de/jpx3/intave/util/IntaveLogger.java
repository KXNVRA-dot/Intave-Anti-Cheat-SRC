// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util;

import java.io.IOException;
import java.io.Writer;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import de.jpx3.intave.antipiracy.IIUA;
import java.util.Objects;
import java.io.File;
import org.bukkit.ChatColor;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.io.PrintWriter;
import de.jpx3.intave.IntavePlugin;

public final class IntaveLogger
{
    private final IntavePlugin plugin;
    private final boolean debug;
    private PrintWriter cWriter;
    
    public IntaveLogger(final IntavePlugin plugin) {
        this.cWriter = null;
        this.plugin = plugin;
        this.debug = plugin.getPluginState().equalsIgnoreCase("Development");
    }
    
    public void info(final String s) {
        System.out.println("[Intave] " + s);
        this.logToFile("(INF) " + s);
    }
    
    public void error(final String s) {
        System.out.println("[Intave] ERROR: " + s);
        this.logToFile("(ERR) " + s);
    }
    
    public void debug(final String s) {
        System.out.println("[Intave] [DEBUG] " + s);
    }
    
    public void logToFile(final String message) {
        try {
            if (!this.plugin.getDataFolder().exists()) {
                return;
            }
            if (!this.plugin.getConfig().getBoolean("logging.file_log")) {
                return;
            }
            if (this.cWriter == null) {
                this.createPrintWriter();
            }
            final PrintWriter pw = this.cWriter;
            final String timestamp = "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/YYYY HH.mm.ss.SSS")) + "] ";
            if (pw == null) {
                IntaveExceptionHandler.printAndSaveToFile("[FATAL] Intave couldn't load java.io.PrintWriter. Expecting internal problem", new NullPointerException("PrintWriter is null"));
                return;
            }
            pw.println(timestamp + ChatColor.stripColor(message.replace("\n", "\\n").replace("\r", "\\r").replace("  ", " ")));
            pw.flush();
        }
        catch (Exception e) {
            IntaveExceptionHandler.printAndSaveToFile("[FATAL] An exception was thrown while intave tried to log a message.", e);
            this.plugin.shutdown();
        }
    }
    
    private int cleanupLogs() {
        final File folder = new File("plugins/Intave/logs");
        final int i = 0;
        if (folder.listFiles() == null) {
            return 0;
        }
        if (Objects.requireNonNull(folder.listFiles()).length < 3) {
            return 0;
        }
        return i;
    }
    
    public final File[] getLogFiles() {
        final File folder = new File("plugins/Intave/logs");
        if (!folder.exists()) {
            return new File[0];
        }
        if (folder.listFiles() == null) {
            return new File[0];
        }
        if (Objects.requireNonNull(folder.listFiles()).length < 1) {
            return new File[0];
        }
        return folder.listFiles();
    }
    
    private void createPrintWriter() {
        final File f = new File("plugins/Intave/logs");
        if (!f.exists()) {
            try {
                f.mkdirs();
            }
            catch (Exception e) {
                IntaveExceptionHandler.printAndSaveToFile("[FATAL] Intave could not create its logfolder.", e);
            }
            this.info("LogFolder not found, creating...");
        }
        final int cleaned = this.cleanupLogs();
        try {
            final long time = IIUA.getCurrentTimeMillis();
            final SimpleDateFormat sdf = new SimpleDateFormat("YYYY_MM_dd_HH-mm-ss");
            final String timestamp = sdf.format(time);
            final File d = new File("plugins" + File.separator + "Intave" + File.separator + "logs", "intave" + timestamp + ".log");
            if (!d.exists()) {
                d.createNewFile();
            }
            this.cWriter = new PrintWriter(new FileWriter(d, true));
        }
        catch (IOException e2) {
            IntaveExceptionHandler.printAndSaveToFile("[FATAL] An exception was thrown while intave tried to log a message", e2);
        }
    }
}
