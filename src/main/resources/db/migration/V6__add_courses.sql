CREATE TABLE IF NOT EXISTS courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    thumbnail_url VARCHAR(255),
    cover_image_url VARCHAR(255),
    time_to_finish VARCHAR(50) NOT NULL,
    create_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    edit_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

ALTER TABLE kanji_lessons
    ADD COLUMN course_id BIGINT NULL,
    ADD CONSTRAINT fk_kanji_lessons_course
        FOREIGN KEY (course_id) REFERENCES courses(id)
        ON DELETE SET NULL;
