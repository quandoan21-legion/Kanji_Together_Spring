package org.t2404e.kanji_together_db.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KanjiMasteryView {
    private Long kanjiId;
    private int masteryLevel;
    private double easeFactor;
    private int intervalDays;
    private int repetitions;
    private LocalDateTime nextReviewAt;
}
