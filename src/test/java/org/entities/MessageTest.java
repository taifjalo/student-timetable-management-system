package org.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    private void setId(Message message, Long id) {
        try {
            Field field = Message.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(message, id);
        } catch (ReflectiveOperationException e) {
            fail("Failed to set id via reflection: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Message: Parameterized constructor should set all fields")
    void messageParameterizedConstructor() {
        User sender = new User();
        sender.setId(1L);
        User recipient = new User();
        recipient.setId(2L);
        LocalDateTime now = LocalDateTime.of(2026, 3, 9, 10, 0);

        Message message = new Message(now, "Hello!", sender, recipient);

        assertEquals(now, message.getSentAt());
        assertEquals("Hello!", message.getContent());
        assertSame(sender, message.getSenderUser());
        assertSame(recipient, message.getRecipientUser());
        assertFalse(message.isRead()); // default is false
    }

    @Test
    @DisplayName("Message: setRead should update read status")
    void messageSetRead() {
        Message message = new Message();
        assertFalse(message.isRead());
        message.setRead(true);
        assertTrue(message.isRead());
    }

    @Test
    @DisplayName("Message: compareTo should order by sentAt")
    void messageCompareTo() {
        Message earlier = new Message();
        earlier.setSentAt(LocalDateTime.of(2026, 3, 9, 8, 0));

        Message later = new Message();
        later.setSentAt(LocalDateTime.of(2026, 3, 9, 10, 0));

        assertTrue(earlier.compareTo(later) < 0);
        assertTrue(later.compareTo(earlier) > 0);
        assertEquals(0, earlier.compareTo(earlier));
    }

    @Test
    @DisplayName("Message: All setters should work correctly")
    void messageSetters() {
        User sender = new User();
        User recipient = new User();
        LocalDateTime time = LocalDateTime.now();

        Message message = new Message();
        message.setSentAt(time);
        message.setContent("Test");
        message.setSenderUser(sender);
        message.setRecipientUser(recipient);

        assertEquals(time, message.getSentAt());
        assertEquals("Test", message.getContent());
        assertSame(sender, message.getSenderUser());
        assertSame(recipient, message.getRecipientUser());
    }

    @Test
    @DisplayName("Message: equals/hashCode should use id identity")
    void messageEqualsAndHashCodeById() {
        Message first = new Message();
        setId(first, 11L);

        Message second = new Message();
        setId(second, 11L);

        Message different = new Message();
        setId(different, 12L);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, different);
        assertNotEquals(null, first);
        assertNotEquals(null, first);
    }

    @Test
    @DisplayName("Message: equals/hashCode when id is null")
    void messageEqualsAndHashCodeWhenIdIsNull() {
        Message first = new Message();
        Message second = new Message();

        assertEquals(0, first.hashCode());
        assertNotEquals(first, second);
        assertEquals(first, first);
    }

}
