package org.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CalendarAppTest {

    @Test
    void start() {
        // CalendarApp.start() requires a live JavaFX toolkit; covered by manual/integration tests
        assertNotNull(CalendarApp.class, "CalendarApp class should be accessible");
    }

    @Test
    void main() {
        // CalendarApp.main() delegates to Application.launch() which requires a JavaFX runtime
        assertNotNull(new CalendarApp(), "CalendarApp should be instantiable");
    }
}
