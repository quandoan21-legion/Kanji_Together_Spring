package org.t2404e.kanji_together_db.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.t2404e.kanji_together_db.dto.FailedKanjiNotificationRequest;
import org.t2404e.kanji_together_db.dto.FailedKanjiNotificationResponse;
import org.t2404e.kanji_together_db.service.notifications.FailedKanjiNotificationService;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationsController {

    @Autowired
    private FailedKanjiNotificationService failedKanjiNotificationService;

    @PostMapping("/failed-kanji")
    public ResponseEntity<FailedKanjiNotificationResponse> sendFailedKanji(
            @RequestBody(required = false) FailedKanjiNotificationRequest request
    ) {
        return ResponseEntity.ok(failedKanjiNotificationService.sendSummary(request));
    }
}
