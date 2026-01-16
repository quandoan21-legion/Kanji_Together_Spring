package org.t2404e.kanji_together_db.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
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

    @JsonProperty("num_strokes")
    private Integer numStrokes;

    @JsonProperty("jlpt")
    private Integer jlpt;

    @JsonProperty("kanji_description")
    private String kanjiDescription;

    @NotBlank(message = "Hán Việt không được để trống")
    private String translation;

    @NotBlank(message = "Nghĩa không được để trống")
    private String meaning;

    private String radical;

    private String components;

    @JsonProperty("writing_image_url")
    private String writingImageUrl;

    @JsonProperty("create_at")
    private LocalDateTime createAt;

    @JsonProperty("is_active")
    private Boolean isActive = true;

    @JsonProperty("create_by")
    private Integer createBy;

    @JsonProperty("edit_by")
    private Integer editBy;
}