package org.t2404e.kanji_together_db.dto;

import java.util.List;

public class KanjiAttemptsResponse {
    private Long kanjiId;
    private Long userId;
    private List<KanjiAttemptItemResponse> attempts;

    public Long getKanjiId() {
        return kanjiId;
    }

    public void setKanjiId(Long kanjiId) {
        this.kanjiId = kanjiId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<KanjiAttemptItemResponse> getAttempts() {
        return attempts;
    }

    public void setAttempts(List<KanjiAttemptItemResponse> attempts) {
        this.attempts = attempts;
    }
}
