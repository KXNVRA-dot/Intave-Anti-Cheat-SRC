// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.stat;

import de.jpx3.intave.antipiracy.IIUA;

public final class StatisticValue
{
    private final Number value;
    private final long timestamp;
    
    public StatisticValue(final Number value) {
        this.value = value;
        this.timestamp = IIUA.getCurrentTimeMillis();
    }
    
    public Number getValue() {
        return this.value;
    }
    
    public long getTimeStamp() {
        return this.timestamp;
    }
}
