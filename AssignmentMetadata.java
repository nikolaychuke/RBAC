import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static AssignmentMetadata now(String assignedBy, String reason) {
        String assignedAt = LocalDateTime.now().format(FORMATTER);
        return new AssignmentMetadata(assignedBy, assignedAt, reason);
    }

    public String format() {
        if (reason == null || reason.trim().isEmpty()) {
            return "Назначено: " + assignedBy + " Дата:" + assignedAt + ", причина: не указана";
        } else {
            return "Назначено: " + assignedBy + " Дата:" +  assignedAt + ", причина: " + reason;
        }
    }

    public static void main(String[] args) {
        System.out.println("Тестрирование метаданных");

        System.out.println("\n1. Создание через конструктор:");
        AssignmentMetadata meta1 = new AssignmentMetadata(
                "admin", "2026-03-01 10:30", "Повышение"
        );
        System.out.println(meta1.format());

        System.out.println("\n2. Создание с текущей датой (now):");
        AssignmentMetadata meta2 = AssignmentMetadata.now("manager", "Назначение прав");
        System.out.println(meta2.format());

        System.out.println("\n3. Создание без причины (null):");
        AssignmentMetadata meta3 = AssignmentMetadata.now("system", null);
        System.out.println(meta3.format());
    }
}