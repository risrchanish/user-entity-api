package com.syncvault.file_service.dto;

import java.util.UUID;

public record ConflictResponse(
        UUID conflictCopyId,
        String conflictFileName,
        String message
) {}