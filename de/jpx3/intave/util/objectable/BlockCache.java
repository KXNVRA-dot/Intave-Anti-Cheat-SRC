// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.objectable;

import java.lang.reflect.InvocationTargetException;
import de.jpx3.intave.api.internal.reflections.Reflections;
import java.lang.reflect.Field;
import de.jpx3.intave.util.data.BlockHardness;
import java.util.Arrays;
import org.bukkit.Material;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class BlockCache
{
    private final Map<Integer, BlockCacheEntry> blockCache;
    
    public BlockCache() {
        this.blockCache = new ConcurrentHashMap<Integer, BlockCacheEntry>();
        this.autoLoadCache();
    }
    
    private void autoLoadCache() {
        final BlockCacheEntry blockCacheEntry;
        Arrays.stream(Material.values()).filter(Material::isBlock).filter(material -> !this.blockCache.containsKey(material.getId())).forEachOrdered(material -> blockCacheEntry = this.blockCache.put(material.getId(), this.loadBlockDataCache(material.name())));
    }
    
    public final BlockCacheEntry getBlockCacheEntry(final Material material) {
        if (!this.blockCache.containsKey(material.getId())) {
            this.blockCache.put(material.getId(), this.loadBlockDataCache(material.name()));
        }
        return this.blockCache.get(material.getId());
    }
    
    private BlockCacheEntry loadBlockDataCache(final String materialname) {
        final Material material = Material.getMaterial(materialname);
        final double hardness = this.getBlockHardness(material);
        return new BlockCacheEntry(material, hardness);
    }
    
    private double getBlockHardness(final Material material) {
        try {
            return BlockHardness.hasBlockHardness(material) ? BlockHardness.getBlockHardness(material) : this.reflectBlockHardness(material);
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    
    private float reflectBlockHardness(final Material material) throws IllegalAccessException {
        if (!material.isBlock()) {
            return 0.0f;
        }
        final int blockId = material.getId();
        final Object nmsBlock = getBlockFromId(blockId);
        try {
            final Field field = nmsBlock.getClass().getDeclaredField("strength");
            field.setAccessible(true);
            return (float)field.get(nmsBlock);
        }
        catch (NoSuchFieldException e) {
            return 0.0f;
        }
    }
    
    public static Object getBlockFromId(final int blockId) {
        Object nmsBlock = null;
        try {
            if (Reflections.useAlternateMethodNames()) {
                final Object blockRegistry = Reflections.getNmsClass("IRegistry").getField("BLOCK").get(null);
                nmsBlock = blockRegistry.getClass().getMethod("fromId", Integer.TYPE).invoke(blockRegistry, blockId);
            }
            else {
                nmsBlock = Reflections.getNmsClass("Block").getMethod("getById", Integer.TYPE).invoke(null, blockId);
            }
        }
        catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException ex2) {
            final ReflectiveOperationException ex;
            final ReflectiveOperationException e = ex;
            e.printStackTrace();
        }
        return nmsBlock;
    }
    
    public class BlockCacheEntry
    {
        private final Material blockMaterial;
        private final double blockHardness;
        
        public BlockCacheEntry(final Material material, final double blockHardness) {
            this.blockMaterial = material;
            this.blockHardness = blockHardness;
        }
        
        public Material getBlockMaterial() {
            return this.blockMaterial;
        }
        
        public double getBlockHardness() {
            return this.blockHardness;
        }
    }
}
