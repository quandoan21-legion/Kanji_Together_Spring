package org.t2404e.kanji_together_db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.t2404e.kanji_together_db.dto.ExamDTO;
import org.t2404e.kanji_together_db.dto.QuestionDTO;
import org.t2404e.kanji_together_db.entity.Exams;
import org.t2404e.kanji_together_db.entity.Questions;
import org.t2404e.kanji_together_db.enums.ExamType; // <--- Import từ package riêng
import org.t2404e.kanji_together_db.repository.ExamsRepository;
import org.t2404e.kanji_together_db.repository.QuestionsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamsService {

    @Autowired private ExamsRepository examsRepository;
    @Autowired private QuestionsRepository questionsRepository;

    @Transactional
    public ExamDTO saveExam(ExamDTO dto) {
        Exams exam;
        if (dto.getId() != null) {
            exam = examsRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Exam ID: " + dto.getId()));
        } else {
            exam = new Exams();
        }

        exam.setName(dto.getName());
        exam.setDuration(dto.getDuration());
        exam.setPassScore(dto.getPassScore());
        exam.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        exam.setTargetRank(dto.getTargetRank());

        // Map Enum
        if (dto.getType() != null) {
            try {
                exam.setType(ExamType.valueOf(dto.getType()));
            } catch (IllegalArgumentException e) {
                exam.setType(ExamType.MINI);
            }
        }

        // Many-to-Many
        if (dto.getQuestionIds() != null) {
            List<Questions> selectedQuestions = questionsRepository.findAllById(dto.getQuestionIds());
            exam.setQuestions(selectedQuestions);
            exam.setTotalQuestions(selectedQuestions.size());
        } else if (exam.getId() == null) {
            exam.setQuestions(new ArrayList<>());
            exam.setTotalQuestions(0);
        }

        Exams saved = examsRepository.save(exam);
        return mapToDTO(saved);
    }

    public Page<ExamDTO> getAllExams(String keyword, String typeStr, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        ExamType type = null;
        if (typeStr != null && !typeStr.isEmpty()) {
            try {
                type = ExamType.valueOf(typeStr);
            } catch (Exception e) {}
        }

        Page<Exams> pageResult = examsRepository.searchExams(keyword, type, pageable);
        return pageResult.map(this::mapToDTO);
    }

    public ExamDTO getExamById(Long id) {
        Exams exam = examsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
        return mapToDTO(exam);
    }

    public void deleteExam(Long id) {
        examsRepository.deleteById(id);
    }

    private ExamDTO mapToDTO(Exams entity) {
        ExamDTO dto = new ExamDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setType(entity.getType() != null ? entity.getType().name() : ExamType.MINI.name());
        dto.setDuration(entity.getDuration());
        dto.setPassScore(entity.getPassScore());
        dto.setStatus(entity.getStatus());
        dto.setTargetRank(entity.getTargetRank());

        // [SỬA ĐOẠN NÀY] Gọi đúng tên getter trong Entity
        dto.setCreatedAt(entity.getCreateAt());
        dto.setUpdatedAt(entity.getEditAt());

        if (entity.getQuestions() != null) {
            dto.setTotalQuestions(entity.getQuestions().size());
            dto.setQuestionIds(entity.getQuestions().stream()
                    .map(Questions::getId)
                    .collect(Collectors.toList()));

            List<QuestionDTO> fullQ = entity.getQuestions().stream().map(q -> {
                QuestionDTO qDto = new QuestionDTO();
                qDto.setId(q.getId());
                qDto.setQuestionText(q.getQuestionText());
                qDto.setQuestionType(q.getQuestionType());
                qDto.setCorrectAnswer(q.getCorrectAnswer()); // Nếu cần
                return qDto;
            }).collect(Collectors.toList());
            dto.setFullQuestions(fullQ);
        }

        return dto;
    }
}