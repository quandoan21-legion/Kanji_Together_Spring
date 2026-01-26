package org.t2404e.kanji_together_db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.t2404e.kanji_together_db.dto.KanjiAttemptItemResponse;
import org.t2404e.kanji_together_db.dto.KanjiAttemptsResponse;
import org.t2404e.kanji_together_db.entity.ExamAttemptAnswers;
import org.t2404e.kanji_together_db.entity.Questions;
import org.t2404e.kanji_together_db.entity.Users;
import org.t2404e.kanji_together_db.repository.ExamAttemptAnswersRepository;
import org.t2404e.kanji_together_db.security.CustomUserDetails;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class KanjiAttemptAnswersService {

    @Autowired
    private ExamAttemptAnswersRepository examAttemptAnswersRepository;

    public KanjiAttemptsResponse getAttempts(Long kanjiId, Long requestedUserId) {
        Users currentUser = resolveCurrentUser();
        Long resolvedUserId = resolveUserId(requestedUserId, currentUser);

        List<ExamAttemptAnswers> attempts = examAttemptAnswersRepository
                .findByUserIdAndKanjiId(resolvedUserId, kanjiId);

        List<KanjiAttemptItemResponse> items = attempts.stream()
                .map(this::toResponseItem)
                .collect(Collectors.toList());

        KanjiAttemptsResponse response = new KanjiAttemptsResponse();
        response.setKanjiId(kanjiId);
        response.setUserId(resolvedUserId);
        response.setAttempts(items);
        return response;
    }

    private Long resolveUserId(Long requestedUserId, Users currentUser) {
        if (currentUser == null) {
            if (requestedUserId == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
            }
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

    private KanjiAttemptItemResponse toResponseItem(ExamAttemptAnswers attempt) {
        Questions question = attempt.question;
        KanjiAttemptItemResponse item = new KanjiAttemptItemResponse();
        item.setQuestionId(question != null ? question.getId() : null);
        item.setQuestionText(question != null ? question.getQuestionText() : null);
        item.setExamResultId(attempt.examResult != null ? attempt.examResult.getId() : null);
        item.setSelectedAnswer(resolveSelectedAnswerText(question, attempt.selectedAnswerId));
        item.setIsCorrect(resolveIsCorrect(attempt, question));
        item.setCreatedAt(attempt.answered_at);
        return item;
    }

    private String resolveSelectedAnswerText(Questions question, Integer selectedAnswerId) {
        if (question == null || selectedAnswerId == null) {
            return null;
        }
        return switch (selectedAnswerId) {
            case 1 -> question.getCorrectAnswer();
            case 2 -> question.getWrongAnswer1();
            case 3 -> question.getWrongAnswer2();
            case 4 -> question.getWrongAnswer3();
            default -> null;
        };
    }

    private Boolean resolveIsCorrect(ExamAttemptAnswers attempt, Questions question) {
        if (attempt.is_correct != null) {
            return attempt.is_correct;
        }
        if (question == null || attempt.selectedAnswerId == null) {
            return null;
        }
        String selectedAnswer = resolveSelectedAnswerText(question, attempt.selectedAnswerId);
        return selectedAnswer != null && selectedAnswer.equals(question.getCorrectAnswer());
    }
}
