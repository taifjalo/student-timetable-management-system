package test.java.com.example.db;

import main.java.com.example.db.Db;
import org.junit.jupiter.api.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Db Unit Tests")
class DbUnitTest {

    @Test
    @Order(1)
    @DisplayName("Constructor throws UnsupportedOperationException")
    void constructorThrowsException() throws Exception {
        Constructor<Db> constructor = Db.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException ex = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, ex.getCause());
        assertEquals("Utility class", ex.getCause().getMessage());
    }

    @Test
    @Order(2)
    @DisplayName("getJdbcUrl returns valid JDBC URL format")
    void getJdbcUrlReturnsValidFormat() {
        String url = Db.getJdbcUrl();

        assertNotNull(url);
        assertTrue(url.startsWith("jdbc:mysql://"));
        assertTrue(url.contains("sslMode=REQUIRED"));
        assertTrue(url.contains("serverTimezone=UTC"));
    }

    @Test
    @Order(3)
    @DisplayName("newConnection throws IllegalStateException when DB_USER not set")
    void newConnectionThrowsWhenUserNotSet() {
        assumeTrue(System.getenv("DB_USER") == null, "DB_USER is set");

        IllegalStateException ex = assertThrows(IllegalStateException.class, Db::newConnection);
        assertTrue(ex.getMessage().contains("DB_USER"));
    }

    @Test
    @Order(4)
    @DisplayName("newConnection with null database throws NullPointerException")
    void newConnectionWithNullDatabaseThrows() {
        assertThrows(NullPointerException.class, () -> Db.newConnection(null));
    }

    @Test
    @Order(5)
    @DisplayName("newConnection with database throws IllegalStateException when DB_USER not set")
    void newConnectionWithDatabaseThrowsWhenUserNotSet() {
        assumeTrue(System.getenv("DB_USER") == null, "DB_USER is set");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> Db.newConnection("test_db"));
        assertTrue(ex.getMessage().contains("DB_USER"));
    }

    @Test
    @Order(6)
    @DisplayName("testConnection throws IllegalStateException when env vars not set")
    void testConnectionThrowsWhenEnvNotSet() {
        assumeTrue(System.getenv("DB_USER") == null, "DB_USER is set");

        assertThrows(IllegalStateException.class, Db::testConnection);
    }

    @Test
    @Order(7)
    @DisplayName("getJdbcUrl contains default host")
    void getJdbcUrlContainsDefaultHost() {
        assumeTrue(System.getenv("DB_HOST") == null, "DB_HOST is set");

        String url = Db.getJdbcUrl();
        assertTrue(url.contains("timetable-student.mysql.database.azure.com"));
    }

    @Test
    @Order(8)
    @DisplayName("getJdbcUrl contains default port")
    void getJdbcUrlContainsDefaultPort() {
        assumeTrue(System.getenv("DB_PORT") == null, "DB_PORT is set");

        String url = Db.getJdbcUrl();
        assertTrue(url.contains(":3306/"));
    }

    @Test
    @Order(9)
    @DisplayName("getJdbcUrl contains default database")
    void getJdbcUrlContainsDefaultDatabase() {
        assumeTrue(System.getenv("DB_NAME") == null, "DB_NAME is set");

        String url = Db.getJdbcUrl();
        assertTrue(url.contains("/student_timetable?"));
    }
}
