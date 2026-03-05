package org.application;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class NotificationsPopupController {

    @FXML private VBox notificationList;

    private record Notification(String title, String body, String time, boolean unread) {}

    private final List<NotificationRow> rows = new ArrayList<>();

    @FXML
    public void initialize() {
        List<Notification> testData = List.of(
                new Notification("Uusi tehtävä lisätty",
                        "Ohjelmistotekniikka: Viikko 9 harjoitustehtävä on julkaistu.",
                        "2 min sitten", true),
                new Notification("Tunnin peruutus",
                        "Matematiikka: Perjantain 7.3. tunti on peruttu.",
                        "1 h sitten", true),
                new Notification("Muistutus: Palautus huomenna",
                        "Kuvaus- ja mallintamismenetelmät: Palautus sulkeutuu 6.3. klo 23:59.",
                        "3 h sitten", true),
                new Notification("Ryhmätyö päivitetty",
                        "Opiskelija Maija Virtanen liittyi ryhmääsi TVT24-O.",
                        "Eilen", false),
                new Notification("Aikataulu muuttunut",
                        "Ohjelmistotekniikka-kurssin luentoaika siirretty pe → to klo 10–12.",
                        "2 pv sitten", false)
        );

        for (Notification n : testData) {
            NotificationRow row = new NotificationRow(n.title(), n.body(), n.time(), n.unread());
            rows.add(row);
            notificationList.getChildren().add(row.getRoot());
        }
    }

//    @FXML
//    private void handleMarkAllRead(MouseEvent event) {
//        rows.forEach(NotificationRow::markRead);
//    }

    private static class NotificationRow {

        private final VBox root;
        private final Region unreadDot;

        NotificationRow(String title, String body, String time, boolean unread) {
            unreadDot = new Region();
            unreadDot.setMinSize(8, 8);
            unreadDot.setMaxSize(8, 8);
//            unreadDot.setStyle(
//                    "-fx-background-color: " + (unread ? "#00956D" : "transparent") + ";" +
//                    "-fx-background-radius: 50%;");

            // ...existing code...

            Label titleLabel = new Label(title);
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
            titleLabel.setWrapText(true);

            Label bodyLabel = new Label(body);
            bodyLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 12;");
            bodyLabel.setWrapText(true);

            Label timeLabel = new Label(time);
            timeLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11;");

            VBox textCol = new VBox(3, titleLabel, bodyLabel, timeLabel);
            textCol.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(textCol, javafx.scene.layout.Priority.ALWAYS);

//            HBox row = new HBox(10, unreadDot, textCol);
            HBox row = new HBox(10, textCol);
            row.setAlignment(javafx.geometry.Pos.TOP_LEFT);
            row.setPadding(new Insets(10, 16, 10, 16));
            row.setMaxWidth(Double.MAX_VALUE);
//            row.setStyle(unread
//                    ? "-fx-background-color: #F0FAF6;"
//                    : "-fx-background-color: white;");

            javafx.scene.control.Separator sep = new javafx.scene.control.Separator();

            root = new VBox(row, sep);

//            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #E8F5F0; -fx-cursor: hand;"));
//            row.setOnMouseExited(e -> row.setStyle(
//                    isUnread() ? "-fx-background-color: #F0FAF6;" : "-fx-background-color: white;"));
        }

        VBox getRoot() { return root; }

//        private boolean isUnread() {
//            return !unreadDot.getStyle().contains("transparent");
//        }
//
//        void markRead() {
//            unreadDot.setStyle("-fx-background-color: transparent; -fx-background-radius: 50%;");
//
//            if (!root.getChildren().isEmpty() && root.getChildren().get(0) instanceof HBox row) {
//                row.setStyle("-fx-background-color: white;");
//            }
//        }
    }
}

