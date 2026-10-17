package com.syncvault.file_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FileVersionResponse(
        UUID versionId,
        Integer versionNumber,
        String s3Key,
        String fileName,
        Long fileSize,
        UUID createdBy,
        LocalDateTime createdAt
) {
}