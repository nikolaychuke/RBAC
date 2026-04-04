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

}