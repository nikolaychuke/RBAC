import java.util.*;

public class Main {
    public static void main(String[] args) {
        RBACSystem system = new RBACSystem();
        system.initialize();
        CommandParser parser = new CommandParser();
        CommandRegistry.registerAllCommands(parser);
        Scanner scanner = new Scanner(System.in);

        System.out.println("Введите 'help' для списка команд");
        System.out.println("Введите 'exit' для выхода\n");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            parser.parseAndExecute(input, scanner, system);
        }
    }
}