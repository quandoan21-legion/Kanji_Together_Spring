package org.t2404e.kanji_together_db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.t2404e.kanji_together_db.entity.KanjiLessons;

import java.util.List;

@Repository
public interface KanjiLessonsRepository extends JpaRepository<KanjiLessons, Long> {

    // 1. TÌM KIẾM & LỌC (Native Query)
    @Query(value = "SELECT * FROM kanji_lessons l WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR l.kanji LIKE CONCAT('%', :keyword, '%') OR l.lesson_description LIKE CONCAT('%', :keyword, '%')) " +
            "AND (:jlpt IS NULL OR l.JLPT = :jlpt) " +
            "AND (:status IS NULL OR :status = '' OR l.status = :status) " +
            // Lọc ngày (Dùng LIKE để so sánh chuỗi ngày "2026-01-29")
            "AND (:createdAt IS NULL OR :createdAt = '' OR l.create_at LIKE CONCAT(:createdAt, '%')) " +
            "ORDER BY l.id DESC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<KanjiLessons> searchAndFilterPaged(
            @Param("keyword") String keyword,
            @Param("jlpt") Integer jlpt,
            @Param("status") String status,
            @Param("createdAt") String createdAt,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    // 2. ĐẾM TỔNG SỐ (Để phân trang)
    @Query(value = "SELECT COUNT(*) FROM kanji_lessons l WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR l.kanji LIKE CONCAT('%', :keyword, '%') OR l.lesson_description LIKE CONCAT('%', :keyword, '%')) " +
            "AND (:jlpt IS NULL OR l.JLPT = :jlpt) " +
            "AND (:status IS NULL OR :status = '' OR l.status = :status) " +
            "AND (:createdAt IS NULL OR :createdAt = '' OR l.create_at LIKE CONCAT(:createdAt, '%'))",
            nativeQuery = true)
    long countSearchAndFilter(
            @Param("keyword") String keyword,
            @Param("jlpt") Integer jlpt,
            @Param("status") String status,
            @Param("createdAt") String createdAt
    );
}