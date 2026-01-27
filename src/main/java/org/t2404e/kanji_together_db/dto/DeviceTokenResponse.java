package org.t2404e.kanji_together_db.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.t2404e.kanji_together_db.enums.DevicePlatform;

import java.time.LocalDateTime;

@Data
public class DeviceTokenResponse {
    private Long id;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("fcm_token")
    private String fcmToken;

    private DevicePlatform platform;

    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty("app_version")
    private String appVersion;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("last_seen_at")
    private LocalDateTime lastSeenAt;

    @JsonProperty("create_at")
    private LocalDateTime createAt;

    @JsonProperty("edit_at")
    private LocalDateTime editAt;

}
