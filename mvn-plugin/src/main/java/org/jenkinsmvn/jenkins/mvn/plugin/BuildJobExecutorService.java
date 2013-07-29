package org.jenkinsmvn.jenkins.mvn.plugin;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class BuildJobExecutorService {
    public static final BuildJobExecutorService INSTANCE = new BuildJobExecutorService();

    private ExecutorService executor;

    public void init(int threads) {
        executor = Executors.newFixedThreadPool(threads);
    }

    public Future submit(Runnable task) {
        return executor.submit(task);
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
