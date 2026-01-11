package org.t2404e.kanji_together_db.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KanjiCharacterDTO {
    private Long id;

    @NotBlank(message = "Chữ Kanji không được để trống")
    private String kanji;

    @JsonProperty("on_pronunciation")
    private String onPronunciation;

    @JsonProperty("kun_pronunciation")
    private String kunPronunciation;

    @NotNull(message = "Số nét không được để trống")
    @JsonProperty("num_strokes")
    private Integer numStrokes;

    @NotNull(message = "Cấp độ JLPT không được để trống")
    @JsonProperty("jlpt")
    private Integer jlpt;

    @JsonProperty("kanji_description")
    private String kanjiDescription;

    @JsonProperty("translation")
    private String translation;

    @JsonProperty("create_at")
    private LocalDateTime createAt;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("create_by")
    private Integer createBy;

    @JsonProperty("edit_by")
    private Integer editBy;
}