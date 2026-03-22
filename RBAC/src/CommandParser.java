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
            System.out.printf("%s - %s\n", entry.getKey(), entry.getValue());
        }
    }

    public void parseAndExecute(String input, Scanner scanner, RBACSystem system) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        String[] parts = input.trim().split("\\s+");
        if (parts.length == 0) {
            return;
        }

        String commandName = parts[0].toLowerCase();
        String[] args = new String[parts.length - 1];
        if (parts.length > 1) {
            System.arraycopy(parts, 1, args, 0, parts.length - 1);
        }

        Command command = commands.get(commandName);
        if (command != null) {
            currentArgs = args;
            command.execute(scanner, system);
            currentArgs = null;
        } else {
            System.out.println("Неизвестная команда: " + commandName);
            System.out.println("Введите 'help' для списка команд");
        }
    }

    private String[] currentArgs;

    public String[] getCurrentArgs() {
        return currentArgs;
    }

    public String getArgValue(String paramName) {
        if (currentArgs == null) return null;
        for (int i = 0; i < currentArgs.length - 1; i++) {
            if (currentArgs[i].equalsIgnoreCase(paramName)) {
                return currentArgs[i + 1];
            }
        }
        return null;
    }

    public boolean hasArg(String paramName) {
        if (currentArgs == null) return false;
        for (String arg : currentArgs) {
            if (arg.equalsIgnoreCase(paramName)) {
                return true;
            }
        }
        return false;
    }
}