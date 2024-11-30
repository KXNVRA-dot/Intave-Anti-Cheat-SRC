// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave;

import de.jpx3.intave.api.external.IntaveHandle;

public final class IntaveAPIAccessGate
{
    public static final boolean isIntaveLoaded() {
        return IntavePlugin.getStaticReference().isLoaded;
    }
    
    public static final IntaveHandle getAPIReflector() {
        if (!isIntaveLoaded()) {
            throw new IllegalStateException();
        }
        return IntavePlugin.getStaticReference().getIntaveHandleAPI();
    }
}
