package org.service;

import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Service that manages the application's active locale and provides helpers for
 * loading localized FXML scenes and adjusting text direction.
 *
 * <p>The current locale is stored as a static field so it is shared across all
 * controller instances within the same JVM session. Supported locales are
 * {@code en_US}, {@code fi_FI}, {@code ar_IQ}, and {@code ru_RU}.
 */
public class LocalizationService {

    private static final String BUNDLE_BASE_NAME = "i18n/MessagesBundle";
    private static Locale currentLocale = new Locale("en", "US");

    /**
     * Returns the currently active locale.
     *
     * @return the application's current {@link Locale}
     */
    public Locale getCurrentLocale() {
        return currentLocale;
    }

    /**
     * Returns the {@link ResourceBundle} for the current locale.
     *
     * @return the loaded bundle from {@code i18n/MessagesBundle_<locale>.properties}
     */
    public ResourceBundle getBundle() {
        return ResourceBundle.getBundle(BUNDLE_BASE_NAME, currentLocale);
    }

    /**
     * Changes the active locale application-wide and updates {@link Locale#setDefault}.
     *
     * @param locale the new locale to apply; must not be {@code null}
     * @throws IllegalArgumentException if {@code locale} is {@code null}
     */
    public void setLocale(Locale locale) {
        if (locale == null) {
            throw new IllegalArgumentException("Locale cannot be null");
        }
        currentLocale = locale;
        Locale.setDefault(currentLocale);
    }

    /**
     * Convenience method to switch language by a short code.
     * Supported codes: {@code "fi"}, {@code "en"}, {@code "ar"}, {@code "ru"}.
     *
     * @param languageCode the ISO 639-1 language code (case-insensitive)
     * @throws IllegalArgumentException if the code is blank or unsupported
     */
    public void switchLanguage(String languageCode) {
        if (languageCode == null || languageCode.isBlank()) {
            throw new IllegalArgumentException("Language code cannot be null or blank");
        }

        switch (languageCode.toLowerCase()) {
            case "fi" -> setLocale(new Locale("fi", "FI"));
            case "en" -> setLocale(new Locale("en", "US"));
            case "ar" -> setLocale(new Locale("ar", "IQ"));
            case "ru" -> setLocale(new Locale("ru", "RU"));
            // reachable until these cases are added and language menu items are added to login.fxml.

            default -> throw new IllegalArgumentException("Unsupported language code: " + languageCode);
        }
    }

    /**
     * Reloads the given FXML into the stage using the current locale's bundle.
     * If the stage already has a scene, its root is replaced in-place; otherwise
     * a new scene is created.
     *
     * @param stage    the stage whose scene should be replaced
     * @param fxmlPath classpath-relative FXML path (e.g. {@code "/login.fxml"})
     * @throws IOException              if the FXML file cannot be loaded
     * @throws IllegalArgumentException if the FXML file is not found on the classpath
     */
    public void reloadScene(Stage stage, String fxmlPath) throws IOException {
        URL resource = getClass().getResource(fxmlPath);
        if (resource == null) {
            throw new IllegalArgumentException("FXML file not found: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(resource, getBundle());
        Parent root = loader.load();
        swapSides(root);

        Scene currentScene = stage.getScene();
        if (currentScene == null) {
            stage.setScene(new Scene(root));
        } else {
            currentScene.setRoot(root);
        }
    }

    /**
     * Sets the node orientation of the given root to RTL for Arabic or LTR for all
     * other locales. Should be called after loading any FXML root.
     *
     * @param root the root {@link Parent} whose text direction should be adjusted
     */
    public void swapSides(Parent root){
        if (currentLocale.getLanguage().equals("ar")) {
            root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        } else {
            root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        }
    }
}
