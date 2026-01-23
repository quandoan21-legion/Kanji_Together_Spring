package org.t2404e.kanji_together_db.dto;

import java.time.LocalDateTime;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
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
