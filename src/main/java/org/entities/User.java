package org.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name="name", nullable=false)
    private String firstName;

    @Column(name="surename", nullable=false)
    private String sureName;

    @Column(nullable=false, unique=true)
    private String username;

    @Column(nullable=false, unique=true)
    private String email;

    @Column(name="phone_number")
    private String phoneNumber;

    @Column(name="password", nullable=false)
    private String passwordHash;

    /*
    @Column(nullable=false)
    private boolean enabled = true;

    @Column(name="created_at", updatable=false)
    private LocalDateTime createdAt = LocalDateTime.now();
     */

    public User() {}
}

