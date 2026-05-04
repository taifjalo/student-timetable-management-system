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
| View Timetable          | Functional | TC-04, TC-18 | Yes |
| Create Lesson (Teacher) | Functional | TC-05        | Yes |
| Create Course           | Functional | TC-06        | Yes |
| Create Group            | Functional | TC-07        | Yes    |
| Send Message            | Functional | TC-08        | Yes |
| Receive Notification    | Functional | TC-09        | Yes |
| Change Password         | Functional | TC-10        | Yes |
| Select Language         | Functional | TC-11        | Yes |
| RTL Layout (Arabic)     | Functional | TC-12        | Yes |
| Logout                  | Functional | TC-13        | Yes |
| Edit/Delete Lesson      | Functional | TC-14        | Yes |
| Search User & Start Conversation | Functional | TC-15        | Yes |
| Mark Notifications Read | Functional | TC-16        | Yes |
| Update Profile Information | Functional | TC-17        | Yes |
| View Timetable in Year View | Functional | TC-18        | Yes |
| UI Readability          | Usability | TC-19        | Yes |
| Language Switch Speed   | Performance | TC-20        | Yes |
| Login Response Time     | Performance | TC-21        | Yes |
| Data Refresh Time       | Performance | TC-22        | Yes |


---

## 4. User Acceptance tests

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

### TC-14 — Edit or Delete Lesson
| Field | Detail |
|---|---|
| **User Story** | As a teacher, I want to edit or delete an existing lesson |
| **Precondition** | User is logged in as teacher or admin, and at least one lesson already exists |
| **Steps** | 1. Open the calendar. 2. Select an existing lesson. 3. Change one lesson detail and save. 4. Reopen the lesson and delete it. |
| **Expected Result** | The edited lesson updates in the calendar, and the deleted lesson no longer appears |
| **Pass Criteria** | Lesson changes persist correctly in the database, and deletion removes the lesson from the timetable view |

### TC-15 — Search User and Start Conversation
| Field | Detail |
|---|---|
| **User Story** | As a user, I want to search for another user and start a conversation |
| **Precondition** | User is logged in, and at least one other user exists in the system |
| **Steps** | 1. Open Messages. 2. Search by first name or last name. 3. Select a matching user. 4. Start a conversation and send a message. |
| **Expected Result** | The matching user is found, a conversation opens, and the message is visible in the thread |
| **Pass Criteria** | The conversation is created successfully and the message is stored with the correct sender and recipient |

### TC-16 — Mark Notifications as Read
| Field | Detail |
|---|---|
| **User Story** | As a user, I want to mark notifications as read individually or all at once |
| **Precondition** | User is logged in and has at least one unread notification |
| **Steps** | 1. Open the notifications pane. 2. Mark one notification as read. 3. Mark all notifications as read. |
| **Expected Result** | The unread indicator updates correctly and the selected notifications change to read state |
| **Pass Criteria** | Notification read status is updated correctly for both single-item and bulk actions |

### TC-17 — Update User Profile Information
| Field | Detail |
|---|---|
| **User Story** | As a user, I want to update my profile information such as first name and email |
| **Precondition** | User is logged in and on the Settings page |
| **Steps** | 1. Open Settings. 2. Change the first name. 3. Change the email address. 4. Save the changes. |
| **Expected Result** | The updated profile information is saved and displayed correctly after refresh or reopen |
| **Pass Criteria** | The first name and email are updated in the `users` table and reflected in the UI |

### TC-18 — View Timetable in Year View
| Field | Detail |
|---|---|
| **User Story** | As a student, I want to view my timetable in year view |
| **Precondition** | User is logged in, lessons are assigned to their group |
| **Steps** | 1. Log in. 2. Open the calendar. 3. Switch to year view. 4. Review the displayed timetable. |
| **Expected Result** | The timetable is displayed in year view with all assigned lessons visible in the annual overview |
| **Pass Criteria** | CalendarFX displays year-view events matching the database records, with correct course colour and lesson details |

### User acceptance tests results

A subset of 10 test cases was executed by all developers, and the results are shown above. The tests were successfully completed by all developers, and no issues were found.

| Test Case                  | Miska Voutilainen | Taif Jalo | Nikita Rybakov | Elias Norta |
|----------------------------|-------------------|-----------|----------------|-------------|
| TC-01 User Registration    | Pass              | Pass      | Pass           | Pass        |
| TC-02 Successful Login     | Pass              | Pass      | Pass           | Pass        |
| TC-03 Failed Login         | Pass              | Pass      | Pass           | Pass        |
| TC-04 View Timetable       | Pass              | Pass      | Pass           | Pass        |
| TC-05 Create Lesson        | Pass              | Pass      | Pass           | Pass        |
| TC-06 Create Course        | Pass              | Pass      | Pass           | Pass        |
| TC-07 Create Group         | Pass              | Pass      | Pass           | Pass        |
| TC-08 Send Message         | Pass              | Pass      | Pass           | Pass        |
| TC-09 Receive Notification | Pass              | Pass      | Pass           | Pass        |
| TC-10 Change Password      | Pass              | Pass      | Pass           | Pass        |


---

## 5. Usability Test Cases

### TC-19 — UI Readability
| Field | Detail                                                                 |
|---|------------------------------------------------------------------------|
| **Test Objective** | Verify the UI is readable and intuitive for a new user                 |
| **Method** | Task-based observation with a user unfamiliar with the system          |
| **Tasks** | 1. Log in. 2. Find tomorrow's lessons. 3. Send a message to a teacher. |
| **Pass Criteria** | User completes all 3 tasks without assistance within 4 minutes         |

---

## 6. Performance Test Cases

### TC-20 — Language Switch Speed
| Field | Detail |
|---|---|
| **Test Objective** | Language switch should feel instant |
| **Method** | Measure time from menu selection to UI update |
| **Pass Criteria** | UI updates within 500ms of language selection |

### TC-21 — Login Response Time
| Field | Detail |
|---|---|
| **Test Objective** | Login should complete quickly |
| **Method** | Measure time from button click to calendar view appearing |
| **Pass Criteria** | Login completes within 3 seconds on a standard network connection |

### TC-22 — Data refresh time

| Field | Detail                                                          |
|---|-----------------------------------------------------------------|
| **Test Objective** | All data should reload quickly                                  |
| **Method** | Measure time from button click to data being refreshed          |
| **Pass Criteria** | Data refreshes after 5 seconds on a standard network connection |

---

## 7. Out of Scope

* Load testing / stress testing
* Security penetration testing
* Mobile device testing (application is desktop-only)
  * Automated test execution (manual test design only for this sprint)