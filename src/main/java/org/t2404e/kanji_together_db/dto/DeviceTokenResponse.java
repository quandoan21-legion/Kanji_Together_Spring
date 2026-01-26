package org.t2404e.kanji_together_db.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeviceTokenResponse {
    private Long id;

    @JsonProperty("user_id")
    private Long userId;

    private String token;

    private String platform;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("last_seen_at")
    private LocalDateTime lastSeenAt;

    @JsonProperty("create_at")
    private LocalDateTime createAt;

    @JsonProperty("edit_at")
    private LocalDateTime editAt;

}
