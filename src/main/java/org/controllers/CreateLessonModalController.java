package org.controllers;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.dao.LessonDao;
import org.dao.NotificationDao;
import org.entities.Course;
import org.entities.Lesson;
import org.entities.User;
import org.service.LessonService;
import org.service.LocalizationService;
import org.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the lesson create/edit modal ({@code create-lesson-modal.fxml}).
 * Operates in two modes depending on whether an existing {@link Lesson} is attached
 * to the calendar entry:
 * <ul>
 *   <li><b>Create mode</b> — no lesson on the entry; saves a new lesson.</li>
 *   <li><b>Edit mode</b> — lesson already attached; allows updating or deleting it.</li>
 * </ul>
 */
public class CreateLessonModalController {

    @FXML private Text modalTitleLabel;
    @FXML private TextField classroomField;
    @FXML private ComboBox<Course> courseComboBox;
    @FXML private Button deleteButton;
    @FXML private Button confirmButton;

    private Entry<?> entry;
    private Calendar<?> calendar;
    private boolean isEditMode = false;
    private Long dbLessonId = null;

    private final LessonService lessonService = new LessonService(
            new LessonDao(), new NotificationService(new NotificationDao()));
    private final LocalizationService localizationService = new LocalizationService();
    private ResourceBundle bundle;

    /**
     * Binds the modal to a calendar entry and determines the editing mode.
     * If the entry's user object is an existing {@link Lesson}, the modal opens
     * in edit mode and stores the lesson's DB ID.
     *
     * @param entry the CalendarFX entry this modal was opened for
     */
    public void setEntry(Entry<?> entry) {
        this.entry = entry;
        this.calendar = entry != null ? entry.getCalendar() : null;
        this.isEditMode = (entry != null && entry.getUserObject() instanceof Lesson);
        if (this.isEditMode) {
            Lesson lesson = (Lesson) entry.getUserObject();
            this.dbLessonId = lesson.getId();
        }
    }

    /**
     * JavaFX initialize callback — loads the resource bundle and populates
     * the course combo box from the database.
     */
    @FXML
    public void initialize() {
        bundle = localizationService.getBundle();
        List<Course> courses = lessonService.getAllCourses();
        if (courses != null) {
            courseComboBox.getItems().addAll(courses);
        }
    }

    /**
     * Configures the modal UI for the current mode (create or edit).
     * Must be called after {@link #setEntry(Entry)} and after the FXML is fully loaded.
     */
    public void applyProps() {
        if (isEditMode) {
            modalTitleLabel.setText(bundle.getString("modal.lesson.edit.title"));
            confirmButton.setText(bundle.getString("modal.lesson.save.button"));
            deleteButton.setVisible(true);
            if (entry != null && entry.getUserObject() instanceof Lesson lesson) {
                classroomField.setText(lesson.getClassroom());
                courseComboBox.setValue(lesson.getCourse());
            }
        } else {
            modalTitleLabel.setText(bundle.getString("modal.lesson.create.title"));
            confirmButton.setText(bundle.getString("modal.lesson.add.button"));
            deleteButton.setVisible(false);
            classroomField.setText("");
            courseComboBox.setValue(null);
        }
    }

    /**
     * FXML action handler for the confirm button.
     * Validates input and either creates a new lesson or updates the existing one.
     * Recipients for notifications are derived from the lesson's currently assigned
     * users (edit mode), or an empty list (create mode, as no groups are assigned yet).
     */
    @FXML
    private void handleConfirm() {
        String classroom = classroomField.getText();
        Course selectedCourse = courseComboBox.getValue();

        if (classroom == null || classroom.isBlank() || selectedCourse == null || entry == null) {
            return;
        }

        LocalDateTime startDt = LocalDateTime.of(entry.getStartDate(), entry.getStartTime());
        LocalDateTime endDt = LocalDateTime.of(entry.getEndDate(), entry.getEndTime());

        // For edit mode derive recipients from the lesson's already-assigned users.
        // For new lessons no groups are assigned yet so the list is empty.
        boolean hasUsers = isEditMode && entry.getUserObject() instanceof Lesson l
                && !l.getAssignedUsers().isEmpty();
        List<Long> recipientIds = hasUsers
                ? ((Lesson) entry.getUserObject()).getAssignedUsers().stream().map(User::getId).toList()
                : List.of();

        try {
            if (isEditMode && dbLessonId != null) {
                Lesson updated = lessonService.updateLesson(
                    dbLessonId, startDt, endDt, classroom, recipientIds
                );
                @SuppressWarnings("unchecked")
                Entry<Object> genericEntry = (Entry<Object>) entry;
                genericEntry.setUserObject(updated);
                genericEntry.setTitle(updated.getCourse().getDisplayName() + " - " + updated.getClassroom());
            } else {
                Lesson saved = lessonService.addLesson(
                    startDt, endDt, selectedCourse.getId(), classroom, List.of()
                );
                @SuppressWarnings("unchecked")
                Entry<Object> genericEntry = (Entry<Object>) entry;
                genericEntry.setUserObject(saved);
                genericEntry.setTitle(saved.getCourse().getDisplayName() + " - " + saved.getClassroom());
                if (calendar != null) {
                    calendar.addEntry(entry);
                }
            }
            closeModal();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * FXML action handler for the delete button.
     * Deletes the lesson from the database and removes it from the calendar.
     * Only visible in edit mode.
     */
    @FXML
    private void handleDelete() {
        try {
            if (dbLessonId != null && entry != null) {
                List<Long> recipientIds = (entry.getUserObject() instanceof Lesson l)
                        ? l.getAssignedUsers().stream().map(User::getId).toList()
                        : List.of();
                lessonService.deleteLesson(dbLessonId, recipientIds);
                if (calendar != null) {
                    calendar.removeEntry(entry);
                }
            }
            closeModal();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Closes the modal window. */
    private void closeModal() {
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();
    }
}
