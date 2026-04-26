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
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
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
public class EntryPopOverContentPane extends PopOverContentPane {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntryPopOverContentPane.class);

    /**
     * Lightweight value holder that identifies a persisted lesson by its database ID.
     * Stored as the user object on a CalendarFX {@link Entry} to distinguish saved
     * entries from newly created, unsaved ones.
     *
     * @param lessonId the database primary key of the persisted lesson
     */
    public record SavedLesson(Long lessonId) {
    }

    /** Holds the mutable state arrays shared between the constructor and listeners. */
    private record EntryState(boolean[] saved, boolean[] saving,
                               Interval[] snapInterval, Calendar<?>[] snapCalendar) {
    }

    private final LocalizationService localizationService = new LocalizationService();
    ResourceBundle selectedBundle = localizationService.getBundle();

    /**
     * Constructs the pop-over content for a calendar entry.
     *
     * @param popOver     the CalendarFX {@link PopOver} hosting this content
     * @param dateControl the {@link DateControl} the entry belongs to
     * @param entry       the calendar entry being viewed or edited
     */
    public EntryPopOverContentPane(PopOver popOver,
                                   DateControl dateControl,
                                   Entry<?> entry) {

        final boolean teacher = SessionManager.getInstance().isTeacher();

        if (teacher) {
            setHeader(buildTeacherHeader(entry, dateControl));
        } else {
            setHeader(buildStudentHeader(entry));
        }

        Lesson existingLesson = loadExistingLesson(entry);

        final Interval[] snapshotInterval = {entry.getInterval()};
        final Calendar<?>[] snapshotCalendar = {entry.getCalendar()};
        final boolean[] savedThisSession = {false};
        final boolean[] savingInProgress = {false};
        EntryState state = new EntryState(savedThisSession, savingInProgress, snapshotInterval, snapshotCalendar);

        if (teacher) {
            addTeacherDetailsPane(dateControl, entry, existingLesson, popOver, state);
        } else {
            addStudentDetailsPane(entry, existingLesson);
        }

        setupPopOverListeners(popOver, entry, state);
        Platform.runLater(this::applyLocalizationAndRTL);
        popOver.setOnShown(e -> applyLocalizationAndRTL());
    }

    private Node buildTeacherHeader(Entry<?> entry, DateControl dateControl) {
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
        return header;
    }

    private Node buildStudentHeader(Entry<?> entry) {
        HBox studentHeader = new HBox();
        studentHeader.setAlignment(Pos.CENTER_LEFT);
        studentHeader.setSpacing(8);
        studentHeader.setPadding(new Insets(8, 12, 8, 12));

        Label titleLabel = new Label();
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        String initialTitle = entry.getCalendar() != null
                ? entry.getCalendar().getName()
                : entry.getTitle();
        titleLabel.setText(initialTitle != null ? initialTitle : "");

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
        return studentHeader;
    }

    private Lesson loadExistingLesson(Entry<?> entry) {
        if (entry.getUserObject() instanceof SavedLesson(var lessonId)) {
            try {
                return new LessonDao().findById(lessonId);
            } catch (Exception ex) {
                LOGGER.warn("Could not load existing lesson: {}", ex.getMessage());
            }
        }
        return null;
    }

    private void addTeacherDetailsPane(DateControl dateControl, Entry<?> entry,
                                        Lesson existingLesson, PopOver popOver, EntryState state) {
        EntryDetailsView details = new EntryDetailsView(entry, dateControl);
        replaceTimeFields(details, entry);
        EventExtraDetailsController ctrl = new EventExtraDetailsController(entry, existingLesson);
        appendExtraFields(details, entry, ctrl, popOver, state);
        PopOverTitledPane detailsPane = new PopOverTitledPane(selectedBundle.getString("event.details"), details);
        getPanes().add(detailsPane);
        setExpandedPane(detailsPane);
    }

    private void addStudentDetailsPane(Entry<?> entry, Lesson existingLesson) {
        GridPane studentView = buildStudentView(entry, existingLesson);
        PopOverTitledPane detailsPane = new PopOverTitledPane(
                selectedBundle.getString("event.details"), studentView);
        getPanes().add(detailsPane);
        setExpandedPane(detailsPane);
    }

    private void setupPopOverListeners(PopOver popOver, Entry<?> entry, EntryState state) {
        popOver.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (Boolean.FALSE.equals(isShowing) && Boolean.TRUE.equals(wasShowing)) {
                handlePopOverHidden(entry, state);
            }
        });

        entry.calendarProperty().addListener((obs, oldCal, newCal) -> {
            if (newCal == null && !state.saving()[0]) {
                Platform.runLater(() -> {
                    if (entry.getCalendar() == null) {
                        popOver.hide(javafx.util.Duration.ZERO);
                    }
                });
            }
        });
    }

    private void handlePopOverHidden(Entry<?> entry, EntryState state) {
        if (state.saving()[0]) {
            return;
        }
        boolean isSaved = entry.getUserObject() instanceof SavedLesson;
        if (!isSaved) {
            entry.removeFromCalendar();
        } else if (!state.saved()[0]) {
            entry.setInterval(state.snapInterval()[0]);
            if (state.snapCalendar()[0] != null && entry.getCalendar() != state.snapCalendar()[0]) {
                entry.setCalendar(state.snapCalendar()[0]);
            }
        }
    }

    /**
     * Builds the read-only detail grid shown to students.
     *
     * @param entry          the calendar entry
     * @param existingLesson the persisted lesson, or {@code null} for new entries
     * @return a populated {@link GridPane}
     */
    private GridPane buildStudentView(Entry<?> entry, Lesson existingLesson) {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);

        ColumnConstraints col0 = new ColumnConstraints();
        ColumnConstraints col1 = new ColumnConstraints();
        grid.getColumnConstraints().addAll(col0, col1);

        Label startVal = valueLabel(formatDateTime(entry.getStartDate(), entry.getStartTime()));
        entry.intervalProperty().addListener((obs, o, n) ->
                startVal.setText(formatDateTime(entry.getStartDate(), entry.getStartTime())));
        addRow(grid, 4, "Start:", startVal);

        Label endVal = valueLabel(formatDateTime(entry.getEndDate(), entry.getEndTime()));
        entry.intervalProperty().addListener((obs, o, n) ->
                endVal.setText(formatDateTime(entry.getEndDate(), entry.getEndTime())));
        addRow(grid, 5, "End:", endVal);

        String classroom = "—";
        String teacherName = "—";
        String groupsText = "—";

        if (existingLesson != null) {
            if (existingLesson.getClassroom() != null && !existingLesson.getClassroom().isBlank()) {
                classroom = existingLesson.getClassroom();
            }

            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                teacherName = currentUser.getFirstName() + " " + currentUser.getSureName();
            }

            List<StudentGroup> groups = existingLesson.getAssignedGroups();
            if (!groups.isEmpty()) {
                groupsText = groups.stream()
                        .map(StudentGroup::getDisplayFieldOfStudies)
                        .collect(Collectors.joining(", "));
            }
        }

        addRow(grid, 0, selectedBundle.getString("event.class"),   valueLabel(classroom));
        addRow(grid, 1, selectedBundle.getString("event.teacher"), valueLabel(teacherName));
        addRow(grid, 3, selectedBundle.getString("event.group"),   valueLabel(groupsText));

        return grid;
    }

    /**
     * Adds a key–value row to a grid pane.
     */
    private void addRow(GridPane grid, int row, String labelText, Label value) {
        Label key = new Label(labelText);
        key.setStyle("-fx-text-fill:#666666; -fx-font-size:12px;");
        GridPane.setHalignment(key, HPos.RIGHT);
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

    /**
     * Locks the CalendarFX entry title field to the calendar's name using a CSS lookup
     * instead of reflection, making the field read-only.
     */
    private void lockTitleToCalendarName(EntryHeaderView header, Entry<?> entry) {
        if (entry.getCalendar() != null) {
            entry.setTitle(entry.getCalendar().getName());
        }
        Platform.runLater(() -> {
            Node tfNode = header.lookup(".text-field");
            if (tfNode instanceof TextField titleField) {
                titleField.setEditable(false);
                titleField.setMouseTransparent(true);
                titleField.setFocusTraversable(false);
                titleField.setStyle("-fx-cursor: default; -fx-background-color: transparent;"
                                    + " -fx-border-color: transparent;");
            }
        });

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
                .findFirst().orElse(null);
        if (grid == null) {
            return;
        }

        HBox startBox = getGridCell(grid, 1, 1);
        HBox endBox = getGridCell(grid, 1, 2);
        if (startBox != null) {
            removeTimeField(startBox);
            startBox.getChildren().add(buildTimeInput(entry, true));
        }
        if (endBox != null) {
            removeTimeField(endBox);
            endBox.getChildren().add(buildTimeInput(entry, false));
        }
    }

    /**
     * Appends the extra lesson fields and Save/Delete action bar to the details grid.
     */
    private void appendExtraFields(EntryDetailsView details,
                                   Entry<?> entry,
                                   EventExtraDetailsController ctrl,
                                   PopOver popOver,
                                   EntryState state) {
        GridPane grid = details.getChildren().stream()
                .filter(GridPane.class::isInstance)
                .map(GridPane.class::cast)
                .findFirst().orElse(null);
        if (grid == null) {
            return;
        }

        if (grid.getColumnConstraints().size() >= 2) {
            ColumnConstraints col2 = grid.getColumnConstraints().get(1);
            col2.setHgrow(Priority.ALWAYS);
            col2.setFillWidth(true);
        }

        final int extraRows = 4;
        grid.getChildren().forEach(n -> {
            Integer r = GridPane.getRowIndex(n);
            GridPane.setRowIndex(n, (r == null ? 0 : r) + extraRows);
        });

        buildRow(grid, 0, selectedBundle.getString("event.class"),      ctrl.getClassIdNode());
        buildRow(grid, 1, selectedBundle.getString("event.teacher"),    ctrl.getTeacherNode());
        buildRow(grid, 2, selectedBundle.getString("event.group"),       ctrl.getGroupRowNode());
        buildRow(grid, 3, selectedBundle.getString("event.students"), ctrl.getStudentsNode());

        int lastRow = grid.getChildren().stream()
                .mapToInt(n -> {
                    Integer r = GridPane.getRowIndex(n);
                    return r == null ? 0 : r;
                })
                .max().orElse(extraRows);

        Button deleteBtn = buildDeleteButton(entry);
        Button saveBtn = buildSaveButton(entry, ctrl, popOver, state);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox actionBar = new HBox(deleteBtn, spacer, saveBtn);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.setMaxWidth(Double.MAX_VALUE);
        GridPane.setMargin(actionBar, new Insets(10, 0, 4, 0));
        GridPane.setColumnSpan(actionBar, 2);
        grid.add(actionBar, 0, lastRow + 1);
    }

    private Button buildDeleteButton(Entry<?> entry) {
        Button deleteBtn = new Button(selectedBundle.getString("event.delete.button"));
        deleteBtn.setStyle("-fx-background-color: #D03800; -fx-text-fill: white;"
                           + " -fx-cursor: hand; -fx-background-radius: 6;");
        deleteBtn.setOnAction(e -> {
            if (entry.getUserObject() instanceof SavedLesson(var lessonId)) {
                Thread.ofVirtual().name("delete-lesson-thread").start(() -> {
                    try {
                        new LessonService(new LessonDao()).deleteLesson(lessonId);
                    } catch (Exception ex) {
                        LOGGER.warn("Delete failed: {}", ex.getMessage());
                    }
                });
            }
            entry.removeFromCalendar();
        });
        return deleteBtn;
    }

    private Button buildSaveButton(Entry<?> entry, EventExtraDetailsController ctrl,
                                    PopOver popOver, EntryState state) {
        Button saveBtn = new Button(selectedBundle.getString("event.save.button"));
        saveBtn.setStyle("-fx-background-color: #00956D; -fx-text-fill: white;"
                         + " -fx-cursor: hand; -fx-background-radius: 6;");
        saveBtn.setOnAction(e -> {
            Calendar<?> cal = entry.getCalendar();
            if (cal == null) {
                LOGGER.warn("Entry has no calendar — cannot save.");
                return;
            }
            Course course = resolveCourse(cal);
            if (course == null) {
                LOGGER.warn("Could not resolve Course — cannot save.");
                return;
            }
            executeSave(entry, ctrl, popOver, state, saveBtn, course);
        });
        return saveBtn;
    }

    private Course resolveCourse(Calendar<?> cal) {
        if (cal.getUserObject() instanceof Course c) {
            return c;
        }
        try {
            return new org.dao.CourseDao().findAll().stream()
                    .filter(c2 -> c2.getName().equals(cal.getName()))
                    .findFirst().orElse(null);
        } catch (Exception ex) {
            LOGGER.warn("Name-based course lookup failed: {}", ex.getMessage());
            return null;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void executeSave(Entry<?> entry, EventExtraDetailsController ctrl,
                              PopOver popOver, EntryState state, Button saveBtn, Course course) {
        String classroom = ctrl.getClassroom();
        var groups = ctrl.getSelectedGroups();
        var users  = ctrl.getSelectedUsers();
        saveBtn.setDisable(true);
        saveBtn.setText(selectedBundle.getString("event.saving.button"));
        state.saving()[0] = true;

        Thread.ofVirtual().name("save-lesson-thread").start(() -> {
            try {
                LessonService svc = new LessonService(new LessonDao());
                Lesson saved;
                if (entry.getUserObject() instanceof SavedLesson(var existingId)) {
                    saved = svc.updateLesson(existingId,
                            entry.getStartAsLocalDateTime(), entry.getEndAsLocalDateTime(),
                            course, classroom, groups, users);
                } else {
                    saved = svc.saveLesson(entry.getStartAsLocalDateTime(),
                            entry.getEndAsLocalDateTime(), course, classroom, groups, users);
                }
                Platform.runLater(() -> {
                    ((Entry) entry).setUserObject(new SavedLesson(saved.getId()));
                    state.snapInterval()[0] = entry.getInterval();
                    state.snapCalendar()[0] = entry.getCalendar();
                    state.saved()[0] = true;
                    state.saving()[0] = false;
                    popOver.hide(javafx.util.Duration.ZERO);
                });
            } catch (Exception ex) {
                LOGGER.warn("Save failed: {}", ex.getMessage());
                Platform.runLater(() -> {
                    state.saving()[0] = false;
                    saveBtn.setDisable(false);
                    saveBtn.setText(selectedBundle.getString("event.save.button"));
                });
            }
        });
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

    /**
     * Builds an HH:MM time input for the given entry's start or end time.
     */
    private HBox buildTimeInput(Entry<?> entry, boolean isStart) {
        LocalTime initial = isStart ? entry.getStartTime() : entry.getEndTime();
        TextField hourField = createNumericField(2, initial != null ? initial.getHour() : 0);
        TextField minuteField = createNumericField(2, initial != null ? initial.getMinute() : 0);
        Label colon = new Label(":");
        colon.setStyle("-fx-font-size: 14px; -fx-padding: 0 2 0 2;");
        HBox box = new HBox(2, hourField, colon, minuteField);
        box.setAlignment(Pos.CENTER_LEFT);

        entry.intervalProperty().addListener((obs, oldI, newI) ->
                syncTimeFields(entry, isStart, hourField, minuteField));

        Runnable commit = buildCommitRunnable(entry, isStart, hourField, minuteField);
        setupFieldListeners(List.of(hourField, minuteField), commit);
        return box;
    }

    private void syncTimeFields(Entry<?> entry, boolean isStart, TextField hourField, TextField minuteField) {
        LocalTime t = isStart ? entry.getStartTime() : entry.getEndTime();
        if (t != null) {
            hourField.setText(String.format("%02d", t.getHour()));
            minuteField.setText(String.format("%02d", t.getMinute()));
        }
    }

    private Runnable buildCommitRunnable(Entry<?> entry, boolean isStart,
                                          TextField hourField, TextField minuteField) {
        return () -> {
            try {
                int h = Math.clamp(Integer.parseInt(hourField.getText().trim()), 0, 23);
                int m = Math.clamp(Integer.parseInt(minuteField.getText().trim()), 0, 59);
                hourField.setText(String.format("%02d", h));
                minuteField.setText(String.format("%02d", m));
                if (isStart) {
                    entry.changeStartTime(LocalTime.of(h, m), true);
                } else {
                    entry.changeEndTime(LocalTime.of(h, m), false);
                }
            } catch (NumberFormatException ex) {
                LocalTime cur = isStart ? entry.getStartTime() : entry.getEndTime();
                if (cur != null) {
                    hourField.setText(String.format("%02d", cur.getHour()));
                    minuteField.setText(String.format("%02d", cur.getMinute()));
                }
            }
        };
    }

    private void setupFieldListeners(List<TextField> fields, Runnable commit) {
        for (TextField tf : fields) {
            tf.focusedProperty().addListener((obs, was, is) -> {
                if (Boolean.TRUE.equals(was) && Boolean.FALSE.equals(is)) {
                    commit.run();
                }
            });
            tf.setOnKeyPressed(ke -> {
                if (ke.getCode() == KeyCode.ENTER) {
                    commit.run();
                }
            });
        }
    }

    private TextField createNumericField(int maxLen, int initialValue) {
        TextField tf = new TextField(String.format("%02d", initialValue));
        tf.setPrefWidth(36);
        tf.setMaxWidth(36);
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

    /**
     * Applies RTL orientation for Arabic and translates residual CalendarFX English labels.
     */
    private void applyLocalizationAndRTL() {
        ResourceBundle bundle = localizationService.getBundle();
        boolean isRTL = bundle.getLocale().getLanguage().equals("ar");

        if (isRTL) {
            this.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        }

        Platform.runLater(() ->
            this.lookupAll("*").forEach(node -> applyToNode(node, isRTL, bundle))
        );
    }

    private void applyToNode(Node node, boolean isRTL, ResourceBundle bundle) {
        if (isRTL) {
            node.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        }
        if (node instanceof Labeled labeled) {
            String text = labeled.getText();
            if (text == null || text.isBlank()) {
                return;
            }
            String localized = translate(text, bundle);
            if (localized != null) {
                labeled.setText(localized);
            }
        }
    }

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
        if (key == null) {
            return null;
        }

        try {
            String translated = bundle.getString(key);
            return text.contains(":") ? translated + ":" : translated;
        } catch (java.util.MissingResourceException e) {
            return text;
        }
    }
}
