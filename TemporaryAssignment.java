import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TemporaryAssignment extends AbstractRoleAssignment {

    private final String expiresAt;
    private final boolean autoRenew;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata, String expiresAt, boolean autoRenew) {
        super(user, role, metadata);
        this.expiresAt = expiresAt;
        this.autoRenew = autoRenew;
    }

    @Override
    public boolean isActive() {
        return !isExpired();
    }

    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }

    public boolean isExpired() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expires = LocalDateTime.parse(expiresAt, FORMATTER);
            return now.isAfter(expires);
        } catch (Exception e) {
            return true;
        }
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public String getTimeRemaining() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expires = LocalDateTime.parse(expiresAt, FORMATTER);

            if (now.isAfter(expires)) {
                return "Истекло";
            }

            long days = ChronoUnit.DAYS.between(now, expires);
            long hours = ChronoUnit.HOURS.between(now, expires) % 24;
            long minutes = ChronoUnit.MINUTES.between(now, expires) % 60;

            return days + " д " + hours + " ч " + minutes + " мин";
        } catch (Exception e) {
            return "Ошибка";
        }
    }

    @Override
    public String summary() {
        String baseSummary = super.summary();
        String expiresInfo = "\nИстекает: " + expiresAt +
                ", автообновление: " + (autoRenew ? "да" : "нет");
        return baseSummary + expiresInfo;
    }
}