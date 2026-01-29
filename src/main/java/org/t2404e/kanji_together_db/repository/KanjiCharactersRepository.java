package org.t2404e.kanji_together_db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.t2404e.kanji_together_db.entity.KanjiCharacters;

import java.util.List;
import java.util.Optional;

public interface KanjiCharactersRepository extends JpaRepository<KanjiCharacters, Long> {

    // [MỚI] Tìm bản gốc đang hoạt động (Chỉ có 1 bản Active cho mỗi chữ Kanji)
    Optional<KanjiCharacters> findByKanjiAndIsActiveTrue(String kanji);

    // [MỚI] Tìm tất cả các bản ghi của chữ này (Bao gồm Active, Pending, Hidden...)
    List<KanjiCharacters> findAllByKanji(String kanji);

    // [MỚI] Lấy danh sách đóng góp (Lịch sử)
    @Query("SELECT k FROM KanjiCharacters k WHERE k.kanji = :kanji AND k.status IN ('PENDING', 'APPROVED', 'REJECTED') ORDER BY k.createAt DESC")
    List<KanjiCharacters> findContributions(@Param("kanji") String kanji);

    // Các hàm cũ giữ nguyên
    List<KanjiCharacters> findAllByIsActiveTrue();
    List<KanjiCharacters> findAllByIsActiveFalse();
    Optional<KanjiCharacters> findFirstByKanji(String kanji);

    // ========================================================
    // KHU VỰC SỬA LỖI: THÊM ĐIỀU KIỆN LỌC NGÀY VÀO SQL
    // ========================================================

    @Query(value = "SELECT * FROM kanji_characters k WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR k.kanji LIKE CONCAT('%', :keyword, '%') OR k.meaning LIKE CONCAT('%', :keyword, '%')) " +
            "AND (k.status <> 'DELETED') " +
            "AND (:isActive IS NULL OR k.is_active = :isActive) " +
            "AND (:status IS NULL OR :status = '' OR k.status = :status) " +
            // --- 👇 DÒNG NÀY QUAN TRỌNG NHẤT: Thêm điều kiện lọc ngày vào đây ---
            "AND (:createdAt IS NULL OR :createdAt = '' OR k.create_at LIKE CONCAT(:createdAt, '%')) " +
            // -------------------------------------------------------------------
            "ORDER BY k.id DESC LIMIT :limit OFFSET :offset", // Thêm ORDER BY k.id DESC để tin mới nhất lên đầu
            nativeQuery = true)
    List<KanjiCharacters> searchAndFilterPaged(
            @Param("keyword") String keyword,
            @Param("isActive") Boolean isActive,
            @Param("status") String status,
            @Param("createdAt") String createdAt,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query(value = "SELECT COUNT(*) FROM kanji_characters k WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR k.kanji LIKE CONCAT('%', :keyword, '%') OR k.meaning LIKE CONCAT('%', :keyword, '%')) " +
            "AND (k.status <> 'DELETED') " +
            "AND (:isActive IS NULL OR k.is_active = :isActive) " +
            "AND (:status IS NULL OR :status = '' OR k.status = :status) " +
            "AND (:createdAt IS NULL OR :createdAt = '' OR k.create_at LIKE CONCAT(:createdAt, '%'))",
            nativeQuery = true)
    long countSearchAndFilter(
            @Param("keyword") String keyword,
            @Param("isActive") Boolean isActive,
            @Param("status") String status,
            @Param("createdAt") String createdAt // Tham số ngày
    );
}