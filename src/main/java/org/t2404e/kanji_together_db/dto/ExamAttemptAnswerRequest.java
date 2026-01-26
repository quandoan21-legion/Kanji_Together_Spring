package org.t2404e.kanji_together_db.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExamAttemptAnswerRequest {
    @JsonAlias("exam_result_id")
    private Long examResultId;
    @JsonAlias("user_id")
    private Long userId;
    @JsonAlias("question_id")
    private Long questionId;
    @JsonAlias("selected_answer_id")
    private Integer selectedAnswerId;
    @JsonAlias("selected_answer")
    private String selectedAnswer;
    @JsonAlias("time_taken_ms")
    private Long timeTakenMs;
    @JsonAlias("answered_at")
    private LocalDateTime answeredAt;

}
