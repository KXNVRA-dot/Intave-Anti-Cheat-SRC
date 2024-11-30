// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.api.internal.reflections;

import java.lang.reflect.Field;

public final class SaveField
{
    private Field f;
    
    public SaveField(final Field f) {
        try {
            f.setAccessible(true);
            this.f = f;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public Object get(final Object instance) {
        try {
            return this.f.get(instance);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void set(final Object instance, final Object value, final boolean stackTrace) {
        try {
            this.f.set(instance, value);
        }
        catch (Exception e) {
            if (stackTrace) {
                e.printStackTrace();
            }
        }
    }
}
