import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class AuditLog {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final List<AuditEntry> entries = new CopyOnWriteArrayList<>();
    private final BlockingQueue<AuditEntry> queue = new LinkedBlockingQueue<>();
    private final ExecutorService consumer = Executors.newSingleThreadExecutor();
    private volatile boolean running = true;

    public AuditLog() {
        consumer.submit(() -> {
            while (running) {
                try {
                    AuditEntry entry = queue.poll(1, TimeUnit.SECONDS);
                    if (entry != null) {
                        entries.add(entry);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    public void log(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        AuditEntry entry = new AuditEntry(timestamp, action, performer, target, details);
        queue.offer(entry);
    }

    public List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }

    public List<AuditEntry> getByPerformer(String performer) {
        if (performer == null) return Collections.emptyList();
        return entries.stream()
                .filter(entry -> entry.performer().equals(performer))
                .collect(Collectors.toList());
    }

    public List<AuditEntry> getByAction(String action) {
        if (action == null) return Collections.emptyList();
        return entries.stream()
                .filter(entry -> entry.action().equals(action))
                .collect(Collectors.toList());
    }

    public void printLog() {
        if (entries.isEmpty()) {
            System.out.println("Лог аудита пуст");
            return;
        }

        System.out.println("\n========== ЛОГ АУДИТА ==========");
        for (AuditEntry entry : entries) {
            System.out.println(entry.timestamp() + " | " + entry.action() + " | " +
                    entry.performer() + " | " + entry.target() + " | " + entry.details());
        }
    }

    public void saveToFile(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (AuditEntry entry : entries) {
                writer.println(entry.timestamp() + "," + entry.action() + "," +
                        entry.performer() + "," + entry.target() + "," + entry.details());
            }
            System.out.println("Лог сохранен в файл: " + filename);
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении лога: " + e.getMessage());
        }
    }

    public void shutdown() {
        running = false;
        consumer.shutdown();
        try {
            consumer.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            consumer.shutdownNow();
        }
    }

    public record AuditEntry(
            String timestamp,
            String action,
            String performer,
            String target,
            String details
    ) {}
}