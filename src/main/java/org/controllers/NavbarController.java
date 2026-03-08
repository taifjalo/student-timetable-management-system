package org.controllers;

import com.calendarfx.view.CalendarView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.CustomTextField;
import org.dao.NotificationDao;
import org.service.NotificationService;
import org.service.SessionManager;

public class NavbarController {

    private org.controlsfx.control.PopOver notificationsPopOver;
    private final NotificationService notificationService = new NotificationService(new NotificationDao());

    @FXML private ToggleButton calViewDay;
    @FXML private ToggleButton calViewWeek;
    @FXML private ToggleButton calViewMonth;
    @FXML private ToggleButton calViewYear;
    @FXML private CustomTextField fieldSearch;
    @FXML private Label badgeLabel;

    @FXML
    public void initialize() {
        refreshBadge();
    }

    private void refreshBadge() {
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
    private void handleAlertsClick(ActionEvent event) {
        if (notificationsPopOver != null && notificationsPopOver.isShowing()) {
            notificationsPopOver.hide();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/notifications-popup/notifications-popup.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user-settings-modal.fxml"));
            Parent root = loader.load();
            UserSettingsController controller = loader.getController();

            Stage modalStage = new Stage();
            modalStage.setTitle("Käyttäjäasetukset");
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
            Parent root = FXMLLoader.load(getClass().getResource("/register.fxml"));
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
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
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
