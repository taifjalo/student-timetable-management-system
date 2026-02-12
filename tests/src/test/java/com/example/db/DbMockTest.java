package test.java.com.example.db;

import main.java.com.example.db.Db;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Db Mock Tests")
class DbMockTest {

    @Test
    @DisplayName("testConnection returns false when connection fails")
    void testConnectionReturnsFalseWhenConnectionFails() {
        SQLException exception = new SQLException("Connection refused");
        
        try (MockedStatic<DriverManager> mockedDriver = mockStatic(DriverManager.class)) {
            mockedDriver.when(() -> DriverManager.getConnection(anyString(), any(Properties.class)))
                    .thenThrow(exception);

            boolean result = Db.testConnection();
            
            assertFalse(result);
        }
    }
}
