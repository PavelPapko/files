-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(50) DEFAULT 'USER',
    is_active BOOLEAN DEFAULT TRUE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_date TIMESTAMP,

    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email)
);

-- Индексы для пользователей
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- Комментарии
COMMENT ON TABLE users IS 'Таблица пользователей системы';
COMMENT ON COLUMN users.username IS 'Уникальное имя пользователя';
COMMENT ON COLUMN users.email IS 'Email пользователя';
COMMENT ON COLUMN users.password_hash IS 'Хеш пароля';
COMMENT ON COLUMN users.role IS 'Роль пользователя (USER, ADMIN)';
COMMENT ON COLUMN users.is_active IS 'Активен ли пользователь';

-- Добавление тестового пользователя
INSERT INTO users (username, email, password_hash, first_name, last_name, role) VALUES
('admin', 'admin@example.com', '$2a$10$XN1NJlQ0PZwZ6GqY6yQ7LOqX7y5y5y5y5y5y5y5y5y5y5y5y5y', 'Admin', 'User', 'ADMIN');

INSERT INTO users (username, email, password_hash, first_name, last_name, role) VALUES
('user1', 'user1@example.com', '$2a$10$XN1NJlQ0PZwZ6GqY6yQ7LOqX7y5y5y5y5y5y5y5y5y5y5y5y', 'Test', 'User', 'USER');