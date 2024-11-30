// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.antipiracy;

import java.net.URLConnection;
import de.jpx3.intave.IntavePlugin;
import java.net.URL;
import java.io.InputStream;
import java.util.Scanner;

public final class IntaveLocalVerification
{
    private String rawHash() {
        final InputStream is = this.getClass().getClassLoader().getResourceAsStream("5ee6db6d-6751-4081-9cbf-28eb0f6cc055");
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
    
    public final String getNewestVersionName() {
        try {
            final String url_path = "https://intave.de/api/latestversion";
            final URL url = new URL("https://intave.de/api/latestversion");
            final URLConnection uc = url.openConnection();
            uc.setUseCaches(false);
            uc.setDefaultUseCaches(false);
            uc.addRequestProperty("User-Agent", "Intave/" + IntavePlugin.getStaticReference().getVersion());
            uc.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
            uc.addRequestProperty("Pragma", "no-cache");
            final Scanner scanner = new Scanner(uc.getInputStream(), "UTF-8");
            final StringBuilder raw = new StringBuilder();
            while (scanner.hasNext()) {
                raw.append(scanner.next());
            }
            return raw.toString();
        }
        catch (Exception f) {
            return "";
        }
    }
    
    public final String getIdFromFile() {
        try {
            final String identifierName = this.rawHash();
            if (identifierName.length() < 2) {
                return "null";
            }
            final String url_path = "https://intave.de/verify.php?id=" + identifierName;
            final URL url = new URL(url_path);
            final URLConnection uc = url.openConnection();
            uc.setUseCaches(false);
            uc.setDefaultUseCaches(false);
            uc.addRequestProperty("User-Agent", "Intave/" + IntavePlugin.getStaticReference().getVersion());
            uc.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
            uc.addRequestProperty("Pragma", "no-cache");
            final Scanner scanner = new Scanner(uc.getInputStream(), "UTF-8");
            final StringBuilder raw = new StringBuilder();
            while (scanner.hasNext()) {
                raw.append(scanner.next());
            }
            return raw.toString();
        }
        catch (Exception f) {
            return "nall";
        }
    }
    
    public final int getIntaveVerification() {
        final String id = this.getIdFromFile();
        if (id.equalsIgnoreCase("error")) {
            return 2;
        }
        if (id.equalsIgnoreCase("null")) {
            return 2;
        }
        if (id.equalsIgnoreCase("banned")) {
            return 3;
        }
        if (id.equalsIgnoreCase("nall")) {
            return 4;
        }
        if (id.equalsIgnoreCase("outdated")) {
            return 5;
        }
        if (id.equalsIgnoreCase("newuse")) {
            return 6;
        }
        return (id.length() > 2) ? 1 : 4;
    }
}
