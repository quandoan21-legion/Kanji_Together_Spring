package org.t2404e.kanji_together_db.controller;

import jakarta.validation.Valid;
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


    @GetMapping
    public ResponseEntity<ApiResponse<List<KanjiCharacterDTO>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách thành công", service.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KanjiCharacterDTO>> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy chi tiết thành công", service.getDetail(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<KanjiCharacterDTO>> create(@RequestBody KanjiCharacterDTO dto) {
        return new ResponseEntity<>(new ApiResponse<>(201, "Tạo Kanji thành công", service.create(dto)), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<KanjiCharacterDTO>> update(@PathVariable Long id, @RequestBody KanjiCharacterDTO dto) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật Kanji thành công", service.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Xóa Kanji thành công", null));
    }
}