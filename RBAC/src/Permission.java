import java.util.*;

public record Permission(String name, String resource, String description) {

    public Permission {
        String originalName = name;
        String originalResource = resource;
        
        if (originalName == null || originalName.trim().isEmpty()) {
            throw new IllegalArgumentException("Ошибка: name не может быть пустым");
        }
        if (originalName.contains(" ")) {
            throw new IllegalArgumentException("Ошибка: name не должно содержать пробелов");
        }

        if (originalResource == null || originalResource.trim().isEmpty()) {
            throw new IllegalArgumentException("Ошибка: resource не может быть пустым");
        }

        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Ошибка: description не может быть пустым");
        }
        
        name = originalName.toUpperCase();
        resource = originalResource.toLowerCase();
    }

    public static Permission create(String name, String resource, String description) {
        return new Permission(name, resource, description);
    }

    public String format() {
        return name + " on " + resource + ": " + description;
    }

    public boolean matches(String namePattern, String resourcePattern) {
        boolean nameMatches;
        boolean resourceMatches;
        
        if (namePattern == null) {
            nameMatches = true;
        } else {
            nameMatches = name.matches(namePattern);
        }
        
        if (resourcePattern == null) {
            resourceMatches = true;
        } else {
            resourceMatches = resource.matches(resourcePattern);
        }

        return nameMatches && resourceMatches;
    }

    public static void main(String[] args) {
        System.out.println("Тестирование прав доступа");

        System.out.println("\n1. Создание валидного permission: read-users-Может просматривать пользователей");
        try {
            Permission p = Permission.create("read", "users", "Может просматривать пользователей");
            System.out.println(p.format());
            System.out.println("name: " + p.name() + " (READ)");
            System.out.println("resource: " + p.resource() + " (users)");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\n2. Проверка преобразования регистра:");
        try {
            Permission p1 = Permission.create("write", "USERS", "Может изменять пользователей");
            Permission p2 = Permission.create("DELETE", "Reports", "Может удалять отчеты");
            System.out.println(p1.format() + " | было так (write, USERS)");
            System.out.println(p2.format() + " | было так (DELETE, Reports)");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\n3. Name с пробелом (\"READ USERS\"):");
        try {
            Permission.create("READ USERS", "users", "Описание");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\n4. null resource:");
        try {
            Permission.create("READ", null, "Описание");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\n5. Пустой name:");
        try {
            Permission.create("", "users", "Описание");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\n7. Тестирование matches() с regex:");

        Permission pRead = Permission.create("READ", "users", "Чтение пользователей");
        Permission pWrite = Permission.create("WRITE", "reports", "Запись отчетов");
        Permission pDelete = Permission.create("DELETE", "settings", "Удаление настроек");

        System.out.println("\nСозданные permission:");
        System.out.println("pRead: " + pRead.format());
        System.out.println("pWrite: " + pWrite.format());
        System.out.println("pDelete: " + pDelete.format());

        System.out.println("\nТесты matches:");

        System.out.println("pRead.matches(\"^READ$\", null): " +
                pRead.matches("^READ$", null));

        System.out.println("pRead.matches(\"^READ$\", \"users\"): " +
                pRead.matches("^READ$", "users"));

        System.out.println("pWrite.matches(\".*ITE.*\", \".*port.*\"): " +
                pWrite.matches(".*ITE.*", ".*port.*"));

        System.out.println("pDelete.matches(\"DEL\", \"setting\"): " +
                pDelete.matches("DEL", "setting"));

        System.out.println("pDelete.matches(\".*DEL.*\", \".*set.*\"): " +
                pDelete.matches(".*DEL.*", ".*set.*"));

    }
}