package dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DTO Tests: ChatPreview")
class ChatPreviewTest {
    @Test
    @DisplayName("ChatPreview: Constructor should set all fields correctly")
    void chatPreviewConstructor() {
        ChatPreview preview = new ChatPreview(1L, "Alice", "Smith", true);

        assertEquals(1L, preview.getUserId());
        assertEquals("Alice", preview.getName());
        assertEquals("Smith", preview.getSurname());
        assertTrue(preview.getIsRead());
    }

    @Test
    @DisplayName("ChatPreview: setIsRead should update isRead value")
    void chatPreviewSetIsRead() {
        ChatPreview preview = new ChatPreview(1L, "Alice", "Smith", true);

        preview.setIsRead(false);
        assertFalse(preview.getIsRead());

        preview.setIsRead(true);
        assertTrue(preview.getIsRead());
    }

    @Test
    @DisplayName("ChatPreview: isReadProperty should reflect current isRead value")
    void chatPreviewIsReadProperty() {
        ChatPreview preview = new ChatPreview(2L, "Bob", "Jones", false);

        assertFalse(preview.isReadProperty().get());

        preview.setIsRead(true);
        assertTrue(preview.isReadProperty().get());
    }

}