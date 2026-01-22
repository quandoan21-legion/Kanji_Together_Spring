package org.t2404e.kanji_together_db.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.t2404e.kanji_together_db.entity.Questions;
import org.t2404e.kanji_together_db.service.QuestionsService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/questions")
public class QuestionsController {

    @Autowired
    private QuestionsService service;

    // SỬA ĐOẠN NÀY: Thêm tham số "kanji" để nhận từ Ruby
    @GetMapping
    public List<Questions> getAll(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String kanji // <--- MỚI
    ) {
        // 1. Ưu tiên tìm theo Kanji trước (cho ô tìm kiếm)
        if (kanji != null && !kanji.isEmpty()) {
            return service.searchByKanji(kanji);
        }

        // 2. Nếu không tìm Kanji thì lọc theo Type (cho dropdown)
        if (type != null && !type.isEmpty()) {
            return service.filterByType(type);
        }

        // 3. Mặc định lấy tất cả câu Active
        return service.getAllActive();
    }

    // --- CÁC HÀM DƯỚI GIỮ NGUYÊN KHÔNG ĐỔI ---

    @GetMapping("/{id}")
    public ResponseEntity<Questions> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getDetail(id));
    }

    @PostMapping
    public ResponseEntity<Questions> create(@RequestBody Questions question) {
        return ResponseEntity.ok(service.create(question));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Questions> update(@PathVariable Long id, @RequestBody Questions question) {
        return ResponseEntity.ok(service.update(id, question));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}