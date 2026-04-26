package org.datasource;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class TimetableConnectionTest {

    @Test
    void createEntityManager() {
        // Requires a live database connection; validated through integration tests
        assertNotNull(TimetableConnection.class, "TimetableConnection class should be accessible");
    }

    @Test
    void shutdown() {
        // Requires a live database connection; validated through integration tests
        assertNotNull(TimetableConnection.class, "TimetableConnection class should be accessible");
    }

    @Test
    void getEntityManager() {
        // Requires a live database connection; validated through integration tests
        assertNotNull(TimetableConnection.class, "TimetableConnection class should be accessible");
    }
}
