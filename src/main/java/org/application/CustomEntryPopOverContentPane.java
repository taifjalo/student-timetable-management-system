package org.application;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.TimeField;
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
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.controllers.EventExtraDetailsController;
import org.controlsfx.control.PopOver;
import org.dao.LessonDao;
import org.entities.Course;
import org.entities.Lesson;
import org.service.LessonService;

import java.time.LocalTime;
import java.util.List;
import java.util.function.UnaryOperator;

public class CustomEntryPopOverContentPane extends PopOverContentPane {

    public record SavedLesson(Long lessonId) {}

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

        lockTitleToCalendarName(header, entry);
        setHeader(header);

        EntryDetailsView details = new EntryDetailsView(entry, dateControl);
        replaceTimeFields(details, entry);

        Lesson existingLesson = null;
        if (entry.getUserObject() instanceof SavedLesson sl) {
            try {
                existingLesson = new LessonDao().findById(sl.lessonId());
            } catch (Exception ex) {
                System.err.println("Could not load existing lesson for pre-population: " + ex.getMessage());
            }
        }

        EventExtraDetailsController ctrl = new EventExtraDetailsController(entry, existingLesson);

        // Snapshot state at open time so we can revert if not saved
        final Interval[]  snapshotInterval = {entry.getInterval()};
        final Calendar[]  snapshotCalendar = {entry.getCalendar()};
        final boolean[] savedThisSession  = {false};
        final boolean[] savingInProgress  = {false};

        appendExtraFields(details, entry, ctrl, popOver, savedThisSession, savingInProgress, snapshotInterval, snapshotCalendar);

        PopOverTitledPane detailsPane = new PopOverTitledPane("Details", details);
        getPanes().add(detailsPane);
        setExpandedPane(detailsPane);

        // Close behaviour
        popOver.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (!isShowing && wasShowing) {
                // Ignore hide events that occur while save is in progress
                if (savingInProgress[0]) return;

                boolean isSaved = entry.getUserObject() instanceof SavedLesson;
                if (!isSaved) {
                    entry.removeFromCalendar();
                } else if (!savedThisSession[0]) {
                    // Closed without saving — revert interval and calendar
                    entry.setInterval(snapshotInterval[0]);
                    if (snapshotCalendar[0] != null && entry.getCalendar() != snapshotCalendar[0]) {
                        entry.setCalendar(snapshotCalendar[0]);
                    }
                }
            }
        });

        // Hide popover only when entry is permanently removed (not during calendar switch)
        entry.calendarProperty().addListener((obs, oldCal, newCal) -> {

            if (newCal == null && !savingInProgress[0]) {
                javafx.application.Platform.runLater(() -> {
                    if (entry.getCalendar() == null) {
                        popOver.hide(javafx.util.Duration.ZERO);
                    }
                });
            }
        });
    }

    /**
     * Makes the title field in the popover header non-editable and ties its
     * value to the name of the calendar (course) the entry belongs to.
     * Updates immediately if the user switches the entry to a different calendar.
     */
    private void lockTitleToCalendarName(EntryHeaderView header, Entry<?> entry) {
        // Set the entry title to the current calendar name right away
        if (entry.getCalendar() != null) {
            entry.setTitle(entry.getCalendar().getName());
        }

        // Reflect into the private titleField and make it non-editable
        try {
            java.lang.reflect.Field titleF =
                    com.calendarfx.view.popover.EntryHeaderView.class.getDeclaredField("titleField");
            titleF.setAccessible(true);
            TextField titleField = (TextField) titleF.get(header);
            titleField.setEditable(false);
            titleField.setMouseTransparent(true);
            titleField.setFocusTraversable(false);
            titleField.setStyle("-fx-cursor: default; -fx-background-color: transparent; " +
                                "-fx-border-color: transparent;");
        } catch (Exception e) {
            System.err.println("lockTitleToCalendarName reflection failed: " + e.getMessage());
        }

        entry.calendarProperty().addListener((obs, oldCal, newCal) -> {
            if (newCal != null) {
                entry.setTitle(newCal.getName());
            }
        });
    }

    private void replaceTimeFields(EntryDetailsView details, Entry<?> entry) {
        GridPane grid = details.getChildren().stream()
                .filter(GridPane.class::isInstance)
                .map(GridPane.class::cast)
                .findFirst()
                .orElse(null);
        if (grid == null) return;

        HBox startDateBox = getGridCell(grid, 1, 1);
        HBox endDateBox   = getGridCell(grid, 1, 2);

        if (startDateBox != null) {
            removeTimeField(startDateBox);
            startDateBox.getChildren().add(buildTimeInput(entry, true));
        }
        if (endDateBox != null) {
            removeTimeField(endDateBox);
            endDateBox.getChildren().add(buildTimeInput(entry, false));
        }
    }

    private HBox getGridCell(GridPane grid, int col, int row) {
        return grid.getChildren().stream()
                .filter(HBox.class::isInstance)
                .map(HBox.class::cast)
                .filter(n -> {
                    Integer c = GridPane.getColumnIndex(n);
                    Integer r = GridPane.getRowIndex(n);
                    return (c == null ? 0 : c) == col && (r == null ? 0 : r) == row;
                })
                .findFirst()
                .orElse(null);
    }

    private void removeTimeField(HBox box) {
        box.getChildren().removeIf(TimeField.class::isInstance);
    }


    private HBox buildTimeInput(Entry<?> entry, boolean isStart) {
        LocalTime initial = isStart ? entry.getStartTime() : entry.getEndTime();

        TextField hourField   = createNumericField(2, initial != null ? initial.getHour()   : 0);
        TextField minuteField = createNumericField(2, initial != null ? initial.getMinute() : 0);

        Label colon = new Label(":");
        colon.setStyle("-fx-font-size: 14px; -fx-padding: 0 2 0 2;");

        HBox box = new HBox(2, hourField, colon, minuteField);
        box.setAlignment(Pos.CENTER_LEFT);

        // Keep display in sync when entry interval changes externally (e.g. date picker)
        entry.intervalProperty().addListener((obs, oldI, newI) -> {
            LocalTime t = isStart ? entry.getStartTime() : entry.getEndTime();
            if (t != null) {
                hourField.setText(String.format("%02d", t.getHour()));
                minuteField.setText(String.format("%02d", t.getMinute()));
            }
        });

        Runnable commit = () -> {
            try {
                int h = clamp(Integer.parseInt(hourField.getText().trim()),   0, 23);
                int m = clamp(Integer.parseInt(minuteField.getText().trim()), 0, 59);
                // Normalise display after clamping
                hourField.setText(String.format("%02d", h));
                minuteField.setText(String.format("%02d", m));
                LocalTime newTime = LocalTime.of(h, m);
                if (isStart) {
                    entry.changeStartTime(newTime, true);
                } else {
                    entry.changeEndTime(newTime, false);
                }
            } catch (NumberFormatException ex) {
                // Restore to current entry value on bad input
                LocalTime current = isStart ? entry.getStartTime() : entry.getEndTime();
                if (current != null) {
                    hourField.setText(String.format("%02d", current.getHour()));
                    minuteField.setText(String.format("%02d", current.getMinute()));
                }
            }
        };

        for (TextField tf : List.of(hourField, minuteField)) {
            tf.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (wasFocused && !isFocused) commit.run();
            });
            tf.setOnKeyPressed(ke -> {
                if (ke.getCode() == KeyCode.ENTER) commit.run();
            });
        }

        return box;
    }

    private TextField createNumericField(int maxLen, int initialValue) {
        TextField tf = new TextField(String.format("%02d", initialValue));
        tf.setPrefWidth(36);
        tf.setMaxWidth(36);
        tf.setAlignment(Pos.CENTER);
        tf.setStyle("-fx-font-size: 13px;");

        // Only allow digit input, max maxLen characters
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d{0," + maxLen + "}")) {
                return change;
            }
            return null;
        };
        tf.setTextFormatter(new TextFormatter<>(filter));
        return tf;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private void appendExtraFields(EntryDetailsView details,
                                   Entry<?> entry,
                                   EventExtraDetailsController ctrl,
                                   PopOver popOver,
                                   boolean[] savedThisSession,
                                   boolean[] savingInProgress,
                                   Interval[] snapshotInterval,
                                   Calendar[] snapshotCalendar) {

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

        // Delete button
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #D03800; -fx-text-fill: white; " +
                           "-fx-cursor: hand; -fx-background-radius: 6;");
        deleteBtn.setOnAction(e -> {
            // If already saved → delete from DB as well
            if (entry.getUserObject() instanceof SavedLesson sl) {
                new Thread(() -> {
                    try {
                        new LessonService(new LessonDao()).deleteLesson(sl.lessonId());
                    } catch (Exception ex) {
                        System.err.println("Failed to delete lesson from DB: " + ex.getMessage());
                    }
                }, "delete-lesson-thread").start();
            }
            entry.removeFromCalendar(); // triggers calendarProperty listener → hides popover
        });

        // Save button
        Button saveBtn = new Button("Save");
        saveBtn.setStyle("-fx-background-color: #00956D; -fx-text-fill: white; " +
                         "-fx-cursor: hand; -fx-background-radius: 6;");
        saveBtn.setOnAction(e -> {
            Calendar cal = entry.getCalendar();
            if (cal == null) {
                System.err.println("Entry has no calendar — cannot save.");
                return;
            }

            Course course = null;
            if (cal.getUserObject() instanceof Course c) {
                course = c;
            } else {

                System.err.println("Calendar userObject is not a Course (was: "
                        + (cal.getUserObject() == null ? "null" : cal.getUserObject().getClass().getName())
                        + "), attempting name-based lookup for: " + cal.getName());
                try {
                    course = new org.dao.CourseDao().findAll().stream()
                            .filter(c2 -> c2.getName().equals(cal.getName()))
                            .findFirst()
                            .orElse(null);
                } catch (Exception ex) {
                    System.err.println("Name-based course lookup failed: " + ex.getMessage());
                }
            }

            if (course == null) {
                System.err.println("Could not resolve a Course for calendar '" + cal.getName() + "' — cannot save.");
                return;
            }

            final Course resolvedCourse = course;

            String classroom = ctrl.getClassroom();
            var groups = ctrl.getSelectedGroups();
            var users  = ctrl.getSelectedUsers();

            saveBtn.setDisable(true);
            saveBtn.setText("Saving…");
            savingInProgress[0] = true;

            new Thread(() -> {
                try {
                    LessonService svc = new LessonService(new LessonDao());
                    Lesson saved;

                    if (entry.getUserObject() instanceof SavedLesson existing) {
                        saved = svc.updateLesson(
                                existing.lessonId(),
                                entry.getStartAsLocalDateTime(),
                                entry.getEndAsLocalDateTime(),
                                resolvedCourse,
                                classroom,
                                groups,
                                users);
                    } else {
                        saved = svc.saveLesson(
                                entry.getStartAsLocalDateTime(),
                                entry.getEndAsLocalDateTime(),
                                resolvedCourse,
                                classroom,
                                groups,
                                users);
                    }

                    javafx.application.Platform.runLater(() -> {

                        ((Entry) entry).setUserObject(new SavedLesson(saved.getId()));
                        snapshotInterval[0] = entry.getInterval();
                        snapshotCalendar[0] = entry.getCalendar();
                        savedThisSession[0] = true;
                        savingInProgress[0] = false;
                        popOver.hide(javafx.util.Duration.ZERO);
                    });
                } catch (Exception ex) {
                    System.err.println("Failed to save lesson: " + ex.getMessage());
                    ex.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        savingInProgress[0] = false;
                        saveBtn.setDisable(false);
                        saveBtn.setText("Save");
                    });
                }
            }, "save-lesson-thread").start();
        });

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
