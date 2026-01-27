package org.t2404e.kanji_together_db.dto;

import lombok.Data;
import java.util.List;

@Data
public class BatchAttemptRequest {
    private List<AttemptRequest> attempts;
}
