// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.data;

import java.util.Arrays;
import org.bukkit.Material;

public enum ToolHardness
{
    WOOD(0.75), 
    STONE(0.4), 
    IRON(0.25), 
    DIAMOND(0.2), 
    SHEARS(0.55), 
    GOLD(0.15);
    
    double hardness;
    
    private ToolHardness(final double hard) {
        this.hardness = hard;
    }
    
    public static double getToolHardness(final Material tool) {
        return Arrays.stream(values()).filter(e -> tool.name().contains(e.name())).findFirst().map(e -> e.hardness).orElse(1.5);
    }
}
