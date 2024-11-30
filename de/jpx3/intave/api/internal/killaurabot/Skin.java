// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.api.internal.killaurabot;

import java.util.Iterator;
import java.net.URLConnection;
import java.util.Scanner;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import java.net.URL;

public final class Skin
{
    private String uuid;
    private String name;
    private String value;
    private String signatur;
    
    public Skin(final String uuid) {
        this.uuid = uuid;
        this.load();
    }
    
    private void load() {
        try {
            final URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + this.uuid + "?unsigned=false");
            final URLConnection uc = url.openConnection();
            uc.setUseCaches(false);
            uc.setDefaultUseCaches(false);
            uc.addRequestProperty("User-Agent", "Mozilla/5.0");
            uc.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
            uc.addRequestProperty("Pragma", "no-cache");
            for (final Object property1 : (JSONArray)((JSONObject)new JSONParser().parse(new Scanner(uc.getInputStream(), "UTF-8").useDelimiter("\\A").next())).get((Object)"properties")) {
                try {
                    final JSONObject property2 = (JSONObject)property1;
                    this.name = (String)property2.get((Object)"name");
                    this.value = (String)property2.get((Object)"value");
                    this.signatur = (property2.containsKey((Object)"signature") ? ((String)property2.get((Object)"signature")) : null);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e2) {
            e2.printStackTrace();
        }
    }
    
    public String getSkinValue() {
        return this.value;
    }
    
    public String getSkinName() {
        return this.name;
    }
    
    public String getSkinSignatur() {
        return this.signatur;
    }
}
