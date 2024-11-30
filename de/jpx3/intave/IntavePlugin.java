package de.jpx3.intave;

import de.jpx3.intave.util.objectable.Checkable;
import java.util.UUID;
import org.bukkit.command.CommandExecutor;
import de.jpx3.intave.command.IntaveExternalCommand;
import java.util.Iterator;
import java.util.Map;
import java.io.File;
import de.jpx3.intave.util.calc.analysis.StringUtils;
import java.util.List;
import de.jpx3.intave.api.internal.reflections.Reflections;
import de.jpx3.intave.util.IntaveExceptionHandler;
import java.util.ArrayList;
import org.bukkit.plugin.Plugin;
import de.jpx3.intave.antipiracy.IIUA;
import org.bukkit.ChatColor;
import de.jpx3.intave.module.IntaveRemoveAccessController;
import de.jpx3.intave.module.LevelNameShareManager;
import de.jpx3.intave.module.ResourceOverwatch;
import de.jpx3.intave.module.CompatibilityTelemetry;
import de.jpx3.intave.module.CrossVersionSupply;
import de.jpx3.intave.module.UpdateNotifyManager;
import de.jpx3.intave.module.ServerStatisticsGateawayAdapter;
import de.jpx3.intave.antipiracy.IntaveLocalVerification;
import de.jpx3.intave.module.PermissionManager;
import de.jpx3.intave.module.CommandWaveManager;
import de.jpx3.intave.api.internal.ProtocolLibAdapter;
import de.jpx3.intave.module.IntaveResponceManager;
import de.jpx3.intave.gui.GuiHandler;
import de.jpx3.intave.module.InternalStatisticsHandler;
import de.jpx3.intave.module.StatisticsProvider;
import de.jpx3.intave.module.IntaveAPIService;
import de.jpx3.intave.module.EventManager;
import de.jpx3.intave.module.FileManager;
import de.jpx3.intave.module.RetributionManager;
import de.jpx3.intave.util.calc.IntaveBlockIterator;
import de.jpx3.intave.module.CheckManager;
import de.jpx3.intave.module.CheckableManager;
import de.jpx3.intave.module.ThresholdsManager;
import de.jpx3.intave.module.ViolationLevelManager;
import de.jpx3.intave.util.IntaveLogger;
import de.jpx3.intave.api.external.IntaveHandle;
import org.bukkit.plugin.java.JavaPlugin;

public final class IntavePlugin extends JavaPlugin
{
    protected boolean isActivated;
    protected boolean isLoaded;
    private static final String INTAVE_STATE = "Public Release";
    private static final String INTAVE_VER = "12.5.5";
    private String ID;
    private String newestVersion;
    private long startupTime;
    private IntavePlugin reference;
    private static IntavePlugin staticReference;
    private IntaveHandle intaveHandleAPI;
    private IntaveLogger logger;
    private ViolationLevelManager violationManager;
    private ThresholdsManager thresholdsManager;
    private CheckableManager checkableManager;
    private CheckManager checkManager;
    private IntaveBlockIterator intaveBlockIterator;
    private RetributionManager retributionManager;
    private FileManager fileManager;
    private EventManager eventManager;
    private IntaveAPIService intaveAPIService;
    private StatisticsProvider statistics;
    private InternalStatisticsHandler internalStatisticsHandler;
    private GuiHandler guiHandler;
    private IntaveResponceManager responceManager;
    private ProtocolLibAdapter protocolLibAdapter;
    private CommandWaveManager commandWaveManager;
    private PermissionManager permissionManager;
    private IntaveLocalVerification intaveLocalVerification;
    private ServerStatisticsGateawayAdapter serverStatisticsGateawayAdapter;
    private UpdateNotifyManager updateNotifyManager;
    private CrossVersionSupply crossVersionSupply;
    private CompatibilityTelemetry compatibiliyTelemetry;
    private ResourceOverwatch resourceOverwatch;
    private LevelNameShareManager levelNameShareManager;
    private IntaveRemoveAccessController removeAccessController;
    private String prefix;
    private ChatColor standartColor;
    
    public IntavePlugin() {
        this.isActivated = false;
        this.isLoaded = false;
        this.ID = null;
        this.newestVersion = "";
        this.prefix = ChatColor.DARK_GRAY + "[" + ChatColor.RED + ChatColor.BOLD + "Intave" + ChatColor.DARK_GRAY + "]" + ChatColor.GRAY + " ";
        this.standartColor = ChatColor.GRAY;
    }
    
    public void onLoad() {
        this.logger = new IntaveLogger(this);
        this.initConfig();
    }
    
    public void onEnable() {
        final long currentTimeMillis = IIUA.getCurrentTimeMillis();
        this.startupTime = currentTimeMillis;
        final long intaveStartTimestamp = currentTimeMillis;
        this.reference = this;
        (IntavePlugin.staticReference = this).loadPrefixFromConfig();
        this.logger.info("Please stand by...");
        this.compatibiliyTelemetry = new CompatibilityTelemetry(this);
        this.crossVersionSupply = new CrossVersionSupply(this);
        this.updateNotifyManager = new UpdateNotifyManager(this);
        this.intaveLocalVerification = new IntaveLocalVerification();
        this.compatibiliyTelemetry.loadComponents();
        this.loadNewestVersionName();
        final int g = this.intaveLocalVerification.getIntaveVerification();
        if (g - 1 == 0) {
            this.ID = this.getIntaveVerificationName();
        }
        if (this.ID != null && this.ID.equalsIgnoreCase(this.getIntaveVerificationName()) && g - 1 == 0) {
            this.isActivated = true;
        }
        if (this.ID != null && this.isActivated) {
            try {
                this.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this, () -> this.compatibiliyTelemetry.acclaimPostHookupTask());
                this.initModules();
                this.initCommands();
            }
            catch (Exception e) {
                final List<String> header = new ArrayList<String>();
                header.add("An exception was thrown while intave initialized internal components.");
                IntaveExceptionHandler.printAndSaveToFile(header, e);
            }
            if (Reflections.useAlternateMethodNames()) {
                this.logger.info("Changing net.minecraft.server." + Reflections.getVersion() + " reflect mode to \"additional\"");
            }
            this.logger.info("Intave was enabled successfully. (took " + (IIUA.getCurrentTimeMillis() - intaveStartTimestamp) + "ms)");
            this.logger.info("You are running Intave " + this.getVersion() + "/" + this.getNewestVersion() + " (Version is " + this.getUpdateNotifyManager().getStateFrom(this.getVersion(), this.getNewestVersion()).getFancyName().toLowerCase() + ")");
            this.isLoaded = true;
            return;
        }
        String h = "";
        if (g - 2 == 0) {
            h = "Invalid id";
        }
        if (g - 3 == 0) {
            h = "Compromised id";
        }
        if (g - 4 == 0) {
            h = "No internet connection";
        }
        if (g - 5 == 0) {
            h = "Outdated version";
        }
        if (g - 6 == 0) {
            h = "This license was not used in a long time. Please contact us";
        }
        this.logger.error("[FATAL] Intave couldn't be loaded. Could not validate: " + h);
    }
    
    public final void onDisable() {
        try {
            if (this.checkManager != null) {
                this.checkManager.handleSystemShutdown();
            }
            if (this.eventManager != null) {
                this.eventManager.shutdownAsyncTasks();
            }
        }
        catch (Exception e) {
            final List<String> header = new ArrayList<String>();
            header.add("An exception was thrown while shutting down intave!");
            IntaveExceptionHandler.printAndSaveToFile(header, e);
        }
    }
    
    public void shutdown() {
        final long start = IIUA.getCurrentTimeMillis();
        System.out.println("[Intave] Disabling intave...");
        if (this.checkManager != null) {
            this.checkManager.handleSystemShutdown();
        }
        if (this.statistics != null) {
            this.statistics.stopAllStatistics();
        }
        if (this.retributionManager != null) {
            this.retributionManager.killRetributionManager();
        }
        if (this.eventManager != null) {
            this.eventManager.shutdownAsyncTasks();
        }
        EventManager.handleMoveEvents = false;
        System.out.println("[Intave] System offline. (took " + (IIUA.getCurrentTimeMillis() - start) + "ms)");
    }
    
    private void initModules() {
        this.logger.info("Loading internal services...");
        this.fileManager = new FileManager();
        this.intaveBlockIterator = new IntaveBlockIterator();
        this.permissionManager = new PermissionManager(this.reference);
        this.protocolLibAdapter = new ProtocolLibAdapter(this.reference);
        this.intaveAPIService = new IntaveAPIService(this.reference);
        this.checkableManager = new CheckableManager(this.reference);
        this.checkManager = new CheckManager(this.reference);
        this.eventManager = new EventManager(this.reference);
        this.responceManager = new IntaveResponceManager(this.reference);
        this.thresholdsManager = new ThresholdsManager(this.reference);
        this.violationManager = new ViolationLevelManager(this.reference);
        this.retributionManager = new RetributionManager(this.reference);
        this.commandWaveManager = new CommandWaveManager(this.reference);
        this.guiHandler = new GuiHandler(this.reference);
        this.statistics = new StatisticsProvider(this.reference);
        this.internalStatisticsHandler = new InternalStatisticsHandler(this.reference);
        this.serverStatisticsGateawayAdapter = new ServerStatisticsGateawayAdapter(this.reference);
        this.resourceOverwatch = new ResourceOverwatch(this.reference);
        this.levelNameShareManager = new LevelNameShareManager(this.reference);
        this.removeAccessController = new IntaveRemoveAccessController(this.reference);
        this.checkManager.setup();
        this.logger.info("Starting API...");
        this.intaveAPIService.startup();
    }
    
    public void loadPrefixFromConfig() {
        try {
            this.prefix = ChatColor.translateAlternateColorCodes('&', StringUtils.replaceString(this.getConfig().getString("violation.prefix"), "§", "&"));
            this.standartColor = ChatColor.getByChar(StringUtils.getLastColorCodeIn(this.prefix));
        }
        catch (Exception e) {
            final List<String> header = new ArrayList<String>();
            header.add("An exception was thrown while intave tried to load its prefix.");
            IntaveExceptionHandler.printAndSaveToFile(header, e);
        }
    }
    
    private void initConfig() {
        if (!this.getDataFolder().exists()) {
            this.getILogger().info("DataFolder not found, creating...");
            final boolean madeDataFolder = this.getDataFolder().mkdirs();
            if (!madeDataFolder) {
                this.getILogger().info("Could not create DataFolder. Might have been already created?");
            }
        }
        final File config = new File(this.getDataFolder(), "config.yml");
        if (!config.exists()) {
            this.getILogger().info("Configuration not found, creating...");
            this.saveResource("config.yml", false);
        }
        else {
            this.getILogger().info("Configuration found, loading...");
        }
        this.reloadConfig();
        if (this.getConfig().getBoolean("logging.file_log")) {
            this.logger.info("Automated file-logging activated. To deactivate file logging, set the value of \"file_log\" to false");
        }
        for (final Map.Entry<String, Object> configEntry : this.getConfig().getValues(true).entrySet()) {
            final String path = configEntry.getKey();
            if (path.contains("thresholds")) {
                this.getConfig().getDefaults().set(path, (Object)null);
            }
        }
    }
    
    private void initCommands() {
        this.getCommand("intave").setExecutor((CommandExecutor)new IntaveExternalCommand(this));
    }
    
    private void loadNewestVersionName() {
        this.setNewestVersion(this.intaveLocalVerification.getNewestVersionName());
        if (this.getNewestVersion().equalsIgnoreCase(this.getVersion())) {
            this.logger.info("Intave(" + this.getNewestVersion() + ") is up to date");
        }
    }
    
    public final String getPluginState() {
        return "Public Release";
    }
    
    public final String getVersion() {
        return "12.5.5";
    }
    
    public final String getPrefix() {
        return this.prefix;
    }
    
    public final String getIntaveVerificationName() {
        if (this.getID() == null) {
            this.setID(this.intaveLocalVerification.getIdFromFile());
        }
        return this.getID();
    }
    
    public final long getStartupTime() {
        return this.startupTime;
    }
    
    public final ChatColor getStandardColor() {
        return this.standartColor;
    }
    
    public final String getID() {
        return this.ID;
    }
    
    public final String getNewestVersion() {
        return this.newestVersion;
    }
    
    public final Checkable catchCheckable(final UUID referencedUUID) {
        return this.checkableManager.getCheckable(referencedUUID);
    }
    
    public final boolean isLinkedToIntave(final UUID referencedUUID) {
        return referencedUUID != null && this.checkableManager.isUUIDLinkedToIntave(referencedUUID);
    }
    
    public final IntaveLogger getILogger() {
        return this.logger;
    }
    
    public static IntavePlugin getStaticReference() {
        return IntavePlugin.staticReference;
    }
    
    public final CheckManager getCheckManager() {
        return this.checkManager;
    }
    
    public final CheckableManager getCheckableManager() {
        return this.checkableManager;
    }
    
    public final IntaveBlockIterator getIntaveBlockIterator() {
        return this.intaveBlockIterator;
    }
    
    public final ViolationLevelManager getViolationManager() {
        return this.violationManager;
    }
    
    public final ThresholdsManager getThresholdsManager() {
        return this.thresholdsManager;
    }
    
    public final IntaveResponceManager getNotifyManager() {
        return this.responceManager;
    }
    
    public final RetributionManager getRetributionManager() {
        return this.retributionManager;
    }
    
    public final StatisticsProvider getStatisticsProvider() {
        return this.statistics;
    }
    
    public final GuiHandler getGuiHandler() {
        return this.guiHandler;
    }
    
    public final IntaveAPIService getIntaveAPIService() {
        return this.intaveAPIService;
    }
    
    public final CommandWaveManager getWaveManager() {
        return this.commandWaveManager;
    }
    
    public final ServerStatisticsGateawayAdapter getServerStatisticsGateawayAdapter() {
        return this.serverStatisticsGateawayAdapter;
    }
    
    public final FileManager getFileManager() {
        return this.fileManager;
    }
    
    public final PermissionManager getPermissionManager() {
        return this.permissionManager;
    }
    
    public final IntaveHandle getIntaveHandleAPI() {
        return this.intaveHandleAPI;
    }
    
    public final UpdateNotifyManager getUpdateNotifyManager() {
        return this.updateNotifyManager;
    }
    
    public final IntaveLocalVerification getIntaveLocalVerification() {
        return this.intaveLocalVerification;
    }
    
    public final CrossVersionSupply getCrossVersionSupply() {
        return this.crossVersionSupply;
    }
    
    public final CompatibilityTelemetry getCompatibiliyTelemetryManager() {
        return this.compatibiliyTelemetry;
    }
    
    public final ResourceOverwatch getResourceOverwatch() {
        return this.resourceOverwatch;
    }
    
    public final EventManager getEventManager() {
        return this.eventManager;
    }
    
    public void setIntaveHandleAPI(final IntaveHandle intaveHandleAPI) {
        this.intaveHandleAPI = intaveHandleAPI;
    }
    
    private void setID(final String s) {
        this.ID = s;
    }
    
    public void setNewestVersion(final String s) {
        this.newestVersion = s;
    }
    
    public IntavePlugin clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("You can not clone me.");
    }
}
