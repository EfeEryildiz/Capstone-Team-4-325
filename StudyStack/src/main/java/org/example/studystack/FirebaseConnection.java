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
    private static boolean initialized = false; // To prevent multiple initializations

    /**
     * Initializes the Firebase connection using the service account key.
     */
    public static void initialize() {
        if (initialized) {
            logger.info("Firebase is already initialized.");
            return;
        }

        try (InputStream serviceAccount = FirebaseConnection.class.getClassLoader().getResourceAsStream("Key.json")) {
            if (serviceAccount == null) {
                throw new IOException("Service account file not found in resources.");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://capstone-team-4-e2207-default-rtdb.firebaseio.com")
                    .build();

            FirebaseApp.initializeApp(options);
            initialized = true;
            logger.info("Firebase initialized successfully.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to initialize Firebase.", e);
        }
    }
}
