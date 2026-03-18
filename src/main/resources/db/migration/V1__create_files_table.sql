-- Таблица для хранения информации о файлах
CREATE TABLE IF NOT EXISTS files (
    id BIGSERIAL PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100),
    uploaded_by VARCHAR(100),
    uploaded_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    is_deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,

    CONSTRAINT uk_filename_unique UNIQUE (filename)
);

-- Индексы для улучшения производительности
CREATE INDEX idx_files_uploaded_date ON files(uploaded_date DESC);
CREATE INDEX idx_files_uploaded_by ON files(uploaded_by);
CREATE INDEX idx_files_is_deleted ON files(is_deleted);
CREATE INDEX idx_files_filename ON files(filename);

-- Комментарии к таблице и колонкам
COMMENT ON TABLE files IS 'Таблица для хранения метаданных файлов';
COMMENT ON COLUMN files.id IS 'Уникальный идентификатор файла';
COMMENT ON COLUMN files.filename IS 'Уникальное имя файла в системе';
COMMENT ON COLUMN files.original_filename IS 'Оригинальное имя файла';
COMMENT ON COLUMN files.file_path IS 'Путь к файлу в хранилище';
COMMENT ON COLUMN files.file_size IS 'Размер файла в байтах';
COMMENT ON COLUMN files.content_type IS 'MIME тип файла';
COMMENT ON COLUMN files.uploaded_by IS 'Кто загрузил файл';
COMMENT ON COLUMN files.uploaded_date IS 'Дата загрузки';
COMMENT ON COLUMN files.description IS 'Описание файла';
COMMENT ON COLUMN files.is_deleted IS 'Флаг удаления (soft delete)';