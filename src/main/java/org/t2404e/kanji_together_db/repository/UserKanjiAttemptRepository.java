package org.t2404e.kanji_together_db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.t2404e.kanji_together_db.entity.UserKanjiAttempt;

@Repository
public interface UserKanjiAttemptRepository extends JpaRepository<UserKanjiAttempt, Long> {
}
