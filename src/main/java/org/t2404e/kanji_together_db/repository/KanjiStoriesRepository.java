package org.t2404e.kanji_together_db.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.t2404e.kanji_together_db.entity.KanjiStories;

@Repository
public interface KanjiStoriesRepository extends JpaRepository<KanjiStories, Long> {

    @Query("SELECT s FROM KanjiStories s " +
            "LEFT JOIN s.kanjiCharacter k " +
            "WHERE " +
            "(:status IS NULL OR s.status = :status) " +
            "AND (:kanjiText IS NULL OR k.kanji LIKE %:kanjiText%) " +
            "AND (:kanjiId IS NULL OR k.id = :kanjiId)")
    Page<KanjiStories> findAllFiltered(
            @Param("status") String status,
            @Param("kanjiText") String kanjiText,
            @Param("kanjiId") Long kanjiId,
            Pageable pageable);
}