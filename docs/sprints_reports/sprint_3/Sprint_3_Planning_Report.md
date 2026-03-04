# Team 6 - Sprint 3 Planning Report

**Project:** Student Timetable Management System  
**Team Members:** Taif Jalo, Miska Voutilainen, Elias Norta, Nikita Rybakov

## Sprint Number & Dates

**Sprint Number:** Sprint 3
**Sprint Duration:** 3 Weeks (16 Feb 2026 – 05 Mar 2026)

## Sprint Goal

The goal of Sprint 3 is to extend the MVP with core features (login, messaging, UI modals), strengthen automated testing, establish CI/CD via Jenkins, prepare the application for functional review, and create the first Docker image.

## Selected Product Backlog Items

The following is a part of product backlog. Full product backlog is accessible through [this link](https://trello.com/b/is0nq5uS/sprint-3).

- Extend functional prototype (login, messaging, UI modals)
- Integrate Jenkins CI/CD pipeline with JaCoCo
- Expand automated unit tests and code coverage
- Prepare for functional review
- Create and test initial Docker image

## Planned Tasks / Breakdown

- Functional Prototype
  - Implement user login authentication
  - Implement messaging feature
  - Implement modals and UI cleanup
  - Connect frontend to backend workflows
  - Optimize backend logic & database queries
  - Fix bugs discovered during earlier sprints
- Jenkins CI/CD
  - Configure Jenkins pipeline
  - Enable Git checkout → Maven build → tests → JaCoCo
  - Publish test & coverage reports
  - Validate pipeline on branch updates
- Testing
  - Expand unit test coverage for all new features
  - Add edge case tests
  - Integrate code coverage reporting
  - Ensure stability before functional review
- Functional Review Readiness
  - Validate all workflows end‑to‑end
  - Ensure UI + backend integration
  - Prepare demo scenarios
- Docker
  - Create initial Dockerfile
  - Build and test local Docker image
  - Validate basic container functionality
- Set up Sprint 3 board lists:
  - Sprint 3 Backlog, Done, Pending, In Progress, Postponed
- Document agreed decisions in Trello and GitHub

## Team Capacity & Assumptions

For Sprint 3, the team members agreed to:

- Work a few hours together on **Sundays and Mondays** also working individually during the three-week Sprint 3 period
- Meetings are conducted via **Discord**
- Smaller meetings may occur when at least two members are available
- Working hours are recorded individually in a **shared Excel file**
- All development and planning activities are performed collaboratively during meetings and individual work sessions.
- Assumption: Jenkins server is available and accessible

## Definition of Done

A Sprint 3 task is considered done when:

- The task objectives are completed and reviewed by the team.
- Code is committed, reviewed, and merged.
- All functionality is tested (unit + manual).
- CI pipeline builds successfully with no failures.
- Code coverage report is generated and stored.
- Documentation is updated (README, sprint report).
- Docker image builds and runs locally.
- The task is moved to the "Done" column in Trello.

_Definition of done is specified in every Trello stories._