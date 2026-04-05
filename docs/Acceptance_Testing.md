# Team 6 - Acceptance Test Plan

**Project:** Student Timetable Management System  
**Version:** 1.0  
**Sprint:** Sprint 6  
**Team Members:** Taif Jalo, Miska Voutilainen, Elias Norta, Nikita Rybakov

---

## 1. Introduction

This Acceptance Test Plan defines the acceptance criteria and test cases for the Student Timetable Management System. The purpose is to verify that all implemented features meet the original project requirements (Product Owner or Customer) and user story definitions before final delivery.

**Note:** This document covers test planning and design only. Test execution is not in scope for Sprint 6.

---

## 2. Acceptance Criteria

### 2.1 Authentication / Authorization
- A user can register with a unique username, and password
- A user can log in with correct credentials and is redirected to the main calendar view
- A user cannot log in with incorrect credentials — an error message is displayed
- Passwords are stored as bcrypt hashes, never in plain text

### 2.2 Timetable / Calendar View
- A student can view their personal timetable in day, week, month, and year views
- A teacher can create, edit, and delete lessons
- Each lesson displays course name, classroom, teacher, and assigned groups
- Lessons are color-coded by course

### 2.3 Course & Group Management
- An admin-`TODO` or teacher can create and delete courses with custom colors
- An admin-`TODO` or teacher can create student groups and assign students to them or remove them from groups
- A teacher can create each lesson by course name, classroom, teacher, and assigned groups, assign students to the lesson, start and end times (All Day/Custom day-time), repeat pattern (None/Daily/Weekly/Monthly), and course color
- Lessons can be assigned to one or more groups 

### 2.4 Messaging
- A user can search for other users by first name, last name and start a conversation
- Messages are sent and received in real time within the same session
- Unread message count is visible in the navbar

### 2.5 Notifications
- Users receive notifications when a new lesson is added to their timetable
- Notifications can be marked as read individually or all at once

### 2.6 User Settings
- A user can update their username, first name, last name, email, and phone number
- A user can change their password by providing the current password and confirming the new one
- A user can select the application language (English, Arabic, Russian, Finnish)
- A user can easily log out from the application, and upon logging out, the session is cleared, and the user is redirected to the Login screen

### 2.7 UI Localization
- All UI text updates correctly when a language is selected
- Arabic selection triggers Right-to-Left layout across all screens
- English, Russian, and Finnish selections use Left-to-Right layout
- user can switch languages at any time, including on the Login screen, and the UI updates immediately without needing to restart the application
- All UI elements are properly aligned and readable in all supported languages, with no text truncation or layout issues
- The application maintains consistent styling and functionality across all languages, ensuring a seamless user experience regardless of the selected language

---

