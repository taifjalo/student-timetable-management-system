package org;

import org.service.AuthService;

public class testRegister {
    public static void main(String[] args) {

        AuthService authService = new AuthService();

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
