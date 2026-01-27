package org.t2404e.kanji_together_db.service;

import org.t2404e.kanji_together_db.entity.UserKanjiMastery;

import java.time.LocalDateTime;

public final class SpacedRepetitionCalculator {
    private SpacedRepetitionCalculator() {
    }

    public static void apply(UserKanjiMastery mastery, boolean correct, LocalDateTime now) {
        apply(mastery, correct, now, false);
    }

    public static void apply(UserKanjiMastery mastery, boolean correct, LocalDateTime now, boolean nextReviewImmediate) {
        if (mastery == null || now == null) {
            throw new IllegalArgumentException("mastery and now must not be null");
        }

        mastery.setLastAttemptAt(now);

        if (correct) {
            mastery.setTotalCorrect(mastery.getTotalCorrect() + 1);
            mastery.setRepetitions(mastery.getRepetitions() + 1);

            int reps = mastery.getRepetitions();
            if (reps == 1) {
                mastery.setIntervalDays(1);
            } else if (reps == 2) {
                mastery.setIntervalDays(3);
            } else {
                int updatedInterval = (int) Math.round(mastery.getIntervalDays() * mastery.getEaseFactor());
                mastery.setIntervalDays(Math.max(4, updatedInterval));
            }

            mastery.setEaseFactor(mastery.getEaseFactor() + 0.1);
            if (nextReviewImmediate) {
                mastery.setNextReviewAt(now);
            } else {
                mastery.setNextReviewAt(now.plusDays(mastery.getIntervalDays()));
            }
            mastery.setMasteryLevel(Math.min(5, mastery.getMasteryLevel() + 1));
            mastery.setLastCorrectAt(now);
        } else {
            mastery.setTotalWrong(mastery.getTotalWrong() + 1);
            mastery.setRepetitions(0);
            mastery.setIntervalDays(0);
            mastery.setEaseFactor(Math.max(1.3, mastery.getEaseFactor() - 0.2));
            mastery.setNextReviewAt(now);
            mastery.setMasteryLevel(Math.max(0, mastery.getMasteryLevel() - 1));
        }

        mastery.setUpdatedAt(now);
    }
}
