package test.java.com.example.db;

import main.java.com.example.db.Db;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Db Integration Tests")
class DbIntegrationTest {

    @BeforeAll
    static void checkEnvironment() {
        assumeTrue(
            System.getenv("DB_USER") != null && System.getenv("DB_PASSWORD") != null,
            "Skipping integration tests: DB_USER and DB_PASSWORD must be set"
        );
    }

    @Test
    @Order(1)
    @DisplayName("testConnection returns true")
    void testConnectionReturnsTrue() {
        assertTrue(Db.testConnection());
    }

    @Test
    @Order(2)
    @DisplayName("newConnection creates valid connection")
    void newConnectionCreatesValidConnection() throws Exception {
        try (Connection conn = Db.newConnection()) {
            assertNotNull(conn);
            assertTrue(conn.isValid(5));
        }
    }

    @Test
    @Order(3)
    @DisplayName("newConnection with database name creates valid connection")
    void newConnectionWithDatabaseCreatesValidConnection() throws Exception {
        String dbName = System.getenv("DB_NAME");
        if (dbName == null) {
            dbName = "student_timetable";
        }

        try (Connection conn = Db.newConnection(dbName)) {
            assertNotNull(conn);
            assertTrue(conn.isValid(5));
        }
    }

    @Test
    @Order(4)
    @DisplayName("Execute SELECT 1 query")
    void executeSimpleQuery() throws Exception {
        try (Connection conn = Db.newConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1 AS result")) {

            assertTrue(rs.next());
            assertEquals(1, rs.getInt("result"));
        }
    }

    @Test
    @Order(5)
    @DisplayName("getJdbcUrl returns configured URL")
    void getJdbcUrlReturnsConfiguredUrl() {
        String url = Db.getJdbcUrl();

        assertNotNull(url);
        assertTrue(url.startsWith("jdbc:mysql://"));

        String host = System.getenv("DB_HOST");
        if (host != null) {
            assertTrue(url.contains(host));
        }
    }
}
