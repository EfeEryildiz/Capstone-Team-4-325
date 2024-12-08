package org.example.studystack;



import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.InputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;

public class FirebaseConnection {
    private static final Logger logger = Logger.getLogger(FirebaseConnection.class.getName());
    private static boolean isInitialized = false;
    private static final String DATABASE_URL = "https://studystack-app-default-rtdb.firebaseio.com/";
    private static final String SERVICE_ACCOUNT_PATH = "serviceAccountKey.json";

    public static synchronized void initialize() {
        if (isInitialized) {
            logger.info("Firebase is already initialized.");
            return;
        }

        try {
            // Try to load the service account file from resources
            InputStream serviceAccount = FirebaseConnection.class.getClassLoader()
                    .getResourceAsStream(SERVICE_ACCOUNT_PATH);
            
            if (serviceAccount == null) {
                logger.warning("Service account file not found in resources, trying alternative path");
                // Try to load from project root
                serviceAccount = new FileInputStream(SERVICE_ACCOUNT_PATH);
            }

            FirebaseOptions options = FirebaseOptions.builder()
                .setDatabaseUrl(DATABASE_URL)
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

            FirebaseApp.initializeApp(options);
            isInitialized = true;
            logger.info("Firebase initialized successfully with database URL: " + DATABASE_URL);

        } catch (Exception e) {
            logger.severe("Failed to initialize Firebase: " + e.getMessage());
            e.printStackTrace();
            tryAlternativeInitialization();
        }
    }

    private static void tryAlternativeInitialization() {
        try {
            // Create credentials with default scopes
            List<String> scopes = Arrays.asList(
                "https://www.googleapis.com/auth/firebase.database",
                "https://www.googleapis.com/auth/userinfo.email"
            );
            GoogleCredentials credentials = GoogleCredentials.create(null).createScoped(scopes);

            FirebaseOptions options = FirebaseOptions.builder()
                .setDatabaseUrl(DATABASE_URL)
                .setCredentials(credentials)
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
