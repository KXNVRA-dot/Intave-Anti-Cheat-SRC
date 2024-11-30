// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.async;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.concurrent.Executors;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.UUID;
import java.util.List;
import java.util.Queue;
import java.util.Map;

public final class IntaveAsyncThreadPool<K>
{
    private final Map<K, Queue<Runnable>> queue;
    private final Map<K, Boolean> locked;
    private final List<Thread> threadList;
    private final List<UUID> threadSignatures;
    public int corePool;
    public int maxPool;
    private final ThreadFactory threadFactory;
    private UUID poolSignature;
    
    public IntaveAsyncThreadPool() {
        this.queue = new ConcurrentHashMap<K, Queue<Runnable>>();
        this.locked = new ConcurrentHashMap<K, Boolean>();
        this.threadList = new CopyOnWriteArrayList<Thread>();
        this.threadSignatures = new CopyOnWriteArrayList<UUID>();
        this.corePool = 0;
        this.maxPool = 8;
        this.threadFactory = Executors.defaultThreadFactory();
        this.poolSignature = UUID.randomUUID();
    }
    
    private void addThread() {
        final UUID threadSig;
        final UUID sig;
        final long start;
        final long duration;
        final BiConsumer<K, Runnable> process;
        final Thread newThread = this.threadFactory.newThread(() -> {
            threadSig = UUID.randomUUID();
            sig = UUID.fromString(this.poolSignature.toString());
            this.threadSignatures.add(threadSig);
            process = ((k, randomRunnable) -> {
                start = System.nanoTime();
                randomRunnable.run();
                this.locked.put(k, false);
                duration = System.nanoTime() - start;
                return;
            });
            while (sig.equals(this.poolSignature) && this.threadSignatures.contains(threadSig)) {
                if (!this.applyRandomRunnable(process)) {
                    try {
                        Thread.sleep(5L);
                    }
                    catch (InterruptedException ex) {}
                }
            }
            return;
        });
        this.threadList.add(newThread);
        newThread.start();
    }
    
    private synchronized void removeThread() {
        this.threadSignatures.remove(0);
    }
    
    private synchronized boolean applyRandomRunnable(final BiConsumer<K, Runnable> runnableBiConsumer) {
        boolean foundEntry = false;
        for (final Map.Entry<K, Queue<Runnable>> next : this.queue.entrySet()) {
            if (!next.getValue().isEmpty()) {
                if (this.locked.containsKey(next.getKey()) && this.locked.get(next.getKey())) {
                    continue;
                }
                runnableBiConsumer.accept(next.getKey(), this.queue.get(next.getKey()).poll());
                foundEntry = true;
                break;
            }
        }
        return foundEntry;
    }
    
    public synchronized boolean hasTasksLeft(final K key) {
        synchronized (this.queue) {
            this.validatQueueEntry(key);
            return !this.queue.get(key).isEmpty();
        }
    }
    
    public synchronized void execute(final K key, final Runnable runnable) {
        synchronized (this.queue) {
            this.validatQueueEntry(key);
            this.queue.get(key).offer(runnable);
        }
    }
    
    public synchronized void clear(final K key) {
        synchronized (this.queue) {
            if (this.queue.containsKey(key)) {
                this.queue.get(key).clear();
            }
        }
    }
    
    private void validatQueueEntry(final K key) {
        if (!this.queue.containsKey(key)) {
            this.queue.put(key, new LinkedList<Runnable>());
        }
    }
    
    public void shutdown() {
        this.clearAll();
        this.poolSignature = UUID.randomUUID();
        this.threadSignatures.clear();
    }
    
    public void clearAll() {
        synchronized (this.queue) {
            this.queue.clear();
        }
    }
}
