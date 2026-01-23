package org.t2404e.kanji_together_db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.t2404e.kanji_together_db.dto.ExamResultCreateRequest;
import org.t2404e.kanji_together_db.dto.ExamResultResponse;
import org.t2404e.kanji_together_db.entity.ExamResults;
import org.t2404e.kanji_together_db.entity.Exams;
import org.t2404e.kanji_together_db.entity.Users;
import org.t2404e.kanji_together_db.exception.CustomValidationException;
import org.t2404e.kanji_together_db.repository.ExamResultsRepository;
import org.t2404e.kanji_together_db.repository.ExamsRepository;
import org.t2404e.kanji_together_db.repository.UsersRepository;

import java.util.HashMap;
import java.util.Map;

@Service
public class ExamResultsService {

    @Autowired
    private ExamResultsRepository examResultsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ExamsRepository examsRepository;

    public ExamResultResponse start(ExamResultCreateRequest request) {
        validateRequest(request);

        Users user = usersRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));

        Exams exam = examsRepository.findById(request.getExamId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài thi"));

        ExamResults result = new ExamResults();
        result.setUser(user);
        result.setExam(exam);
        result.setTotal_question(request.getTotalQuestion() != null ? request.getTotalQuestion() : 0);
        result.setCorrect_answer(0);

        ExamResults saved = examResultsRepository.save(result);

        ExamResultResponse response = new ExamResultResponse();
        response.setId(saved.getId());
        response.setUserId(user.getId());
        response.setExamId(exam.getId());
        response.setTotalQuestion(saved.getTotal_question());
        response.setCorrectAnswer(saved.getCorrect_answer());
        response.setCreatedAt(saved.getCreate_at());
        return response;
    }

    private void validateRequest(ExamResultCreateRequest request) {
        Map<String, String> errors = new HashMap<>();

        if (request.getUserId() == null) {
            errors.put("user_id", "Vui lòng nhập user_id");
        }
        if (request.getExamId() == null) {
            errors.put("exam_id", "Vui lòng nhập exam_id");
        }
        if (request.getTotalQuestion() != null && request.getTotalQuestion() < 0) {
            errors.put("total_question", "total_question không hợp lệ");
        }

        if (!errors.isEmpty()) {
            throw new CustomValidationException(errors);
        }
    }
}
