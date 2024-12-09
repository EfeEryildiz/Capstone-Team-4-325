package org.example.studystack;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DataStoreTest {
    
    private DataStore dataStore;

    @BeforeEach
    public void setUp() {
        dataStore = DataStore.getInstance();
        dataStore.clear();
    }

    @Test
    public void testSingleton() {
        DataStore instance1 = DataStore.getInstance();
        DataStore instance2 = DataStore.getInstance();
        assertSame(instance1, instance2);
    }

    @Test
    public void testAddAndRemoveNote() {
        Note note = new Note("Test Note", "Content");
        dataStore.getNotesList().add(note);
        assertEquals(1, dataStore.getNotesList().size());
        
        dataStore.getNotesList().remove(note);
        assertEquals(0, dataStore.getNotesList().size());
    }

    @Test
    public void testClearDataStore() {
        dataStore.getNotesList().add(new Note("Note 1", "Content 1"));
        dataStore.getNotesList().add(new Note("Note 2", "Content 2"));
        
        dataStore.clear();
        assertEquals(0, dataStore.getNotesList().size());
    }

    @Test
    public void testMultipleOperations() {
        Note note1 = new Note("Note 1", "Content 1");
        Note note2 = new Note("Note 2", "Content 2");
        
        dataStore.getNotesList().add(note1);
        dataStore.getNotesList().add(note2);
        assertEquals(2, dataStore.getNotesList().size());
        
        note1.setContent("Updated Content");
        assertEquals("Updated Content", dataStore.getNotesList().get(0).getContent());
        
        dataStore.getNotesList().remove(note1);
        assertEquals(1, dataStore.getNotesList().size());
        assertEquals("Note 2", dataStore.getNotesList().get(0).getTitle());
    }
} 