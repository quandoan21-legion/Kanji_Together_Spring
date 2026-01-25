package org.t2404e.kanji_together_db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.t2404e.kanji_together_db.entity.KanjiCharacters;

import java.util.List;
import java.util.Optional;

public interface KanjiCharactersRepository extends JpaRepository<KanjiCharacters, Long>, KanjiCharactersRepositoryCustom {

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

    @Query("SELECT k FROM KanjiCharacters k WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            " lower(k.kanji) LIKE lower(concat('%', :keyword, '%')) OR " +
            " lower(k.meaning) LIKE lower(concat('%', :keyword, '%')) OR " +
            " lower(k.translation) LIKE lower(concat('%', :keyword, '%')) OR " +
            " lower(k.onPronunciation) LIKE lower(concat('%', :keyword, '%')) OR " +
            " lower(k.kunPronunciation) LIKE lower(concat('%', :keyword, '%'))) " +
            "AND " +
            "(k.status IS NULL OR k.status <> 'DELETED') " +
            "AND " +
            "(:isActive IS NULL OR k.isActive = :isActive) " +
            "AND " +
            "(:status IS NULL OR :status = '' OR k.status = :status)")
    List<KanjiCharacters> searchAndFilter(@Param("keyword") String keyword,
                                          @Param("isActive") Boolean isActive,
                                          @Param("status") String status);
}
