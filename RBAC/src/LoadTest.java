import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class LoadTest {

    @Test
    public void testConcurrentOperations() throws InterruptedException {
        RBACSystem system = new RBACSystem();
        system.initialize();

        int threadCount = 10;
        int operationsPerThread = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger error = new AtomicInteger(0);

        for (int t = 0; t < threadCount; t++) {
            final int tid = t;
            executor.submit(() -> {
                for (int i = 0; i < operationsPerThread; i++) {
                    try {
                        String name = "u" + tid + "_" + i + "_" + System.currentTimeMillis();
                        User user = User.create(name, "Test", name + "@test.com");
                        system.getUserManager().add(user);
                        system.getUserManager().findByUsername("admin");
                        system.getUserManager().findByFilterParallel(UserFilters.byUsernameContains("admin"));
                        Role role = new Role("Role_" + tid + "_" + i, "Test");
                        system.getRoleManager().add(role);
                        success.incrementAndGet();
                    } catch (Exception e) {
                        error.incrementAndGet();
                    }
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        System.out.println("Успешно: " + success.get());
        System.out.println("Ошибок: " + error.get());

        assertEquals(0, error.get());
    }
}