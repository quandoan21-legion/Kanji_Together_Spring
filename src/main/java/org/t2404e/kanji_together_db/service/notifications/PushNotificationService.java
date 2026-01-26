package org.t2404e.kanji_together_db.service.notifications;

import java.util.List;
import java.util.Map;

public interface PushNotificationService {
    void sendToTokens(Long userId, List<String> tokens, String title, String body, Map<String, String> data);

    void sendToTokens(Long userId, List<String> tokens, String title, String body, Map<String, String> data, String accessTokenOverride);
}
