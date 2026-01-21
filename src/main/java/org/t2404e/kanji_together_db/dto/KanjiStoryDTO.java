package org.t2404e.kanji_together_db.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KanjiStoryDTO {
    private Long id;
    @JsonProperty("kanji_id")
    private Long kanjiId;
    @NotBlank(message = "Mặt chữ Kanji không được để trống")
    @Pattern(regexp = "^[\\u4E00-\\u9FAF]$", message = "Chỉ được nhập duy nhất 1 ký tự Kanji")
    @JsonProperty("kanji_text")
    private String kanjiText;

    @JsonProperty("kanji_story")
    private String kanjiStory;

    @JsonProperty("status")
    private String status;

    @JsonProperty("user_email")
    private String userEmail;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("create_at")
    private LocalDateTime createAt;

    @JsonProperty("create_by")
    private Integer createBy;

    @JsonProperty("edit_by")
    private Integer editBy;

    // --- CÁC TRƯỜNG DỮ LIỆU THÔ NGƯỜI DÙNG GỬI (Dùng để hiển thị ở cột bên trái View) ---

    @JsonProperty("user_translation")
    private String userTranslation;
    @JsonProperty("user_meaning")
    private String userMeaning;
    @JsonProperty("user_num_strokes")
    private Integer userNumStrokes;
    @JsonProperty("user_radical")
    private String userRadical;
    @JsonProperty("user_components")
    private String userComponents;
    @JsonProperty("user_vocabulary")
    private String userVocabulary;
    @JsonProperty("user_examples")
    private String userExamples;
    @JsonProperty("user_onyomi")
    private String userOnyomi;
    @JsonProperty("user_kunyomi")
    private String userKunyomi;
    @JsonProperty("reject_reason")
    private String rejectReason;
}