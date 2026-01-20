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

        // 1. GET ALL WITH FILTER (Ruby dùng để hiển thị danh sách)
    @GetMapping
    public ResponseEntity<ApiResponse<List<KanjiStoryDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<KanjiStoryDTO> list = service.getAll(page, size);
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách thành công", list));
    }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<KanjiStoryDTO>> getById(@PathVariable Long id) {
            // Gọi service lấy chi tiết 1 bài viết
            KanjiStoryDTO dto = service.getById(id);
            return ResponseEntity.ok(new ApiResponse<>(200, "Lấy chi tiết thành công", dto));
        }
        // 2. APPROVE (MỚI: Ruby gọi hàm này khi Admin nhấn Duyệt)
        @PutMapping("/{id}/approve")
        public ResponseEntity<ApiResponse<KanjiStoryDTO>> approve(
                @PathVariable Long id,
                @RequestBody Map<String, Object> adminData) {
            return ResponseEntity.ok(new ApiResponse<>(200, "Duyệt thành công", service.approve(id, adminData)));
        }

        // 3. REJECT (MỚI: Ruby gọi hàm này khi Admin nhấn Từ chối)
        @PutMapping("/{id}/reject")
        public ResponseEntity<ApiResponse<Void>> reject(
                @PathVariable Long id,
                @RequestParam String reason) {
            service.reject(id, reason);
            return ResponseEntity.ok(new ApiResponse<>(200, "Đã từ chối bài viết", null));
        }

        // 4. CREATE, UPDATE, DELETE (Giữ nguyên các hàm của bạn)
        @PostMapping
        public ResponseEntity<ApiResponse<KanjiStoryDTO>> create(@Valid @RequestBody KanjiStoryDTO dto) {
            return new ResponseEntity<>(new ApiResponse<>(201, "Tạo thành công", service.create(dto)), HttpStatus.CREATED);
        }

        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<KanjiStoryDTO>> update(@PathVariable Long id, @RequestBody KanjiStoryDTO dto) {
            // service.update(id, dto) bây giờ sẽ hết báo đỏ
            return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật thành công", service.update(id, dto)));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
            service.delete(id);
            return ResponseEntity.ok(new ApiResponse<>(200, "Xóa thành công", null));
        }
    }
