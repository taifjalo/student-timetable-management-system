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
- A user cannot log in with incorrect credentials - an error message is displayed
- Passwords are stored as bcrypt hashes, never in plain text

### 2.2 Timetable / Calendar View
- A student can view their personal timetable in day, week, month, and year views
- A teacher can create, edit, and delete lessons
- Each lesson displays course name, classroom, teacher, and assigned groups
- Lessons are color-coded by course

### 2.3 Course & Group Management
- An admin or teacher can create and delete courses with custom colors
- An admin or teacher can create student groups and assign students to them or remove them from groups
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

## 3. Test Coverage Matrix

| User Story              | Test Type | Test Case ID | Coverd | 
|-------------------------|---|--------------|-----|
| Register Account        | Functional | TC-01        | Yes |
| Login                   | Functional | TC-02, TC-03 | Yes |
| View Timetable          | Functional | TC-04        | Yes|
| Create Lesson (Teacher) | Functional | TC-05        | Yes |
| Create Course           | Functional | TC-06        | Yes |
| Create Group            | Functional | TC-07        | Yes    |
| Send Message            | Functional | TC-08        | Yes |
| Receive Notification    | Functional | TC-09        | Yes |
| Change Password         | Functional | TC-10        | Yes |
| Select Language         | Functional | TC-11        | Yes |
| RTL Layout (Arabic)     | Functional | TC-12        | Yes |
| Logout                  | Functional | TC-13        | Yes |
| UI Readability          | Usability | TC-14        | Yes |
| Language Switch Speed   | Performance | TC-15        | Yes |
| Login Response Time     | Performance | TC-16        | Yes |


---

## 4. Functional Test Cases

### TC-01 — User Registration
| Field | Detail |
|---|---|
| **User Story** | As a new user, I want to register an account |
| **Precondition** | Application is running, user is on the Register screen |
| **Steps** | 1. Enter unique username. 2. Enter valid email. 3. Enter password. 4. Confirm password. 5. Click Register. |
| **Expected Result** | Account is created, user is redirected to Login screen |
| **Pass Criteria** | User record exists in the `users` table with hashed password |

### TC-02 — Successful Login
| Field | Detail |
|---|---|
| **User Story** | As a registered user, I want to log in |
| **Precondition** | User account exists in the database |
| **Steps** | 1. Enter correct username. 2. Enter correct password. 3. Click Sign in. |
| **Expected Result** | User is redirected to the main calendar view |
| **Pass Criteria** | `SessionManager` holds the logged-in user object |

### TC-03 — Failed Login
| Field | Detail |
|---|---|
| **User Story** | As a user, I want to see an error if my credentials are wrong |
| **Precondition** | Application is running |
| **Steps** | 1. Enter incorrect username or password. 2. Click Sign in. |
| **Expected Result** | Error message is displayed, user stays on Login screen |
| **Pass Criteria** | No session is created, error label is visible |

### TC-04 — View Timetable
| Field | Detail |
|---|---|
| **User Story** | As a student, I want to view my timetable |
| **Precondition** | User is logged in, lessons are assigned to their group |
| **Steps** | 1. Log in. 2. View the calendar. 3. Switch between day/week/month views. |
| **Expected Result** | All assigned lessons appear with correct time, course colour, and classroom |
| **Pass Criteria** | CalendarFX displays events matching the database records |

### TC-05 — Create Lesson
| Field | Detail |
|---|---|
| **User Story** | As a teacher, I want to create a lesson |
| **Precondition** | User is logged in as teacher or admin, courses and groups exist |
| **Steps** | 1. Click on a time slot. 2. Select course, classroom, group. 3. Save. |
| **Expected Result** | Lesson appears on the calendar for all assigned group members |
| **Pass Criteria** | Lesson record exists in `lessons` and `assigned_groups` tables |

### TC-06 — Create Course
| Field | Detail |
|---|---|
| **User Story** | As a teacher, I want to create a course with a colour |
| **Precondition** | User is logged in as teacher or admin |
| **Steps** | 1. Open source tray. 2. Click Create Course. 3. Enter name and select colour. 4. Save. |
| **Expected Result** | Course appears in the source tray with the chosen colour |
| **Pass Criteria** | Course record exists in `courses` table |

### TC-07 — Create Group
| Field | Detail                                                                                |
|---|---------------------------------------------------------------------------------------|
| **User Story** | As a teacher, I want to create a group with a selected students                       |
| **Precondition** | User is logged in as teacher or admin                                                 |
| **Steps** | 1. Open source tray. 2. Click Create Group. 3. Enter name and select students. 4. Save. |
| **Expected Result** | Groups appears in the source tray                               |
| **Pass Criteria** |Groups record exists in `Groups` table                                                |

### TC-08 — Send Message
| Field | Detail |
|---|---|
| **User Story** | As a user, I want to send a message to another user |
| **Precondition** | Both users exist in the system |
| **Steps** | 1. Open Messages. 2. Search for recipient. 3. Type message. 4. Click Send. |
| **Expected Result** | Message appears in the conversation for both sender and recipient |
| **Pass Criteria** | Message record exists in `messages` table with correct `sender_user_id` and `recipient_user_id` |

### TC-09 — Receive Notification
| Field | Detail                                                                                                   |
|---|----------------------------------------------------------------------------------------------------------|
| **User Story** | As a user, I want to receive notifications for new lessons, upcoming classes, changes, or cancellations. |
| **Precondition** | User is logged in, a new lesson is created and assigned to their group                                   |
| **Steps** | 1. Log in. 2. Wait for a new lesson to be created. 3. Observe notifications.                             |
| **Expected Result** | Notification appears in the notifications pane with correct lesson details                               |
| **Pass Criteria** | Notification record exists in `notifications` table with correct `user_id` and `message` content         |

### TC-10 — Change Password
| Field | Detail |
|---|---|
| **User Story** | As a user, I want to change my password |
| **Precondition** | User is logged in |
| **Steps** | 1. Open Settings. 2. Enter current password. 3. Enter new password twice. 4. Save. |
| **Expected Result** | Password is updated, success message shown |
| **Pass Criteria** | New bcrypt hash is stored in `users` table |

### TC-11&12 — Select Language
| Field | Detail                                                                                               |
|---|------------------------------------------------------------------------------------------------------|
| **User Story** | As a user, I want to switch the UI language                                                          |
| **Precondition** | User is on Login screen or User Setting                                                              |
| **Steps** | 1. From the Navbar Navigate to User Setting 2. Click language menu. 3. Select Arabic. 4. Observe UI. |
| **Expected Result** | All UI text updates to Arabic, layout switches to RTL                                                |
| **Pass Criteria** | `LocalizationService.currentLocale` is `ar_IQ`, root pane orientation is `RIGHT_TO_LEFT`             |

### TC-13 — Logout
| Field | Detail                                                                                              |
|---|-----------------------------------------------------------------------------------------------------|
| **User Story** | As a user, I want to log out of the system, so that my account stays secure when I’m done using it. |
| **Precondition** | User is logged in                                                                                   |
| **Steps** | 1. From the Navbar Navigate to User Setting 2. Click Logout. 3. Confirm logout if prompted.         |
| **Expected Result** | User session is cleared, user is redirected to Login screen                                         |
| **Pass Criteria** | `SessionManager` has no logged-in user, Login screen is displayed                                   |

---
