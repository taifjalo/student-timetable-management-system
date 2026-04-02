# DB Localization — Group Member Handoff

### Overview

The notification localization is complete. What remains is localizing **user-entered content**: course names and group field-of-study names. These are stored as single strings in the DB today. The work below adds optional translation tables so teachers can provide names in multiple languages.

---

### Step 1 — Run the DB migrations

Done.

`language_code` values used by the app: `en`, `fi`, `ar`, `ru` — these come from `LocalizationService.getCurrentLocale().getLanguage()`.

---

### Step 2 — Create JPA entities

#### `src/main/java/org/entities/CourseTranslation.java`

You need an `@EmbeddedId` composite key class first:

```java
package org.entities;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CourseTranslationId implements Serializable {
    private Integer courseId;
    private String languageCode;

    public CourseTranslationId() {}
    public CourseTranslationId(Integer courseId, String languageCode) {
        this.courseId = courseId;
        this.languageCode = languageCode;
    }
    // equals() and hashCode() are mandatory for composite keys
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CourseTranslationId that)) return false;
        return Objects.equals(courseId, that.courseId) && Objects.equals(languageCode, that.languageCode);
    }
    @Override public int hashCode() { return Objects.hash(courseId, languageCode); }
}
```

Then the entity:

```java
package org.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "course_translations")
public class CourseTranslation {

    @EmbeddedId
    private CourseTranslationId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("courseId")
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "name", nullable = false)
    private String name;

    public CourseTranslation() {}
    public CourseTranslation(Course course, String languageCode, String name) {
        this.course = course;
        this.id = new CourseTranslationId(Math.toIntExact(course.getId()), languageCode);
        this.name = name;
    }

    public CourseTranslationId getId() { return id; }
    public Course getCourse()         { return course; }
    public String getName()           { return name; }
    public void setName(String name)  { this.name = name; }
}
```

#### `src/main/java/org/entities/GroupTranslation.java`

Same pattern. Composite key class:

```java
package org.entities;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class GroupTranslationId implements Serializable {
    private String groupCode;
    private String languageCode;

    public GroupTranslationId() {}
    public GroupTranslationId(String groupCode, String languageCode) {
        this.groupCode = groupCode;
        this.languageCode = languageCode;
    }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupTranslationId that)) return false;
        return Objects.equals(groupCode, that.groupCode) && Objects.equals(languageCode, that.languageCode);
    }
    @Override public int hashCode() { return Objects.hash(groupCode, languageCode); }
}
```

Entity:

```java
package org.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "group_translations")
public class GroupTranslation {

    @EmbeddedId
    private GroupTranslationId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupCode")
    @JoinColumn(name = "group_code")
    private StudentGroup studentGroup;

    @Column(name = "field_of_studies", nullable = false)
    private String fieldOfStudies;

    public GroupTranslation() {}
    public GroupTranslation(StudentGroup group, String languageCode, String fieldOfStudies) {
        this.studentGroup = group;
        this.id = new GroupTranslationId(group.getGroupCode(), languageCode);
        this.fieldOfStudies = fieldOfStudies;
    }

    public GroupTranslationId getId()               { return id; }
    public StudentGroup getStudentGroup()            { return studentGroup; }
    public String getFieldOfStudies()                { return fieldOfStudies; }
    public void setFieldOfStudies(String value)      { this.fieldOfStudies = value; }
}
```

---

### Step 3 — Register entities in persistence.xml

Open `src/main/resources/META-INF/persistence.xml` and add four `<class>` entries inside the `<persistence-unit>` block:

```xml
<class>org.entities.CourseTranslation</class>
<class>org.entities.CourseTranslationId</class>
<class>org.entities.GroupTranslation</class>
<class>org.entities.GroupTranslationId</class>
```

---

### Step 4 — Add DAO methods

#### In `CourseDao`

```java
/**
 * Returns the translated course name for the given language.
 * Falls back to the base Course.name if no translation row exists.
 */
public String findTranslatedName(Long courseId, String languageCode) {
    try (EntityManager em = TimetableConnection.createEntityManager()) {
        List<String> result = em.createQuery(
            "SELECT ct.name FROM CourseTranslation ct " +
            "WHERE ct.id.courseId = :courseId AND ct.id.languageCode = :lang",
            String.class
        ).setParameter("courseId", Math.toIntExact(courseId))
         .setParameter("lang", languageCode)
         .getResultList();

        if (!result.isEmpty()) return result.get(0);

        // fallback to the canonical English name
        return em.find(Course.class, courseId).getName();
    }
}

/** Inserts or updates a translation row (upsert via merge). */
public void saveTranslation(Long courseId, String languageCode, String translatedName) {
    try (EntityManager em = TimetableConnection.createEntityManager()) {
        em.getTransaction().begin();
        Course course = em.getReference(Course.class, courseId);
        CourseTranslation ct = new CourseTranslation(course, languageCode, translatedName);
        em.merge(ct);
        em.getTransaction().commit();
    }
}
```

#### In `GroupDao`

```java
/**
 * Returns the translated field_of_studies for the given language.
 * Falls back to StudentGroup.fieldOfStudies if no translation row exists.
 */
public String findTranslatedFieldOfStudies(String groupCode, String languageCode) {
    try (EntityManager em = TimetableConnection.createEntityManager()) {
        List<String> result = em.createQuery(
            "SELECT gt.fieldOfStudies FROM GroupTranslation gt " +
            "WHERE gt.id.groupCode = :groupCode AND gt.id.languageCode = :lang",
            String.class
        ).setParameter("groupCode", groupCode)
         .setParameter("lang", languageCode)
         .getResultList();

        if (!result.isEmpty()) return result.get(0);

        return em.find(StudentGroup.class, groupCode).getFieldOfStudies();
    }
}

/** Inserts or updates a translation row. */
public void saveTranslation(String groupCode, String languageCode, String translatedField) {
    try (EntityManager em = TimetableConnection.createEntityManager()) {
        em.getTransaction().begin();
        StudentGroup group = em.getReference(StudentGroup.class, groupCode);
        GroupTranslation gt = new GroupTranslation(group, languageCode, translatedField);
        em.merge(gt);
        em.getTransaction().commit();
    }
}
```

---

### Step 5 — Service layer changes

Wherever a course or group name is fetched to display in the UI, pass the current language code.

**Pattern** — get the language code:
```java
String lang = LocalizationService.getCurrentLocale().getLanguage(); // "en", "fi", "ar", "ru"
```

In `CourseService`, replace calls that return `course.getName()` with:
```java
courseDao.findTranslatedName(courseId, lang)
```

In `GroupService`, replace calls that return `group.getFieldOfStudies()` with:
```java
groupDao.findTranslatedFieldOfStudies(groupCode, lang)
```

---

### Step 6 — UI: translation input fields in create/edit modals

Teachers should be able to enter optional translated names when creating or editing a course or group.

#### `CreateCourseModalController` / `EditCourseModalController`

Add optional text fields for FI, AR, RU names below the main name field. On save, for each non-empty field call:
```java
courseDao.saveTranslation(courseId, "fi", fiNameField.getText().trim());
courseDao.saveTranslation(courseId, "ar", arNameField.getText().trim());
courseDao.saveTranslation(courseId, "ru", ruNameField.getText().trim());
```
Skip the call if the field is blank — do not overwrite an existing translation with an empty string.

When the edit modal opens, pre-populate the translation fields by calling `courseDao.findTranslatedName()` for each language. If the returned value equals the canonical name (the fallback), leave the field empty so the user knows no translation has been saved yet.

#### `CreateGroupModalController` / `EditGroupModalController`

Same pattern for `field_of_studies` translations using `groupDao.saveTranslation()`.

---

### Step 7 — Display translated names in the calendar/sidebar

The places that currently show course and group names need to call the translated lookup instead of the raw entity field. Key locations:

- `SourceTrayController` — course and group lists in the left sidebar
- `EventExtraDetailsController` — course name and group name in the event details panel

In both controllers, call the service method that now returns the translated name for the active locale. No FXML changes are needed — just update the string that gets passed to the `Label`/`Text` node.

---

### What NOT to localize

These fields are intentionally left as single-language strings:

| Field | Reason |
|---|---|
| `lessons.classroom` | Room codes (e.g. `A101`) are identifiers, not sentences |
| `messages.content` | User chat is never translated — it stays in the language it was written |
| `users.name` / `users.surname` | Proper nouns |
| `courses.color_code` | Internal hex/name value |

---

### Verification checklist (for group members, after implementation)

- [ ] Create a course, enter FI/AR/RU translations in the modal → save → switch language → confirm the sidebar shows the translated name
- [ ] Edit a course, change one translation → save → confirm it updated in the DB (`SELECT * FROM course_translations WHERE course_id = ?`)
- [ ] Create a group with translations → verify `group_translations` rows in DB
- [ ] Switch to a language that has no translation saved for a course → confirm fallback to the canonical English name, no crash
- [ ] Confirm old courses without any `course_translations` rows still display correctly
