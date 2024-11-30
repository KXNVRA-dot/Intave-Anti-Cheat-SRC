// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.async;

import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.CompletableFuture;
import java.util.Map;

final class GroupExecutorService<K>
{
    private final Map<K, CompletableFuture<?>> lastFuturePerGroup;
    private final ExecutorService executor;
    private boolean isShutdown;
    
    GroupExecutorService(final ExecutorService executor) {
        this.lastFuturePerGroup = new ConcurrentHashMap<K, CompletableFuture<?>>();
        if (executor == null) {
            throw new IllegalArgumentException();
        }
        this.executor = executor;
    }
    
    void shutdown() {
        this.isShutdown = true;
    }
    
    boolean isShutdown() {
        return this.isShutdown;
    }
    
    public final <T> Future<T> submit(final K groupKey, final Callable<T> task) {
        if (this.isShutdown) {
            throw new RejectedExecutionException();
        }
        final CompletableFuture<?> lastFuture = this.lastFuturePerGroup.computeIfAbsent(groupKey, (Function<? super K, ? extends CompletableFuture<?>>)CompletableFuture::completedFuture);
        final CompletableFuture<T> nextFuture = lastFuture.thenApplyAsync((Function<?, ? extends T>)callingNext((Callable<? extends U>)task), (Executor)this.executor);
        this.lastFuturePerGroup.put(groupKey, nextFuture);
        nextFuture.whenComplete((result, error) -> {
            if (this.isShutdown && this.lastFuturePerGroup.values().stream().allMatch(Future::isDone)) {
                this.executor.shutdown();
            }
            return;
        });
        return nextFuture;
    }
    
    public final Future<?> submit(final K groupKey, final Runnable task) {
        return this.submit(groupKey, () -> {
            task.run();
            return null;
        });
    }
    
    private static <T> Function<Object, T> callingNext(final Callable<T> task) {
        return previousResult -> {
            try {
                return task.call();
            }
            catch (Exception ex) {
                throw new CompletionException(ex);
            }
        };
    }
}
