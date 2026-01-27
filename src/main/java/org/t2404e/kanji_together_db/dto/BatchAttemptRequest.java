package org.t2404e.kanji_together_db.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
public class BatchAttemptRequest {
    @Schema(description = "List of attempts to submit in a single transaction", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<AttemptRequest> attempts;
}
