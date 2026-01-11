package org.t2404e.kanji_together_db.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.t2404e.kanji_together_db.dto.ApiResponse;
import org.t2404e.kanji_together_db.dto.UserDTO;
import org.t2404e.kanji_together_db.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    // 1. GET ALL
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        return ResponseEntity.ok(new ApiResponse<>(200, "Thành công", userService.getAllUsers()));
    }

    // 2. GET DETAIL
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Thành công", userService.getUserById(id)));
    }

    // 3. POST CREATE (Quan trọng nhất cho ticket này)
    @PostMapping
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody UserDTO request) {
        UserDTO newUser = userService.createUser(request);
        return new ResponseEntity<>(new ApiResponse<>(201, "Tạo User thành công", newUser), HttpStatus.CREATED);
    }

    // 4. PUT UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable Long id, @RequestBody UserDTO request) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật thành công", userService.updateUser(id, request)));
    }

    // 5. DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.softDeleteUser(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Xóa user thành công (Soft Delete)", null));
    }
}