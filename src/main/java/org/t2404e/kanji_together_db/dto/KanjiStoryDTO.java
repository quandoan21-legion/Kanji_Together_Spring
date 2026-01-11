package org.t2404e.kanji_together_db.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KanjiStoryDTO {
    private Long id;

    @NotBlank(message = "Nội dung câu chuyện không được để trống")
    @JsonProperty("kanji_story") // JSON sẽ trả về key là "kanji_story" cho khớp DB
    private String kanjiStory;

    @NotNull(message = "Phải chỉ định ID của chữ Kanji")
    @JsonProperty("kanji_id")
    private Long kanjiId;

    // Field này chỉ dùng để hiển thị tên Kanji cho tiện (Output only)
    @JsonProperty("kanji_text")
    private String kanjiText;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("create_at")
    private LocalDateTime createAt;

    @JsonProperty("create_by")
    private Integer createBy; // ID người tạo

    @JsonProperty("edit_by")
    private Integer editBy;   // ID người sửa
}