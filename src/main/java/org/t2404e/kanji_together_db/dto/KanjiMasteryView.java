package org.t2404e.kanji_together_db.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KanjiMasteryView {
    @Schema(description = "ID of the kanji", example = "3001")
    private Long kanjiId;
    @Schema(description = "Current mastery level for the user", example = "3")
    private int masteryLevel;
    @Schema(description = "Spaced repetition ease factor", example = "2.5")
    private double easeFactor;
    @Schema(description = "Days until next review interval", example = "7")
    private int intervalDays;
    @Schema(description = "Number of consecutive successful reviews", example = "4")
    private int repetitions;
    @Schema(description = "Next scheduled review time", example = "2026-02-03T09:30:00")
    private LocalDateTime nextReviewAt;
}
