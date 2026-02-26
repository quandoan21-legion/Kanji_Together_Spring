package org.t2404e.kanji_together_db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.t2404e.kanji_together_db.dto.ExamAttemptAnswerRequest;
import org.t2404e.kanji_together_db.dto.ExamAttemptAnswerResponse;
import org.t2404e.kanji_together_db.entity.ExamAttemptAnswers;
import org.t2404e.kanji_together_db.entity.ExamResults;
import org.t2404e.kanji_together_db.entity.KanjiCharacters;
import org.t2404e.kanji_together_db.entity.Questions;
import org.t2404e.kanji_together_db.entity.Users;
import org.t2404e.kanji_together_db.exception.CustomValidationException;
import org.t2404e.kanji_together_db.repository.ExamAttemptAnswersRepository;
import org.t2404e.kanji_together_db.repository.ExamResultsRepository;
import org.t2404e.kanji_together_db.repository.QuestionsRepository;
import org.t2404e.kanji_together_db.repository.UsersRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExamAttemptAnswersService {

    @Autowired
    private ExamAttemptAnswersRepository examAttemptAnswersRepository;

    @Autowired
    private QuestionsRepository questionsRepository;

    @Autowired
    private ExamResultsRepository examResultsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private UserKanjiMasteryService masteryService;

    public ExamAttemptAnswerResponse create(ExamAttemptAnswerRequest request) {
        validateRequest(request);

        Questions question = questionsRepository.findById(request.getQuestionId())
                .filter(q -> q.getStatus() == 1)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy câu hỏi hoặc đã bị xóa"));

        ExamResults examResult = examResultsRepository.findById(request.getExamResultId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy kết quả bài thi"));

        AnswerResolution resolution = resolveSelectedAnswer(question, request);
        Users answerUser = resolveAnswerUser(examResult, request);

        ExamAttemptAnswers attempt = new ExamAttemptAnswers();
        attempt.examResult = examResult;
        attempt.user = answerUser;
        attempt.question = question;
        attempt.selectedAnswerId = resolution.selectedAnswerId;
        attempt.is_correct = resolution.isCorrect;
        attempt.answered_at = request.getAnsweredAt() != null ? request.getAnsweredAt() : LocalDateTime.now();
        attempt.timeTakenMs = request.getTimeTakenMs();

        ExamAttemptAnswers saved = examAttemptAnswersRepository.save(attempt);
        
        // Update mastery for all kanji characters in this question
        if (answerUser != null && question.getKanjiCharacters() != null && !question.getKanjiCharacters().isEmpty()) {
            for (KanjiCharacters kanji : question.getKanjiCharacters()) {
                masteryService.updateMastery(answerUser.getId(), kanji.getId(), resolution.isCorrect);
            }
        }
        
        long attemptCount = examAttemptAnswersRepository.countByUser_IdAndQuestion_Id(
                answerUser.getId(),
                question.getId()
        );

        ExamAttemptAnswerResponse response = new ExamAttemptAnswerResponse();
        response.setId(saved.id);
        response.setExamResultId(examResult.getId());
        response.setUserId(answerUser != null ? answerUser.getId() : null);
        response.setQuestionId(question.getId());
        response.setSelectedAnswerId(resolution.selectedAnswerId);
        response.setSelectedAnswer(resolution.selectedAnswer);
        response.setIsCorrect(resolution.isCorrect);
        response.setTimeTakenMs(saved.timeTakenMs);
        response.setAnsweredAt(saved.answered_at);
        response.setAttemptCount(attemptCount);
        response.setCorrectAnswer(question.getCorrectAnswer());
        response.setWrongAnswer1(question.getWrongAnswer1());
        response.setWrongAnswer2(question.getWrongAnswer2());
        response.setWrongAnswer3(question.getWrongAnswer3());
        return response;
    }

    private void validateRequest(ExamAttemptAnswerRequest request) {
        Map<String, String> errors = new HashMap<>();

        if (request.getExamResultId() == null) {
            errors.put("exam_result_id", "Vui lòng nhập exam_result_id");
        }
        if (request.getQuestionId() == null) {
            errors.put("question_id", "Vui lòng nhập question_id");
        }
        if (request.getUserId() == null && request.getExamResultId() == null) {
            errors.put("user_id", "Vui lòng nhập user_id hoặc exam_result_id hợp lệ");
        }
        boolean missingAnswer = isEmpty(request.getSelectedAnswer()) && request.getSelectedAnswerId() == null;
        if (missingAnswer) {
            errors.put("selected_answer", "Vui lòng chọn đáp án");
        }
        if (request.getTimeTakenMs() == null || request.getTimeTakenMs() < 0) {
            errors.put("time_taken_ms", "Vui lòng nhập thời gian làm bài hợp lệ");
        }

        if (!errors.isEmpty()) {
            throw new CustomValidationException(errors);
        }
    }

    private AnswerResolution resolveSelectedAnswer(Questions question, ExamAttemptAnswerRequest request) {
        String selectedAnswer = request.getSelectedAnswer();
        Integer selectedAnswerId = request.getSelectedAnswerId();

        if (!isEmpty(selectedAnswer)) {
            String trimmed = selectedAnswer.trim();
            Integer resolvedId = mapAnswerToId(question, trimmed);
            if (resolvedId == null) {
                throw new CustomValidationException(Map.of(
                        "selected_answer", "Đáp án không hợp lệ với câu hỏi này"
                ));
            }
            boolean isCorrect = trimmed.equals(question.getCorrectAnswer());
            return new AnswerResolution(resolvedId, trimmed, isCorrect);
        }

        String answerText = answerTextFromId(question, selectedAnswerId);
        if (answerText == null) {
            throw new CustomValidationException(Map.of(
                    "selected_answer_id", "Giá trị selected_answer_id phải từ 1 đến 4"
            ));
        }
        boolean isCorrect = answerText.equals(question.getCorrectAnswer());
        return new AnswerResolution(selectedAnswerId, answerText, isCorrect);
    }

    private Users resolveAnswerUser(ExamResults examResult, ExamAttemptAnswerRequest request) {
        Users userFromResult = examResult.getUser();
        if (userFromResult != null) {
            Long requestedUserId = request.getUserId();
            if (requestedUserId != null && !requestedUserId.equals(userFromResult.getId())) {
                throw new CustomValidationException(Map.of(
                        "user_id", "user_id không khớp với kết quả bài thi"
                ));
            }
            return userFromResult;
        }

        if (request.getUserId() == null) {
            throw new CustomValidationException(Map.of(
                    "user_id", "Vui lòng nhập user_id"
            ));
        }

        return usersRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
    }

    private Integer mapAnswerToId(Questions question, String answer) {
        if (answer.equals(question.getCorrectAnswer())) {
            return 1;
        }
        if (answer.equals(question.getWrongAnswer1())) {
            return 2;
        }
        if (answer.equals(question.getWrongAnswer2())) {
            return 3;
        }
        if (answer.equals(question.getWrongAnswer3())) {
            return 4;
        }
        return null;
    }

    private String answerTextFromId(Questions question, Integer selectedAnswerId) {
        if (selectedAnswerId == null) {
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

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class AnswerResolution {
        private final Integer selectedAnswerId;
        private final String selectedAnswer;
        private final boolean isCorrect;

        private AnswerResolution(Integer selectedAnswerId, String selectedAnswer, boolean isCorrect) {
            this.selectedAnswerId = selectedAnswerId;
            this.selectedAnswer = selectedAnswer;
            this.isCorrect = isCorrect;
        }
    }
}
