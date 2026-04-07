package org.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

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

    @Transient
    private String localizedFieldOfStudies;

    /** Required no-arg constructor for JPA. */
    public StudentGroup() {
    }

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
    public String getGroupCode() {
        return groupCode;
    }

    /** Sets the group code (primary key). */
    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    /** Returns the display name / field of studies for this group. */
    public String getFieldOfStudies() {
        return fieldOfStudies;
    }

    /** Sets the display name / field of studies. */
    public void setFieldOfStudies(String fieldOfStudies) {
        this.fieldOfStudies = fieldOfStudies;
    }

    /**
     * Returns the best available display name for this group.
     * Prefers {@code localizedFieldOfStudies} if set, falls back to {@code fieldOfStudies},
     * and ultimately returns {@code "Unknown group"} if neither is populated.
     *
     * @return a non-null display string for use in the UI
     */
    public String getDisplayFieldOfStudies() {
        if (localizedFieldOfStudies != null && !localizedFieldOfStudies.isBlank()) {
            return localizedFieldOfStudies;
        }
        if (fieldOfStudies != null && !fieldOfStudies.isBlank()) {
            return fieldOfStudies;
        }
        return "Unknown group";
    }

    /**
     * Returns the locale-specific translation of the field of studies, or {@code null}
     * if no translation has been stored for the current locale.
     *
     * @return the localized field-of-studies string, or {@code null}
     */
    public String getLocalizedFieldOfStudies() {
        return localizedFieldOfStudies;
    }

    /**
     * Sets the locale-specific translation of the field of studies.
     *
     * @param localizedFieldOfStudies the translated string, or {@code null} to clear it
     */
    public void setLocalizedFieldOfStudies(String localizedFieldOfStudies) {
        this.localizedFieldOfStudies = localizedFieldOfStudies;
    }
}
