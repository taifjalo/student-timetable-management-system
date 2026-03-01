package org;

import org.dao.MessageDao;
import org.entities.Message;
import org.entities.User;
import org.service.AuthService;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {

        AuthService authService = new AuthService();
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




        // Message Test
        MessageDao dao = new MessageDao();

        Message m = new Message(LocalDateTime.now(), "hello", 1, 1);


        dao.create(m);

        System.out.println("Saved. Total: " + dao.findAll().size());

    }
}
