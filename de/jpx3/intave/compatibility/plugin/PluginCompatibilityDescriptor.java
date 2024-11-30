// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.compatibility.plugin;

import de.jpx3.intave.IntavePlugin;
import java.util.stream.Stream;

public interface PluginCompatibilityDescriptor
{
    String getName();
    
    String[] getKnownProblems();
    
    Stream<String> getKnownProblemsFor(final String p0);
    
    boolean fixProblems(final IntavePlugin p0);
    
    boolean compatibilityAdviced();
    
    boolean compatibilityProblematic();
}
