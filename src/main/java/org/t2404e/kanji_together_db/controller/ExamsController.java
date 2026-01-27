package org.t2404e.kanji_together_db.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.t2404e.kanji_together_db.dto.ExamDTO;
import org.t2404e.kanji_together_db.service.ExamsService;

@RestController
@RequestMapping("/api/v1/exams")
@CrossOrigin("*")
public class ExamsController {

    @Autowired
    private ExamsService examsService;

    // 1. Tạo hoặc Sửa Exam
    @PostMapping
    public ResponseEntity<ExamDTO> createOrUpdate(@RequestBody ExamDTO dto) {
        return ResponseEntity.ok(examsService.saveExam(dto));
    }

    // 2. Lấy danh sách phân trang
    @GetMapping
    public ResponseEntity<Page<ExamDTO>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(examsService.getAllExams(keyword, type, page, size));
    }

    // 3. Lấy chi tiết 1 Exam (bao gồm list ID câu hỏi đã chọn)
    @GetMapping("/{id}")
    public ResponseEntity<ExamDTO> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(examsService.getExamById(id));
    }

    // 4. Xóa
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        examsService.deleteExam(id);
        return ResponseEntity.ok("Deleted");
    }
}