package org.t2404e.kanji_together_db.controller;
import org.t2404e.kanji_together_db.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.t2404e.kanji_together_db.dto.ClazzDTO;
import org.t2404e.kanji_together_db.service.ClazzService;

@RestController
@RequestMapping("/api/v1/clazz")
public class ClazzController {

    @Autowired
    private ClazzService clazzService;

    // 1. Create
    @PostMapping
    public ResponseEntity<ClazzDTO> create(@Valid @RequestBody ClazzDTO request) {
        return new ResponseEntity<>(clazzService.create(request), HttpStatus.CREATED);
    }

    // 2. Get Detail
    @GetMapping("/{id}")
    public ResponseEntity<ClazzDTO> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(clazzService.getDetail(id));
    }

    // 3. List with Filter & Paging
    @GetMapping
    public ResponseEntity<Page<ClazzDTO>> getList(
            @RequestParam(name = "q", required = false) String keyword,
            @RequestParam(name = "is_active", required = false) Boolean isActive,
            @PageableDefault(size = 20, sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(clazzService.getList(keyword, isActive, pageable));
    }

    // 4. Update
    @PutMapping("/{id}")
    public ResponseEntity<ClazzDTO> update(@PathVariable Long id, @RequestBody ClazzDTO request) {
        return ResponseEntity.ok(clazzService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        clazzService.delete(id);
        ApiResponse<Void> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Đã xóa thành công lớp học!",
                null
        );
        return ResponseEntity.ok(response);
    }
}