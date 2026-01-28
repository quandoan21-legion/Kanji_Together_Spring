package org.t2404e.kanji_together_db.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.t2404e.kanji_together_db.dto.ApiResponse;
import org.t2404e.kanji_together_db.dto.UserDTO;
import org.t2404e.kanji_together_db.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    // 1. GET ALL - CẬP NHẬT: Hỗ trợ tìm kiếm và phân trang
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getAllUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Sắp xếp ID giảm dần để người mới nhất hiện lên trên đầu
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<UserDTO> result = userService.getFilteredUsers(name, email, active, pageable);
        return ResponseEntity.ok(new ApiResponse<>(200, "Thành công", result));
    }

    // 2. GET DETAIL (Giữ nguyên - Rất tốt)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Thành công", userService.getUserById(id)));
    }

    // 3. POST CREATE (Giữ nguyên - Rất tốt)
    @PostMapping
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody UserDTO request) {
        UserDTO newUser = userService.createUser(request);
        return new ResponseEntity<>(new ApiResponse<>(201, "Tạo User thành công", newUser), HttpStatus.CREATED);
    }

    // 4. PUT UPDATE (Giữ nguyên - Rất tốt)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable Long id, @RequestBody UserDTO request) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật thành công", userService.updateUser(id, request)));
    }

    // 5. DELETE (Giữ nguyên - Soft Delete là lựa chọn chuyên nghiệp)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.softDeleteUser(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Xóa user thành công (Soft Delete)", null));
    }
}