import org.dao.MessageDao;
import org.dao.UserDao;
import org.entities.User;
import org.service.AuthService;
import org.service.ChatService;

import java.util.Scanner;
import java.util.UUID;


import java.util.Scanner;
import java.util.UUID;

public class Main {

    private static User createUser(AuthService authService) {
        return authService.register(
                "User",
                "New",
                "Username" + UUID.randomUUID(),
                "user" + UUID.randomUUID() + "@test.com",
                "090" + UUID.randomUUID().toString().substring(0,7),
                "123" +UUID.randomUUID().toString().substring(0,7)
        );
    }

    public static void main(String[] args) {

        UserDao userDao = new UserDao();
        AuthService authService = new AuthService(userDao);

        MessageDao messageDao = new MessageDao();
        ChatService chatService = new ChatService(messageDao);


        Scanner sc = new Scanner(System.in);

        System.out.println("""
                            Choose the test type for Auth and Message End-to-End Simulation:
                            1. Manual test
                            2. Automatic test
                            """);

        int input = sc.nextInt();
        sc.nextLine(); // consume newline

        switch (input) {

            case 1:

                System.out.println("""
                                    Choose Manual operation:
                                    1. Login - Hint use (Username: "user", Password: "12345678")
                                    2. Register
                                    3. Message
                                    """);

                int operationM = sc.nextInt();
                sc.nextLine(); // consume newline

                if (operationM == 1) {

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

                } else if (operationM == 2) {

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
                } else if (operationM == 3) {
                    try {
                        User sender = createUser(authService);
                        User recipient = createUser(authService);

                        System.out.print("\\n\"Enter Your Message: " + "\n");
                        String message = sc.nextLine();

                        messageDao.saveMessage(sender.getId(), recipient.getId(), message);

                        System.out.println(
                                "Sent to : " + recipient.getUsername() + "\n" +
                                "From:     " + sender.getUsername() + "\n" +
                                "Message:  " + message
                        );

                    } catch (Exception e) {
                        System.out.println("Message sending field: " + e.getMessage());
                    }
                }

                break;

            case 2:

                System.out.println("""
                                    Choose Automatic operation:
                                    1. Login
                                    2. Register
                                    3. Message
                                    """);

                int operationA = sc.nextInt();
                sc.nextLine(); // consume newline

                if (operationA == 1) {
                    // Automatic Test
                    try {
                        User loggedUser = authService.login("user", "12345678");
                        System.out.println("Login success: " + loggedUser.getUsername());
                    } catch (Exception e) {
                        System.out.println("Login failed: " + e.getMessage());
                    }
                }

                else if (operationA == 2) {
                    try {
                        authService.register(
                                "User",
                                "New",
                                "Username" + UUID.randomUUID(),
                                "user" + UUID.randomUUID() + "@test.com",
                                "090" + UUID.randomUUID().toString().substring(0,7),
                                "123" + UUID.randomUUID().toString().substring(0,7)
                        );

                        System.out.println("Automatic test executed");
                    } catch (Exception e) {
                        System.out.println("Register failed: " + e.getMessage());
                    }
                }
                else if (operationA == 3) {
                    try {
                        User sender = createUser(authService);
                        User recipient = createUser(authService);

                        String message = "Hello there!";
                                messageDao.saveMessage(sender.getId(), recipient.getId(), message);

                        System.out.println(
                                "Sent to : " + recipient.getUsername() + "\n" +
                                "From:     " + sender.getUsername() + "\n" +
                                "Message:  " + message
                        );

                    } catch (Exception e) {
                        System.out.println("Message sending field: " + e.getMessage());
                    }
                }
                break;

            default:
                System.out.println("Invalid choice.");
        }
        sc.close();
    }
}