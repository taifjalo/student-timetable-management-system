package org.service;

import org.entities.User;

public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /** Returns true if the currently logged-in user has the "teacher" role. */
    public boolean isTeacher() {
        return currentUser != null && "teacher".equalsIgnoreCase(currentUser.getRole());
    }
}

