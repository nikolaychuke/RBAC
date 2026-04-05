import java.util.*;

public class CommandRegistry {

    private CommandRegistry() {}

    public static void registerAllCommands(CommandParser parser) {

        parser.registerCommand("user-list", "Вывести список всех пользователей. " +
                        "Параметры: --username <name>, --email <email>, --domain <domain>, --fullname <name>",
                (scanner, system) -> {
                    List<User> users;
                    String[] args = parser.getCurrentArgs();

                    Set<String> validParams = new HashSet<>(Arrays.asList("--username", "--email", "--domain", "--fullname"));

                    boolean hasInvalidParam = false;
                    for (int i = 0; i < args.length; i++) {
                        String arg = args[i];
                        if (arg.startsWith("--")) {
                            if (!validParams.contains(arg)) {
                                System.out.println("Ошибка: Неизвестный параметр '" + arg + "'");
                                System.out.println("Допустимые параметры: --username, --email, --domain, --fullname");
                                System.out.println("Примеры использования:");
                                System.out.println("  user-list");
                                System.out.println("  user-list --username <name>");
                                System.out.println("  user-list --email <email>");
                                System.out.println("  user-list --domain <domain>");
                                System.out.println("  user-list --fullname <name>");
                                hasInvalidParam = true;
                                break;
                            }
                            if (i + 1 >= args.length) {
                                System.out.println("Ошибка: Параметр '" + arg + "' требует значение");
                                System.out.println("Пример: " + arg + " <значение>");
                                hasInvalidParam = true;
                                break;
                            }
                        }
                    }

                    if (hasInvalidParam) {
                        return;
                    }

                    String username = parser.getArgValue("--username");
                    String email = parser.getArgValue("--email");
                    String domain = parser.getArgValue("--domain");
                    String fullName = parser.getArgValue("--fullname");

                    if (username != null) {
                        Optional<User> optUser = system.getUserManager().findByUsername(username);
                        users = optUser.map(Collections::singletonList).orElse(Collections.emptyList());
                        System.out.println("Фильтр по username: " + username);
                    } else if (email != null) {
                        if (email.trim().isEmpty()) {
                            System.out.println("Ошибка: Параметр --email не может быть пустым");
                            return;
                        }
                        users = system.getUserManager().findByFilter(user -> user.email().toLowerCase().contains(email.toLowerCase()));
                        System.out.println("Фильтр по email: " + email);
                    } else if (domain != null) {
                        if (domain.trim().isEmpty()) {
                            System.out.println("Ошибка: Параметр --domain не может быть пустым");
                            return;
                        }
                        users = system.getUserManager().findByFilter(UserFilters.byEmailDomain(domain));
                        System.out.println("Фильтр по домену: " + domain);
                    } else if (fullName != null) {
                        if (fullName.trim().isEmpty()) {
                            System.out.println("Ошибка: Параметр --fullname не может быть пустым");
                            return;
                        }
                        users = system.getUserManager().findByFilter(UserFilters.byFullNameContains(fullName));
                        System.out.println("Фильтр по полному имени: " + fullName);
                    } else {
                        users = system.getUserManager().findAll();
                    }

                    if (users.isEmpty()) {
                        System.out.println("Пользователи не найдены");
                        return;
                    }
                    printUsersTable(users);
                });

        parser.registerCommand("user-create", "Создать нового пользователя",
                (scanner, system) -> {
                    try {
                        System.out.print("Введите username: ");
                        String username = scanner.nextLine().trim();
                        System.out.print("Введите полное имя: ");
                        String fullName = scanner.nextLine().trim();
                        System.out.print("Введите email: ");
                        String email = scanner.nextLine().trim();

                        User user = User.create(username, fullName, email);
                        system.getUserManager().add(user);
                        system.getAuditLog().log("CREATE_USER", system.getCurrentUser(), username, "Полное имя: " + fullName + ", Email: " + email);
                        System.out.println("Пользователь успешно создан: " + user.format());
                    } catch (IllegalArgumentException e) {
                        System.out.println("Ошибка: " + e.getMessage());
                    }
                });

        parser.registerCommand("user-view", "Просмотр информации о пользователе",
                (scanner, system) -> {
                    System.out.print("Введите username: ");
                    String username = scanner.nextLine().trim();

                    Optional<User> optUser = system.getUserManager().findByUsername(username);
                    if (optUser.isEmpty()) {
                        System.out.println("Пользователь не найден");
                        return;
                    }

                    User user = optUser.get();
                    System.out.println("\n========== ИНФОРМАЦИЯ О ПОЛЬЗОВАТЕЛЕ ==========");
                    System.out.println("Username: " + user.username());
                    System.out.println("Полное имя: " + user.fullName());
                    System.out.println("Email: " + user.email());

                    List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
                    System.out.println("\nНазначенные роли (" + assignments.size() + "):");
                    if (assignments.isEmpty()) {
                        System.out.println("  (нет назначенных ролей)");
                    } else {
                        for (RoleAssignment ra : assignments) {
                            System.out.println("  - " + ra.role().getName() +
                                    " [" + ra.assignmentType() + "]" +
                                    (ra.isActive() ? " (активно)" : " (неактивно)"));
                        }
                    }

                    Set<Permission> permissions = system.getAssignmentManager().getUserPermissions(user);
                    System.out.println("\nВсе права (" + permissions.size() + "):");
                    if (permissions.isEmpty()) {
                        System.out.println("  (нет прав)");
                    } else {
                        for (Permission p : permissions) {
                            System.out.println("  - " + p.format());
                        }
                    }
                });

        parser.registerCommand("user-update", "Обновить данные пользователя",
                (scanner, system) -> {
                    System.out.print("Введите username: ");
                    String username = scanner.nextLine().trim();

                    Optional<User> optUser = system.getUserManager().findByUsername(username);
                    if (optUser.isEmpty()) {
                        System.out.println("Пользователь не найден");
                        return;
                    }

                    User oldUser = optUser.get();
                    System.out.print("Введите новое полное имя (оставьте пустым для сохранения текущего): ");
                    String fullName = scanner.nextLine().trim();
                    if (fullName.isEmpty()) fullName = oldUser.fullName();

                    System.out.print("Введите новый email (оставьте пустым для сохранения текущего): ");
                    String email = scanner.nextLine().trim();
                    if (email.isEmpty()) email = oldUser.email();

                    try {
                        system.getUserManager().update(username, fullName, email);
                        system.getAuditLog().log("UPDATE_USER", system.getCurrentUser(), username, "Новое имя: " + fullName + ", Email: " + email);
                        System.out.println("Пользователь успешно обновлен");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Ошибка: " + e.getMessage());
                    }
                });

        parser.registerCommand("user-delete", "Удалить пользователя",
                (scanner, system) -> {
                    System.out.print("Введите username: ");
                    String username = scanner.nextLine().trim();

                    Optional<User> optUser = system.getUserManager().findByUsername(username);
                    if (optUser.isEmpty()) {
                        System.out.println("Пользователь не найден");
                        return;
                    }

                    System.out.print("Подтвердите удаление (введите 'да'): ");
                    String confirm = scanner.nextLine().trim();
                    if (!confirm.equals("да")) {
                        System.out.println("Удаление отменено");
                        return;
                    }

                    User user = optUser.get();

                    List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
                    for (RoleAssignment ra : assignments) {
                        system.getAssignmentManager().remove(ra);
                    }

                    system.getAuditLog().log("DELETE_USER", system.getCurrentUser(), username, "Удалены все назначения");
                    system.getUserManager().remove(user);
                    System.out.println("Пользователь " + username + " удален");
                });

        parser.registerCommand("user-search", "Поиск пользователей по фильтрам",
                (scanner, system) -> {
                    System.out.println("\nВыберите фильтр:");
                    System.out.println("1 - По username (содержит)");
                    System.out.println("2 - По email (содержит)");
                    System.out.println("3 - По домену email");
                    System.out.println("4 - По полному имени (содержит)");
                    System.out.print("Ваш выбор: ");

                    String choice = scanner.nextLine().trim();
                    UserFilter filter = null;

                    switch (choice) {
                        case "1":
                            System.out.print("Введите часть username: ");
                            String uname = scanner.nextLine().trim();
                            filter = UserFilters.byUsernameContains(uname);
                            break;
                        case "2":
                            System.out.print("Введите часть email: ");
                            String emailPart = scanner.nextLine().trim();
                            filter = user -> user.email().toLowerCase().contains(emailPart.toLowerCase());
                            break;
                        case "3":
                            System.out.print("Введите домен (например, @test.com): ");
                            String domain = scanner.nextLine().trim();
                            filter = UserFilters.byEmailDomain(domain);
                            break;
                        case "4":
                            System.out.print("Введите часть полного имени: ");
                            String namePart = scanner.nextLine().trim();
                            filter = UserFilters.byFullNameContains(namePart);
                            break;
                        default:
                            System.out.println("Неверный выбор");
                            return;
                    }

                    List<User> users = system.getUserManager().findByFilter(filter);
                    if (users.isEmpty()) {
                        System.out.println("Пользователи не найдены");
                    } else {
                        printUsersTable(users);
                    }
                });

        parser.registerCommand("role-list", "Вывести список всех ролей",
                (scanner, system) -> {
                    List<Role> roles = system.getRoleManager().findAll();
                    if (roles.isEmpty()) {
                        System.out.println("Роли не найдены");
                        return;
                    }

                    System.out.println("\n========== СПИСОК РОЛЕЙ ==========");
                    System.out.printf("%-20s %-15s %s\n", "Название", "Количество прав", "ID");
                    System.out.println("----------------------------------------");
                    for (Role role : roles) {
                        System.out.printf("%-20s %-15d %s\n",
                                role.getName(),
                                role.getPermissions().size(),
                                role.getId());
                    }
                    System.out.println("=====================================");
                });

        parser.registerCommand("role-create", "Создать новую роль",
                (scanner, system) -> {
                    System.out.print("Введите название роли: ");
                    String name = scanner.nextLine().trim();

                    System.out.print("Введите описание роли: ");
                    String description = scanner.nextLine().trim();

                    Role role = new Role(name, description);
                    system.getRoleManager().add(role);
                    system.getAuditLog().log("CREATE_ROLE", system.getCurrentUser(), name, "Описание: " + description);
                    System.out.println("Роль создана: " + role.getName());

                    boolean adding = true;
                    while (adding) {
                        System.out.print("\nДобавить право? (да/нет): ");
                        String add = scanner.nextLine().trim();
                        if (!add.equalsIgnoreCase("да")) break;

                        System.out.print("Введите название права: ");
                        String permName = scanner.nextLine().trim().toUpperCase();
                        System.out.print("Введите ресурс: ");
                        String resource = scanner.nextLine().trim().toLowerCase();
                        System.out.print("Введите описание: ");
                        String permDesc = scanner.nextLine().trim();

                        try {
                            Permission perm = Permission.create(permName, resource, permDesc);
                            role.addPermission(perm);
                            System.out.println("Право добавлено");
                        } catch (IllegalArgumentException e) {
                            System.out.println("Ошибка: " + e.getMessage());
                        }
                    }
                });

        parser.registerCommand("role-view", "Просмотр информации о роли",
                (scanner, system) -> {
                    System.out.print("Введите название роли: ");
                    String roleName = scanner.nextLine().trim();

                    Optional<Role> optRole = system.getRoleManager().findByName(roleName);
                    if (optRole.isEmpty()) {
                        System.out.println("Роль не найдена");
                        return;
                    }

                    System.out.println("\n" + optRole.get().format());
                });

        parser.registerCommand("role-update", "Обновить название/описание роли",
                (scanner, system) -> {
                    System.out.print("Введите название роли: ");
                    String oldName = scanner.nextLine().trim();

                    Optional<Role> optRole = system.getRoleManager().findByName(oldName);
                    if (optRole.isEmpty()) {
                        System.out.println("Роль не найдена");
                        return;
                    }

                    Role oldRole = optRole.get();
                    System.out.print("Введите новое название (оставьте пустым для сохранения): ");
                    String newName = scanner.nextLine().trim();
                    if (newName.isEmpty()) newName = oldRole.getName();

                    System.out.print("Введите новое описание (оставьте пустым для сохранения): ");
                    String newDesc = scanner.nextLine().trim();
                    if (newDesc.isEmpty()) newDesc = oldRole.getDescription();

                    system.getRoleManager().remove(oldRole);
                    Role newRole = new Role(oldRole.getId(), newName, newDesc);
                    for (Permission p : oldRole.getPermissions()) {
                        newRole.addPermission(p);
                    }
                    system.getRoleManager().add(newRole);
                    system.getAuditLog().log("UPDATE_ROLE", system.getCurrentUser(), oldName, "Новое имя: " + newName + ", Описание: " + newDesc);
                    System.out.println("Роль обновлена");
                });

        parser.registerCommand("role-delete", "Удалить роль",
                (scanner, system) -> {
                    System.out.print("Введите название роли: ");
                    String roleName = scanner.nextLine().trim();

                    Optional<Role> optRole = system.getRoleManager().findByName(roleName);
                    if (optRole.isEmpty()) {
                        System.out.println("Роль не найдена");
                        return;
                    }

                    Role role = optRole.get();

                    List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);
                    if (!assignments.isEmpty()) {
                        System.out.println("Предупреждение: роль назначена следующим пользователям:");
                        for (RoleAssignment ra : assignments) {
                            System.out.println("  - " + ra.user().username());
                        }
                        System.out.print("Подтвердите удаление (введите 'да'): ");
                        String confirm = scanner.nextLine().trim();
                        if (!confirm.equals("да")) {
                            System.out.println("Удаление отменено");
                            return;
                        }

                        for (RoleAssignment ra : assignments) {
                            system.getAssignmentManager().remove(ra);
                        }
                    }

                    system.getRoleManager().remove(role);
                    system.getAuditLog().log("DELETE_ROLE", system.getCurrentUser(), roleName, "Удалено назначений: " + assignments.size());
                    System.out.println("Роль " + roleName + " удалена");
                });

        parser.registerCommand("role-add-permission", "Добавить право к роли",
                (scanner, system) -> {
                    System.out.print("Введите название роли: ");
                    String roleName = scanner.nextLine().trim();

                    Optional<Role> optRole = system.getRoleManager().findByName(roleName);
                    if (optRole.isEmpty()) {
                        System.out.println("Роль не найдена");
                        return;
                    }

                    System.out.print("Введите название права: ");
                    String permName = scanner.nextLine().trim().toUpperCase();
                    System.out.print("Введите ресурс: ");
                    String resource = scanner.nextLine().trim().toLowerCase();
                    System.out.print("Введите описание: ");
                    String description = scanner.nextLine().trim();

                    try {
                        Permission perm = Permission.create(permName, resource, description);
                        optRole.get().addPermission(perm);
                        system.getAuditLog().log("ADD_PERMISSION", system.getCurrentUser(), roleName, "Право: " + permName + " на ресурс " + resource);
                        System.out.println("Право добавлено к роли " + roleName);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Ошибка: " + e.getMessage());
                    }
                });

        parser.registerCommand("role-remove-permission", "Удалить право из роли",
                (scanner, system) -> {
                    System.out.print("Введите название роли: ");
                    String roleName = scanner.nextLine().trim();

                    Optional<Role> optRole = system.getRoleManager().findByName(roleName);
                    if (optRole.isEmpty()) {
                        System.out.println("Роль не найдена");
                        return;
                    }

                    Role role = optRole.get();
                    Set<Permission> permissions = role.getPermissions();
                    if (permissions.isEmpty()) {
                        System.out.println("У роли нет прав");
                        return;
                    }

                    System.out.println("\nСписок прав роли:");
                    List<Permission> permList = new ArrayList<>(permissions);
                    for (int i = 0; i < permList.size(); i++) {
                        System.out.printf("%d. %s\n", i + 1, permList.get(i).format());
                    }

                    System.out.print("Введите номер права для удаления: ");
                    try {
                        int index = Integer.parseInt(scanner.nextLine().trim()) - 1;
                        if (index >= 0 && index < permList.size()) {
                            role.removePermission(permList.get(index));
                            system.getAuditLog().log("REMOVE_PERMISSION", system.getCurrentUser(), roleName, "Удалено право: " + permList.get(index).name());
                            System.out.println("Право удалено");
                        } else {
                            System.out.println("Неверный номер");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Неверный ввод");
                    }
                });

        parser.registerCommand("role-search", "Поиск ролей",
                (scanner, system) -> {
                    System.out.println("\nВыберите фильтр:");
                    System.out.println("1 - По имени (содержит)");
                    System.out.println("2 - По наличию конкретного права");
                    System.out.println("3 - По минимальному количеству прав");
                    System.out.print("Ваш выбор: ");

                    String choice = scanner.nextLine().trim();
                    RoleFilter filter = null;

                    switch (choice) {
                        case "1":
                            System.out.print("Введите часть имени: ");
                            String name = scanner.nextLine().trim();
                            filter = RoleFilters.byNameContains(name);
                            break;
                        case "2":
                            System.out.print("Введите название права: ");
                            String permName = scanner.nextLine().trim();
                            System.out.print("Введите ресурс: ");
                            String resource = scanner.nextLine().trim();
                            filter = RoleFilters.hasPermission(permName, resource);
                            break;
                        case "3":
                            System.out.print("Введите минимальное количество прав: ");
                            try {
                                int min = Integer.parseInt(scanner.nextLine().trim());
                                filter = RoleFilters.hasAtLeastNPermissions(min);
                            } catch (NumberFormatException e) {
                                System.out.println("Неверный ввод");
                                return;
                            }
                            break;
                        default:
                            System.out.println("Неверный выбор");
                            return;
                    }

                    List<Role> roles = system.getRoleManager().findByFilter(filter);
                    if (roles.isEmpty()) {
                        System.out.println("Роли не найдены");
                    } else {
                        for (Role role : roles) {
                            System.out.println("\n" + role.format());
                        }
                    }
                });

        parser.registerCommand("assign-role", "Назначить роль пользователю",
                (scanner, system) -> {
                    System.out.print("Введите username: ");
                    String username = scanner.nextLine().trim();

                    Optional<User> optUser = system.getUserManager().findByUsername(username);
                    if (optUser.isEmpty()) {
                        System.out.println("Пользователь не найден");
                        return;
                    }
                    User user = optUser.get();

                    List<Role> roles = system.getRoleManager().findAll();
                    if (roles.isEmpty()) {
                        System.out.println("Нет доступных ролей");
                        return;
                    }

                    System.out.println("\nДоступные роли:");
                    for (int i = 0; i < roles.size(); i++) {
                        System.out.printf("%d. %s - %s\n", i + 1, roles.get(i).getName(),
                                roles.get(i).getDescription());
                    }

                    System.out.print("Выберите номер роли: ");
                    try {
                        int index = Integer.parseInt(scanner.nextLine().trim()) - 1;
                        if (index < 0 || index >= roles.size()) {
                            System.out.println("Неверный выбор");
                            return;
                        }
                        Role role = roles.get(index);

                        System.out.print("Тип назначения (1 - постоянное, 2 - временное): ");
                        String typeChoice = scanner.nextLine().trim();

                        System.out.print("Причина назначения: ");
                        String reason = scanner.nextLine().trim();
                        if (reason.isEmpty()) reason = null;

                        AssignmentMetadata metadata = AssignmentMetadata.now(system.getCurrentUser(), reason);

                        if (typeChoice.equals("1")) {
                            PermanentAssignment assignment = new PermanentAssignment(user, role, metadata);
                            system.getAssignmentManager().add(assignment);
                            system.getAuditLog().log("ASSIGN_ROLE", system.getCurrentUser(), username, "Роль: " + role.getName() + ", Тип: постоянное");
                            System.out.println("Постоянное назначение создано");
                        } else if (typeChoice.equals("2")) {
                            System.out.print("Дата истечения (формат: dd.MM.yyyy HH:mm): ");
                            String expiresAt = scanner.nextLine().trim();
                            System.out.print("Автообновление? (да/нет): ");
                            boolean autoRenew = scanner.nextLine().trim().equalsIgnoreCase("да");
                            TemporaryAssignment assignment = new TemporaryAssignment(user, role,
                                    metadata, expiresAt, autoRenew);
                            system.getAssignmentManager().add(assignment);
                            system.getAuditLog().log("ASSIGN_ROLE", system.getCurrentUser(), username, "Роль: " + role.getName() + ", Тип: временное");
                            System.out.println("Временное назначение создано");
                        } else {
                            System.out.println("Неверный тип назначения");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Неверный ввод");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Ошибка: " + e.getMessage());
                    }
                });

        parser.registerCommand("revoke-role", "Отозвать роль у пользователя",
                (scanner, system) -> {
                    System.out.print("Введите username: ");
                    String username = scanner.nextLine().trim();

                    Optional<User> optUser = system.getUserManager().findByUsername(username);
                    if (optUser.isEmpty()) {
                        System.out.println("Пользователь не найден");
                        return;
                    }
                    User user = optUser.get();

                    List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
                    if (assignments.isEmpty()) {
                        System.out.println("У пользователя нет назначенных ролей");
                        return;
                    }

                    System.out.println("\nАктивные назначения:");
                    List<RoleAssignment> active = new ArrayList<>();
                    for (int i = 0; i < assignments.size(); i++) {
                        RoleAssignment ra = assignments.get(i);
                        if (ra.isActive()) {
                            active.add(ra);
                            System.out.printf("%d. %s [%s]\n", active.size(),
                                    ra.role().getName(), ra.assignmentType());
                        }
                    }

                    if (active.isEmpty()) {
                        System.out.println("Нет активных назначений");
                        return;
                    }

                    System.out.print("Выберите номер назначения для отзыва: ");
                    try {
                        int index = Integer.parseInt(scanner.nextLine().trim()) - 1;
                        if (index >= 0 && index < active.size()) {
                            RoleAssignment assignment = active.get(index);
                            if (assignment instanceof TemporaryAssignment) {
                                ((TemporaryAssignment) assignment).revoke();
                                system.getAuditLog().log("REVOKE_ROLE", system.getCurrentUser(), username, "Отозвана роль: " + assignment.role().getName());
                                System.out.println("Назначение отозвано");
                            } else if (assignment instanceof PermanentAssignment) {
                                ((PermanentAssignment) assignment).revoke();
                                system.getAuditLog().log("REVOKE_ROLE", system.getCurrentUser(), username, "Помечено как неактивное: " + assignment.role().getName());
                                System.out.println("Постоянное назначение помечено как неактивное");
                            }
                        } else {
                            System.out.println("Неверный выбор");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Неверный ввод");
                    }
                });

        parser.registerCommand("assignment-list", "Список всех назначений",
                (scanner, system) -> {
                    List<RoleAssignment> assignments = system.getAssignmentManager().findAll();
                    if (assignments.isEmpty()) {
                        System.out.println("Назначения не найдены");
                        return;
                    }

                    System.out.println("\n========== ВСЕ НАЗНАЧЕНИЯ ==========");
                    System.out.printf("%-15s %-15s %-12s %-10s %-20s\n",
                            "Username", "Роль", "Тип", "Статус", "Дата назначения");
                    System.out.println("---------------------------------------------------------------");
                    for (RoleAssignment ra : assignments) {
                        String status = ra.isActive() ? "активно" : "неактивно";
                        System.out.printf("%-15s %-15s %-12s %-10s %-20s\n",
                                ra.user().username(),
                                ra.role().getName(),
                                ra.assignmentType(),
                                status,
                                ra.metadata().assignedAt());
                    }
                    System.out.println("=====================================");
                });

        parser.registerCommand("assignment-list-user", "Назначения конкретного пользователя",
                (scanner, system) -> {
                    System.out.print("Введите username: ");
                    String username = scanner.nextLine().trim();

                    Optional<User> optUser = system.getUserManager().findByUsername(username);
                    if (optUser.isEmpty()) {
                        System.out.println("Пользователь не найден");
                        return;
                    }

                    List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(optUser.get());
                    if (assignments.isEmpty()) {
                        System.out.println("У пользователя нет назначений");
                        return;
                    }

                    for (RoleAssignment ra : assignments) {
                        System.out.println("\n========== НАЗНАЧЕНИЕ ==========");
                        System.out.println("Тип: " + ra.assignmentType());
                        System.out.println("Роль: " + ra.role().getName());
                        System.out.println("Пользователь: " + ra.user().username());
                        System.out.println("Назначено: " + ra.metadata().assignedBy() +
                                " (" + ra.metadata().assignedAt() + ")");
                        if (ra.metadata().reason() != null) {
                            System.out.println("Причина: " + ra.metadata().reason());
                        }
                        System.out.println("Статус: " + (ra.isActive() ? "АКТИВНО" : "НЕАКТИВНО"));

                        if (ra instanceof TemporaryAssignment) {
                            TemporaryAssignment temp = (TemporaryAssignment) ra;
                            System.out.println("Истекает: " + temp.getExpiresAt());
                            System.out.println("Осталось: " + temp.getTimeRemaining());
                        }
                        System.out.println("==================================");
                    }
                });

        parser.registerCommand("assignment-list-role", "Список пользователей с конкретной ролью",
                (scanner, system) -> {
                    System.out.print("Введите название роли: ");
                    String roleName = scanner.nextLine().trim();

                    Optional<Role> optRole = system.getRoleManager().findByName(roleName);
                    if (optRole.isEmpty()) {
                        System.out.println("Роль не найдена");
                        return;
                    }

                    List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(optRole.get());
                    if (assignments.isEmpty()) {
                        System.out.println("Нет пользователей с этой ролью");
                        return;
                    }

                    System.out.println("\nПользователи с ролью " + roleName + ":");
                    for (RoleAssignment ra : assignments) {
                        System.out.println("  - " + ra.user().username() +
                                " [" + ra.assignmentType() + "]" +
                                (ra.isActive() ? " (активно)" : " (неактивно)"));
                    }
                });

        parser.registerCommand("assignment-active", "Показать только активные назначения",
                (scanner, system) -> {
                    List<RoleAssignment> active = system.getAssignmentManager().getActiveAssignments();
                    if (active.isEmpty()) {
                        System.out.println("Нет активных назначений");
                        return;
                    }

                    System.out.println("\nАктивные назначения:");
                    for (RoleAssignment ra : active) {
                        System.out.println("  - " + ra.user().username() + " -> " +
                                ra.role().getName() + " [" + ra.assignmentType() + "]");
                    }
                });

        parser.registerCommand("assignment-expired", "Показать истёкшие временные назначения",
                (scanner, system) -> {
                    List<RoleAssignment> expired = system.getAssignmentManager().getExpiredAssignments();
                    if (expired.isEmpty()) {
                        System.out.println("Нет истёкших назначений");
                        return;
                    }

                    System.out.println("\nИстёкшие назначения:");
                    for (RoleAssignment ra : expired) {
                        if (ra instanceof TemporaryAssignment) {
                            TemporaryAssignment temp = (TemporaryAssignment) ra;
                            System.out.println("  - " + ra.user().username() + " -> " +
                                    ra.role().getName() + " (истекло: " + temp.getExpiresAt() + ")");
                        }
                    }
                });

        parser.registerCommand("assignment-extend", "Продлить временное назначение",
                (scanner, system) -> {
                    System.out.print("Введите username: ");
                    String username = scanner.nextLine().trim();

                    Optional<User> optUser = system.getUserManager().findByUsername(username);
                    if (optUser.isEmpty()) {
                        System.out.println("Пользователь не найден");
                        return;
                    }

                    List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(optUser.get());
                    List<TemporaryAssignment> tempAssignments = new ArrayList<>();

                    for (RoleAssignment ra : assignments) {
                        if (ra instanceof TemporaryAssignment && ra.isActive()) {
                            tempAssignments.add((TemporaryAssignment) ra);
                        }
                    }

                    if (tempAssignments.isEmpty()) {
                        System.out.println("Нет активных временных назначений у пользователя");
                        return;
                    }

                    System.out.println("\nАктивные временные назначения:");
                    for (int i = 0; i < tempAssignments.size(); i++) {
                        TemporaryAssignment temp = tempAssignments.get(i);
                        System.out.printf("%d. %s (истекает: %s, осталось: %s)\n",
                                i + 1, temp.role().getName(),
                                temp.getExpiresAt(), temp.getTimeRemaining());
                    }

                    System.out.print("Выберите номер назначения: ");
                    try {
                        int index = Integer.parseInt(scanner.nextLine().trim()) - 1;
                        if (index >= 0 && index < tempAssignments.size()) {
                            System.out.print("Введите новую дату истечения (dd.MM.yyyy HH:mm): ");
                            String newDate = scanner.nextLine().trim();
                            tempAssignments.get(index).extend(newDate);
                            System.out.println("Назначение продлено");
                        } else {
                            System.out.println("Неверный выбор");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Неверный ввод");
                    }
                });

        parser.registerCommand("assignment-search", "Поиск назначений по фильтрам",
                (scanner, system) -> {
                    System.out.println("\nВыберите фильтр:");
                    System.out.println("1 - По пользователю");
                    System.out.println("2 - По роли");
                    System.out.println("3 - По типу (постоянное/временное)");
                    System.out.println("4 - По статусу (активное/неактивное)");
                    System.out.print("Ваш выбор: ");

                    String choice = scanner.nextLine().trim();
                    AssignmentFilter filter = null;

                    switch (choice) {
                        case "1":
                            System.out.print("Введите username: ");
                            String username = scanner.nextLine().trim();
                            filter = AssignmentFilters.byUsername(username);
                            break;
                        case "2":
                            System.out.print("Введите название роли: ");
                            String roleName = scanner.nextLine().trim();
                            filter = AssignmentFilters.byRoleName(roleName);
                            break;
                        case "3":
                            System.out.print("Тип (PERMANENT/TEMPORARY): ");
                            String type = scanner.nextLine().trim().toUpperCase();
                            filter = AssignmentFilters.byType(type);
                            break;
                        case "4":
                            System.out.print("Статус (1 - активные, 2 - неактивные): ");
                            String status = scanner.nextLine().trim();
                            if (status.equals("1")) {
                                filter = AssignmentFilters.activeOnly();
                            } else if (status.equals("2")) {
                                filter = AssignmentFilters.inactiveOnly();
                            } else {
                                System.out.println("Неверный выбор");
                                return;
                            }
                            break;
                        default:
                            System.out.println("Неверный выбор");
                            return;
                    }

                    List<RoleAssignment> assignments = system.getAssignmentManager().findByFilter(filter);
                    if (assignments.isEmpty()) {
                        System.out.println("Назначения не найдены");
                    } else {
                        for (RoleAssignment ra : assignments) {
                            System.out.println("  - " + ra.user().username() + " -> " +
                                    ra.role().getName() + " [" + ra.assignmentType() + "]");
                        }
                    }
                });

        parser.registerCommand("permissions-user", "Все права конкретного пользователя",
                (scanner, system) -> {
                    System.out.print("Введите username: ");
                    String username = scanner.nextLine().trim();

                    Optional<User> optUser = system.getUserManager().findByUsername(username);
                    if (optUser.isEmpty()) {
                        System.out.println("Пользователь не найден");
                        return;
                    }

                    Set<Permission> permissions = system.getAssignmentManager().getUserPermissions(optUser.get());
                    if (permissions.isEmpty()) {
                        System.out.println("У пользователя нет прав");
                        return;
                    }

                    Map<String, List<Permission>> byResource = new HashMap<>();
                    for (Permission p : permissions) {
                        byResource.computeIfAbsent(p.resource(), k -> new ArrayList<>()).add(p);
                    }

                    System.out.println("\nПрава пользователя " + username + ":");
                    for (Map.Entry<String, List<Permission>> entry : byResource.entrySet()) {
                        System.out.println("\nРесурс: " + entry.getKey());
                        for (Permission p : entry.getValue()) {
                            System.out.println("  - " + p.name() + ": " + p.description());
                        }
                    }
                });

        parser.registerCommand("permissions-check", "Проверить наличие права у пользователя",
                (scanner, system) -> {
                    System.out.print("Введите username: ");
                    String username = scanner.nextLine().trim();

                    Optional<User> optUser = system.getUserManager().findByUsername(username);
                    if (optUser.isEmpty()) {
                        System.out.println("Пользователь не найден");
                        return;
                    }

                    System.out.print("Введите название права: ");
                    String permName = scanner.nextLine().trim();
                    System.out.print("Введите ресурс: ");
                    String resource = scanner.nextLine().trim();

                    boolean has = system.getAssignmentManager().userHasPermission(optUser.get(), permName, resource);
                    if (has) {
                        System.out.println("Пользователь имеет право " + permName + " на ресурс " + resource);

                        List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(optUser.get());
                        for (RoleAssignment ra : assignments) {
                            if (ra.role().hasPermission(permName, resource)) {
                                System.out.println("  - через роль: " + ra.role().getName());
                            }
                        }
                    } else {
                        System.out.println("Пользователь НЕ имеет право " + permName + " на ресурс " + resource);
                    }
                });

        parser.registerCommand("help", "Показать справку по командам",
                (scanner, system) -> parser.printHelp());

        parser.registerCommand("stats", "Показать статистику системы",
                (scanner, system) -> {
                    System.out.println(system.generateStatistics());

                    List<RoleAssignment> all = system.getAssignmentManager().findAll();
                    long active = system.getAssignmentManager().getActiveAssignments().size();
                    long expired = system.getAssignmentManager().getExpiredAssignments().size();

                    System.out.println("Активных назначений: " + active);
                    System.out.println("Истекших назначений: " + expired);

                    if (system.getUserManager().count() > 0) {
                        double avgRoles = (double) all.size() / system.getUserManager().count();
                        System.out.printf("Среднее количество ролей на пользователя: %.2f\n", avgRoles);
                    }

                    Map<String, Integer> roleCount = new HashMap<>();
                    for (RoleAssignment ra : all) {
                        roleCount.merge(ra.role().getName(), 1, Integer::sum);
                    }

                    List<Map.Entry<String, Integer>> sorted = new ArrayList<>(roleCount.entrySet());
                    sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

                    System.out.println("\nТоп-3 самых популярных ролей:");
                    for (int i = 0; i < Math.min(3, sorted.size()); i++) {
                        System.out.printf("%d. %s (%d назначений)\n", i + 1,
                                sorted.get(i).getKey(), sorted.get(i).getValue());
                    }
                });

        parser.registerCommand("clear", "Очистить экран",
                (scanner, system) -> {
                    for (int i = 0; i < 50; i++) {
                        System.out.println();
                    }
                    System.out.println("Экран очищен");
                });

        parser.registerCommand("exit", "Выйти из программы",
                (scanner, system) -> {
                    System.out.print("Вы уверены, что хотите выйти? (да/нет): ");
                    String confirm = scanner.nextLine().trim();
                    if (confirm.equalsIgnoreCase("да")) {
                        System.out.println("До свидания!");
                        System.exit(0);
                    } else {
                        System.out.println("Выход отменен");
                    }
                });

        parser.registerCommand("audit-log", "Показать лог аудита",
                (scanner, system) -> {
                    System.out.println("1 - Показать лог");
                    System.out.println("2 - Сохранить в файл");
                    String choice = scanner.nextLine().trim();
                    if (choice.equals("1")) {
                        system.getAuditLog().printLog();
                    } else if (choice.equals("2")) {
                        System.out.print("Имя файла: ");
                        String filename = scanner.nextLine().trim();
                        system.getAuditLog().saveToFile(filename);
                    }
                });

        parser.registerCommand("report-users", "Сформировать отчёт по пользователям",
                (scanner, system) -> {
                    String report = system.getReportGenerator().generateUserReport(
                            system.getUserManager(),
                            system.getAssignmentManager()
                    );
                    System.out.println(report);

                    System.out.print("\nСохранить отчёт в файл? (да/нет): ");
                    String save = scanner.nextLine().trim();
                    if (save.equalsIgnoreCase("да")) {
                        System.out.print("Введите имя файла: ");
                        String filename = scanner.nextLine().trim();
                        system.getReportGenerator().exportToFile(report, filename);
                    }
                });

        parser.registerCommand("report-roles", "Сформировать отчёт по ролям",
                (scanner, system) -> {
                    String report = system.getReportGenerator().generateRoleReport(
                            system.getRoleManager(),
                            system.getAssignmentManager()
                    );
                    System.out.println(report);

                    System.out.print("\nСохранить отчёт в файл? (да/нет): ");
                    String save = scanner.nextLine().trim();
                    if (save.equalsIgnoreCase("да")) {
                        System.out.print("Введите имя файла: ");
                        String filename = scanner.nextLine().trim();
                        system.getReportGenerator().exportToFile(report, filename);
                    }
                });

        parser.registerCommand("report-matrix", "Сформировать матрицу прав",
                (scanner, system) -> {
                    String report = system.getReportGenerator().generatePermissionMatrix(
                            system.getUserManager(),
                            system.getAssignmentManager()
                    );
                    System.out.println(report);

                    System.out.print("\nСохранить отчёт в файл? (да/нет): ");
                    String save = scanner.nextLine().trim();
                    if (save.equalsIgnoreCase("да")) {
                        System.out.print("Введите имя файла: ");
                        String filename = scanner.nextLine().trim();
                        system.getReportGenerator().exportToFile(report, filename);
                    }
                });
    }

    private static void printUsersTable(List<User> users) {
        System.out.println("\n========== СПИСОК ПОЛЬЗОВАТЕЛЕЙ ==========");
        System.out.printf("%-15s %-20s %-25s\n", "Username", "Полное имя", "Email");
        System.out.println("----------------------------------------------------------");
        for (User user : users) {
            System.out.printf("%-15s %-20s %-25s\n",
                    user.username(),
                    user.fullName().length() > 20 ? user.fullName().substring(0, 17) + "..." : user.fullName(),
                    user.email());
        }
    }


}