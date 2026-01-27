package org.t2404e.kanji_together_db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_kanji_attempts",
        indexes = {
                @Index(name = "idx_uka_user_kanji", columnList = "user_id, kanji_id"),
                @Index(name = "idx_uka_user_answered_at", columnList = "user_id, answered_at")
        }
)
@Data
@NoArgsConstructor
public class UserKanjiAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kanji_id", nullable = false)
    private KanjiCharacters kanji;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_attempt_id", nullable = false)
    private UserQuestionAttempt questionAttempt;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;
}
