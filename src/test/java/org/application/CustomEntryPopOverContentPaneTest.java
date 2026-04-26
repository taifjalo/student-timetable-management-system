package org.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CustomEntryPopOverContentPaneTest {

    @Test
    void savedLessonRecordStoresId() {
        EntryPopOverContentPane.SavedLesson lesson = new EntryPopOverContentPane.SavedLesson(42L);
        assertNotNull(lesson, "SavedLesson should be instantiable");
        assertEquals(42L, lesson.lessonId(), "SavedLesson should store the provided ID");
    }
}
