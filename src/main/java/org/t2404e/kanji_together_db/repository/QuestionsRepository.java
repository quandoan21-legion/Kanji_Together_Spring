package org.t2404e.kanji_together_db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.t2404e.kanji_together_db.entity.Questions;

import java.util.List;

@Repository
public interface QuestionsRepository extends JpaRepository<Questions, Long> {

    // 1. Tìm tất cả câu đang hoạt động
    List<Questions> findAllByStatus(Integer status);

    // 2. Lọc theo loại và status
    List<Questions> findByQuestionTypeAndStatus(String questionType, Integer status);

    // 3. Tìm kiếm theo nội dung text
    List<Questions> findByQuestionTextContainingAndStatus(String text, Integer status);

    // 4. Lọc theo Exam ID
    @Query("SELECT q FROM Questions q JOIN q.exams e WHERE e.id = :examId AND q.status = :status")
    List<Questions> findByExamIdAndStatus(@Param("examId") Long examId, @Param("status") Integer status);

    // 5. MỚI: Tìm kiếm câu hỏi theo chữ Kanji (ĐÃ SỬA LỖI)
    @Query("SELECT DISTINCT q FROM Questions q JOIN q.kanjiCharacters k WHERE k.kanji LIKE %:kanji% AND q.status = 1")
    List<Questions> findByKanjiCharacter(@Param("kanji") String kanji);
}