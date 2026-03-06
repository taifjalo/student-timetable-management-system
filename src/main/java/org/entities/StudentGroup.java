package org.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "student_groups")
public class StudentGroup {

    @Id
    @Column(name = "group_code", nullable = false)
    private String groupCode;

    @Column(name = "field_of_studies", nullable = false)
    private String fieldOfStudies;

    public StudentGroup() {}

    public StudentGroup(String groupCode, String fieldOfStudies) {
        this.groupCode = groupCode;
        this.fieldOfStudies = fieldOfStudies;
    }

    public String getGroupCode() { return groupCode; }
    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }

    public String getFieldOfStudies() { return fieldOfStudies; }
    public void setFieldOfStudies(String fieldOfStudies) { this.fieldOfStudies = fieldOfStudies; }
}
