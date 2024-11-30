// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.api.external;

import de.jpx3.intave.api.external.linked.exceptions.IntaveInternalException;

public interface IntaveHandle
{
    void setPermissionOverrideHook(final IntavePermissionOverrideHook p0);
    
    TelicCheckMirror getCheckMirrorOf(final String p0) throws IntaveInternalException, NullPointerException;
}
