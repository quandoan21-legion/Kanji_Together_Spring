package org.t2404e.kanji_together_db.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.time.LocalDateTime;

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

    public Long getExamResultId() {
        return examResultId;
    }

    public void setExamResultId(Long examResultId) {
        this.examResultId = examResultId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getSelectedAnswerId() {
        return selectedAnswerId;
    }

    public void setSelectedAnswerId(Integer selectedAnswerId) {
        this.selectedAnswerId = selectedAnswerId;
    }

    public String getSelectedAnswer() {
        return selectedAnswer;
    }

    public void setSelectedAnswer(String selectedAnswer) {
        this.selectedAnswer = selectedAnswer;
    }

    public Long getTimeTakenMs() {
        return timeTakenMs;
    }

    public void setTimeTakenMs(Long timeTakenMs) {
        this.timeTakenMs = timeTakenMs;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }
}
