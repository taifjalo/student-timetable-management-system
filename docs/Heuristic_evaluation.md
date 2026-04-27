## Heuristic Evaluation Summary Report

This summary combines the most relevant findings from heuristic evaluations conducted by the program developers.  
The report focuses on recurring and important usability issues, their severity, and suggested improvements.

| Heuristic | Main Issue | Severity | Suggested Improvement |
|---|---|---:|---|
| **H1-1: Simple & Natural Dialog** | Some views present too much information at once or do not clearly explain the current system state. Empty states are also not always clear, which may make users unsure what to do next. | 2 | Reduce unnecessary visual clutter, group repeated information, and add clear empty-state messages such as “No messages yet. Search for a user to start a conversation.” |
| **H1-2: Speak the User’s Language** | Some labels and messages use technical, inconsistent, or unclear wording. Users may see internal terms, inconsistent terminology, or unclear login-related messages. | 2 | Replace technical terms with user-friendly labels, standardize terminology across the application, and use clear user-facing messages instead of internal or raw system text. |
| **H1-3: Minimize Users’ Memory Load** | Users are sometimes required to remember information that should be visible in the interface, such as which course, classroom, group, or lesson is relevant. | 2 | Add secondary labels or metadata, such as group code, teacher, classroom, or time slot. Show more relevant details in previews, add autocomplete where useful, and display only relevant items for each user role. |
| **H1-4: Consistency** | Dialogs, buttons, layouts, colors, and input components are not always consistent across the application. Similar actions may appear or behave differently in different parts of the system. | 2 | Standardize modal layouts, button placement, button colors, and date/time input components. Use one consistent design system for forms, dialogs, and actions. |
| **H1-5: Feedback** | The application does not always clearly confirm that actions were completed. Some success messages may be visually confusing, and some loading or refresh actions do not provide enough status feedback. | 2 | Show clear success messages or toast notifications after save, create, update, and delete actions. Use positive colors for success feedback and add loading or progress indicators where actions take time. |
| **H1-6: Clearly Marked Exits** | Some dialogs or pop-ups do not provide clear and consistent ways to exit. Exit controls may be small, poorly placed, or missing a clear alternative such as a Cancel button. | 2 | Add clearly labelled Cancel buttons near primary actions, keep close buttons visible and consistently placed, and support Esc to close dialogs. |
| **H1-7: Shortcuts** | The application lacks keyboard shortcuts for frequent actions, which slows down experienced users and makes repeated tasks less efficient. | 3 | Add common shortcuts such as Enter to send messages, Esc to close dialogs, shortcuts for timetable navigation, and shortcuts for frequently used actions. Show shortcut hints in tooltips. |
| **H1-8: Precise & Constructive Error Messages** | Error messages are sometimes too vague, too generic, missing, or not connected to the exact field that caused the problem. In some cases, users may not know what went wrong or how to fix it. | 3 | Provide specific field-level error messages directly under the relevant input. Highlight incorrect fields and use clear messages such as “Enter a course name”, “Add at least one student”, or “Incorrect username or password.” |
| **H1-9: Prevent Errors** | The system allows preventable errors, such as invalid input, incomplete forms, destructive actions without confirmation, invalid time ranges, or conflicting data. | 3 | Add input validation before saving, disable action buttons until required fields are valid, validate phone, email, and time fields, warn about conflicts, and add confirmation dialogs for destructive actions. |
| **H1-10: Help & Documentation** | The application lacks enough user-facing help, documentation, onboarding, or tooltips. New users may need to discover features by trial and error. | 3 | Add an in-app help section, FAQ, onboarding guide, and tooltips for icon-only buttons. Include a quick-start guide for students and teachers covering the main features. |

### Overall Summary

The evaluations show that the application covers the main functional requirements, but several usability issues reduce clarity, efficiency, and confidence for users. The most serious findings are related to unclear error messages, weak error prevention, lack of shortcuts, and missing user guidance.

Repeated findings also show issues with consistency, feedback, and memory load. These issues appear especially in dialogs, forms, timetable-related workflows, course and lesson management, and messaging.

The highest priority improvements should be field-level validation, clearer error messages, confirmation dialogs for destructive actions, consistent dialog and button design, keyboard shortcuts, and basic help or onboarding for new users.

### Severity Summary

| Severity | Heuristic Areas | Areas |
|---|---:|---|
| 1 — Cosmetic | 0 | — |
| 2 — Minor usability problem | 6 | H1-1, H1-2, H1-3, H1-4, H1-5, H1-6 |
| 3 — Major usability problem | 4 | H1-7, H1-8, H1-9, H1-10 |
| 4 — Usability catastrophe | 0 | — |