package org.controllers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ChatControllerTest {

    @Test
    void controllerIsInstantiable() {
        // ChatController requires a JavaFX scene graph for @FXML injection
        // UI behaviour is covered by manual/integration tests
        assertNotNull(new ChatController(), "ChatController should be instantiable without a scene");
    }
}
