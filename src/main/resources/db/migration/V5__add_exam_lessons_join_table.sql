CREATE TABLE IF NOT EXISTS exam_lessons (
    exam_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    PRIMARY KEY (exam_id, lesson_id),
    CONSTRAINT fk_exam_lessons_exam
        FOREIGN KEY (exam_id) REFERENCES exams(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_exam_lessons_lesson
        FOREIGN KEY (lesson_id) REFERENCES kanji_lessons(id)
        ON DELETE CASCADE
);
