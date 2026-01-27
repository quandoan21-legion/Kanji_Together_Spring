CREATE TABLE user_question_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    is_correct BOOLEAN NOT NULL,
    selected_answer VARCHAR(255) NULL,
    time_spent_ms INT NULL,
    answered_at DATETIME NOT NULL,
    INDEX idx_uqa_user_answered_at (user_id, answered_at),
    CONSTRAINT fk_uqa_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_uqa_question FOREIGN KEY (question_id) REFERENCES questions(id)
);

CREATE TABLE user_kanji_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    kanji_id BIGINT NOT NULL,
    question_attempt_id BIGINT NOT NULL,
    is_correct BOOLEAN NOT NULL,
    answered_at DATETIME NOT NULL,
    INDEX idx_uka_user_kanji (user_id, kanji_id),
    INDEX idx_uka_user_answered_at (user_id, answered_at),
    CONSTRAINT fk_uka_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_uka_kanji FOREIGN KEY (kanji_id) REFERENCES kanji_characters(id),
    CONSTRAINT fk_uka_question_attempt FOREIGN KEY (question_attempt_id) REFERENCES user_question_attempts(id)
);

CREATE TABLE user_kanji_mastery (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    kanji_id BIGINT NOT NULL,
    ease_factor DOUBLE NOT NULL DEFAULT 2.5,
    interval_days INT NOT NULL DEFAULT 1,
    repetitions INT NOT NULL DEFAULT 0,
    last_attempt_at DATETIME NULL,
    last_correct_at DATETIME NULL,
    next_review_at DATETIME NOT NULL,
    total_correct INT NOT NULL DEFAULT 0,
    total_wrong INT NOT NULL DEFAULT 0,
    mastery_level INT NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_user_kanji (user_id, kanji_id),
    INDEX idx_ukm_user_next_review (user_id, next_review_at),
    CONSTRAINT fk_ukm_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_ukm_kanji FOREIGN KEY (kanji_id) REFERENCES kanji_characters(id)
);
