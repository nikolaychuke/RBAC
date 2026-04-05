import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class AuditLogTest {

    private AuditLog auditLog;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    public void setUp() {
        auditLog = new AuditLog();
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    public void testLog() {
        auditLog.log("CREATE_USER", "admin", "testuser", "Test User");

        List<AuditLog.AuditEntry> entries = auditLog.getAll();
        assertEquals(1, entries.size());

        AuditLog.AuditEntry entry = entries.get(0);
        assertEquals("CREATE_USER", entry.action());
        assertEquals("admin", entry.performer());
        assertEquals("testuser", entry.target());
        assertEquals("Test User", entry.details());
        assertNotNull(entry.timestamp());
    }

    @Test
    public void testGetAll() {
        auditLog.log("CREATE_USER", "admin", "user1", "details1");
        auditLog.log("DELETE_USER", "admin", "user2", "details2");

        List<AuditLog.AuditEntry> entries = auditLog.getAll();
        assertEquals(2, entries.size());
    }

    @Test
    public void testGetByPerformer() {
        auditLog.log("CREATE_USER", "admin", "user1", "details1");
        auditLog.log("DELETE_ROLE", "manager", "role1", "details2");
        auditLog.log("ASSIGN_ROLE", "admin", "user2", "details3");

        List<AuditLog.AuditEntry> adminEntries = auditLog.getByPerformer("admin");
        assertEquals(2, adminEntries.size());

        List<AuditLog.AuditEntry> managerEntries = auditLog.getByPerformer("manager");
        assertEquals(1, managerEntries.size());

        List<AuditLog.AuditEntry> unknownEntries = auditLog.getByPerformer("unknown");
        assertEquals(0, unknownEntries.size());
    }

    @Test
    public void testGetByAction() {
        auditLog.log("CREATE_USER", "admin", "user1", "details1");
        auditLog.log("CREATE_USER", "manager", "user2", "details2");
        auditLog.log("DELETE_ROLE", "admin", "role1", "details3");

        List<AuditLog.AuditEntry> createEntries = auditLog.getByAction("CREATE_USER");
        assertEquals(2, createEntries.size());

        List<AuditLog.AuditEntry> deleteEntries = auditLog.getByAction("DELETE_ROLE");
        assertEquals(1, deleteEntries.size());

        List<AuditLog.AuditEntry> unknownEntries = auditLog.getByAction("UNKNOWN");
        assertEquals(0, unknownEntries.size());
    }

    @Test
    public void testPrintLogEmpty() {
        auditLog.printLog();
        String output = outputStream.toString();
        assertTrue(output.contains("Лог аудита пуст"));
    }

    @Test
    public void testPrintLogWithEntries() {
        auditLog.log("CREATE_USER", "admin", "testuser", "Test details");
        auditLog.printLog();

        String output = outputStream.toString();
        assertTrue(output.contains("CREATE_USER"));
        assertTrue(output.contains("admin"));
        assertTrue(output.contains("testuser"));
    }

    @Test
    public void testSaveToFile() throws IOException {
        String filename = "test_audit.csv";

        auditLog.log("CREATE_USER", "admin", "user1", "details1");
        auditLog.saveToFile(filename);

        assertTrue(Files.exists(Paths.get(filename)));

        String content = new String(Files.readAllBytes(Paths.get(filename)));
        assertTrue(content.contains("CREATE_USER"));
        assertTrue(content.contains("admin"));

        Files.deleteIfExists(Paths.get(filename));
    }
}