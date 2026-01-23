package org.t2404e.kanji_together_db.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.t2404e.kanji_together_db.dto.ExamAttemptAnswerRequest;
import org.t2404e.kanji_together_db.dto.ExamAttemptAnswerResponse;
import org.t2404e.kanji_together_db.service.ExamAttemptAnswersService;

@RestController
@RequestMapping("/api/v1/exam-attempt-answers")
public class ExamAttemptAnswersController {

    @Autowired
    private ExamAttemptAnswersService examAttemptAnswersService;

    @PostMapping
    public ResponseEntity<ExamAttemptAnswerResponse> create(@RequestBody ExamAttemptAnswerRequest request) {
        return ResponseEntity.ok(examAttemptAnswersService.create(request));
    }
}
