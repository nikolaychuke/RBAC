import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DateUtilsTest {

    @Test
    public void testGetCurrentDate() {
        String currentDate = DateUtils.getCurrentDate();
        assertNotNull(currentDate);
        assertTrue(currentDate.matches("\\d{2}\\.\\d{2}\\.\\d{4}"));
    }

    @Test
    public void testGetCurrentDateTime() {
        String currentDateTime = DateUtils.getCurrentDateTime();
        assertNotNull(currentDateTime);
        assertTrue(currentDateTime.matches("\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    public void testIsBefore() {
        assertTrue(DateUtils.isBefore("05.04.2026", "06.04.2026"));
        assertFalse(DateUtils.isBefore("07.04.2026", "06.04.2026"));
        assertFalse(DateUtils.isBefore("06.04.2026", "06.04.2026"));
        assertFalse(DateUtils.isBefore(null, "06.04.2026"));
        assertFalse(DateUtils.isBefore("05.04.2026", null));
    }

    @Test
    public void testIsAfter() {
        assertTrue(DateUtils.isAfter("07.04.2026", "06.04.2026"));
        assertFalse(DateUtils.isAfter("05.04.2026", "06.04.2026"));
        assertFalse(DateUtils.isAfter("06.04.2026", "06.04.2026"));
        assertFalse(DateUtils.isAfter(null, "06.04.2026"));
        assertFalse(DateUtils.isAfter("07.04.2026", null));
    }

    @Test
    public void testAddDays() {
        assertEquals("10.04.2026", DateUtils.addDays("06.04.2026", 4));
        assertEquals("30.03.2026", DateUtils.addDays("06.04.2026", -7));
        assertNull(DateUtils.addDays(null, 5));
    }

    @Test
    public void testFormatRelativeTime() {
        String result = DateUtils.formatRelativeTime(DateUtils.getCurrentDate());
        assertEquals("today", result);
    }

    @Test
    public void testFormatRelativeTimePast() {
        String result = DateUtils.formatRelativeTime("04.04.2020");
        assertTrue(result.contains("days ago") || result.contains("day ago"));
    }

    @Test
    public void testFormatRelativeTimeFuture() {
        String result = DateUtils.formatRelativeTime("04.04.2030");
        assertTrue(result.contains("in") && result.contains("days"));
    }
}