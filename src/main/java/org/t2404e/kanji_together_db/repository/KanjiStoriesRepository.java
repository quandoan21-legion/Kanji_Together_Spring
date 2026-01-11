// KanjiStoriesRepository.java
package org.t2404e.kanji_together_db.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.t2404e.kanji_together_db.entity.KanjiStories;

public interface KanjiStoriesRepository extends JpaRepository<KanjiStories, Long> { }