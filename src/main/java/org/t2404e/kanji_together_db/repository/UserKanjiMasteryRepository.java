package org.t2404e.kanji_together_db.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.t2404e.kanji_together_db.entity.UserKanjiMastery;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserKanjiMasteryRepository extends JpaRepository<UserKanjiMastery, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM UserKanjiMastery m WHERE m.user.id = :userId AND m.kanji.id = :kanjiId")
    Optional<UserKanjiMastery> findForUpdate(@Param("userId") Long userId, @Param("kanjiId") Long kanjiId);

    Optional<UserKanjiMastery> findByUser_IdAndKanji_Id(Long userId, Long kanjiId);

    List<UserKanjiMastery> findByUser_IdAndNextReviewAtLessThanEqualOrderByNextReviewAtAsc(
            Long userId,
            LocalDateTime nextReviewAt,
            Pageable pageable
    );
}
