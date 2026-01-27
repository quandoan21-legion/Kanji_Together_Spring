package org.t2404e.kanji_together_db.service;

import org.junit.jupiter.api.Test;
import org.t2404e.kanji_together_db.entity.UserKanjiMastery;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpacedRepetitionCalculatorTest {

    @Test
    void applyCorrectFromDefault() {
        UserKanjiMastery mastery = defaultMastery();
        LocalDateTime now = LocalDateTime.of(2026, 1, 27, 10, 30);

        SpacedRepetitionCalculator.apply(mastery, true, now);

        assertEquals(1, mastery.getRepetitions());
        assertEquals(1, mastery.getIntervalDays());
        assertEquals(1, mastery.getTotalCorrect());
        assertEquals(1, mastery.getMasteryLevel());
        assertEquals(2.6, mastery.getEaseFactor(), 0.0001);
        assertEquals(now.plusDays(1), mastery.getNextReviewAt());
    }

    @Test
    void applyCorrectTwiceUpdatesInterval() {
        UserKanjiMastery mastery = defaultMastery();
        LocalDateTime now = LocalDateTime.of(2026, 1, 27, 10, 30);

        SpacedRepetitionCalculator.apply(mastery, true, now);
        SpacedRepetitionCalculator.apply(mastery, true, now.plusMinutes(1));

        assertEquals(2, mastery.getRepetitions());
        assertEquals(3, mastery.getIntervalDays());
    }

    @Test
    void applyWrongResetsProgress() {
        UserKanjiMastery mastery = defaultMastery();
        LocalDateTime now = LocalDateTime.of(2026, 1, 27, 10, 30);

        SpacedRepetitionCalculator.apply(mastery, false, now);

        assertEquals(0, mastery.getRepetitions());
        assertEquals(1, mastery.getIntervalDays());
        assertEquals(1, mastery.getTotalWrong());
        assertEquals(0, mastery.getMasteryLevel());
        assertEquals(2.3, mastery.getEaseFactor(), 0.0001);
        assertEquals(now.plusDays(1), mastery.getNextReviewAt());
    }

    private UserKanjiMastery defaultMastery() {
        UserKanjiMastery mastery = new UserKanjiMastery();
        mastery.setEaseFactor(2.5);
        mastery.setIntervalDays(1);
        mastery.setRepetitions(0);
        mastery.setMasteryLevel(0);
        mastery.setTotalCorrect(0);
        mastery.setTotalWrong(0);
        mastery.setNextReviewAt(LocalDateTime.of(2026, 1, 28, 10, 30));
        mastery.setUpdatedAt(LocalDateTime.of(2026, 1, 27, 10, 30));
        return mastery;
    }
}
