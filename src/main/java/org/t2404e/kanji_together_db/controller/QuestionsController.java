package org.t2404e.kanji_together_db.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.t2404e.kanji_together_db.dto.QuestionDTO;
import org.t2404e.kanji_together_db.entity.Questions;
import org.t2404e.kanji_together_db.service.QuestionsService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/questions")
@CrossOrigin(origins = "*")
public class QuestionsController {

    @Autowired
    private QuestionsService service;

    // [ĐÃ CẬP NHẬT]: Thêm tham số page, size và trả về Page<?>
    @GetMapping
    public ResponseEntity<Page<Questions>> getAll(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "exam_id", required = false) Long examId,
            @RequestParam(defaultValue = "0") int page, // Mặc định trang 0
            @RequestParam(defaultValue = "10") int size // Mặc định 10 phần tử/trang
    ) {
        // 1. Tạo đối tượng Pageable (Sắp xếp ID mới nhất lên đầu)
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // 2. Gọi Service xử lý tìm kiếm + phân trang
        // Lưu ý: Bạn cần cập nhật Service để nhận Pageable này
        Page<Questions> result = service.getAllQuestions(keyword, type, examId, pageable);

        return ResponseEntity.ok(result);
    }

    // --- CÁC HÀM KHÁC GIỮ NGUYÊN ---

    @GetMapping("/{id}")
    public ResponseEntity<Questions> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getDetail(id));
    }

    @GetMapping("/kanji/{kanjiId}")
    public ResponseEntity<List<QuestionDTO>> getByKanjiId(@PathVariable Long kanjiId) {
        return ResponseEntity.ok(service.getByKanjiId(kanjiId));
    }

    @PostMapping
    public ResponseEntity<Questions> create(@RequestBody QuestionDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Questions> update(@PathVariable Long id, @RequestBody QuestionDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}
