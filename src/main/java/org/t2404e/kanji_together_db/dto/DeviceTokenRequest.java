package org.t2404e.kanji_together_db.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DeviceTokenRequest {
    @JsonProperty("user_id")
    private Long userId;

    private String token;

    private String platform;

    @JsonProperty("is_active")
    private Boolean isActive;

}
