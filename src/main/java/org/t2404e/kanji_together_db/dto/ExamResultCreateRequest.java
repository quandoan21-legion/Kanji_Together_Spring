package org.t2404e.kanji_together_db.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class ExamResultCreateRequest {
    @JsonAlias("user_id")
    private Long userId;
    @JsonAlias("exam_id")
    private Long examId;
    @JsonAlias("total_question")
    private Integer totalQuestion;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public Integer getTotalQuestion() {
        return totalQuestion;
    }

    public void setTotalQuestion(Integer totalQuestion) {
        this.totalQuestion = totalQuestion;
    }
}
