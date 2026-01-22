package org.t2404e.kanji_together_db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.t2404e.kanji_together_db.entity.Questions;
import org.t2404e.kanji_together_db.exception.CustomValidationException;
import org.t2404e.kanji_together_db.repository.QuestionsRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QuestionsService {

    @Autowired
    private QuestionsRepository repository;

    // Lấy danh sách câu hỏi Active
    public List<Questions> getAllActive() {
        return repository.findAllByStatus(1);
    }

    // Lọc theo loại (chỉ lấy Active)
    public List<Questions> filterByType(String type) {
        return repository.findByQuestionTypeAndStatus(type, 1);
    }

    // Lấy chi tiết
    public Questions getDetail(Long id) {
        return repository.findById(id)
                .filter(q -> q.getStatus() == 1) // Chỉ lấy nếu đang Active
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy câu hỏi hoặc đã bị xóa"));
    }

    // TẠO MỚI
    public Questions create(Questions question) {
        normalizeData(question);
        validateQuestionData(question);

        // Mặc định Active khi tạo mới
        question.setStatus(1);

        return repository.save(question);
    }

    // CẬP NHẬT
    public Questions update(Long id, Questions questionDetails) {
        Questions existingQuestion = getDetail(id); // Đã bao gồm check tồn tại

        normalizeData(questionDetails);
        validateQuestionData(questionDetails);

        // Update fields
        existingQuestion.setQuestionType(questionDetails.getQuestionType());
        existingQuestion.setQuestionText(questionDetails.getQuestionText());
        existingQuestion.setCorrectAnswer(questionDetails.getCorrectAnswer());
        existingQuestion.setWrongAnswer1(questionDetails.getWrongAnswer1());
        existingQuestion.setWrongAnswer2(questionDetails.getWrongAnswer2());
        existingQuestion.setWrongAnswer3(questionDetails.getWrongAnswer3());
        return repository.save(existingQuestion);
    }

    // XÓA MỀM (Soft Delete Manual)
    public void delete(Long id) {
        Questions question = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy câu hỏi để xóa"));

        // Logic xóa mềm thủ công: Set status = 0
        question.setStatus(0);
        repository.save(question);
    }

    // --- VALIDATION & NORMALIZATION (Giống KanjiCharacters) ---

    private void normalizeData(Questions q) {
        if (q.getQuestionText() != null) q.setQuestionText(q.getQuestionText().trim());
        if (q.getCorrectAnswer() != null) q.setCorrectAnswer(q.getCorrectAnswer().trim());
        if (q.getWrongAnswer1() != null) q.setWrongAnswer1(q.getWrongAnswer1().trim());
        if (q.getWrongAnswer2() != null) q.setWrongAnswer2(q.getWrongAnswer2().trim());
        if (q.getWrongAnswer3() != null) q.setWrongAnswer3(q.getWrongAnswer3().trim());
    }

    private void validateQuestionData(Questions q) {
        Map<String, String> errors = new HashMap<>();

        if (isEmpty(q.getQuestionType())) {
            errors.put("question_type", "Vui lòng chọn loại câu hỏi");
        }
        if (isEmpty(q.getQuestionText())) {
            errors.put("question_text", "Vui lòng nhập nội dung câu hỏi");
        }
        if (isEmpty(q.getCorrectAnswer())) {
            errors.put("correct_answer", "Vui lòng nhập đáp án đúng");
        }

        if (isEmpty(q.getWrongAnswer1())) {
            errors.put("wrong_answer_1", "Vui lòng nhập đáp án sai 1");
        }
        if (isEmpty(q.getWrongAnswer2())) {
            errors.put("wrong_answer_2", "Vui lòng nhập đáp án sai 2");
        }
        if (isEmpty(q.getWrongAnswer3())) {
            errors.put("wrong_answer_3", "Vui lòng nhập đáp án sai 3");
        }
        // --------------------------------------

        if (!errors.isEmpty()) {
            throw new CustomValidationException(errors);
        }
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}