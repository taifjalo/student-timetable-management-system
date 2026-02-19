package org;

import org.entities.User;
import org.service.AuthService;

public class testLogin {
    public static void main(String[] args) {
        AuthService authService = new AuthService();

        try {
            User loggedUser = authService.login("usernew", "12345678");
            System.out.println("Login success: " + loggedUser.getUsername());

        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
        }

    }
}
