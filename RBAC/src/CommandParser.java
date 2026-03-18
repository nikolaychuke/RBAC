import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandParser {

    private Map<String, Command> commands;
    private Map<String, String> commandDescriptions;

    public CommandParser() {
        this.commands = new HashMap<>();
        this.commandDescriptions = new HashMap<>();
    }

    public void registerCommand(String name, String description, Command command) {
        commands.put(name, command);
        commandDescriptions.put(name, description);
    }

    public void executeCommand(String commandName, Scanner scanner, RBACSystem system) {
        Command command = commands.get(commandName);
        if (command != null) {
            command.execute(scanner, system);
        } else {
            System.out.println("Неизвестная команда: " + commandName);
            System.out.println("Введите 'help' для списка команд");
        }
    }

    public void printHelp() {
        System.out.println("========== СПИСОК КОМАНД ==========");
        for (Map.Entry<String, String> entry : commandDescriptions.entrySet()) {
            System.out.printf("%-20s - %s\n", entry.getKey(), entry.getValue());
        }
        System.out.println("====================================");
    }

    public void parseAndExecute(String input, Scanner originalScanner, RBACSystem system) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        String[] parts = input.trim().split("\\s+", 2);
        String commandName = parts[0].toLowerCase();

        Scanner commandScanner;

        if (parts.length > 1) {
            String args = parts[1];
            String[] argArray = args.split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (String arg : argArray) {
                sb.append(arg).append("\n");
            }
            commandScanner = new Scanner(new java.io.ByteArrayInputStream(sb.toString().getBytes()));
        } else {
            commandScanner = originalScanner;
        }

        executeCommand(commandName, commandScanner, system);
    }
}