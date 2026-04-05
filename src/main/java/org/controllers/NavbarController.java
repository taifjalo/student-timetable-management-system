package org.controllers;

import com.calendarfx.view.CalendarView;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.controlsfx.control.textfield.CustomTextField;
import org.dao.NotificationDao;
import org.entities.User;
import org.service.LocalizationService;
import org.service.NotificationService;
import org.service.SessionManager;

import java.util.ResourceBundle;

public class NavbarController {

    private org.controlsfx.control.PopOver notificationsPopOver;
    private final NotificationService notificationService = new NotificationService(new NotificationDao());
    private MainAppController mainAppController;
    LocalizationService localizationService = new LocalizationService();

    @FXML private ToggleButton calViewDay;
    @FXML private ToggleButton calViewWeek;
    @FXML private ToggleButton calViewMonth;
    @FXML private ToggleButton calViewYear;
    @FXML private CustomTextField fieldSearch;
    @FXML private Label badgeLabel;
    @FXML private ImageView refreshIcon;

    private Timeline spinTimeline;

    @FXML
    public void initialize() {
        refreshBadge();
    }

    public void setMainAppController(MainAppController mainAppController) {
        this.mainAppController = mainAppController;
    }

    private void startSpin() {
        if (refreshIcon == null) return;
        refreshIcon.setRotate(0);
        spinTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,       new KeyValue(refreshIcon.rotateProperty(), 0)),
                new KeyFrame(Duration.seconds(1), new KeyValue(refreshIcon.rotateProperty(), 360))
        );
        spinTimeline.setCycleCount(Timeline.INDEFINITE);
        spinTimeline.play();
    }

    public void stopSpin() {
        if (spinTimeline != null) {
            spinTimeline.stop();
            spinTimeline = null;
        }
        if (refreshIcon != null) {
            refreshIcon.setRotate(0);
        }
    }

    public void refreshBadge() {
        if (SessionManager.getInstance().getCurrentUser() == null) return;
        long count = notificationService.getUnreadCount(
                SessionManager.getInstance().getCurrentUser().getId());
        if (count > 0) {
            badgeLabel.setText(count > 99 ? "99+" : String.valueOf(count));
            badgeLabel.setVisible(true);
        } else {
            badgeLabel.setVisible(false);
        }
    }

    public void setCalendarView(CalendarView calendarView) {
        fieldSearch.textProperty().addListener((obs, oldVal, newVal) ->
                calendarView.getSearchField().setText(newVal));

        calViewDay.setOnAction(e -> { calendarView.showDayPage(); calViewDay.setSelected(true); });
        calViewWeek.setOnAction(e -> { calendarView.showWeekPage(); calViewWeek.setSelected(true); });
        calViewMonth.setOnAction(e -> { calendarView.showMonthPage(); calViewMonth.setSelected(true); });
        calViewYear.setOnAction(e -> { calendarView.showYearPage(); calViewYear.setSelected(true); });
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        if (mainAppController != null) {
            startSpin();
            mainAppController.refresh();
        }
    }

    @FXML
    private void handleAlertsClick(ActionEvent event) {
        if (notificationsPopOver != null && notificationsPopOver.isShowing()) {
            notificationsPopOver.hide();
            return;
        }
        try {
            // Bundle must be passed here — notifications-popup.fxml uses %key syntax for localized
            // strings. If loaded without a bundle, JavaFX throws MissingResourceException at runtime.
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/notifications-popup/notifications-popup.fxml"),
                    new LocalizationService().getBundle());
            Node content = loader.load();

            notificationsPopOver = new org.controlsfx.control.PopOver(content);
            notificationsPopOver.setArrowLocation(org.controlsfx.control.PopOver.ArrowLocation.TOP_RIGHT);
            notificationsPopOver.setDetachable(false);
            notificationsPopOver.setAnimated(false);
            notificationsPopOver.setHeaderAlwaysVisible(false);
            notificationsPopOver.setArrowSize(10);
            notificationsPopOver.showingProperty().addListener((obs, wasShowing, isShowing) -> {
                if (!isShowing) refreshBadge();
            });
            notificationsPopOver.show((Node) event.getSource());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProfileClick(ActionEvent event) {
        try {
            ResourceBundle bundle = new LocalizationService().getBundle();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user-settings-modal.fxml"), bundle);
            Parent root = loader.load();
            localizationService.swapSides(root);

            UserSettingsController controller = loader.getController();

            Stage modalStage = new Stage();
            modalStage.setTitle(bundle.getString("settings.modal.stage.title"));
            modalStage.setScene(new Scene(root));
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            modalStage.setResizable(false);

            controller.setStage(modalStage);
            modalStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGoToRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/register.fxml"), localizationService.getBundle());
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

    @FXML
    private void handleGoToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"), localizationService.getBundle());
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

    @FXML
    private void openChat(){
        try {
            SessionManager sessionManager = SessionManager.getInstance();
            User currentUser = sessionManager.getCurrentUser();

            ResourceBundle bundle = localizationService.getBundle();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/chat-view/messages.fxml"), bundle);
            Parent root = loader.load();
            localizationService.swapSides(root);

            ChatController ChatController = loader.getController();
            ChatController.setUserId(currentUser.getId());
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            scene.getStylesheets().add(
                    getClass().getResource("/chat-view/message.css").toExternalForm()
            );
            scene.setFill(null);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
