package org.t2404e.kanji_together_db.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.t2404e.kanji_together_db.dto.ApiResponse;
import org.t2404e.kanji_together_db.dto.KanjiCharacterDTO;
import org.t2404e.kanji_together_db.service.KanjiCharactersService;

@RestController
@RequestMapping("/api/v1/user/kanjis")
public class UserKanjisController {

    @Autowired
    private KanjiCharactersService service;

    @PostMapping
    public ResponseEntity<ApiResponse<KanjiCharacterDTO>> create(@RequestBody KanjiCharacterDTO dto) {
        return new ResponseEntity<>(new ApiResponse<>(201, "Tạo Kanji thành công", service.createForUser(dto)),
                HttpStatus.CREATED);
    }
}
