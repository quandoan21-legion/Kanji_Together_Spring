package org.t2404e.kanji_together_db.service.notifications;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.t2404e.kanji_together_db.dto.KanjiMasteryView;
import org.t2404e.kanji_together_db.dto.NotificationLogDTO;
import org.t2404e.kanji_together_db.dto.NotificationSendResult;
import org.t2404e.kanji_together_db.entity.KanjiCharacters;
import org.t2404e.kanji_together_db.entity.NotificationLog;
import org.t2404e.kanji_together_db.entity.UserDeviceTokens;
import org.t2404e.kanji_together_db.entity.Users;
import org.t2404e.kanji_together_db.enums.NotificationStatus;
import org.t2404e.kanji_together_db.repository.KanjiCharactersRepository;
import org.t2404e.kanji_together_db.repository.NotificationLogRepository;
import org.t2404e.kanji_together_db.repository.UserDeviceTokensRepository;
import org.t2404e.kanji_together_db.repository.UserKanjiMasteryRepository;
import org.t2404e.kanji_together_db.repository.UsersRepository;
import org.t2404e.kanji_together_db.service.UserAttemptService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ReviewDueNotificationService {
    private final UserKanjiMasteryRepository userKanjiMasteryRepository;
    private final UserDeviceTokensRepository userDeviceTokensRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final KanjiCharactersRepository kanjiCharactersRepository;
    private final UsersRepository usersRepository;
    private final UserAttemptService userAttemptService;
    private final FcmPushNotificationService fcmPushNotificationService;

    @Value("${notifications.review-due.dedup-hours:23}")
    private int dedupHours;

    @Value("${notifications.review-due.max-kanji:20}")
    private int maxKanji;

    @Value("${notifications.review-due.title:Kanji review reminder}")
    private String title;

    public ReviewDueNotificationService(UserKanjiMasteryRepository userKanjiMasteryRepository,
                                        UserDeviceTokensRepository userDeviceTokensRepository,
                                        NotificationLogRepository notificationLogRepository,
                                        KanjiCharactersRepository kanjiCharactersRepository,
                                        UsersRepository usersRepository,
                                        UserAttemptService userAttemptService,
                                        FcmPushNotificationService fcmPushNotificationService) {
        this.userKanjiMasteryRepository = userKanjiMasteryRepository;
        this.userDeviceTokensRepository = userDeviceTokensRepository;
        this.notificationLogRepository = notificationLogRepository;
        this.kanjiCharactersRepository = kanjiCharactersRepository;
        this.usersRepository = usersRepository;
        this.userAttemptService = userAttemptService;
        this.fcmPushNotificationService = fcmPushNotificationService;
    }

    public int sendDueNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<Long> userIds = userKanjiMasteryRepository.findDistinctUserIdsDue(now);
        int sentCount = 0;
        for (Long userId : userIds) {
            NotificationSendResult result = sendDueForUser(userId, false);
            if (result.getSent() > 0) {
                sentCount++;
            }
        }
        return sentCount;
    }

    public NotificationSendResult sendDueForUser(Long userId, boolean ignoreDedup) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing userId");
        }
        NotificationSendResult result = new NotificationSendResult();
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy user"));

        List<KanjiMasteryView> due = userAttemptService.getDueMastery(userId, maxKanji);
        if (due.isEmpty()) {
            result.setStatus("skipped");
            result.setSkippedReason("no_due_items");
            return result;
        }

        List<Long> kanjiIds = due.stream()
                .map(KanjiMasteryView::getKanjiId)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
        if (kanjiIds.isEmpty()) {
            result.setStatus("skipped");
            result.setSkippedReason("no_kanji_ids");
            return result;
        }

        String kanjiIdString = kanjiIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        String kanjiHash = sha256Hex(kanjiIdString);

        if (!ignoreDedup && isDuplicate(userId, kanjiHash)) {
            result.setStatus("skipped");
            result.setSkippedReason("dedup");
            return result;
        }

        List<UserDeviceTokens> tokens = userDeviceTokensRepository.findByUser_IdAndIsActiveTrue(userId);
        List<String> fcmTokens = tokens.stream()
                .map(UserDeviceTokens::getFcmToken)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .collect(Collectors.toList());
        if (fcmTokens.isEmpty()) {
            result.setStatus("skipped");
            result.setSkippedReason("no_tokens");
            return result;
        }

        String body = buildReminderBody(kanjiIds);
        Map<String, String> data = new HashMap<>();
        data.put("type", "review_due");
        data.put("kanji_ids", kanjiIdString);
        data.put("count", String.valueOf(kanjiIds.size()));

        List<NotificationLog> pendingLogs = new ArrayList<>();
        for (String token : fcmTokens) {
            NotificationLog log = new NotificationLog();
            log.setUser(user);
            log.setFcmToken(token);
            log.setKanjiIds(kanjiIdString);
            log.setKanjiHash(kanjiHash);
            log.setStatus(NotificationStatus.PENDING);
            pendingLogs.add(log);
        }
        notificationLogRepository.saveAll(pendingLogs);

        FcmBatchResult batchResult = fcmPushNotificationService.sendMulticast(userId, fcmTokens, title, body, data);
        Map<String, NotificationStatus> statuses = batchResult.getStatuses();
        Map<String, String> errors = batchResult.getErrors();

        for (NotificationLog log : pendingLogs) {
            NotificationStatus status = statuses.getOrDefault(log.getFcmToken(), NotificationStatus.FAILED);
            log.setStatus(status);
            if (status == NotificationStatus.FAILED) {
                log.setErrorMessage(errors.get(log.getFcmToken()));
            }
        }
        notificationLogRepository.saveAll(pendingLogs);

        int sent = (int) statuses.values().stream().filter(s -> s == NotificationStatus.SENT).count();
        int failed = (int) statuses.values().stream().filter(s -> s == NotificationStatus.FAILED).count();
        result.setAttempted(fcmTokens.size());
        result.setSent(sent);
        result.setFailed(failed);
        result.setErrors(errors);
        if (batchResult.getSkippedReason() != null) {
            result.setStatus("skipped");
            result.setSkippedReason(batchResult.getSkippedReason());
        } else if (sent > 0) {
            result.setStatus("sent");
        } else {
            result.setStatus("failed");
        }
        return result;
    }

    public NotificationSendResult sendTestNotification(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing userId");
        }
        NotificationSendResult result = new NotificationSendResult();
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy user"));
        List<UserDeviceTokens> tokens = userDeviceTokensRepository.findByUser_IdAndIsActiveTrue(userId);
        List<String> fcmTokens = tokens.stream()
                .map(UserDeviceTokens::getFcmToken)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .collect(Collectors.toList());
        if (fcmTokens.isEmpty()) {
            result.setStatus("skipped");
            result.setSkippedReason("no_tokens");
            return result;
        }

        String body = "This is a test notification.";
        Map<String, String> data = Map.of("type", "test");
        List<NotificationLog> pendingLogs = new ArrayList<>();
        for (String token : fcmTokens) {
            NotificationLog log = new NotificationLog();
            log.setUser(user);
            log.setFcmToken(token);
            log.setKanjiIds("TEST");
            log.setKanjiHash(sha256Hex("TEST"));
            log.setStatus(NotificationStatus.PENDING);
            pendingLogs.add(log);
        }
        notificationLogRepository.saveAll(pendingLogs);

        FcmBatchResult batchResult = fcmPushNotificationService.sendMulticast(userId, fcmTokens, title, body, data);
        Map<String, NotificationStatus> statuses = batchResult.getStatuses();
        Map<String, String> errors = batchResult.getErrors();
        for (NotificationLog log : pendingLogs) {
            NotificationStatus status = statuses.getOrDefault(log.getFcmToken(), NotificationStatus.FAILED);
            log.setStatus(status);
            if (status == NotificationStatus.FAILED) {
                log.setErrorMessage(errors.get(log.getFcmToken()));
            }
        }
        notificationLogRepository.saveAll(pendingLogs);
        int sent = (int) statuses.values().stream().filter(s -> s == NotificationStatus.SENT).count();
        int failed = (int) statuses.values().stream().filter(s -> s == NotificationStatus.FAILED).count();
        result.setAttempted(fcmTokens.size());
        result.setSent(sent);
        result.setFailed(failed);
        result.setErrors(errors);
        if (batchResult.getSkippedReason() != null) {
            result.setStatus("skipped");
            result.setSkippedReason(batchResult.getSkippedReason());
        } else if (sent > 0) {
            result.setStatus("sent");
        } else {
            result.setStatus("failed");
        }
        return result;
    }

    public List<NotificationLogDTO> getHistory(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing userId");
        }
        return notificationLogRepository.findByUser_IdOrderByCreateAtDesc(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private NotificationLogDTO toDto(NotificationLog log) {
        NotificationLogDTO dto = new NotificationLogDTO();
        dto.setId(log.getId());
        dto.setUserId(log.getUser() != null ? log.getUser().getId() : null);
        dto.setFcmToken(log.getFcmToken());
        dto.setKanjiIds(log.getKanjiIds());
        dto.setKanjiHash(log.getKanjiHash());
        dto.setStatus(log.getStatus());
        dto.setErrorMessage(log.getErrorMessage());
        dto.setCreateAt(log.getCreateAt());
        return dto;
    }

    private boolean isDuplicate(Long userId, String kanjiHash) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(dedupHours);
        return notificationLogRepository.existsByUser_IdAndKanjiHashAndCreateAtAfter(userId, kanjiHash, cutoff);
    }

    private String buildReminderBody(List<Long> kanjiIds) {
        List<KanjiCharacters> kanjiList = kanjiCharactersRepository.findAllById(kanjiIds);
        List<String> kanjiChars = kanjiList.stream()
                .map(KanjiCharacters::getKanji)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        int count = kanjiIds.size();
        if (kanjiChars.isEmpty()) {
            return "You need to practice this kanji now.";
        }
        int limit = Math.max(1, Math.min(maxKanji, kanjiChars.size()));
        String shown = String.join(", ", kanjiChars.subList(0, limit));
        int remaining = kanjiChars.size() - limit;
        if (remaining > 0) {
            return "You need to practice this kanji now: " + shown + " and " + remaining + " more.";
        }
        return "You need to practice this kanji now: " + shown + ".";
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                String part = Integer.toHexString(0xff & b);
                if (part.length() == 1) {
                    hex.append('0');
                }
                hex.append(part);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Missing SHA-256", e);
        }
    }
}
