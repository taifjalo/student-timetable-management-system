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
import java.util.ResourceBundle;


/**
 * Controller for the login screen ({@code login.fxml}).
 * Handles user authentication and language selection.
 */
public class LoginController {

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

    private final LocalizationService localizationService = new LocalizationService();
    ResourceBundle selectedBundle = localizationService.getBundle();

    /**
     * Handles the "Login" button click.
     * Validates input, authenticates the user, stores the session, and
     * navigates to the main app screen.
     *
     * @param event the action event from the button click
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            messageLabel.setText(selectedBundle.getString("login.error"));
            return;
        }

        try {
            User user = authService.login(username, password);
            SessionManager.getInstance().login(user);

            // Switch to the main calendar app scene
            ResourceBundle bundle = localizationService.getBundle();
            Parent root = FXMLLoader.load(getClass().getResource("/ui/main-app.fxml"), bundle);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            localizationService.swapSides(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            messageLabel.setText(selectedBundle.getString("login.invalid.credentials.error") + e.getMessage());
        }
    }

    /**
     * JavaFX initialize callback — syncs the language menu button label to the current locale.
     */
    @FXML
    private void initialize() {
        applyLanguageMenuSelection();
    }

    /**
     * Handles language selection from the menu button.
     * Switches the active locale and reloads the login screen in the new language.
     *
     * @param event the action event from the menu item click
     * @throws IOException if the FXML reload fails
     */
    @FXML
    private void handleChangeLocalization(ActionEvent event) throws IOException {
        MenuItem selectedItem = (MenuItem) event.getSource();

        if (event.getSource() == fiItem) {
            localizationService.switchLanguage("fi");
        } else if (event.getSource() == enItem) {
            localizationService.switchLanguage("en");
        } else if (event.getSource() == arItem) {
            localizationService.switchLanguage("ar");
        } else if (event.getSource() == ruItem) {
            localizationService.switchLanguage("ru");
        }

        languageMenuButton.setText(selectedItem.getText());

        // Reload the Scene when the Lang changes
        Stage stage = (Stage) languageMenuButton.getScene().getWindow();
        ResourceBundle bundle = localizationService.getBundle();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/auth/login.fxml"), bundle);
        Parent root = loader.load();

        // To RTL or LTL
        localizationService.swapSides(root);

        Scene scene = stage.getScene();
        if (scene == null) {
            stage.setScene(new Scene(root));
        } else {
            scene.setRoot(root);
        }
    }

    /**
     * Syncs the language {@link MenuButton} label to whichever locale is currently active.
     * Does nothing if the button is not yet initialized.
     */
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

    /**
     * Handles the "Go to register" link click, navigating to the registration screen.
     *
     * @param event the action event from the link click
     */
    @FXML
    private void handleGoToRegister(ActionEvent event) {
        try {
            URL registerFxml = getClass().getResource("/ui/auth/register.fxml");
            if (registerFxml == null) {
                throw new IllegalArgumentException("register.fxml not found");
            }

            ResourceBundle bundle = localizationService.getBundle();
            FXMLLoader loader = new FXMLLoader(registerFxml, localizationService.getBundle());
            Parent root = loader.load();
            localizationService.swapSides(root);

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
