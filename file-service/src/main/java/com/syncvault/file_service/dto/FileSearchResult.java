package com.syncvault.file_service.dto;

import java.util.UUID;

public record FileSearchResult(
        UUID fileId,
        String fileName,
        String aiSummary,
        String aiTags,
        double relevanceScore
) {
}