package maining;
import java.util.List;
import java.util.Scanner;

public final class ConsoleUtils {

    private ConsoleUtils() {}

    public static String promptString(Scanner scanner, String message, boolean required) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();

            if (required && (input == null || input.isEmpty())) {
                System.out.println("Ошибка: Поле обязательно для заполнения");
                continue;
            }

            if (!required && input.isEmpty()) {
                return null;
            }

            return input;
        }
    }

    public static int promptInt(Scanner scanner, String message, int min, int max) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("Ошибка: Введите число от %d до %d\n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Введите корректное число");
            }
        }
    }

    public static boolean promptYesNo(Scanner scanner, String message) {
        while (true) {
            System.out.print(message + " (да/нет): ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("да") || input.equals("yes") || input.equals("y")) {
                return true;
            }
            if (input.equals("нет") || input.equals("no") || input.equals("n")) {
                return false;
            }
            System.out.println("Ошибка: Введите 'да' или 'нет'");
        }
    }

    public static <T> T promptChoice(Scanner scanner, String message, List<T> options) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Список не может быть пустым");
        }
        while (true) {
            System.out.println("\n" + message);
            for (int i = 0; i < options.size(); i++) {
                System.out.printf("%d. %s\n", i + 1, options.get(i).toString());
            }
            System.out.print("Ваш выбор (1-" + options.size() + "): ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice >= 1 && choice <= options.size()) {
                    return options.get(choice - 1);
                }
                System.out.printf("Ошибка: Введите число от 1 до %d\n", options.size());
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Введите корректное число");
            }
        }
    }
}