package com.syncvault.file_service.dto;

public record PatchFileRequest(
        String fileName,
        String aiDescription
) {}