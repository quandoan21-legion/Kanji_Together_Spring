package org.t2404e.kanji_together_db.dto;

import lombok.Data;
import java.util.List;

@Data
public class FailedKanjiNotificationResponse {
    private Long userId;
    private String title;
    private String body;
    private Integer sentTo;
    private List<FailedKanjiCountItem> items;

}
