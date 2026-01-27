package org.t2404e.kanji_together_db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.t2404e.kanji_together_db.dto.QuestionDTO;
import org.t2404e.kanji_together_db.entity.Exams;
import org.t2404e.kanji_together_db.entity.KanjiCharacters;
import org.t2404e.kanji_together_db.entity.Questions;
import org.t2404e.kanji_together_db.enums.QuestionType;
import org.t2404e.kanji_together_db.exception.CustomValidationException;
import org.t2404e.kanji_together_db.repository.ExamsRepository;
import org.t2404e.kanji_together_db.repository.KanjiCharactersRepository;
import org.t2404e.kanji_together_db.repository.QuestionsRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QuestionsService {

    @Autowired
    private QuestionsRepository repository;

    @Autowired
    private KanjiCharactersRepository kanjiRepository;

    public Page<Questions> getAllQuestions(String keyword, String typeStr, Long examId, Pageable pageable) {

        // 1. Xử lý Keyword
        String finalKeyword = (keyword == null) ? "" : keyword.trim();

        // 2. Xử lý Loại câu hỏi
        QuestionType type = null;
        if (typeStr != null && !typeStr.trim().isEmpty()) {
            try {
                type = QuestionType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                type = null; // Nếu type sai định dạng thì không lọc theo type
            }
        }

        // 3. Gọi Repository với đầy đủ 4 tham số: type, keyword, examId, pageable
        return repository.searchQuestions(type, finalKeyword, examId, pageable);
    }

    // --- CÁC HÀM TRUY VẤN CHI TIẾT ---

    public Questions getDetail(Long id) {
        return repository.findById(id)
                .filter(q -> q.getStatus() == 1)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy câu hỏi hoặc đã bị xóa"));
    }

    // --- CÁC HÀM THAY ĐỔI DỮ LIỆU (CREATE/UPDATE/DELETE) ---

    public Questions create(QuestionDTO dto) {
        validateDTO(dto);
        Questions question = new Questions();
        mapDtoToEntity(dto, question);
        question.setStatus(1);
        return repository.save(question);
    }

    public Questions update(Long id, QuestionDTO dto) {
        Questions existingQuestion = getDetail(id);
        validateDTO(dto);
        mapDtoToEntity(dto, existingQuestion);
        return repository.save(existingQuestion);
    }

    public void delete(Long id) {
        Questions question = getDetail(id);
        question.setStatus(0); // Soft delete
        repository.save(question);
    }

    // --- CÁC PHƯƠNG THỨC HỖ TRỢ (PRIVATE HELPERS) ---

    private void mapDtoToEntity(QuestionDTO dto, Questions entity) {
        if (dto.getQuestionType() != null) {
            entity.setQuestionType(dto.getQuestionType());
        }

        if (dto.getQuestionText() != null) entity.setQuestionText(dto.getQuestionText().trim());
        if (dto.getCorrectAnswer() != null) entity.setCorrectAnswer(dto.getCorrectAnswer().trim());
        if (dto.getWrongAnswer1() != null) entity.setWrongAnswer1(dto.getWrongAnswer1().trim());
        if (dto.getWrongAnswer2() != null) entity.setWrongAnswer2(dto.getWrongAnswer2().trim());
        if (dto.getWrongAnswer3() != null) entity.setWrongAnswer3(dto.getWrongAnswer3().trim());

        // Liên kết với danh sách KanjiCharacters
        if (dto.getKanjiIds() != null) {
            if (!dto.getKanjiIds().isEmpty()) {
                List<KanjiCharacters> kanjis = kanjiRepository.findAllById(dto.getKanjiIds());
                entity.setKanjiCharacters(kanjis);
            } else {
                entity.setKanjiCharacters(new ArrayList<>());
            }
        }
    }
    private void validateDTO(QuestionDTO dto) {
        Map<String, String> errors = new HashMap<>();

        if (dto.getQuestionType() == null) errors.put("question_type", "Vui lòng chọn loại câu hỏi");
        if (dto.getQuestionText() == null || dto.getQuestionText().trim().isEmpty()) errors.put("question_text", "Nội dung câu hỏi không được để trống");
        if (dto.getCorrectAnswer() == null || dto.getCorrectAnswer().trim().isEmpty()) errors.put("correct_answer", "Đáp án đúng không được để trống");
        if (dto.getWrongAnswer1() == null || dto.getWrongAnswer1().trim().isEmpty()) errors.put("wrong_answer_1", "Thiếu đáp án sai 1");

        if (!errors.isEmpty()) {
            throw new CustomValidationException(errors);
        }
    }
}