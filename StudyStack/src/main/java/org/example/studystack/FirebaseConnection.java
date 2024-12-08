package org.example.studystack;



import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.InputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FirebaseConnection {
    private static final Logger logger = Logger.getLogger(FirebaseConnection.class.getName());
    private static boolean isInitialized = false;
    private static final String DATABASE_URL = "https://studystack-app-default-rtdb.firebaseio.com/";

    public static synchronized void initialize() {
        if (isInitialized) {
            logger.info("Firebase is already initialized.");
            return;
        }

        try {
            // Initialize the Firebase app if it doesn't exist
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                    .setDatabaseUrl(DATABASE_URL)
                    .setCredentials(GoogleCredentials.getApplicationDefault())
                    .build();

                FirebaseApp.initializeApp(options);
                logger.info("Firebase initialized successfully with database URL: " + DATABASE_URL);
            } else {
                logger.info("Firebase app already exists");
            }

            isInitialized = true;
        } catch (Exception e) {
            logger.severe("Failed to initialize Firebase: " + e.getMessage());
            e.printStackTrace();
            // Try alternative initialization
            tryAlternativeInitialization();
        }
    }

    private static void tryAlternativeInitialization() {
        try {
            // Try initializing with just the database URL if credentials fail
            FirebaseOptions options = FirebaseOptions.builder()
                .setDatabaseUrl(DATABASE_URL)
                .build();

            FirebaseApp.initializeApp(options);
            isInitialized = true;
            logger.info("Firebase initialized successfully with alternative method");
        } catch (Exception e) {
            logger.severe("Failed alternative Firebase initialization: " + e.getMessage());
            throw new RuntimeException("Could not initialize Firebase", e);
        }
    }

    public static boolean isInitialized() {
        return isInitialized;
    }
}
