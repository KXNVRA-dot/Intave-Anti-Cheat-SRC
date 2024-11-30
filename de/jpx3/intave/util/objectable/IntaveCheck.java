// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.objectable;

import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.util.event.CheckableDamageEntityEvent;
import de.jpx3.intave.util.event.CheckableVelocityEvent;
import de.jpx3.intave.util.event.CheckableMoveEvent;
import de.jpx3.intave.util.enums.CheatCategory;

public class IntaveCheck
{
    private String checkName;
    private CheatCategory cheatCategory;
    private String checkDescription;
    private boolean isCheckActivated;
    private boolean isVLReduceCustom;
    
    public IntaveCheck(final String check_name, final CheatCategory check_category) {
        this.checkDescription = "";
        this.isCheckActivated = true;
        this.isVLReduceCustom = false;
        this.checkName = check_name;
        this.cheatCategory = check_category;
    }
    
    public void onCheckableMove(final CheckableMoveEvent e) {
    }
    
    public void onCheckableVelocity(final CheckableVelocityEvent e) {
    }
    
    public void onCheckableDamageEntity(final CheckableDamageEntityEvent e) {
    }
    
    public void onSystemShutdown() {
    }
    
    public final String getName() {
        return this.checkName;
    }
    
    public final String getConfigPath() {
        return IntavePlugin.getStaticReference().getThresholdsManager().getConfigPathFromName(this.getName());
    }
    
    public final String getDescription() {
        return this.checkDescription;
    }
    
    public final CheatCategory getCategory() {
        return this.cheatCategory;
    }
    
    public final boolean isActivated() {
        return this.isCheckActivated;
    }
    
    public void setActivated(final boolean active) {
        this.isCheckActivated = active;
    }
    
    public boolean isVLReduceCustom() {
        return this.isVLReduceCustom;
    }
    
    public void setVLRedCust(final boolean custom) {
        this.isVLReduceCustom = custom;
    }
    
    public void setDescription(final String newDescription) {
        this.checkDescription = newDescription;
    }
}
