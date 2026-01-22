package org.t2404e.kanji_together_db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.t2404e.kanji_together_db.entity.Questions;
import java.util.List;

public interface QuestionsRepository extends JpaRepository<Questions, Long> {

    // Tìm tất cả câu đang hoạt động (Status = 1)
    List<Questions> findAllByStatus(Integer status);

    // Lọc theo loại và status
    List<Questions> findByQuestionTypeAndStatus(String questionType, Integer status);

    // Tìm kiếm theo nội dung và status
    List<Questions> findByQuestionTextContainingAndStatus(String text, Integer status);

    // Lọc theo Exam ID và status
    List<Questions> findByExamIdAndStatus(Long examId, Integer status);
}