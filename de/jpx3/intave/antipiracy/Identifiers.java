// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.antipiracy;

import java.io.InputStream;
import java.util.Scanner;
import de.jpx3.intave.IntavePlugin;

public final class Identifiers
{
    public static String fakeCerificateBits;
    
    public static String spigot() {
        return IntavePlugin.getStaticReference().getIntaveVerificationName();
    }
    
    public static String resource() {
        if (Identifiers.fakeCerificateBits.isEmpty()) {
            Identifiers.fakeCerificateBits = rawHash().substring(4, 10);
        }
        return Identifiers.fakeCerificateBits;
    }
    
    private static String rawHash() {
        final InputStream is = Identifiers.class.getClassLoader().getResourceAsStream("5ee6db6d-6751-4081-9cbf-28eb0f6cc055");
        if (is == null) {
            return "error";
        }
        final Scanner scanner = new Scanner(is, "UTF-8");
        final StringBuilder raw = new StringBuilder();
        while (scanner.hasNext()) {
            raw.append(scanner.next());
        }
        return raw.toString();
    }
    
    static {
        Identifiers.fakeCerificateBits = "";
    }
}
