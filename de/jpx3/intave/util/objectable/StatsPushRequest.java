// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.objectable;

import java.util.UUID;

public final class StatsPushRequest
{
    private final UUID uuid;
    private final String nbt;
    private final String check;
    private final String reason;
    
    public StatsPushRequest(final UUID uuid, final String nbt, final String check, final String reason) {
        this.uuid = uuid;
        this.nbt = nbt;
        this.check = check;
        this.reason = reason;
    }
    
    public UUID getUuid() {
        return this.uuid;
    }
    
    public String getNBT() {
        return this.nbt;
    }
    
    public String getCheck() {
        return this.check;
    }
    
    public String getReason() {
        return this.reason;
    }
}
