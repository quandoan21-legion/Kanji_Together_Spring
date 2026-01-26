package org.t2404e.kanji_together_db.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExamResultResponse {
    private Long id;
    private Long userId;
    private Long examId;
    private Integer totalQuestion;
    private Integer correctAnswer;
    private LocalDateTime createdAt;

}
