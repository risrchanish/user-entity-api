package com.syncvault.file_service.dto;

public record FileDownloadResponse(
        String downloadUrl,
        long expiresInSeconds
) {}