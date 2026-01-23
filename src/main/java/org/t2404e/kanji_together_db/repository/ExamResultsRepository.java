package org.t2404e.kanji_together_db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.t2404e.kanji_together_db.entity.ExamResults;

public interface ExamResultsRepository extends JpaRepository<ExamResults, Long> {
}
