package org.t2404e.kanji_together_db.repository;

import org.t2404e.kanji_together_db.entity.KanjiCharacters;

import java.util.List;

public interface KanjiCharactersRepositoryCustom {
    List<KanjiCharacters> searchAndFilter(String keyword, Boolean isActive, String status, int limit, int offset);
}
