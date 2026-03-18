-- Добавление внешних ключей к таблице files
ALTER TABLE files
ADD COLUMN IF NOT EXISTS user_id BIGINT,
ADD COLUMN IF NOT EXISTS folder_id BIGINT;

-- Добавление внешнего ключа к users
ALTER TABLE files
ADD CONSTRAINT fk_files_user
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;

-- Индекс для user_id
CREATE INDEX idx_files_user_id ON files(user_id);

-- Таблица для папок/категорий
CREATE TABLE IF NOT EXISTS folders (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_id BIGINT,
    created_by BIGINT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_folders_parent FOREIGN KEY (parent_id) REFERENCES folders(id) ON DELETE CASCADE,
    CONSTRAINT fk_folders_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Добавление внешнего ключа к folders
ALTER TABLE files
ADD CONSTRAINT fk_files_folder
FOREIGN KEY (folder_id) REFERENCES folders(id) ON DELETE SET NULL;

-- Индекс для folder_id
CREATE INDEX idx_files_folder_id ON files(folder_id);