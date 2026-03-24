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
import org.entities.StudentGroup;
import org.entities.User;
import org.service.LessonService;
import org.service.LocalizationService;
import org.service.SessionManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class CustomEntryPopOverContentPane extends PopOverContentPane {

    public record SavedLesson(Long lessonId) {}

    private final LocalizationService localizationService = new LocalizationService();
    ResourceBundle selectedBundle = localizationService.getBundle();

    public CustomEntryPopOverContentPane(PopOver popOver,
                                         DateControl dateControl,
                                         Entry<?> entry) {

        final boolean teacher = SessionManager.getInstance().isTeacher();

        if (teacher) {
            // Full CalendarFX header for teachers
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
            lockTitleToCalendarName(header, entry, true);
            setHeader(header);
        } else {
            // Simple, read-only header for students: just show the course / entry title
            javafx.scene.layout.HBox studentHeader = new javafx.scene.layout.HBox();
            studentHeader.setAlignment(Pos.CENTER_LEFT);
            studentHeader.setSpacing(8);
            studentHeader.setPadding(new Insets(8, 12, 8, 12));

            Label titleLabel = new Label();
            titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            String initialTitle = entry.getCalendar() != null
                    ? entry.getCalendar().getName()
                    : entry.getTitle();
            titleLabel.setText(initialTitle != null ? initialTitle : "");

            // Keep the label in sync if calendar or title changes
            entry.calendarProperty().addListener((obs, oldCal, newCal) -> {
                if (newCal != null) {
                    titleLabel.setText(newCal.getName());
                }
            });
            entry.titleProperty().addListener((obs, oldT, newT) -> {
                if (entry.getCalendar() == null && newT != null) {
                    titleLabel.setText(newT);
                }
            });

            studentHeader.getChildren().add(titleLabel);
            setHeader(studentHeader);
        }

        Lesson existingLesson = null;
        if (entry.getUserObject() instanceof SavedLesson sl) {
            try {
                existingLesson = new LessonDao().findById(sl.lessonId());
            } catch (Exception ex) {
                System.err.println("Could not load existing lesson: " + ex.getMessage());
            }
        }

        final Interval[] snapshotInterval = {entry.getInterval()};
        final Calendar[] snapshotCalendar = {entry.getCalendar()};
        final boolean[] savedThisSession  = {false};
        final boolean[] savingInProgress  = {false};

        if (teacher) {
            EntryDetailsView details = new EntryDetailsView(entry, dateControl);
            replaceTimeFields(details, entry);

            EventExtraDetailsController ctrl =
                    new EventExtraDetailsController(entry, existingLesson);
            appendExtraFields(details, entry, ctrl, popOver,
                    savedThisSession, savingInProgress, snapshotInterval, snapshotCalendar);

            PopOverTitledPane detailsPane = new PopOverTitledPane(selectedBundle.getString("event.details"), details);
            getPanes().add(detailsPane);
            setExpandedPane(detailsPane);

        } else {
            GridPane studentView = buildStudentView(entry, existingLesson);
            PopOverTitledPane detailsPane = new PopOverTitledPane(selectedBundle.getString("event.details"), studentView);
            getPanes().add(detailsPane);
            setExpandedPane(detailsPane);
        }

        popOver.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (!isShowing && wasShowing) {
                if (savingInProgress[0]) return;
                boolean isSaved = entry.getUserObject() instanceof SavedLesson;
                if (!isSaved) {
                    entry.removeFromCalendar();
                } else if (!savedThisSession[0]) {
                    entry.setInterval(snapshotInterval[0]);
                    if (snapshotCalendar[0] != null && entry.getCalendar() != snapshotCalendar[0]) {
                        entry.setCalendar(snapshotCalendar[0]);
                    }
                }
            }
        });

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


    private GridPane buildStudentView(Entry<?> entry, Lesson existingLesson) {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
//        grid.setPadding(new Insets(8, 12, 8, 12));

        ColumnConstraints col0 = new ColumnConstraints();
//        col0.setMinWidth(90);
//        col0.setHgrow(Priority.NEVER);
        ColumnConstraints col1 = new ColumnConstraints();
//        col1.setHgrow(Priority.ALWAYS);
//        col1.setFillWidth(true);
        grid.getColumnConstraints().addAll(col0, col1);

        // Start
        Label startVal = valueLabel(formatDateTime(entry.getStartDate(), entry.getStartTime()));
        entry.intervalProperty().addListener((obs, o, n) ->
                startVal.setText(formatDateTime(entry.getStartDate(), entry.getStartTime())));
        addRow(grid, 4, "Start:", startVal);

        // End
        Label endVal = valueLabel(formatDateTime(entry.getEndDate(), entry.getEndTime()));
        entry.intervalProperty().addListener((obs, o, n) ->
                endVal.setText(formatDateTime(entry.getEndDate(), entry.getEndTime())));
        addRow(grid, 5, "End:", endVal);

        // Full day
//        addRow(grid, 2, "Full day:", valueLabel(entry.isFullDay() ? "Yes" : "No"));

        // Repeat
//        String rule = entry.getRecurrenceRule();
//        addRow(grid, 3, "Repeat:", valueLabel(rule == null || rule.isBlank() ? "None" : rule));

        // Lesson-specific fields
        String classroom = "—";
        String teacherName = "—";
        String groupsText = "—";
        String studentsText = "—";

        if (existingLesson != null) {
            if (existingLesson.getClassroom() != null && !existingLesson.getClassroom().isBlank())
                classroom = existingLesson.getClassroom();

            org.entities.User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser != null)
                teacherName = currentUser.getFirstName() + " " + currentUser.getSureName();

            List<StudentGroup> groups = existingLesson.getAssignedGroups();
            if (groups != null && !groups.isEmpty())
                groupsText = groups.stream()
                        .map(StudentGroup::getFieldOfStudies)
                        .collect(Collectors.joining(", "));

            List<User> users = existingLesson.getAssignedUsers();
            if (users != null && !users.isEmpty())
                studentsText = users.stream()
                        .map(u -> u.getFirstName() + " " + u.getSureName())
                        .collect(Collectors.joining(", "));
        }

        addRow(grid, 0, selectedBundle.getString("event.class"),      valueLabel(classroom));
        addRow(grid, 1, selectedBundle.getString("event.teacher"),    valueLabel(teacherName));
        addRow(grid, 3, selectedBundle.getString("event.group"),       valueLabel(groupsText));
//        addRow(grid, 7, "Opiskelijat:", valueLabel(studentsText));

        return grid;
    }

    private void addRow(GridPane grid, int row, String labelText, Label value) {
        Label key = new Label(labelText);
        key.setStyle("-fx-text-fill:#666666; -fx-font-size:12px;");
        GridPane.setHalignment(key, HPos.RIGHT);
//        GridPane.setMargin(key, new Insets(2, 8, 2, 0));
//        GridPane.setMargin(value, new Insets(2, 0, 2, 0));
        GridPane.setHgrow(value, Priority.ALWAYS);
        GridPane.setFillWidth(value, true);
        grid.add(key,   0, row);
        grid.add(value, 1, row);
    }

    private Label valueLabel(String text) {
        Label lbl = new Label(text != null ? text : "—");
        lbl.setStyle("-fx-font-size:13px;");
        lbl.setWrapText(true);
        return lbl;
    }

    private void lockTitleToCalendarName(EntryHeaderView header, Entry<?> entry, boolean teacher) {
        if (entry.getCalendar() != null) {
            entry.setTitle(entry.getCalendar().getName());
        }
        try {
            java.lang.reflect.Field titleF =
                    EntryHeaderView.class.getDeclaredField("titleField");
            titleF.setAccessible(true);
            TextField titleField = (TextField) titleF.get(header);
            titleField.setEditable(false);
            titleField.setMouseTransparent(true);
            titleField.setFocusTraversable(false);
            titleField.setStyle("-fx-cursor: default; -fx-background-color: transparent;" +
                                " -fx-border-color: transparent;");
        } catch (Exception e) {
            System.err.println("lockTitleToCalendarName reflection failed: " + e.getMessage());
        }

        if (!teacher) {
            header.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    javafx.application.Platform.runLater(() ->
                        header.lookupAll(".combo-box").forEach(node -> node.setVisible(false))
                    );
                }
            });
        }

        entry.calendarProperty().addListener((obs, oldCal, newCal) -> {
            if (newCal != null) entry.setTitle(newCal.getName());
        });
    }


    private void replaceTimeFields(EntryDetailsView details, Entry<?> entry) {
        GridPane grid = details.getChildren().stream()
                .filter(GridPane.class::isInstance)
                .map(GridPane.class::cast)
                .findFirst().orElse(null);
        if (grid == null) return;

        HBox startBox = getGridCell(grid, 1, 1);
        HBox endBox   = getGridCell(grid, 1, 2);
        if (startBox != null) { removeTimeField(startBox); startBox.getChildren().add(buildTimeInput(entry, true)); }
        if (endBox   != null) { removeTimeField(endBox);   endBox.getChildren().add(buildTimeInput(entry, false)); }
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
                .findFirst().orElse(null);
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

        buildRow(grid, 0, selectedBundle.getString("event.class"),      ctrl.getClassIdNode());
        buildRow(grid, 1, selectedBundle.getString("event.teacher"),    ctrl.getTeacherNode());
        buildRow(grid, 2, selectedBundle.getString("event.group"),       ctrl.getGroupRowNode());
        buildRow(grid, 3, selectedBundle.getString("event.students"), ctrl.getStudentsNode());

        int lastRow = grid.getChildren().stream()
                .mapToInt(n -> { Integer r = GridPane.getRowIndex(n); return r == null ? 0 : r; })
                .max().orElse(EXTRA_ROWS);

        Button deleteBtn = new Button(selectedBundle.getString("event.delete.button"));
        deleteBtn.setStyle("-fx-background-color: #D03800; -fx-text-fill: white;" +
                           " -fx-cursor: hand; -fx-background-radius: 6;");
        deleteBtn.setOnAction(e -> {
            if (entry.getUserObject() instanceof SavedLesson sl) {
                new Thread(() -> {
                    try { new LessonService(new LessonDao()).deleteLesson(sl.lessonId()); }
                    catch (Exception ex) { System.err.println("Delete failed: " + ex.getMessage()); }
                }, "delete-lesson-thread").start();
            }
            entry.removeFromCalendar();
        });

        Button saveBtn = new Button(selectedBundle.getString("event.save.button"));
        saveBtn.setStyle("-fx-background-color: #00956D; -fx-text-fill: white;" +
                         " -fx-cursor: hand; -fx-background-radius: 6;");
        saveBtn.setOnAction(e -> {
            Calendar cal = entry.getCalendar();
            if (cal == null) { System.err.println("Entry has no calendar — cannot save."); return; }

            Course course = null;
            if (cal.getUserObject() instanceof Course c) {
                course = c;
            } else {
                try {
                    course = new org.dao.CourseDao().findAll().stream()
                            .filter(c2 -> c2.getName().equals(cal.getName()))
                            .findFirst().orElse(null);
                } catch (Exception ex) {
                    System.err.println("Name-based course lookup failed: " + ex.getMessage());
                }
            }
            if (course == null) { System.err.println("Could not resolve Course — cannot save."); return; }

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
                        saved = svc.updateLesson(existing.lessonId(),
                                entry.getStartAsLocalDateTime(), entry.getEndAsLocalDateTime(),
                                resolvedCourse, classroom, groups, users);
                    } else {
                        saved = svc.saveLesson(entry.getStartAsLocalDateTime(),
                                entry.getEndAsLocalDateTime(), resolvedCourse, classroom, groups, users);
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
                    System.err.println("Save failed: " + ex.getMessage());
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

    private HBox getGridCell(GridPane grid, int col, int row) {
        return grid.getChildren().stream()
                .filter(HBox.class::isInstance).map(HBox.class::cast)
                .filter(n -> (Objects.requireNonNullElse(GridPane.getColumnIndex(n), 0)) == col
                          && (Objects.requireNonNullElse(GridPane.getRowIndex(n), 0)) == row)
                .findFirst().orElse(null);
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
                hourField.setText(String.format("%02d", h));
                minuteField.setText(String.format("%02d", m));
                if (isStart) entry.changeStartTime(LocalTime.of(h, m), true);
                else         entry.changeEndTime(LocalTime.of(h, m), false);
            } catch (NumberFormatException ex) {
                LocalTime cur = isStart ? entry.getStartTime() : entry.getEndTime();
                if (cur != null) {
                    hourField.setText(String.format("%02d", cur.getHour()));
                    minuteField.setText(String.format("%02d", cur.getMinute()));
                }
            }
        };

        for (TextField tf : List.of(hourField, minuteField)) {
            tf.focusedProperty().addListener((obs, was, is) -> { if (was && !is) commit.run(); });
            tf.setOnKeyPressed(ke -> { if (ke.getCode() == KeyCode.ENTER) commit.run(); });
        }
        return box;
    }

    private TextField createNumericField(int maxLen, int initialValue) {
        TextField tf = new TextField(String.format("%02d", initialValue));
        tf.setPrefWidth(36); tf.setMaxWidth(36);
        tf.setAlignment(Pos.CENTER);
        tf.setStyle("-fx-font-size: 13px;");
        UnaryOperator<TextFormatter.Change> filter = change ->
                change.getControlNewText().matches("\\d{0," + maxLen + "}") ? change : null;
        tf.setTextFormatter(new TextFormatter<>(filter));
        return tf;
    }

    private String formatDateTime(LocalDate date, LocalTime time) {
        String d = date != null
                ? String.format("%02d.%02d.%04d", date.getDayOfMonth(), date.getMonthValue(), date.getYear())
                : "—";
        String t = time != null
                ? String.format("%02d:%02d", time.getHour(), time.getMinute())
                : "—";
        return d + "  " + t;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
