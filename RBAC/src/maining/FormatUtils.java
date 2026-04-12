package maining;
import java.util.List;

public final class FormatUtils {

    private FormatUtils() {}

    public static String formatTable(String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0) {
            return "";
        }

        int columnCount = headers.length;
        int[] columnWidths = new int[columnCount];

        for (int i = 0; i < columnCount; i++) {
            columnWidths[i] = headers[i].length();
        }

        if (rows != null) {
            for (String[] row : rows) {
                if (row != null) {
                    for (int i = 0; i < Math.min(row.length, columnCount); i++) {
                        if (row[i] != null) {
                            columnWidths[i] = Math.max(columnWidths[i], row[i].length());
                        }
                    }
                }
            }
        }

        StringBuilder sb = new StringBuilder();

        sb.append("+");
        for (int i = 0; i < columnCount; i++) {
            sb.append("-".repeat(columnWidths[i] + 2));
            sb.append("+");
        }
        sb.append("\n");

        sb.append("|");
        for (int i = 0; i < columnCount; i++) {
            sb.append(String.format(" %-" + columnWidths[i] + "s |", headers[i]));
        }
        sb.append("\n");

        sb.append("+");
        for (int i = 0; i < columnCount; i++) {
            sb.append("-".repeat(columnWidths[i] + 2));
            sb.append("+");
        }
        sb.append("\n");

        if (rows != null) {
            for (String[] row : rows) {
                if (row != null) {
                    sb.append("|");
                    for (int i = 0; i < columnCount; i++) {
                        String value = (i < row.length && row[i] != null) ? row[i] : "";
                        sb.append(String.format(" %-" + columnWidths[i] + "s |", value));
                    }
                    sb.append("\n");
                }
            }

            sb.append("+");
            for (int i = 0; i < columnCount; i++) {
                sb.append("-".repeat(columnWidths[i] + 2));
                sb.append("+");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public static String formatBox(String text) {
        if (text == null || text.isEmpty()) {
            text = "";
        }

        int width = text.length() + 4;
        StringBuilder sb = new StringBuilder();

        sb.append("+").append("-".repeat(width)).append("+\n");
        sb.append(String.format("|  %s  |\n", text));
        sb.append("+").append("-".repeat(width)).append("+");

        return sb.toString();
    }

    public static String formatHeader(String text) {
        if (text == null || text.isEmpty()) {
            text = "";
        }

        return String.format("\n========== %s ==========\n", text);
    }

    public static String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        if (maxLength <= 3) {
            return "...";
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    public static String padRight(String text, int length) {
        if (text == null) {
            text = "";
        }
        if (text.length() >= length) {
            return text;
        }
        return String.format("%-" + length + "s", text);
    }

    public static String padLeft(String text, int length) {
        if (text == null) {
            text = "";
        }
        if (text.length() >= length) {
            return text;
        }
        return String.format("%" + length + "s", text);
    }
}