// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.objectable;

import de.jpx3.intave.gui.iGui;
import de.jpx3.intave.check.combat.KillAuraCheck;
import de.jpx3.intave.util.stat.StatisticValue;
import de.jpx3.intave.check.combat.EntitySpoofCheck;
import org.bukkit.block.Block;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.World;
import de.jpx3.intave.util.IntaveExceptionHandler;
import org.bukkit.permissions.Permissible;
import de.jpx3.intave.api.external.IntavePermission;
import de.jpx3.intave.util.enums.iNotifyType;
import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.api.internal.reflections.Reflections;
import de.jpx3.intave.util.enums.BotClassifier;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import com.comphenix.protocol.utility.MinecraftVersion;
import de.jpx3.intave.api.internal.ProtocolLibAdapter;
import de.jpx3.intave.api.internal.ViaVersionAdapter;
import java.util.Objects;
import de.jpx3.intave.antipiracy.IIUA;
import de.jpx3.intave.util.event.CheckableDamageEntityEvent;
import de.jpx3.intave.util.event.CheckableVelocityEvent;
import de.jpx3.intave.util.event.CheckableMoveEvent;
import org.bukkit.entity.Player;

public final class Checkable
{
    private final Player bukkitPlayer;
    private final CheckableMeta checkableMeta;
    private final PatternManager patternManager;
    private final CachingManager cachingManager;
    public final long setupTime;
    public CheckableMoveEvent checkableMoveEvent;
    public CheckableVelocityEvent checkableVelocityEvent;
    public CheckableDamageEntityEvent checkableDamageEntityEvent;
    
    public Checkable(final Player bukkitPlayer) {
        this.bukkitPlayer = bukkitPlayer;
        this.setupTime = IIUA.getCurrentTimeMillis();
        this.checkableMeta = new CheckableMeta();
        this.patternManager = new PatternManager();
        this.cachingManager = new CachingManager();
    }
    
    public void setup() {
        Objects.requireNonNull(this.checkableMeta);
        this.checkableMeta.setup();
    }
    
    public final boolean hasWaterPower() {
        if (this.getMeta().getSystemValues().isOn1d13 == null) {
            boolean is1d13 = false;
            if (ViaVersionAdapter.hasViaVersion()) {
                is1d13 = (ViaVersionAdapter.getPlayerVersion(this.asBukkitPlayer()) > 383);
            }
            else if (ProtocolLibAdapter.getProtocolManager().getMinecraftVersion().isAtLeast(new MinecraftVersion("1.13"))) {
                is1d13 = true;
            }
            this.getMeta().getSystemValues().isOn1d13 = is1d13;
        }
        return this.getMeta().getSystemValues().isOn1d13;
    }
    
    public final Location getVerifiedLocation() {
        return this.getMeta().getLocationValues().verifiedLocation;
    }
    
    public final CheckableMeta getMeta() {
        return this.checkableMeta;
    }
    
    public final PatternManager getPatternManager() {
        return this.patternManager;
    }
    
    public final CachingManager getCorrelatingCacher() {
        return this.cachingManager;
    }
    
    public final void clearViolations() {
        this.getMeta().getVioValues().violationLevels.clear();
        this.getMeta().getHeuristicValues().flagKalysaMachineLearning = false;
    }
    
    public boolean isBypassing(final String check) {
        return IIUA.getCurrentTimeMillis() < this.getMeta().getSystemValues().bypassRequests.getOrDefault(check.toLowerCase().trim(), 0L);
    }
    
    public void setSilentVelocity(final Vector velocity) {
        this.getMeta().getTimedValues().lastTimeVelocityIGrequested = IIUA.getCurrentTimeMillis();
        this.getMeta().getSyncedValues().hadPullDownVelocity = false;
        if (this.bukkitPlayer != null) {
            this.bukkitPlayer.setVelocity(velocity);
        }
    }
    
    public void performVelocityCheck(final Vector velocity) {
        this.getMeta().getSyncedValues().requestedCheckVelocity = velocity;
        this.getMeta().getSyncedValues().shouldPerformVelocityCheck = true;
    }
    
    private void registerCustomBypass(final String checkname, final long start, final long duration) {
        this.getMeta().getSystemValues().bypassRequests.put(checkname.toLowerCase().trim(), start + duration);
    }
    
    public void requestBots(final BotClassifier classifier) {
        if (!Reflections.getVersion().equalsIgnoreCase("v1_8_R3")) {
            IntavePlugin.getStaticReference().getILogger().error("KillAura-Bots are unsupported in version " + Reflections.getVersion() + "! Please use v1_8_R3 instead or wait for upcomming updates.");
        }
        else {
            this.getMeta().getSystemValues().botRequests.put(classifier, IIUA.getCurrentTimeMillis());
        }
    }
    
    public boolean isRequestingBots(final BotClassifier classifier) {
        return Reflections.getVersion().equalsIgnoreCase("v1_8_R3") && IIUA.getCurrentTimeMillis() - this.getMeta().getSystemValues().botRequests.getOrDefault(classifier, 0L) < 2000L;
    }
    
    public boolean isReciving(final iNotifyType type) {
        if (!this.getMeta().getSystemValues().iNotifyRecive.containsKey(type)) {
            final boolean defNotify = type.equals(iNotifyType.NOTIFY) && this.hasPermissionFor(iNotifyType.NOTIFY);
            this.getMeta().getSystemValues().iNotifyRecive.put(type, defNotify);
        }
        return this.getMeta().getSystemValues().iNotifyRecive.get(type);
    }
    
    private boolean hasPermissionFor(final iNotifyType type) {
        return this.bukkitPlayer != null && ((IntavePlugin.getStaticReference().getPermissionManager().hasPermission(IntavePermission.ADMIN_VERBOSE, (Permissible)this.bukkitPlayer) && type.equals(iNotifyType.VERBOSE)) || (IntavePlugin.getStaticReference().getPermissionManager().hasPermission(IntavePermission.ADMIN_NOTIFY, (Permissible)this.bukkitPlayer) && type.equals(iNotifyType.NOTIFY)));
    }
    
    public void makeRecive(final iNotifyType type, final boolean recive) {
        this.getMeta().getSystemValues().iNotifyRecive.put(type, recive);
    }
    
    public boolean wasCurrentlyHittet() {
        return IIUA.getCurrentTimeMillis() - this.getMeta().getTimedValues().lastHitByEntityTimestamp < 3750L && this.getMeta().getTimedValues().lastHitByEntityTimestamp > this.getMeta().getTimedValues().lastTimeFlagged;
    }
    
    public int getPing() {
        if (this.bukkitPlayer == null) {
            return 0;
        }
        if (IIUA.getCurrentTimeMillis() - this.getMeta().getTimedValues().lastTimePingSet < 3000L) {
            return this.getMeta().getSyncedValues().lastPing;
        }
        try {
            final Object converted = Class.forName("org.bukkit.craftbukkit." + Reflections.getVersion() + ".entity.CraftPlayer").cast(this.bukkitPlayer);
            final Object entityPlayer = converted.getClass().getMethod("getHandle", (Class<?>[])new Class[0]).invoke(converted, new Object[0]);
            final int ping = entityPlayer.getClass().getField("ping").getInt(entityPlayer);
            this.getMeta().getSyncedValues().lastPing = ping;
            this.getMeta().getTimedValues().lastTimePingSet = IIUA.getCurrentTimeMillis();
            return ping;
        }
        catch (Exception e) {
            IntaveExceptionHandler.printAndSaveToFile("Failed to load ping for player " + this.bukkitPlayer.getUniqueId(), e);
            return 0;
        }
    }
    
    public final void saveFlag() {
        this.getMeta().getTimedValues().lastTimeFlagged = IIUA.getCurrentTimeMillis();
    }
    
    public final Vector getEstimatedVelocity() {
        if (this.bukkitPlayer == null) {
            return new Vector(0, 0, 0);
        }
        return (this.bukkitPlayer.getVelocity().getX() == 0.0 && this.bukkitPlayer.getVelocity().getZ() == 0.0) ? this.getMeta().getSyncedValues().lastVelocity : this.bukkitPlayer.getVelocity();
    }
    
    public final void removePacketsFromBalanceCalc(final int packets) {
        final CheckableMeta.SyncedValues syncedValues = this.getMeta().getSyncedValues();
        syncedValues.packetCounter -= packets;
        final CheckableMeta.SyncedValues syncedValues2 = this.getMeta().getSyncedValues();
        syncedValues2.packetCounter_4Ticks -= packets;
    }
    
    public final void fallDistanceRenew(final double yMovement) {
        if (this.bukkitPlayer == null || this.getMeta() == null || this.getMeta().getSyncedValues() == null) {
            return;
        }
        this.bukkitPlayer.setFallDistance((float)(this.getMeta().getSyncedValues().lastFallDistance + Math.abs(yMovement)));
        this.getMeta().getSyncedValues().lastFallDistance = this.bukkitPlayer.getFallDistance();
    }
    
    public final Player asBukkitPlayer() throws NullPointerException {
        if (this.bukkitPlayer == null) {
            throw new NullPointerException("Could not reference player through database: Player is not reachable");
        }
        return this.bukkitPlayer;
    }
    
    @Override
    public int hashCode() {
        return this.bukkitPlayer.getUniqueId().hashCode();
    }
    
    @Override
    public String toString() {
        return "Checkable{uuid=" + this.bukkitPlayer.getUniqueId().toString() + "}";
    }
    
    public final class CheckableMeta
    {
        private BalanceValues balanceValues;
        private SyncedValues syncedValues;
        private ViolationValues vioValues;
        private TimedValues timedValues;
        private LocationValues locationValues;
        private SystemValues systemValues;
        private HeuristicValues heuristicValues;
        
        private void setup() {
            this.balanceValues = new BalanceValues();
            this.syncedValues = new SyncedValues();
            this.vioValues = new ViolationValues();
            this.timedValues = new TimedValues();
            this.locationValues = new LocationValues();
            this.systemValues = new SystemValues();
            this.heuristicValues = new HeuristicValues();
            this.setupLocationValues();
        }
        
        private void setupLocationValues() {
            final Location nullLocation = (Checkable.this.bukkitPlayer != null) ? Checkable.this.bukkitPlayer.getLocation() : new Location((World)Bukkit.getWorlds().get(0), 0.0, 0.0, 0.0);
            this.locationValues.locationTick4 = nullLocation;
            this.locationValues.lastLocationOnGround = nullLocation;
            this.locationValues.lastHitted = nullLocation;
            this.locationValues.penaltyLocation = nullLocation;
            this.locationValues.lastSafeLocationBeforeRE = nullLocation;
            this.locationValues.lastLocationSynced = nullLocation;
            this.locationValues.lastVelocity = nullLocation;
            this.locationValues.verifiedLocation = nullLocation;
            this.locationValues.nofallVerifyLocation = nullLocation;
            this.locationValues.lastTeleportToLocation = nullLocation;
            this.systemValues.lockedLocationCache = new CacheableLocation(nullLocation);
        }
        
        public final void resetAllHeuristicalData() {
            this.heuristicValues = new HeuristicValues();
        }
        
        public BalanceValues getBalanceValues() {
            return this.balanceValues;
        }
        
        public SyncedValues getSyncedValues() {
            return this.syncedValues;
        }
        
        public ViolationValues getVioValues() {
            return this.vioValues;
        }
        
        public TimedValues getTimedValues() {
            return this.timedValues;
        }
        
        public LocationValues getLocationValues() {
            return this.locationValues;
        }
        
        public SystemValues getSystemValues() {
            return this.systemValues;
        }
        
        public HeuristicValues getHeuristicValues() {
            return this.heuristicValues;
        }
        
        public final class HeuristicValues
        {
            public Map<String, Double> patternConfidences;
            public Map<String, Boolean> enginesTriggered;
            public List<Long> lastHitsTimestamps;
            public List<Double> angleMissPattern;
            public List<Double> swingPosTSDiff;
            public List<Double> accuracy;
            public List<Double> animationPacketsBalance;
            public boolean flagKalysaMachineLearning;
            public boolean flagPattern57;
            public boolean flaggedAnyPatternInCurrentCyc;
            public boolean lastSneakWasSuspicious;
            public boolean needsAttack;
            public boolean awaitingAttack;
            public double heuristicEntityVL;
            public double scaffoldwalkVL;
            public double lastYawMotionEquality;
            public double lastYawMotionEqualityDiff;
            public double moveEQ;
            public double lastYawDiffCompacity;
            public double lastBPULocationDist;
            public double lastDirectionDiff;
            public double lastStrafeValue;
            public double lastStrafeOGValue;
            public double lastrDirDiff;
            public double lastrnDirDiff;
            public double lastAtanReach;
            public double lastTYAngleDiff;
            public double lastCDiff;
            public double lastMotionSpeedProp;
            public float lastDirection;
            public float lastYawMove;
            public float lastYawMoveDif;
            public float lastYawADiff;
            public float lastrDir;
            public float lastrnDir;
            public float lastPLYaw;
            public float lastPLPitch;
            public long lastTimeHittedOther;
            public long lastBLiDFlag;
            public long lastMotionBlockFlag;
            public long lastAimAssistFlag;
            public long lastFADmgA;
            public long lastFAJump;
            public long lastFASummoidClean;
            public long lastFragVLClean;
            public long lastMRVLClean;
            public long lastSTRFROTClean;
            public long lastSTRFROT2Clean;
            public long lastIYKClean;
            public long lastPositionLookPacket;
            public long lastAttackPacket;
            public long lastTime51CBTReset;
            public long lastAttackExpectFailed;
            public long lastCarbonDebug;
            public long lastAnimationPacket;
            public long lastBlockPlacePacket;
            public int postHitRotationCounter;
            public int suspiciousAimBotAims;
            public int fastAttackVL;
            public int autoBlockVL;
            public int fragVL;
            public int lastSneakDurationTicks;
            public int silimarRotationVL;
            public int orientalRotationVL;
            public int tracingVL;
            public int iSVL;
            public int rotMoveComparisonVL;
            public int rotMoveComparisonVL2;
            public int rotMoveComparisonVL3;
            public double rotMoveComparisonVL4;
            public int rotMoveComparisonVL_combined_total;
            public int correspVL;
            public int deltaVL;
            public int machineSneakVL;
            public int iykVL;
            public int moreKBKVL;
            public int mouseUnsmooth;
            public int atanVL;
            public int autoClickerVL;
            public int engineFlagDelay;
            
            public HeuristicValues() {
                this.patternConfidences = new ConcurrentHashMap<String, Double>();
                this.enginesTriggered = new ConcurrentHashMap<String, Boolean>();
                this.lastHitsTimestamps = new ArrayList<Long>();
                this.angleMissPattern = new ArrayList<Double>();
                this.swingPosTSDiff = new ArrayList<Double>();
                this.accuracy = new ArrayList<Double>();
                this.animationPacketsBalance = new ArrayList<Double>();
                this.flagPattern57 = false;
                this.flaggedAnyPatternInCurrentCyc = false;
                this.lastSneakWasSuspicious = false;
                this.needsAttack = false;
                this.awaitingAttack = false;
                this.heuristicEntityVL = 35.0;
                this.scaffoldwalkVL = 50.0;
                this.moveEQ = 50.0;
                this.lastYawDiffCompacity = 50.0;
                this.lastBPULocationDist = 1.0;
                this.lastTime51CBTReset = IIUA.getCurrentTimeMillis();
                this.engineFlagDelay = 0;
            }
        }
        
        public final class BalanceValues
        {
            public final List<Long> lastPPacketBalance;
            public final List<Integer> movementBalance;
            public final List<Integer> lastSecondPacketCounts;
            public final List<Integer> last4TicksPacketCounts;
            public final List<Integer> pingDifferenceBalance;
            public final List<Long> lastItemClickDiffs;
            public final List<Long> blockPlaceInCornerDiff;
            public final List<Long> blockPlaceUnderDiff;
            
            public BalanceValues() {
                this.lastPPacketBalance = new ArrayList<Long>();
                this.movementBalance = new ArrayList<Integer>();
                this.lastSecondPacketCounts = new ArrayList<Integer>();
                this.last4TicksPacketCounts = new ArrayList<Integer>();
                this.pingDifferenceBalance = new ArrayList<Integer>();
                this.lastItemClickDiffs = new ArrayList<Long>();
                this.blockPlaceInCornerDiff = new ArrayList<Long>();
                this.blockPlaceUnderDiff = new ArrayList<Long>();
            }
        }
        
        public final class SyncedValues
        {
            public final Vector lastBlockFaceLook;
            public Vector lastVelocity;
            public Vector lastVelocityFI;
            public Vector lastNonArtifVelocityFI;
            public Vector lastVector;
            public Vector requestedCheckVelocity;
            public Vector verifMotionVec3D;
            public List<Double> lastYMotions;
            public ItemStack lastClickedItemStack;
            public String lastChatMessage;
            public UUID lastHitted;
            public UUID lastHittedPlayer;
            public UUID lastEntityAttacked;
            public boolean swungArm;
            public boolean positionUpdateExpected;
            public boolean swungArmAndExpectingAttackPacket;
            public boolean attacked;
            public boolean hasEntities;
            public boolean movedWithEntities;
            public boolean hasAnOpenInventory;
            public boolean hadGoodVelocity;
            public boolean hadYVelocityStartMotion;
            public boolean hadYVelocity29F;
            public boolean hadYVelocity20F;
            public boolean hadArtificialVelocity;
            public boolean willGetArtificialVelocity;
            public boolean isUsingItem;
            public boolean hadPullDownVelocity;
            public boolean shouldPerformVelocityCheck;
            public boolean canHitOthers;
            public boolean hasActiveMove;
            public boolean claimingInActiveTaskToBeOnGround;
            public boolean isInSlimeJump;
            public boolean moveArrivedAfterAttack;
            public boolean hadVelocityOverflow;
            public boolean packetOverflowTeleportMade;
            public boolean awaitingPingPacket;
            public boolean flyToggle;
            public double lastYawDiff;
            public double lastSquaredMovement;
            public double lastXZMovement;
            public double lastXMovement;
            public double lastZMovement;
            public double lastXMovementCypta;
            public double lastZMovementCypta;
            public double lastFallDistance;
            public double lastTickSpeed;
            public double lastYmovement;
            public double lastNFYmovement;
            public double lastVFIXZDiff;
            public double lastHReachDistance;
            public double lastMPDiffBalaceDiff;
            public float lastDirectionMidAir;
            public long syncedMovements;
            public long ticksVelocityMidAir;
            public long ticksInAir;
            public long ticksOnLadder;
            public long ticksSneaking;
            public long ticksOnGround;
            public long ticksInLiquid;
            public long ticksSinceLastMovementCorrection;
            public int ticksSinceLastSpeedFX;
            public long ticksInLiquitFloating;
            public long ticksInAir_experimental;
            public int sneakToggles;
            public int lookPacketsInWebCounter;
            public int clickCounter;
            public int itemuseCounter;
            public int awaitingPingPacketId;
            public int itemuseCounter_last;
            public int clickCounter_last;
            public int sprintToggleCounter;
            public int packetCounter;
            public int packetCounter_4Ticks;
            public int velocityCheckLength;
            public int lastSkinAttributeId;
            public int lastPing;
            public int lastClickedSlot;
            public int ticksMotionKellCorr;
            
            public SyncedValues() {
                this.lastBlockFaceLook = new Vector(0, 0, 0);
                this.lastVelocity = new Vector(Math.toIntExact(0L), Math.toIntExact(0L), Math.toIntExact(0L));
                this.lastVelocityFI = new Vector(Math.toIntExact(0L), Math.toIntExact(0L), Math.toIntExact(0L));
                this.lastNonArtifVelocityFI = new Vector(Math.toIntExact(0L), Math.toIntExact(0L), Math.toIntExact(0L));
                this.lastVector = new Vector(Math.toIntExact(0L), Math.toIntExact(0L), Math.toIntExact(0L));
                this.requestedCheckVelocity = new Vector(Math.toIntExact(0L), Math.toIntExact(0L), Math.toIntExact(0L));
                this.verifMotionVec3D = new Vector(Math.toIntExact(0L), Math.toIntExact(0L), Math.toIntExact(0L));
                this.lastYMotions = new ArrayList<Double>();
                this.lastClickedItemStack = null;
                this.lastChatMessage = "";
                this.swungArm = true;
                this.positionUpdateExpected = true;
                this.swungArmAndExpectingAttackPacket = false;
                this.attacked = true;
                this.hadGoodVelocity = true;
                this.hadYVelocityStartMotion = true;
                this.hadYVelocity29F = false;
                this.hadYVelocity20F = false;
                this.hadPullDownVelocity = true;
                this.canHitOthers = true;
                this.hasActiveMove = false;
                this.claimingInActiveTaskToBeOnGround = false;
                this.isInSlimeJump = false;
                this.moveArrivedAfterAttack = false;
                this.hadVelocityOverflow = false;
                this.packetOverflowTeleportMade = false;
                this.awaitingPingPacket = false;
                this.flyToggle = false;
                this.ticksVelocityMidAir = -1L;
                this.ticksInAir = Math.toIntExact(0L);
                this.ticksOnLadder = Math.toIntExact(0L);
                this.ticksSneaking = Math.toIntExact(0L);
                this.ticksOnGround = Math.toIntExact(0L);
                this.ticksInLiquid = Math.toIntExact(0L);
                this.ticksSinceLastMovementCorrection = Math.toIntExact(0L);
                this.ticksInLiquitFloating = Math.toIntExact(0L);
                this.ticksInAir_experimental = Math.toIntExact(0L);
            }
        }
        
        public final class ViolationValues
        {
            public List<Block> blocksToRemove;
            public Map<String, Integer> violationLevels;
            public boolean cancelBlockPlacement;
            public boolean flagIMPSM;
            public boolean flagNextTimeMove;
            public boolean isInDeadQueueMode;
            public boolean cryptaBuffer;
            public long lastCryptaBuffer;
            public long lastTimeSuspiciousForHitBox;
            public long lastTimeSuspiciousForSpeed;
            public long lastTimeSuspiciousForVelocityFly;
            public long lastTimeSuspiciousForMorePackets;
            public long lastTimeSuspiciousForReach;
            public long lastTimeSuspiciousForChestStealer;
            public long lastTimeSuspiciousForInteract;
            public long lastTimeSuspiciousForKillAura;
            public long lastTimeSuspiciousForScaffoldWalk;
            public long lastTimeSuspiciousForPingSpoof;
            public long lastTimeSuspiciousForJesus;
            public long lastTimeSuspiciousForTowerhop;
            public long lastTimeSuspiciousForSaveWalk;
            public long lastTimeSuspiciousForGroundFly;
            public long lastTimeSuspiciousForVelocityMotionReduce;
            public long lastTimeClaimedWrongInventory;
            public long lastTimeHitAntiCheatBot;
            public long lastKillAuraFlag;
            public long lastHitboxFlag;
            public long lastVeloVerifFlag;
            public long lastMLAJFlag;
            public int suspiciousExpandedBlockPlacements;
            public int suspiciousMovementDiffsPostHit;
            public int suspiciousLowPackets;
            public int suspiciousMorePackets4Tick;
            public int suspiciousOnGroundMovements;
            public int suspiciousFallMoves;
            public int suspiciousYDropVL;
            public int suspiciousVelocity;
            public int suspiciousPostHitY;
            public int suspiciousNoFallMovements;
            public int suspiciousJesusMoves;
            public int suspiciousBunnyHops;
            public int suspiciousFastClimbs;
            public int suspiciousPostGHITMoves;
            public int suspiciousSafeWalkBlockPlaces;
            public int suspiciousOvermainBalanceMoves;
            public int stableWaterYMotionVL;
            public int suspiciousNoSlowdownMoves;
            public int moveBalanceVL;
            public int velocityVL;
            public int velocityVL2;
            public int velocityVL3;
            public int velocity_increasementVL;
            public int unstablePacketVl;
            public int killauraVl;
            public int skinBlinkVL;
            public int webFlyVL;
            public int pingSpoofVL;
            public int customPayloadVL;
            public int glideVL;
            public int interactVL;
            public int inventoryMoveVL;
            public int closePacketVL;
            public int itemStealerVL;
            public int hitboxFOVVL;
            public int hitboxRVL;
            public int syntexVL;
            public int quickPacketVl;
            public int cryptaAirVl;
            public int cryptaGroundVl;
            
            public ViolationValues() {
                this.blocksToRemove = new ArrayList<Block>();
                this.violationLevels = new ConcurrentHashMap<String, Integer>();
                this.cancelBlockPlacement = false;
                this.flagIMPSM = false;
                this.flagNextTimeMove = false;
                this.isInDeadQueueMode = false;
                this.cryptaBuffer = false;
                this.syntexVL = 0;
                this.quickPacketVl = 0;
                this.cryptaAirVl = 0;
                this.cryptaGroundVl = 0;
            }
        }
        
        public final class TimedValues
        {
            public final Map<String, Long> lastTimeFlaggedFor;
            public long lastDeath;
            public long lastPacketCheck;
            public long lastTimeIncreasedYMidair;
            public long lastDamageTakenTimestamp;
            public long lastHitByPlayerTimestamp;
            public long lastHitByEntityTimestamp;
            public long lastHitByProjectile;
            public long lastFlagForVelocity;
            public long lastHitOtherTimestamp;
            public long lastHitOtherPlayerTimestamp;
            public long lastCPSCounterResett;
            public long lastPositionPacketDiff;
            public long lastItemUsePacketsCounterResett;
            public long lastMovePacketsCounterResett;
            public long lastTimeOnGroundACC;
            public long lastTimeClickedItem;
            public long lastTimeBlockExplodeAffect;
            public long lastTimeChatMessageSent;
            public long lastTimeUsedItem;
            public long lastTimeUnusedItem;
            public long lastTimeSlimeWasNear;
            public long lastTimeShotArrow;
            public long lastTimeStartedBowPulling;
            public long lastTimeRightClick;
            public long lastTimeHittedWithKnockbackItem;
            public long lastTimeHitCanceled;
            public long lastTimeVelocityIGrequested;
            public long lastTimeInteractedWithABed;
            public long lastTimeInteractedWithVehicle;
            public long lastTimeVelocityAdded;
            public long lastTimeXZMovementCancelled;
            public long lastTimeFlying;
            public long lastTimeHadLeviation;
            public long lastTimeChangedPosition;
            public long lastTimeToggledFlight;
            public long lastTimeChangedRotation;
            public long lastTimeBlockPlaced;
            public long lastTimeBlockPlacedUnder;
            public long lastTimeBlockPlacedBelow;
            public long lastTimeBlockPlacedCorner;
            public long lastBlockPlaceBlockRequest;
            public long lastBlockPlaceUnderBlockRequest;
            public long lastTimeBlockBreak;
            public long lastBlockBreakBlockRequest;
            public long lastTimeInWater;
            public long lastTimeInCobweb;
            public long lastTimeOpenedIntentory;
            public long lastTimeClosedInventory;
            public long lastTimeValidBlockPlaced;
            public long lastTimeBlockPacketArrived;
            public long lastTimeEntitiesSpawned;
            public long lastTimeCollisitionWasPossible;
            public long lastTimeCollidedWithTopBlock;
            public long lastTimeHeadInHeap;
            public long lastTimeMovedOnWaterConsistant;
            public long lastTimeSneakF;
            public long lastTimeStandingOnAPiston;
            public long lastTimeStandingOnASlime;
            public long lastTimeStandingOnSoulSand;
            public long lastTimeStandingOnIce;
            public long lastTimeStandingInsideBlock;
            public long lastNearbyBlockPhysicsEvent;
            public long lastTimeClimbedALadder;
            public long lastTimeFlagged;
            public long lastTimeTeleported;
            public long lastTimeTeleportedLongDistance;
            public long lastTimeStandingOnABlock;
            public long lastTimeStepableNearby;
            public long lastKillAuraEIDOverride;
            public long lastTimeHitBlockRequest;
            public long lastTimeHitCancelRequest;
            public long lastTimePositionPacketSent;
            public long lastTimeFlyPacketSent;
            public long lastTimeFakeVelocityApplied;
            public long lastTimeLeftClickedBlock;
            public long lastTimeLeftClickedEntity;
            public long lastTimehadGenericSpeed;
            public long lastTimeUsedelytra;
            public long lastInstandHeathPotion;
            public long lastSneakToggleListenerRefresh;
            public long lastSprintToggleListenerRefresh;
            public long lastSprintToggle;
            public long lastSneakToggled;
            public long lastGameModeChange;
            public long lastFlightToggle;
            public long lastTeleportNano;
            public long lastMoveEvent;
            public long lastRegainEvent;
            public long lastClickInteraction;
            public long lastClickInv;
            public long lastClickInvLeftC;
            public long lastClickInvRightC;
            public long lastTimeClickCancelBlockRequest;
            public long lastTimeKeepAlivePacketSent;
            public long lastTimeCustomPayloadPacketSent;
            public long lastTimePingSet;
            
            public TimedValues() {
                this.lastTimeFlaggedFor = new ConcurrentHashMap<String, Long>();
            }
        }
        
        public final class LocationValues
        {
            public List<Location> locationsMidair;
            public Location lastLocationOnGround;
            public Location lastLocationOnGroundACC;
            public Location lastSafeLocationBeforeRE;
            public Location lastLocationServerTickSync;
            public Location penaltyLocationServerTickSync;
            public Location lastLocationSynced;
            public Location lastLocation;
            public Location penaltyLocation;
            public Location locationTick4;
            public Location lastVelocity;
            public Location lastHitted;
            public Location lastBlockPlacedUnder;
            public Location verifiedLocation;
            public Location nofallVerifyLocation;
            public Location lastTeleportToLocation;
            
            public LocationValues() {
                this.locationsMidair = new ArrayList<Location>();
                this.lastHitted = null;
            }
        }
        
        public final class SystemValues
        {
            private Map<iNotifyType, Boolean> iNotifyRecive;
            private Map<BotClassifier, Long> botRequests;
            private Map<String, Long> bypassRequests;
            CacheableLocation lockedLocationCache;
            Vector lockedVectorCache;
            public EntitySpoofCheck.EntitySpoofServer entitySpoofServer;
            public Map<String, List<StatisticValue>> statistics;
            public KillAuraCheck.StaticBotBuffer botBuffer;
            public iGui currentGUI;
            public String lastClickedPlayerInfo;
            public Boolean isOn1d13;
            public long queredMoves;
            public long executedMoves;
            
            public SystemValues() {
                this.iNotifyRecive = new ConcurrentHashMap<iNotifyType, Boolean>();
                this.botRequests = new ConcurrentHashMap<BotClassifier, Long>();
                this.bypassRequests = new ConcurrentHashMap<String, Long>();
                this.lockedVectorCache = new Vector(0, 0, 0);
                this.entitySpoofServer = new EntitySpoofCheck.EntitySpoofServer();
                this.statistics = new ConcurrentHashMap<String, List<StatisticValue>>();
                this.botBuffer = new KillAuraCheck.StaticBotBuffer();
                this.currentGUI = null;
                this.lastClickedPlayerInfo = "Error";
                this.isOn1d13 = null;
                this.queredMoves = 0L;
                this.executedMoves = 0L;
            }
        }
    }
    
    public final class CachingManager
    {
        public final synchronized Location getClonedAdding(final Location location, final double x, final double y, final double z) {
            return this.getCloned(location).add(x, y, z);
        }
        
        public final synchronized Location getClonedRemoving(final Location location, final double x, final double y, final double z) {
            return this.getCloned(location).subtract(x, y, z);
        }
        
        public final synchronized Location getCloned(final Location location) {
            return Checkable.this.getMeta().getSystemValues().lockedLocationCache.setTo(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        }
        
        public final synchronized Vector getCloned(final Vector vector) {
            return Checkable.this.getMeta().getSystemValues().lockedVectorCache.setX(vector.getX()).setY(vector.getY()).setZ(vector.getZ());
        }
    }
    
    public class CacheableLocation extends Location
    {
        public CacheableLocation(final Checkable this$0, final Location location) {
            this(this$0, location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        }
        
        public CacheableLocation(final World world, final double x, final double y, final double z) {
            super(world, x, y, z);
        }
        
        public CacheableLocation(final World world, final double x, final double y, final double z, final float yaw, final float pitch) {
            super(world, x, y, z, yaw, pitch);
        }
        
        public final synchronized CacheableLocation setTo(final World world, final double x, final double y, final double z) {
            return this.setTo(world, x, y, z, this.getYaw(), this.getPitch());
        }
        
        public final synchronized CacheableLocation setTo(final World world, final double x, final double y, final double z, final float yaw, final float pitch) {
            this.setWorld(world);
            this.setX(x);
            this.setY(y);
            this.setZ(z);
            this.setYaw(yaw);
            this.setPitch(pitch);
            return this;
        }
    }
    
    public final class PatternManager
    {
        public double getPatternConfidenceFor(final String pattern) {
            return Checkable.this.getMeta().getHeuristicValues().patternConfidences.getOrDefault(pattern, 50.0);
        }
        
        public void setPatternConfidenceFor(final String pattern, final double conf) {
            Checkable.this.getMeta().getHeuristicValues().patternConfidences.put(pattern, Math.max(50.0, Math.min(conf, 100.0)));
        }
        
        public final Map<String, Double> getAllPatternConfidences() {
            return Checkable.this.getMeta().getHeuristicValues().patternConfidences;
        }
        
        public void multiplyPatternConfidence(final String pattern, final double multiplier) {
            this.multiplyPatternConfidence(pattern, multiplier, 100.0);
        }
        
        public void multiplyPatternConfidence(final String pattern, final double multiplier, final double max_confidence) {
            double newConfidence = this.getPatternConfidenceFor(pattern) * multiplier;
            if (newConfidence > max_confidence) {
                newConfidence = max_confidence - 2.0;
            }
            if (newConfidence >= 50.0) {
                this.setPatternConfidenceFor(pattern, newConfidence);
            }
        }
        
        public void clearPatterns() {
            Checkable.this.getMeta().getHeuristicValues().patternConfidences.clear();
        }
    }
}
