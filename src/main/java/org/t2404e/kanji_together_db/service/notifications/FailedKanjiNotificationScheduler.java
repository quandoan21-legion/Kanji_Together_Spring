package org.t2404e.kanji_together_db.service.notifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.t2404e.kanji_together_db.config.FailedKanjiNotificationProperties;
import org.t2404e.kanji_together_db.entity.ExamAttemptAnswers;
import org.t2404e.kanji_together_db.entity.KanjiCharacters;
import org.t2404e.kanji_together_db.entity.UserDeviceTokens;
import org.t2404e.kanji_together_db.repository.ExamAttemptAnswersRepository;
import org.t2404e.kanji_together_db.repository.UserDeviceTokensRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FailedKanjiNotificationScheduler {

    @Autowired
    private ExamAttemptAnswersRepository examAttemptAnswersRepository;

    @Autowired
    private UserDeviceTokensRepository userDeviceTokensRepository;

    @Autowired
    private PushNotificationService pushNotificationService;

    @Autowired
    private FailedKanjiNotificationProperties properties;

    @Scheduled(cron = "${notifications.failed-kanji.cron:0 */5 * * * *}")
    public void sendFailedKanjiSummary() {
        if (!properties.isEnabled()) {
            return;
        }

        LocalDateTime since = LocalDateTime.now().minusHours(properties.getRecentHours());
        List<ExamAttemptAnswers> attempts = examAttemptAnswersRepository.findFailedAttemptsSince(since);
        if (attempts.isEmpty()) {
            return;
        }

        Map<Long, Set<String>> kanjiByUser = new HashMap<>();
        for (ExamAttemptAnswers attempt : attempts) {
            if (attempt.user == null || attempt.question == null || attempt.question.getKanjiCharacters() == null) {
                continue;
            }
            Set<String> kanjiSet = kanjiByUser.computeIfAbsent(attempt.user.getId(), k -> new HashSet<>());
            for (KanjiCharacters kanji : attempt.question.getKanjiCharacters()) {
                if (kanji != null && kanji.getKanji() != null && !kanji.getKanji().isBlank()) {
                    kanjiSet.add(kanji.getKanji().trim());
                }
            }
        }

        if (kanjiByUser.isEmpty()) {
            return;
        }

        List<Long> userIds = new ArrayList<>(kanjiByUser.keySet());
        List<UserDeviceTokens> tokens = userDeviceTokensRepository.findByUser_IdInAndIsActiveTrue(userIds);
        Map<Long, List<String>> tokensByUser = tokens.stream()
                .filter(token -> token.getUser() != null && token.getUser().getId() != null)
                .collect(Collectors.groupingBy(
                        token -> token.getUser().getId(),
                        Collectors.mapping(UserDeviceTokens::getToken, Collectors.toList())
                ));

        for (Map.Entry<Long, Set<String>> entry : kanjiByUser.entrySet()) {
            Long userId = entry.getKey();
            List<String> userTokens = tokensByUser.getOrDefault(userId, List.of());
            if (userTokens.isEmpty()) {
                continue;
            }
            String body = buildSummaryBody(entry.getValue(), properties.getMaxKanji());
            pushNotificationService.sendToTokens(
                    userId,
                    userTokens,
                    "Kanji review reminder",
                    body,
                    Map.of("type", "failed_kanji_summary")
            );
        }
    }

    private String buildSummaryBody(Set<String> kanjiSet, int maxKanji) {
        List<String> kanjiList = kanjiSet.stream().sorted().collect(Collectors.toList());
        if (kanjiList.isEmpty()) {
            return "You have some failed kanji to review.";
        }

        int limit = Math.max(1, maxKanji);
        List<String> shown = kanjiList.subList(0, Math.min(limit, kanjiList.size()));
        String base = "You recently missed: " + String.join(", ", shown);
        int remaining = kanjiList.size() - shown.size();
        if (remaining > 0) {
            return base + " and " + remaining + " more.";
        }
        return base + ".";
    }
}
