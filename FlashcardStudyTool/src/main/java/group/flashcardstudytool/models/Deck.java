package group.flashcardstudytool.models;

import java.util.UUID;

public class Deck {
    private String id;
    private String name;

    public Deck() {
        this.id = UUID.randomUUID().toString();
    }

    public Deck(String name) {
        this();
        this.name = name;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return name;
    }
}
