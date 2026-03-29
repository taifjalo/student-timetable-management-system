# Student Timetable Management System

**Overview:** This course project implements a **student timetable management system** using an Agile/Scrum process. The goal of the project is to create a convenient alternative application for scheduling students' lessons and courses with the most essential features, namely allowing students to view their schedule, allowing teachers to add courses and lessons, providing a chat between students and teachers, as well as a notification system.

## Technology Stack

The project uses the following key technologies:

- **Java:** Core language for both front-end and back-end. [`Java`](https://github.com/topics/java) is widely used for desktop and enterprise applications.
- **JavaFX:** Framework for building the graphical user interface (desktop GUI) on top of Java.
- **Maven:** Build tool and dependency manager for compiling the code, running tests, and packaging the application (using a standard [`pom.xml`](https://github.com/taifjalo/student-timetable-management-system/blob/main/pom.xml) structure).
- **Testing:** JUnit is used for writing unit tests. We measure code coverage using JaCoCo, a free Java code [coverage library](https://www.eclemma.org/jacoco/#:~:text=JaCoCo%20is%20a%20free%20code,existing%20libraries%20for%20many%20years). [JaCoCo](https://www.jenkins.io/doc/pipeline/steps/jacoco/#:~:text=) integrates with Maven and Jenkins to generate reports.
- **Continuous Integration / Delivery:** [Jenkins](https://www.jenkins.io/#:~:text=As%20an%20extensible%20automation%20server%2C,delivery%20hub%20for%20any%20project) is used as the CI/CD server. It automatically builds the project on each commit, runs the JUnit tests, and invokes JaCoCo to record coverage metrics. (The Jenkins JaCoCo plugin enforces coverage thresholds on the build.
  ![file link](https://github.com/taifjalo/student-timetable-management-system/blob/main/docs/CI_Jenkins.png)
- **Containerization:** [Docker](https://docs.docker.com/reference/cli/docker/image/push/#:~:text=Use%20,hosted%20one) is used to package the application for deployment. A `Dockerfile` is provided to build a runnable image of the Java app. We push the image to Docker Hub so it can be shared and deployed easily.
- **Localization:** Java's built-in `java.util.ResourceBundle` is used for UI internationalization (i18n). Translation strings are stored in `.properties` files under `src/main/resources/i18n/MessagesBundle/`. The `LanguageManager` service handles runtime language switching and RTL layout detection for Arabic.
- **Project Management:** [Trello](https://trello.com/w/sep1_studenttimetablemanagementsystem/home) was used to manage the [product backlog](https://www.atlassian.com/agile/scrum/backlogs#:~:text=A%20product%20backlog%20is%20a,knows%20what%20to%20deliver%20first) and sprint boards, following Scrum guidelines. [User stories](https://app.uxcel.com/glossary/user-stories) were written in the format "As a [user], I want [functionality] so that [benefit] and prioritized based on business value. All design artifacts (UI mockups, database diagrams) and sprint reports are stored in the `Documents/` folder of the repository.

## Language Support

The application supports the following languages, selectable from the Settings screen:

| Language          | Code | Direction |
|-------------------|------|-----------|
| English           | `en` | LTR (default) |
| Finnish           | `fi` | LTR |
| Arabic — العربية  | `ar` | RTL |
| Russian — Русский | `ru` | LTR |

**How to switch language:**
1. Navigate to **Log in** page
2. Select your preferred language from the **Language** dropdown
3. The UI updates instantly across all screens
4. Selecting Arabic automatically switches the layout to **Right-to-Left**

Translation files are located at `src/main/resources/MessagesBundle/`:
- `MessagesBundle_en_US.properties`
- `MessagesBundle_fi_FI.properties`
- `MessagesBundle_ar_IQ.properties`
- `MessagesBundle_ru_RU.properties`


## ER diagram
![file link](https://github.com/taifjalo/student-timetable-management-system/blob/main/docs/diagrams/Timetable%20ER-schema.png)
The ER diagram illustrates the main entities, attributes, and relationships required for the application's database.

## DB diagram
![file link](https://github.com/taifjalo/student-timetable-management-system/blob/main/docs/diagrams/Timetable%20database-schema.png)
The database schema obtained from the ER diagram.

## Use Case diagram
![file link](https://github.com/taifjalo/student-timetable-management-system/blob/main/docs/diagrams/Use%20case%20diagram.png)
The use case diagram shows how users interact with the system and what actions they can perform.

## Sprint Reports
[Sprint reports folder](https://github.com/taifjalo/student-timetable-management-system/tree/main/docs/sprints_reports)