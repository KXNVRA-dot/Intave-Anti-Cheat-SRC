// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.module;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import java.io.IOException;
import java.io.File;

public final class FileManager
{
    public final void createNewFile(final String filename, final String path) {
        final File file = new File("plugins/" + path, filename);
        if (file.exists()) {
            return;
        }
        try {
            file.createNewFile();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public final File getFile(final String filename, final String path) {
        return new File("plugins/" + path, filename);
    }
    
    public final void deleteFile(final String filename, final String path) {
        final File file = new File("plugins/" + path, filename);
        file.delete();
    }
    
    public final FileConfiguration getConfiguration(final String filename, final String path) {
        return (FileConfiguration)YamlConfiguration.loadConfiguration(this.getFile(filename, path));
    }
}
