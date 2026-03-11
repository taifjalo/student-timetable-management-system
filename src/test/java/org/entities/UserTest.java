package org.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    @Test
    @DisplayName("User: All getters and setters should work correctly")
    void userGettersAndSetters() {
        User user = new User();
        user.setId(10L);
        user.setFirstName("Alice");
        user.setSureName("Smith");
        user.setUsername("alice123");
        user.setEmail("alice@example.com");
        user.setPhoneNumber("0401234567");
        user.setPasswordHash("hashedPw");
        user.setRole("student");

        assertEquals(10L, user.getId());
        assertEquals("Alice", user.getFirstName());
        assertEquals("Smith", user.getSureName());
        assertEquals("alice123", user.getUsername());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals("0401234567", user.getPhoneNumber());
        assertEquals("hashedPw", user.getPasswordHash());
        assertEquals("student", user.getRole());
    }

}