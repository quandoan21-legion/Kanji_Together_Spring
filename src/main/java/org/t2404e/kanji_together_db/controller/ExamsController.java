package org.t2404e.kanji_together_db.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.t2404e.kanji_together_db.entity.Exams;
import org.t2404e.kanji_together_db.service.ExamsService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exams")
@CrossOrigin(origins = "*")
public class ExamsController {

    @Autowired
    private ExamsService examsService;

    @GetMapping
    public ResponseEntity<List<Exams>> getAll() {
        return ResponseEntity.ok(examsService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Exams> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(examsService.getDetail(id));
    }

    @PostMapping
    public ResponseEntity<Exams> create(@RequestBody Exams exam) {
        return ResponseEntity.ok(examsService.create(exam));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Exams> update(@PathVariable Long id, @RequestBody Exams exam) {
        return ResponseEntity.ok(examsService.update(id, exam));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        examsService.delete(id);
        return ResponseEntity.ok().build();
    }
}