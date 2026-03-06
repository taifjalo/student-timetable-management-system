package org.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.dao.UserDao;
import org.entities.User;
import org.service.AuthService;
import org.service.SessionManager;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    UserDao userDao = new UserDao();
    AuthService authService = new AuthService(userDao);

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            messageLabel.setText("Syötä käyttäjänimi ja salasana.");
            return;
        }

        try {
            User user = authService.login(username, password);
            SessionManager.getInstance().login(user);
            System.out.println("Logged in as: " + user.getUsername());

            // Switch to the main calendar app scene
            Parent root = FXMLLoader.load(getClass().getResource("/main-app.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            messageLabel.setText("Kirjautuminen epäonnistui: " + e.getMessage());
        }
    }

    @FXML
    private void handleGoToRegister(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/register.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
