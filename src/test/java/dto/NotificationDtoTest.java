package dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DTO Tests: NotificationDto")
class NotificationDtoTest {
    @Test
    @DisplayName("NotificationDto: Constructor should set all fields correctly")
    void notificationDtoConstructor() {
        LocalDateTime sentAt = LocalDateTime.of(2026, 3, 9, 12, 0);
        NotificationDto dto = new NotificationDto(10L, "Test notification", sentAt, false);

        assertEquals(10L, dto.getNotificationId());
        assertEquals("Test notification", dto.getContent());
        assertEquals(sentAt, dto.getSentAt());
        assertFalse(dto.isRead());
    }

    @Test
    @DisplayName("NotificationDto: isRead true should be reflected")
    void notificationDtoIsReadTrue() {
        LocalDateTime sentAt = LocalDateTime.now();
        NotificationDto dto = new NotificationDto(1L, "Content", sentAt, true);

        assertTrue(dto.isRead());
    }
}
