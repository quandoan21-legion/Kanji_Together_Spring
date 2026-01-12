package org.t2404e.kanji_together_db.dto;

import lombok.Data;

@Data
public class AiStoryRequest {
    private String kanji;
    private String custom_prompt;
}