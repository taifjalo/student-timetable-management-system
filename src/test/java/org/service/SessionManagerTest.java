package org.service;

import org.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Logic Test: SessionManager singleton tests")
class SessionManagerTest {

    @BeforeEach
    void resetSession() {
        // Always start each test with a clean logout
        SessionManager.getInstance().logout();
    }

    @Test
    @DisplayName("Singleton: getInstance should always return the same instance")
    void shouldReturnSameInstance() {
        SessionManager a = SessionManager.getInstance();
        SessionManager b = SessionManager.getInstance();
        assertSame(a, b);
    }

    @Test
    @DisplayName("Initial State: currentUser should be null before login")
    void currentUserShouldBeNullInitially() {
        assertNull(SessionManager.getInstance().getCurrentUser());
    }

    @Test
    @DisplayName("isLoggedIn: Should return false when no user is logged in")
    void shouldReturnFalseWhenNotLoggedIn() {
        assertFalse(SessionManager.getInstance().isLoggedIn());
    }

    @Test
    @DisplayName("Login: Should set current user")
    void shouldSetCurrentUserOnLogin() {
        User user = new User();
        user.setUsername("testUser");

        SessionManager.getInstance().login(user);

        assertNotNull(SessionManager.getInstance().getCurrentUser());
        assertEquals("testUser", SessionManager.getInstance().getCurrentUser().getUsername());
    }

    @Test
    @DisplayName("isLoggedIn: Should return true after login")
    void shouldReturnTrueAfterLogin() {
        User user = new User();
        user.setUsername("testUser");

        SessionManager.getInstance().login(user);

        assertTrue(SessionManager.getInstance().isLoggedIn());
    }

    @Test
    @DisplayName("Logout: Should clear current user")
    void shouldClearCurrentUserOnLogout() {
        User user = new User();
        user.setUsername("testUser");
        SessionManager.getInstance().login(user);

        SessionManager.getInstance().logout();

        assertNull(SessionManager.getInstance().getCurrentUser());
        assertFalse(SessionManager.getInstance().isLoggedIn());
    }

    @Test
    @DisplayName("Login: Should replace existing user on re-login")
    void shouldReplaceUserOnReLogin() {
        User user1 = new User();
        user1.setUsername("user1");

        User user2 = new User();
        user2.setUsername("user2");

        SessionManager.getInstance().login(user1);
        SessionManager.getInstance().login(user2);

        assertEquals("user2", SessionManager.getInstance().getCurrentUser().getUsername());
    }
}