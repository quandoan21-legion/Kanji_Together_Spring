package org.t2404e.kanji_together_db.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class KanjiLessonDTO {
    private Long id;

    private String kanji;

    private Integer jlpt;

    @JsonProperty("lesson_description")
    private String lessonDescription;
    // -----------------------------------------------------

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime editAt;

    private Integer createBy;
    private Integer editBy;


    // Phải thêm annotation này thì Java mới nhận được mảng ID từ Rails
    @JsonProperty("kanji_ids")
    private List<Long> kanjiIds;
    // --------------------------------------------

    @JsonProperty("exam_ids")
    private List<Long> examIds;

    // Danh sách chi tiết trả về (Chứa kiến thức Kanji)
    private List<KanjiFullInfo> kanjiList;

    // --- CLASS CON CHỨA KIẾN THỨC KANJI ---
    @Data
    public static class KanjiFullInfo {
        private Long id;
        private String kanji;
        private Integer jlpt;
    }
}
