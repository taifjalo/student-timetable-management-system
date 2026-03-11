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

    private User createUser(String role) {
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setRole(role);
        return user;
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
    @DisplayName("Login: Should set current user")
    void shouldSetCurrentUserOnLogin() {

        SessionManager.getInstance().login(createUser("teacher"));

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


    // Role is Teacher


    @Test
    @DisplayName("isTeacher: Should return true when role is 'teacher'")
    void shouldReturnTrueForTeacherRole() {
        SessionManager.getInstance().login(createUser("teacher"));
        assertTrue(SessionManager.getInstance().isTeacher());
    }

    @Test
    @DisplayName("isTeacher: Should return true when role is 'TEACHER' (uppercase)")
    void shouldReturnTrueForUppercaseTeacher() {
        SessionManager.getInstance().login(createUser("TEACHER"));
        assertTrue(SessionManager.getInstance().isTeacher());
    }

    @Test
    @DisplayName("isTeacher: Should return false when role is 'student'")
    void shouldReturnFalseForStudentRole() {
        SessionManager.getInstance().login(createUser("student"));
        assertFalse(SessionManager.getInstance().isTeacher());
    }

    @Test
    @DisplayName("isTeacher: Should return false when no user is logged in")
    void shouldReturnFalseWhenNotLoggedIn() {
        assertFalse(SessionManager.getInstance().isTeacher());
    }

    @Test
    @DisplayName("isTeacher: Should return false when role is null")
    void shouldReturnFalseWhenRoleIsNull() {
        SessionManager.getInstance().login(createUser(null));
        assertFalse(SessionManager.getInstance().isTeacher());
    }

    @Test
    @DisplayName("isLoggedIn: Should be true after login")
    void shouldBeLoggedInAfterLogin() {
        SessionManager.getInstance().login(createUser("student"));
        assertTrue(SessionManager.getInstance().isLoggedIn());
    }

    @Test
    @DisplayName("isLoggedIn: Should be false after logout")
    void shouldNotBeLoggedInAfterLogout() {
        SessionManager.getInstance().login(createUser("teacher"));
        SessionManager.getInstance().logout();
        assertFalse(SessionManager.getInstance().isLoggedIn());
    }
}