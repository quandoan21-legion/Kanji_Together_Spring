package org.t2404e.kanji_together_db.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.t2404e.kanji_together_db.enums.QuestionType;

import java.util.List;

@Data
public class QuestionDTO {

    private Long id;

    @JsonProperty("question_type")
    private QuestionType questionType;

    @JsonProperty("question_text")
    private String questionText;

    @JsonProperty("correct_answer")
    private String correctAnswer;

    @JsonProperty("wrong_answer_1")
    private String wrongAnswer1;

    @JsonProperty("wrong_answer_2")
    private String wrongAnswer2;

    @JsonProperty("wrong_answer_3")
    private String wrongAnswer3;

    @JsonProperty("exam_id")
    private Long examId;

    @JsonProperty("kanji_ids")
    private List<Long> kanjiIds;

    private Integer status;
}