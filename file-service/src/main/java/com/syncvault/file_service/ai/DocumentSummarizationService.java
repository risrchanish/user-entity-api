package com.syncvault.file_service.ai;

import com.syncvault.aisdk.LLMClient;
import com.syncvault.aisdk.model.LLMResponse;
import com.syncvault.file_service.dto.AiDescriptionResult;
import com.syncvault.file_service.entity.FileMetadata;
import com.syncvault.file_service.repository.FileMetadataRepository;
import com.syncvault.file_service.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
public class DocumentSummarizationService {

    private static final int MAX_TEXT_LENGTH = 8000; // AC-019-10, next round

    private static final String SYSTEM_PROMPT = """
            You are a concise, factual document summarizer. Given the text of a document,
            respond with EXACTLY three lines, in this exact format, and nothing else:
            SUMMARY: <a 2-3 sentence summary>
            DESCRIPTION: <a one-line description, under 500 characters>
            TAGS: <3-5 relevant tags, comma-separated>
            """;

    private final FileMetadataRepository fileMetadataRepository;
    private final LLMClient llmClient;

    public DocumentSummarizationService(FileMetadataRepository fileMetadataRepository,
                                        LLMClient llmClient) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.llmClient = llmClient;
    }

    public void processAndPersist(UUID fileId, String extractedText) {
        FileMetadata fileMetadata = fileMetadataRepository.findById(fileId).orElse(null);
        if (fileMetadata == null) {
            log.warn("File {} not found during AI processing — likely deleted. Skipping.", fileId);
            return;
        }

        if (extractedText.isBlank()) {
            fileMetadata.setAiProcessedAt(LocalDateTime.now());
            fileMetadataRepository.save(fileMetadata);
            return;
        }

        String text = extractedText;
        if (text.length() > MAX_TEXT_LENGTH) {
            log.warn("Extracted text for file {} is {} chars, truncating to {} before LLM call.",
                    fileId, text.length(), MAX_TEXT_LENGTH);
            text = text.substring(0, MAX_TEXT_LENGTH);
        }

        try {
            LLMResponse response = llmClient.completeWithSystem(SYSTEM_PROMPT, text);
            AiDescriptionResult result = AiDescriptionResult.parse(response.content());

            fileMetadata.setAiSummary(result.summary());
            fileMetadata.setAiDescription(truncate(result.description(), 500));
            fileMetadata.setAiTags(truncate(result.tags(), 255));
        } catch (Exception ex) {
            log.error("AI processing failed for file {}: {}", fileId, ex.getMessage(), ex);
        }

        fileMetadata.setAiProcessedAt(LocalDateTime.now());
        fileMetadataRepository.save(fileMetadata);
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}