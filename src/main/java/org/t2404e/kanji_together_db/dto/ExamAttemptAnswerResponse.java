package org.t2404e.kanji_together_db.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExamAttemptAnswerResponse {
    private Long id;
    private Long examResultId;
    private Long userId;
    private Long questionId;
    private Integer selectedAnswerId;
    private String selectedAnswer;
    private Boolean isCorrect;
    private Long timeTakenMs;
    private LocalDateTime answeredAt;
    private Long attemptCount;
    private String correctAnswer;
    private String wrongAnswer1;
    private String wrongAnswer2;
    private String wrongAnswer3;

}
