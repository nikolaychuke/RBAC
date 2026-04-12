package maining;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ReportGenerator {

    public String generateUserReport(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();

        sb.append("================== ОТЧЁТ ПО ПОЛЬЗОВАТЕЛЯМ ==================\n");

        for (User user : userManager.findAll()) {
            sb.append(String.format("\nПользователь: %s (%s)\n", user.username(), user.fullName()));
            sb.append(String.format("Email: %s\n", user.email()));

            List<RoleAssignment> assignments = assignmentManager.findByUser(user);
            sb.append("Роли: ");

            if (assignments.isEmpty()) {
                sb.append("(нет ролей)");
            } else {
                for (int i = 0; i < assignments.size(); i++) {
                    RoleAssignment ra = assignments.get(i);
                    sb.append(ra.role().getName());
                    if (!ra.isActive()) sb.append(" (неактивна)");
                    if (i < assignments.size() - 1) sb.append(", ");
                }
            }
            sb.append("\n");
        }

        sb.append("==================================================");
        return sb.toString();
    }

    public String generateUserReportParallel(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();

        sb.append("================== ОТЧЁТ ПО ПОЛЬЗОВАТЕЛЯМ (PARALLEL) ==================\n");

        List<User> users = userManager.findAll();

        String result = users.parallelStream()
                .map(user -> {
                    StringBuilder userSb = new StringBuilder();
                    userSb.append(String.format("\nПользователь: %s (%s)\n", user.username(), user.fullName()));
                    userSb.append(String.format("Email: %s\n", user.email()));

                    List<RoleAssignment> assignments = assignmentManager.findByUser(user);
                    userSb.append("Роли: ");

                    if (assignments.isEmpty()) {
                        userSb.append("(нет ролей)");
                    } else {
                        for (int i = 0; i < assignments.size(); i++) {
                            RoleAssignment ra = assignments.get(i);
                            userSb.append(ra.role().getName());
                            if (!ra.isActive()) userSb.append(" (неактивна)");
                            if (i < assignments.size() - 1) userSb.append(", ");
                        }
                    }
                    userSb.append("\n");
                    return userSb.toString();
                })
                .collect(Collectors.joining());

        sb.append(result);
        sb.append("==================================================");
        return sb.toString();
    }

    public String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();

        sb.append("================== ОТЧЁТ ПО РОЛЯМ ==================\n");

        for (Role role : roleManager.findAll()) {
            List<RoleAssignment> assignments = assignmentManager.findByRole(role);
            int activeCount = 0;
            for (RoleAssignment ra : assignments) {
                if (ra.isActive()) activeCount++;
            }

            sb.append(String.format("\nРоль: %s\n", role.getName()));
            sb.append(String.format("Описание: %s\n", role.getDescription()));
            sb.append(String.format("Количество прав: %d\n", role.getPermissions().size()));
            sb.append(String.format("Пользователей: %d (активных: %d)\n", assignments.size(), activeCount));
        }

        sb.append("==================================================");
        return sb.toString();
    }

    public String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();

        sb.append("================== МАТРИЦА ПРАВ ==================\n");

        Set<String> allResources = new HashSet<>();
        Map<String, Set<String>> userPermissions = new HashMap<>();

        for (User user : userManager.findAll()) {
            Set<Permission> perms = assignmentManager.getUserPermissions(user);
            Set<String> permStrings = new HashSet<>();
            for (Permission p : perms) {
                String key = p.name() + ":" + p.resource();
                permStrings.add(key);
                allResources.add(p.resource());
            }
            userPermissions.put(user.username(), permStrings);
        }

        List<String> sortedResources = new ArrayList<>(allResources);
        Collections.sort(sortedResources);

        sb.append(String.format("%-15s", "Пользователь"));
        for (String resource : sortedResources) {
            sb.append(String.format(" | %-10s", resource));
        }
        sb.append("\n-------------------------------------------------\n");

        for (User user : userManager.findAll()) {
            sb.append(String.format("%-15s", user.username()));
            Set<String> perms = userPermissions.get(user.username());

            for (String resource : sortedResources) {
                boolean hasRead = perms != null && perms.contains("READ:" + resource);
                boolean hasWrite = perms != null && perms.contains("WRITE:" + resource);
                boolean hasDelete = perms != null && perms.contains("DELETE:" + resource);

                String rights = "";
                if (hasRead) rights += "R";
                if (hasWrite) rights += "W";
                if (hasDelete) rights += "D";
                if (rights.isEmpty()) rights = "-";

                sb.append(String.format(" | %-10s", rights));
            }
            sb.append("\n");
        }

        sb.append("==================================================");
        return sb.toString();
    }

    public String generatePermissionMatrixParallel(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();

        sb.append("================== МАТРИЦА ПРАВ (PARALLEL) ==================\n");

        List<User> users = userManager.findAll();

        Set<String> allResources = users.parallelStream()
                .flatMap(user -> assignmentManager.getUserPermissions(user).stream())
                .map(Permission::resource)
                .collect(Collectors.toSet());

        Map<String, Set<String>> userPermissions = users.parallelStream()
                .collect(Collectors.toConcurrentMap(
                        User::username,
                        user -> assignmentManager.getUserPermissions(user).stream()
                                .map(p -> p.name() + ":" + p.resource())
                                .collect(Collectors.toSet())
                ));

        List<String> sortedResources = new ArrayList<>(allResources);
        Collections.sort(sortedResources);

        sb.append(String.format("%-15s", "Пользователь"));
        for (String resource : sortedResources) {
            sb.append(String.format(" | %-10s", resource));
        }
        sb.append("\n-------------------------------------------------\n");

        for (User user : users) {
            sb.append(String.format("%-15s", user.username()));
            Set<String> perms = userPermissions.get(user.username());

            for (String resource : sortedResources) {
                boolean hasRead = perms != null && perms.contains("READ:" + resource);
                boolean hasWrite = perms != null && perms.contains("WRITE:" + resource);
                boolean hasDelete = perms != null && perms.contains("DELETE:" + resource);

                String rights = "";
                if (hasRead) rights += "R";
                if (hasWrite) rights += "W";
                if (hasDelete) rights += "D";
                if (rights.isEmpty()) rights = "-";

                sb.append(String.format(" | %-10s", rights));
            }
            sb.append("\n");
        }

        sb.append("==================================================");
        return sb.toString();
    }

    public void exportToFile(String report, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.print(report);
            System.out.println("Отчёт сохранён в файл: " + filename);
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении отчёта: " + e.getMessage());
        }
    }
}