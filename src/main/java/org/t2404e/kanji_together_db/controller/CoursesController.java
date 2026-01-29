package org.t2404e.kanji_together_db.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.t2404e.kanji_together_db.dto.ApiResponse;
import org.t2404e.kanji_together_db.dto.CourseDTO;
import org.t2404e.kanji_together_db.service.CoursesService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@CrossOrigin(origins = "*")
public class CoursesController {

    @Autowired
    private CoursesService coursesService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseDTO>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách thành công", coursesService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDTO>> getDetail(@PathVariable Long id) {
        CourseDTO dto = coursesService.getDetail(id);
        if (dto == null) {
            return ResponseEntity.status(404).body(new ApiResponse<>(404, "Không tìm thấy khóa học", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy chi tiết thành công", dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CourseDTO>> create(@RequestBody CourseDTO dto) {
        CourseDTO created = coursesService.create(dto);
        return ResponseEntity.status(201).body(new ApiResponse<>(201, "Tạo khóa học thành công", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDTO>> update(@PathVariable Long id, @RequestBody CourseDTO dto) {
        CourseDTO updated = coursesService.update(id, dto);
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật thành công", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        coursesService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Xóa thành công", null));
    }
}
