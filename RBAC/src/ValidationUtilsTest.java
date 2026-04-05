import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static org.junit.jupiter.api.Assertions.*;

public class ValidationUtilsTest {

    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    public void setUp() {
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    public void testIsValidUsername() {
        assertTrue(ValidationUtils.isValidUsername("admin"));
        assertTrue(ValidationUtils.isValidUsername("user_123"));
        assertTrue(ValidationUtils.isValidUsername("abc123"));

        assertFalse(ValidationUtils.isValidUsername("ab"));
        assertFalse(ValidationUtils.isValidUsername("abcdefghijklmnopqrstuvwxyz1234567890"));
        assertFalse(ValidationUtils.isValidUsername("user name"));
        assertFalse(ValidationUtils.isValidUsername("user@name"));
        assertFalse(ValidationUtils.isValidUsername("русский"));
        assertFalse(ValidationUtils.isValidUsername(null));
        assertFalse(ValidationUtils.isValidUsername(""));
    }

    @Test
    public void testIsValidEmail() {
        assertTrue(ValidationUtils.isValidEmail("admin@test.com"));
        assertTrue(ValidationUtils.isValidEmail("user123@mail.ru"));

        assertFalse(ValidationUtils.isValidEmail("admin@test"));
        assertFalse(ValidationUtils.isValidEmail("admin@test@com"));
        assertFalse(ValidationUtils.isValidEmail("admin test@com"));
        assertFalse(ValidationUtils.isValidEmail("admin@testcom"));
        assertFalse(ValidationUtils.isValidEmail(""));
        assertFalse(ValidationUtils.isValidEmail(null));
    }

    @Test
    public void testIsValidDate() {
        assertTrue(ValidationUtils.isValidDate("04.04.2026 15:00"));
        assertTrue(ValidationUtils.isValidDate("01.01.2026 00:00"));
        assertTrue(ValidationUtils.isValidDate("31.03.2026 14:30"));

        assertFalse(ValidationUtils.isValidDate("04.04.2026 15:00:00"));
        assertFalse(ValidationUtils.isValidDate("04.04.2026"));
        assertFalse(ValidationUtils.isValidDate("04/04/2026 15:00"));
        assertFalse(ValidationUtils.isValidDate("04.14.2026 15:00"));
        assertFalse(ValidationUtils.isValidDate("32.04.2026 15:00"));
        assertFalse(ValidationUtils.isValidDate("04.04.2026 25:00"));
        assertFalse(ValidationUtils.isValidDate("04.04.2026 15:60"));
        assertFalse(ValidationUtils.isValidDate(""));
        assertFalse(ValidationUtils.isValidDate(null));
    }

    @Test
    public void testNormalizeString() {
        assertEquals("test word", ValidationUtils.normalizeString("  test word  "));
        assertEquals("test word", ValidationUtils.normalizeString("test word"));
        assertEquals("word", ValidationUtils.normalizeString("  word  "));
        assertEquals("test word", ValidationUtils.normalizeString("test word"));
    }

    @Test
    public void testRequireNonEmpty() {

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.requireNonEmpty("", "username");
        });
        assertEquals("username не может быть пустым", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.requireNonEmpty("   ", "email");
        });
        assertEquals("email не может быть пустым", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.requireNonEmpty(null, "password");
        });
        assertEquals("password не может быть пустым", exception.getMessage());
    }

    @Test
    public void testRequireNonEmptyWithDifferentFieldNames() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.requireNonEmpty("", "username");
        });
        assertEquals("username не может быть пустым", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.requireNonEmpty("", "fullName");
        });
        assertEquals("fullName не может быть пустым", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.requireNonEmpty("", "email");
        });
        assertEquals("email не может быть пустым", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.requireNonEmpty("", "expiresAt");
        });
        assertEquals("expiresAt не может быть пустым", exception.getMessage());
    }
}