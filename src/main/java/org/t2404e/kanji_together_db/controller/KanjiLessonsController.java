package org.t2404e.kanji_together_db.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.t2404e.kanji_together_db.dto.ApiResponse; // Đảm bảo bạn đã có class này
import org.t2404e.kanji_together_db.dto.KanjiLessonDTO;
import org.t2404e.kanji_together_db.service.KanjiLessonsService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/lessons")
@CrossOrigin(origins = "*")
public class KanjiLessonsController {

    @Autowired
    private KanjiLessonsService service;

    // --- 1. LẤY DANH SÁCH ---
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAll(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "jlpt", required = false) Integer jlpt,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "created_at", required = false) String createdAt,
            @RequestParam(name = "page", required = false) Integer page, // Spring sẽ tự hiểu page 0
            @RequestParam(name = "size", required = false) Integer size
    ) {
        // Service của bạn trả về Map, controller bọc nó vào ApiResponse
        Map<String, Object> result = service.getAll(search, jlpt, status, createdAt, page, size);
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách thành công", result));
    }

    // --- 2. CHI TIẾT ---
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KanjiLessonDTO>> getDetail(@PathVariable Long id) {
        KanjiLessonDTO dto = service.getDetail(id);
        if (dto == null) {
            // Trả về lỗi 404 nếu không tìm thấy
            return ResponseEntity.status(404).body(new ApiResponse<>(404, "Không tìm thấy bài học", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy chi tiết thành công", dto));
    }

    // --- 3. TẠO MỚI (QUAN TRỌNG NHẤT) ---
    @PostMapping
    public ResponseEntity<ApiResponse<KanjiLessonDTO>> create(@RequestBody KanjiLessonDTO dto) {
        // @RequestBody: Giúp map JSON từ Rails -> DTO Java (gồm kanjiIds, lessonDescription...)
        KanjiLessonDTO created = service.create(dto);
        return ResponseEntity.status(201).body(new ApiResponse<>(201, "Tạo bài học thành công", created));
    }

    // --- 4. CẬP NHẬT (QUAN TRỌNG NHÌ) ---
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<KanjiLessonDTO>> update(@PathVariable Long id, @RequestBody KanjiLessonDTO dto) {
        KanjiLessonDTO updated = service.update(id, dto);
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật thành công", updated));
    }

    // --- 5. XÓA ---
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Xóa thành công", null));
    }
}