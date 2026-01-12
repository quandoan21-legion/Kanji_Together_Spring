package org.t2404e.kanji_together_db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.t2404e.kanji_together_db.entity.KanjiStoryAi;

@Repository
public interface KanjiStoryAiRepository extends JpaRepository<KanjiStoryAi, Long> {
    boolean existsByKanji(String kanji);
}