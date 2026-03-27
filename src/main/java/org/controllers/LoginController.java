package org.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.dao.UserDao;
import org.entities.User;
import org.service.AuthService;
import org.service.LocalizationService;
import org.service.SessionManager;
import javafx.scene.control.MenuButton;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

public class LoginController {

    private final LocalizationService localizationService = new LocalizationService();

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private MenuButton languageMenuButton;
    @FXML private MenuItem fiItem;
    @FXML private MenuItem enItem;
    @FXML private MenuItem arItem;
    @FXML private MenuItem ruItem;

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
    private void initialize() {
        applyLanguageMenuSelection();
    }

    @FXML
    private void handleChangeLocalization(ActionEvent event) throws IOException {

        MenuItem selectedItem = (MenuItem) event.getSource();

        if (event.getSource() == fiItem) {
            // Finnish
            System.out.println("Finnish localization selected");
            localizationService.switchLanguage("fi");
        } else if (event.getSource() == enItem) {
            // English
            System.out.println("English localization selected");
            localizationService.switchLanguage("en");
        }
        else if (event.getSource() == arItem) {
            // Arabic
            System.out.println("تم اختيار اللغة العربية");
            localizationService.switchLanguage("ar");
        }
        else if (event.getSource() == ruItem) {
            // English
            System.out.println("Russian localization selected");
            localizationService.switchLanguage("ru");
        }

        // Keep the selected value visible before and after scene reload.
        languageMenuButton.setText(selectedItem.getText());
        Stage stage = (Stage) languageMenuButton.getScene().getWindow();
        localizationService.reloadScene(stage, "/login.fxml");

    }

    private void applyLanguageMenuSelection() {
        if (languageMenuButton == null) {
            return;
        }

        Locale currentLocale = localizationService.getCurrentLocale();
        String lang = currentLocale.getLanguage();
        languageMenuButton.setText(switch (lang) {
            case "fi" -> fiItem.getText();
            case "ar" -> arItem.getText();
            case "ru" -> ruItem.getText();
            default   -> enItem.getText();
        });
    }

    @FXML
    private void handleGoToRegister(ActionEvent event) {
        try {
            URL registerFxml = getClass().getResource("/register.fxml");
            if (registerFxml == null) {
                throw new IllegalArgumentException("register.fxml not found");
            }

            FXMLLoader loader = new FXMLLoader(registerFxml, localizationService.getBundle());
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
