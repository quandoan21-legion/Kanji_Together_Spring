package org.t2404e.kanji_together_db.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AttemptRequest {
    @Schema(description = "ID of the user submitting the attempt", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;
    @Schema(description = "ID of the question being answered", example = "456", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long questionId;
    @Schema(description = "Whether the answer was correct", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean correct;
    @Schema(description = "Answer choice selected by the user", example = "B", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String selectedAnswer;
    @Schema(description = "Time spent answering in milliseconds", example = "18000", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer timeSpentMs;
    @Schema(description = "ISO-8601 timestamp; if omitted, server uses current time", example = "2026-01-27T04:12:19", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime answeredAt;
}
