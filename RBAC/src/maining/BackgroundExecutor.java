package maining;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BackgroundExecutor {

    private final ExecutorService executor;

    public BackgroundExecutor() {
        this.executor = Executors.newCachedThreadPool();
    }

    public void submit(Runnable task) {
        executor.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                System.err.println("Ошибка: " + e.getMessage());
            }
        });
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}