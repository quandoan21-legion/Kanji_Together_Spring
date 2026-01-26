package org.t2404e.kanji_together_db.service.notifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.t2404e.kanji_together_db.config.FailedKanjiNotificationProperties;
import org.t2404e.kanji_together_db.dto.FailedKanjiCountItem;
import org.t2404e.kanji_together_db.dto.FailedKanjiNotificationRequest;
import org.t2404e.kanji_together_db.dto.FailedKanjiNotificationResponse;
import org.t2404e.kanji_together_db.entity.UserDeviceTokens;
import org.t2404e.kanji_together_db.entity.Users;
import org.t2404e.kanji_together_db.repository.ExamAttemptAnswersRepository;
import org.t2404e.kanji_together_db.repository.UserDeviceTokensRepository;
import org.t2404e.kanji_together_db.security.CustomUserDetails;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FailedKanjiNotificationService {

    @Autowired
    private ExamAttemptAnswersRepository examAttemptAnswersRepository;

    @Autowired
    private UserDeviceTokensRepository userDeviceTokensRepository;

    @Autowired
    private PushNotificationService pushNotificationService;

    @Autowired
    private FailedKanjiNotificationProperties properties;

    public FailedKanjiNotificationResponse sendSummary(FailedKanjiNotificationRequest request) {
        FailedKanjiNotificationRequest resolvedRequest = request != null ? request : new FailedKanjiNotificationRequest();
        Users currentUser = resolveCurrentUser();
        Long userId = resolveUserId(resolvedRequest.getUserId(), currentUser);
        FailedKanjiNotificationResponse response = new FailedKanjiNotificationResponse();
        response.setUserId(userId);
        response.setTitle(resolveTitle(resolvedRequest.getTitle()));

        if (userId == null) {
            response.setBody("Missing userId.");
            response.setSentTo(0);
            response.setItems(List.of());
            return response;
        }

        List<ExamAttemptAnswersRepository.KanjiFailCount> counts =
                examAttemptAnswersRepository.findFailedKanjiCountsByUserId(userId);

        List<FailedKanjiCountItem> items = counts.stream()
                .map(this::toItem)
                .collect(Collectors.toList());
        response.setItems(items);

        if (items.isEmpty()) {
            response.setBody("No failed kanji found.");
            response.setSentTo(0);
            return response;
        }

        int maxKanji = resolveMaxKanji(resolvedRequest.getMaxKanji());
        String body = buildSummaryBody(items, maxKanji);
        response.setBody(body);

        List<String> tokens = resolveTokens(resolvedRequest.getDeviceToken(), userId);

        if (tokens.isEmpty()) {
            response.setSentTo(0);
            return response;
        }

        String accessTokenOverride = resolveAccessTokenOverride(resolvedRequest.getAccessToken());
        if (accessTokenOverride != null) {
            pushNotificationService.sendToTokens(
                    userId,
                    tokens,
                    response.getTitle(),
                    response.getBody(),
                    Map.of("type", "failed_kanji_summary"),
                    accessTokenOverride
            );
        } else {
            pushNotificationService.sendToTokens(
                    userId,
                    tokens,
                    response.getTitle(),
                    response.getBody(),
                    Map.of("type", "failed_kanji_summary")
            );
        }

        response.setSentTo(tokens.size());
        return response;
    }

    private FailedKanjiCountItem toItem(ExamAttemptAnswersRepository.KanjiFailCount count) {
        FailedKanjiCountItem item = new FailedKanjiCountItem();
        item.setKanjiId(count.getKanjiId());
        item.setKanji(count.getKanji());
        item.setFailCount(count.getFailCount());
        return item;
    }

    private String buildSummaryBody(List<FailedKanjiCountItem> items, int maxKanji) {
        int limit = Math.max(1, maxKanji);
        List<FailedKanjiCountItem> shown = items.subList(0, Math.min(limit, items.size()));
        String body = shown.stream()
                .map(item -> item.getKanji() + "x" + item.getFailCount())
                .collect(Collectors.joining(", "));
        int remaining = items.size() - shown.size();
        if (remaining > 0) {
            return "Failed kanji: " + body + " and " + remaining + " more.";
        }
        return "Failed kanji: " + body;
    }

    private int resolveMaxKanji(Integer requestMax) {
        if (requestMax != null && requestMax > 0) {
            return requestMax;
        }
        return Math.max(1, properties.getMaxKanji());
    }

    private List<String> resolveTokens(String deviceToken, Long userId) {
        if (deviceToken != null && !deviceToken.trim().isEmpty()) {
            return List.of(deviceToken.trim());
        }
        return userDeviceTokensRepository
                .findByUser_IdInAndIsActiveTrue(List.of(userId))
                .stream()
                .map(UserDeviceTokens::getToken)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .collect(Collectors.toList());
    }

    private String resolveAccessTokenOverride(String accessToken) {
        if (accessToken != null && !accessToken.trim().isEmpty()) {
            return accessToken.trim();
        }
        return null;
    }

    private String resolveTitle(String requestTitle) {
        if (requestTitle != null && !requestTitle.trim().isEmpty()) {
            return requestTitle.trim();
        }
        return "Kanji Together";
    }

    private Long resolveUserId(Long requestedUserId, Users currentUser) {
        if (currentUser == null) {
            return requestedUserId;
        }

        if (!isAdmin() && requestedUserId != null && !Objects.equals(requestedUserId, currentUser.getId())) {
            return currentUser.getId();
        }

        return requestedUserId != null ? requestedUserId : currentUser.getId();
    }

    private Users resolveCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUser();
        }
        return null;
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if ("ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }
}
