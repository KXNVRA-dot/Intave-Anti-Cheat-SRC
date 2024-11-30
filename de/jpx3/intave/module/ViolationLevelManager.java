// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.module;

import de.jpx3.intave.antipiracy.IIUA;
import org.bukkit.entity.Player;
import de.jpx3.intave.IntavePlugin;

public final class ViolationLevelManager
{
    private final IntavePlugin plugin;
    
    public ViolationLevelManager(final IntavePlugin plugin) {
        this.plugin = plugin;
    }
    
    public void addViolationLevel(final Player p, final int level, final String modulename) {
        this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues().violationLevels.put(modulename.toLowerCase(), this.getViolationLevel(p, modulename) + level);
    }
    
    public void takeViolationLevel(final Player p, final int level, final String modulename) {
        if (this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues().violationLevels.containsKey(modulename.toLowerCase())) {
            final int currentVL = this.getViolationLevel(p, modulename);
            if (currentVL - level >= 0) {
                this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues().violationLevels.put(modulename.toLowerCase(), currentVL - level);
            }
            else if (currentVL < level && currentVL > 0) {
                this.resetViolationLevel(p, modulename);
            }
        }
    }
    
    public void resetViolationLevel(final Player p, final String modulename) {
        this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues().violationLevels.put(modulename.toLowerCase(), 0);
    }
    
    public void reset(final Player p) {
        this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).clearViolations();
    }
    
    public int getViolationLevel(final Player p, final String modulename) {
        return this.plugin.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues().violationLevels.getOrDefault(modulename.toLowerCase(), 0);
    }
}
