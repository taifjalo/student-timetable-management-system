package org.service;

import org.entities.User;

/**
 * Application-wide singleton that holds the currently authenticated user.
 * Provides a lightweight session layer for the JavaFX desktop app where there
 * is no HTTP session or security context.
 *
 * <p>Not thread-safe — intended for use on the JavaFX application thread only.
 */
public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    /**
     * Returns the single application-wide instance, creating it on first call.
     *
     * @return the {@code SessionManager} singleton
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Stores the authenticated user, effectively starting a session.
     *
     * @param user the user who has just logged in
     */
    public void login(User user) {
        this.currentUser = user;
    }

    /**
     * Clears the stored user, effectively ending the session.
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Returns the currently logged-in user, or {@code null} if no session is active.
     *
     * @return the current {@link User}, or {@code null}
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Returns {@code true} if a user is currently logged in.
     *
     * @return {@code true} when a session is active
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Returns {@code true} if the currently logged-in user has the {@code "teacher"} role.
     *
     * @return {@code true} for teachers, {@code false} for students or when not logged in
     */
    public boolean isTeacher() {
        return currentUser != null && "teacher".equalsIgnoreCase(currentUser.getRole());
    }
}
