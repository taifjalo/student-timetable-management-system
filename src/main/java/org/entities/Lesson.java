package org.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a single scheduled lesson on the timetable.
 * Maps to the {@code lessons} table.
 * A lesson belongs to one {@link Course} and can be assigned to multiple
 * {@link StudentGroup}s and individual {@link User}s via join tables.
 */
@Entity
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_id")
    private Long id;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "classroom", nullable = false)
    private String classroom;

    @ManyToMany
    @JoinTable(
        name = "assigned_groups",
        joinColumns = @JoinColumn(name = "lesson_id"),
        inverseJoinColumns = @JoinColumn(name = "group_code")
    )
    private List<StudentGroup> assignedGroups = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "assigned_users",
        joinColumns = @JoinColumn(name = "lesson_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> assignedUsers = new ArrayList<>();

    /** Required no-arg constructor for JPA. */
    public Lesson() {
    }

    /**
     * Creates a new lesson without any group or user assignments.
     *
     * @param startAt   lesson start date-time
     * @param endAt     lesson end date-time
     * @param course    the course this lesson belongs to
     * @param classroom room identifier (e.g. {@code "A101"})
     */
    public Lesson(LocalDateTime startAt, LocalDateTime endAt, Course course, String classroom) {
        this.startAt = startAt;
        this.endAt = endAt;
        this.course = course;
        this.classroom = classroom;
    }

    /** Returns the surrogate primary key. */
    public Long getId() {
        return id;
    }

    /** Sets the surrogate primary key (used by JPA; do not call manually). */
    public void setId(Long id) {
        this.id = id;
    }

    /** Returns the lesson start date-time. */
    public LocalDateTime getStartAt() {
        return startAt;
    }

    /** Sets the lesson start date-time. */
    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    /** Returns the lesson end date-time. */
    public LocalDateTime getEndAt() {
        return endAt;
    }

    /** Sets the lesson end date-time. */
    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    /** Returns the course this lesson belongs to. */
    public Course getCourse() {
        return course;
    }

    /** Sets the course this lesson belongs to. */
    public void setCourse(Course course) {
        this.course = course;
    }

    /** Returns the classroom identifier (e.g. {@code "A101"}). */
    public String getClassroom() {
        return classroom;
    }

    /** Sets the classroom identifier. */
    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    /** Returns a defensive copy of the student groups assigned to this lesson. */
    public List<StudentGroup> getAssignedGroups() {
        return assignedGroups == null ? new ArrayList<>() : new ArrayList<>(assignedGroups);
    }

    /** Sets the student groups assigned to this lesson (stores a defensive copy). */
    public void setAssignedGroups(List<StudentGroup> assignedGroups) {
        this.assignedGroups = assignedGroups == null ? new ArrayList<>() : new ArrayList<>(assignedGroups);
    }

    /** Returns a defensive copy of the individual users directly assigned to this lesson. */
    public List<User> getAssignedUsers() {
        return assignedUsers == null ? new ArrayList<>() : new ArrayList<>(assignedUsers);
    }

    /** Sets the individual users directly assigned to this lesson (stores a defensive copy). */
    public void setAssignedUsers(List<User> assignedUsers) {
        this.assignedUsers = assignedUsers == null ? new ArrayList<>() : new ArrayList<>(assignedUsers);
    }
}
