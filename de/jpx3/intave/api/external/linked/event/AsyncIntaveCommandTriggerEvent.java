// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.api.external.linked.event;

import org.bukkit.entity.Player;

public final class AsyncIntaveCommandTriggerEvent extends iIntaveExternalEvent
{
    private Player punished;
    private String command;
    private boolean isWaveExecuted;
    
    public AsyncIntaveCommandTriggerEvent(final Player punished, final String command, final boolean isWaveExecuted) {
        this.punished = punished;
        this.command = command;
        this.isWaveExecuted = isWaveExecuted;
        this.setCancelled(false);
    }
    
    public final Player getPlayer() {
        return this.punished;
    }
    
    public final String getCommand() {
        return this.command;
    }
    
    public void setCommand(final String command) {
        this.command = command;
    }
    
    public void renew(final Player punished, final String command, final boolean isWaveExecuted) {
        this.punished = punished;
        this.command = command;
        this.isWaveExecuted = isWaveExecuted;
        this.setCancelled(false);
    }
    
    public boolean isExecutedByWave() {
        return this.isWaveExecuted;
    }
}
