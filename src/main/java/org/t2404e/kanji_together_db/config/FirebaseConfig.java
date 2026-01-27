package org.t2404e.kanji_together_db.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import jakarta.annotation.PostConstruct;
import java.io.IOException;

@Configuration
public class FirebaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${fcm.service-account-file:}")
    private Resource serviceAccountFile;

    @PostConstruct
    public void init() {
        if (FirebaseApp.getApps().isEmpty()) {
            if (serviceAccountFile == null || !serviceAccountFile.exists()) {
                logger.warn("Firebase service account file not configured. FCM will be disabled.");
                return;
            }
            try {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccountFile.getInputStream()))
                        .build();
                FirebaseApp.initializeApp(options);
            } catch (IOException ex) {
                logger.warn("Failed to initialize Firebase: {}", ex.getMessage());
            }
        }
    }
}
