
USE todolist;

ALTER DATABASE todolist CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT SELECT, INSERT, UPDATE, DELETE ON todolist.* TO 'todolist_user'@'%';
GRANT CREATE, ALTER, DROP ON todolist.* TO 'todolist_user'@'%';
FLUSH PRIVILEGES;

CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    is_completed BOOLEAN DEFAULT FALSE,
    priority INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    INDEX idx_is_completed (is_completed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_user_active ON user(is_active);
CREATE INDEX idx_task_priority ON task(priority);
CREATE INDEX idx_task_user_completed ON task(user_id, is_completed);


CREATE OR REPLACE VIEW vw_tasks_summary AS
SELECT 
    u.id AS user_id,
    u.username,
    COUNT(*) AS total_tasks,
    SUM(CASE WHEN t.is_completed = TRUE THEN 1 ELSE 0 END) AS completed_tasks,
    SUM(CASE WHEN t.is_completed = FALSE THEN 1 ELSE 0 END) AS pending_tasks,
    MAX(t.created_at) AS last_task_date
FROM user u
LEFT JOIN task t ON u.id = t.user_id
WHERE u.is_active = TRUE
GROUP BY u.id, u.username;


SHOW TABLES;

DESCRIBE user;

DESCRIBE task;
