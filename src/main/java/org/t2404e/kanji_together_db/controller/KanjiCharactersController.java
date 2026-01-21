package org.t2404e.kanji_together_db.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.t2404e.kanji_together_db.dto.ApiResponse;
import org.t2404e.kanji_together_db.dto.KanjiCharacterDTO;
import org.t2404e.kanji_together_db.service.KanjiCharactersService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/kanjis")
public class KanjiCharactersController {

    @Autowired
    private KanjiCharactersService service;

    // 1. LẤY DANH SÁCH ĐÃ DUYỆT (Active = true)
    @GetMapping
    public ResponseEntity<ApiResponse<List<KanjiCharacterDTO>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách thành công", service.getAll()));
    }

    // 2. LẤY DANH SÁCH CHỜ DUYỆT (Active = false)
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<KanjiCharacterDTO>>> getPending() {
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách chờ duyệt", service.getPending()));
    }

    // 3. LẤY CHI TIẾT
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KanjiCharacterDTO>> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy chi tiết thành công", service.getDetail(id)));
    }

    // 4. TẠO MỚI (Bởi Admin)
    @PostMapping
    public ResponseEntity<ApiResponse<KanjiCharacterDTO>> create(@RequestBody KanjiCharacterDTO dto) {
        return new ResponseEntity<>(new ApiResponse<>(201, "Tạo Kanji thành công", service.create(dto)), HttpStatus.CREATED);
    }

    // 5. NGƯỜI DÙNG ĐÓNG GÓP
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<KanjiCharacterDTO>> submit(@RequestBody KanjiCharacterDTO dto) {
        return new ResponseEntity<>(new ApiResponse<>(201, "Đã gửi đóng góp, vui lòng chờ duyệt", service.createForUser(dto)), HttpStatus.CREATED);
    }

    // 6. CẬP NHẬT HOẶC DUYỆT
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<KanjiCharacterDTO>> update(@PathVariable Long id, @RequestBody KanjiCharacterDTO dto) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật thành công", service.update(id, dto)));
    }

    // 7. XÓA
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Xóa thành công", null));
    }
}