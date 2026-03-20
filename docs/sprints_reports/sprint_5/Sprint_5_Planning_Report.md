# Team 6 - Sprint 5 Planning Report

**Project:** Student Timetable Management System  
**Team Members:** Taif Jalo, Miska Voutilainen, Elias Norta, Nikita Rybakov

## Sprint Number & Dates

**Sprint Number:** Sprint 5  
**Sprint Duration:** 2 Weeks (17 Mar 2026 – 31 Mar 2026)

## Sprint Goal

The goal of Sprint 5 is to prepare the application for full multilingual support by localizing the user interface.
This includes externalizing all static text from the UI, implementing a language selection mechanism, and fully localizing the GUI to support at least two non-Latin languages — Arabic (with RTL layout support) and Russian — in addition to the default English.

## Selected Product Backlog Items

The following is a part of the product backlog. Full product backlog is accessible through [this link](https://trello.com/w/sep1_studenttimetablemanagementsystem/home).

* Externalize all static UI text into ResourceBundle `.properties` files
* Implement `LanguageManager` service for runtime language switching
* Add language selector (ComboBox) to the User Settings screen
* Implement full Arabic localization with RTL layout support
* Implement full Russian localization
* Update `MainAppController` to handle `NodeOrientation` switching (LTR/RTL)
* Update README with localization setup instructions
* Add new UML diagrams reflecting updated architecture to `docs/diagrams/`
* Update product backlog in Trello with localization user stories and acceptance criteria
* Write Sprint 5 Review Report
* Write Sprint 6 Planning Report

These items were chosen because the course focus for this sprint is UI localization and internationalization (i18n), and all selected items directly contribute to making the application multilingual.

## Planned Tasks / Breakdown

* Create `src/main/resources/messages/` directory and three `.properties` files:
    + `messages_en.properties` — English (default)
    + `messages_ar.properties` — Arabic
    + `messages_ru.properties` — Russian
* Externalize all text strings from the following controllers:
    + `LoginController`, `RegisterController`
    + `NavbarController`, `MainAppController`
    + `UserSettingsController`
    + `ChatController`, `MessageController`, `ChatPreviewController`
    + `NotificationsPopupController`
    + `CreateCourseModalController`, `CreateGroupModalController`
    + `EventExtraDetailsController`
* Create `LanguageManager.java` singleton service in `org.service`:
    + Handles locale switching between `en`, `ar`, `ru`
    + Exposes `get(key)` method to retrieve translated strings
    + Exposes `isRTL()` method for direction detection
* Add a `ComboBox<String>` language selector to `UserSettingsController`
* Implement `refreshUI()` method in `MainAppController` to reload UI text after language switch
* Apply `NodeOrientation.RIGHT_TO_LEFT` to root pane when Arabic is selected
* Update README.md with:
    + Language support section
    + Instructions for switching language in Settings
    + Description of the `ResourceBundle` localization framework used
* Add updated architecture diagram to `docs/diagrams/`
* Update Trello product backlog with localization user stories
* Write Sprint 5 Planning and Review reports

## Team Capacity & Assumptions

For Sprint 5, the team members agreed to:

* Work collaboratively on **Sundays and Mondays** and individually during the two-week sprint period
* Meetings are conducted via **Discord**
* Smaller meetings may occur when at least two members are available
* Working hours are recorded individually in a **shared Excel file**
* All implementation and planning activities are performed collaboratively during meetings and individual work sessions

**Localization approach:**  
The team decided to use Java's built-in `java.util.ResourceBundle` for i18n — no external library required. This approach is natively compatible with JavaFX and keeps the project dependency-free for localization purposes.

**Languages chosen:**
* English 🇬🇧 — Default (Latin)
* Arabic 🇸🇦 — Non-Latin, RTL
* Russian 🇷🇺 — Non-Latin, LTR

## Definition of Done

A Sprint 5 task is considered done when:

* The task objectives are completed and reviewed by the team
* All UI strings in the relevant screen are fully translated in all three `.properties` files
* The language switch works at runtime without requiring an app restart
* For Arabic: RTL layout is correctly applied across all screens
* The task is moved to the "Done" column in Trello
* Relevant code is committed to the GitHub repository

*Definition of done is specified in every Trello story.*