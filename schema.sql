-- =======================================================
-- TypeFlow Database Schema (MySQL 8.0+)
-- =======================================================

CREATE DATABASE IF NOT EXISTS typeflow_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE typeflow_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    avatar VARCHAR(255) DEFAULT 'U',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_username (username),
    INDEX idx_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Typing Results Table
CREATE TABLE IF NOT EXISTS typing_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    duration INT NOT NULL, -- 30, 60, 120 seconds
    raw_wpm DOUBLE NOT NULL,
    net_wpm DOUBLE NOT NULL,
    accuracy DOUBLE NOT NULL,
    correct_chars INT NOT NULL,
    incorrect_chars INT NOT NULL,
    total_chars INT NOT NULL,
    backspaces INT DEFAULT 0,
    speed_history_json TEXT NULL,
    completion_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiration_timestamp TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_tr_user_id (user_id),
    INDEX idx_tr_completion_timestamp (completion_timestamp),
    INDEX idx_tr_duration_netwpm (duration, net_wpm)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Key Errors Table (Heatmap & Diagnostics)
CREATE TABLE IF NOT EXISTS key_errors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    typing_result_id BIGINT NOT NULL,
    expected_key VARCHAR(20) NOT NULL,
    actual_key VARCHAR(20) NOT NULL,
    error_type VARCHAR(40) NOT NULL, -- CASE_A_SUBSTITUTION, CASE_B_OMISSION, CASE_B_INSERTION, CORRECTION
    error_count INT NOT NULL DEFAULT 1,
    FOREIGN KEY (typing_result_id) REFERENCES typing_results(id) ON DELETE CASCADE,
    INDEX idx_ke_result_id (typing_result_id),
    INDEX idx_ke_expected_key (expected_key),
    INDEX idx_ke_actual_key (actual_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
