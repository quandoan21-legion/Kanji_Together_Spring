package org.t2404e.kanji_together_db.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.t2404e.kanji_together_db.dto.AiStoryRequest;
import org.t2404e.kanji_together_db.dto.ApiResponse;
import org.t2404e.kanji_together_db.entity.KanjiStoryAi;
import org.t2404e.kanji_together_db.service.AiKanjiService;

@RestController
@RequestMapping("/api/v1/ai-kanji")
public class AiKanjiController {

    @Autowired
    private AiKanjiService aiService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<KanjiStoryAi>> generateStory(@RequestBody AiStoryRequest request) {
        KanjiStoryAi result = aiService.createStoryFromAi(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "AI tạo story thành công!", result));
    }
}