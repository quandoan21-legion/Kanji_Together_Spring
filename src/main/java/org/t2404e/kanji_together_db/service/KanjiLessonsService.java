package org.t2404e.kanji_together_db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.t2404e.kanji_together_db.dto.KanjiLessonDTO;
import org.t2404e.kanji_together_db.entity.KanjiCharacters;
import org.t2404e.kanji_together_db.entity.Exams;
import org.t2404e.kanji_together_db.entity.KanjiLessons;
import org.t2404e.kanji_together_db.entity.Questions;
import org.t2404e.kanji_together_db.enums.ExamType;
import org.t2404e.kanji_together_db.repository.ExamsRepository;
import org.t2404e.kanji_together_db.repository.KanjiCharactersRepository;
import org.t2404e.kanji_together_db.repository.KanjiLessonsRepository;
import org.t2404e.kanji_together_db.repository.QuestionsRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KanjiLessonsService {

    @Autowired
    private KanjiLessonsRepository repository;

    @Autowired
    private KanjiCharactersRepository kanjiRepo;

    @Autowired
    private ExamsRepository examsRepository;

    @Autowired
    private QuestionsRepository questionsRepository;

    // --- 1. LẤY DANH SÁCH ---
    public Map<String, Object> getAll(String keyword, Integer jlpt, String status, String createdAt, Integer page, Integer size) {
        int finalLimit = (size != null) ? size : 10;
        Integer javaPage = (page != null) ? page : 0;
        int offset = finalLimit * javaPage;

        List<KanjiLessons> entities = repository.searchAndFilterPaged(keyword, jlpt, status, createdAt, finalLimit, offset);
        List<Map<String, Object>> dtos = entities.stream().map(this::mapToListDTO).collect(Collectors.toList());

        long totalElements = repository.countSearchAndFilter(keyword, jlpt, status, createdAt);
        int totalPages = (int) Math.ceil((double) totalElements / finalLimit);

        Map<String, Object> response = new HashMap<>();
        response.put("lessons", dtos);
        response.put("totalElements", totalElements);
        response.put("totalPages", totalPages);
        response.put("currentPage", javaPage + 1);

        return response;
    }

    // --- 2. TẠO MỚI (CÓ LOG DEBUG) ---
    public KanjiLessonDTO create(KanjiLessonDTO dto) {
        // --- IN LOG ĐỂ KIỂM TRA DỮ LIỆU ĐẦU VÀO ---
        System.out.println(">>> DEBUG CREATE START <<<");
        System.out.println("1. Tên bài: " + dto.getKanji());
        System.out.println("2. Mô tả: " + dto.getLessonDescription());
        System.out.println("3. List ID Kanji: " + dto.getKanjiIds());
        // -------------------------------------------

        if (dto.getKanji() == null || dto.getKanji().trim().isEmpty()) {
            throw new RuntimeException("Tên bài học không được để trống!");
        }

        if (dto.getKanji().length() > 20) {
            throw new RuntimeException("Tên bài học quá dài (Tối đa 20 ký tự)!");
        }

        if (dto.getJlpt() == null || dto.getJlpt() < 1 || dto.getJlpt() > 5) {
            throw new RuntimeException("Cấp độ JLPT không hợp lệ (Phải từ 1 đến 5)!");
        }

        KanjiLessons entity = mapToEntity(dto);

        if (entity.getStatus() == null || entity.getStatus().isEmpty()) {
            entity.setStatus("ACTIVE");
        }

        entity.setCreateAt(LocalDateTime.now());
        entity.setEditAt(LocalDateTime.now());

        // LƯU DANH SÁCH KANJI
        if (dto.getKanjiIds() != null && !dto.getKanjiIds().isEmpty()) {
            List<KanjiCharacters> kanjis = kanjiRepo.findAllById(dto.getKanjiIds());
            entity.setKanjiCharacters(kanjis);
            System.out.println(">>> DEBUG: Tìm thấy " + kanjis.size() + " Kanji trong DB.");
        } else {
            System.out.println(">>> DEBUG: Không có Kanji nào được chọn.");
        }

        if (dto.getExamIds() != null) {
            List<Exams> exams = examsRepository.findAllById(dto.getExamIds());
            entity.setExams(exams);
            System.out.println(">>> DEBUG: Liên kết " + exams.size() + " exam với bài học.");
        }

        KanjiLessons saved = repository.save(entity);
        System.out.println(">>> DEBUG: Đã lưu thành công bài học ID: " + saved.getId());

        Exams savedExam = null;
        Exams exam = buildLessonExam(saved, dto);
        if (exam != null) {
            savedExam = examsRepository.save(exam);
            List<Exams> updatedExams = new ArrayList<>();
            if (saved.getExams() != null) {
                updatedExams.addAll(saved.getExams());
            }
            updatedExams.add(savedExam);
            saved.setExams(updatedExams);
            saved = repository.save(saved);
        }

        KanjiLessonDTO response = mapToDTO(saved);
        if (savedExam != null) {
            List<Long> examIds = response.getExamIds();
            if (examIds == null) {
                examIds = new ArrayList<>();
            }
            if (!examIds.contains(savedExam.getId())) {
                examIds.add(savedExam.getId());
            }
            response.setExamIds(examIds);
        }

        return response;
    }

    // --- 3. CẬP NHẬT (CÓ LOG DEBUG) ---
    public KanjiLessonDTO update(Long id, KanjiLessonDTO newData) {
        System.out.println(">>> DEBUG UPDATE ID: " + id);
        System.out.println(">>> Dữ liệu update: Mô tả=" + newData.getLessonDescription() + ", IDs=" + newData.getKanjiIds());

        if (newData.getKanji() != null && newData.getKanji().trim().isEmpty()) {
            throw new RuntimeException("Tên bài học không được để trống!");
        }

        if (newData.getKanji() != null && newData.getKanji().length() > 20) {
            throw new RuntimeException("Tên bài học quá dài (Tối đa 20 ký tự)!");
        }

        if (newData.getJlpt() != null && (newData.getJlpt() < 1 || newData.getJlpt() > 5)) {
            throw new RuntimeException("Cấp độ JLPT phải từ 1 đến 5!");
        }

        return repository.findById(id).map(lesson -> {
            lesson.setKanji(newData.getKanji());
            lesson.setJlpt(newData.getJlpt());

            // QUAN TRỌNG: Cập nhật mô tả
            lesson.setLessonDescription(newData.getLessonDescription());

            lesson.setStatus(newData.getStatus());
            lesson.setEditAt(LocalDateTime.now());

            // QUAN TRỌNG: Cập nhật Kanji
            if (newData.getKanjiIds() != null) {
                List<KanjiCharacters> kanjis = kanjiRepo.findAllById(newData.getKanjiIds());
                lesson.setKanjiCharacters(kanjis);
                System.out.println(">>> DEBUG: Đã update " + kanjis.size() + " kanji cho bài học.");
            }

            if (newData.getExamIds() != null) {
                List<Exams> exams = examsRepository.findAllById(newData.getExamIds());
                lesson.setExams(exams);
                System.out.println(">>> DEBUG: Đã update " + exams.size() + " exam cho bài học.");
            }

            return mapToDTO(repository.save(lesson));
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy bài học với ID: " + id));
    }

    public KanjiLessonDTO getDetail(Long id) {
        return repository.findById(id).map(this::mapToDetailDTO).orElse(null);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    // ========== HELPER MAPPING ==========
    private KanjiLessonDTO mapToDTO(KanjiLessons entity) {
        KanjiLessonDTO dto = new KanjiLessonDTO();
        dto.setId(entity.getId());
        dto.setKanji(entity.getKanji());
        dto.setJlpt(entity.getJlpt());

        // Map mô tả
        dto.setLessonDescription(entity.getLessonDescription());

        dto.setStatus(entity.getStatus());
        dto.setCreateAt(entity.getCreateAt());
        dto.setEditAt(entity.getEditAt());
        dto.setCreateBy(entity.getCreateBy());
        dto.setEditBy(entity.getEditBy());

        if (entity.getKanjiCharacters() != null) {
            // 1. List ID
            dto.setKanjiIds(entity.getKanjiCharacters().stream()
                    .map(KanjiCharacters::getId)
                    .collect(Collectors.toList()));

            // 2. List Minimal Info (id + kanji)
            List<KanjiLessonDTO.KanjiFullInfo> minimalInfos = entity.getKanjiCharacters().stream().map(k -> {
                KanjiLessonDTO.KanjiFullInfo info = new KanjiLessonDTO.KanjiFullInfo();
                info.setId(k.getId());
                info.setKanji(k.getKanji());
                info.setJlpt(k.getJlpt());
                return info;
            }).collect(Collectors.toList());

            dto.setKanjiList(minimalInfos);
        }

        if (entity.getExams() != null) {
            dto.setExamIds(entity.getExams().stream()
                    .map(Exams::getId)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private Map<String, Object> mapToListDTO(KanjiLessons entity) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", entity.getId());
        dto.put("kanji", entity.getKanji());
        dto.put("jlpt", entity.getJlpt());
        dto.put("lesson_description", entity.getLessonDescription());
        dto.put("status", entity.getStatus());
        dto.put("create_at", entity.getCreateAt());
        return dto;
    }

    private KanjiLessonDTO mapToDetailDTO(KanjiLessons entity) {
        KanjiLessonDTO dto = mapToDTO(entity);

        if (entity.getKanjiCharacters() != null && !entity.getKanjiCharacters().isEmpty()) {
            String combinedKanji = entity.getKanjiCharacters().stream()
                    .map(KanjiCharacters::getKanji)
                    .collect(Collectors.joining(""));
            dto.setKanji(combinedKanji);
        }

        return dto;
    }

    private KanjiLessons mapToEntity(KanjiLessonDTO dto) {
        KanjiLessons entity = new KanjiLessons();
        entity.setKanji(dto.getKanji());
        entity.setJlpt(dto.getJlpt());

        // Map mô tả vào Entity
        entity.setLessonDescription(dto.getLessonDescription());

        entity.setStatus(dto.getStatus());
        return entity;
    }

    private Exams buildLessonExam(KanjiLessons lesson, KanjiLessonDTO dto) {
        Exams exam = new Exams();
        String lessonDesc = dto.getLessonDescription() != null ? dto.getLessonDescription().trim() : "";
        if (lessonDesc.isEmpty()) {
            lessonDesc = "Lesson " + lesson.getId();
        }
        exam.setName(lessonDesc + " Exam");
        exam.setType(ExamType.MINI);
        exam.setStatus(1);
        exam.setLessons(List.of(lesson));

        if (dto.getKanjiIds() != null && !dto.getKanjiIds().isEmpty()) {
            List<Questions> questions = questionsRepository.findActiveByKanjiIds(dto.getKanjiIds());
            exam.setQuestions(questions);
            exam.setTotalQuestions(questions.size());
        } else {
            exam.setQuestions(new ArrayList<>());
            exam.setTotalQuestions(0);
        }

        int totalQuestions = exam.getTotalQuestions() != null ? exam.getTotalQuestions() : 0;
        int passScore = totalQuestions == 0 ? 0 : (int) Math.ceil(totalQuestions * 0.75);
        exam.setDuration(15);
        exam.setPassScore(passScore);
        if (lesson.getJlpt() != null) {
            exam.setTargetRank("N" + lesson.getJlpt());
        }

        return exam;
    }
}
