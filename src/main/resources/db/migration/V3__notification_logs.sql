CREATE TABLE IF NOT EXISTS notification_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    fcm_token VARCHAR(255) NOT NULL,
    kanji_ids TEXT NOT NULL,
    kanji_hash VARCHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL,
    error_message TEXT NULL,
    create_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    edit_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_notification_logs_user_time (user_id, create_at),
    INDEX idx_notification_logs_hash_time (kanji_hash, create_at),
    CONSTRAINT fk_notification_logs_user FOREIGN KEY (user_id) REFERENCES users(id)
);
