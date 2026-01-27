package org.t2404e.kanji_together_db.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AttemptRequest {
    @Schema(description = "ID of the user submitting the attempt", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonAlias("userId")
    private Long userId;
    @Schema(description = "ID of the question being answered", example = "456", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonAlias("questionId")
    private Long questionId;
    @Schema(description = "Whether the answer was correct", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonAlias("correct")
    private Boolean correct;
    @Schema(description = "Answer choice selected by the user", example = "B", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonAlias("selectedAnswer")
    private String selectedAnswer;
    @Schema(description = "Time spent answering in milliseconds", example = "18000", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonAlias("timeSpentMs")
    private Integer timeSpentMs;
    @Schema(description = "ISO-8601 timestamp; if omitted, server uses current time", example = "2026-01-27T04:12:19", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonAlias("answeredAt")
    private LocalDateTime answeredAt;
}
