package org.t2404e.kanji_together_db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.t2404e.kanji_together_db.entity.Exams;
import org.t2404e.kanji_together_db.enums.EExamType;
import org.t2404e.kanji_together_db.enums.ERank;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamsRepository extends JpaRepository<Exams, Long> {

    // 1. Tìm danh sách theo trạng thái (Active/Inactive)
    List<Exams> findAllByStatus(Integer status);

    // 2. Tìm đề thi theo Loại (VD: Tìm bài Entrance Exam đang Active)
    // Dùng Optional vì có thể chưa tạo đề nào
    Optional<Exams> findFirstByExamTypeAndStatus(EExamType examType, Integer status);

    // 3. Tìm đề thi theo Rank mục tiêu (VD: Đề thi lên Rank N4)
    List<Exams> findByTargetRankAndStatus(ERank targetRank, Integer status);

    // 4. Tìm đề thi gắn với Bài học cụ thể (Mini Exam của bài 1)
    Optional<Exams> findByLessonIdAndStatus(Long lessonId, Integer status);
}