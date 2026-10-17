CREATE TABLE files (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    mime_type VARCHAR(100),
    s3_key VARCHAR(500) NOT NULL,
    version_number INT NOT NULL DEFAULT 1,
    deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_at TIMESTAMP,
    conflict_copy BOOLEAN NOT NULL DEFAULT false,
    original_file_id UUID,
    ai_summary TEXT,
    ai_description VARCHAR(500),
    ai_tags VARCHAR(255),
    ai_processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_files_user_id ON files(user_id);
CREATE INDEX idx_files_user_deleted ON files(user_id, deleted);