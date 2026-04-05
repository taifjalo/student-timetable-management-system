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
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
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
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Custom CalendarFX pop-over content pane shown when a user clicks on a calendar entry.
 * Replaces the default CalendarFX pop-over with a role-aware layout:
 *
 * <ul>
 *   <li><b>Teachers</b> get the full CalendarFX {@link EntryHeaderView} (title locked to
 *       the calendar name), custom HH:MM time inputs, and the {@link EventExtraDetailsController}
 *       extra fields (classroom, teacher, groups, students) plus Save/Delete action buttons.</li>
 *   <li><b>Students</b> get a simple read-only header (course name label) and a
 *       {@link javafx.scene.layout.GridPane} grid showing start/end time, classroom,
 *       teacher, and assigned groups — no editing controls.</li>
 * </ul>
 *
 * <p>Unsaved new entries are removed from the calendar when the pop-over closes.
 * Saved entries whose interval was changed but not yet re-saved are reverted to the
 * snapshot taken at construction time.
 *
 * <p>After the pop-over is shown, {@link #applyLocalizationAndRTL()} is called to
 * translate any residual English CalendarFX labels and apply RTL orientation for Arabic.
 */
public class CustomEntryPopOverContentPane extends PopOverContentPane {

    /**
     * Lightweight value holder that identifies a persisted lesson by its database ID.
     * Stored as the user object on a CalendarFX {@link Entry} to distinguish saved
     * entries from newly created, unsaved ones.
     *
     * @param lessonId the database primary key of the persisted lesson
     */
    public record SavedLesson(Long lessonId) {}

    private final LocalizationService localizationService = new LocalizationService();
    ResourceBundle selectedBundle = localizationService.getBundle();

    /**
     * Constructs the pop-over content for a calendar entry.
     * Builds a role-appropriate header and detail pane, wires up pop-over lifecycle
     * listeners for unsaved-entry cleanup and interval revert, and schedules RTL
     * localization after the pop-over is shown.
     *
     * @param popOver     the CalendarFX {@link PopOver} hosting this content
     * @param dateControl the {@link DateControl} the entry belongs to
     * @param entry       the calendar entry being viewed or edited
     */
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
        Platform.runLater(this::applyLocalizationAndRTL);
        popOver.setOnShown(e -> applyLocalizationAndRTL());
    }


    /**
     * Builds the read-only detail grid shown to students.
     * Displays start/end time, classroom, teacher, and assigned groups.
     * All values fall back to {@code "—"} when unavailable.
     *
     * @param entry          the calendar entry
     * @param existingLesson the persisted lesson, or {@code null} for new entries
     * @return a populated {@link GridPane}
     */
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

    /**
     * Adds a key–value row to a grid pane. The key label is right-aligned in column 0;
     * the value label fills column 1 with horizontal grow.
     *
     * @param grid      the target grid
     * @param row       the grid row index
     * @param labelText the key label text
     * @param value     the value label node
     */
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

    /**
     * Creates a styled value {@link Label} with text wrapping enabled.
     * Returns a label showing {@code "—"} when {@code text} is {@code null}.
     *
     * @param text the text to display
     * @return the configured label
     */
    private Label valueLabel(String text) {
        Label lbl = new Label(text != null ? text : "—");
        lbl.setStyle("-fx-font-size:13px;");
        lbl.setWrapText(true);
        return lbl;
    }

    /**
     * Locks the CalendarFX entry title text field to the calendar's name so it
     * cannot be edited independently. Uses reflection to access the private
     * {@code titleField} in {@link EntryHeaderView} and makes it read-only.
     * Also hides the calendar-picker combo box for non-teachers.
     *
     * @param header  the CalendarFX header view
     * @param entry   the entry whose title to sync
     * @param teacher {@code true} if the current user is a teacher
     */
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


    /**
     * Replaces the default CalendarFX {@link TimeField} widgets in the
     * {@link EntryDetailsView} grid with custom HH:MM text-field pairs built by
     * {@link #buildTimeInput(Entry, boolean)}. Targets grid cells (col 1, row 1) for
     * start and (col 1, row 2) for end.
     *
     * @param details the CalendarFX details view whose time fields to replace
     * @param entry   the entry whose interval the new inputs operate on
     */
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

    /**
     * Appends the extra lesson fields (classroom, teacher, group, students) and the
     * Save/Delete action bar to the {@link EntryDetailsView} grid.
     * Shifts all existing grid rows down by {@code EXTRA_ROWS} to make space at the top.
     * The Save button persists the lesson via {@link LessonService} on a background thread;
     * the Delete button deletes it and removes the entry from the calendar.
     *
     * @param details           the CalendarFX details view to append to
     * @param entry             the calendar entry being edited
     * @param ctrl              the extra-details widget supplying classroom/group/user values
     * @param popOver           the hosting pop-over (closed after a successful save)
     * @param savedThisSession  single-element array flag: {@code true} after a successful save
     * @param savingInProgress  single-element array flag: {@code true} while save is running
     * @param snapshotInterval  single-element array holding the interval at pop-over open time
     * @param snapshotCalendar  single-element array holding the calendar at pop-over open time
     */
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

    /**
     * Adds a labeled row to the detail grid. The label is right-aligned with margin;
     * the field node is placed in column 1 with horizontal grow.
     *
     * @param grid      the target grid
     * @param row       the grid row index
     * @param labelText the left-column label text
     * @param field     the right-column control/node
     */
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

    /**
     * Finds the {@link HBox} at the given grid column and row, or {@code null} if absent.
     *
     * @param grid the grid to search
     * @param col  the column index
     * @param row  the row index
     * @return the matching {@link HBox}, or {@code null}
     */
    private HBox getGridCell(GridPane grid, int col, int row) {
        return grid.getChildren().stream()
                .filter(HBox.class::isInstance).map(HBox.class::cast)
                .filter(n -> (Objects.requireNonNullElse(GridPane.getColumnIndex(n), 0)) == col
                          && (Objects.requireNonNullElse(GridPane.getRowIndex(n), 0)) == row)
                .findFirst().orElse(null);
    }

    /**
     * Removes the first {@link TimeField} child from the given {@link HBox}.
     *
     * @param box the HBox containing a CalendarFX TimeField
     */
    private void removeTimeField(HBox box) {
        box.getChildren().removeIf(TimeField.class::isInstance);
    }

    /**
     * Builds an HH:MM time input consisting of two numeric text fields and a colon separator.
     * Commits the parsed time to the entry interval on focus-lost or Enter key press;
     * reverts to the entry's current time on invalid input.
     * Listens on the entry's interval property to keep the fields in sync with external changes.
     *
     * @param entry   the entry whose start or end time is being edited
     * @param isStart {@code true} to control the start time; {@code false} for end time
     * @return the constructed {@link HBox} time input widget
     */
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

    /**
     * Creates a fixed-width numeric text field with a digit-count formatter.
     * The field is pre-filled with {@code initialValue} zero-padded to two digits.
     *
     * @param maxLen       the maximum number of digits allowed
     * @param initialValue the initial integer value to display
     * @return the configured {@link TextField}
     */
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

    /**
     * Formats a date and time as {@code "dd.MM.yyyy  HH:mm"}.
     * Returns {@code "—"} for any {@code null} component.
     *
     * @param date the local date, or {@code null}
     * @param time the local time, or {@code null}
     * @return the formatted string
     */
    private String formatDateTime(LocalDate date, LocalTime time) {
        String d = date != null
                ? String.format("%02d.%02d.%04d", date.getDayOfMonth(), date.getMonthValue(), date.getYear())
                : "—";
        String t = time != null
                ? String.format("%02d:%02d", time.getHour(), time.getMinute())
                : "—";
        return d + "  " + t;
    }

    /**
     * Clamps an integer value to the inclusive range [{@code min}, {@code max}].
     *
     * @param value the value to clamp
     * @param min   the minimum allowed value
     * @param max   the maximum allowed value
     * @return the clamped value
     */
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Applies RTL node orientation when the active locale is Arabic, then walks all
     * descendant nodes to translate residual hard-coded English text using
     * {@link #translate(String, ResourceBundle)}.
     * Runs on the JavaFX thread via {@link Platform#runLater(Runnable)}.
     */
    private void applyLocalizationAndRTL() {

        ResourceBundle bundle = localizationService.getBundle();
        boolean isRTL = bundle.getLocale().getLanguage().equals("ar");

        if (isRTL) {
            this.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        }

        Platform.runLater(() -> {

            this.lookupAll("*").forEach(node -> {

                if (isRTL) {
                    node.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                }


                if (node instanceof Labeled labeled) {

                    String text = labeled.getText();
                    if (text == null || text.isBlank()) return;

                    String localized = translate(text, bundle);
                    if (localized != null) {
                        labeled.setText(localized);
                    }
                }
            });
        });
    }

    /**
     * Looks up a localized replacement for a known CalendarFX label string.
     * Strips a trailing colon before the map lookup and restores it on the result
     * if the original text ended with one.
     * Returns {@code null} if the text is not in the known-label map.
     *
     * @param text   the raw label text from the CalendarFX node
     * @param bundle the active locale's resource bundle
     * @return the translated string (with colon if applicable), or {@code null} if not found
     */
    private String translate(String text, ResourceBundle bundle) {

        String clean = text.replace(":", "").trim();

        Map<String, String> map = Map.of(
                "Today", "today.button",
                "Full Day", "calendar.label.allDay",
                "From", "event.from",
                "To", "event.to",
                "Repeat", "event.repeat"
        );

        String key = map.get(clean);
        if (key == null) return null;

        try {
            String translated = bundle.getString(key);

            return text.contains(":") ? translated + ":" : translated;

        } catch (Exception e) {
            return text;
        }
    }
}
