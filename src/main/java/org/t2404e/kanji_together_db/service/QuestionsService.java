package org.t2404e.kanji_together_db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.t2404e.kanji_together_db.entity.KanjiCharacters; // Import Entity Kanji
import org.t2404e.kanji_together_db.entity.Questions;
import org.t2404e.kanji_together_db.exception.CustomValidationException;
import org.t2404e.kanji_together_db.repository.KanjiCharactersRepository; // Import Repo Kanji
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
    private KanjiCharactersRepository kanjiRepository; // <--- MỚI: Cần cái này để check Kanji tồn tại

    // Lấy danh sách câu hỏi Active
    public List<Questions> getAllActive() {
        return repository.findAllByStatus(1);
    }

    // Lọc theo loại (chỉ lấy Active)
    public List<Questions> filterByType(String type) {
        return repository.findByQuestionTypeAndStatus(type, 1);
    }

    // --- MỚI: TÌM KIẾM THEO CHỮ KANJI ---
    // Hỗ trợ tính năng tìm kiếm mà bạn vừa thêm ở Controller Ruby
    public List<Questions> searchByKanji(String kanjiChar) {
        return repository.findByKanjiCharacter(kanjiChar);
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

        // --- MỚI: Xử lý danh sách Kanji liên quan ---
        processRelatedKanjis(question);

        // Mặc định Active khi tạo mới
        question.setStatus(1);

        return repository.save(question);
    }

    // CẬP NHẬT
    public Questions update(Long id, Questions questionDetails) {
        Questions existingQuestion = getDetail(id); // Đã bao gồm check tồn tại

        normalizeData(questionDetails);
        validateQuestionData(questionDetails);

        // Update fields cơ bản
        existingQuestion.setQuestionType(questionDetails.getQuestionType());
        existingQuestion.setQuestionText(questionDetails.getQuestionText());
        existingQuestion.setCorrectAnswer(questionDetails.getCorrectAnswer());
        existingQuestion.setWrongAnswer1(questionDetails.getWrongAnswer1());
        existingQuestion.setWrongAnswer2(questionDetails.getWrongAnswer2());
        existingQuestion.setWrongAnswer3(questionDetails.getWrongAnswer3());

        // --- MỚI: Update danh sách Kanji liên quan ---
        // Copy danh sách Kanji từ request sang object cũ
        if (questionDetails.getKanjiCharacters() != null) {
            processRelatedKanjis(questionDetails); // Validate ID trước
            existingQuestion.setKanjiCharacters(questionDetails.getKanjiCharacters());
        } else {
            // Nếu gửi null hoặc rỗng thì có thể muốn xóa hết liên kết, hoặc giữ nguyên tùy logic
            // Ở đây tôi chọn cách: Nếu gửi list rỗng -> Xóa liên kết.
            existingQuestion.setKanjiCharacters(new ArrayList<>());
        }

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

    // --- HELPER: Xử lý Kanji List ---
    private void processRelatedKanjis(Questions q) {
        if (q.getKanjiCharacters() != null && !q.getKanjiCharacters().isEmpty()) {
            List<KanjiCharacters> validKanjis = new ArrayList<>();
            for (KanjiCharacters k : q.getKanjiCharacters()) {
                // Ruby chỉ gửi ID (ví dụ: {id: 1}), nên ta phải tìm trong DB xem có thật không
                if (k.getId() != null) {
                    kanjiRepository.findById(k.getId()).ifPresent(validKanjis::add);
                }
            }
            // Gán lại danh sách những Kanji ĐÃ TÌM THẤY trong DB
            q.setKanjiCharacters(validKanjis);
        }
    }

    // --- VALIDATION & NORMALIZATION (Giữ nguyên chặt chẽ) ---

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

        if (!errors.isEmpty()) {
            throw new CustomValidationException(errors);
        }
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}