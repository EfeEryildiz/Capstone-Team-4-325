package org.example.studystack;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NotebookControllerTest {
    
    private Note testNote;
    
    @BeforeEach
    public void setUp() {
        testNote = new Note("Test Title", "Test Content");
        DataStore.getInstance().clear();
    }

    @Test
    public void testNoteCreation() {
        DataStore.getInstance().getNotesList().add(testNote);
        assertEquals(1, DataStore.getInstance().getNotesList().size());
        assertEquals("Test Title", DataStore.getInstance().getNotesList().get(0).getTitle());
    }

    @Test
    public void testNoteContentUpdate() {
        testNote.setContent("Updated Content");
        assertEquals("Updated Content", testNote.getContent());
    }

    @Test
    public void testNoteTitleUpdate() {
        testNote.setTitle("New Title");
        assertEquals("New Title", testNote.getTitle());
    }

    @Test
    public void testMultipleNotes() {
        Note note1 = new Note("Title 1", "Content 1");
        Note note2 = new Note("Title 2", "Content 2");
        
        DataStore.getInstance().getNotesList().add(note1);
        DataStore.getInstance().getNotesList().add(note2);
        
        assertEquals(2, DataStore.getInstance().getNotesList().size());
        assertEquals("Title 1", DataStore.getInstance().getNotesList().get(0).getTitle());
        assertEquals("Title 2", DataStore.getInstance().getNotesList().get(1).getTitle());
    }

    @Test
    public void testNoteRemoval() {
        DataStore.getInstance().getNotesList().add(testNote);
        assertEquals(1, DataStore.getInstance().getNotesList().size());
        
        DataStore.getInstance().getNotesList().remove(testNote);
        assertEquals(0, DataStore.getInstance().getNotesList().size());
    }

    @Test
    public void testEmptyNoteContent() {
        Note emptyNote = new Note("Empty Note", "");
        assertEquals("", emptyNote.getContent());
    }

    @Test
    public void testNullNoteContent() {
        Note nullNote = new Note("Null Note", null);
        assertNull(nullNote.getContent());
    }
} 