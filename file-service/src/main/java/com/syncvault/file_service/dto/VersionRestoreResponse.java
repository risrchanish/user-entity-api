package com.syncvault.file_service.dto;

import java.util.UUID;

public record VersionRestoreResponse(
        UUID fileId,
        String fileName,
        Long fileSize,
        Integer restoredFromVersion,
        Integer currentVersion,
        String message
) {}