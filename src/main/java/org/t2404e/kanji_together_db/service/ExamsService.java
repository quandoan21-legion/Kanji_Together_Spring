package org.t2404e.kanji_together_db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.t2404e.kanji_together_db.entity.Exams;
import org.t2404e.kanji_together_db.repository.ExamsRepository;

import java.util.List;

@Service
public class ExamsService {

    @Autowired
    private ExamsRepository examsRepository;

    // 1. Lấy tất cả (Mới nhất lên đầu)
    public List<Exams> getAll() {
        // Bạn có thể viết thêm method findAllByOrderByIdDesc() trong Repo nếu muốn
        return examsRepository.findAll();
    }

    // 2. Lấy chi tiết
    public Exams getDetail(Long id) {
        return examsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đề thi"));
    }

    // 3. Tạo mới
    public Exams create(Exams exam) {
        // Validate cơ bản
        if (exam.getName() == null || exam.getName().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên đề thi không được để trống");
        }
        if (exam.getExamType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phải chọn loại đề thi");
        }

        exam.setStatus(1); // Mặc định Active
        return examsRepository.save(exam);
    }

    // 4. Cập nhật
    public Exams update(Long id, Exams payload) {
        Exams exist = getDetail(id);

        exist.setName(payload.getName());
        exist.setExamType(payload.getExamType());
        exist.setTargetRank(payload.getTargetRank());
        exist.setDuration(payload.getDuration());
        exist.setPassScore(payload.getPassScore());
        exist.setTotalQuestions(payload.getTotalQuestions());
        exist.setLessonId(payload.getLessonId());

        // --- MỚI: Cập nhật danh sách câu hỏi ---
        if (payload.getQuestions() != null) {
            exist.setQuestions(payload.getQuestions());
        }

        return examsRepository.save(exist);
    }

    // 5. Xóa (Soft Delete hoặc Hard Delete tùy bạn)
    // Ở đây tôi làm Hard Delete cho gọn, nếu muốn Soft Delete thì set status = 0
    public void delete(Long id) {
        if (!examsRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đề thi");
        }
        examsRepository.deleteById(id);
    }
}