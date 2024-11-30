// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.module;

import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import de.jpx3.intave.util.enums.CheatCategory;
import java.util.Iterator;
import de.jpx3.intave.api.external.linked.exceptions.IntaveInternalException;
import de.jpx3.intave.util.event.CheckableDamageEntityEvent;
import de.jpx3.intave.util.event.CheckableVelocityEvent;
import de.jpx3.intave.util.event.CheckableMoveEvent;
import de.jpx3.intave.util.event.iIntaveInternalEvent;
import de.jpx3.intave.check.connection.ProxyCheck;
import de.jpx3.intave.check.other.TeamingCheck;
import de.jpx3.intave.check.connection.ClientCommand;
import de.jpx3.intave.check.other.MultipleAccountsCheck;
import de.jpx3.intave.check.other.IntaveBanCheck;
import de.jpx3.intave.check.connection.SkinBlinkCheck;
import de.jpx3.intave.check.world.InvalidAbilities;
import de.jpx3.intave.check.movement.CorruptPacket;
import de.jpx3.intave.check.combat.PseudoPlayerCheck;
import de.jpx3.intave.check.movement.NoFallCheck;
import de.jpx3.intave.check.combat.ClickSpeedCheck;
import de.jpx3.intave.check.other.ChatCheck;
import de.jpx3.intave.check.world.InteractCheck;
import de.jpx3.intave.check.world.FastBreakCheck;
import de.jpx3.intave.check.connection.IllegalRotationCheck;
import de.jpx3.intave.check.combat.NoSwingCheck;
import de.jpx3.intave.check.combat.FastBowCheck;
import de.jpx3.intave.check.combat.HitBoxCheck;
import de.jpx3.intave.check.connection.FastRelogCheck;
import de.jpx3.intave.check.connection.MoveCheck;
import de.jpx3.intave.check.movement.ImpossibleMovement;
import de.jpx3.intave.check.world.MachineBlockCheck;
import de.jpx3.intave.check.movement.KnockbackCheck;
import de.jpx3.intave.check.movement.FlightCheck;
import de.jpx3.intave.check.movement.CelerityCheck;
import de.jpx3.intave.check.combat.Heuristics;
import de.jpx3.intave.check.combat.KillAuraCheck;
import java.util.concurrent.CopyOnWriteArrayList;
import de.jpx3.intave.util.objectable.IntaveCheck;
import java.util.List;
import de.jpx3.intave.IntavePlugin;

public final class CheckManager
{
    private final IntavePlugin plugin;
    private final List<IntaveCheck> checkHashMap;
    
    public CheckManager(final IntavePlugin plugin) {
        this.plugin = plugin;
        this.checkHashMap = new CopyOnWriteArrayList<IntaveCheck>();
    }
    
    public void setup() {
        this.stop();
        this.addCheck(new KillAuraCheck(this.plugin));
        this.addCheck(new Heuristics(this.plugin));
        this.addCheck(new CelerityCheck(this.plugin));
        this.addCheck(new FlightCheck(this.plugin));
        this.addCheck(new KnockbackCheck(this.plugin));
        this.addCheck(new MachineBlockCheck(this.plugin));
        this.addCheck(new ImpossibleMovement(this.plugin));
        this.addCheck(new MoveCheck(this.plugin));
        this.addCheck(new FastRelogCheck(this.plugin));
        this.addCheck(new HitBoxCheck(this.plugin));
        this.addCheck(new FastBowCheck(this.plugin));
        this.addCheck(new NoSwingCheck(this.plugin));
        this.addCheck(new IllegalRotationCheck(this.plugin));
        this.addCheck(new FastBreakCheck(this.plugin));
        this.addCheck(new InteractCheck(this.plugin));
        this.addCheck(new ChatCheck(this.plugin));
        this.addCheck(new ClickSpeedCheck(this.plugin));
        this.addCheck(new NoFallCheck(this.plugin));
        this.addCheck(new PseudoPlayerCheck(this.plugin));
        this.addCheck(new CorruptPacket(this.plugin));
        this.addCheck(new InvalidAbilities(this.plugin));
        this.addCheck(new SkinBlinkCheck(this.plugin));
        this.addCheck(new IntaveBanCheck(this.plugin));
        this.addCheck(new MultipleAccountsCheck(this.plugin));
        this.addCheck(new ClientCommand(this.plugin));
        this.addCheck(new TeamingCheck(this.plugin));
        this.addCheck(new ProxyCheck(this.plugin));
    }
    
    void hook(final iIntaveInternalEvent event) {
        if (event instanceof CheckableMoveEvent) {
            for (final IntaveCheck i : this.getAllChecks()) {
                if (!((CheckableMoveEvent)event).isCancelled() && i.isActivated()) {
                    i.onCheckableMove((CheckableMoveEvent)event);
                }
            }
        }
        else if (event instanceof CheckableVelocityEvent) {
            for (final IntaveCheck i : this.getAllChecks()) {
                if (i.isActivated()) {
                    i.onCheckableVelocity((CheckableVelocityEvent)event);
                }
            }
        }
        else {
            if (!(event instanceof CheckableDamageEntityEvent)) {
                throw new IntaveInternalException("Unknown event was hooked to intaves registry! ClassName: " + event.getClass().getSimpleName());
            }
            for (final IntaveCheck i : this.getAllChecks()) {
                if (!((CheckableDamageEntityEvent)event).isCancelled() && i.isActivated()) {
                    i.onCheckableDamageEntity((CheckableDamageEntityEvent)event);
                }
            }
        }
    }
    
    public void handleSystemShutdown() {
        this.getAllChecks().stream().filter(IntaveCheck::isActivated).forEachOrdered(IntaveCheck::onSystemShutdown);
        this.stop();
    }
    
    private void stop() {
        this.checkHashMap.clear();
    }
    
    private void addCheck(final IntaveCheck intaveCheck) {
        final boolean ac = this.plugin.getConfig().getBoolean(intaveCheck.getConfigPath() + ".enabled");
        intaveCheck.setActivated(ac);
        this.checkHashMap.add(intaveCheck);
    }
    
    public void removeCheck(final IntaveCheck intaveCheck) {
        this.checkHashMap.remove(intaveCheck);
    }
    
    public final IntaveCheck getCheck(final String checkname) {
        for (final IntaveCheck check : this.getAllChecks()) {
            if (check.getName().equalsIgnoreCase(checkname)) {
                return check;
            }
        }
        throw new IntaveInternalException(new NullPointerException("Check " + checkname + " couldn't be found."));
    }
    
    public final boolean checkIsLoaded(final String checkname) {
        return this.getAllChecks().stream().anyMatch(check -> check.getName().equalsIgnoreCase(checkname));
    }
    
    public final List<IntaveCheck> getAllChecksOf(final CheatCategory cheatCategory) {
        return this.getAllChecks().stream().filter(intaveCheck -> intaveCheck.getCategory().equals(cheatCategory)).collect((Collector<? super Object, ?, List<IntaveCheck>>)Collectors.toList());
    }
    
    private List<IntaveCheck> getAllChecks() {
        return this.checkHashMap;
    }
    
    public final List<String> getAllCheckNames() {
        return this.getAllChecks().stream().map((Function<? super Object, ?>)IntaveCheck::getName).collect((Collector<? super Object, ?, List<String>>)Collectors.toList());
    }
    
    public void reloadChecks() {
        this.handleSystemShutdown();
        this.setup();
    }
}
