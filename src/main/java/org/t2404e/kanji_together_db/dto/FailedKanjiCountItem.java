package org.t2404e.kanji_together_db.dto;

import lombok.Data;

@Data
public class FailedKanjiCountItem {
    private Long kanjiId;
    private String kanji;
    private Long failCount;
}
