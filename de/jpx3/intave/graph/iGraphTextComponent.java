// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.graph;

import java.awt.Color;

public final class iGraphTextComponent
{
    private String text;
    private int x;
    private int y;
    private Color color;
    
    public iGraphTextComponent(final String text, final int x, final int y) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = Color.WHITE;
    }
    
    public iGraphTextComponent(final String text, final int x, final int y, final Color color) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
    }
    
    public String getText() {
        return this.text;
    }
    
    public int getX() {
        return this.x;
    }
    
    public int getY() {
        return this.y;
    }
    
    public Color getColor() {
        return this.color;
    }
}
