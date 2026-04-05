package org.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    public StudentProfile() {}

    public StudentProfile(User user, LocalDateTime studyStartDate, LocalDate studyEndDate, StudentGroup studentGroup) {
        this.user = user;
        this.studyStartDate = studyStartDate;
        this.studyEndDate = studyEndDate;
        this.studentGroup = studentGroup;
    }

    public Long getUserId() { return userId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getStudyStartDate() { return studyStartDate; }
    public void setStudyStartDate(LocalDateTime studyStartDate) { this.studyStartDate = studyStartDate; }

    public LocalDate getStudyEndDate() { return studyEndDate; }
    public void setStudyEndDate(LocalDate studyEndDate) { this.studyEndDate = studyEndDate; }

    public StudentGroup getStudentGroup() { return studentGroup; }
    public void setStudentGroup(StudentGroup studentGroup) { this.studentGroup = studentGroup; }
}

