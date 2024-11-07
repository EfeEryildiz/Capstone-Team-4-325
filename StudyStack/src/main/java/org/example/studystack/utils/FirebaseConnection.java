// FirebaseConnection.java
package org.example.studystack.utils;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FirebaseConnection {
        private static Firestore db;
        private static boolean initialized = false;
        private static final String PROJECT_ID = "studystack-app";  // Your new project ID

        public static void initialize() {
            if (initialized) return;

            try {
                // Load the new service account key
                InputStream serviceAccount = new FileInputStream("StudyStack/src/main/resources/org/example/studystack/key.json");

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setProjectId(PROJECT_ID)
                        // The email shown in your screenshot indicates this is your new project
                        .setServiceAccountId("firebase-adminsdk-33ixk@studystack-app.iam.gserviceaccount.com")
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                    System.out.println("Firebase initialized successfully with new service account");
                }

                db = FirestoreClient.getFirestore();
                initialized = true;

            } catch (IOException e) {
                System.err.println("Error initializing Firebase: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to initialize Firebase", e);
            }
        }

        public static Firestore getFirestore() {
            if (!initialized) {
                initialize();
            }
            return db;
        }

        // Helper method to check initialization status
        public static boolean isInitialized() {
            return initialized && db != null;
        }
    }