// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.compatibility.plugin.plugins;

import de.jpx3.intave.IntavePlugin;
import java.util.stream.Stream;
import de.jpx3.intave.compatibility.plugin.PluginCompatibilityDescriptor;

public class ProtocolLibDescriptor implements PluginCompatibilityDescriptor
{
    @Override
    public String getName() {
        return "ProtocolLib";
    }
    
    @Override
    public String[] getKnownProblems() {
        return new String[] { "plugin might not be reload-friendly (server reload problematic)" };
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
        return true;
    }
    
    @Override
    public boolean compatibilityProblematic() {
        return false;
    }
}
