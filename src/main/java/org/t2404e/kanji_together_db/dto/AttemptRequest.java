package org.t2404e.kanji_together_db.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AttemptRequest {
    private Long userId;
    private Long questionId;
    private Boolean correct;
    private String selectedAnswer;
    private Integer timeSpentMs;
    private LocalDateTime answeredAt;
}
