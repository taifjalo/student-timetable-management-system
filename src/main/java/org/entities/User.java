// entity. This folder contains the entity classes. The entity classes are annotated with JPA annotations.

package org.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity representing an application user.
 * Maps to the {@code users} table.
 * A user can be a student or a teacher, distinguished by the {@code role} field.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String firstName;

    @Column(name = "surname", nullable = false)
    private String sureName;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "password", nullable = false)
    private String passwordHash;

    @Column(name = "role")
    private String role;

    /** Required no-arg constructor for JPA. */
    public User() {
    }

    /** Returns the surrogate primary key. */
    public Long getId() {
        return id;
    }

    /** Sets the surrogate primary key (used by JPA; do not call manually). */
    public void setId(Long id) {
        this.id = id;
    }

    /** Returns the user's first name. */
    public String getFirstName() {
        return firstName;
    }

    /** Sets the user's first name. */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /** Returns the user's surname. */
    public String getSureName() {
        return sureName;
    }

    /** Sets the user's surname. */
    public void setSureName(String sureName) {
        this.sureName = sureName;
    }

    /** Returns the unique login username. */
    public String getUsername() {
        return username;
    }

    /** Sets the unique login username. */
    public void setUsername(String username) {
        this.username = username;
    }

    /** Returns the unique email address. */
    public String getEmail() {
        return email;
    }

    /** Sets the unique email address. */
    public void setEmail(String email) {
        this.email = email;
    }

    /** Returns the phone number, or {@code null} if not set. */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /** Sets the phone number. */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /** Returns the BCrypt-hashed password stored in the database. */
    public String getPasswordHash() {
        return passwordHash;
    }

    /** Sets the BCrypt-hashed password. */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /** Returns the role string (e.g. {@code "student"} or {@code "teacher"}). */
    public String getRole() {
        return role;
    }

    /** Sets the role string. */
    public void setRole(String role) {
        this.role = role;
    }
}
