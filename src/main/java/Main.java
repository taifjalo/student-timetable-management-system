import org.dao.UserDao;
import org.entities.User;
import org.service.AuthService;
import java.util.UUID;


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
                "Username" +UUID.randomUUID(),
                "user" + UUID.randomUUID() + "@test.com",
                "090" + UUID.randomUUID().toString().substring(0,7), // Make always Random phone number to pass DB
                "123" + UUID.randomUUID().toString().substring(0,7)         // Make always Password number to pass DB
        );
        System.out.println("User registered successfully");
    }
}
