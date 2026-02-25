package org.t2404e.kanji_together_db.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. BẮT LỖI TỰ ĐỊNH NGHĨA
    @ExceptionHandler(CustomValidationException.class)
    public ResponseEntity<Map<String, Object>> handleCustomValidation(CustomValidationException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("message", "Dữ liệu nhập vào chưa đúng quy chuẩn!");
        body.put("errors", ex.getErrors());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 2. BẮT LỖI @VALID CỦA SPRING
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        body.put("status", 400);
        body.put("message", "Vui lòng điền đầy đủ thông tin!");
        body.put("errors", errors); // Trả về format y hệt CustomValidationException

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 3. BẮT LỖI HTTP CHUNG
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", ex.getStatusCode().value());
        body.put("message", ex.getReason() != null ? ex.getReason() : "Lỗi hệ thống");
        return new ResponseEntity<>(body, ex.getStatusCode());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Map<String, Object> body = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        body.put("status", 400);
        body.put("message", "Dữ liệu nhập vào chưa đúng quy chuẩn!");

        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException invalidFormat) {
            String field = invalidFormat.getPath().stream()
                    .map(ref -> ref.getFieldName())
                    .filter(name -> name != null && !name.isBlank())
                    .collect(Collectors.joining("."));
            String value = String.valueOf(invalidFormat.getValue());
            if (field.isBlank()) {
                field = "request";
            }
            errors.put(field, "Giá trị không hợp lệ: " + value);
        } else {
            errors.put("request", "Dữ liệu không hợp lệ hoặc sai định dạng");
        }

        body.put("errors", errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 500);
        body.put("message", "Lỗi Server nội bộ: " + ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(org.springframework.dao.DataIntegrityViolationException ex) {
        Map<String, Object> body = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        body.put("status", 409); // Conflict
        body.put("message", "Lỗi xung đột dữ liệu!");
        
        String errorMsg = ex.getMessage() != null ? ex.getMessage() : "";
        System.err.println("[DataIntegrityViolationException] " + errorMsg);

        // Kiểm tra xem có phải lỗi trùng lặp Kanji không
        if (errorMsg.contains("uc_kanji_char")) {
            errors.put("kanji", "Chữ Kanji này đã tồn tại trong hệ thống (Master)!");
        } else if (errorMsg.contains("uk_user_kanji")) {
            errors.put("mastery", "Chữ này đã được học rồi, hệ thống sẽ cập nhật tiến độ.");
        } else {
            errors.put("database", "Vi phạm ràng buộc dữ liệu hoặc khóa ngoại. Chi tiết: " + errorMsg);
        }

        body.put("errors", errors);
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }
}
