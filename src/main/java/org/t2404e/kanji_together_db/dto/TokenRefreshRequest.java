package org.t2404e.kanji_together_db.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenRefreshRequest {
    @NotBlank(message = "Refresh token không được để trống")
    @JsonProperty("refresh_token")
    private String refreshToken;
}
