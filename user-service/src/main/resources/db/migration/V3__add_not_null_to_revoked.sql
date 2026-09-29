UPDATE refresh_tokens SET revoked = FALSE WHERE revoked IS NULL;
ALTER TABLE refresh_tokens ALTER COLUMN revoked SET NOT NULL;