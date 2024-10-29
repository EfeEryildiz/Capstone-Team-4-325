package org.example.studystack;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DataStore {

    private static DataStore instance = null;

    private ObservableList<Note> notesList;
    private ObservableList<Deck> decksList;

    private DataStore() {
        notesList = FXCollections.observableArrayList();
        decksList = FXCollections.observableArrayList();
    }

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    public ObservableList<Note> getNotesList() {
        return notesList;
    }

    public ObservableList<Deck> getDecksList() {
        return decksList;
    }
}