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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayInputStream;
import java.util.Properties;

public class FirebaseConnection {
    private static final Logger logger = Logger.getLogger(FirebaseConnection.class.getName());
    private static boolean isInitialized = false;
    private static final String DATABASE_URL = "https://studystack-app-default-rtdb.firebaseio.com/";
    private static final String SERVICE_ACCOUNT_PATH = "Key.json";

    public static synchronized void initialize() {
        if (isInitialized) {
            logger.info("Firebase is already initialized.");
            return;
        }

        try {
            InputStream serviceAccount = FirebaseConnection.class.getClassLoader()
                    .getResourceAsStream(SERVICE_ACCOUNT_PATH);
            
            if (serviceAccount == null) {
                logger.severe("Key.json not found in resources!");
                throw new RuntimeException("Key.json not found");
            }

            // Verify Key.json content
            String serviceAccountContent = new String(serviceAccount.readAllBytes(), StandardCharsets.UTF_8);
            JSONObject serviceAccountJson = new JSONObject(serviceAccountContent);
            logger.info("Found Key.json for project: " + serviceAccountJson.getString("project_id"));
            logger.info("Using client email: " + serviceAccountJson.getString("client_email"));
            
            // Reset stream for Firebase use
            serviceAccount = new ByteArrayInputStream(serviceAccountContent.getBytes(StandardCharsets.UTF_8));

            // Also verify config.properties
            Properties props = new Properties();
            InputStream configStream = FirebaseConnection.class.getClassLoader()
                    .getResourceAsStream("config.properties");
            
            if (configStream == null) {
                logger.severe("config.properties not found in resources!");
                throw new RuntimeException("config.properties not found");
            }
            
            props.load(configStream);
            String apiKey = props.getProperty("firebase.api.key");
            if (apiKey == null || apiKey.trim().isEmpty()) {
                logger.severe("Firebase API key not found in config.properties!");
                throw new RuntimeException("Firebase API key not found");
            }
            logger.info("Found Firebase API key (first 10 chars): " + apiKey.substring(0, 10) + "...");

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
            throw new RuntimeException("Failed to initialize Firebase", e);
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
