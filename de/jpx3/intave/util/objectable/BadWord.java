// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.objectable;

public final class BadWord
{
    private final String word;
    private final boolean needs_verbally_abused;
    private final int vl;
    private final boolean needsSpaceBefore;
    
    public BadWord(final String word, final boolean needs_verbally_abused, final int vl) {
        this.word = word;
        this.needs_verbally_abused = needs_verbally_abused;
        this.vl = vl;
        this.needsSpaceBefore = false;
    }
    
    public BadWord(final String word, final boolean needs_verbally_abused, final int vl, final boolean needsSpaceBefore) {
        this.word = word;
        this.vl = vl;
        this.needs_verbally_abused = needs_verbally_abused;
        this.needsSpaceBefore = needsSpaceBefore;
    }
    
    public final String getBadWord() {
        return this.word;
    }
    
    public final boolean isVerballyAbusedNeeded() {
        return this.needs_verbally_abused;
    }
    
    public final boolean isSpaceBeforeNeeded() {
        return this.needsSpaceBefore;
    }
    
    public final int getVLSummant() {
        return this.vl;
    }
}
