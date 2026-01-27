package org.t2404e.kanji_together_db.service.notifications;

import org.t2404e.kanji_together_db.enums.NotificationStatus;

import java.util.Map;

public class FcmBatchResult {
    private final Map<String, NotificationStatus> statuses;
    private final Map<String, String> errors;
    private final String skippedReason;

    public FcmBatchResult(Map<String, NotificationStatus> statuses, Map<String, String> errors, String skippedReason) {
        this.statuses = statuses;
        this.errors = errors;
        this.skippedReason = skippedReason;
    }

    public Map<String, NotificationStatus> getStatuses() {
        return statuses;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public String getSkippedReason() {
        return skippedReason;
    }
}
