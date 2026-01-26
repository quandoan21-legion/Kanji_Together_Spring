package org.t2404e.kanji_together_db.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class ExamResultCreateRequest {
    @JsonAlias("user_id")
    private Long userId;
    @JsonAlias("exam_id")
    private Long examId;
    @JsonAlias("total_question")
    private Integer totalQuestion;

}
