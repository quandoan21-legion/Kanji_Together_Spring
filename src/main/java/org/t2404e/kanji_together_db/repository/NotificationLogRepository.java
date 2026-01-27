package org.t2404e.kanji_together_db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.t2404e.kanji_together_db.entity.NotificationLog;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    boolean existsByUser_IdAndKanjiHashAndCreateAtAfter(Long userId, String kanjiHash, LocalDateTime cutoff);

    List<NotificationLog> findByUser_IdOrderByCreateAtDesc(Long userId);

    @Query("SELECT l FROM NotificationLog l WHERE l.user.id = :userId AND l.kanjiHash = :kanjiHash AND l.createAt >= :cutoff")
    List<NotificationLog> findRecentByUserAndHash(@Param("userId") Long userId,
                                                  @Param("kanjiHash") String kanjiHash,
                                                  @Param("cutoff") LocalDateTime cutoff);
}
