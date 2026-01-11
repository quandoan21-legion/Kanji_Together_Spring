package org.t2404e.kanji_together_db.exception; // <-- Package mới tạo

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.t2404e.kanji_together_db.dto.ApiResponse; // Import DTO của bạn

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Object>> handleResponseStatusException(ResponseStatusException ex) {
        // Lấy status code (ví dụ 400, 404)
        int statusCode = ex.getStatusCode().value();

        // Lấy message lỗi bạn đã viết trong Service
        String message = ex.getReason();

        // Đóng gói vào ApiResponse của bạn
        ApiResponse<Object> response = new ApiResponse<>(statusCode, message, null);

        // Trả về ResponseEntity
        return new ResponseEntity<>(response, ex.getStatusCode());
    }
}