package org.entities;

import jakarta.persistence.*;

/**
 * JPA entity representing an academic course (e.g. "Mathematics").
 * Maps to the {@code courses} table.
 * Each course has a display color used to distinguish it on the calendar UI.
 */
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    /** Hex color code (e.g. {@code "#77C04B"}) used to style this course on the calendar. */
    @Column(name = "color_code", nullable = false)
    private String colorCode;

    /** Required no-arg constructor for JPA. */
    public Course() {}

    /**
     * Creates a new course with the given name and color code.
     *
     * @param name      display name of the course
     * @param colorCode hex color string (e.g. {@code "#77C04B"})
     */
    public Course(String name, String colorCode) {
        this.name = name;
        this.colorCode = colorCode;
    }

    /** Returns the surrogate primary key. */
    public Long getId() { return id; }

    /** Sets the surrogate primary key (used by JPA; do not call manually). */
    public void setId(Long id) { this.id = id; }

    /** Returns the display name of this course. */
    public String getName() { return name; }

    /** Sets the display name. */
    public void setName(String name) { this.name = name; }

    /** Returns the hex color code for this course. */
    public String getColorCode() { return colorCode; }

    /** Sets the hex color code. */
    public void setColorCode(String colorCode) { this.colorCode = colorCode; }
}
