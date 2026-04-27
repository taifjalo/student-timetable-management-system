# Team 6 - Sprint 7 Planning Report

**Project:** Student Timetable Management System  
**Team Members:** Taif Jalo, Miska Voutilainen, Elias Norta, Nikita Rybakov

## Sprint Number & Dates

**Sprint Number:** Sprint 7  
**Sprint Duration:** 2 Weeks (14 Apr 2026 – 28 Apr 2026)

## Sprint Goal

The goal of Sprint 7 is to verify that the Student Timetable Management System is functional, usable, and aligned with the initial requirements defined by the Product Owner. Since previous sprints focused on UI localization, database localization, code cleanup, and static code analysis, Sprint 7 focuses mainly on testing both functional and non-functional aspects of the application.

This sprint includes final unit testing, user acceptance testing, heuristic evaluation, bug tracking, Jenkins build verification, SonarQube reporting, and documentation updates. The sprint also aims to identify and address usability, performance, and quality issues before the final documentation phase.

## Selected Product Backlog Items

The following is a part of the product backlog. Full product backlog is accessible through [this link](https://trello.com/w/sep1_studenttimetablemanagementsystem/home).

* Create a final test plan including objectives, resources, test environment, and test tasks
* Conduct final unit testing for implemented features
* Perform user acceptance testing based on Sprint 6 acceptance criteria
* Perform heuristic evaluation of the application
* Create a bug and issue tracking table for problems found during testing
* Review and refine Trello user stories related to the latest functionality
* Use Jenkins to verify successful project build
* Use Jenkins to generate or support the static code analysis report
* Review SonarQube quality results and ensure grades are A or at minimum B
* Document technical changes caused by testing results
* Update project documentation in GitHub and Trello
* Optionally perform performance testing, security testing, UI refinement, code optimization, and peer code review

These items were chosen because the course focus for Sprint 7 is testing the final product. The selected tasks help verify that the system works correctly, meets user expectations, and is ready for final documentation and review.

## Planned Tasks / Breakdown

* Test Planning
    + Create a test plan with clear objectives
    + Define resources, testing environment, and responsibilities
    + Define functional and non-functional test tasks
    + Prepare acceptance test cases based on Sprint 6 criteria

* Functional Testing
    + Conduct final unit testing for implemented features
    + Run integration or system-level checks where needed
    + Verify main user flows:
        + User registration
        + Login and failed login
        + Viewing timetable
        + Creating lessons
        + Creating courses
        + Creating groups
        + Sending messages
        + Receiving notifications
        + Changing password
    + Document test results and any bugs found

* Bug and Issue Tracking
    + Create a table for bugs and issues found during testing
    + Record issue description, severity, status, and suggested fix
    + Track which issues were fixed or left as known limitations
    + Use the findings to improve the final report

* Jenkins and Static Code Analysis
    + Run Jenkins build to confirm project builds successfully
    + Verify Docker deployment where applicable
    + Generate or review the SonarQube static code analysis report
    + Ensure SonarQube grades are A, or at minimum B
    + Document screenshots and key metrics from the analysis

* User Acceptance Testing
    + Execute selected User Acceptance Tests
    + Record results from all developers
    + Summarize passed and failed test cases
    + Confirm whether the application satisfies the planned acceptance criteria

* Heuristic Evaluation
    + Evaluate the application using Nielsen’s heuristic criteria
    + Identify usability issues related to:
        + Simple and natural dialog
        + User language
        + Memory load
        + Consistency
        + Feedback
        + Clearly marked exits
        + Shortcuts
        + Error messages
        + Error prevention
        + Help and documentation
    + Summarize findings, severity, and suggested improvements

* Documentation Updates
    + Update GitHub documentation to reflect Sprint 7 work
    + Update Trello user stories and task statuses
    + Document technical changes caused by testing
    + Add test reports, heuristic evaluation summary, and UAT results
    + Prepare material for the Sprint 7 review report

* Optional Improvements
    + Perform performance testing if time allows
    + Review basic security-related issues
    + Improve UI/UX based on heuristic evaluation findings
    + Optimize code where needed
    + Conduct peer code reviews for important changes

## Team Capacity & Assumptions

For Sprint 7, the team members agreed to:

* Work collaboratively during weekends and individually during the two-week sprint period
* Use Discord for team meetings and coordination
* Hold smaller meetings when at least two members are available
* Record working hours individually in the Sprint Review Report
* Use Trello to track sprint progress and task completion
* Use GitHub for code, documentation, and report updates
* Focus primarily on testing, reporting, and final quality verification rather than adding major new features

**Testing approach:**  
The team will test both functional and non-functional aspects of the system. Functional testing will verify that the implemented features work as expected. Non-functional testing will focus on usability, acceptance, documentation, code quality, and optional performance/security checks.

**Code quality approach:**  
The team will use Jenkins and SonarQube to verify build success and static code quality. The SonarQube report should show all grades as A, or at minimum B.

## Definition of Done

A Sprint 7 task is considered done when:

* The task objectives are completed and reviewed by the team
* A final test plan is created with objectives, resources, environment, and test tasks
* Final unit tests are executed and results are documented
* User Acceptance Tests are executed and summarized
* Heuristic evaluation is completed and summarized
* Bugs and issues found during testing are recorded in a tracking table
* Trello user stories related to the latest functionality are reviewed and updated
* Jenkins build is successful
* SonarQube static code analysis report is generated or reviewed
* SonarQube grades are A or at minimum B
* Technical changes from testing are documented
* Relevant GitHub and Trello documentation is updated
* The task is moved to the “Done” column in Trello
* Relevant code, reports, screenshots, and documents are committed to the GitHub repository

*Definition of done is specified in every Trello story.*