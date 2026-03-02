import eu.hansolo.tilesfx.addons.Switch;
import org.dao.UserDao;
import org.entities.User;
import org.service.AuthService;

import java.util.Scanner;
import java.util.UUID;


import java.util.Scanner;
import java.util.UUID;

public class Main {

    public static void main(String[] args) {

        UserDao userDao = new UserDao();
        AuthService authService = new AuthService(userDao);

        Scanner sc = new Scanner(System.in);

        System.out.println("""
                                Choose the test type for Auth End-to-End (Integration purpose):
                                1. Manual test
                                2. Automatic test
                                """);

        int input = sc.nextInt();
        sc.nextLine(); // consume newline

        switch (input) {

            case 1:

                System.out.println("""
                                    Choose operation:
                                    1. Login - Hint use (Username: "usernew", Password: "12345678")
                                    2. Register
                                    """);

                int operation = sc.nextInt();
                sc.nextLine(); // consume newline

                if (operation == 1) {

                    System.out.print("Enter username: ");
                    String username = sc.nextLine();

                    System.out.print("Enter password: ");
                    String password = sc.nextLine();

                    try {
                        User loggedUser = authService.login(username, password);
                        System.out.println("BE HAPPY! Login success: " + loggedUser.getUsername());
                    } catch (Exception e) {
                        System.out.println("Login failed: " + e.getMessage());
                    }

                } else if (operation == 2) {

                    System.out.print("Enter name: ");
                    String name = sc.nextLine();

                    System.out.print("Enter surname: ");
                    String surname = sc.nextLine();

                    System.out.print("Enter username: ");
                    String username = sc.nextLine();

                    System.out.print("Enter email: ");
                    String email = sc.nextLine();

                    System.out.print("Enter phone: ");
                    String phone = sc.nextLine();

                    System.out.print("Enter password: ");
                    String password = sc.nextLine();

                    try {
                        authService.register(
                                name,
                                surname,
                                username,
                                email,
                                phone,
                                password
                        );
                        System.out.println("User registered successfully");
                    } catch (Exception e) {
                        System.out.println("Register failed: " + e.getMessage());
                    }
                }

                break;

            case 2:

                // Automatic Test
                try {
                    User loggedUser = authService.login("usernew", "12345678");
                    System.out.println("Login success: " + loggedUser.getUsername());
                } catch (Exception e) {
                    System.out.println("Login failed: " + e.getMessage());
                }

                authService.register(
                        "User",
                        "New",
                        "Username" + UUID.randomUUID(),
                        "user" + UUID.randomUUID() + "@test.com",
                        "090" + UUID.randomUUID().toString().substring(0,7),
                        "123" + UUID.randomUUID().toString().substring(0,7)
                );

                System.out.println("Automatic test executed");

                break;

            default:
                System.out.println("Invalid choice.");
        }

        sc.close();
    }
}