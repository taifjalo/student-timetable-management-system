package org.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public Lesson() {}

    public Lesson(LocalDateTime startAt, LocalDateTime endAt, Course course, String classroom) {
        this.startAt = startAt;
        this.endAt = endAt;
        this.course = course;
        this.classroom = classroom;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }

    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public String getClassroom() { return classroom; }
    public void setClassroom(String classroom) { this.classroom = classroom; }

    public List<StudentGroup> getAssignedGroups() { return assignedGroups; }
    public void setAssignedGroups(List<StudentGroup> assignedGroups) { this.assignedGroups = assignedGroups; }

    public List<User> getAssignedUsers() { return assignedUsers; }
    public void setAssignedUsers(List<User> assignedUsers) { this.assignedUsers = assignedUsers; }
}
