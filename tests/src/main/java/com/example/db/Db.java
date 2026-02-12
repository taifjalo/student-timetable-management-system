package main.java.com.example.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

public final class Db {

    private static final String DEFAULT_HOST = "timetable-student.mysql.database.azure.com";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DATABASE = "student_timetable";
    private static final String ENV_DB_USER = "DB_USER";
    private static final String ENV_DB_PASSWORD = "DB_PASSWORD";
    private static final String ENV_DB_NAME = "DB_NAME";
    private static final String ENV_DB_HOST = "DB_HOST";
    private static final String ENV_DB_PORT = "DB_PORT";

    private Db() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Connection newConnection() throws SQLException {
        var user = getRequiredEnv(ENV_DB_USER);
        var password = getRequiredEnv(ENV_DB_PASSWORD);
        var database = getEnvOrDefault(ENV_DB_NAME, DEFAULT_DATABASE);
        var host = getEnvOrDefault(ENV_DB_HOST, DEFAULT_HOST);
        var port = getEnvOrDefault(ENV_DB_PORT, DEFAULT_PORT);

        return DriverManager.getConnection(buildJdbcUrl(host, port, database), buildConnectionProperties(user, password));
    }

    public static Connection newConnection(String databaseName) throws SQLException {
        Objects.requireNonNull(databaseName, "databaseName must not be null");
        var user = getRequiredEnv(ENV_DB_USER);
        var password = getRequiredEnv(ENV_DB_PASSWORD);
        var host = getEnvOrDefault(ENV_DB_HOST, DEFAULT_HOST);
        var port = getEnvOrDefault(ENV_DB_PORT, DEFAULT_PORT);

        return DriverManager.getConnection(buildJdbcUrl(host, port, databaseName), buildConnectionProperties(user, password));
    }

    public static boolean testConnection() {
        try (var conn = newConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }

    public static String getJdbcUrl() {
        var host = getEnvOrDefault(ENV_DB_HOST, DEFAULT_HOST);
        var port = getEnvOrDefault(ENV_DB_PORT, DEFAULT_PORT);
        var database = getEnvOrDefault(ENV_DB_NAME, DEFAULT_DATABASE);
        return buildJdbcUrl(host, port, database);
    }

    private static String buildJdbcUrl(String host, String port, String database) {
        return String.format(
            "jdbc:mysql://%s:%s/%s?sslMode=REQUIRED&serverTimezone=UTC&tcpKeepAlive=true" +
            "&connectTimeout=10000&socketTimeout=30000&useServerPrepStmts=true&cachePrepStmts=true",
            host, port, database
        );
    }

    private static Properties buildConnectionProperties(String user, String password) {
        var props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("characterEncoding", "UTF-8");

        // DEV-ONLY: Uncomment if "Public Key Retrieval is not allowed" error occurs
        // props.setProperty("allowPublicKeyRetrieval", "true"); // insecure - temp fix only

        // PRODUCTION: For VERIFY_CA/VERIFY_IDENTITY, configure truststore:
        // props.setProperty("sslMode", "VERIFY_CA");
        // props.setProperty("trustCertificateKeyStoreUrl", "file:/path/to/truststore.jks");
        // props.setProperty("trustCertificateKeyStorePassword", System.getenv("TRUSTSTORE_PASSWORD"));

        return props;
    }

    private static String getRequiredEnv(String name) {
        var value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required environment variable '%s' is not set".formatted(name));
        }
        return value;
    }

    private static String getEnvOrDefault(String name, String defaultValue) {
        var value = System.getenv(name);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}
