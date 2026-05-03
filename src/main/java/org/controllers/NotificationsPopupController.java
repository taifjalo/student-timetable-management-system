package org.controllers;

import dto.NotificationDto;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.dao.NotificationDao;
import org.service.LocalizationService;
import org.service.NotificationService;
import org.service.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the notifications popup ({@code notifications-popup.fxml}).
 * Renders each notification as a styled row with a relative timestamp and a
 * "mark as read" action. Supports right-to-left layout for Arabic.
 *
 * <p>Notification text is pre-rendered for the recipient's locale by the DAO at
 * save time and is read directly from the DTO without any further translation step.
 */
public class NotificationsPopupController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationsPopupController.class);

    @FXML private VBox popupRoot;
    @FXML private VBox notificationList;
    @FXML private Label markAllLabel;

    private final NotificationService notificationService = new NotificationService(new NotificationDao());
    private final LocalizationService localizationService = new LocalizationService();
    private final List<NotificationRow> rows = new ArrayList<>();
    private Long userId;
    private ResourceBundle bundle;

    /**
     * JavaFX initialize callback — loads the current user's notifications and
     * builds the popup content. Sets RTL orientation for Arabic.
     */
    @FXML
    public void initialize() {
        bundle = localizationService.getBundle();
        if ("ar".equalsIgnoreCase(localizationService.getCurrentLocale().getLanguage())) {
            popupRoot.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        }
        userId = SessionManager.getInstance().getCurrentUser().getId();
        List<NotificationDto> notifications = notificationService.getNotificationDtosForUser(userId);
        LOGGER.debug("Fetched {} notifications for user {}", notifications.size(), userId);

        if (notifications.isEmpty()) {
            Label empty = new Label(bundle.getString("notifications.popup.empty"));
            empty.setStyle("-fx-text-fill: #999; -fx-font-size: 13; -fx-padding: 20;");
            notificationList.getChildren().add(empty);
            if (markAllLabel != null) {
                markAllLabel.setVisible(false);
            }
            return;
        }

        for (NotificationDto dto : notifications) {
            LOGGER.debug("Creating row for notification: {}", dto.getContent());
            NotificationRow row = new NotificationRow(
                    dto.getContent(),
                    formatTime(dto.getSentAt()),
                    !dto.isRead(),
                    () -> notificationService.markAsRead(userId, dto.getNotificationId()),
                    bundle.getString("notifications.popup.mark.read.button")
            );
            rows.add(row);
            notificationList.getChildren().add(row.getRoot());
        }
    }

    /**
     * FXML action handler for the "Mark all as read" label click.
     * Persists all-read state and updates every row's visual state.
     *
     * @param event the mouse event
     */
    @FXML
    private void handleMarkAllRead(MouseEvent event) {
        notificationService.markAllAsRead(userId);
        rows.forEach(NotificationRow::markRead);
    }

    /**
     * Formats a notification timestamp as a human-readable relative time string
     * using the active locale's bundle (e.g. "5 min ago", "2 h ago").
     *
     * @param sentAt the time the notification was sent
     * @return a localized relative time string
     */
    private String formatTime(LocalDateTime sentAt) {
        long minutes = ChronoUnit.MINUTES.between(sentAt, LocalDateTime.now());
        if (minutes < 1) {
            return bundle.getString("notifications.popup.time.just.now");
        }
        if (minutes < 60) {
            return minutes + " " + bundle.getString("notifications.popup.time.min.ago");
        }
        long hours = ChronoUnit.HOURS.between(sentAt, LocalDateTime.now());
        if (hours < 24) {
            return hours + " " + bundle.getString("notifications.popup.time.h.ago");
        }
        long days = ChronoUnit.DAYS.between(sentAt, LocalDateTime.now());
        return days + " " + bundle.getString("notifications.popup.time.d.ago");
    }

    /**
     * Programmatically constructed notification row widget.
     * Displays the message content, relative time, and a "mark as read" link
     * that hides itself once clicked.
     */
    private static class NotificationRow {

        private final VBox root;
        private final Region dot;
        private final HBox row;
        private final Label markReadBtn;
        private boolean unread;

        /**
         * Constructs a notification row.
         *
         * @param content      the localized notification text
         * @param time         the relative time string
         * @param unread       {@code true} if the notification has not been read
         * @param onMarkRead   action to run when the user marks this notification as read
         * @param markReadText localized label for the mark-as-read button
         */
        NotificationRow(String content, String time, boolean unread, Runnable onMarkRead, String markReadText) {
            this.unread = unread;

            dot = new Region();
            dot.setMinSize(8, 8);
            dot.setMaxSize(8, 8);

            Label contentLabel = new Label(content);
            contentLabel.setStyle("-fx-font-size: 13;");
            contentLabel.setWrapText(true);

            Label timeLabel = new Label(time);
            timeLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11;");

            markReadBtn = new Label(markReadText);
            markReadBtn.setStyle("-fx-text-fill: #00956D; -fx-font-size: 11; -fx-cursor: hand;");
            markReadBtn.setVisible(unread);
            markReadBtn.setOnMouseClicked(e -> {
                e.consume();
                if (this.unread) {
                    onMarkRead.run();
                    markRead();
                }
            });
            HBox markReadRow = new HBox(markReadBtn);
            markReadRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

            VBox textCol = new VBox(3, contentLabel, timeLabel, markReadRow);
            textCol.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(textCol, javafx.scene.layout.Priority.ALWAYS);

            row = new HBox(10, dot, textCol);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 16, 10, 16));
            row.setMaxWidth(Double.MAX_VALUE);

            applyStyle();

            row.setOnMouseEntered(e -> row.setStyle(
                    (this.unread ? "-fx-background-color: #D8F0E8;" : "-fx-background-color: #F5F5F5;")));
            row.setOnMouseExited(e -> applyStyle());

            javafx.scene.control.Separator sep = new javafx.scene.control.Separator();
            root = new VBox(row, sep);
        }

        /** Applies the background and dot color based on the current read state. */
        private void applyStyle() {
            row.setStyle((unread ? "-fx-background-color: #F0FAF6;" : "-fx-background-color: white;")
                    + " -fx-cursor: hand;");
            dot.setStyle(unread
                    ? "-fx-background-color: #00956D; -fx-background-radius: 50%;"
                    : "-fx-background-color: transparent;");
        }

        /** Marks this row as read, hiding the mark-read button and updating styling. */
        void markRead() {
            unread = false;
            markReadBtn.setVisible(false);
            markReadBtn.setManaged(false);
            applyStyle();
        }

        /** Returns the root VBox node to add to the notification list. */
        VBox getRoot() {
            return root;
        }


    }

}
