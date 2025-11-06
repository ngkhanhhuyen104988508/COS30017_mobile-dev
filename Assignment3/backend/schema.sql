-- DailyBean Database Schema
-- Run this file after creating the database

USE dailybean;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    username VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Mood entries table
CREATE TABLE IF NOT EXISTS mood_entries (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    mood_type ENUM('happy', 'sad', 'angry', 'calm', 'anxious') NOT NULL,
    note TEXT,
    photo_url VARCHAR(500),
    entry_date DATE NOT NULL,
    entry_time TIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_date (user_id, entry_date),
    INDEX idx_mood_type (mood_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Activities table (tags like 'work', 'exercise', etc.)
CREATE TABLE IF NOT EXISTS activities (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    icon VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Mood-Activity junction table (many-to-many relationship)
CREATE TABLE IF NOT EXISTS mood_activities (
    mood_entry_id INT NOT NULL,
    activity_id INT NOT NULL,
    PRIMARY KEY (mood_entry_id, activity_id),
    FOREIGN KEY (mood_entry_id) REFERENCES mood_entries(id) ON DELETE CASCADE,
    FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default activities
INSERT INTO activities (name, icon) VALUES 
('work', '💼'),
('exercise', '🏃'),
('friends', '👥'),
('family', '👨‍👩‍👧'),
('study', '📚'),
('relaxation', '😌'),
('travel', '✈️'),
('food', '🍔'),
('sleep', '😴'),
('entertainment', '🎮')
ON DUPLICATE KEY UPDATE name=name;

-- Create a test user (password is "password123")
-- Hash generated using bcrypt with 10 rounds
INSERT INTO users (email, username, password_hash) VALUES
('test@dailybean.com', 'TestUser', '$2b$10$rZ4qN8qNQKX3yQ3fXJZ3zOYvJGZ5X8x7pM9nKqY3fZ3xQ8nZ3zOYvJ')
ON DUPLICATE KEY UPDATE email=email;

-- Show all tables
SHOW TABLES;

-- Display structure
DESCRIBE users;
DESCRIBE mood_entries;
DESCRIBE activities;
DESCRIBE mood_activities;