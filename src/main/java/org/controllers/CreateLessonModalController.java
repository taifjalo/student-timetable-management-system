package org.controllers;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
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
import org.service.LessonService;
import org.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;

public class CreateLessonModalController {

    @FXML private Text modalTitleLabel;
    @FXML private TextField classroomField;
    @FXML private ComboBox<Course> courseComboBox;
    @FXML private Button deleteButton;
    @FXML private Button confirmButton;

    private Entry<?> entry;
    private Calendar calendar;
    private boolean isEditMode = false;
    private Long dbLessonId = null;

    private final LessonService lessonService = new LessonService(new LessonDao(), new NotificationService(new NotificationDao()));

    // Hardcoded recipient IDs for lesson event alerts
    private static final List<Long> HARDCODED_RECIPIENT_IDS = List.of(1L);

    public void setEntry(Entry<?> entry) {
        this.entry = entry;
        this.calendar = entry != null ? entry.getCalendar() : null;
        this.isEditMode = (entry != null && entry.getUserObject() instanceof Lesson);
        if (this.isEditMode) {
            Lesson lesson = (Lesson) entry.getUserObject();
            this.dbLessonId = lesson.getId();
        }
    }

    @FXML
    public void initialize() {
        // Load courses into combo box
        List<Course> courses = lessonService.getAllCourses();
        if (courses != null) {
            courseComboBox.getItems().addAll(courses);
        }
    }

    public void applyProps() {
        if (isEditMode) {
            modalTitleLabel.setText("Edit Lesson");
            confirmButton.setText("Save");
            deleteButton.setVisible(true);
            if (entry != null && entry.getUserObject() instanceof Lesson lesson) {
                classroomField.setText(lesson.getClassroom());
                courseComboBox.setValue(lesson.getCourse());
            }
        } else {
            modalTitleLabel.setText("Create Lesson");
            confirmButton.setText("Add");
            deleteButton.setVisible(false);
            classroomField.setText("");
            courseComboBox.setValue(null);
        }
    }

    @FXML
    private void handleConfirm() {
        String classroom = classroomField.getText();
        Course selectedCourse = courseComboBox.getValue();

        if (classroom == null || classroom.isBlank() || selectedCourse == null || entry == null) {
            System.out.println("Validation failed: classroom or course is missing");
            return;
        }

        LocalDateTime startDt = LocalDateTime.of(entry.getStartDate(), entry.getStartTime());
        LocalDateTime endDt = LocalDateTime.of(entry.getEndDate(), entry.getEndTime());

        try {
            if (isEditMode && dbLessonId != null) {
                Lesson updated = lessonService.updateLesson(
                    dbLessonId, startDt, endDt, classroom, HARDCODED_RECIPIENT_IDS
                );
                @SuppressWarnings("unchecked")
                Entry<Object> genericEntry = (Entry<Object>) entry;
                genericEntry.setUserObject(updated);
                genericEntry.setTitle(updated.getCourse().getName() + " - " + updated.getClassroom());
            } else {
                Lesson saved = lessonService.addLesson(
                    startDt, endDt, selectedCourse.getId(), classroom, HARDCODED_RECIPIENT_IDS
                );
                @SuppressWarnings("unchecked")
                Entry<Object> genericEntry = (Entry<Object>) entry;
                genericEntry.setUserObject(saved);
                genericEntry.setTitle(saved.getCourse().getName() + " - " + saved.getClassroom());
                if (calendar != null) {
                    calendar.addEntry(entry);
                }
            }
            closeModal();
        } catch (Exception e) {
            System.out.println("DB operation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        try {
            if (dbLessonId != null) {
                lessonService.deleteLesson(dbLessonId, HARDCODED_RECIPIENT_IDS);
                if (calendar != null && entry != null) {
                    calendar.removeEntry(entry);
                }
            }
            closeModal();
        } catch (Exception e) {
            System.out.println("Delete failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void closeModal() {
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();
    }
}
