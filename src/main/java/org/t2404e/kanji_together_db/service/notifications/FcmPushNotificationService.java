package org.t2404e.kanji_together_db.service.notifications;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.t2404e.kanji_together_db.enums.NotificationStatus;
import org.t2404e.kanji_together_db.repository.UserDeviceTokensRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FcmPushNotificationService implements PushNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(FcmPushNotificationService.class);

    private final UserDeviceTokensRepository userDeviceTokensRepository;

    public FcmPushNotificationService(UserDeviceTokensRepository userDeviceTokensRepository) {
        this.userDeviceTokensRepository = userDeviceTokensRepository;
    }

    @Override
    public void sendToTokens(Long userId, List<String> tokens, String title, String body, Map<String, String> data) {
        sendToTokens(userId, tokens, title, body, data, null);
    }

    @Override
    public void sendToTokens(Long userId, List<String> tokens, String title, String body, Map<String, String> data, String accessTokenOverride) {
        if (tokens == null || tokens.isEmpty()) {
            return;
        }
        if (FirebaseApp.getApps().isEmpty()) {
            logger.warn("Firebase not initialized. Skipping push for userId={}, tokens={}, title={}", userId, tokens.size(), title);
            return;
        }
        for (String token : tokens) {
            if (token == null || token.isBlank()) {
                continue;
            }
            try {
                Message message = Message.builder()
                        .setToken(token)
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .putAllData(data != null ? data : Map.of())
                        .build();
                FirebaseMessaging.getInstance().send(message);
            } catch (FirebaseMessagingException ex) {
                logger.warn("FCM push failed for userId={}, token={}, error={}", userId, token, ex.getMessage());
                if (isInvalidToken(ex)) {
                    deactivateToken(token);
                }
            }
        }
    }

    public FcmBatchResult sendMulticast(Long userId, List<String> tokens, String title, String body, Map<String, String> data) {
        if (tokens == null || tokens.isEmpty()) {
            return new FcmBatchResult(Map.of(), Map.of(), "no_tokens");
        }
        if (FirebaseApp.getApps().isEmpty()) {
            logger.warn("Firebase not initialized. Skipping push for userId={}, tokens={}, title={}", userId, tokens.size(), title);
            return new FcmBatchResult(Map.of(), Map.of("batch", "firebase_not_initialized"), "firebase_not_initialized");
        }

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data != null ? data : Map.of())
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            Map<String, NotificationStatus> statuses = new LinkedHashMap<>();
            Map<String, String> errors = new LinkedHashMap<>();
            List<SendResponse> responses = response.getResponses();
            for (int i = 0; i < responses.size(); i++) {
                String token = tokens.get(i);
                SendResponse sendResponse = responses.get(i);
                if (sendResponse.isSuccessful()) {
                    statuses.put(token, NotificationStatus.SENT);
                } else {
                    statuses.put(token, NotificationStatus.FAILED);
                    FirebaseMessagingException exception = sendResponse.getException();
                    String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
                    errors.put(token, errorMessage);
                    if (exception != null && isInvalidToken(exception)) {
                        deactivateToken(token);
                    }
                }
            }
            return new FcmBatchResult(statuses, errors, null);
        } catch (FirebaseMessagingException ex) {
            logger.warn("FCM multicast failed for userId={}, tokens={}, error={}", userId, tokens.size(), ex.getMessage());
            return new FcmBatchResult(Map.of(), Map.of("batch", ex.getMessage()), "fcm_error");
        }
    }

    private boolean isInvalidToken(FirebaseMessagingException ex) {
        MessagingErrorCode code = ex.getMessagingErrorCode();
        return code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT;
    }

    private void deactivateToken(String token) {
        userDeviceTokensRepository.findByFcmToken(token).ifPresent(record -> {
            record.setIsActive(false);
            userDeviceTokensRepository.save(record);
        });
    }
}
