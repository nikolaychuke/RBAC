package maining;
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
                                System.out.println(FormatUtils.formatBox("Ошибка: Неизвестный параметр '" + arg + "'"));
                                System.out.println(FormatUtils.formatBox("Допустимые параметры: --username, --email, --domain, --fullname"));
                                System.out.println(FormatUtils.formatBox("Примеры использования:"));
                                System.out.println(FormatUtils.formatBox("  user-list"));
                                System.out.println(FormatUtils.formatBox("  user-list --username <name>"));
                                System.out.println(FormatUtils.formatBox("  user-list --email <email>"));
                                System.out.println(FormatUtils.formatBox("  user-list --domain <domain>"));
                                System.out.println(FormatUtils.formatBox("  user-list --fullname <name>"));
                                hasInvalidParam = true;
                                break;
                            }
                            if (i + 1 >= args.length) {
                                System.out.println(FormatUtils.formatBox("Ошибка: Параметр '" + arg + "' требует значение"));
                                System.out.println(FormatUtils.formatBox("Пример: " + arg + " <значение>"));
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
                        System.out.println(FormatUtils.formatBox("Фильтр по username: " + username));
                    } else if (email != null) {
                        if (email.trim().isEmpty()) {
                            System.out.println(FormatUtils.formatBox("Ошибка: Параметр --email не может быть пустым"));
                            return;
                        }
                        users = system.getUserManager().findByFilter(user -> user.email().toLowerCase().contains(email.toLowerCase()));
                        System.out.println(FormatUtils.formatBox("Фильтр по email: " + email));
                    } else if (domain != null) {
                        if (domain.trim().isEmpty()) {
                            System.out.println(FormatUtils.formatBox("Ошибка: Параметр --domain не может быть пустым"));
                            return;
                        }
                        users = system.getUserManager().findByFilter(UserFilters.byEmailDomain(domain));
                        System.out.println(FormatUtils.formatBox("Фильтр по домену: " + domain));
                    } else if (fullName != null) {
                        if (fullName.trim().isEmpty()) {
                            System.out.println(FormatUtils.formatBox("Ошибка: Параметр --fullname не может быть пустым"));
                            return;
                        }
                        users = system.getUserManager().findByFilter(UserFilters.byFullNameContains(fullName));
                        System.out.println(FormatUtils.formatBox("Фильтр по полному имени: " + fullName));
                    } else {
                        users = system.getUserManager().findAll();
                    }

                    if (users.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Пользователи не найдены"));
                        return;
                    }
                    printUsersTable(users);
                });

        parser.registerCommand("user-create", "Создать нового пользователя",
                (scanner, system) -> {
                    try {
                        String username = ConsoleUtils.promptString(scanner, "Введите username: ", true);
                        String fullName = ConsoleUtils.promptString(scanner, "Введите полное имя: ", true);
                        String email = ConsoleUtils.promptString(scanner, "Введите email: ", true);

                        User user = User.create(username, fullName, email);
                        system.getUserManager().add(user);
                        system.getAuditLog().log("CREATE_USER", system.getCurrentUser(), username, "Полное имя: " + fullName + ", Email: " + email);
                        System.out.println(FormatUtils.formatBox("Пользователь успешно создан"));
                        System.out.println(user.format());
                    } catch (IllegalArgumentException e) {
                        System.out.println(FormatUtils.formatBox("Ошибка: " + e.getMessage()));
                    }
                });

        parser.registerCommand("user-view", "Просмотр информации о пользователе",
                (scanner, system) -> {
                    String username = ConsoleUtils.promptString(scanner, "Введите username: ", true);

                    Optional<User> optUser = system.getUserManager().findByUsername(username);
                    if (optUser.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Пользователь не найден"));
                        return;
                    }

                    User user = optUser.get();
                    System.out.println(FormatUtils.formatHeader("ИНФОРМАЦИЯ О ПОЛЬЗОВАТЕЛЕ"));
                    System.out.println(FormatUtils.formatBox("Username: " + user.username()));
                    System.out.println(FormatUtils.formatBox("Полное имя: " + user.fullName()));
                    System.out.println(FormatUtils.formatBox("Email: " + user.email()));

                    List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
                    System.out.println(FormatUtils.formatBox("\nНазначенные роли (" + assignments.size() + "):"));
                    if (assignments.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("  (нет назначенных ролей)"));
                    } else {
                        for (RoleAssignment ra : assignments) {
                            System.out.println(FormatUtils.formatBox("  - " + ra.role().getName() +
                                    " [" + ra.assignmentType() + "]" +
                                    (ra.isActive() ? " (активно)" : " (неактивно)")));
                        }
                    }

                    Set<Permission> permissions = system.getAssignmentManager().getUserPermissions(user);
                    System.out.println(FormatUtils.formatBox("\nВсе права (" + permissions.size() + "):"));
                    if (permissions.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("  (нет прав)"));
                    } else {
                        for (Permission p : permissions) {
                            System.out.println(FormatUtils.formatBox("  - " + p.format()));
                        }
                    }
                });

        parser.registerCommand("user-update", "Обновить данные пользователя",
                (scanner, system) -> {
                    String username = ConsoleUtils.promptString(scanner, "Введите username: ", true);

                    Optional<User> optUser = system.getUserManager().findByUsername(username);
                    if (optUser.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Пользователь не найден"));
                        return;
                    }

                    User oldUser = optUser.get();
                    String fullName = ConsoleUtils.promptString(scanner, "Введите новое полное имя (оставьте пустым для сохранения): ", false);
                    if (fullName == null || fullName.isEmpty()) fullName = oldUser.fullName();

                    String email = ConsoleUtils.promptString(scanner, "Введите новый email (оставьте пустым для сохранения): ", false);
                    if (email == null || email.isEmpty()) email = oldUser.email();

                    try {
                        system.getUserManager().update(username, fullName, email);
                        system.getAuditLog().log("UPDATE_USER", system.getCurrentUser(), username, "Новое имя: " + fullName + ", Email: " + email);
                        System.out.println(FormatUtils.formatBox("Пользователь успешно обновлен"));
                    } catch (IllegalArgumentException e) {
                        System.out.println(FormatUtils.formatBox("Ошибка: " + e.getMessage()));
                    }
                });

        parser.registerCommand("user-delete", "Удалить пользователя",
                (scanner, system) -> {
                    String username = ConsoleUtils.promptString(scanner, "Введите username: ", true);

                    Optional<User> optUser = system.getUserManager().findByUsername(username);
                    if (optUser.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Пользователь не найден"));
                        return;
                    }

                    if (!ConsoleUtils.promptYesNo(scanner, "Подтвердите удаление")) {
                        System.out.println(FormatUtils.formatBox("Удаление отменено"));
                        return;
                    }

                    User user = optUser.get();

                    List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
                    for (RoleAssignment ra : assignments) {
                        system.getAssignmentManager().remove(ra);
                    }

                    system.getAuditLog().log("DELETE_USER", system.getCurrentUser(), username, "Удалены все назначения");
                    system.getUserManager().remove(user);
                    System.out.println(FormatUtils.formatBox("Пользователь " + username + " удален"));
                });

        parser.registerCommand("user-search", "Поиск пользователей по фильтрам",
                (scanner, system) -> {
                    List<String> filters = Arrays.asList(
                            "По username (содержит)",
                            "По email (содержит)",
                            "По домену email",
                            "По полному имени (содержит)"
                    );
                    String choice = ConsoleUtils.promptChoice(scanner, "Выберите фильтр:", filters);

                    UserFilter filter = null;

                    if (choice.equals("По username (содержит)")) {
                        String searchValue = ConsoleUtils.promptString(scanner, "Введите часть username: ", true);
                        filter = UserFilters.byUsernameContains(searchValue);
                    } else if (choice.equals("По email (содержит)")) {
                        String searchValue = ConsoleUtils.promptString(scanner, "Введите часть email: ", true);
                        final String finalSearchValue = searchValue;
                        filter = user -> user.email().toLowerCase().contains(finalSearchValue.toLowerCase());
                    } else if (choice.equals("По домену email")) {
                        String searchValue = ConsoleUtils.promptString(scanner, "Введите домен (например, @test.com): ", true);
                        filter = UserFilters.byEmailDomain(searchValue);
                    } else if (choice.equals("По полному имени (содержит)")) {
                        String searchValue = ConsoleUtils.promptString(scanner, "Введите часть полного имени: ", true);
                        filter = UserFilters.byFullNameContains(searchValue);
                    }

                    List<User> users = system.getUserManager().findByFilter(filter);
                    if (users.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Пользователи не найдены"));
                    } else {
                        printUsersTable(users);
                    }
                });

        parser.registerCommand("role-list", "Вывести список всех ролей",
                (scanner, system) -> {
                    List<Role> roles = system.getRoleManager().findAll();
                    if (roles.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Роли не найдены"));
                        return;
                    }

                    String[] headers = {"Название", "Количество прав", "ID"};
                    List<String[]> rows = new ArrayList<>();

                    for (Role role : roles) {
                        String[] row = {
                                role.getName(),
                                String.valueOf(role.getPermissions().size()),
                                role.getId()
                        };
                        rows.add(row);
                    }

                    System.out.println(FormatUtils.formatTable(headers, rows));
                });

        parser.registerCommand("role-create", "Создать новую роль",
                (scanner, system) -> {
                    String name = ConsoleUtils.promptString(scanner, "Введите название роли: ", true);
                    String description = ConsoleUtils.promptString(scanner, "Введите описание роли: ", true);

                    Role role = new Role(name, description);
                    system.getRoleManager().add(role);
                    system.getAuditLog().log("CREATE_ROLE", system.getCurrentUser(), name, "Описание: " + description);
                    System.out.println(FormatUtils.formatBox("Роль создана: " + role.getName()));

                    boolean adding = true;
                    while (adding) {
                        if (!ConsoleUtils.promptYesNo(scanner, "Добавить право?")) break;

                        String permName = ConsoleUtils.promptString(scanner, "Введите название права: ", true);
                        String resource = ConsoleUtils.promptString(scanner, "Введите ресурс: ", true);
                        String permDesc = ConsoleUtils.promptString(scanner, "Введите описание: ", true);

                        try {
                            Permission perm = Permission.create(permName, resource, permDesc);
                            role.addPermission(perm);
                            System.out.println(FormatUtils.formatBox("Право добавлено"));
                        } catch (IllegalArgumentException e) {
                            System.out.println(FormatUtils.formatBox("Ошибка: " + e.getMessage()));
                        }
                    }
                });

        parser.registerCommand("role-view", "Просмотр информации о роли",
                (scanner, system) -> {
                    String roleName = ConsoleUtils.promptString(scanner, "Введите название роли: ", true);

                    Optional<Role> optRole = system.getRoleManager().findByName(roleName);
                    if (optRole.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Роль не найдена"));
                        return;
                    }

                    System.out.println(optRole.get().format());
                });

        parser.registerCommand("role-update", "Обновить название/описание роли",
                (scanner, system) -> {
                    String oldName = ConsoleUtils.promptString(scanner, "Введите название роли: ", true);

                    Optional<Role> optRole = system.getRoleManager().findByName(oldName);
                    if (optRole.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Роль не найдена"));
                        return;
                    }

                    Role oldRole = optRole.get();
                    String newName = ConsoleUtils.promptString(scanner, "Введите новое название (оставьте пустым для сохранения): ", false);
                    if (newName == null || newName.isEmpty()) newName = oldRole.getName();

                    String newDesc = ConsoleUtils.promptString(scanner, "Введите новое описание (оставьте пустым для сохранения): ", false);
                    if (newDesc == null || newDesc.isEmpty()) newDesc = oldRole.getDescription();

                    system.getRoleManager().remove(oldRole);
                    Role newRole = new Role(oldRole.getId(), newName, newDesc);
                    for (Permission p : oldRole.getPermissions()) {
                        newRole.addPermission(p);
                    }
                    system.getRoleManager().add(newRole);
                    system.getAuditLog().log("UPDATE_ROLE", system.getCurrentUser(), oldName, "Новое имя: " + newName + ", Описание: " + newDesc);
                    System.out.println(FormatUtils.formatBox("Роль обновлена"));
                });

        parser.registerCommand("role-delete", "Удалить роль",
                (scanner, system) -> {
                    String roleName = ConsoleUtils.promptString(scanner, "Введите название роли: ", true);

                    Optional<Role> optRole = system.getRoleManager().findByName(roleName);
                    if (optRole.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Роль не найдена"));
                        return;
                    }

                    Role role = optRole.get();

                    List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);
                    if (!assignments.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Предупреждение: роль назначена следующим пользователям:"));
                        for (RoleAssignment ra : assignments) {
                            System.out.println(FormatUtils.formatBox("  - " + ra.user().username()));
                        }
                        if (!ConsoleUtils.promptYesNo(scanner, "Подтвердите удаление")) {
                            System.out.println(FormatUtils.formatBox("Удаление отменено"));
                            return;
                        }

                        for (RoleAssignment ra : assignments) {
                            system.getAssignmentManager().remove(ra);
                        }
                    }

                    system.getRoleManager().remove(role);
                    system.getAuditLog().log("DELETE_ROLE", system.getCurrentUser(), roleName, "Удалено назначений: " + assignments.size());
                    System.out.println(FormatUtils.formatBox("Роль " + roleName + " удалена"));
                });

        parser.registerCommand("role-add-permission", "Добавить право к роли",
                (scanner, system) -> {
                    String roleName = ConsoleUtils.promptString(scanner, "Введите название роли: ", true);

                    Optional<Role> optRole = system.getRoleManager().findByName(roleName);
                    if (optRole.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Роль не найдена"));
                        return;
                    }

                    String permName = ConsoleUtils.promptString(scanner, "Введите название права: ", true);
                    String resource = ConsoleUtils.promptString(scanner, "Введите ресурс: ", true);
                    String description = ConsoleUtils.promptString(scanner, "Введите описание: ", true);

                    try {
                        Permission perm = Permission.create(permName, resource, description);
                        optRole.get().addPermission(perm);
                        system.getAuditLog().log("ADD_PERMISSION", system.getCurrentUser(), roleName, "Право: " + permName + " на ресурс " + resource);
                        System.out.println(FormatUtils.formatBox("Право добавлено к роли " + roleName));
                    } catch (IllegalArgumentException e) {
                        System.out.println(FormatUtils.formatBox("Ошибка: " + e.getMessage()));
                    }
                });

        parser.registerCommand("role-remove-permission", "Удалить право из роли",
                (scanner, system) -> {
                    String roleName = ConsoleUtils.promptString(scanner, "Введите название роли: ", true);

                    Optional<Role> optRole = system.getRoleManager().findByName(roleName);
                    if (optRole.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Роль не найдена"));
                        return;
                    }

                    Role role = optRole.get();
                    Set<Permission> permissions = role.getPermissions();
                    if (permissions.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("У роли нет прав"));
                        return;
                    }

                    List<Permission> permList = new ArrayList<>(permissions);
                    int index = ConsoleUtils.promptInt(scanner, "Введите номер права для удаления: ", 1, permList.size());

                    role.removePermission(permList.get(index - 1));
                    system.getAuditLog().log("REMOVE_PERMISSION", system.getCurrentUser(), roleName, "Удалено право: " + permList.get(index - 1).name());
                    System.out.println(FormatUtils.formatBox("Право удалено"));
                });

        parser.registerCommand("role-search", "Поиск ролей",
                (scanner, system) -> {
                    List<String> searchFilters = Arrays.asList(
                            "По имени (содержит)",
                            "По наличию конкретного права",
                            "По минимальному количеству прав"
                    );
                    String choice = ConsoleUtils.promptChoice(scanner, "Выберите фильтр:", searchFilters);

                    RoleFilter filter = null;

                    if (choice.equals("По имени (содержит)")) {
                        String name = ConsoleUtils.promptString(scanner, "Введите часть имени: ", true);
                        filter = RoleFilters.byNameContains(name);
                    } else if (choice.equals("По наличию конкретного права")) {
                        String permName = ConsoleUtils.promptString(scanner, "Введите название права: ", true);
                        String resource = ConsoleUtils.promptString(scanner, "Введите ресурс: ", true);
                        filter = RoleFilters.hasPermission(permName, resource);
                    } else {
                        int min = ConsoleUtils.promptInt(scanner, "Введите минимальное количество прав: ", 0, Integer.MAX_VALUE);
                        filter = RoleFilters.hasAtLeastNPermissions(min);
                    }

                    List<Role> roles = system.getRoleManager().findByFilter(filter);
                    if (roles.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Роли не найдены"));
                    } else {
                        for (Role role : roles) {
                            System.out.println(FormatUtils.formatBox("\n" + role.format()));
                        }
                    }
                });

        parser.registerCommand("assign-role", "Назначить роль пользователю",
                (scanner, system) -> {
                    String username = ConsoleUtils.promptString(scanner, "Введите username: ", true);

                    Optional<User> optUser = system.getUserManager().findByUsername(username);
                    if (optUser.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Пользователь не найден"));
                        return;
                    }
                    User user = optUser.get();

                    List<Role> roles = system.getRoleManager().findAll();
                    if (roles.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Нет доступных ролей"));
                        return;
                    }

                    Role role = ConsoleUtils.promptChoice(scanner, "Доступные роли:", roles);

                    List<String> types = Arrays.asList("Постоянное", "Временное");
                    String typeChoice = ConsoleUtils.promptChoice(scanner, "Тип назначения:", types);

                    String reason = ConsoleUtils.promptString(scanner, "Причина назначения: ", false);

                    AssignmentMetadata metadata = AssignmentMetadata.now(system.getCurrentUser(), reason);

                    if (typeChoice.equals("Постоянное")) {
                        PermanentAssignment assignment = new PermanentAssignment(user, role, metadata);
                        system.getAssignmentManager().add(assignment);
                        system.getAuditLog().log("ASSIGN_ROLE", system.getCurrentUser(), username, "Роль: " + role.getName() + ", Тип: постоянное");
                        System.out.println(FormatUtils.formatBox("Постоянное назначение создано"));
                    } else {
                        String expiresAt = ConsoleUtils.promptString(scanner, "Дата истечения (формат: dd.MM.yyyy HH:mm): ", true);
                        boolean autoRenew = ConsoleUtils.promptYesNo(scanner, "Автообновление?");

                        try {
                            TemporaryAssignment assignment = new TemporaryAssignment(user, role,
                                    metadata, expiresAt, autoRenew);
                            system.getAssignmentManager().add(assignment);
                            system.getAuditLog().log("ASSIGN_ROLE", system.getCurrentUser(), username, "Роль: " + role.getName() + ", Тип: временное");
                            System.out.println(FormatUtils.formatBox("Временное назначение создано"));
                        } catch (IllegalArgumentException e) {
                            System.out.println(FormatUtils.formatBox("Ошибка: " + e.getMessage()));
                        }
                    }
                });

        parser.registerCommand("revoke-role", "Отозвать роль у пользователя",
                (scanner, system) -> {
                    String username = ConsoleUtils.promptString(scanner, "Введите username: ", true);

                    Optional<User> optUser = system.getUserManager().findByUsername(username);
                    if (optUser.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Пользователь не найден"));
                        return;
                    }
                    User user = optUser.get();

                    List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
                    if (assignments.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("У пользователя нет назначенных ролей"));
                        return;
                    }

                    List<RoleAssignment> active = new ArrayList<>();
                    for (RoleAssignment ra : assignments) {
                        if (ra.isActive()) {
                            active.add(ra);
                        }
                    }

                    if (active.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Нет активных назначений"));
                        return;
                    }

                    RoleAssignment assignment = ConsoleUtils.promptChoice(scanner, "Активные назначения:", active);

                    if (assignment instanceof TemporaryAssignment) {
                        ((TemporaryAssignment) assignment).revoke();
                        system.getAuditLog().log("REVOKE_ROLE", system.getCurrentUser(), username, "Отозвана роль: " + assignment.role().getName());
                        System.out.println(FormatUtils.formatBox("Назначение отозвано"));
                    } else if (assignment instanceof PermanentAssignment) {
                        ((PermanentAssignment) assignment).revoke();
                        system.getAuditLog().log("REVOKE_ROLE", system.getCurrentUser(), username, "Помечено как неактивное: " + assignment.role().getName());
                        System.out.println(FormatUtils.formatBox("Постоянное назначение помечено как неактивное"));
                    }
                });

        parser.registerCommand("assignment-list", "Список всех назначений",
                (scanner, system) -> {
                    List<RoleAssignment> assignments = system.getAssignmentManager().findAll();
                    if (assignments.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Назначения не найдены"));
                        return;
                    }

                    String[] headers = {"Username", "Роль", "Тип", "Статус", "Дата назначения"};
                    List<String[]> rows = new ArrayList<>();

                    for (RoleAssignment ra : assignments) {
                        String[] row = {
                                ra.user().username(),
                                ra.role().getName(),
                                ra.assignmentType(),
                                ra.isActive() ? "активно" : "неактивно",
                                ra.metadata().assignedAt()
                        };
                        rows.add(row);
                    }

                    System.out.println(FormatUtils.formatTable(headers, rows));
                });

        parser.registerCommand("assignment-list-user", "Назначения конкретного пользователя",
                (scanner, system) -> {
                    String username = ConsoleUtils.promptString(scanner, "Введите username: ", true);

                    Optional<User> optUser = system.getUserManager().findByUsername(username);
                    if (optUser.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Пользователь не найден"));
                        return;
                    }

                    List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(optUser.get());
                    if (assignments.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("У пользователя нет назначений"));
                        return;
                    }

                    for (RoleAssignment ra : assignments) {
                        System.out.println(FormatUtils.formatHeader("НАЗНАЧЕНИЕ"));
                        System.out.println(FormatUtils.formatBox("Тип: " + ra.assignmentType()));
                        System.out.println(FormatUtils.formatBox("Роль: " + ra.role().getName()));
                        System.out.println(FormatUtils.formatBox("Пользователь: " + ra.user().username()));
                        System.out.println(FormatUtils.formatBox("Назначено: " + ra.metadata().assignedBy() +
                                " (" + ra.metadata().assignedAt() + ")"));
                        if (ra.metadata().reason() != null) {
                            System.out.println(FormatUtils.formatBox("Причина: " + ra.metadata().reason()));
                        }
                        System.out.println(FormatUtils.formatBox("Статус: " + (ra.isActive() ? "АКТИВНО" : "НЕАКТИВНО")));

                        if (ra instanceof TemporaryAssignment) {
                            TemporaryAssignment temp = (TemporaryAssignment) ra;
                            System.out.println(FormatUtils.formatBox("Истекает: " + temp.getExpiresAt()));
                            System.out.println(FormatUtils.formatBox("Осталось: " + temp.getTimeRemaining()));
                        }
                    }
                });

        parser.registerCommand("assignment-list-role", "Список пользователей с конкретной ролью",
                (scanner, system) -> {
                    String roleName = ConsoleUtils.promptString(scanner, "Введите название роли: ", true);

                    Optional<Role> optRole = system.getRoleManager().findByName(roleName);
                    if (optRole.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Роль не найдена"));
                        return;
                    }

                    List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(optRole.get());
                    if (assignments.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Нет пользователей с этой ролью"));
                        return;
                    }

                    System.out.println(FormatUtils.formatHeader("Пользователи с ролью " + roleName));
                    for (RoleAssignment ra : assignments) {
                        System.out.println(FormatUtils.formatBox("  - " + ra.user().username() +
                                " [" + ra.assignmentType() + "]" +
                                (ra.isActive() ? " (активно)" : " (неактивно)")));
                    }
                });

        parser.registerCommand("assignment-active", "Показать только активные назначения",
                (scanner, system) -> {
                    List<RoleAssignment> active = system.getAssignmentManager().getActiveAssignments();
                    if (active.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Нет активных назначений"));
                        return;
                    }

                    String[] headers = {"Username", "Роль", "Тип"};
                    List<String[]> rows = new ArrayList<>();

                    for (RoleAssignment ra : active) {
                        String[] row = {
                                ra.user().username(),
                                ra.role().getName(),
                                ra.assignmentType()
                        };
                        rows.add(row);
                    }

                    System.out.println(FormatUtils.formatTable(headers, rows));
                });

        parser.registerCommand("assignment-expired", "Показать истёкшие временные назначения",
                (scanner, system) -> {
                    List<RoleAssignment> expired = system.getAssignmentManager().getExpiredAssignments();
                    if (expired.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Нет истёкших назначений"));
                        return;
                    }

                    String[] headers = {"Username", "Роль", "Истекло"};
                    List<String[]> rows = new ArrayList<>();

                    for (RoleAssignment ra : expired) {
                        if (ra instanceof TemporaryAssignment) {
                            TemporaryAssignment temp = (TemporaryAssignment) ra;
                            String[] row = {
                                    ra.user().username(),
                                    ra.role().getName(),
                                    temp.getExpiresAt()
                            };
                            rows.add(row);
                        }
                    }

                    System.out.println(FormatUtils.formatTable(headers, rows));
                });

        parser.registerCommand("assignment-extend", "Продлить временное назначение",
                (scanner, system) -> {
                    String username = ConsoleUtils.promptString(scanner, "Введите username: ", true);

                    Optional<User> optUser = system.getUserManager().findByUsername(username);
                    if (optUser.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Пользователь не найден"));
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
                        System.out.println(FormatUtils.formatBox("Нет активных временных назначений у пользователя"));
                        return;
                    }

                    TemporaryAssignment temp = ConsoleUtils.promptChoice(scanner, "Активные временные назначения:", tempAssignments);

                    String newDate = ConsoleUtils.promptString(scanner, "Введите новую дату истечения (dd.MM.yyyy HH:mm): ", true);
                    temp.extend(newDate);
                    System.out.println(FormatUtils.formatBox("Назначение продлено"));
                });

        parser.registerCommand("assignment-search", "Поиск назначений по фильтрам",
                (scanner, system) -> {
                    List<String> searchFilters = Arrays.asList(
                            "По пользователю",
                            "По роли",
                            "По типу (постоянное/временное)",
                            "По статусу (активное/неактивное)"
                    );
                    String choice = ConsoleUtils.promptChoice(scanner, "Выберите фильтр:", searchFilters);

                    AssignmentFilter filter = null;

                    if (choice.equals("По пользователю")) {
                        String username = ConsoleUtils.promptString(scanner, "Введите username: ", true);
                        filter = AssignmentFilters.byUsername(username);
                    } else if (choice.equals("По роли")) {
                        String roleName = ConsoleUtils.promptString(scanner, "Введите название роли: ", true);
                        filter = AssignmentFilters.byRoleName(roleName);
                    } else if (choice.equals("По типу (постоянное/временное)")) {
                        List<String> types = Arrays.asList("PERMANENT", "TEMPORARY");
                        String type = ConsoleUtils.promptChoice(scanner, "Выберите тип:", types);
                        filter = AssignmentFilters.byType(type);
                    } else {
                        List<String> statuses = Arrays.asList("активные", "неактивные");
                        String status = ConsoleUtils.promptChoice(scanner, "Выберите статус:", statuses);
                        if (status.equals("активные")) {
                            filter = AssignmentFilters.activeOnly();
                        } else {
                            filter = AssignmentFilters.inactiveOnly();
                        }
                    }

                    List<RoleAssignment> assignments = system.getAssignmentManager().findByFilter(filter);
                    if (assignments.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Назначения не найдены"));
                    } else {
                        String[] headers = {"Username", "Роль", "Тип"};
                        List<String[]> rows = new ArrayList<>();
                        for (RoleAssignment ra : assignments) {
                            String[] row = {
                                    ra.user().username(),
                                    ra.role().getName(),
                                    ra.assignmentType()
                            };
                            rows.add(row);
                        }
                        System.out.println(FormatUtils.formatTable(headers, rows));
                    }
                });

        parser.registerCommand("permissions-user", "Все права конкретного пользователя",
                (scanner, system) -> {
                    String username = ConsoleUtils.promptString(scanner, "Введите username: ", true);

                    Optional<User> optUser = system.getUserManager().findByUsername(username);
                    if (optUser.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Пользователь не найден"));
                        return;
                    }

                    Set<Permission> permissions = system.getAssignmentManager().getUserPermissions(optUser.get());
                    if (permissions.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("У пользователя нет прав"));
                        return;
                    }

                    Map<String, List<Permission>> byResource = new HashMap<>();
                    for (Permission p : permissions) {
                        byResource.computeIfAbsent(p.resource(), k -> new ArrayList<>()).add(p);
                    }

                    System.out.println(FormatUtils.formatHeader("Права пользователя " + username));
                    for (Map.Entry<String, List<Permission>> entry : byResource.entrySet()) {
                        System.out.println(FormatUtils.formatBox("\nРесурс: " + entry.getKey()));
                        for (Permission p : entry.getValue()) {
                            System.out.println(FormatUtils.formatBox("  - " + p.name() + ": " + p.description()));
                        }
                    }
                });

        parser.registerCommand("permissions-check", "Проверить наличие права у пользователя",
                (scanner, system) -> {
                    String username = ConsoleUtils.promptString(scanner, "Введите username: ", true);

                    Optional<User> optUser = system.getUserManager().findByUsername(username);
                    if (optUser.isEmpty()) {
                        System.out.println(FormatUtils.formatBox("Пользователь не найден"));
                        return;
                    }

                    String permName = ConsoleUtils.promptString(scanner, "Введите название права: ", true);
                    String resource = ConsoleUtils.promptString(scanner, "Введите ресурс: ", true);

                    boolean has = system.getAssignmentManager().userHasPermission(optUser.get(), permName, resource);
                    if (has) {
                        System.out.println(FormatUtils.formatBox("Пользователь имеет право " + permName + " на ресурс " + resource));

                        List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(optUser.get());
                        for (RoleAssignment ra : assignments) {
                            if (ra.role().hasPermission(permName, resource)) {
                                System.out.println(FormatUtils.formatBox("  - через роль: " + ra.role().getName()));
                            }
                        }
                    } else {
                        System.out.println(FormatUtils.formatBox("Пользователь НЕ имеет право " + permName + " на ресурс " + resource));
                    }
                });

        parser.registerCommand("help", "Показать справку по командам",
                (scanner, system) -> parser.printHelp());

        parser.registerCommand("stats", "Показать статистику системы",
                (scanner, system) -> {
                    System.out.println(FormatUtils.formatHeader("СТАТИСТИКА СИСТЕМЫ"));
                    System.out.println(system.generateStatistics());

                    List<RoleAssignment> all = system.getAssignmentManager().findAll();
                    long active = system.getAssignmentManager().getActiveAssignments().size();
                    long expired = system.getAssignmentManager().getExpiredAssignments().size();

                    System.out.println(FormatUtils.formatBox("Активных назначений: " + active));
                    System.out.println(FormatUtils.formatBox("Истекших назначений: " + expired));

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

                    System.out.println(FormatUtils.formatBox("\nТоп-3 самых популярных ролей:"));
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
                    System.out.println(FormatUtils.formatBox("Экран очищен"));
                });

        parser.registerCommand("exit", "Выйти из программы",
                (scanner, system) -> {
                    if (ConsoleUtils.promptYesNo(scanner, "Вы уверены, что хотите выйти?")) {
                        System.out.println(FormatUtils.formatBox("До свидания!"));
                        System.exit(0);
                    } else {
                        System.out.println(FormatUtils.formatBox("Выход отменен"));
                    }
                });

        parser.registerCommand("audit-log", "Показать лог аудита",
                (scanner, system) -> {
                    List<String> options = Arrays.asList("Показать лог", "Сохранить в файл");
                    String choice = ConsoleUtils.promptChoice(scanner, "Выберите действие:", options);

                    if (choice.equals("Показать лог")) {
                        system.getAuditLog().printLog();
                    } else {
                        String filename = ConsoleUtils.promptString(scanner, "Имя файла: ", true);
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

                    if (ConsoleUtils.promptYesNo(scanner, "Сохранить отчёт в файл?")) {
                        String filename = ConsoleUtils.promptString(scanner, "Введите имя файла: ", true);
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

                    if (ConsoleUtils.promptYesNo(scanner, "Сохранить отчёт в файл?")) {
                        String filename = ConsoleUtils.promptString(scanner, "Введите имя файла: ", true);
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

                    if (ConsoleUtils.promptYesNo(scanner, "Сохранить отчёт в файл?")) {
                        String filename = ConsoleUtils.promptString(scanner, "Введите имя файла: ", true);
                        system.getReportGenerator().exportToFile(report, filename);
                    }
                });

        parser.registerCommand("report-users-async", "Запустить генерацию отчёта в фоне",
                (scanner, system) -> {
                    System.out.println(FormatUtils.formatBox("Генерация отчёта запущена в фоне..."));
                    system.getBackgroundExecutor().submit(() -> {
                        String report = system.getReportGenerator().generateUserReportParallel(
                                system.getUserManager(),
                                system.getAssignmentManager()
                        );
                        System.out.println("\n" + report);
                        system.getAuditLog().log("REPORT_USERS_ASYNC", system.getCurrentUser(), "system",
                                "Отчёт сгенерирован асинхронно");
                    });
                });

        parser.registerCommand("save-async", "Сохранить данные в файл в фоне",
                (scanner, system) -> {
                    String filename = ConsoleUtils.promptString(scanner, "Введите имя файла: ", true);
                    String report = system.getReportGenerator().generateUserReport(
                            system.getUserManager(),
                            system.getAssignmentManager()
                    );
                    System.out.println(FormatUtils.formatBox("Сохранение запущено в фоне..."));
                    system.getBackgroundExecutor().submit(() -> {
                        system.getReportGenerator().exportToFile(report, filename);
                        system.getAuditLog().log("SAVE_ASYNC", system.getCurrentUser(), filename,
                                "Данные сохранены асинхронно");
                    });
                });
    }

    private static void printUsersTable(List<User> users) {
        String[] headers = {"Username", "Full Name", "Email"};
        List<String[]> rows = new ArrayList<>();

        for (User user : users) {
            String[] row = {
                    user.username(),
                    FormatUtils.truncate(user.fullName(), 20),
                    user.email()
            };
            rows.add(row);
        }

        System.out.println(FormatUtils.formatTable(headers, rows));
    }
}