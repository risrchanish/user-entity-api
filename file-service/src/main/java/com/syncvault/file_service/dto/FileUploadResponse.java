package com.syncvault.file_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FileUploadResponse(
        UUID fileId,
        String fileName,
        Long fileSize,
        String contentType,
        Integer versionNumber,
        String s3Key,
        LocalDateTime createdAt
) {
}
