package org.t2404e.kanji_together_db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.t2404e.kanji_together_db.entity.ExamAttemptAnswers;

import java.util.List;

public interface ExamAttemptAnswersRepository extends JpaRepository<ExamAttemptAnswers, Long> {
    long countByUser_IdAndQuestion_Id(Long userId, Long questionId);

    @Query("""
            select distinct attempt
            from ExamAttemptAnswers attempt
            join fetch attempt.question question
            join question.kanjiCharacters kanji
            left join fetch attempt.examResult examResult
            where attempt.user.id = :userId
              and kanji.id = :kanjiId
            order by attempt.answered_at desc
            """)
    List<ExamAttemptAnswers> findByUserIdAndKanjiId(@Param("userId") Long userId, @Param("kanjiId") Long kanjiId);
}
