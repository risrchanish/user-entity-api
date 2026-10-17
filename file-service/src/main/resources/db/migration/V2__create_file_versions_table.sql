CREATE TABLE file_versions (
    id UUID PRIMARY KEY,
    file_id UUID NOT NULL REFERENCES files(id),
    user_id UUID NOT NULL,
    created_by UUID NOT NULL,
    version_number INT NOT NULL,
    s3_key VARCHAR(500) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_file_versions_file_id ON file_versions(file_id);