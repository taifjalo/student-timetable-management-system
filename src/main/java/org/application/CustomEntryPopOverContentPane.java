package org.application;

import com.calendarfx.model.Entry;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.popover.EntryDetailsView;
import com.calendarfx.view.popover.EntryHeaderView;
import com.calendarfx.view.popover.PopOverContentPane;
import com.calendarfx.view.popover.PopOverTitledPane;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.controllers.EventExtraDetailsController;
import org.controlsfx.control.PopOver;

import java.util.List;

public class CustomEntryPopOverContentPane extends PopOverContentPane {

    public CustomEntryPopOverContentPane(PopOver popOver,
                                         DateControl dateControl,
                                         Entry<?> entry) {

        EntryHeaderView header = new EntryHeaderView(entry, dateControl.getCalendars());

        List<Node> locationNodes = header.getChildren().stream()
                .filter(n -> {
                    Integer row = GridPane.getRowIndex(n);
                    return row != null && row == 1;
                })
                .toList();
        header.getChildren().removeAll(locationNodes);
        if (header.getRowConstraints().size() > 1) {
            header.getRowConstraints().remove(1);
        }

        setHeader(header);

        EntryDetailsView details = new EntryDetailsView(entry, dateControl);
        appendExtraFields(details, entry);

        PopOverTitledPane detailsPane = new PopOverTitledPane("Details", details);
        getPanes().add(detailsPane);
        setExpandedPane(detailsPane);

        entry.calendarProperty().addListener((obs, oldCal, newCal) -> {
            if (newCal == null) {
                popOver.hide(javafx.util.Duration.ZERO);
            }
        });
    }


    private void appendExtraFields(EntryDetailsView details, Entry<?> entry) {
        GridPane grid = details.getChildren().stream()
                .filter(GridPane.class::isInstance)
                .map(GridPane.class::cast)
                .findFirst()
                .orElse(null);

        if (grid == null) return;

        if (grid.getColumnConstraints().size() >= 2) {
            ColumnConstraints col2 = grid.getColumnConstraints().get(1);
            col2.setHgrow(Priority.ALWAYS);
            col2.setFillWidth(true);
        }

        EventExtraDetailsController ctrl = new EventExtraDetailsController(entry);

        final int EXTRA_ROWS = 4;

        grid.getChildren().forEach(n -> {
            Integer r = GridPane.getRowIndex(n);
            GridPane.setRowIndex(n, (r == null ? 0 : r) + EXTRA_ROWS);
        });

        buildRow(grid, 0, "Luokka:",      ctrl.getClassIdNode());
        buildRow(grid, 1, "Opettaja:",    ctrl.getTeacherNode());
        buildRow(grid, 2, "Ryhmä:",       ctrl.getGroupRowNode());
        buildRow(grid, 3, "Opiskelijat:", ctrl.getStudentsNode());

        int lastRow = grid.getChildren().stream()
                .mapToInt(n -> { Integer r = GridPane.getRowIndex(n); return r == null ? 0 : r; })
                .max().orElse(EXTRA_ROWS);

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #D03800; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 6;");
        deleteBtn.setOnAction(e -> entry.removeFromCalendar());

        Button saveBtn = new Button("Save");
        saveBtn.setStyle("-fx-background-color: #00956D; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 6;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionBar = new HBox(deleteBtn, spacer, saveBtn);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.setMaxWidth(Double.MAX_VALUE);
        GridPane.setMargin(actionBar, new Insets(10, 0, 4, 0));
        GridPane.setColumnSpan(actionBar, 2);
        grid.add(actionBar, 0, lastRow + 1);

    }

    private void buildRow(GridPane grid, int row, String labelText, Node field) {
        Label label = new Label(labelText);
        GridPane.setHalignment(label, HPos.RIGHT);
        GridPane.setMargin(label, new Insets(4, 8, 0, 0));
        GridPane.setMargin(field, new Insets(4, 0, 0, 0));
        GridPane.setHgrow(field, Priority.ALWAYS);
        GridPane.setFillWidth(field, true);
        grid.add(label, 0, row);
        grid.add(field, 1, row);
    }
}
