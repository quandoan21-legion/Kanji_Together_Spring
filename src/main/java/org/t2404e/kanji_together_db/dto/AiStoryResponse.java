package org.t2404e.kanji_together_db.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiStoryResponse {
    private String kanji;
    private String meaning;
    private String story;
    private String jlpt_level;
}