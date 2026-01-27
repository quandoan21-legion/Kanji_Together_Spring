package org.t2404e.kanji_together_db.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.t2404e.kanji_together_db.entity.Exams;
import org.t2404e.kanji_together_db.enums.ExamType; // <--- Import Enum

@Repository
public interface ExamsRepository extends JpaRepository<Exams, Long> {

    @Query("SELECT e FROM Exams e WHERE " +
            "(:keyword IS NULL OR e.name LIKE %:keyword%) AND " +
            "(:type IS NULL OR e.type = :type)")
    Page<Exams> searchExams(String keyword, ExamType type, Pageable pageable);
}