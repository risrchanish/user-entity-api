package com.syncvault.file_service.ai;

import com.syncvault.file_service.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.UUID;

@Component
@Slf4j
public class FileAiCoordinator {

    private final S3Service s3Service;
    private final DocumentTextExtractor textExtractor;
    private final DocumentSummarizationService documentSummarizationService;
    private final EmbeddingService embeddingService;

    public FileAiCoordinator(S3Service s3Service,
                              DocumentTextExtractor textExtractor,
                              DocumentSummarizationService documentSummarizationService,
                              EmbeddingService embeddingService) {
        this.s3Service = s3Service;
        this.textExtractor = textExtractor;
        this.documentSummarizationService = documentSummarizationService;
        this.embeddingService = embeddingService;
    }

    public void processAsync(UUID fileId, UUID userId, String s3Key, String mimeType) {
        Thread.ofVirtual()
                .name("ai-extract-" + fileId)
                .start(() -> {
                    byte[] bytes = s3Service.downloadFile(s3Key);
                    String text;
                    try (InputStream in = new ByteArrayInputStream(bytes)) {
                        text = textExtractor.extractText(in, mimeType);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }

                    Thread.ofVirtual()
                            .name("ai-summary-" + fileId)
                            .start(() -> documentSummarizationService.processAndPersist(fileId, text));

                    Thread.ofVirtual()
                            .name("ai-embed-" + fileId)
                            .start(() -> embeddingService.generateAndStore(fileId, userId, text));
                });
    }
}