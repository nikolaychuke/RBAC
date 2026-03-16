import java.util.*;

public class Role {
    private final String id;
    private final String name;
    private final String description;
    private final Set<Permission> permissions;

    private static int counter = 1;

    public Role(String name, String description) {

        this.id = "role_" + counter++;
        this.name = name;
        this.description = description;
        this.permissions = new HashSet<>();
    }

    public Role(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.permissions = new HashSet<>();

    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void addPermission(Permission permission) {
        if (permission != null) {
            permissions.add(permission);
        }
    }

    public void removePermission(Permission permission) {
        permissions.remove(permission);
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    public boolean hasPermission(String permissionName, String resource) {
        for (Permission p : permissions) {
            if (p.name().equalsIgnoreCase(permissionName) &&
                    p.resource().equalsIgnoreCase(resource)) {
                return true;
            }
        }
        return false;
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    public String format() {
        StringBuilder sb = new StringBuilder();

        sb.append("Role: ").append(name).append(" [ID: ").append(id).append("]\n");
        sb.append("Description: ").append(description).append("\n");
        sb.append("Permissions (").append(permissions.size()).append("):\n");

        if (permissions.isEmpty()) {
            sb.append(" - (нет прав)\n");
        } else {
            for (Permission p : permissions) {
                sb.append(" - ").append(p.format()).append("\n");
            }
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Role{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", permissions=" + permissions.size() +
                '}';
    }

    public static void main(String[] args) {
        System.out.println("Тестирование ROLE");

        Permission p1 = Permission.create("READ", "users", "Может просматривать пользователей");
        Permission p2 = Permission.create("WRITE", "users", "Может изменять пользователей");
        Permission p3 = Permission.create("DELETE", "users", "Может удалять пользователей");
        Permission p4 = Permission.create("READ", "reports", "Может просматривать отчеты");

        System.out.println(p1);
        System.out.println(p2);
        System.out.println(p3);
        System.out.println(p4);


        System.out.println("\n1. Создание ролей:");

        Role admin = new Role("Administrator", "Полный доступ к системе");
        Role manager = new Role("Manager", "Управление пользователями и отчетами");
        Role viewer = new Role("Viewer", "Только просмотр");

        System.out.println(admin);
        System.out.println(manager);
        System.out.println(viewer);

        System.out.println("\n2. Добавление прав:");

        admin.addPermission(p1);
        admin.addPermission(p2);
        admin.addPermission(p3);
        admin.addPermission(p4);

        manager.addPermission(p1);
        manager.addPermission(p2);
        manager.addPermission(p4);

        viewer.addPermission(p1);
        viewer.addPermission(p4);

        System.out.println("\n" + admin.format());
        System.out.println(manager.format());
        System.out.println(viewer.format());

        System.out.println("Права добавлены");

        System.out.println("\n3. Проверка метода hasPermission:");
        System.out.println("admin.hasPermission(READ users): " + admin.hasPermission(p1));
        System.out.println("admin.hasPermission(DELETE): " + admin.hasPermission(p3));
        System.out.println("manager.hasPermission(DELETE): " + manager.hasPermission(p3));
        System.out.println("manager.hasPermission('READ', 'users'): " + manager.hasPermission("READ", "users"));
        System.out.println("viewer.hasPermission('WRITE', 'users'): " + viewer.hasPermission("WRITE", "users"));

        System.out.println("\n4. Удаление права:");
        admin.removePermission(p3);
        System.out.println("   admin.hasPermission(DELETE) после удаления: " + admin.hasPermission(p3));

        System.out.println("\n5. Форматированный вывод (format):");
        System.out.println("\n" + admin.format());
        System.out.println(manager.format());
        System.out.println(viewer.format());

        System.out.println("6. Проверка equals и hashCode по id:");

        Role adminCopy = new Role(admin.getId(), "Administrator", "Копия");

        System.out.println("admin.equals(adminCopy): " + admin.equals(adminCopy));
        System.out.println("admin.hashCode(): " + admin.hashCode());
        System.out.println("adminCopy.hashCode(): " + adminCopy.hashCode());

        System.out.println("\n7. Неизменяемая копия permissions (admin):");
        try {
            Set<Permission> perm = admin.getPermissions();
            System.out.println("Получили копию: " + perm.size() + " прав");

            perm.add(p3);
        } catch (UnsupportedOperationException e) {
            System.out.println("Нельзя изменить");
        }

        System.out.println("\n8. Проверка уникальности ID:");
        Role role1 = new Role("Test1", "Тестовая роль 1");
        Role role2 = new Role("Test2", "Тестовая роль 2");
        Role role3 = new Role("Test3", "Тестовая роль 3");

        System.out.println("role1 ID: " + role1.getId());
        System.out.println("role2 ID: " + role2.getId());
        System.out.println("role3 ID: " + role3.getId());
    }
}