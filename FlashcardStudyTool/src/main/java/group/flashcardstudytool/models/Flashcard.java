package group.flashcardstudytool.models;

import java.util.UUID;

public class Flashcard {
        private String id;
        private String front;
        private String back;
        private String deckId;

        public Flashcard() {
            this.id = UUID.randomUUID().toString();
        }

        public Flashcard(String front, String back, String deckId) {
            this();
            this.front = front;
            this.back = back;
            this.deckId = deckId;
        }

        // Getters and setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFront() {
            return front;
        }

        public void setFront(String front) {
            this.front = front;
        }

        public String getBack() {
            return back;
        }

        public void setBack(String back) {
            this.back = back;
        }

        public String getDeckId() {
            return deckId;
        }

        public void setDeckId(String deckId) {
            this.deckId = deckId;
        }

        @Override
        public String toString() {
            return "Flashcard{" +
                    "id='" + id + '\'' +
                    ", front='" + front + '\'' +
                    ", back='" + back + '\'' +
                    ", deckId='" + deckId + '\'' +
                    '}';
        }
    }

