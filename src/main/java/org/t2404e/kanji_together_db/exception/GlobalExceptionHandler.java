package org.t2404e.kanji_together_db.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.t2404e.kanji_together_db.dto.ApiResponse;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Bắt các lỗi ResponseStatusException thông thường (404, 500...)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Object>> handleResponseStatusException(ResponseStatusException ex) {
        int statusCode = ex.getStatusCode().value();
        String message = ex.getReason();

        ApiResponse<Object> response = new ApiResponse<>(statusCode, message, null);
        return new ResponseEntity<>(response, ex.getStatusCode());
    }

    // 2. Bắt lỗi CustomValidationException để gửi lỗi từng ô về cho Ruby
    @ExceptionHandler(CustomValidationException.class)
    public ResponseEntity<Map<String, Object>> handleCustomValidation(CustomValidationException ex) {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("status", 400);
        body.put("message", "Dữ liệu nhập vào không hợp lệ!");
        body.put("errors", ex.getErrors()); // Chứa danh sách: { "on_pronunciation": "Phải là Katakana", ... }

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}