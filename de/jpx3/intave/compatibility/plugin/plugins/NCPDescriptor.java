// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.compatibility.plugin.plugins;

import de.jpx3.intave.IntavePlugin;
import java.util.stream.Stream;
import de.jpx3.intave.compatibility.plugin.PluginCompatibilityDescriptor;

public class NCPDescriptor implements PluginCompatibilityDescriptor
{
    @Override
    public final String getName() {
        return "NoCheatPlus";
    }
    
    @Override
    public String[] getKnownProblems() {
        return new String[] { "plugin is defined as anticheat (causing slow violation-level-system)" };
    }
    
    @Override
    public Stream<String> getKnownProblemsFor(final String version) {
        return null;
    }
    
    @Override
    public boolean fixProblems(final IntavePlugin intaveInstance) {
        return false;
    }
    
    @Override
    public boolean compatibilityAdviced() {
        return false;
    }
    
    @Override
    public boolean compatibilityProblematic() {
        return false;
    }
}
