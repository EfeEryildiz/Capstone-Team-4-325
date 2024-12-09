package org.example.studystack;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NoteTest {
    
    @Test
    public void testNoteCreation() {
        Note note = new Note("Test Title", "Test Content");
        assertEquals("Test Title", note.getTitle());
        assertEquals("Test Content", note.getContent());
    }

    @Test
    public void testNoteModification() {
        Note note = new Note("Original Title", "Original Content");
        note.setTitle("New Title");
        note.setContent("New Content");
        assertEquals("New Title", note.getTitle());
        assertEquals("New Content", note.getContent());
    }

    @Test
    public void testToString() {
        Note note = new Note("Test Note", "Content");
        assertEquals("Test Note", note.toString());
    }

    @Test
    public void testNullValues() {
        Note note = new Note(null, null);
        assertNull(note.getTitle());
        assertNull(note.getContent());
    }
} 