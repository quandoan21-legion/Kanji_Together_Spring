package org.t2404e.kanji_together_db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.t2404e.kanji_together_db.entity.Exams;
import org.t2404e.kanji_together_db.repository.ExamsRepository;

import java.util.List;

@Service
public class ExamsService {

    @Autowired
    private ExamsRepository examsRepository;

    public List<Exams> getAll() {
        return examsRepository.findAll();
    }
}
