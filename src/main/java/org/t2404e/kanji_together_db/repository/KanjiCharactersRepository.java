
package org.t2404e.kanji_together_db.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.t2404e.kanji_together_db.entity.KanjiCharacters;

import java.util.List;
import java.util.Optional;

public interface KanjiCharactersRepository extends JpaRepository<KanjiCharacters, Long> {
    Optional<KanjiCharacters> findByKanji(String kanji);
    List<KanjiCharacters> findAllByIsActiveTrue();
}