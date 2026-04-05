import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class CommandParserTest {

    private RBACSystem system;
    private CommandParser parser;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    public void setUp() {
        system = new RBACSystem();
        system.initialize();
        parser = new CommandParser();
        CommandRegistry.registerAllCommands(parser);
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    public void testHelpCommand() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        parser.parseAndExecute("help", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("СПИСОК КОМАНД"));
        assertTrue(output.contains("user-list"));
        assertTrue(output.contains("role-list"));
        assertTrue(output.contains("help"));
        assertTrue(output.contains("stats"));
    }

    @Test
    public void testStatsCommand() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        parser.parseAndExecute("stats", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("СТАТИСТИКА СИСТЕМЫ"));
        assertTrue(output.contains("Пользователей:"));
        assertTrue(output.contains("Ролей:"));
        assertTrue(output.contains("Назначений:"));
        assertTrue(output.contains("Активных назначений:"));
        assertTrue(output.contains("Среднее количество ролей"));
        assertTrue(output.contains("Топ-3 самых популярных ролей"));
    }

    @Test
    public void testClearCommand() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        parser.parseAndExecute("clear", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("Экран очищен"));
        assertTrue(output.length() > 50);
    }

    @Test
    public void testUnknownCommand() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        parser.parseAndExecute("unknowncommand", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("Неизвестная команда"));
    }

    @Test
    public void testEmptyInput() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        parser.parseAndExecute("", scanner, system);

        String output = outputStream.toString();
        assertEquals("", output);
    }

    @Test
    public void testUserListCommand() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        parser.parseAndExecute("user-list", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("admin"));
        assertTrue(output.contains("Administrator"));
    }

    @Test
    public void testUserListWithUsernameParam() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        parser.parseAndExecute("user-list --username admin", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("Фильтр по username: admin"));
        assertTrue(output.contains("admin"));
    }

    @Test
    public void testUserListWithEmailParam() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        parser.parseAndExecute("user-list --email admin", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("Фильтр по email: admin"));
        assertTrue(output.contains("admin@test.com"));
    }

    @Test
    public void testUserListWithDomainParam() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        parser.parseAndExecute("user-list --domain @test.com", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("Фильтр по домену: @test.com"));
        assertTrue(output.contains("admin@test.com"));
    }

    @Test
    public void testUserListWithFullNameParam() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        parser.parseAndExecute("user-list --fullname Administrator", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("Фильтр по полному имени: Administrator"));
        assertTrue(output.contains("Administrator"));
    }

    @Test
    public void testUserListWithInvalidParam() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        parser.parseAndExecute("user-list --invalid", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("Ошибка: Неизвестный параметр"));
    }

    @Test
    public void testUserListWithMissingValue() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        parser.parseAndExecute("user-list --username", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("требует значение"));
    }

    @Test
    public void testUserCreateCommand() {
        String input = "testuser\nTest User\ntest@test.com\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("user-create", scanner, system);

        assertTrue(system.getUserManager().exists("testuser"));
        String output = outputStream.toString();
        assertTrue(output.contains("Пользователь успешно создан"));
    }

    @Test
    public void testUserCreateWithInvalidData() {
        String input = "jo\nTest User\ninvalid\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("user-create", scanner, system);

        assertFalse(system.getUserManager().exists("jo"));
        String output = outputStream.toString();
        assertTrue(output.contains("Ошибка"));
    }

    @Test
    public void testUserCreateDuplicate() {
        User user = User.create("duplicate", "Duplicate User", "dup@test.com");
        system.getUserManager().add(user);

        String input = "duplicate\nDuplicate User\ndup@test.com\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("user-create", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("уже есть"));
    }

    @Test
    public void testUserViewCommand() {
        String input = "admin\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("user-view", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("ИНФОРМАЦИЯ О ПОЛЬЗОВАТЕЛЕ"));
        assertTrue(output.contains("admin"));
        assertTrue(output.contains("Administrator"));
        assertTrue(output.contains("admin@test.com"));
        assertTrue(output.contains("Назначенные роли"));
        assertTrue(output.contains("Admin"));
    }

    @Test
    public void testUserViewNotFound() {
        String input = "nonexistent\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("user-view", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("Пользователь не найден"));
    }

    @Test
    public void testUserUpdateCommand() {
        String input = "admin\nNew Admin Name\nnewadmin@test.com\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("user-update", scanner, system);

        User updated = system.getUserManager().findByUsername("admin").get();
        assertEquals("New Admin Name", updated.fullName());
        assertEquals("newadmin@test.com", updated.email());

        String output = outputStream.toString();
        assertTrue(output.contains("Пользователь успешно обновлен"));
    }

    @Test
    public void testUserUpdateNotFound() {
        String input = "nonexistent\nNew Name\nemail@test.com\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("user-update", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("Пользователь не найден"));
    }

    @Test
    public void testUserDeleteCommand() {
        User user = User.create("todelete", "To Delete", "delete@test.com");
        system.getUserManager().add(user);

        String input = "todelete\nда\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("user-delete", scanner, system);

        assertFalse(system.getUserManager().exists("todelete"));
        String output = outputStream.toString();
        assertTrue(output.contains("Пользователь todelete удален"));
    }

    @Test
    public void testUserSearchCommandByUsername() {
        String input = "1\nadmin\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("user-search", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("admin"));
    }

    @Test
    public void testUserSearchCommandByEmail() {
        String input = "2\nadmin\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("user-search", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("admin@test.com"));
    }

    @Test
    public void testUserSearchCommandByDomain() {
        String input = "3\n@test.com\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("user-search", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("admin@test.com"));
    }

    @Test
    public void testUserSearchCommandByFullName() {
        String input = "4\nAdministrator\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("user-search", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("Administrator"));
    }

    @Test
    public void testUserSearchInvalidChoice() {
        String input = "5\n1\nadmin\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("user-search", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("Ошибка: Введите число от 1 до 4"));
        assertTrue(output.contains("admin"));
    }

    @Test
    public void testRoleListCommand() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        parser.parseAndExecute("role-list", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("Admin"));
        assertTrue(output.contains("Manager"));
        assertTrue(output.contains("Viewer"));
    }

    @Test
    public void testRoleCreateCommand() {
        String input = "TestRole\nTest Description\nнет\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("role-create", scanner, system);

        assertTrue(system.getRoleManager().exists("TestRole"));
        String output = outputStream.toString();
        assertTrue(output.contains("Роль создана: TestRole"));
    }

    @Test
    public void testRoleCreateWithPermissions() {
        String input = "TestRoleWithPerm\nDescription\nда\nREAD\nusers\nRead users\nнет\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("role-create", scanner, system);

        Role role = system.getRoleManager().findByName("TestRoleWithPerm").get();
        assertEquals(1, role.getPermissions().size());
        assertTrue(role.hasPermission("READ", "users"));
    }

    @Test
    public void testRoleViewCommand() {
        String input = "Admin\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("role-view", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("Role: Admin"));
        assertTrue(output.contains("Полный доступ к системе"));
    }

    @Test
    public void testRoleViewNotFound() {
        String input = "Nonexistent\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("role-view", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("Роль не найдена"));
    }

    @Test
    public void testRoleUpdateCommand() {
        String input = "Manager\nNewManager\nUpdated Description\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("role-update", scanner, system);

        Role role = system.getRoleManager().findByName("NewManager").get();
        assertEquals("NewManager", role.getName());
        assertEquals("Updated Description", role.getDescription());
    }

    @Test
    public void testRoleDeleteCommandWithoutAssignments() {
        Role role = new Role("TestRoleDel", "Test Description");
        system.getRoleManager().add(role);

        String input = "TestRoleDel\nда\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("role-delete", scanner, system);

        assertFalse(system.getRoleManager().exists("TestRoleDel"));
        String output = outputStream.toString();
        assertTrue(output.contains("Роль TestRoleDel удалена"));
    }

    @Test
    public void testRoleAddPermissionCommand() {
        String input = "Viewer\nWRITE\nreports\nWrite reports\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("role-add-permission", scanner, system);

        Role viewer = system.getRoleManager().findByName("Viewer").get();
        assertTrue(viewer.hasPermission("WRITE", "reports"));
    }

    @Test
    public void testRoleRemovePermissionCommand() {
        String input = "Viewer\n1\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        Role viewer = system.getRoleManager().findByName("Viewer").get();
        int initialCount = viewer.getPermissions().size();

        parser.parseAndExecute("role-remove-permission", scanner, system);

        assertEquals(initialCount - 1, viewer.getPermissions().size());
    }

    @Test
    public void testRoleSearchByName() {
        String input = "1\nAdmin\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("role-search", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("Role: Admin"));
    }

    @Test
    public void testRoleSearchByPermission() {
        String input = "2\nREAD\nusers\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("role-search", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("READ") || output.contains("Admin") || output.contains("Manager"));
    }

    @Test
    public void testRoleSearchByMinPermissions() {
        String input = "3\n5\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("role-search", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("Role: Admin"));
    }

    @Test
    public void testRoleSearchInvalidChoice() {
        String input = "4\n1\nAdmin\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("role-search", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("Ошибка: Введите число от 1 до 3"));
        assertTrue(output.contains("Admin"));
    }

    @Test
    public void testAssignRoleUserNotFound() {
        String input = "nonexistent\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("assign-role", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("Пользователь не найден"));
    }

    @Test
    public void testRevokeRoleNoAssignments() {
        User user = User.create("nouser", "No User", "no@test.com");
        system.getUserManager().add(user);

        String input = "nouser\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("revoke-role", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("У пользователя нет назначенных ролей"));
    }

    @Test
    public void testAssignmentListCommand() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        parser.parseAndExecute("assignment-list", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("admin"));
        assertTrue(output.contains("Admin"));
    }

    @Test
    public void testAssignmentListUserCommand() {
        String input = "admin\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("assignment-list-user", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("НАЗНАЧЕНИЕ"));
        assertTrue(output.contains("Admin"));
        assertTrue(output.contains("PERMANENT"));
    }

    @Test
    public void testAssignmentListRoleCommand() {
        String input = "Admin\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("assignment-list-role", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("Пользователи с ролью Admin"));
        assertTrue(output.contains("admin"));
    }

    @Test
    public void testAssignmentActiveCommand() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        parser.parseAndExecute("assignment-active", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("admin"));
        assertTrue(output.contains("Admin"));
    }

    @Test
    public void testAssignmentExpiredCommand() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        parser.parseAndExecute("assignment-expired", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("Нет истёкших назначений") || output.contains("Истекло"));
    }

    @Test
    public void testAssignmentExtendCommand() {
        User admin = system.getUserManager().findByUsername("admin").get();
        Role viewer = system.getRoleManager().findByName("Viewer").get();
        AssignmentMetadata metadata = AssignmentMetadata.now("system", "Test");
        TemporaryAssignment temp = new TemporaryAssignment(admin, viewer, metadata,
                "31.12.2026 23:59", false);
        system.getAssignmentManager().add(temp);

        String input = "admin\n1\n31.12.2027 23:59\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("assignment-extend", scanner, system);

        assertEquals("31.12.2027 23:59", temp.getExpiresAt());
        String output = outputStream.toString();
        assertTrue(output.contains("Назначение продлено"));
    }

    @Test
    public void testAssignmentSearchByUser() {
        String input = "1\nadmin\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("assignment-search", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("admin"));
        assertTrue(output.contains("Admin"));
    }

    @Test
    public void testAssignmentSearchByRole() {
        String input = "2\nAdmin\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("assignment-search", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("admin"));
        assertTrue(output.contains("Admin"));
    }

    @Test
    public void testAssignmentSearchByType() {
        String input = "3\n1\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("assignment-search", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("admin"));
        assertTrue(output.contains("Admin"));
    }

    @Test
    public void testAssignmentSearchByStatus() {
        String input = "4\n1\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("assignment-search", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("admin"));
        assertTrue(output.contains("Admin"));
    }

    @Test
    public void testAssignmentSearchInvalidChoice() {
        String input = "5\n1\nadmin\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("assignment-search", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("Ошибка: Введите число от 1 до 4"));
        assertTrue(output.contains("admin"));
    }

    @Test
    public void testPermissionsUserCommand() {
        String input = "admin\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("permissions-user", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("Права пользователя admin"));
        assertTrue(output.contains("READ") || output.contains("WRITE") || output.contains("DELETE"));
    }

    @Test
    public void testPermissionsUserNotFound() {
        String input = "nonexistent\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("permissions-user", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("Пользователь не найден"));
    }

    @Test
    public void testPermissionsCheckCommand() {
        String input = "admin\nREAD\nusers\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("permissions-check", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("Пользователь имеет право READ на ресурс users"));
        assertTrue(output.contains("через роль: Admin"));
    }

    @Test
    public void testPermissionsCheckNoPermission() {
        User user = User.create("noperm", "No Perm", "noperm@test.com");
        system.getUserManager().add(user);

        String input = "noperm\nREAD\nusers\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("permissions-check", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("Пользователь НЕ имеет право READ на ресурс users"));
    }

    @Test
    public void testPermissionsCheckUserNotFound() {
        String input = "nonexistent\nREAD\nusers\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        parser.parseAndExecute("permissions-check", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("Пользователь не найден"));
    }

    @Test
    public void testExecuteCommandDirect() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        parser.executeCommand("help", scanner, system);

        String output = outputStream.toString();
        assertTrue(output.contains("СПИСОК КОМАНД"));
    }

    @Test
    public void testExecuteUnknownCommand() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        parser.executeCommand("nonexistent", scanner, system);

        String output = outputStream.toString();
    }
}