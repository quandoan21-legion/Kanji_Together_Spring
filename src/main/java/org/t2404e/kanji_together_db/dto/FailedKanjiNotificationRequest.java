package org.t2404e.kanji_together_db.dto;

import lombok.Data;

@Data
public class FailedKanjiNotificationRequest {
    private Long userId;
    private String title;
    private Integer maxKanji;
    private String deviceToken;
    private String accessToken;
}
