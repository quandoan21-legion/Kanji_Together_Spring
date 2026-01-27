package org.t2404e.kanji_together_db.dto;

import lombok.Data;
import java.util.List;

@Data
public class AttemptResponse {
    private Long questionAttemptId;
    private List<KanjiMasteryView> updatedKanji;
}
