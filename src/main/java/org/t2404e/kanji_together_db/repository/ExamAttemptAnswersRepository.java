package org.t2404e.kanji_together_db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.t2404e.kanji_together_db.entity.ExamAttemptAnswers;

import java.time.LocalDateTime;
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

    @Query("""
            select attempt
            from ExamAttemptAnswers attempt
            join fetch attempt.question question
            join fetch question.kanjiCharacters kanji
            where attempt.is_correct = false
              and attempt.answered_at >= :since
            """)
    List<ExamAttemptAnswers> findFailedAttemptsSince(@Param("since") LocalDateTime since);

    @Query("""
            select kanji.id as kanjiId, kanji.kanji as kanji, count(attempt.id) as failCount
            from ExamAttemptAnswers attempt
            join attempt.question question
            join question.kanjiCharacters kanji
            where attempt.user.id = :userId
              and attempt.is_correct = false
            group by kanji.id, kanji.kanji
            order by failCount desc
            """)
    List<KanjiFailCount> findFailedKanjiCountsByUserId(@Param("userId") Long userId);

    interface KanjiFailCount {
        Long getKanjiId();

        String getKanji();

        Long getFailCount();
    }
}
