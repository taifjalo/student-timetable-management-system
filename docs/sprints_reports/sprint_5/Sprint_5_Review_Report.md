# Team 6 - Sprint 5 Review Report

## Sprint Goal

The goal of Sprint 5 was to prepare the application for full multilingual support by localizing the user interface.
This includes externalizing all static text from the UI, implementing a language selection mechanism, and fully localizing the GUI to support Arabic (with RTL layout support), Finnish and Russian, in addition to the default English.

## Completed User Stories / Tasks

The full Product Backlog and Sprint setup are available in the Trello workspace:  
[Trello Workspace – Student Timetable Management System](https://trello.com/w/sep1_studenttimetablemanagementsystem/home)

* All static UI text was externalized from controllers into `resource/i18n` folder with three `ResourceBundle` `.properties` files:
    + `MessagesBundle_en_US.properties` — English (default)
    + `MessagesBundle_fi_FI.properties` — Finnish
    + `MessagesBundle_ar_IQ.properties` — Arabic
    + `MessagesBundle_ru_RU.properties` — Russian
* `LocalizationService.java` service was created in `org.service`, handling locale switching, `ResourceBundle` loading, and RTL detection via `isRTL()`.
* A language selector `ComboBox` was added to `LoginController` and TODO `UserSettingsController`, allowing users to switch between English, Suomi, العربية, and Русский at runtime.
* RTL layout support was implemented using `NodeOrientation.RIGHT_TO_LEFT` on the root pane in `MainAppController`. All child components inherit the direction automatically.
* A `refreshUI()` method was implemented in `LoginController` to reload all UI text after a language switch without restarting the application.
* README.md was updated with a language support section and instructions for switching languages.
* Product backlog was updated in Trello with localization user stories and acceptance criteria.
* Sprint 5 Planning Report was written and uploaded to the GitHub repository.

## Demo Summary (What Was Shown in the Review)

During the Sprint 5 review, the team demonstrated the fully localized application running in all three supported languages. The demo showed the language selector in the Settings screen, switching between English, Arabic, and Russian, and the UI updating instantly in all screens — login, timetable, chat, notifications, and settings.

The Arabic RTL layout was demonstrated, showing how the entire application layout mirrors correctly when Arabic is selected. The team also walked through the `LocalizationService` service and the `.properties` file structure to show how the localization framework is implemented.

## Team Member Contributions

| Team Member Name | Assigned Tasks                                                                                                                                                                                | Time Spent (hrs) | In-class Tasks |
|---|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------|----------------|
| Taif Jalo | LocalizationService service, MessagesBundle_en/ar/ru.properties — Part of all string translations, Sprint Planning, Sprint reports, UserSettingsController update, Login and Register update. | 30               | Submitted      |
| Miska Voutilainen |                                                                                                                                                                                               | ?                | ?              |
| Elias Norta | Add LocalizationService, MessagesBundle_en_US.properties, externalizing strings, Language selector UI (login/user settings), refreshUI()                                                      | 25               | Submitted      |
| Nikita Rybakov | RTL support, NodeOrientation, layout adjustments across all screens. Chat, FX Calendar, Create course modal, Event window, Notification localization                                          | 23               | Submitted      |

## What Went Well

* Java's built-in `ResourceBundle` was straightforward to integrate with no additional dependencies
* RTL support via `NodeOrientation` worked cleanly across all JavaFX components
* The `LocalizationService` made it easy to access translations from any controller
* Good team collaboration and clear task distribution
* All planned tasks were completed within the sprint

## What Could Be Improved

* CalendarFX has limited native RTL support — the calendar view itself does not fully mirror in Arabic mode; only the surrounding layout does
* Initial setup of `.properties` files for Arabic required careful UTF-8 encoding configuration in the IDE

## Next Sprint Focus (Sprint 6 — Database Localization)

* Extend localization to the database layer — store multilingual content (course names, lesson descriptions, notifications) per user locale
* Update the DB schema to support multilingual data (add locale columns or separate translation tables)
* Update DAO and Service layers to fetch locale-specific content from the database
* Bind database-driven localized content to the timetable and calendar view
* Write unit tests for the updated DAO/Service localization logic
* Update Trello backlog and write Sprint 6 Planning and Review Reports
