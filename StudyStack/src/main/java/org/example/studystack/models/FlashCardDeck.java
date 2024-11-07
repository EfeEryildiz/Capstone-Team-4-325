// FlashcardDeck.java
package org.example.studystack.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FlashCardDeck {
    private String id;
    private String name;
    private String description;
    private List<FlashCardModel> flashcards;
    private String userId;
    private LocalDateTime createdAt;

    public FlashCardDeck(String name, String description, String userId) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.userId = userId;
        this.flashcards = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }

    // Deck management methods
    public void addFlashcard(FlashCardModel flashcard) {
        flashcards.add(flashcard);
    }

    public void removeFlashcard(String flashcardId) {
        flashcards.removeIf(f -> f.getId().equals(flashcardId));
    }

    public List<FlashCardModel> getFlashcards() {
        return new ArrayList<>(flashcards);
    }

    // Getters and setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getUserId() { return userId; }
}
