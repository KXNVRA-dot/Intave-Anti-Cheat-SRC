// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.calc;

import java.util.NoSuchElementException;
import org.bukkit.entity.LivingEntity;
import org.bukkit.Location;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.bukkit.World;
import java.util.ArrayList;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Block;
import java.util.List;

public final class IntaveBlockIterator
{
    private int maxDistance;
    private boolean end;
    private final int gridSize = 16777216;
    private final List<Block> cashedOutput;
    private Block[] blockQueue;
    private int currentBlock;
    private int currentDistance;
    private int maxDistanceInt;
    private int secondError;
    private int thirdError;
    private int secondStep;
    private int thirdStep;
    private BlockFace mainFace;
    private BlockFace secondFace;
    private BlockFace thirdFace;
    
    public IntaveBlockIterator() {
        this.end = false;
        this.cashedOutput = new ArrayList<Block>();
        this.blockQueue = new Block[3];
        this.currentBlock = 0;
        this.currentDistance = 0;
    }
    
    public final List<Block> calc(final World world, final Vector start, final Vector direction, final double yOffset, final int maxDistance) {
        this.maxDistance = maxDistance;
        this.clearCache();
        final Vector startClone = start.clone();
        startClone.setY(startClone.getY() + yOffset);
        this.currentDistance = 0;
        double mainDirection = 0.0;
        double secondDirection = 0.0;
        double thirdDirection = 0.0;
        double mainPosition = 0.0;
        double secondPosition = 0.0;
        double thirdPosition = 0.0;
        final Block startBlock = world.getBlockAt(NumberConversions.floor(startClone.getX()), NumberConversions.floor(startClone.getY()), NumberConversions.floor(startClone.getZ()));
        if (this.getXLength(direction) > mainDirection) {
            this.mainFace = this.getXFace(direction);
            mainDirection = this.getXLength(direction);
            mainPosition = this.getXPosition(direction, startClone, startBlock);
            this.secondFace = this.getYFace(direction);
            secondDirection = this.getYLength(direction);
            secondPosition = this.getYPosition(direction, startClone, startBlock);
            this.thirdFace = this.getZFace(direction);
            thirdDirection = this.getZLength(direction);
            thirdPosition = this.getZPosition(direction, startClone, startBlock);
        }
        if (this.getYLength(direction) > mainDirection) {
            this.mainFace = this.getYFace(direction);
            mainDirection = this.getYLength(direction);
            mainPosition = this.getYPosition(direction, startClone, startBlock);
            this.secondFace = this.getZFace(direction);
            secondDirection = this.getZLength(direction);
            secondPosition = this.getZPosition(direction, startClone, startBlock);
            this.thirdFace = this.getXFace(direction);
            thirdDirection = this.getXLength(direction);
            thirdPosition = this.getXPosition(direction, startClone, startBlock);
        }
        if (this.getZLength(direction) > mainDirection) {
            this.mainFace = this.getZFace(direction);
            mainDirection = this.getZLength(direction);
            mainPosition = this.getZPosition(direction, startClone, startBlock);
            this.secondFace = this.getXFace(direction);
            secondDirection = this.getXLength(direction);
            secondPosition = this.getXPosition(direction, startClone, startBlock);
            this.thirdFace = this.getYFace(direction);
            thirdDirection = this.getYLength(direction);
            thirdPosition = this.getYPosition(direction, startClone, startBlock);
        }
        final double d = mainPosition / mainDirection;
        final double secondd = secondPosition - secondDirection * d;
        final double thirdd = thirdPosition - thirdDirection * d;
        this.secondError = NumberConversions.floor(secondd * 1.6777216E7);
        this.secondStep = NumberConversions.round(secondDirection / mainDirection * 1.6777216E7);
        this.thirdError = NumberConversions.floor(thirdd * 1.6777216E7);
        this.thirdStep = NumberConversions.round(thirdDirection / mainDirection * 1.6777216E7);
        if (this.secondError + this.secondStep <= 0) {
            this.secondError = -this.secondStep + 1;
        }
        if (this.thirdError + this.thirdStep <= 0) {
            this.thirdError = -this.thirdStep + 1;
        }
        Block lastBlock = startBlock.getRelative(this.mainFace.getOppositeFace());
        if (this.secondError < 0) {
            this.secondError += 16777216;
            lastBlock = lastBlock.getRelative(this.secondFace.getOppositeFace());
        }
        if (this.thirdError < 0) {
            this.thirdError += 16777216;
            lastBlock = lastBlock.getRelative(this.thirdFace.getOppositeFace());
        }
        this.secondError -= 16777216;
        this.thirdError -= 16777216;
        this.blockQueue[0] = lastBlock;
        this.currentBlock = -1;
        this.scan();
        boolean startBlockFound = false;
        for (int cnt = this.currentBlock; cnt >= 0; --cnt) {
            if (this.blockEquals(this.blockQueue[cnt], startBlock)) {
                this.currentBlock = cnt;
                startBlockFound = true;
                break;
            }
        }
        this.maxDistanceInt = NumberConversions.round(maxDistance / (Math.sqrt(mainDirection * mainDirection + secondDirection * secondDirection + thirdDirection * thirdDirection) / mainDirection));
        this.cashedOutput.clear();
        if (!startBlockFound) {
            return this.cashedOutput;
        }
        do {
            this.cashedOutput.add(this.next());
        } while (this.hasNext());
        return this.cashedOutput;
    }
    
    private void clearCache() {
        this.blockQueue = new Block[3];
        this.end = false;
    }
    
    private boolean blockEquals(final Block a, final Block b) {
        return a.getX() == b.getX() && a.getY() == b.getY() && a.getZ() == b.getZ();
    }
    
    private BlockFace getXFace(final Vector direction) {
        return (direction.getX() > 0.0) ? BlockFace.EAST : BlockFace.WEST;
    }
    
    private BlockFace getYFace(final Vector direction) {
        return (direction.getY() > 0.0) ? BlockFace.UP : BlockFace.DOWN;
    }
    
    private BlockFace getZFace(final Vector direction) {
        return (direction.getZ() > 0.0) ? BlockFace.SOUTH : BlockFace.NORTH;
    }
    
    private double getXLength(final Vector direction) {
        return Math.abs(direction.getX());
    }
    
    private double getYLength(final Vector direction) {
        return Math.abs(direction.getY());
    }
    
    private double getZLength(final Vector direction) {
        return Math.abs(direction.getZ());
    }
    
    private double getPosition(final double direction, final double position, final int blockPosition) {
        return (direction > 0.0) ? (position - blockPosition) : (blockPosition + 1 - position);
    }
    
    private double getXPosition(final Vector direction, final Vector position, final Block block) {
        return this.getPosition(direction.getX(), position.getX(), block.getX());
    }
    
    private double getYPosition(final Vector direction, final Vector position, final Block block) {
        return this.getPosition(direction.getY(), position.getY(), block.getY());
    }
    
    private double getZPosition(final Vector direction, final Vector position, final Block block) {
        return this.getPosition(direction.getZ(), position.getZ(), block.getZ());
    }
    
    public final List<Block> calc(final Location loc, final double yOffset, final int maxDistance) {
        if (loc == null) {
            return new ArrayList<Block>();
        }
        return this.calc(loc.getWorld(), loc.toVector(), loc.getDirection(), yOffset, maxDistance);
    }
    
    public final List<Block> calc(final Location loc, final double yOffset) {
        return this.calc(loc.getWorld(), loc.toVector(), loc.getDirection(), yOffset, 3);
    }
    
    public final List<Block> calc(final Location loc) {
        return this.calc(loc, 0.0);
    }
    
    public final List<Block> calc(final LivingEntity entity, final int maxDistance) {
        return this.calc(entity.getLocation(), entity.getEyeHeight(), maxDistance);
    }
    
    public final List<Block> calc(final LivingEntity entity) {
        return this.calc(entity, 3);
    }
    
    private boolean hasNext() {
        this.scan();
        return this.currentBlock != -1;
    }
    
    private Block next() {
        this.scan();
        if (this.currentBlock <= -1) {
            throw new NoSuchElementException();
        }
        return this.blockQueue[this.currentBlock--];
    }
    
    private void scan() {
        if (this.currentBlock >= 0) {
            return;
        }
        if (this.maxDistance != 0 && this.currentDistance > this.maxDistanceInt) {
            this.end = true;
            return;
        }
        if (this.end) {
            return;
        }
        ++this.currentDistance;
        this.secondError += this.secondStep;
        this.thirdError += this.thirdStep;
        if (this.secondError > 0 && this.thirdError > 0) {
            this.blockQueue[2] = this.blockQueue[0].getRelative(this.mainFace);
            if (this.secondStep * (long)this.thirdError < this.thirdStep * (long)this.secondError) {
                this.blockQueue[1] = this.blockQueue[2].getRelative(this.secondFace);
                this.blockQueue[0] = this.blockQueue[1].getRelative(this.thirdFace);
            }
            else {
                this.blockQueue[1] = this.blockQueue[2].getRelative(this.thirdFace);
                this.blockQueue[0] = this.blockQueue[1].getRelative(this.secondFace);
            }
            this.thirdError -= 16777216;
            this.secondError -= 16777216;
            this.currentBlock = 2;
        }
        else if (this.secondError > 0) {
            this.blockQueue[1] = this.blockQueue[0].getRelative(this.mainFace);
            this.blockQueue[0] = this.blockQueue[1].getRelative(this.secondFace);
            this.secondError -= 16777216;
            this.currentBlock = 1;
        }
        else if (this.thirdError > 0) {
            this.blockQueue[1] = this.blockQueue[0].getRelative(this.mainFace);
            this.blockQueue[0] = this.blockQueue[1].getRelative(this.thirdFace);
            this.thirdError -= 16777216;
            this.currentBlock = 1;
        }
        else {
            this.blockQueue[0] = this.blockQueue[0].getRelative(this.mainFace);
            this.currentBlock = 0;
        }
    }
}
