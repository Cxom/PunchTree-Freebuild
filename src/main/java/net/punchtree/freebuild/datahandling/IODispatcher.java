package net.punchtree.freebuild.datahandling;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class IODispatcher {
    private static final ExecutorService yamlExecutor;

    static {
        yamlExecutor = Executors.newFixedThreadPool(2);
    }

    public <T> CompletableFuture<T> submitYamlTask(Supplier<T> task) {
        return CompletableFuture.supplyAsync(task, yamlExecutor);
    }

    public void shutdown() {
        yamlExecutor.shutdown();
    }
}
