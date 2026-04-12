package maining;
import java.util.*;
import java.util.concurrent.*;

public class RBACSystem {

    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;
    private AuditLog auditLog;
    private ReportGenerator reportGenerator;
    private BackgroundExecutor backgroundExecutor;
    private ScheduledExecutorService scheduler;
    private String currentUser;

    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager();
        this.auditLog = new AuditLog();
        this.reportGenerator = new ReportGenerator();
        this.backgroundExecutor = new BackgroundExecutor();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.currentUser = "system";

        startExpiredAssignmentsChecker();
        startStatisticsLogger();
    }

    private void startExpiredAssignmentsChecker() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<RoleAssignment> assignments = assignmentManager.findAll();
                int expiredCount = 0;

                for (RoleAssignment ra : assignments) {
                    if (ra instanceof TemporaryAssignment && ra.isActive()) {
                        TemporaryAssignment temp = (TemporaryAssignment) ra;
                        if (temp.isExpired()) {
                            temp.revoke();
                            expiredCount++;
                        }
                    }
                }

                if (expiredCount > 0) {
                    auditLog.log("CLEANUP_EXPIRED", "system", "temporary_assignments",
                            "Помечено неактивными: " + expiredCount);
                }
            } catch (Exception e) {
                System.err.println("Ошибка при проверке истекших назначений: " + e.getMessage());
            }
        }, 10, 30, TimeUnit.SECONDS);
    }

    private void startStatisticsLogger() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                String stats = generateStatistics();
                auditLog.log("STATISTICS_REPORT", "system", "system", stats);
            } catch (Exception e) {
                System.err.println("Ошибка при логировании статистики: " + e.getMessage());
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    public BackgroundExecutor getBackgroundExecutor() {return backgroundExecutor;}

    public ReportGenerator getReportGenerator() {return reportGenerator;}

    public UserManager getUserManager() {return userManager;}

    public RoleManager getRoleManager() {return roleManager;}

    public AssignmentManager getAssignmentManager() {return assignmentManager;}

    public AuditLog getAuditLog() {return auditLog;}

    public void setCurrentUser(String username) {this.currentUser = username;}

    public String getCurrentUser() {return currentUser;}

    public void shutdown() {
        backgroundExecutor.shutdown();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        auditLog.shutdown();
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
        managerRole.addPermission(readSettings);
        roleManager.add(managerRole);

        Role viewerRole = new Role("Viewer", "Только просмотр");
        viewerRole.addPermission(readUsers);
        viewerRole.addPermission(readReports);
        viewerRole.addPermission(readSettings);
        roleManager.add(viewerRole);

        User admin = User.create("admin", "Administrator", "admin@test.com");
        userManager.add(admin);

        User manager1 = User.create("manager1", "Мanager", "manager1@test.com");
        userManager.add(manager1);

        User manager2 = User.create("manager2", "Manager2", "manager2@test.com");
        userManager.add(manager2);

        User viewer1 = User.create("viewer1", "Viewer", "viewer1@test.com");
        userManager.add(viewer1);

        User viewer2 = User.create("viewer2", "Viewer2", "viewer2@test.com");
        userManager.add(viewer2);

        AssignmentMetadata metadata = AssignmentMetadata.now(currentUser, "Инициализация");

        RoleAssignment adminAssignment = new PermanentAssignment(admin, adminRole, metadata);
        assignmentManager.add(adminAssignment);

        RoleAssignment manager1Assignment = new PermanentAssignment(manager1, managerRole, metadata);
        assignmentManager.add(manager1Assignment);

        RoleAssignment manager2Assignment = new PermanentAssignment(manager2, managerRole, metadata);
        assignmentManager.add(manager2Assignment);

        RoleAssignment viewer1Assignment = new PermanentAssignment(viewer1, viewerRole, metadata);
        assignmentManager.add(viewer1Assignment);

        RoleAssignment viewer2Assignment = new PermanentAssignment(viewer2, viewerRole, metadata);
        assignmentManager.add(viewer2Assignment);
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