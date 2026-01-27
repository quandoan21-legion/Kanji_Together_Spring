package org.t2404e.kanji_together_db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_kanji_mastery",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_kanji", columnNames = {"user_id", "kanji_id"})
        },
        indexes = {
                @Index(name = "idx_ukm_user_next_review", columnList = "user_id, next_review_at")
        }
)
@Data
@NoArgsConstructor
public class UserKanjiMastery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kanji_id", nullable = false)
    private KanjiCharacters kanji;

    @Column(name = "ease_factor", nullable = false)
    private Double easeFactor;

    @Column(name = "interval_days", nullable = false)
    private Integer intervalDays;

    @Column(name = "repetitions", nullable = false)
    private Integer repetitions;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Column(name = "last_correct_at")
    private LocalDateTime lastCorrectAt;

    @Column(name = "next_review_at", nullable = false)
    private LocalDateTime nextReviewAt;

    @Column(name = "total_correct", nullable = false)
    private Integer totalCorrect;

    @Column(name = "total_wrong", nullable = false)
    private Integer totalWrong;

    @Column(name = "mastery_level", nullable = false)
    private Integer masteryLevel;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
