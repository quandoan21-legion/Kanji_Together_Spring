package org.t2404e.kanji_together_db.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenRefreshResponse {
    @JsonProperty("access_token")
    private String accessToken;
}
