package org.t2404e.kanji_together_db.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.t2404e.kanji_together_db.dto.KanjiAttemptsResponse;
import org.t2404e.kanji_together_db.service.KanjiAttemptAnswersService;

@RestController
@RequestMapping("/api/kanji")
public class KanjiAttemptsController {

    @Autowired
    private KanjiAttemptAnswersService kanjiAttemptAnswersService;

    @GetMapping("/{kanjiId}/attempts")
    public ResponseEntity<KanjiAttemptsResponse> getAttempts(
            @PathVariable Long kanjiId,
            @RequestParam(required = false) Long userId
    ) {
        return ResponseEntity.ok(kanjiAttemptAnswersService.getAttempts(kanjiId, userId));
    }
}
