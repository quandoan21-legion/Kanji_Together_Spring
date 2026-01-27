package org.t2404e.kanji_together_db.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
public class AttemptResponse {
    @Schema(description = "ID of the saved question attempt", example = "98765")
    private Long questionAttemptId;
    @Schema(description = "Updated mastery entries impacted by this attempt")
    private List<KanjiMasteryView> updatedKanji;
}
