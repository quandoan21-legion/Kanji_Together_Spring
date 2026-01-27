package org.t2404e.kanji_together_db.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.t2404e.kanji_together_db.dto.KanjiMasteryView;
import org.t2404e.kanji_together_db.entity.Exams;
import org.t2404e.kanji_together_db.entity.Questions;
import org.t2404e.kanji_together_db.enums.ExamType;
import org.t2404e.kanji_together_db.repository.ExamsRepository;
import org.t2404e.kanji_together_db.repository.QuestionsRepository;
import org.t2404e.kanji_together_db.repository.UsersRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DailyExamService {
    private static final Logger logger = LoggerFactory.getLogger(DailyExamService.class);

    private final ExamsRepository examsRepository;
    private final QuestionsRepository questionsRepository;
    private final UsersRepository usersRepository;

    public DailyExamService(ExamsRepository examsRepository,
                            QuestionsRepository questionsRepository,
                            UsersRepository usersRepository) {
        this.examsRepository = examsRepository;
        this.questionsRepository = questionsRepository;
        this.usersRepository = usersRepository;
    }

    @Transactional
    public void createOrUpdateDailyExam(Long userId, List<KanjiMasteryView> dueList) {
        if (userId == null) {
            return;
        }
        Set<Long> dueKanjiIds = extractKanjiIds(dueList);
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        Optional<Exams> existingExam = examsRepository.findDailyExamForUpdate(
                userId.intValue(), ExamType.DAILY, start, end);

        if (existingExam.isEmpty()) {
            if (dueKanjiIds.isEmpty()) {
                return;
            }
            Exams created = createDailyExamFromDueReviews(userId, today, dueKanjiIds);
            examsRepository.save(created);
            setUserHasDailyExam(userId, true);
            logger.info("Created daily exam for userId={}, kanjiCount={}, questionCount={}",
                    userId, dueKanjiIds.size(), created.getTotalQuestions());
            return;
        }

        Exams exam = existingExam.get();
        Set<Long> examKanjiIds = getExamKanjiIds(exam);

        Set<Long> newKanji = new LinkedHashSet<>(dueKanjiIds);
        newKanji.removeAll(examKanjiIds);

        Set<Long> removedKanji = new LinkedHashSet<>(examKanjiIds);
        removedKanji.removeAll(dueKanjiIds);

        if (dueKanjiIds.isEmpty()) {
            if (exam.getQuestions() != null && !exam.getQuestions().isEmpty()) {
                exam.setQuestions(new ArrayList<>());
                exam.setTotalQuestions(0);
                examsRepository.save(exam);
                logger.info("Cleared daily exam questions for userId={} (no due kanji)", userId);
            }
            setUserHasDailyExam(userId, true);
            return;
        }

        boolean updated = false;
        List<Questions> updatedQuestions = new ArrayList<>(exam.getQuestions() != null
                ? exam.getQuestions() : List.of());

        if (!removedKanji.isEmpty()) {
            updatedQuestions = updatedQuestions.stream()
                    .filter(q -> !questionHasKanji(q, removedKanji))
                    .collect(Collectors.toList());
            updated = true;
        }

        if (!newKanji.isEmpty()) {
            Set<Long> existingQuestionIds = updatedQuestions.stream()
                    .map(Questions::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            List<Questions> toAdd = questionsRepository.findActiveByKanjiIds(new ArrayList<>(newKanji));
            for (Questions question : toAdd) {
                if (question.getId() != null && !existingQuestionIds.contains(question.getId())) {
                    updatedQuestions.add(question);
                    existingQuestionIds.add(question.getId());
                }
            }
            updated = true;
        }

        if (updated) {
            exam.setQuestions(updatedQuestions);
            exam.setTotalQuestions(updatedQuestions.size());
            examsRepository.save(exam);
            logger.info("Updated daily exam for userId={}, addedKanji={}, removedKanji={}, questionCount={}",
                    userId, newKanji.size(), removedKanji.size(), updatedQuestions.size());
        }

        setUserHasDailyExam(userId, true);
    }

    private Exams createDailyExamFromDueReviews(Long userId, LocalDate today, Set<Long> dueKanjiIds) {
        Exams exam = new Exams();
        exam.setName("Daily Review - " + today);
        exam.setType(ExamType.DAILY);
        exam.setStatus(1);
        exam.setCreateBy(userId != null ? userId.intValue() : null);
        List<Questions> questions = dueKanjiIds.isEmpty()
                ? new ArrayList<>()
                : questionsRepository.findActiveByKanjiIds(new ArrayList<>(dueKanjiIds));
        exam.setQuestions(questions);
        exam.setTotalQuestions(questions.size());
        return exam;
    }

    private Set<Long> extractKanjiIds(List<KanjiMasteryView> dueList) {
        if (dueList == null || dueList.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return dueList.stream()
                .map(KanjiMasteryView::getKanjiId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<Long> getExamKanjiIds(Exams exam) {
        if (exam == null || exam.getQuestions() == null) {
            return new LinkedHashSet<>();
        }
        Set<Long> kanjiIds = new LinkedHashSet<>();
        for (Questions question : exam.getQuestions()) {
            if (question.getKanjiCharacters() == null) {
                continue;
            }
            question.getKanjiCharacters().stream()
                    .map(k -> k.getId())
                    .filter(Objects::nonNull)
                    .forEach(kanjiIds::add);
        }
        return kanjiIds;
    }

    private boolean questionHasKanji(Questions question, Set<Long> kanjiIds) {
        if (question == null || question.getKanjiCharacters() == null || kanjiIds.isEmpty()) {
            return false;
        }
        return question.getKanjiCharacters().stream()
                .map(k -> k.getId())
                .filter(Objects::nonNull)
                .anyMatch(kanjiIds::contains);
    }

    private void setUserHasDailyExam(Long userId, boolean hasDailyExam) {
        usersRepository.findById(userId).ifPresent(user -> {
            user.setHaveDailyExam(hasDailyExam);
            usersRepository.save(user);
        });
    }
}
