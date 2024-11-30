// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.api.external;

public enum IntavePermission
{
    BYPASS("intave.bypass"), 
    ADMIN_NOTIFY("intave.admin.notify"), 
    ADMIN_SNOTIFY("intave.admin.snotify"), 
    ADMIN_VERBOSE("intave.admin.verbose"), 
    ADMIN_RELOAD("intave.admin.reload"), 
    ADMIN_BOT("intave.admin.bot"), 
    ADMIN_MESSAGE("intave.admin.message"), 
    ADMIN_DEBUG("intave.admin.debug"), 
    @Deprecated
    ADMIN_BROADCAST("intave.admin.broadcast"), 
    ADMIN_WAVE("intave.admin.wave"), 
    ADMIN_GUI("intave.admin.gui"), 
    ADMIN_RESETVL("intave.admin.resetvl"), 
    ADMIN_AQUIREREJOINBLOCK("intave.admin.aqrejoinblock"), 
    COMMAND_USE("intave.command"), 
    PLAYER_SCORE_LOOKUP("intave.admin.playerscorelookup");
    
    private final String bukkitName;
    
    private IntavePermission(final String bukkitName) {
        this.bukkitName = bukkitName;
    }
    
    public final String getBukkitName() {
        return this.bukkitName;
    }
}
