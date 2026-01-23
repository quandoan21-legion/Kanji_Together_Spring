package org.t2404e.kanji_together_db.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.t2404e.kanji_together_db.entity.Exams;
import org.t2404e.kanji_together_db.service.ExamsService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exams")
public class ExamsController {

    @Autowired
    private ExamsService examsService;

    @GetMapping
    public List<Exams> getAll() {
        return examsService.getAll();
    }
}
