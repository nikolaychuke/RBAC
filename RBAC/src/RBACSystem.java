import java.util.*;

public class RBACSystem {

    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;
    private String currentUser;

    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager();
        this.currentUser = "system";
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public AssignmentManager getAssignmentManager() {
        return assignmentManager;
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void initialize() {
        Permission readUsers = Permission.create("READ", "users", "Может просматривать пользователей");
        Permission writeUsers = Permission.create("WRITE", "users", "Может изменять пользователей");
        Permission deleteUsers = Permission.create("DELETE", "users", "Может удалять пользователей");

        Permission readReports = Permission.create("READ", "reports", "Может просматривать отчеты");
        Permission writeReports = Permission.create("WRITE", "reports", "Может создавать отчеты");

        Permission readSettings = Permission.create("READ", "settings", "Может просматривать настройки");
        Permission writeSettings = Permission.create("WRITE", "settings", "Может изменять настройки");

        Role adminRole = new Role("Admin", "Полный доступ к системе");
        adminRole.addPermission(readUsers);
        adminRole.addPermission(writeUsers);
        adminRole.addPermission(deleteUsers);
        adminRole.addPermission(readReports);
        adminRole.addPermission(writeReports);
        adminRole.addPermission(readSettings);
        adminRole.addPermission(writeSettings);
        roleManager.add(adminRole);

        Role managerRole = new Role("Manager", "Управление пользователями и отчетами");
        managerRole.addPermission(readUsers);
        managerRole.addPermission(writeUsers);
        managerRole.addPermission(readReports);
        managerRole.addPermission(writeReports);
        roleManager.add(managerRole);

        Role viewerRole = new Role("Viewer", "Только просмотр");
        viewerRole.addPermission(readUsers);
        viewerRole.addPermission(readReports);
        viewerRole.addPermission(readSettings);
        roleManager.add(viewerRole);

        User admin = User.create("admin", "Administrator", "admin@test.com");
        userManager.add(admin);

        AssignmentMetadata metadata = AssignmentMetadata.now(currentUser, "Инициализация");
        RoleAssignment assignment = new PermanentAssignment(admin, adminRole, metadata);
        assignmentManager.add(assignment);
    }

    public String generateStatistics() {
        StringBuilder sb = new StringBuilder();

        int userCount = userManager.count();
        int roleCount = roleManager.count();
        int assignmentCount = assignmentManager.count();

        sb.append("========== СТАТИСТИКА СИСТЕМЫ ==========\n");
        sb.append("Пользователей: ").append(userCount).append("\n");
        sb.append("Ролей: ").append(roleCount).append("\n");
        sb.append("Назначений: ").append(assignmentCount).append("\n");
        sb.append("==========================================");

        return sb.toString();
    }
}