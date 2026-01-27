package org.t2404e.kanji_together_db.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.t2404e.kanji_together_db.dto.AttemptRequest;
import org.t2404e.kanji_together_db.dto.AttemptResponse;
import org.t2404e.kanji_together_db.dto.KanjiMasteryView;
import org.t2404e.kanji_together_db.entity.KanjiCharacters;
import org.t2404e.kanji_together_db.entity.Questions;
import org.t2404e.kanji_together_db.entity.UserKanjiAttempt;
import org.t2404e.kanji_together_db.entity.UserKanjiMastery;
import org.t2404e.kanji_together_db.entity.UserQuestionAttempt;
import org.t2404e.kanji_together_db.entity.Users;
import org.t2404e.kanji_together_db.exception.CustomValidationException;
import org.t2404e.kanji_together_db.repository.KanjiCharactersRepository;
import org.t2404e.kanji_together_db.repository.QuestionsRepository;
import org.t2404e.kanji_together_db.repository.UserKanjiAttemptRepository;
import org.t2404e.kanji_together_db.repository.UserKanjiMasteryRepository;
import org.t2404e.kanji_together_db.repository.UserQuestionAttemptRepository;
import org.t2404e.kanji_together_db.repository.UsersRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserAttemptService {
    private final UsersRepository usersRepository;
    private final QuestionsRepository questionsRepository;
    private final KanjiCharactersRepository kanjiCharactersRepository;
    private final UserQuestionAttemptRepository userQuestionAttemptRepository;
    private final UserKanjiAttemptRepository userKanjiAttemptRepository;
    private final UserKanjiMasteryRepository userKanjiMasteryRepository;

    public UserAttemptService(UsersRepository usersRepository,
                              QuestionsRepository questionsRepository,
                              KanjiCharactersRepository kanjiCharactersRepository,
                              UserQuestionAttemptRepository userQuestionAttemptRepository,
                              UserKanjiAttemptRepository userKanjiAttemptRepository,
                              UserKanjiMasteryRepository userKanjiMasteryRepository) {
        this.usersRepository = usersRepository;
        this.questionsRepository = questionsRepository;
        this.kanjiCharactersRepository = kanjiCharactersRepository;
        this.userQuestionAttemptRepository = userQuestionAttemptRepository;
        this.userKanjiAttemptRepository = userKanjiAttemptRepository;
        this.userKanjiMasteryRepository = userKanjiMasteryRepository;
    }

    @Transactional
    public AttemptResponse submitOne(AttemptRequest request) {
        validateRequest(request);

        Users user = usersRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));

        Questions question = questionsRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy câu hỏi"));

        LocalDateTime answeredAt = request.getAnsweredAt() != null ? request.getAnsweredAt() : LocalDateTime.now();

        UserQuestionAttempt questionAttempt = new UserQuestionAttempt();
        questionAttempt.setUser(user);
        questionAttempt.setQuestion(question);
        questionAttempt.setIsCorrect(request.getCorrect());
        questionAttempt.setSelectedAnswer(request.getSelectedAnswer());
        questionAttempt.setTimeSpentMs(request.getTimeSpentMs());
        questionAttempt.setAnsweredAt(answeredAt);
        UserQuestionAttempt savedAttempt = userQuestionAttemptRepository.save(questionAttempt);

        List<Long> kanjiIds = questionsRepository.findKanjiIdsByQuestionId(question.getId());
        List<KanjiMasteryView> masteryViews = new ArrayList<>();

        for (Long kanjiId : kanjiIds) {
            KanjiCharacters kanji = kanjiCharactersRepository.findById(kanjiId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Kanji"));

            UserKanjiAttempt kanjiAttempt = new UserKanjiAttempt();
            kanjiAttempt.setUser(user);
            kanjiAttempt.setKanji(kanji);
            kanjiAttempt.setQuestionAttempt(savedAttempt);
            kanjiAttempt.setIsCorrect(request.getCorrect());
            kanjiAttempt.setAnsweredAt(answeredAt);
            userKanjiAttemptRepository.save(kanjiAttempt);

            UserKanjiMastery mastery = upsertAndApplyMastery(user, kanji, request.getCorrect(), answeredAt);
            masteryViews.add(toView(mastery));
        }

        AttemptResponse response = new AttemptResponse();
        response.setQuestionAttemptId(savedAttempt.getId());
        response.setUpdatedKanji(masteryViews);
        return response;
    }

    @Transactional
    public List<AttemptResponse> submitBatch(List<AttemptRequest> attempts) {
        if (attempts == null || attempts.isEmpty()) {
            throw new CustomValidationException(Map.of("attempts", "Vui lòng nhập danh sách attempts"));
        }

        List<AttemptResponse> responses = new ArrayList<>();
        for (AttemptRequest request : attempts) {
            responses.add(submitOne(request));
        }
        return responses;
    }

    public UserKanjiMastery getMastery(Long userId, Long kanjiId) {
        return userKanjiMasteryRepository.findByUser_IdAndKanji_Id(userId, kanjiId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy mastery"));
    }

    public List<KanjiMasteryView> getDueMastery(Long userId, int limit) {
        if (limit <= 0) {
            throw new CustomValidationException(Map.of("limit", "limit phải lớn hơn 0"));
        }

        List<UserKanjiMastery> due = userKanjiMasteryRepository
                .findByUser_IdAndNextReviewAtLessThanEqualOrderByNextReviewAtAsc(
                        userId,
                        LocalDateTime.now(),
                        PageRequest.of(0, limit)
                );

        List<KanjiMasteryView> responses = new ArrayList<>();
        for (UserKanjiMastery mastery : due) {
            responses.add(toView(mastery));
        }
        return responses;
    }

    private UserKanjiMastery upsertAndApplyMastery(Users user, KanjiCharacters kanji, boolean correct, LocalDateTime now) {
        UserKanjiMastery mastery = userKanjiMasteryRepository.findForUpdate(user.getId(), kanji.getId())
                .orElse(null);

        if (mastery == null) {
            mastery = createDefaultMastery(user, kanji, now);
            try {
                userKanjiMasteryRepository.saveAndFlush(mastery);
            } catch (DataIntegrityViolationException ex) {
                mastery = userKanjiMasteryRepository.findForUpdate(user.getId(), kanji.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Xung đột tạo mastery"));
            }
        }

        SpacedRepetitionCalculator.apply(mastery, correct, now);
        return userKanjiMasteryRepository.save(mastery);
    }

    private UserKanjiMastery createDefaultMastery(Users user, KanjiCharacters kanji, LocalDateTime now) {
        UserKanjiMastery mastery = new UserKanjiMastery();
        mastery.setUser(user);
        mastery.setKanji(kanji);
        mastery.setEaseFactor(2.5);
        mastery.setIntervalDays(1);
        mastery.setRepetitions(0);
        mastery.setMasteryLevel(0);
        mastery.setTotalCorrect(0);
        mastery.setTotalWrong(0);
        mastery.setNextReviewAt(now.plusDays(1));
        mastery.setUpdatedAt(now);
        return mastery;
    }

    private KanjiMasteryView toView(UserKanjiMastery mastery) {
        KanjiMasteryView view = new KanjiMasteryView();
        view.setKanjiId(mastery.getKanji().getId());
        view.setMasteryLevel(mastery.getMasteryLevel());
        view.setEaseFactor(mastery.getEaseFactor());
        view.setIntervalDays(mastery.getIntervalDays());
        view.setRepetitions(mastery.getRepetitions());
        view.setNextReviewAt(mastery.getNextReviewAt());
        return view;
    }

    private void validateRequest(AttemptRequest request) {
        Map<String, String> errors = new HashMap<>();

        if (request == null) {
            errors.put("request", "Vui lòng nhập dữ liệu");
        } else {
            if (request.getUserId() == null) {
                errors.put("user_id", "Vui lòng nhập user_id");
            }
            if (request.getQuestionId() == null) {
                errors.put("question_id", "Vui lòng nhập question_id");
            }
            if (request.getCorrect() == null) {
                errors.put("correct", "Vui lòng nhập correct");
            }
            if (request.getTimeSpentMs() != null && request.getTimeSpentMs() < 0) {
                errors.put("time_spent_ms", "Thời gian làm bài không hợp lệ");
            }
        }

        if (!errors.isEmpty()) {
            throw new CustomValidationException(errors);
        }
    }
}
