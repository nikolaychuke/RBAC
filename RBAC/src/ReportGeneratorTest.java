import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.*;

public class ReportGeneratorTest {

    private RBACSystem system;
    private ReportGenerator reportGenerator;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    public void setUp() {
        system = new RBACSystem();
        system.initialize();
        reportGenerator = new ReportGenerator();
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    public void testGenerateUserReport() {
        String report = reportGenerator.generateUserReport(
                system.getUserManager(),
                system.getAssignmentManager()
        );

        assertTrue(report.contains("ОТЧЁТ ПО ПОЛЬЗОВАТЕЛЯМ"));
        assertTrue(report.contains("admin"));
        assertTrue(report.contains("Admin"));
    }

    @Test
    public void testGenerateRoleReport() {
        String report = reportGenerator.generateRoleReport(
                system.getRoleManager(),
                system.getAssignmentManager()
        );

        assertTrue(report.contains("ОТЧЁТ ПО РОЛЯМ"));
        assertTrue(report.contains("Admin"));
        assertTrue(report.contains("Manager"));
        assertTrue(report.contains("Viewer"));
        assertTrue(report.contains("Количество прав"));
        assertTrue(report.contains("Пользователей"));
    }

    @Test
    public void testGeneratePermissionMatrix() {
        String report = reportGenerator.generatePermissionMatrix(
                system.getUserManager(),
                system.getAssignmentManager()
        );

        assertTrue(report.contains("МАТРИЦА ПРАВ"));
        assertTrue(report.contains("Пользователь"));
        assertTrue(report.contains("admin"));
    }

    @Test
    public void testExportToFile() throws IOException {
        String filename = "report.txt";
        String testReport = "Test report content";

        reportGenerator.exportToFile(testReport, filename);

        assertTrue(Files.exists(Paths.get(filename)));

        String content = new String(Files.readAllBytes(Paths.get(filename)));
        assertEquals(testReport, content);

        Files.deleteIfExists(Paths.get(filename));
    }

    @Test
    public void testExportToFileWithError() {
        String filename = "/invalid/report";
        String testReport = "Test report";

        reportGenerator.exportToFile(testReport, filename);

        String output = outputStream.toString();
        assertTrue(output.contains("Ошибка при сохранении отчёта"));
    }
}