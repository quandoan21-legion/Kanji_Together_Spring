CREATE TABLE IF NOT EXISTS user_device_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    fcm_token VARCHAR(255) NOT NULL,
    platform VARCHAR(20) NULL,
    device_id VARCHAR(255) NULL,
    app_version VARCHAR(50) NULL,
    last_seen_at DATETIME NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    create_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    edit_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_device_tokens_fcm (fcm_token),
    CONSTRAINT fk_user_device_tokens_user FOREIGN KEY (user_id) REFERENCES users(id)
);

SET @has_token := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'user_device_tokens'
      AND column_name = 'token'
);
SET @rename_token := IF(
    @has_token > 0,
    'ALTER TABLE user_device_tokens CHANGE COLUMN token fcm_token VARCHAR(255) NOT NULL',
    'SELECT 1'
);
PREPARE stmt FROM @rename_token;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_device_id := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'user_device_tokens'
      AND column_name = 'device_id'
);
SET @add_device_id := IF(
    @has_device_id > 0,
    'SELECT 1',
    'ALTER TABLE user_device_tokens ADD COLUMN device_id VARCHAR(255) NULL'
);
PREPARE stmt FROM @add_device_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_app_version := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'user_device_tokens'
      AND column_name = 'app_version'
);
SET @add_app_version := IF(
    @has_app_version > 0,
    'SELECT 1',
    'ALTER TABLE user_device_tokens ADD COLUMN app_version VARCHAR(50) NULL'
);
PREPARE stmt FROM @add_app_version;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE user_device_tokens
    MODIFY COLUMN platform ENUM('ANDROID','IOS') NULL;
