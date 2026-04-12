package maining;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class DateUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private DateUtils() {}

    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

    public static boolean isBefore(String date1, String date2) {
        if (date1 == null || date2 == null) return false;
        return date1.compareTo(date2) < 0;
    }

    public static boolean isAfter(String date1, String date2) {
        if (date1 == null || date2 == null) return false;
        return date1.compareTo(date2) > 0;
    }

    public static String addDays(String date, int days) {
        if (date == null) return null;
        try {
            LocalDate localDate = LocalDate.parse(date, DATE_FORMATTER);
            return localDate.plusDays(days).format(DATE_FORMATTER);
        } catch (Exception e) {
            return date;
        }
    }

    public static String formatRelativeTime(String date) {
        if (date == null) return "неизвестно";

        try {
            LocalDate targetDate = LocalDate.parse(date, DATE_FORMATTER);
            LocalDate now = LocalDate.now();

            long daysBetween = ChronoUnit.DAYS.between(now, targetDate);

            if (daysBetween < 0) {
                long daysAgo = -daysBetween;
                if (daysAgo == 1) return "1 day ago";
                return daysAgo + " days ago";
            } else if (daysBetween > 0) {
                if (daysBetween == 1) return "in 1 day";
                return "in " + daysBetween + " days";
            } else {
                return "today";
            }
        } catch (Exception e) {
            return "неизвестно";
        }
    }
}