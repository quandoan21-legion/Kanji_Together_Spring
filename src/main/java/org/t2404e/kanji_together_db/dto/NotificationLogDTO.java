package org.t2404e.kanji_together_db.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.t2404e.kanji_together_db.enums.NotificationStatus;

import java.time.LocalDateTime;

@Data
public class NotificationLogDTO {
    private Long id;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("fcm_token")
    private String fcmToken;

    @JsonProperty("kanji_ids")
    private String kanjiIds;

    @JsonProperty("kanji_hash")
    private String kanjiHash;

    private NotificationStatus status;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("create_at")
    private LocalDateTime createAt;
}
