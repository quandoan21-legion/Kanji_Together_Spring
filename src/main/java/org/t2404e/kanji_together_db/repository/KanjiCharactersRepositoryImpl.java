package org.t2404e.kanji_together_db.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.t2404e.kanji_together_db.entity.KanjiCharacters;

import java.util.List;

@Repository
public class KanjiCharactersRepositoryImpl implements KanjiCharactersRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<KanjiCharacters> searchAndFilter(String keyword, Boolean isActive, String status, int limit, int offset) {
        String jpql = "SELECT k FROM KanjiCharacters k WHERE " +
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
                "(:status IS NULL OR :status = '' OR k.status = :status)";

        TypedQuery<KanjiCharacters> query = entityManager.createQuery(jpql, KanjiCharacters.class);
        query.setParameter("keyword", keyword);
        query.setParameter("isActive", isActive);
        query.setParameter("status", status);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }
}
