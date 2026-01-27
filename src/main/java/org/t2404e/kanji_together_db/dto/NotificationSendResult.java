package org.t2404e.kanji_together_db.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class NotificationSendResult {
    private String status;

    @JsonProperty("attempted")
    private int attempted;

    @JsonProperty("sent")
    private int sent;

    @JsonProperty("failed")
    private int failed;

    @JsonProperty("skipped_reason")
    private String skippedReason;

    private Map<String, String> errors;
}
