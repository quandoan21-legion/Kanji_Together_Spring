package org.t2404e.kanji_together_db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.t2404e.kanji_together_db.dto.CourseDTO;
import org.t2404e.kanji_together_db.entity.Courses;
import org.t2404e.kanji_together_db.entity.KanjiLessons;
import org.t2404e.kanji_together_db.enums.CourseCategory;
import org.t2404e.kanji_together_db.repository.CoursesRepository;
import org.t2404e.kanji_together_db.repository.KanjiLessonsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CoursesService {

    @Autowired
    private CoursesRepository coursesRepository;

    @Autowired
    private KanjiLessonsRepository lessonsRepository;

    public List<CourseDTO> getAll() {
        return coursesRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public CourseDTO getDetail(Long id) {
        return coursesRepository.findById(id).map(this::mapToDTO).orElse(null);
    }

    @Transactional
    public CourseDTO create(CourseDTO dto) {
        Courses course = new Courses();
        apply(dto, course);
        Courses saved = coursesRepository.save(course);
        updateLessons(saved, dto.getLessonIds());
        return mapToDTO(saved);
    }

    @Transactional
    public CourseDTO update(Long id, CourseDTO dto) {
        Courses course = coursesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học với ID: " + id));
        apply(dto, course);
        Courses saved = coursesRepository.save(course);
        updateLessons(saved, dto.getLessonIds());
        return mapToDTO(saved);
    }

    public void delete(Long id) {
        coursesRepository.deleteById(id);
    }

    private void apply(CourseDTO dto, Courses course) {
        course.setName(dto.getName());
        course.setDescription(dto.getDescription());
        if (dto.getCategory() != null) {
            course.setCategory(CourseCategory.valueOf(dto.getCategory()));
        }
        course.setThumbnailUrl(dto.getThumbnailUrl());
        course.setCoverImageUrl(dto.getCoverImageUrl());
        course.setTimeToFinish(dto.getTimeToFinish());
    }

    private void updateLessons(Courses course, List<Long> lessonIds) {
        if (lessonIds == null) {
            return;
        }

        List<KanjiLessons> current = lessonsRepository.findByCourseId(course.getId());
        for (KanjiLessons lesson : current) {
            if (!lessonIds.contains(lesson.getId())) {
                lesson.setCourse(null);
            }
        }
        lessonsRepository.saveAll(current);

        List<KanjiLessons> selected = lessonsRepository.findAllById(lessonIds);
        for (KanjiLessons lesson : selected) {
            lesson.setCourse(course);
        }
        lessonsRepository.saveAll(selected);
    }

    private CourseDTO mapToDTO(Courses entity) {
        CourseDTO dto = new CourseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setCategory(entity.getCategory() != null ? entity.getCategory().name() : null);
        dto.setThumbnailUrl(entity.getThumbnailUrl());
        dto.setCoverImageUrl(entity.getCoverImageUrl());
        dto.setTimeToFinish(entity.getTimeToFinish());

        List<KanjiLessons> lessons = entity.getLessons();
        if (lessons != null) {
            dto.setLessonIds(lessons.stream().map(KanjiLessons::getId).collect(Collectors.toList()));
        } else {
            dto.setLessonIds(new ArrayList<>());
        }

        return dto;
    }
}
