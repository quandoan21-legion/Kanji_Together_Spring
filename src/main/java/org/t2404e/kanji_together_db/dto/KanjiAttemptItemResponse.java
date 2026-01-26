package org.t2404e.kanji_together_db.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KanjiAttemptItemResponse {
    private Long questionId;
    private String questionText;
    private Long examResultId;
    private String selectedAnswer;
    private Boolean isCorrect;
    private LocalDateTime createdAt;

}
