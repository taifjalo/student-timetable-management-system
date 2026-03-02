
package org;

import org.dao.MessageDao;
import org.dao.UserDao;
import org.entities.Message;
import org.entities.User;
import org.service.AuthService;

import java.time.LocalDateTime;



public class Main {
    public static void main(String[] args) {

        System.out.println("All Tests are applied for End-to-End for func Integration Test purpose: ");

        UserDao userDao = new UserDao();
        AuthService authService = new AuthService(userDao);

        // Login Test:
        try {
            User loggedUser = authService.login("usernew", "12345678");
            System.out.println("Login success: " + loggedUser.getUsername());

        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
        }

        // Register Test:
        authService.register(
                "User",
                "New",
                "usernew2",
                "user0@example.com",
                "07712345670",
                "123456780"
        );
        System.out.println("User registered successfully");
    }
}
