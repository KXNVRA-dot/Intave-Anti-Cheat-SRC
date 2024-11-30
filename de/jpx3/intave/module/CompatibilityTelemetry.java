// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.module;

import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import de.jpx3.intave.compatibility.plugin.plugins.NCPDescriptor;
import de.jpx3.intave.compatibility.plugin.plugins.ProtocolLibDescriptor;
import de.jpx3.intave.compatibility.plugin.plugins.SpartanDescriptor;
import de.jpx3.intave.compatibility.plugin.plugins.AACDescriptor;
import java.nio.file.spi.FileSystemProvider;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.StandardOpenOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.net.URL;
import org.bukkit.plugin.Plugin;
import java.io.File;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.Iterator;
import org.bukkit.Bukkit;
import java.util.Arrays;
import de.jpx3.intave.util.IntaveExceptionHandler;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Map;
import de.jpx3.intave.compatibility.plugin.PluginCompatibilityDescriptor;
import java.util.List;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.event.Listener;

public class CompatibilityTelemetry implements Listener
{
    private final IntavePlugin plugin;
    private final List<PluginCompatibilityDescriptor> pluginCompatibilityDescriptors;
    public final Map<String, String> essentialComponents;
    private static final int BUFFER_SIZE = 8192;
    
    public CompatibilityTelemetry(final IntavePlugin plugin) {
        this.pluginCompatibilityDescriptors = new CopyOnWriteArrayList<PluginCompatibilityDescriptor>();
        this.essentialComponents = new ConcurrentHashMap<String, String>();
        this.plugin = plugin;
        this.loadPluginDescriptions();
    }
    
    public void acclaimPostHookupTask() {
        new Thread("intave-compatibility-task") {
            @Override
            public void run() {
                try {
                    Thread.sleep(250L);
                }
                catch (InterruptedException ex) {}
                CompatibilityTelemetry.this.printPluginCompatiblityInformation();
            }
        }.start();
    }
    
    private void printPluginCompatiblityInformation() {
        if (this.pluginCompatibilityDescriptors.size() < 1) {
            IntaveExceptionHandler.printAndSaveToFile("Could not find plugin-description files?", new NullPointerException());
            return;
        }
        for (final PluginCompatibilityDescriptor pluginDescriptor : this.pluginCompatibilityDescriptors) {
            if (this.checkForPlugin(pluginDescriptor.getName())) {
                this.log("It looks like you are running " + pluginDescriptor.getName() + " on your server.");
                final boolean knownProblems = pluginDescriptor.getKnownProblems().length > 0 || pluginDescriptor.getKnownProblemsFor("*") != null;
                if (knownProblems) {
                    this.log("   This plugin might cause following conflicts:");
                    Arrays.stream(pluginDescriptor.getKnownProblems()).forEachOrdered(problem -> this.log("      - " + problem));
                    final Stream<String> versionRelatedProblems = pluginDescriptor.getKnownProblemsFor(Bukkit.getServer().getPluginManager().getPlugin(pluginDescriptor.getName()).getDescription().getVersion());
                    if (versionRelatedProblems != null) {
                        this.log("   This plugin could cause following conflicts on it's current version:");
                        versionRelatedProblems.forEach(s -> this.log("      - " + s));
                    }
                    this.log("   Fixing problems...");
                    final boolean foundPossibleSolution = pluginDescriptor.fixProblems(this.plugin);
                    this.log("   " + (foundPossibleSolution ? "Compatibility solution applied" : "No solution found"));
                }
                if (pluginDescriptor.compatibilityProblematic()) {
                    this.log("   This plugin will conflict with intave, please remove it");
                }
                if (!pluginDescriptor.compatibilityAdviced()) {
                    continue;
                }
                this.log("   It looks like this plugin is essential to intave, so do not remove it.");
            }
        }
    }
    
    public boolean loadComponents() {
        this.essentialComponents.put("ProtocolLib", "https://intave.de/cnd/global/ProtocolLib.jar");
        return this.essentialComponents.keySet().stream().anyMatch((Predicate<? super Object>)this::loadComponent);
    }
    
    private boolean loadComponent(final String componentName) {
        this.log("Checking existence of component \"" + componentName + "\"...");
        final Plugin componentPlugin = Bukkit.getPluginManager().getPlugin(componentName);
        if (componentPlugin != null) {
            if (!componentPlugin.isEnabled()) {
                this.log("Component \"" + componentName + "\" was not enabled. Enabling it...");
                Bukkit.getPluginManager().enablePlugin(componentPlugin);
            }
            else {
                this.log("Component \"" + componentName + "\" is OK");
            }
            return false;
        }
        this.log("Plugin for component \"" + componentName + "\" is not available?");
        final File componentPluginFile = new File(this.plugin.getDataFolder().getParentFile().getAbsolutePath() + "/" + componentName + ".jar");
        if (!componentPluginFile.exists()) {
            this.log("Trying to automatically download \"" + componentName + "\" plugin from \"" + this.essentialComponents.get(componentName) + "\"...");
            try {
                this.downloadComponentPlugin(componentPluginFile, componentName, this.essentialComponents.get(componentName));
                return true;
            }
            catch (Exception e) {
                IntaveExceptionHandler.printAndSaveToFile(e, "Couldn't download component-plugin \"" + componentName + "\"");
            }
        }
        return false;
    }
    
    private void downloadComponentPlugin(final File componentPluginFile, final String componentName, final String downloadURL) throws IOException, InvalidPluginException, InvalidDescriptionException, InterruptedException {
        final URL website = new URL(downloadURL);
        try (final InputStream in = website.openStream()) {
            this.log("Downloading, please wait...");
            final long downloadStart = System.nanoTime();
            final long size = this.download(in, componentPluginFile.toPath(), new CopyOption[0]);
            final long duration = (System.nanoTime() - downloadStart) / 1000000L;
            final double kbps = size / 1000.0 / duration * 1000.0;
            this.log("Download complete (" + (int)(size / 1000.0) + " KB @ ~" + (long)kbps + " kbps in " + duration + " ms), loading component...");
            final Plugin compPlug = this.plugin.getServer().getPluginManager().loadPlugin(componentPluginFile);
            compPlug.onLoad();
            this.plugin.getServer().getPluginManager().enablePlugin(compPlug);
            this.log("Component was loaded");
        }
    }
    
    private long download(final InputStream in, final Path target, final CopyOption... options) throws IOException {
        try {
            final OutputStream ostream = this.newOutputStream(target, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        }
        catch (FileAlreadyExistsException x) {
            throw x;
        }
        OutputStream ostream;
        try (final OutputStream out = ostream) {
            return this.copy(in, out);
        }
    }
    
    private long copy(final InputStream source, final OutputStream sink) throws IOException {
        long nread = 0L;
        final byte[] buf = new byte[8192];
        int nStart = -1;
        int n;
        while ((n = source.read(buf)) > 0) {
            if (nStart < 0) {
                nStart = n;
            }
            sink.write(buf, 0, n);
            nread += n;
        }
        return nread;
    }
    
    private OutputStream newOutputStream(final Path path, final OpenOption... options) throws IOException {
        return this.provider(path).newOutputStream(path, options);
    }
    
    private FileSystemProvider provider(final Path path) {
        return path.getFileSystem().provider();
    }
    
    public void log(final String message) {
        this.plugin.getILogger().info(message);
    }
    
    public void loadPluginDescriptions() {
        this.loadPluginDescription(new AACDescriptor());
        this.loadPluginDescription(new SpartanDescriptor());
        this.loadPluginDescription(new ProtocolLibDescriptor());
        this.loadPluginDescription(new NCPDescriptor());
    }
    
    public void loadPluginDescription(final PluginCompatibilityDescriptor compatibilityDescriptor) {
        this.pluginCompatibilityDescriptors.add(compatibilityDescriptor);
    }
    
    public boolean checkForPlugin(final String pluginname) {
        return this.plugin.getServer().getPluginManager().isPluginEnabled(pluginname);
    }
    
    @EventHandler
    public void on(final PlayerTeleportEvent e) {
        final Player p = e.getPlayer();
        final boolean teleportThrewWorlds = !e.getFrom().getWorld().getUID().equals(e.getTo().getWorld().getUID());
        if (teleportThrewWorlds) {
            final Player player;
            final Player player2;
            this.plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this.plugin, () -> Bukkit.getOnlinePlayers().stream().filter(player::canSee).forEach(o -> {
                player2.hidePlayer(o);
                player2.showPlayer(o);
            }), 5L);
        }
    }
}
