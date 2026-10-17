package com.syncvault.file_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FileResponse(
        UUID fileId,
        String fileName,
        Long fileSize,
        String contentType,
        Integer versionNumber,
        String storageKey,
        Boolean deleted,
        Boolean conflictCopy,
        UUID originalFileId,
        String aiSummary,
        String aiDescription,
        String aiTags,
        LocalDateTime aiProcessedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}