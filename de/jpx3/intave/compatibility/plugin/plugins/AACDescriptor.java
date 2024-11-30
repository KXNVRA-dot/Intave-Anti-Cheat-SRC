// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.compatibility.plugin.plugins;

import de.jpx3.intave.module.EventManager;
import de.jpx3.intave.check.connection.MoveCheck;
import de.jpx3.intave.IntavePlugin;
import java.util.stream.Stream;
import de.jpx3.intave.compatibility.plugin.PluginCompatibilityDescriptor;

public class AACDescriptor implements PluginCompatibilityDescriptor
{
    @Override
    public String getName() {
        return "AAC";
    }
    
    @Override
    public String[] getKnownProblems() {
        return new String[] { "plugin rewrites movement packets to an illegal state (nofall will flag false)", "plugin is defined as anticheat (causing slow violation-level-system)", "plugin doesn't accept async teleportations (will spam errors)" };
    }
    
    @Override
    public Stream<String> getKnownProblemsFor(final String version) {
        return null;
    }
    
    @Override
    public boolean fixProblems(final IntavePlugin intaveInstance) {
        intaveInstance.getCompatibiliyTelemetryManager().log("   Compatibility Fix: Disabling check \"NoFall\"...");
        intaveInstance.getCheckManager().getCheck("NoFall").setActivated(false);
        intaveInstance.getCompatibiliyTelemetryManager().log("   Compatibility Fix: Forcing synced teleportations... (dangerous)");
        MoveCheck.ONLY_SYNC_TP = true;
        intaveInstance.getCompatibiliyTelemetryManager().log("   Compatibility Fix: Forcing internal sync... (dangerous & performance-heavy)");
        return EventManager.handleMoveEventsSync = true;
    }
    
    @Override
    public boolean compatibilityAdviced() {
        return false;
    }
    
    @Override
    public boolean compatibilityProblematic() {
        return true;
    }
}
