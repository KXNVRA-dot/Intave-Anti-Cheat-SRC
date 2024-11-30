// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.async;

import java.util.concurrent.RejectedExecutionException;
import de.jpx3.intave.antipiracy.IIUA;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.Executors;
import java.util.Queue;
import java.util.Map;
import java.util.concurrent.Executor;

public final class OrderingExecutor implements Executor
{
    private final Executor delegate;
    private final Executor telemetryDelegate;
    private final Map<Object, Queue<Runnable>> keyedTasks;
    private final OrderedTask orderedTaskCache;
    
    public OrderingExecutor(final Executor delegate) {
        this.telemetryDelegate = Executors.newSingleThreadExecutor(new IntaveTelemetryThreadFactory());
        this.keyedTasks = new ConcurrentHashMap<Object, Queue<Runnable>>();
        this.orderedTaskCache = new OrderedTask(null, null, null);
        this.delegate = delegate;
    }
    
    @Override
    public void execute(final Runnable task) {
        this.delegate.execute(task);
    }
    
    public final void execute(final Runnable task, final Object key) {
        if (key == null) {
            this.execute(task);
            return;
        }
        Queue<Runnable> dependencyQueue;
        final boolean first;
        final Runnable wrappedTask;
        this.telemetryDelegate.execute(() -> {
            synchronized (this.keyedTasks) {
                dependencyQueue = this.keyedTasks.getOrDefault(key, null);
                first = (dependencyQueue == null);
                if (first) {
                    dependencyQueue = new LinkedList<Runnable>();
                    this.keyedTasks.put(key, dependencyQueue);
                }
                wrappedTask = this.wrap(task, dependencyQueue, key);
                if (!first) {
                    dependencyQueue.add(wrappedTask);
                }
            }
            if (first) {
                this.delegate.execute(wrappedTask);
            }
        });
    }
    
    public final void removeAllTasks(final Object key) {
        synchronized (this.keyedTasks) {
            if (this.keyedTasks.containsKey(key)) {
                this.keyedTasks.get(key).clear();
            }
        }
    }
    
    public final boolean hasTasksLeft(final Object key) {
        synchronized (this.keyedTasks) {
            return this.keyedTasks.containsKey(key) && this.keyedTasks.get(key).size() > 0;
        }
    }
    
    public final void removeAllTasks() {
        synchronized (this.keyedTasks) {
            this.keyedTasks.keySet().forEach(object -> this.keyedTasks.get(object).clear());
            this.keyedTasks.clear();
        }
    }
    
    private Runnable wrap(final Runnable task, final Queue<Runnable> dependencyQueue, final Object key) {
        synchronized (this.orderedTaskCache) {
            final OrderedTask orderedTaskClone = this.orderedTaskCache.clone();
            orderedTaskClone.setTask(task);
            orderedTaskClone.setDependencyQueue(dependencyQueue);
            orderedTaskClone.setKey(key);
            return orderedTaskClone;
        }
    }
    
    public final Executor getDelegate() {
        return this.delegate;
    }
    
    public static class IntaveThreadFactory implements ThreadFactory
    {
        private static final AtomicInteger poolNumber;
        private final ThreadGroup group;
        private final AtomicInteger threadNumber;
        private final String namePrefix;
        
        public IntaveThreadFactory() {
            this.threadNumber = new AtomicInteger(1);
            final SecurityManager s = System.getSecurityManager();
            this.group = ((s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup());
            this.namePrefix = "Intave-TaskWorker [" + IntaveThreadFactory.poolNumber.getAndIncrement() + "/";
        }
        
        @Override
        public Thread newThread(final Runnable r) {
            final Thread t = new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement() + "]", 0L);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            t.setPriority(10);
            return t;
        }
        
        static {
            poolNumber = new AtomicInteger(1);
        }
    }
    
    public static class IntaveTelemetryThreadFactory implements ThreadFactory
    {
        private static final AtomicInteger poolNumber;
        private final ThreadGroup group;
        private final AtomicInteger threadNumber;
        private final String namePrefix;
        
        public IntaveTelemetryThreadFactory() {
            this.threadNumber = new AtomicInteger(1);
            final SecurityManager s = System.getSecurityManager();
            this.group = ((s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup());
            this.namePrefix = "Intave-Telemetry [" + IntaveTelemetryThreadFactory.poolNumber.getAndIncrement() + "/";
        }
        
        @Override
        public Thread newThread(final Runnable r) {
            final Thread t = new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement() + "]", 0L);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            t.setPriority(10);
            return t;
        }
        
        static {
            poolNumber = new AtomicInteger(1);
        }
    }
    
    class OrderedTask implements Runnable, Cloneable
    {
        private Queue<Runnable> dependencyQueue;
        private Runnable task;
        private Object key;
        private long initialized;
        
        OrderedTask(final Runnable task, final Queue<Runnable> dependencyQueue, final Object key) {
            this.initialized = IIUA.getCurrentTimeMillis();
            this.task = task;
            this.dependencyQueue = dependencyQueue;
            this.key = key;
        }
        
        final Queue<Runnable> getDependencyQueue() {
            return this.dependencyQueue;
        }
        
        final void setDependencyQueue(final Queue<Runnable> dependencyQueue) {
            this.dependencyQueue = dependencyQueue;
        }
        
        final Runnable getTask() {
            return this.task;
        }
        
        final void setTask(final Runnable task) {
            this.task = task;
        }
        
        final Object getKey() {
            return this.key;
        }
        
        final void setKey(final Object key) {
            this.key = key;
        }
        
        @Override
        public final void run() {
            try {
                this.getTask().run();
                Runnable nextTask = null;
                synchronized (OrderingExecutor.this.keyedTasks) {
                    if (this.dependencyQueue.isEmpty()) {
                        OrderingExecutor.this.keyedTasks.remove(this.key);
                    }
                    else {
                        nextTask = this.dependencyQueue.poll();
                    }
                }
                if (nextTask != null) {
                    try {
                        OrderingExecutor.this.delegate.execute(nextTask);
                    }
                    catch (RejectedExecutionException ex) {}
                }
            }
            finally {
                Runnable nextTask2 = null;
                synchronized (OrderingExecutor.this.keyedTasks) {
                    if (this.dependencyQueue.isEmpty()) {
                        OrderingExecutor.this.keyedTasks.remove(this.key);
                    }
                    else {
                        nextTask2 = this.dependencyQueue.poll();
                    }
                }
                if (nextTask2 != null) {
                    try {
                        OrderingExecutor.this.delegate.execute(nextTask2);
                    }
                    catch (RejectedExecutionException ex2) {}
                }
            }
        }
        
        public final OrderedTask clone() {
            try {
                return (OrderedTask)super.clone();
            }
            catch (CloneNotSupportedException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
