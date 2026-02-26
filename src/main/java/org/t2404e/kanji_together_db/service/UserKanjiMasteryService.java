package org.t2404e.kanji_together_db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.t2404e.kanji_together_db.entity.KanjiCharacters;
import org.t2404e.kanji_together_db.entity.UserKanjiMastery;
import org.t2404e.kanji_together_db.entity.Users;
import org.t2404e.kanji_together_db.repository.KanjiCharactersRepository;
import org.t2404e.kanji_together_db.repository.UserKanjiMasteryRepository;
import org.t2404e.kanji_together_db.repository.UsersRepository;

import java.time.LocalDateTime;

@Service
public class UserKanjiMasteryService {

    @Autowired
    private UserKanjiMasteryRepository masteryRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private KanjiCharactersRepository kanjiRepository;

    /**
     * Update or create mastery record after user answers a question
     * Uses SM2 (SuperMemo 2) algorithm for spaced repetition
     */
    @Transactional
    public UserKanjiMastery updateMastery(Long userId, Long kanjiId, boolean isCorrect) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        KanjiCharacters kanji = kanjiRepository.findById(kanjiId)
                .orElseThrow(() -> new RuntimeException("Kanji not found: " + kanjiId));

        UserKanjiMastery mastery = masteryRepository.findByUser_IdAndKanji_Id(userId, kanjiId)
                .orElseGet(() -> createNewMastery(user, kanji));

        // Update mastery based on answer correctness
        updateMasteryValues(mastery, isCorrect);

        return masteryRepository.save(mastery);
    }

    /**
     * Create a new mastery record for a user-kanji pair
     */
    private UserKanjiMastery createNewMastery(Users user, KanjiCharacters kanji) {
        UserKanjiMastery mastery = new UserKanjiMastery();
        mastery.setUser(user);
        mastery.setKanji(kanji);
        mastery.setEaseFactor(2.5); // SM2 initial ease factor
        mastery.setIntervalDays(1); // TESTING: 1 second
        mastery.setRepetitions(0);
        mastery.setTotalCorrect(0);
        mastery.setTotalWrong(0);
        mastery.setMasteryLevel(0); // 0-5 levels
        mastery.setNextReviewAt(LocalDateTime.now().plusSeconds(1)); // TESTING: 1 second
        mastery.setUpdatedAt(LocalDateTime.now());
        return mastery;
    }

    /**
     * Update mastery values using SM2 algorithm
     */
    private void updateMasteryValues(UserKanjiMastery mastery, boolean isCorrect) {
        LocalDateTime now = LocalDateTime.now();

        if (isCorrect) {
            mastery.setTotalCorrect(mastery.getTotalCorrect() + 1);
            mastery.setLastCorrectAt(now);
            mastery.setRepetitions(mastery.getRepetitions() + 1);

            // SM2 Algorithm: Calculate new interval
            int interval = calculateInterval(mastery.getRepetitions(), mastery.getIntervalDays());
            mastery.setIntervalDays(interval);

            // Update ease factor (increases with correct answers)
            double newEaseFactor = mastery.getEaseFactor() + 0.1;
            mastery.setEaseFactor(Math.max(1.3, newEaseFactor)); // Min ease factor is 1.3
        } else {
            mastery.setTotalWrong(mastery.getTotalWrong() + 1);
            mastery.setRepetitions(0); // Reset repetitions on wrong answer
            mastery.setIntervalDays(1); // Review again tomorrow
            mastery.setEaseFactor(Math.max(1.3, mastery.getEaseFactor() - 0.2)); // Decrease ease factor
        }

        mastery.setLastAttemptAt(now);
        // TESTING: Using seconds instead of days for quick testing
        mastery.setNextReviewAt(now.plusSeconds(mastery.getIntervalDays()));
        
        // Calculate mastery level (0-5)
        mastery.setMasteryLevel(calculateMasteryLevel(mastery.getTotalCorrect(), mastery.getTotalWrong()));
        
        mastery.setUpdatedAt(now);
    }

    /**
     * Calculate interval days using SM2 algorithm
     * TESTING MODE: Using seconds instead of days for quick testing
     */
    private int calculateInterval(int repetitions, int previousInterval) {
        if (repetitions == 1) {
            return 1;  // 1 second (testing)
        } else if (repetitions == 2) {
            return 2;  // 2 seconds (testing)
        } else {
            return previousInterval * 2;  // Double previous interval
        }
    }

    /**
     * Calculate mastery level based on correct/wrong attempts
     * Level 0-5: 0=new, 1=learning, 2=familiar, 3=proficient, 4=expert, 5=master
     */
    private int calculateMasteryLevel(int correct, int wrong) {
        int total = correct + wrong;
        if (total == 0) {
            return 0;
        }

        double correctPercentage = (double) correct / total * 100;

        if (correctPercentage >= 95 && correct >= 5) {
            return 5; // Master (95%+ correct, minimum 5 correct attempts)
        } else if (correctPercentage >= 85 && correct >= 4) {
            return 4; // Expert (85%+ correct, minimum 4 correct attempts)
        } else if (correctPercentage >= 70 && correct >= 3) {
            return 3; // Proficient (70%+ correct, minimum 3 correct attempts)
        } else if (correctPercentage >= 50 && correct >= 2) {
            return 2; // Familiar (50%+ correct, minimum 2 correct attempts)
        } else if (correct >= 1) {
            return 1; // Learning (at least 1 correct attempt)
        } else {
            return 0; // New (no correct attempts yet)
        }
    }
}
