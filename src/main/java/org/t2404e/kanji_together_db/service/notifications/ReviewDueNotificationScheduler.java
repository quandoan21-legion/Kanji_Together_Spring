package org.t2404e.kanji_together_db.service.notifications;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ReviewDueNotificationScheduler {
    private final ReviewDueNotificationService reviewDueNotificationService;

    @Value("${notifications.review-due.enabled:true}")
    private boolean enabled;

    public ReviewDueNotificationScheduler(ReviewDueNotificationService reviewDueNotificationService) {
        this.reviewDueNotificationService = reviewDueNotificationService;
    }

    @Scheduled(cron = "${notifications.review-due.cron:0 0 * * * *}")
    public void sendDueReviews() {
        if (!enabled) {
            return;
        }
        reviewDueNotificationService.sendDueNotifications();
    }
}
