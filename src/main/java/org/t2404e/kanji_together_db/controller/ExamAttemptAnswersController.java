package org.t2404e.kanji_together_db.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.t2404e.kanji_together_db.dto.ApiResponse;
import org.t2404e.kanji_together_db.dto.ExamAttemptAnswerRequest;
import org.t2404e.kanji_together_db.dto.ExamAttemptAnswerResponse;
import org.t2404e.kanji_together_db.service.ExamAttemptAnswersService;

@RestController
@RequestMapping("/api/v1/exam-attempt-answers")
@CrossOrigin(origins = "*")
public class ExamAttemptAnswersController {

    @Autowired
    private ExamAttemptAnswersService service;

    /**
     * Submit an answer for a question during an exam
     * This endpoint will:
     * 1. Save the answer attempt
     * 2. Update user mastery for the associated kanji
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ExamAttemptAnswerResponse>> submitAnswer(
            @RequestBody ExamAttemptAnswerRequest request) {
        ExamAttemptAnswerResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Đáp án đã được lưu và cập nhật mastery", response));
    }
}
