import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

public class BackgroundExecutorTest {

    @Test
    public void testBackgroundExecutor() throws InterruptedException {
        BackgroundExecutor executor = new BackgroundExecutor();
        AtomicBoolean executed = new AtomicBoolean(false);

        executor.submit(() -> {
            executed.set(true);
        });

        Thread.sleep(500);
        assertTrue(executed.get());

        executor.shutdown();
    }

    @Test
    public void testMultipleTasks() throws InterruptedException {
        BackgroundExecutor executor = new BackgroundExecutor();
        AtomicBoolean task1 = new AtomicBoolean(false);
        AtomicBoolean task2 = new AtomicBoolean(false);

        executor.submit(() -> task1.set(true));
        executor.submit(() -> task2.set(true));

        Thread.sleep(500);
        assertTrue(task1.get());
        assertTrue(task2.get());

        executor.shutdown();
    }
}