package org.example.studystack;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Deck {
    private String name;
    private ObservableList<Flashcard> flashcards;

    public Deck(String name) {
        this.name = name;
        this.flashcards = FXCollections.observableArrayList();
    }

    public String getName() {
        return name;
    }

    public ObservableList<Flashcard> getFlashcards() {
        return flashcards;
    }

    @Override
    public String toString() {
        return name;
    }
}
