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

    // 1. LẤY DANH SÁCH (HỖ TRỢ LỌC ACTIVE, PENDING, REJECTED...)
    @GetMapping
    public ResponseEntity<ApiResponse<List<KanjiCharacterDTO>>> getAll(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "kanji", required = false) String kanji,
            @RequestParam(name = "is_active", required = false) Boolean isActive,
            @RequestParam(name = "status", required = false) String status, // Tham số quan trọng để lọc PENDING
            @RequestParam(name = "page", required = false) Integer page
    ) {
        String keyword = (search != null) ? search : kanji;
        // Gọi Service với đủ 3 tham số lọc
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách thành công", service.getAll(keyword, isActive, status, page)));
    }

    // 2. LẤY LỊCH SỬ ĐÓNG GÓP (MỚI - Dùng cho Admin xem ai đã đóng góp cho chữ này)
    @GetMapping("/contributions")
    public ResponseEntity<ApiResponse<List<KanjiCharacterDTO>>> getContributions(@RequestParam String kanji) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy lịch sử đóng góp thành công", service.getContributions(kanji)));
    }

    // 3. LẤY CHI TIẾT
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KanjiCharacterDTO>> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy chi tiết thành công", service.getDetail(id)));
    }

    // 4. TẠO MỚI BỞI ADMIN (Quản lý bản Gốc Master)
    @PostMapping
    public ResponseEntity<ApiResponse<KanjiCharacterDTO>> create(@Valid @RequestBody KanjiCharacterDTO dto) {
        return new ResponseEntity<>(new ApiResponse<>(201, "Admin tạo Kanji thành công", service.create(dto)), HttpStatus.CREATED);
    }

    // 5. NGƯỜI DÙNG ĐÓNG GÓP (Tạo bản PENDING)
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<KanjiCharacterDTO>> submit(@Valid @RequestBody KanjiCharacterDTO dto) {
        // Gọi hàm createForUser để luôn tạo bản nháp mới
        return new ResponseEntity<>(new ApiResponse<>(201, "Đã gửi đóng góp, vui lòng chờ duyệt", service.createForUser(dto)), HttpStatus.CREATED);
    }

    // 6. CẬP NHẬT (Admin sửa trực tiếp bản ghi)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<KanjiCharacterDTO>> update(@PathVariable Long id, @Valid @RequestBody KanjiCharacterDTO dto) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật thành công", service.update(id, dto)));
    }

    // ==========================================================
    // CÁC API MỚI PHỤC VỤ QUY TRÌNH DUYỆT (APPROVAL FLOW)
    // ==========================================================

    // 7. DUYỆT BÀI (Approve -> Merge vào bản gốc -> Chuyển status APPROVED)
    // 7. DUYỆT BÀI (CÓ VALIDATE & NHẬN DỮ LIỆU TỪ ADMIN)
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<KanjiCharacterDTO>> approve(
            @PathVariable Long id,
            @Valid @RequestBody KanjiCharacterDTO finalData // <--- Thêm dòng này để nhận dữ liệu Admin đã sửa
    ) {
        KanjiCharacterDTO result = service.approve(id, finalData);
        return ResponseEntity.ok(new ApiResponse<>(200, "Đã duyệt và xuất bản thành công", result));
    }

    // 8. TỪ CHỐI BÀI (Reject -> Chuyển status REJECTED)
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(@PathVariable Long id) {
        service.reject(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Đã từ chối đóng góp", null));
    }

    // 9. XÓA (Soft Delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Xóa thành công", null));
    }
}
