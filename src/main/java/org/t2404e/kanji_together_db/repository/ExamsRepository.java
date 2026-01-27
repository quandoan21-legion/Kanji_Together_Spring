package org.t2404e.kanji_together_db.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.t2404e.kanji_together_db.entity.Exams;
import org.t2404e.kanji_together_db.enums.ExamType; // <--- Import Enum

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ExamsRepository extends JpaRepository<Exams, Long> {

    @Query("SELECT e FROM Exams e WHERE " +
            "(:keyword IS NULL OR e.name LIKE %:keyword%) AND " +
            "(:type IS NULL OR e.type = :type)")
    Page<Exams> searchExams(String keyword, ExamType type, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT DISTINCT e
            FROM Exams e
            LEFT JOIN FETCH e.questions
            WHERE e.createBy = :creatorId
              AND e.type = :type
              AND e.createAt >= :start
              AND e.createAt < :end
            """)
    Optional<Exams> findDailyExamForUpdate(@Param("creatorId") Integer creatorId,
                                           @Param("type") ExamType type,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    @Query("""
            SELECT e
            FROM Exams e
            WHERE e.createBy = :creatorId
              AND e.type = :type
              AND e.createAt >= :start
              AND e.createAt < :end
            """)
    Optional<Exams> findDailyExam(@Param("creatorId") Integer creatorId,
                                  @Param("type") ExamType type,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);
}
