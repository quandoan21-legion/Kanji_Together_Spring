package org.t2404e.kanji_together_db.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.t2404e.kanji_together_db.dto.NotificationLogDTO;
import org.t2404e.kanji_together_db.dto.NotificationSendResult;
import org.t2404e.kanji_together_db.service.notifications.ReviewDueNotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final ReviewDueNotificationService reviewDueNotificationService;

    public NotificationController(ReviewDueNotificationService reviewDueNotificationService) {
        this.reviewDueNotificationService = reviewDueNotificationService;
    }

    @PostMapping("/send/{userId}")
    public ResponseEntity<NotificationSendResult> sendDue(@PathVariable Long userId,
                                                          @RequestParam(name = "ignoreDedup", defaultValue = "false") boolean ignoreDedup) {
        return ResponseEntity.ok(reviewDueNotificationService.sendDueForUser(userId, ignoreDedup));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<NotificationLogDTO>> history(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewDueNotificationService.getHistory(userId));
    }

    @GetMapping("/test/{userId}")
    public ResponseEntity<NotificationSendResult> test(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewDueNotificationService.sendTestNotification(userId));
    }
}
