# Team 6 - Sprint 6 Planning Report

**Project:** Student Timetable Management System  
**Team Members:** Taif Jalo, Miska Voutilainen, Elias Norta, Nikita Rybakov

## Sprint Number & Dates

**Sprint Number:** Sprint 6
**Sprint Duration:** 2 Weeks (31 Mar 2026 – 14 Apr 2026)

## Sprint Goal

The goal of Sprint 6 is to extend localization support into the database layer and to improve overall code quality and maintainability. This sprint also focuses on preparing the project for acceptance by refactoring, planning acceptance tests, and by performing systematic code review via SonarQube.

## Selected Product Backlog Items

The following is a part of the product backlog. Full product backlog is accessible through [this link](https://trello.com/w/sep1_studenttimetablemanagementsystem/home).

* Design and implement a database localization strategy
* Prepare the database schema and ERD for multilingual support
* Ensure proper character encoding (UTF-8) and locale configuration
* Validate data retrieval and display across supported languages
* Run static code analysis using SonarQube and SonarScanner
* Analyze and document code metrics (cyclomatic complexity, duplicate code, LOC)
* Refactor and simplify complex methods and remove redundant code
* Enforce Java coding conventions and add inline documentation
* Re-run unit tests to confirm stability after refactoring
* Define acceptance criteria and design acceptance test cases
* Create or update ER diagrams and UML models in /docs
* Write Sprint 7 Planning Report

These items were chosen because the course focus for Sprint 6 is database localization, code quality, and acceptance test planning. All selected items directly contribute to a production-ready, well-documented, and maintainable codebase.

## Planned Tasks / Breakdown

* Database Localization
  + Update database schema to support multilingual fields
  + Configure UTF-8 character encoding across all relevant tables
  + Document the chosen localization method in the GitHub README
* Static Code Review
  + Run static analysis tools:
  + SonarQube and SonarScanner for error and complexity detection
  + Analyze code metrics:
  + Document findings in a Statistical Code Review Report:
* Code Clean-Up and Refactoring
  + Refactor and simplify:
  + Split complex functions into smaller units
  + Remove redundant or duplicate code
  + Simplify naming conventions and ensure consistent formatting
* Acceptance Test Planning
  + Design acceptance tests covering:
  + Functional testing
  + Usability testing
  + Performance and reliability testing
* Architecture Design Documentation
  + Create or update ER diagram reflecting the updated database schema
  + Update UML models to reflect Sprint 6 changes
  + Commit all design files to /docs in the GitHub repository (PDF or image format)

## Team Capacity & Assumptions

For Sprint 6, the team members agreed to:

* Work collaboratively on **Saturday and Sundays** and individually during the two-week sprint period
* Meetings are conducted via **Discord**
* Smaller meetings may occur when at least two members are available
* Working hours are recorded individually in a **Sprint Review Report**
* All implementation and planning activities are performed collaboratively during meetings and individual work sessions

**Code review approach:**  
The team will use SonarQube with SonarScanner for static analysis. Results will be documented with screenshots and metric tables. Refactoring will follow Java coding conventions and will be verified by re-running the existing unit test suite.

**Languages supported (carried over from Sprint 5):**
* English 🇬🇧 — Default (Latin)
* Finnish 🇫🇮 — Non-Latin, LTR
* Arabic 🇸🇦 — Non-Latin, RTL
* Russian 🇷🇺 — Non-Latin, LTR

## Definition of Done

A Sprint 6 task is considered done when:

* The task objectives are completed and reviewed by the team
* Database localization is implemented, tested, and documented in the README
* Static code analysis report is complete with metrics, findings, and screenshots
* Refactored code passes all unit tests with no regressions
* Acceptance Test Plan document is written with defined criteria and test cases
* ER diagram and UML sketches are committed to /docs in the GitHub repository
* The task is moved to the "Done" column in Trello
* Relevant code and documents are committed to the GitHub repository

*Definition of done is specified in every Trello story.*