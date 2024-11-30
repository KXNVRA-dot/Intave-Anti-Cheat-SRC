// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.api.internal.reflections;

import java.lang.reflect.Method;

public final class SaveMethod
{
    private Method m;
    
    public SaveMethod(final Method m) {
        try {
            m.setAccessible(true);
            this.m = m;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public Object invoke(final Object instance, final Boolean stackTrace, final Object... args) {
        try {
            return this.m.invoke(instance, args);
        }
        catch (Exception e) {
            if (stackTrace) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
