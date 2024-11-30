// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.enums;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum CheatCategory
{
    COMBAT(Material.IRON_SWORD, "Combat related cheats, for example: killaura", ChatColor.GRAY + "Combat"), 
    MOVING(Material.FEATHER, "Blocking movement cheats like fly or speed", ChatColor.GRAY + "Movement"), 
    NETWORK(Material.COMMAND, "Network related checks to ensure a stable and safe connection", ChatColor.GRAY + "Network"), 
    WORLD(Material.GRASS, "World checks check world interactions by players", ChatColor.GRAY + "World"), 
    OTHER(Material.PISTON_BASE, "Abstract categorized checks. Chat checks fe.", ChatColor.GRAY + "Other"), 
    FLOAT(Material.COAL_BLOCK, "Not used", "Null"), 
    NULL(Material.COAL_BLOCK, "Not used", "Null");
    
    public Material material;
    public String description;
    public String name;
    
    private CheatCategory(final Material material, final String description, final String name) {
        this.material = material;
        this.description = description;
        this.name = name;
    }
    
    public Material getMaterial() {
        return this.material;
    }
    
    public void setMaterial(final Material material) {
        this.material = material;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(final String description) {
        this.description = description;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
}
