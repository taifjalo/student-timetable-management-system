package org.entities;

import jakarta.persistence.*;

/**
 * JPA entity representing a student group (e.g. a class cohort like {@code "SWD22S"}).
 * Maps to the {@code student_groups} table.
 * The {@code groupCode} is the natural, user-defined primary key.
 */
@Entity
@Table(name = "student_groups")
public class StudentGroup {

    @Id
    @Column(name = "group_code", nullable = false)
    private String groupCode;

    @Column(name = "field_of_studies", nullable = false)
    private String fieldOfStudies;

    /** Required no-arg constructor for JPA. */
    public StudentGroup() {}

    /**
     * Creates a new student group.
     *
     * @param groupCode      the unique code identifying this group (e.g. {@code "SWD22S"})
     * @param fieldOfStudies display name or field of study for the group
     */
    public StudentGroup(String groupCode, String fieldOfStudies) {
        this.groupCode = groupCode;
        this.fieldOfStudies = fieldOfStudies;
    }

    /** Returns the unique group code that is used as the primary key. */
    public String getGroupCode() { return groupCode; }
    /** Sets the group code (primary key). */
    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }

    /** Returns the display name / field of studies for this group. */
    public String getFieldOfStudies() { return fieldOfStudies; }
    /** Sets the display name / field of studies. */
    public void setFieldOfStudies(String fieldOfStudies) { this.fieldOfStudies = fieldOfStudies; }
}
