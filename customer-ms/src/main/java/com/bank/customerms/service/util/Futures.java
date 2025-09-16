package com.bank.customerms.service.util;

import java.util.concurrent.*;

public final class Futures {
    private static final ExecutorService EXEC = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors()));

    private Futures() {}

    public static Executor executor() { return EXEC; }

    public static <T> CompletableFuture<T> supply(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try { return task.call(); }
            catch (Exception e) { throw new CompletionException(e); }
        }, EXEC);
    }
}
