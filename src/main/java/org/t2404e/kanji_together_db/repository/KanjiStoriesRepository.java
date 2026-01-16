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

    @Query("SELECT s FROM KanjiStories s WHERE " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:email IS NULL OR s.user.email LIKE %:email%) AND " +
            "(:kanjiId IS NULL OR s.kanjiCharacter.id = :kanjiId)")
    Page<KanjiStories> findAllFiltered(
            @Param("status") String status,
            @Param("email") String email,
            @Param("kanjiId") Long kanjiId,
            Pageable pageable);
}