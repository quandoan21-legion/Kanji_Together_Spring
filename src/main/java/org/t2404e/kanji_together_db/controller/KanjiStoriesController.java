package org.t2404e.kanji_together_db.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.t2404e.kanji_together_db.dto.ApiResponse;
import org.t2404e.kanji_together_db.dto.KanjiStoryDTO;
import org.t2404e.kanji_together_db.service.KanjiStoriesService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/kanji-stories")
public class KanjiStoriesController {

    @Autowired
    private KanjiStoriesService service;

    // ==================================================================================
    // 1. LẤY DANH SÁCH & CHI TIẾT
    // ==================================================================================

    // GET ALL WITH FILTER (Đã sửa: Thay lọc Email bằng lọc Kanji Text)
    @GetMapping
    public ResponseEntity<ApiResponse<List<KanjiStoryDTO>>> getAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String kanji,
            @RequestParam(required = false) Long kanjiId,
            @RequestParam(defaultValue = "0") int page
    ) {
        // Gọi service với tham số kanji text mới
        List<KanjiStoryDTO> list = service.getAllFiltered(status, kanji, kanjiId, page);
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách thành công", list));
    }

    // GET BY ID (Giữ nguyên)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KanjiStoryDTO>> getById(@PathVariable Long id) {
        KanjiStoryDTO dto = service.getById(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy chi tiết thành công", dto));
    }

    // ==================================================================================
    // 2. CHỨC NĂNG DUYỆT BÀI (ADMIN) - GIỮ NGUYÊN
    // ==================================================================================

    // APPROVE (Duyệt bài)
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<KanjiStoryDTO>> approve(
            @PathVariable Long id,
            @RequestBody Map<String, Object> adminData) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Duyệt thành công", service.approve(id, adminData)));
    }


    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectStory(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String status
    ) {
        // Truyền cả reason vào service
        service.reject(id, status, reason);

        return ResponseEntity.ok(new ApiResponse<>(200, "Thành công", null));
    }
    // ==================================================================================
    // 3. CRUD CƠ BẢN (CREATE, UPDATE, DELETE) - GIỮ NGUYÊN
    // ==================================================================================

    // CREATE
    @PostMapping
    public ResponseEntity<ApiResponse<KanjiStoryDTO>> create(@Valid @RequestBody KanjiStoryDTO dto) {
        return new ResponseEntity<>(new ApiResponse<>(201, "Tạo thành công", service.create(dto)), HttpStatus.CREATED);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<KanjiStoryDTO>> update(@PathVariable Long id, @RequestBody KanjiStoryDTO dto) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật thành công", service.update(id, dto)));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Xóa thành công", null));
    }
}