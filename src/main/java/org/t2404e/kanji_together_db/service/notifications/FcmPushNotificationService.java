package org.t2404e.kanji_together_db.service.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class FcmPushNotificationService implements PushNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(FcmPushNotificationService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${fcm.project-id:}")
    private String projectId;

    @Value("${fcm.access-token:}")
    private String accessToken;

    @Value("${fcm.endpoint-template:https://fcm.googleapis.com/v1/projects/%s/messages:send}")
    private String endpointTemplate;

    @Override
    public void sendToTokens(Long userId, List<String> tokens, String title, String body, Map<String, String> data) {
        sendToTokens(userId, tokens, title, body, data, null);
    }

    @Override
    public void sendToTokens(Long userId, List<String> tokens, String title, String body, Map<String, String> data, String accessTokenOverride) {
        if (tokens == null || tokens.isEmpty()) {
            return;
        }
        String resolvedAccessToken = resolveAccessToken(accessTokenOverride);
        if (projectId == null || projectId.isBlank() || resolvedAccessToken == null || resolvedAccessToken.isBlank()) {
            logger.warn("FCM not configured. Skipping push for userId={}, tokens={}, title={}", userId, tokens.size(), title);
            return;
        }

        String endpoint = String.format(endpointTemplate, projectId);
        for (String token : tokens) {
            if (token == null || token.isBlank()) {
                continue;
            }
            Map<String, Object> payload = Map.of(
                    "message", Map.of(
                            "token", token,
                            "notification", Map.of(
                                    "title", title,
                                    "body", body
                            ),
                            "data", data != null ? data : Map.of()
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(resolvedAccessToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            try {
                restTemplate.postForEntity(endpoint, request, String.class);
            } catch (RestClientException ex) {
                logger.warn("FCM push failed for userId={}, token={}, error={}", userId, token, ex.getMessage());
            }
        }
    }

    private String resolveAccessToken(String override) {
        if (override != null && !override.isBlank()) {
            return override.trim();
        }
        return accessToken != null ? accessToken.trim() : null;
    }
}
