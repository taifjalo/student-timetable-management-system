# Team 6 - Sprint 7 Review Report

## Sprint Goal

The goal of Sprint 7 was to verify that the Student Timetable Management System is functional, usable, and aligned with the initial requirements defined by the Product Owner.

This sprint focused on functional and non-functional testing of the final product. The team performed Jenkins build verification, SonarQube code quality analysis, User Acceptance Testing, Heuristic Evaluation, bug tracking, and documentation updates.

## Completed Tasks

The full Product Backlog and Sprint setup are available in the Trello workspace:  
[Trello Workspace – Student Timetable Management System](https://trello.com/w/sep1_studenttimetablemanagementsystem/home)

* Jenkins pipeline was executed successfully.
  + The project was successfully built through Jenkins.
  + Test results were published.
  + Code coverage report was generated.
  + SonarCloud/SonarQube analysis was executed through Jenkins.
  + Docker image was built successfully.
  + Docker image was pushed to Docker Hub.

* User Acceptance Testing was completed.
  + 17 User Acceptance Test cases were first created based on the acceptance criteria defined in Sprint 6.
  + A subset of 10 User Acceptance Test cases was executed as required by the assignment.
  + All selected test cases were performed by all developers.
  + The tested functionality included registration, login, failed login, timetable viewing, lesson creation, course creation, group creation, messaging, notification receiving, and password changing.
  + All executed User Acceptance Tests passed successfully.

* Heuristic Evaluation was completed.
  + Each developer performed an individual heuristic evaluation of the application.
  + The evaluations were based on Nielsen’s heuristic criteria.
  + A combined Heuristic Evaluation Summary Report was created.
  + The summary report includes the most relevant findings, severity levels, and suggested improvements.

* Bug and issue tracking was completed.
  + Issues found during testing were documented.
  + Two practical issues were fixed during the sprint.
  + The bug tracking table is included below.

* Documentation was updated.
  + Sprint 7 Planning Report was written and uploaded.
  + Sprint 7 Review Report was written.
  + User Acceptance Test results were documented.
  + Heuristic Evaluation Summary Report was documented.
  + Jenkins and SonarQube results were added to the project documentation.

## Demo Summary (What Was Shown in the Review)

During the Sprint 7 review, the team demonstrated the final testing status of the application.

The team showed the successful Jenkins pipeline execution, including build, testing, coverage publishing, SonarCloud/SonarQube analysis, Docker image build, and Docker Hub push.

The team also showed the SonarQube dashboard, where the Quality Gate passed successfully. The final analysis showed A grades for Security, Reliability, and Maintainability. Code coverage reached 82.1%, which satisfies the required 80% coverage target.

In addition, the team presented the User Acceptance Test results and the Heuristic Evaluation Summary Report created from evaluations completed by all developers.

## Jenkins and SonarQube Results

The Jenkins pipeline was completed successfully. The pipeline included build, test result publishing, code coverage report generation, SonarCloud/SonarQube analysis, Docker image build, and Docker Hub push.

SonarQube results:

| Metric | Result |
|---|---:|
| Quality Gate | Passed |
| Security | A |
| Reliability | A |
| Maintainability | A |
| Security Issues | 0 |
| Reliability Issues | 0 |
| Maintainability Issues | 1 |
| Accepted Issues | 0 |
| Security Hotspots | 0 |
| Coverage | 82.1% |
| Duplications | 3.3% |

The required coverage target of 80% was reached, with final coverage at **82.1%**.

## User Acceptance Testing Summary

17 User Acceptance Test cases were created based on the acceptance criteria defined in Sprint 6.  
A subset of 10 User Acceptance Test cases was executed by all developers as required by the assignment.

| Test Case | Result |
|---|---|
| TC-01 User Registration | Passed |
| TC-02 Successful Login | Passed |
| TC-03 Failed Login | Passed |
| TC-04 View Timetable | Passed |
| TC-05 Create Lesson | Passed |
| TC-06 Create Course | Passed |
| TC-07 Create Group | Passed |
| TC-08 Send Message | Passed |
| TC-09 Receive Notification | Passed |
| TC-10 Change Password | Passed |

All selected User Acceptance Tests passed successfully.

## Heuristic Evaluation Summary

Each developer performed a heuristic evaluation of the application using Nielsen’s heuristic criteria.

The findings were combined into a summary report. The summary focused on recurring usability issues, severity levels, and suggested improvements.

The most important findings were related to:

* Error messages and validation
* Error prevention
* Missing shortcuts
* Help and documentation
* Consistency in dialogs and buttons
* Feedback after user actions
* Memory load caused by repeated or unclear information

No usability catastrophe issues were identified.

## Bug and Issue Tracking

| Issue | Description | Status | Fix / Resolution |
|---|---|---|---|
| UI layout breaks when switching the application language to Arabic from settings | When the application language was changed to Arabic, the UI layout did not update correctly into mirrored right-to-left orientation. | Fixed | Added a delay to allow the UI to refresh correctly when switching to mirrored RTL orientation. |
| Slow login when the application contains many courses | Login became slow when the system had a large number of courses to load. | Fixed | Added a course loading animation to improve user feedback during login and course loading. |

## Specification and Architecture Changes from Test Results

No specification or architecture changes were applied as a result of Sprint 7 testing.

All selected functional tests passed successfully, and no test result required changes to the system architecture or technical specifications. The issues found during testing were handled as implementation-level fixes and UI improvements.

## Team Member Contributions

| Team Member Name | Assigned Tasks                                                                                                            | Time Spent (hrs) | In-class Tasks |
|---|---------------------------------------------------------------------------------------------------------------------------|-----------------:|----------------|
| Taif Jalo |                                                                                                                           |                  |                |
| Miska Voutilainen | User acceptance testing, SonarQube issue fixes (reduce complexity, add loggers, virtual threads, record patterns, fix tests, remove commented code, add generics, static setter, constants, boolean expressions), resolve merge conflicts |               25 | done           |
| Elias Norta |                                                                                                                           |                  |                |
| Nikita Rybakov | Sprint 7 scrum master, sprint review, sprint report, documentation, heuristic evaluation summary, user acсeptance testing |               20 | done           |

## What Went Well

* Jenkins pipeline was executed successfully.
* SonarQube Quality Gate passed.
* Code coverage reached 82.1%, meeting the required 80% target.
* Security, Reliability, and Maintainability received A grades.
* User Acceptance Tests were executed successfully by all developers.
* Heuristic evaluations were completed by each developer.
* A combined Heuristic Evaluation Summary Report was created.
* Identified bugs were tracked and fixed.
* Documentation was updated for Sprint 7.

## What Could Be Improved

* More User Acceptance Tests could be executed in the future to increase test coverage of less common features.
* More performance testing could be performed with larger datasets.
* Help and onboarding documentation could be improved further for first-time users.
* Keyboard shortcuts could be expanded to improve efficiency for frequent users.
* UI consistency could be improved further across all dialogs and forms.

## Final Sprint Status

Sprint 7 was completed successfully.

The application passed the selected User Acceptance Tests, Jenkins build completed successfully, SonarQube Quality Gate passed, and code coverage reached the required target. The main testing-related issues were documented and fixed, and the project is ready for final documentation and presentation.