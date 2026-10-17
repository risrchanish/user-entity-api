package com.syncvault.file_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FileUpdateResponse(
        UUID fileId,
        String fileName,
        Long fileSize,
        Integer versionNumber,
        String storageKey,
        LocalDateTime updatedAt
) {
}