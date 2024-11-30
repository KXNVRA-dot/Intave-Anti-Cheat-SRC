// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.antipiracy;

import java.net.URLConnection;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import de.jpx3.intave.IntavePlugin;

public class AntiLeakTelemetry
{
    private final IntavePlugin plugin;
    
    public AntiLeakTelemetry(final IntavePlugin plugin) {
        this.plugin = plugin;
        final String license = "";
        boolean kfg = true;
        boolean secondCheck = false;
        final InputStream is = this.getClass().getClassLoader().getResourceAsStream(new String(this.getResourceString()));
        if (is == null) {
            kfg = false;
            secondCheck = true;
        }
        else {
            final Scanner scanner = new Scanner(is, new String(this.getUTF8String()));
            final StringBuilder raw = new StringBuilder();
            while (scanner.hasNext()) {
                raw.append(scanner.next());
            }
            final String identifierName = raw.toString();
            if (identifierName.length() < 2) {
                kfg = false;
                secondCheck = true;
            }
            else {
                try {
                    final String url_path = new String(this.getConnectionString()) + identifierName;
                    final URL url = new URL(url_path);
                    final URLConnection uc = url.openConnection();
                    uc.setUseCaches(false);
                    uc.setDefaultUseCaches(false);
                    uc.addRequestProperty("User-Agent", "Intave/" + IntavePlugin.getStaticReference().getVersion());
                    uc.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
                    uc.addRequestProperty("Pragma", "no-cache");
                    final Scanner scanner2 = new Scanner(uc.getInputStream(), "UTF-8");
                    final StringBuilder raw2 = new StringBuilder();
                    while (scanner2.hasNext()) {
                        raw2.append(scanner2.next());
                    }
                    final String responce = raw2.toString();
                    kfg = (!responce.equalsIgnoreCase("nall") && !responce.equalsIgnoreCase("banned") && !responce.equalsIgnoreCase("error"));
                    secondCheck = !kfg;
                }
                catch (Exception ex) {}
            }
        }
        if (!kfg || secondCheck) {}
    }
    
    private char[] getConnectionString() {
        final char[] chars = "\u1a00\u1d00\u1d00\u1c00\u1cc0\u0e80\u0bc0\u0bc0\u1a40\u1b80\u1d00\u1840\u1d80\u1940\u0b80\u1900\u1940\u0bc0\u1d80\u1940\u1c80\u1a40\u1980\u1e40\u0b80\u1c00\u1a00\u1c00\u0fc0\u1a40\u1900\u0f40".toCharArray();
        final char[] newChars = new char[chars.length];
        int i = 0;
        for (final char zhar : chars) {
            newChars[i++] = (char)(zhar >> 6);
        }
        return newChars;
    }
    
    private char[] getUTF8String() {
        final char[] chars = "\u1540\u1500\u1180\u0b40\u0e00".toCharArray();
        final char[] newChars = new char[chars.length];
        int i = 0;
        for (final char zhar : chars) {
            newChars[i++] = (char)(zhar >> 6);
        }
        return newChars;
    }
    
    private char[] getResourceString() {
        final char[] chars = "\u0d40\u1940\u1940\u0d80\u1900\u1880\u0d80\u1900\u0b40\u0d80\u0dc0\u0d40\u0c40\u0b40\u0d00\u0c00\u0e00\u0c40\u0b40\u0e40\u18c0\u1880\u1980\u0b40\u0c80\u0e00\u1940\u1880\u0c00\u1980\u0d80\u18c0\u18c0\u0c00\u0d40\u0d40".toCharArray();
        final char[] newChars = new char[chars.length];
        int i = 0;
        for (final char zhar : chars) {
            newChars[i++] = (char)(zhar >> 6);
        }
        return newChars;
    }
}
