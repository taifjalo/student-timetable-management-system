# Student Timetable Management System

**Overview:** This course project implements a **student timetable management system** using an [Agile/Scrum](https://www.atlassian.com/agile/project-management#:~:text=match%20at%20L1634%20,collaboration%2C%20flexibility%2C%20and%20customer%20feedback) process. The team followed an iterative, incremental development approach focused on frequent feedback. The system is written entirely in Java with a MySQL/Azure backend. It provides a simple GUI (JavaFX) for managing class and subject schedules. According to the project description, the database schema consists of five tables (three subject tables and two class tables) and the app runs as a menu-driven program on Java/MySQL.

## Technology Stack

The project uses the following key technologies:

- **Java:** Core language for both front-end and back-end. [`Java`](https://github.com/topics/java) is widely used for desktop and enterprise applications.
- **JavaFX:** Framework for building the graphical user interface (desktop GUI) on top of Java.
- **Maven:** Build tool and dependency manager for compiling the code, running tests, and packaging the application (using a standard [`pom.xml`](https://github.com/taifjalo/student-timetable-management-system/blob/main/pom.xml) structure).
- **Database:** A relational database (Azure/MySQL) stores the timetable data. [Ten tables (userss, classes, subjects, etc.)](https://github.com/taifjalo/student-timetable-management-system/blob/main/docs/DB_script) hold the application data. The schema is defined in the repository’s **`TABLES USED IN THE DATABASE.png`** ![file link](https://github.com/taifjalo/student-timetable-management-system/blob/main/docs/diagrams/Timetable%20database-schema.png).
- **Testing:** JUnit is used for writing unit tests. We measure code coverage using JaCoCo, a free Java code [coverage library](https://www.eclemma.org/jacoco/#:~:text=JaCoCo%20is%20a%20free%20code,existing%20libraries%20for%20many%20years). [JaCoCo](https://www.jenkins.io/doc/pipeline/steps/jacoco/#:~:text=) integrates with Maven and Jenkins to generate reports.
- **Continuous Integration / Delivery:** [Jenkins](https://www.jenkins.io/#:~:text=As%20an%20extensible%20automation%20server%2C,delivery%20hub%20for%20any%20project) is used as the CI/CD server. It automatically builds the project on each commit, runs the JUnit tests, and invokes JaCoCo to record coverage metrics. (The Jenkins JaCoCo plugin enforces coverage thresholds on the build【26†L48-L54】.)
- **Containerization:** [Docker](https://docs.docker.com/reference/cli/docker/image/push/#:~:text=Use%20,hosted%20one) is used to package the application for deployment. A `Dockerfile` is provided to build a runnable image of the Java app. We push the image to Docker Hub so it can be shared and deployed easily.
- **Project Management:** [Trello](https://trello.com/w/sep1_studenttimetablemanagementsystem/home) was used to manage the [product backlog](https://www.atlassian.com/agile/scrum/backlogs#:~:text=A%20product%20backlog%20is%20a,knows%20what%20to%20deliver%20first) and sprint boards, following Scrum guidelines. [User stories](https://app.uxcel.com/glossary/user-stories) were written in the format “As a [user], I want [functionality] so that [benefit] and prioritized based on business value. All design artifacts (UI mockups, database diagrams) and sprint reports are stored in the `Documents/` folder of the repository.

## Installation & Setup

To set up the project locally:

1. **Prerequisites:** Install JDK 11+ (or compatible Java), Maven, and a Azure/MySQL server on your machine. Also install Docker if you plan to run the containerized image.
2. **Clone the repository:**
   ```bash
   git clone https://github.com/taifjalo/student-timetable-management-system.git
   cd student-timetable-management-system
   ```
3. **Create the database:** Use the provided `TABLES USED IN THE DATABASE.png` (in the repo) to create the required tables in your database. Populate them with any sample data as needed. Update the database URL, username, and password in the source code or configuration files if your setup differs.
4. **Build the project:** Compile and package the application using Maven:
   ```bash
   mvn clean package
   ```
   This will compile the Java code, run unit tests, and produce a JAR in `target/`.
5. **Run the application:** Launch the application (for example, by executing the JAR file). In a terminal:
   ```bash
   java -jar target/student-timetable-1.0.jar
   ```
   (Replace `student-timetable-1.0.jar` with the actual JAR name produced.) The app will start and display a menu-driven interface for managing timetables. Follow the on-screen options to create, update, and view schedules.

## Continuous Integration & Testing

[Jenkins](https://www.jenkins.io/#:~:text=As%20an%20extensible%20automation%20server%2C,delivery%20hub%20for%20any%20project) is configured to automate the build and test workflow. On each commit to `main`, Jenkins pulls the latest code and runs a pipeline that:

- **Builds** the project with Maven (`mvn compile`),
- **Executes** the unit tests (`mvn test` using JUnit),
- **Generates** a code coverage report with JaCoCo.

We use the Jenkins [JaCoCo plugin](https://www.jenkins.io/doc/pipeline/steps/jacoco/#:~:text=) to enforce coverage thresholds so that the build fails if code coverage drops below the set minimum. The generated HTML coverage report is published (for example, on a web server or Jenkins dashboard). Integrating [JaCoCo](https://www.eclemma.org/jacoco/#:~:text=JaCoCo%20is%20a%20free%20code,existing%20libraries%20for%20many%20years) ensures that at least the critical parts of the application are covered by tests. The Jenkins pipeline also stores build logs and test reports in the project repository (e.g. under `target/` and `reports/`).

## Docker Container and Deployment

[A `Dockerfile`](https://docs.docker.com/reference/cli/docker/image/push/#:~:text=Use%20,hosted%20one) is provided to containerize the application. To build the Docker image, use:

```bash
docker build -t taifjalo/timetable-management:1.0 .
```

This creates an image (tagged `taifjalo/timetable-management:1.0`) containing the Java application. To test the image locally, run:

```bash
docker run --rm -p 8080:8080 taifjalo/timetable-management:1.0
```

(This example exposes port 8080; adjust the port if the app uses a different one.) Once the container is running, you can interact with the application via its interface (e.g. through the console or a web UI, depending on how the app is designed).

When satisfied, push the image to Docker Hub so it can be accessed by others:

```bash
docker push taifjalo/timetable-management:1.0
```

This uses [`docker image push`](https://docs.docker.com/reference/cli/docker/image/push/#:~:text=Use%20,hosted%20one) to share the image in a public registry. Finally, the image was deployed on a test environment (such as Docker Playground or a cloud VM) to verify that it runs correctly outside the local machine.

## Project Management & Documentation

The team followed Scrum with two-week sprints. All features and tasks were tracked in a product backlog ([Trello](https://trello.com/w/sep1_studenttimetablemanagementsystem/home)) and broken down into user stories【19†L65-L68】【21†L1655-L1660】. A one-page **Product Vision** document and a detailed **Project Plan** were created in Sprint 1 to define goals, timelines, and milestones. Those documents, along with Figma UI mockups, database ER diagrams, and sprint reports, are available in the `Documents/` directory of this repository.

## Contribution

This repository contains the complete project artifacts. All team members contributed code (see commit history) and documentation. If you wish to contribute improvements or fixes, please fork the repository and submit a pull request. Issues are tracked via GitHub Issues. Before contributing, ensure that your changes build successfully (`mvn test`) and respect the project’s coding standards.

---

**Sources:** Project requirements and course materials were used to define the scope and deliverables. The technical choices are supported by standard practices: [Java](https://github.com/topics/java) for applications, [Jenkins](https://www.jenkins.io/#:~:text=As%20an%20extensible%20automation%20server%2C,delivery%20hub%20for%20any%20project) for CI/CD, [JaCoCo](https://www.eclemma.org/jacoco/#:~:text=JaCoCo%20is%20a%20free%20code,existing%20libraries%20for%20many%20years) for coverage, and [Docker](https://docs.docker.com/reference/cli/docker/image/push/#:~:text=Use%20,hosted%20one) for containerization. The concepts of [user stories](https://app.uxcel.com/glossary/user-stories#:~:text=A%20user%20story%20is%20a,rather%20than%20just%20technical%20implementation) and product backlogs follow [Agile guidelines](https://www.atlassian.com/agile/scrum/backlogs#:~:text=A%20product%20backlog%20is%20a,knows%20what%20to%20deliver%20first). All code and documentation are maintained in this GitHub repository.
