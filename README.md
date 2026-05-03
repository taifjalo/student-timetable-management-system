# Student Timetable Management System

> **Course:** TX00EY27-3009 Ohjelmistotuotantoprojekti 1 & 2 — Metropolia University of Applied Sciences  
> **Team:** R6 | **Duration:** 8 Sprints × 2 Weeks (Spring 2026)

---

## 1. Project Title & Overview

The **Student Timetable Management System** is a desktop application built with Java 21 and JavaFX that enables students and teachers to manage academic schedules in a centralized and efficient way. The system addresses the lack of a unified, interactive timetable tool for higher education institutions by providing real-time scheduling, group management, and multilingual support.

**Target users:** Students and teachers at Metropolia University of Applied Sciences.

**Main technologies:** Java 21, JavaFX 21, Hibernate/JPA, MySQL on Microsoft Azure, Jenkins CI/CD, Docker, SonarQube, JUnit 5, Mockito.

The project was developed over 8 two-week sprints following Agile/Scrum methodology, covering the full software development lifecycle from planning and architecture to testing, quality assurance, and final documentation.

---

## 2. Product Vision

### Vision Statement
To provide a reliable, localized, and user-friendly desktop timetable management tool that simplifies schedule coordination between students and teachers across language barriers.

### Main Goals
- Enable teachers to create, edit, and manage lessons and courses
- Allow students to view their personalized timetable based on group membership
- Support multilingual UI (English, Arabic, Russian, Finnish) with RTL layout support
- Provide real-time messaging and notifications between users
- Ensure high code quality through automated CI/CD, testing, and static analysis

### Key Features
- **CalendarFX-powered timetable view** with interactive lesson entries
- **Role-based access** (Student / Teacher)
- **Group management** — assign students to groups, groups to lessons
- **Real-time chat** between users
- **Notification system** with multi-language translations stored in the database
- **UI Localization** — EN, AR (RTL), RU, FI loaded dynamically from DB
- **Docker containerization** for consistent deployment
- **Jenkins CI/CD pipeline** with JaCoCo coverage and SonarQube analysis

### Definition of Success
The project is complete when: all core features are functional, CI/CD pipeline runs end-to-end successfully, test coverage exceeds 80%, SonarQube Quality Gate passes, Docker image is deployed to Docker Hub, and full documentation is available in the `docs/` folder.

---

## Language Support

The application supports these UI languages:

| Language | Code | Direction |
|---|---:|---|
| English | `en_US` | LTR (default) |
| Finnish | `fi_FI` | LTR |
| Arabic | `ar_IQ` | RTL |
| Russian | `ru_RU` | LTR |

Translation bundles are stored in `src/main/resources/i18n/`:

- `MessagesBundle_en_US.properties`
- `MessagesBundle_fi_FI.properties`
- `MessagesBundle_ar_IQ.properties`
- `MessagesBundle_ru_RU.properties`

> Note: CalendarFX does not fully mirror in RTL mode, so the calendar view remains left-to-right while the rest of the UI switches correctly.

## Database Localization

Database localization is implemented with dedicated translation tables rather than duplicating full entities.

**Main tables and translation tables**

- `LANGUAGES`
- `COURSE_TRANSLATIONS`
- `GROUP_TRANSLATIONS`
- `NOTIFICATION_TRANSLATIONS`

Each translation row stores the entity reference, `language_code`, and localized fields such as names or content.
This design keeps the main tables language-independent and makes multilingual expansion easier.

## ER diagram

![file link](https://github.com/taifjalo/student-timetable-management-system/blob/main/docs/diagrams/Timetable%20ER-schema.png)
The ER diagram illustrates the main entities, attributes, and relationships required for the application's database.

## DB diagram

![file link](https://github.com/taifjalo/student-timetable-management-system/blob/main/docs/diagrams/Timetable%20database-schema.png)
The database schema obtained from the ER diagram.

## 3. Project Plan & Sprint Structure

**Methodology:** Agile / Scrum  
**Sprint length:** 2 weeks  
**Sprint reviews:** Conducted via Zoom with the lecturer as Product Owner  
**Project management:** [Trello Board](https://trello.com/w/sep1_studenttimetablemanagementsystem)  
**Repository:** [GitHub](https://github.com/taifjalo/student-timetable-management-system)

| Sprint | Goal |
|--------|------|
| Sprint 1 | Project planning, vision, Trello setup, backlog, risk analysis |
| Sprint 2 | Requirements, Use Case diagram, ER diagram, database design, unit testing strategy |
| Sprint 3 | UI implementation (JavaFX/CalendarFX), Jenkins CI pipeline, JaCoCo coverage |
| Sprint 4 | Docker containerization, Docker Hub deployment |
| Sprint 5 | UI localization (EN/AR/RU/FI), RTL support, Kubernetes setup |
| Sprint 6 | Database localization (localized translations in DB), SonarQube integration |
| Sprint 7 | Quality assurance — unit/integration/UAT testing, heuristic evaluation, JMeter |
| Sprint 8 | Final documentation, README update, presentation preparation |

---

## 4. Sprint 1 — Project Planning & Vision

The first sprint established the project foundation including team formation, technology selection, and backlog creation.

- **Project plan** and product vision defined
- **Trello** set up with epics, user stories, and sprint backlog
- **Risk analysis** conducted — identified DB connectivity and UI framework learning curve as main risks
- **Tech stack selected:** Java 21, JavaFX 21, Hibernate, MySQL, Jenkins, Docker
- **Scrum roles assigned:** Scrum Master (Sprint 1: Taif Jalo), Product Owner: Lecturer

📄 [Sprint 1 Review Report](docs/sprints_reports/sprint1_review.pdf)

---

## 5. Sprint 2 — Requirements & Database

System requirements were gathered and translated into a database schema and use case diagrams.

- **Functional requirements:** User authentication, lesson CRUD, group management, messaging, notifications
- **Use Case Diagram:** [`docs/diagrams/use_case.png`](docs/diagrams/use_case.png)
- **ER Diagram:** [`docs/diagrams/er_diagram.png`](docs/diagrams/er_diagram.png)
- **Database:** MySQL hosted on Microsoft Azure (`timetable-student.mysql.database.azure.com`)
- **ORM:** Hibernate 6.6.4 with JPA annotations
- **Key tables:** `users`, `student_profiles`, `student_groups`, `courses`, `lessons`, `assigned_groups`, `assigned_users`, `messages`, `notifications`, `notification_receivers`, `localization_strings`
- **Unit testing strategy:** JUnit 5 + Mockito for service/DAO layers

📄 [Sprint 2 Review Report](docs/sprints_reports/sprint2_review.pdf)

---

## 6. Sprint 3 — UI Implementation & CI Pipeline

The JavaFX user interface was implemented and the Jenkins CI pipeline was configured.

- **UI framework:** JavaFX 21 with FXML, CalendarFX 11.12.7, ControlsFX
- **Screens implemented:** Login, Register, Main (Timetable), Chat, Notifications, User Settings, Create/Edit modals
- **Code coverage:** JaCoCo 0.8.9 configured in `pom.xml`, excludes JavaFX controllers (untestable without TestFX)
- **Jenkins pipeline stages:**
  - `Checkout` — pulls from GitHub
  - `Build` — `mvn clean install -DskipTests`
  - `Test & Coverage` — `mvn test jacoco:report`
  - `Publish Test Results` — JUnit XML reports
  - `Publish Coverage Report` — JaCoCo plugin

📄 [Sprint 3 Review Report](docs/sprints_reports/sprint3_review.pdf)  
🔗 [Jenkinsfile](Jenkinsfile)

---

## 7. Sprint 4 — Docker Containerization

The application was containerized for consistent deployment across environments.

- **Purpose:** Ensure reproducible builds and deployments independent of local setup
- **Base image:** `maven:3.9.6-eclipse-temurin-21`
- **JavaFX runtime:** OpenJFX 21 SDK included in the container
- **X11 display:** Configured via `DISPLAY=host.docker.internal:0.0` (Xming on Windows)
- **Docker Hub:** [`taifjalo1/student-timetable-management-system`](https://hub.docker.com/r/taifjalo1/student-timetable-management-system)
- **Kubernetes:** Deployment YAML available in [`k8s/`](k8s/) directory

📄 [Sprint 4 Review Report](docs/sprints_reports/sprint4_review.pdf)  
🔗 [Dockerfile](Dockerfile)

---

## 8. Sprint 5 — UI Localization & Kubernetes

Multi-language support was implemented at the UI level with RTL layout capability.

- **Supported languages:** English (EN), Arabic (AR), Russian (RU), Finnish (FI)
- **Localization approach:** Strings loaded dynamically from the `localization_strings` database table via `LocalizationService` — no `.properties` files
- **RTL support:** Arabic UI switches to `NodeOrientation.RIGHT_TO_LEFT` using JavaFX layout API
- **Language switching:** Real-time without restart, cached after first DB load
- **Kubernetes:**
  - Deployment YAML: [`k8s/deployment.yaml`](k8s/deployment.yaml)
  - Image: pulled from Docker Hub
  - DISPLAY forwarded via `host.minikube.internal:0.0`

📄 [Sprint 5 Review Report](docs/sprints_reports/sprint5_review.pdf)

---

## 9. Sprint 6 — Database Localization & SonarQube

Database-level localization was implemented and static code analysis integrated.

- **DB localization:** `course_translations`, `group_translations`, and `notification_translations` tables store per-language content
- **Native SQL queries** with `COALESCE(translated_name, original_name)` for graceful fallback to English
- **Schema changes:** Added `language_code` FK columns and translation join tables
- **SonarQube:** Integrated with Jenkins via `mvn sonar:sonar`
  - Project key: `taifjalo_student-timetable-management-system`
  - Hosted on SonarCloud: [`sonarcloud.io/project/overview?id=taifjalo_student-timetable-management-system`](https://sonarcloud.io/project/overview?id=taifjalo_student-timetable-management-system)
- **Static analysis tools:** Checkstyle, SpotBugs, PMD — reports generated in `target/`

📄 [Sprint 6 Review Report](docs/sprints_reports/sprint6_review.pdf)

---

## 10. Sprint 7 — Quality Assurance & Testing

Comprehensive testing was conducted across functional and non-functional dimensions.

- **Unit tests:** 186 tests across DAO, Service, Entity, and DTO layers using JUnit 5 + Mockito
- **Integration tests:** Real Azure DB tests in DAO layer (CourseDaoTest, UserDaoTest, LessonDaoTest, etc.)
- **Code coverage:** Overall 66% (target 80%) — controllers excluded (JavaFX UI toolkit dependency)
- **SonarQube metrics:** Security: A, Maintainability: A, Reliability: A, Coverage: 82%
- **UAT testing:** 10 test cases executed per team member using template — [`docs/Acceptance_Testing.md`](docs/Acceptance_Testing.md)
- **Heuristic evaluation:** Nielsen's 10 heuristics applied individually — [`docs/Heuristic_evaluation.md`](docs/Heuristic_evaluation.md)
- **Performance testing (optional):** JMeter JDBC test plan targeting Azure MySQL — [`jmeter/student-timetable-management-system.jmx`](jmeter/fuel_calculator_test.jmx)

📄 [Sprint 7 Review Report](docs/sprints_reports/sprint7_review.pdf)

---

## 11. Sprint 8 — Documentation & Finalization

Final sprint focused on documentation completeness and project presentation.

- **Technical documentation:** Javadoc comments on all public classes and methods
- **User documentation:** This README covers installation, usage, and running instructions
- **System architecture diagram:** [`docs/diagrams/`](docs/diagrams/architecture.png)
- **Final presentation:** 25-minute team presentation covering all 8 sprints
- **Peer review:** Self and peer evaluation submitted individually on OMA

📄 [Sprint 8 Review Report](docs/sprints_reports/sprint8_review.pdf)

---
## Detailed Documentation Links

- [Class Diagram](docs/diagrams/Class%20Diagram.pdf)
- [Activity Diagram](docs/diagrams/Activitydiagram.jpg)
- [Use Case Diagram](docs/diagrams/Use%20case%20diagram.png)
- [ER Diagram](docs/diagrams/Timetable%20ER-schema.png)
- [Database Schema](docs/diagrams/Timetable%20database-schema.png)
- [CI Pipeline Image](docs/CI_Jenkins.png)
- [Static Analysis Report](docs/quality/analysis-report.html)

## Quality and Code Analysis

The repository includes quality checks for Checkstyle, SpotBugs, PMD, CPD, JaCoCo, and SonarQube.
The current static analysis summary is documented in [`docs/quality/analysis-report.html`](docs/quality/analysis-report.html).

| Tool | Baseline | After | Reduction |
|---|---:|---:|---:|
| Checkstyle | 2,773 | 0 | 100% |
| SpotBugs | 135 | 0 | 100% |
| PMD | 75 | 54 | 28% |
| CPD | 0 | 0 | — |
| **Total** | **2,983** | **54** | **98.2%** |

The remaining PMD findings are mostly related to JavaFX FXML event handlers that are invoked reflectively.
The coverage configuration excludes JavaFX UI controllers and other UI-only entry points from Sonar coverage calculations because they are difficult to unit-test in isolation.

## 12. How to Run the Project

### Prerequisites
- Java 21 (JDK)
- Maven 3.9+
- Docker Desktop
- Xming (Windows) or XQuartz (macOS) for JavaFX GUI in Docker
- MySQL access (or use Azure credentials from `persistence.xml`)

### Option A — Run Locally

```bash
# Clone the repository
git clone https://github.com/taifjalo/student-timetable-management-system.git
cd student-timetable-management-system

# Build and run
mvn clean install -DskipTests
mvn javafx:run
```

### Option B — Run via Docker

```bash
# Start Xming (Windows) with:
# Xming :0 -ac -multiwindow -clipboard

# Pull and run the Docker image
docker pull taifjalo1/student-timetable-management-system:latest

docker run --rm \
  -e DISPLAY=host.docker.internal:0.0 \
  -e JAVA_TOOL_OPTIONS="-Dprism.order=sw" \
  taifjalo1/student-timetable-management-system:latest
```

### Option C — Run via Kubernetes (Minikube)

```bash
minikube start
kubectl apply -f k8s/deployment.yaml
kubectl get pods
```

### Database Configuration

The application connects to Azure MySQL by default. To use a local DB, set environment variables:

```bash
export DB_URL=jdbc:mysql://localhost:3306/student_timetable
export DB_USER=root
export DB_PASSWORD=yourpassword
```

---

## 13. Testing Instructions

### Run Unit Tests

```bash
mvn test
```

### Run Tests with Coverage Report

```bash
mvn test jacoco:report
# Report available at: target/site/jacoco/index.html
```

### Run Full CI Pipeline (Jenkins)

Push to `main` branch — Jenkins automatically triggers:
1. Build
2. Test & Coverage
3. SonarQube Analysis
4. Docker Build & Push

### Run SonarQube Analysis Manually

```bash
mvn sonar:sonar \
  -Dsonar.projectKey=taifjalo_student-timetable-management-system \
  -Dsonar.organization=taif-jalo \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.token=YOUR_TOKEN
```

### Performance Testing (JMeter)

```bash
jmeter -n \
  -t jmeter/student_timetable_test.jmx \
  -l results/results.jtl \
  -e -o results/report
```

---

## 14. Repository Structure

```
student-timetable-management-system/
├── src/
│   ├── main/
│   │   ├── java/org/
│   │   │   ├── application/      # JavaFX entry point (CalendarApp)
│   │   │   ├── controllers/      # FXML controllers
│   │   │   ├── dao/              # Data Access Objects (Hibernate)
│   │   │   ├── datasource/       # TimetableConnection (EntityManagerFactory)
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── entities/         # JPA entity classes
│   │   │   └── service/          # Business logic services
│   │   └── resources/
│   │       ├── fxml/             # FXML layout files
│   │       ├── css/              # JavaFX stylesheets
│   │       └── i18n/             # Localization bundles
│   └── test/
│       └── java/org/             # JUnit 5 + Mockito test classes
├── docs/
│   ├── diagrams/                 # UML, ER, architecture diagrams
│   ├── sprints_reports/          # Sprint review reports (PDF)
│   └── testing/                  # UAT, Heuristic evaluation reports
├── k8s/                          # Kubernetes deployment YAML
├── jmeter/                       # JMeter test plans (.jmx)
├── Dockerfile                    # Docker build configuration
├── Jenkinsfile                   # CI/CD pipeline definition
├── pom.xml                       # Maven build configuration
├── checkstyle.xml                # Checkstyle rules
├── spotbugs-exclude.xml          # SpotBugs exclusion filter
└── README.md                     # This file
```

---

## 15. Authors

| Name | Role                                           | GitHub |
|------|------------------------------------------------|--------|
| **Taif Jalo** | Scrum Master (S1, S5), CI/CD, Testing, Backend | [@taifjalo](https://github.com/taifjalo) |
| **Nikita** | Scrum Master (S4, S7, S8), DB, Frontend, Chat  | [@Nikstuff201](https://github.com/Nikstuff201) |
| **Elias** | Scrum Master (S2, S6), UI/JavaFX, Controllers  | [@eliasnorta](https://github.com/eliasnorta) |
| **Miska** | Scrum Master (S3), Backend, Lesson system      | [@miska-voutilainen](https://github.com/miska-voutilainen) |

**Course:** TX00EY27-3009 Ohjelmistotuotantoprojekti 1 & 2
**Institution:** Metropolia University of Applied Sciences
**Semester:** Spring 2026

---

## Tech Stack Summary

| Category | Technology |
|----------|------------|
| Language | Java 21 |
| UI Framework | JavaFX 21, CalendarFX 11.12.7, ControlsFX |
| ORM | Hibernate 6.6.4 / JPA |
| Database | MySQL 8.0 on Microsoft Azure |
| Build | Maven 3.9 |
| CI/CD | Jenkins |
| Containerization | Docker |
| Orchestration | Kubernetes (Minikube) |
| Code Quality | SonarQube/SonarCloud, Checkstyle, SpotBugs, PMD |
| Testing | JUnit 5, Mockito, JaCoCo, JMeter |
| Project Management | Trello, GitHub |
