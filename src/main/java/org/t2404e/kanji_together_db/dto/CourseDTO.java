package org.t2404e.kanji_together_db.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CourseDTO {
    private Long id;
    private String name;
    private String description;
    private String category;

    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;

    @JsonProperty("cover_image_url")
    private String coverImageUrl;

    @JsonProperty("time_to_finish")
    private String timeToFinish;

    @JsonProperty("lesson_ids")
    private List<Long> lessonIds;
}
