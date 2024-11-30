// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.objectable;

import java.util.Arrays;
import java.util.stream.Collector;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.ArrayList;
import java.util.Random;

public final class ReplicatedRandom extends Random
{
    public boolean replicateState(final int firstNextInt, final int secondNextInt) {
        return this.replicateState(firstNextInt, 32, secondNextInt, 32);
    }
    
    public boolean replicateState(final float firstNextFloat, final float secondNextFloat) {
        return this.replicateState((int)(firstNextFloat * 1.6777216E7f), 24, (int)(secondNextFloat * 1.6777216E7f), 24);
    }
    
    public boolean replicateState(final int nextN, final int n, final int nextM, final int m) {
        final long multiplier = 25214903917L;
        final long addend = 11L;
        final long mask = 281474976710655L;
        final long upperMOf48Mask = (1L << m) - 1L << 48 - m;
        final long oldSeedUpperN = (long)nextN << 48 - n & 0xFFFFFFFFFFFFL;
        final long newSeedUpperM = (long)nextM << 48 - m & 0xFFFFFFFFFFFFL;
        final ArrayList<Long> possibleSeeds = LongStream.rangeClosed(oldSeedUpperN, oldSeedUpperN | (1L << 48 - n) - 1L).map(oldSeed -> oldSeed * 25214903917L + 11L & 0xFFFFFFFFFFFFL).filter(newSeed -> (newSeed & upperMOf48Mask) == newSeedUpperM).boxed().collect((Collector<? super Long, ?, ArrayList<Long>>)Collectors.toCollection((Supplier<R>)ArrayList::new));
        if (possibleSeeds.size() == 1) {
            this.setSeed((long)possibleSeeds.get(0) ^ 0x5DEECE66DL);
            return true;
        }
        if (possibleSeeds.size() >= 1) {
            System.out.println("Didn't find a unique seed. Possible seeds were: " + Arrays.toString(possibleSeeds.toArray()));
            return false;
        }
        System.out.println("Failed to find seed!");
        return false;
    }
}
