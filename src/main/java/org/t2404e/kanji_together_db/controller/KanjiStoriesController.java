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

@RestController
@RequestMapping("/api/v1/kanji-stories")
public class KanjiStoriesController {

    @Autowired
    private KanjiStoriesService service;

    // 1. GET ALL
    @GetMapping
    public ResponseEntity<ApiResponse<List<KanjiStoryDTO>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách thành công", service.getAll()));
    }

    // 2. CREATE
    @PostMapping
    public ResponseEntity<ApiResponse<KanjiStoryDTO>> create(@Valid @RequestBody KanjiStoryDTO dto) {
        return new ResponseEntity<>(new ApiResponse<>(201, "Tạo Story thành công", service.create(dto)), HttpStatus.CREATED);
    }

    // 3. UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<KanjiStoryDTO>> update(@PathVariable Long id, @RequestBody KanjiStoryDTO dto) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật Story thành công", service.update(id, dto)));
    }

    // 4. DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Xóa Story thành công", null));
    }
}