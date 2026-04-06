package org.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA entity representing a student's academic profile.
 * Maps to the {@code student_profiles} table.
 * Uses a shared primary key with {@link User} via {@code @MapsId}, so
 * {@code userId == user.getId()}.
 */
@Entity
@Table(name = "student_profiles")
public class StudentProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "study_start_date", nullable = false)
    private LocalDateTime studyStartDate;

    @Column(name = "study_end_date", nullable = false)
    private LocalDate studyEndDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_code")
    private StudentGroup studentGroup;

    /** Required no-arg constructor for JPA. */
    public StudentProfile() {}

    /**
     * Creates a new student profile.
     *
     * @param user            the user this profile belongs to
     * @param studyStartDate  date-time when the student started their studies
     * @param studyEndDate    expected graduation / end date
     * @param studentGroup    the group this student is assigned to (may be {@code null})
     */
    public StudentProfile(User user, LocalDateTime studyStartDate, LocalDate studyEndDate, StudentGroup studentGroup) {
        this.user = user;
        this.studyStartDate = studyStartDate;
        this.studyEndDate = studyEndDate;
        this.studentGroup = studentGroup;
    }

    /** Returns the user ID (mirrors {@code user.getId()} via {@code @MapsId}). */
    public Long getUserId() { return userId; }

    /** Returns the associated user. */
    public User getUser() { return user; }
    /** Sets the associated user. */
    public void setUser(User user) { this.user = user; }

    /** Returns the date-time the student started their studies. */
    public LocalDateTime getStudyStartDate() { return studyStartDate; }
    /** Sets the study start date-time. */
    public void setStudyStartDate(LocalDateTime studyStartDate) { this.studyStartDate = studyStartDate; }

    /** Returns the expected study end date. */
    public LocalDate getStudyEndDate() { return studyEndDate; }
    /** Sets the expected study end date. */
    public void setStudyEndDate(LocalDate studyEndDate) { this.studyEndDate = studyEndDate; }

    /** Returns the student group this student belongs to, or {@code null} if unassigned. */
    public StudentGroup getStudentGroup() { return studentGroup; }
    /** Assigns the student to a group. */
    public void setStudentGroup(StudentGroup studentGroup) { this.studentGroup = studentGroup; }
}
