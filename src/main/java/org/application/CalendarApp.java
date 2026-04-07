package org.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * JavaFX application entry point for Easytable.
 * Loads the login screen with the system default locale bundle and displays it maximized.
 */
public class CalendarApp extends Application {

    /**
     * JavaFX lifecycle callback — builds the initial scene from {@code login.fxml}
     * using the system default locale bundle, attaches the global stylesheet, and
     * shows the primary stage maximized.
     *
     * @param primaryStage the primary stage provided by the JavaFX runtime
     * @throws Exception if the FXML or resource loading fails
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Locale systemLocale = Locale.getDefault();
        Locale startupLocale = switch (systemLocale.getLanguage()) {
            case "fi" -> Locale.of("fi", "FI");
            case "ar" -> Locale.of("ar", "IQ");
            case "ru" -> Locale.of("ru", "RU");
            default -> Locale.of("en", "US");
        };

        ResourceBundle bundle = ResourceBundle.getBundle("i18n/MessagesBundle", startupLocale);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/auth/login.fxml"), bundle);

//        Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setTitle("Easytable");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    /**
     * Application main method — delegates to {@link Application#launch(String...)}.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
