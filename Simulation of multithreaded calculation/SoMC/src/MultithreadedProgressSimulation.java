import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MultithreadedProgressSimulation {

    private static final int NUM_THREADS = 5;
    private static final int PROGRESS_STEPS = 50;
    private static final int STEP_DELAY_MS = 30;

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        Thread[] threads = new Thread[NUM_THREADS];
        long[] durations = new long[NUM_THREADS];
        int[] steps = new int[NUM_THREADS];
        boolean[] completed = new boolean[NUM_THREADS];

        for (int i = 0; i < NUM_THREADS; i++) {
            final int idx = i;
            threads[i] = new Thread(() -> {
                long start = System.nanoTime();
                try {
                    startLatch.await();
                    for (int s = 0; s <= PROGRESS_STEPS; s++) {
                        steps[idx] = s;
                        Thread.sleep(STEP_DELAY_MS);
                    }
                } catch (InterruptedException ignored) {}
                long end = System.nanoTime();
                durations[idx] = TimeUnit.NANOSECONDS.toMillis(end - start);
                completed[idx] = true;
            });
            threads[i].start();
        }

        startLatch.countDown();

        while (true) {
            boolean allDone = true;
            for (int i = 0; i < NUM_THREADS; i++) {
                if (completed[i]) {
                    System.out.printf("Поток %d (ID: %d) Завершён за %d мс\n",
                            i + 1, threads[i].getId(), durations[i]);
                } else {
                    allDone = false;
                    int percent = (steps[i] * 100) / PROGRESS_STEPS;
                    int filled = (steps[i] * 20) / PROGRESS_STEPS;
                    System.out.printf("Поток %d (ID: %d) [", i + 1, threads[i].getId());
                    for (int j = 0; j < 20; j++) {
                        System.out.print(j < filled ? "#" : ".");
                    }
                    System.out.printf("] %3d%%\n", percent);
                }
            }
            if (allDone) break;
            Thread.sleep(50);
        }
    }

}