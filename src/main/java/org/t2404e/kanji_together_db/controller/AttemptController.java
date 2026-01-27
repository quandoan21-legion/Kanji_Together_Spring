package org.t2404e.kanji_together_db.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.t2404e.kanji_together_db.dto.AttemptRequest;
import org.t2404e.kanji_together_db.dto.AttemptResponse;
import org.t2404e.kanji_together_db.dto.BatchAttemptRequest;
import org.t2404e.kanji_together_db.dto.KanjiMasteryView;
import org.t2404e.kanji_together_db.entity.UserKanjiMastery;
import org.t2404e.kanji_together_db.service.UserAttemptService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AttemptController {
    private final UserAttemptService userAttemptService;

    public AttemptController(UserAttemptService userAttemptService) {
        this.userAttemptService = userAttemptService;
    }

    @PostMapping("/attempts")
    public ResponseEntity<AttemptResponse> submitAttempt(@RequestBody AttemptRequest request) {
        return ResponseEntity.ok(userAttemptService.submitOne(request));
    }

    @PostMapping("/attempts/batch")
    public ResponseEntity<List<AttemptResponse>> submitBatch(@RequestBody BatchAttemptRequest request) {
        List<AttemptRequest> attempts = request != null ? request.getAttempts() : null;
        return ResponseEntity.ok(userAttemptService.submitBatch(attempts));
    }

    @GetMapping("/users/{userId}/kanji/{kanjiId}/mastery")
    public ResponseEntity<KanjiMasteryView> getMastery(
            @PathVariable Long userId,
            @PathVariable Long kanjiId
    ) {
        UserKanjiMastery mastery = userAttemptService.getMastery(userId, kanjiId);
        KanjiMasteryView view = new KanjiMasteryView();
        view.setKanjiId(mastery.getKanji().getId());
        view.setMasteryLevel(mastery.getMasteryLevel());
        view.setEaseFactor(mastery.getEaseFactor());
        view.setIntervalDays(mastery.getIntervalDays());
        view.setRepetitions(mastery.getRepetitions());
        view.setNextReviewAt(mastery.getNextReviewAt());
        return ResponseEntity.ok(view);
    }

    @GetMapping("/review/due")
    public ResponseEntity<List<KanjiMasteryView>> getDue(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(userAttemptService.getDueMastery(userId, limit));
    }
}
