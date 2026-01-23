package org.t2404e.kanji_together_db.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.t2404e.kanji_together_db.dto.ExamResultCreateRequest;
import org.t2404e.kanji_together_db.dto.ExamResultResponse;
import org.t2404e.kanji_together_db.service.ExamResultsService;

@RestController
@RequestMapping("/api/v1/exam-results")
public class ExamResultsController {

    @Autowired
    private ExamResultsService examResultsService;

    @PostMapping("/start")
    public ResponseEntity<ExamResultResponse> start(@RequestBody ExamResultCreateRequest request) {
        return ResponseEntity.ok(examResultsService.start(request));
    }
}
